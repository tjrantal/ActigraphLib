/*
The software is licensed under a Creative Commons Attribution 3.0 Unported License.
Copyright (C) 2015 - 2018 Timo Rantalainen, tjrantal at gmail dot com
*/

/*
	Actigraph GT3X+ file reader (https://github.com/actigraph/GT3X-File-Format) for the log.bin data
	Specifically written for Eija Laakkonen's & Vuokko Kovanen's ERMA project actigraph data.
	Calculates Vähä-Ypyä's mean amplitude deviation values per second, and extract each accelerometry peak above 1.3 g
*/

package com.video4coach;
import java.io.*;
import java.util.*;

import com.video4coach.gt3x.ActigraphInfo;	//Info file parsing

import timo.jyu.utils.Utils;	//ERMA-specific utility functions
import timo.jyu.utils.Value;	//Helper class to store mad and peak values

public class ErmaReader{
	private byte[] data;
	public ArrayList<Value> mads;	//Holds one second Mad values, and corresponding timestamps
	public ArrayList<Value> peaks; //Holds peaks and corresponding timestamps (uses the start of the peak as the instant)
	private Locale locale;
	

	
	
	public ErmaReader(String fileName,ActigraphInfo header,Locale locale){
		int dataLength =0;
		this.locale = locale;
		try {
			/*Read the file into memory (make sure you've got sufficient memory available...)*/
			DataInputStream di = new DataInputStream( new FileInputStream(fileName));
			//System.out.println(di.available());
			dataLength =di.available();
			data = new byte[dataLength];	//Reserve memory for the file data
			di.readFully(data);	/*Read the file into memory*/
			di.close();	//Close the file
		} catch (Exception err){System.err.println("Error: "+err.getMessage());}
		decodeData(data,header.getAccelerationScale());
	}
	
	public ErmaReader(byte[] data,ActigraphInfo header,Locale locale){
		this.locale = locale;
		decodeData(data,header.getAccelerationScale());
		//System.out.println("Decoded data "+accelerations[0].length);
	}
	
	
	public void decodeData(byte[] data,double aScale){
		int dataLength = data.length;
		int maxArrayLength = (int) (((double) dataLength)*8d/(12d*3d));
		//System.out.println("Max array length "+maxArrayLength);
		ArrayList<ArrayList<Short>> tempAccelerations  = new ArrayList<ArrayList<Short>>();	//Y, X, Z
		
		ArrayList<Value> resultant = new ArrayList<Value>();	//Used to hold resultant
	   mads = new ArrayList<Value>();
		peaks = new ArrayList<Value>();
		
		
		for (int i = 0; i<3;++i){
			tempAccelerations.add(new ArrayList<Short>(maxArrayLength));
		}
		
		/*Read packages here
			package format https://github.com/actigraph/GT3X-File-Format
			offset
			0 1 byte	= separator 1E
			1 1 byte	= package ID, I'm only interested in 0, which is activity https://github.com/actigraph/GT3X-File-Format/blob/master/LogRecords/Activity.md
			2 4 bytes	= timestamp in Unix time format (4 bytes)
			6 2 bytes 	= Size (n) uint16
			8 n bytes	= Payload
			8+4 1 byte	= Checksum
			
		*/
		int pointer = 0;	//used to know where we are in the file data
		LogRecord logrecord;
		int pointerIncrementTargetForGC = dataLength/25;
		int pointerIncrement = 0;
		long currentTStamp = -1;
		int prevMidnight = Integer.MAX_VALUE;
		int nextMidnight = -1;
		int cnt = 0;
		while (pointer < dataLength){
			//Check that parsing is successful, should have 1E to indicate start of package
			logrecord = new LogRecord(data,pointer);
			//System.out.println("P "+pointer);
			/*Extract accelerations here*/
			if (logrecord.type == 0x00){
				if (nextMidnight < 0){
					long tempMidnight = Utils.getNextMidnight(1000l*((long) logrecord.timeStamp), locale);
					prevMidnight = (int) (tempMidnight/1000l);	//Convert back to actigraph tStamps
					//System.out.println("Midnight +1");
					nextMidnight = (int) (Utils.getNextMidnight(tempMidnight,locale)/1000l);
				}
				
				//Have a full day of data in memory, calculate one second MADs, and 
				if (logrecord.timeStamp >= nextMidnight){
					++cnt;
					System.out.println("\nFound full day of data resultant size "+resultant.size());
					//Calculate MADs
					mads.addAll(Utils.getMads(resultant));
					//System.out.println("Mads size "+mads.size());
					//Detect peaks NEEDS TO BE IMPLEMENTED!!!
					peaks.addAll(Utils.getPeaks(resultant));
										
					//Reset raw data
					resultant.clear();
					for (int d = 0;d<tempAccelerations.size(); ++d){
						tempAccelerations.get(d).clear();
					}

					//Update midnight time stamps
					prevMidnight = nextMidnight;
					nextMidnight = (int) (Utils.getNextMidnight(1000l*((long) nextMidnight),locale)/1000l);
				}
				
				
				//Worry about this data if it is between midnights
				if (logrecord.timeStamp >= prevMidnight && logrecord.timeStamp < nextMidnight){
			
					int valuesInRecord = (int) logrecord.payload.length*8/12;
					int valuesExtracted = 0;
					int direction = 0;	//Used to keep track of which dimension to extract
					//Loop through the payload to extract the accelerometry values
					short valueBits;
					int payloadPointer = 0;
					while (valuesExtracted <valuesInRecord){
						//The values are 12 bit back-to-back -> 2 values take 3 bytes
						if (valuesExtracted % 2 == 0){
							valueBits =(short) (((logrecord.payload[payloadPointer]& 0xff)<<4)| ((logrecord.payload[payloadPointer+1] & 0xf0)>>4));
						} else {
							valueBits =(short) (((logrecord.payload[payloadPointer]& 0x0f)<<8)| (logrecord.payload[payloadPointer+1] & 0xff));
							++payloadPointer;
						}
						++valuesExtracted;
						++payloadPointer;
						if (valueBits > 2047){
							valueBits |=0xf000;	//Set the sign
						}
						//Assign the value to the correct dimension
						tempAccelerations.get(direction).add(valueBits);
						++direction;
						if (direction>2){
							direction = 0;
							//Add resultant, and corresponding time stamp
							resultant.add(new Value(logrecord.timeStamp,
								Math.sqrt(
	Math.pow(((double) tempAccelerations.get(0).get(tempAccelerations.get(direction).size()-1))/aScale,2d)+
	Math.pow(((double) tempAccelerations.get(1).get(tempAccelerations.get(direction).size()-1))/aScale,2d)+
	Math.pow(((double) tempAccelerations.get(2).get(tempAccelerations.get(direction).size()-1))/aScale,2d))
							));

						}
						

					}
				}
			}
			pointerIncrement += logrecord.nextRecordPointer-pointer;
			pointer = logrecord.nextRecordPointer;
			System.out.print("Processed \t"+((int) (((double)pointer)/((double)dataLength)*100d))+"\r");
		}
		return;
	}
	
	private int min(int[] a){
		return min(min(a[0],a[1]),a[2]);
	}
	
	private int min(int a,int b){
		return a < b ? a:b;
	}
	/*Helper class to contain log record*/
	public class LogRecord{
		public int separator;
		public int type;
		public int timeStamp;
		public int size;
		public byte[] payload;
		public int checksum;
		public int nextRecordPointer;
		LogRecord(byte[] data, int pointer){
			separator	= data[pointer];
			type		= data[pointer+1];
			timeStamp	= (int) (((0xff & data[pointer+5])<<24) | ((0xff & data[pointer+4])<<16) | ((0xff & data[pointer+3])<<8) | (0xff & data[pointer+2]));
			size		=(int) (((0xff & data[pointer+7])<<8) | (0xff & data[pointer+6]));
			payload = Arrays.copyOfRange(data,pointer+8,pointer+8+size);	//Copy the record payload
			checksum = data[pointer+8+size];
			nextRecordPointer = pointer+8+size+1;
		}
	}

}
