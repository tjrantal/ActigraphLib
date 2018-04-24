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

*/

public class ReadGT3x {
	public ActigraphInfo header;
	public ArrayList<Double> resultant;
	public ArrayList<Double> tStamps;

	public ReadGT3x(String fileIn){
		
		header = getHeader(fileIn);
		//Debugging, log acc rate, and scaling factors
		log(String.format("Acc rate %d",header.getSampleRate()));
		log(String.format("AccScale %.3f",header.getAccelerationScale()));
		resultant = getResultant(fileIn);
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
				}
				log(String.format("Info.txt size %d bytes",(int) entry.getSize()));
				byte[] buffer = new byte[bufferSize];
				while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
					log(String.format("Read %d bytes",size));
				}
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

	private ArrayList<Double> getResultant(String fileIn) {
		ArrayList<Double> resultant = null;
		try {

			// Take the filename from the input arguments
			FileInputStream fis = new FileInputStream(fileIn);

			//
			// Creating input stream that also maintains the checksum of the data 
			CheckedInputStream checksum = new CheckedInputStream(fis,new Adler32());
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
			ZipEntry entry;
			int bufferSize  = 4095;
			int size;
			//
			// Read each entry from the ZipInputStream until activity.bin or log.bin is found
			while ((entry = zis.getNextEntry()) != null) {
				System.out.println("Unzipping: " + entry.getName());

				if (! (entry.getName().equalsIgnoreCase("activity.bin") == true ||
						entry.getName().equalsIgnoreCase("log.bin") == true) ){
					continue;	//Ignore the rest of the loop -> zis-getNextEntry()
				}
				byte[] buffer = new byte[bufferSize];
				long bytesOfData = entry.getSize();
				long bytesRead = 0l;
				
				//LogBinFileReader implementation for reading the data.
				//Get scaling from info.txt
				
				while (bytesRead < bytesOfData){
					size = zis.read(buffer, 0, buffer.length);
					bytesRead += (long) size;
					//log(String.format("Read returned %d bytes",size));
					
					//Decode buffer, calculate resultant, and add to resultant
					
				}
				/*
				//binary data file found if got this far
				int size;
				byte[] buffer = new byte[2048];

				FileOutputStream fos = new FileOutputStream(args[1]+entry.getName());
				BufferedOutputStream bos = new BufferedOutputStream(fos,
						buffer.length);

				while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}
				bos.flush();
				bos.close();
				*/
			}

			zis.close();
			fis.close();

			//
			// Print out the checksum value
			//
			System.out.println("Checksum = "
					+ checksum.getChecksum().getValue());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return resultant;
	}

}
