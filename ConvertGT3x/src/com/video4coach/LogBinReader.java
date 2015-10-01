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
public class LogBinReader{
	private byte[] data;
	public ArrayList<ArrayList<Short>> accelerations;	//Y, X, Z
	public LogBinReader(String fileName){
		try {
			/*Read the file into memory (make sure you've got sufficient memory available...)*/
			DataInputStream di = new DataInputStream( new FileInputStream(fileName));
			//System.out.println(di.available());
			data = new byte[di.available()];	//Reserve memory for the file data
			di.readFully(data);	/*Read the file into memory*/
			di.close();	//Close the file
		} catch (Exception err){System.err.println("Error: "+err.getMessage());}
		accelerations = new ArrayList<ArrayList<Short>>();
		for (int i = 0; i<3;++i){
			accelerations.add(new ArrayList<Short>());
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
		while (pointer < data.length){
			//Check that parsing is successful, should have 1E to indicate start of package
			LogRecord logrecord = new LogRecord(data,pointer);
			System.out.println("P "+pointer);
			/*Extract accelerations here*/
			if (logrecord.type == 0x00){
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
					accelerations.get(direction).add(valueBits);
					++direction;
					if (direction>2){
						direction = 0;
					}
				}
				for (int j = 0;j<accelerations.size();++j){
						System.out.print(accelerations.get(j).size()+"\t");
				}
				System.out.println("\t");
				System.out.println("ind\ty\tx\tz\t");
				for (int i = 0;i<accelerations.get(0).size();++i){
					System.out.print(i+"\t");
					for (int j = 0;j<accelerations.size();++j){
						System.out.print(accelerations.get(j).get(i)+"\t");
					}
					System.out.println("");
				}
				//System.out.println("Found activity data, pointer "+pointer+" values "+valuesInRecord);
				
				break;
			}
			pointer = logrecord.nextRecordPointer;
		}
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
	
	public static void main(String[] a){
		LogBinReader lbr = new LogBinReader(a[0]);
	}
}