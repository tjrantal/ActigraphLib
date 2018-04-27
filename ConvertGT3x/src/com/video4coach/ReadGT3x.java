package com.video4coach;

import java.io.*;
import java.util.zip.*;
import java.util.*;	//ArrayList
import com.video4coach.gt3x.*;	//Info file parsing

/*
	A class for reading accelerations from a .gt3x file without creating temporary files in 	between
	.gt3x file is a zip file, and accelerations are bit-packed in the unzipped file. 
	Read in two steps
	1) get sample rate and scaling from info.txt 
	2) read data from data.bin subsequently
	
		This class is meant for data processing so handles data one day at a time starting from the first mignight in the file, and ending in the last. Discards the first and the last bits of data in the file

*/

public class ReadGT3x {
	public ActigraphInfo header;
	public ArrayList<Double> resultant;
	public ArrayList<Double> tStamps;

	public ReadGT3x(String fileIn){
		
		header = getHeader(fileIn);
		if (header == null){
			return;
		}
		//Debugging, log acc rate, and scaling factors
		log(String.format("Acc rate %d",header.getSampleRate()));
		log(String.format("AccScale %.3f",header.getAccelerationScale()));
		byte[] data = unzipData(fileIn);
		if (data == null){
			return;
		}
		log(String.format("Got data %d",data.length));
		LogBinReader lbr = new LogBinReader(data);
		log("Got lbr");
	}

	private static void log(String text) {
		System.out.println(text);
	}

	public static void main(String[] a){
		ReadGT3x gt = new ReadGT3x(a[0]);
	}

	private ActigraphInfo getHeader(String fileIn){
		ActigraphInfo header = null;
		try {
			//Open file inputstream
			FileInputStream fis = new FileInputStream(fileIn);
			// Create input stream that also maintains the checksum of the data
			CheckedInputStream checksum = new CheckedInputStream(fis,
					new Adler32());
			//Handle this as zip inputstream (which it is)
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
			ZipEntry entry;
			
			//Open outputstreams to store the bytes in
			int bufferSize  = 2048;
			int size;
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(fos,bufferSize);

			// Read each entry from the ZipInputStream until find info.txt
			while ((entry = zis.getNextEntry()) != null) {
				//log("Unzipping: " + entry.getName());

				if (! (entry.getName().equals("info.txt") == true) ){
					continue;	//Skip the rest of the loop -> getNextEntry();
				}else{
					break;	//Found the entry of interest
				}
				
			}
			
			//Read the file	
			log(String.format("Info.txt size %d bytes",(int) entry.getSize()));
			byte[] buffer = new byte[bufferSize];
			while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
				bos.write(buffer, 0, size);
				log(String.format("Read %d bytes",size));
			}
		
			bos.flush();
			String infoTxtString = new String(fos.toByteArray());
			log(infoTxtString);
			
			//Close streams			
			bos.close();
			zis.close();
			fis.close();
			
			//Parse infotxt here
			header = (new ActigraphInfoParser()).extractInfo(infoTxtString,true);
			
			// Print out the checksum value
			//log("Checksum = "+ checksum.getChecksum().getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return header;
	}

	private byte[] unzipData(String fileIn) {
		byte[] data = null;
		log("Unzip data "+fileIn);
		
		try {

			// Take the filename from the input arguments
			FileInputStream fis = new FileInputStream(fileIn);

			// Creating input stream that also maintains the checksum of the data 
			CheckedInputStream checksum = new CheckedInputStream(fis,new Adler32());
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
			ZipEntry entry;
			
			//Unzip the file into memory as bytes, handle bytes as the next step
			int bufferSize = (int) Math.pow(2d,11d);
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(fos,bufferSize);
			
			// Read each entry from the ZipInputStream until find info.txt
			while ((entry = zis.getNextEntry()) != null) {
				//log("Unzipping: " + entry.getName());

				if (!(entry.getName().equalsIgnoreCase("activity.bin") == true ||
						entry.getName().equalsIgnoreCase("log.bin") == true) ){

					continue;	//Skip the rest of the loop -> getNextEntry();
				}else{
					break;	//Found the entry of interest
				}
				
			}
			
			//Log.bin found
			log(String.format("Binary file size %d bytes",(int) entry.getSize()));
			
			int size;
			byte[] buffer = new byte[bufferSize];
			while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
				bos.write(buffer, 0, size);
				//log(String.format("Read %d bytes",size));
			}
			
			bos.flush();
			data = fos.toByteArray();
			log(String.format("Data in memory %d",data.length));
						
			//Close streams			
			bos.close();
			zis.close();
			fis.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public void decodeData(byte[] data){
		
		
	}

}
