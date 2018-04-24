package com.video4coach.gt3x;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActigraphInfoParser {
		
	protected ActigraphInfo header = new ActigraphInfo();		
	
	//This var mapps the date format to the right type
	private int dateMapping[][] = { {0,1,2}, //day month year
									{1,0,2}, //month day year
									{2,0,1}, //year day month
									{2,1,0} }; //year month day
	
	public ActigraphInfoParser(){
	    
	    
	  }
	
	public int findStringIndex(String [] tokens, String text) {
		int idx = -1;
		
		for (int i = 0;i< tokens.length; i++ ) {
			if (text.equals(tokens[i]))
				return i;
		}
		
		return idx;		
	}
	
	private boolean hasExtention(String filename) {
		boolean hasExtention = false;
    	if (filename.length()>4) {
    		if (filename.charAt(filename.length()-4)=='.')
    			hasExtention = true;
    		if (filename.charAt(filename.length()-3)=='.')
    			hasExtention = true;
    	}
		
		return hasExtention;
	}
	
	
	/** Call extractInfo with fileName */
	public final ActigraphInfo extractInfo(String aFileName) throws FileNotFoundException, ParseException {
		return extractInfo(new Scanner(new File(aFileName)));
	}

	/** Call extractInfo with info file text string */
	public final ActigraphInfo extractInfo(String textFileString,boolean isTextFileString) throws FileNotFoundException, ParseException {
		return extractInfo(new Scanner(textFileString));
	}
	
	
	/** Template method that calls {@link #processLine(String)}. 
	 * @throws ParseException */
	public final ActigraphInfo extractInfo(Scanner scanner) throws FileNotFoundException, ParseException {
		/*
		File fFile = new File(aFileName); 
	    
	    String delimiter = "[\\"+ File.separator + " .]";
	    String [] tokens = aFileName.split(delimiter);
		Scanner scanner = new Scanner(fFile);
			*/	
		
		try {
			// first use a Scanner to get each line
			
			
			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();

				String lineDelimiter = ":";				
				
				String[] tokens = line.split(lineDelimiter);

				/*
				Serial Number: NEO1F07120490
				Device Type: GT3XPlus
				Firmware: 3.2.1
				Battery Voltage: 3.91
				Sample Rate: 50
				Start Date: 635185437600000000
				Stop Date: 0
				TimeZone: 01:00:00
				Download Date: 635185440370000000
				Board Revision: 5
				Unexpected Resets: 0
				Sex: Male
				Height: 172
				Mass: 70
				Age: 45
				Race: White / Caucasian
				Limb: Waist
				Side: Right
				Dominance: Dominant
				DateOfBirth: 620796379200000000
				Subject Name: JANBROND

				 */

				Pattern pattern = Pattern.compile("[0-9]+");

				Matcher matcher = pattern.matcher(line);
				
				int idx = 0;

				//Old serial number
				if (line.indexOf("Serial Number") >= 0) {
					// Extract the serial number of the accelrometer					
				    
				    if (matcher.find()) {				    	
				    	String Serial = matcher.group();
				    	header.setSerialNumber(Serial);
				    }

				}
				
				//Old firmaware version number
				if (line.indexOf("Ver") >= 0) {
					// The version of the firmaware
							
					idx = findStringIndex(tokens,"Ver");
					
					if (idx>=0 && idx<tokens.length) {
						header.setFirmwareVersion(tokens[idx+1]);
					}
					
				}
				
				/*TimeZone*/
				if (line.indexOf("TimeZone") == 0) {					
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						header.setTimeZone(val);
					}
				}
				
				/*Unexpected Resets*/
				if (line.indexOf("Unexpected Resets") == 0) {					
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						header.setUnexpectedResets(Integer.parseInt(val));
					}
				}
				
				/*Last Sample Time*/
				if (line.indexOf("Last Sample Time") == 0) {					
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						header.setLastSampleTime(Long.parseLong(val));
					}
				}
				
				/*Stop Date*/
				if (line.indexOf("Stop Date") == 0) {					
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						header.setStopDate(Long.parseLong(val));
					}
				}
				
				/*Download Date*/
				if (line.indexOf("Download Date") == 0) {					
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						header.setDownloadDate(Long.parseLong(val));
					}
				}
				
				/*Start Date*/
				if (line.indexOf("Start Date") == 0) {					
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						header.setStartDate(Long.parseLong(val));
					}
				}
				
				/*Sample Rate*/
				if (line.indexOf("Sample Rate") == 0) {
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						
						header.setSampleRate(Integer.parseInt(val));
					}
					
				}
				
				/*Subject Name*/
				if (line.indexOf("Subject Name") == 0) {
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						header.setSubjectName(val);
					}
					
				}
				
				/*AccelerationScale*/
				if (line.indexOf("Acceleration Scale") >= 0) {
					// ok found
					if (matcher.find()) {
						String val = matcher.group();
						
						header.setAccelerationScale(Double.parseDouble(val));
					}
					
				}
			}
		} finally {
			// ensure the underlying stream is always closed
			scanner.close();
		}

		return header;
	}
}
