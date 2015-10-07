/*
The software is licensed under a Creative Commons Attribution 3.0 Unported License.
Copyright (C) 2015 Timo Rantalainen, tjrantal at gmail dot com
*/

/*
	Actigraph GT3X+ file reader (https://github.com/actigraph/GT3X-File-Format) for the log.bin data
*/

package com.video4coach;
import java.io.*;
import java.util.*;

public class LogBinFileReader{
	private byte[] data;
	private ArrayList<ArrayList<Short>> tempAccelerations;	//Y, X, Z
	public short[][] accelerations;	//This will be returned to matlab
	private byte[] sensorValue;
	public byte[] headerData;	//To be used by the subclass LogRecordHeader
	public byte[] shortBytes;
	public byte[] tStampBytes;
	public LogBinFileReader(String[] args){
		sensorValue = new byte[3];
		headerData = new byte[8];
		shortBytes = new byte[2];
		tStampBytes = new byte[4];
		try {
			/*Read the file into memory (make sure you've got sufficient memory available...)*/
			FileInputStream fi = new FileInputStream(args[0]);
			BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(args[1]));	//Spit the results out to a binary file
			//BufferedWriter bf = new BufferedWriter(new FileWriter(args[1]));
			//System.out.println(fi.available());
			int dataLength =fi.available();
			
			//fi.readFully(data);	/*Read the file into memory*/
			
			/*
			tempAccelerations = new ArrayList<ArrayList<Short>>();
			int maxArrayLength = (int) (((double) dataLength)*8d/(12d*3d));
			System.out.println("Max array length "+maxArrayLength);
			for (int i = 0; i<3;++i){
				tempAccelerations.add(new ArrayList<Short>(maxArrayLength));
			}
			*/
			
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
			LogBinFileReader.LogRecordHeader logrecord;
			int pointerIncrement = 0;
			//bf.write("y\tx\tz\n");
			while (fi.available() > 0){
				//Check that parsing is successful, should have 1E to indicate start of package
				logrecord = this.new LogRecordHeader(fi);
				//System.out.println("LrecordType "+logrecord.type);
				//System.out.println("P "+pointer);
				/*Extract accelerations here*/
				/*JATKA TASTA, Modify implementation to use data instead of logrecord copy??*/
				if (logrecord.type == 0x00){
					int valuesInRecord = (int) logrecord.size*8/12;
					int valuesExtracted = 0;
					int direction = 0;	//Used to keep track of which dimension to extract
					//Loop through the payload to extract the accelerometry values
					short valueBits;
					while (valuesExtracted <valuesInRecord){
						//The values are 12 bit back-to-back -> 2 values take 3 bytes
						if (valuesExtracted % 2 == 0){
							fi.read(sensorValue);	 //Read the next 3 bytes
							valueBits =(short) (((sensorValue[0]& 0xff)<<4)| ((sensorValue[1] & 0xf0)>>4));
						} else {
							valueBits =(short) (((sensorValue[1]& 0x0f)<<8)| (sensorValue[2] & 0xff));
						}
						++valuesExtracted;
						if (valueBits > 2047){
							valueBits |=0xf000;	//Set the sign
						}
						//Assign the value to the correct dimension
						//tempAccelerations.get(direction).add(valueBits);
						++direction;
						shortBytes[0] = (byte) (valueBits & 0x00FF);	//LSB first (java is MSB)
						shortBytes[1] = (byte) ((valueBits & 0xFF00)>>8);	//MSB second
						bo.write(shortBytes);

						if (direction>2){
							direction = 0;
							//Write time stamp
							
							tStampBytes[0] = (byte) (logrecord.timeStamp &	0x000000FF);	//LSB first (java is MSB)
							tStampBytes[1] = (byte) ((logrecord.timeStamp &	0x0000FF00)>>8);	//MSB second
							tStampBytes[2] = (byte) ((logrecord.timeStamp &	0x00FF0000)>>16);	//LSB first (java is MSB)
							tStampBytes[3] = (byte) ((logrecord.timeStamp &	0xFF000000)>>24);	//LSB first (java is MSB)
							bo.write(tStampBytes);
							//bf.write(valueBits+"\n");
						}else{
							//bf.write(valueBits+"\t");
						}
					}
					/*
					for (int j = 0;j<tempAccelerations.size();++j){
							System.out.print(tempAccelerations.get(j).size()+"\t");
					}
					System.out.println("\t");
					System.out.println("ind\ty\tx\tz\t");
					for (int i = 0;i<tempAccelerations.get(0).size();++i){
						System.out.print(i+"\t");
						for (int j = 0;j<tempAccelerations.size();++j){
							System.out.print(tempAccelerations.get(j).get(i)+"\t");
						}
						System.out.println("");
					}
					//System.out.println("Found activity data, pointer "+pointer+" values "+valuesInRecord);
					fi.close();
					return;
					*/
				}else{
					//Skip this log record
					fi.skip(logrecord.size);
				}
				//discard checksum
				fi.skip(1);	//Read one byte (checksum)
				System.out.print("Processed \t"+((int) ((1d-((double)fi.available())/((double)dataLength))*100d))+"\r");
			}
			bo.close();	//Done writing...
			//bf.close();	//Done writing...
			fi.close();	//Close the file
		} catch (Exception err){System.err.println("Error: "+err.getMessage());}
		/*
		//Get the results into a primitive short array
		int arrayLength = min(new int[]{tempAccelerations.get(0).size(),tempAccelerations.get(1).size(),tempAccelerations.get(2).size()});
		for (int j = 0;j<tempAccelerations.size();++j){
			accelerations[j] = new short[arrayLength];
			for (int i = 0;i<arrayLength;++i){
				accelerations[j][i] = tempAccelerations.get(j).get(i);
			}
			tempAccelerations.get(j).clear();
		}
		*/
	}
	
	private int min(int[] a){
		return min(min(a[0],a[1]),a[2]);
	}
	
	private int min(int a,int b){
		return a < b ? a:b;
	}
	/*Helper class to contain log record header*/
	public class LogRecordHeader{
		public int separator;
		public int type;
		public int timeStamp;
		public int size;
		//public byte[] headerData;
		LogRecordHeader(FileInputStream fi){
			//headerData = new byte[8];
			try{
				fi.read(headerData);
			}catch (Exception err){System.err.println("Error: "+err.getMessage());}
			separator	=headerData[0];
			type		= headerData[1];
			timeStamp	= (int) (((0xff & headerData[5])<<24) | ((0xff & headerData[4])<<16) | ((0xff & headerData[3])<<8) | (0xff & headerData[2]));
			size		=(int) (((0xff & headerData[7])<<8) | (0xff & headerData[6]));
		}
	}
	
	public static void main(String[] a){
		LogBinFileReader lbr = new LogBinFileReader(a);
	}
}