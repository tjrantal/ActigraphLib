package timo.jyu.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.text.ParsePosition;
import java.util.ArrayList;

/*Utility class to work with dates, and to calculate MADs and detect peaks*/

public class Utils{

	public static long getNextMidnight(long tStamp, Locale locale){
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",locale); //Create simple date format
     	Date date = new Date(tStamp);
    	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM,locale);
    	String savedStartDate = df.format(date);
    	
    	//System.out.println("savedStartDate "+savedStartDate);
		String[] temp = savedStartDate.split(" ");
		String firstPriorMidnight = temp[0] + " 00:00:00";
		//System.out.println("firstPriorMidnight "+firstPriorMidnight);
		
		Date tempDate = sdf.parse(firstPriorMidnight, new ParsePosition(0));   //Parse the dateString
		Calendar calendar = Calendar.getInstance(locale); 
		calendar.setTime(tempDate);
		calendar.add(Calendar.DATE,1);

		String stringStamp = df.format(calendar.getTime());
		//System.out.println("First midnight "+stringStamp);
		return calendar.getTimeInMillis();  //Java ms timestamp
		
	}
	
	/**Go through the data. Get any peak over 1.3 g, 
		Add the time stamp and peak value to the return array.*/
	public static ArrayList<Value> getPeaks(ArrayList<Value> resultant){
		ArrayList<Value> peaks = new ArrayList<Value>();
		ArrayList<Double> temp = new ArrayList<Double>();
		long tStamp = resultant.get(0).tStamp;
		int cnt = 0;
		while (cnt < resultant.size()){
			//If peak initiation
			if (resultant.get(cnt).value >= 1.3){
				//We've found a peak
				double maxVal = Double.NEGATIVE_INFINITY;
				long peakStamp = 0;
				while (cnt < resultant.size() && resultant.get(cnt).value >= 1.3){
					if (resultant.get(cnt).value > maxVal){
						maxVal = resultant.get(cnt).value;
						peakStamp = resultant.get(cnt).tStamp;
					}
					++cnt;
				}
				//Gone past the peak
				peaks.add(new Value(peakStamp,maxVal));

			}else{
				++cnt;
			}
		}
		return peaks;
	}
	
	public static ArrayList<Value> getMads(ArrayList<Value> resultant){
		ArrayList<Value> mads = new ArrayList<Value>();
		ArrayList<Double> temp = new ArrayList<Double>();
		long tStamp = resultant.get(0).tStamp;
		int cnt = 0;
		while (cnt < resultant.size()){
			//Add data to temp
			if (resultant.get(cnt).tStamp > tStamp){
				//calc mad here, add to mads, increment tStamp, and clear temp
				//System.out.println("Next second temp size "+temp.size());
				mads.add(new Value(tStamp,Maths.calcMad(temp)));
				tStamp = resultant.get(cnt).tStamp;
				temp.clear();

			}
			temp.add(resultant.get(cnt).value);
			++cnt;
		}
		
		//System.out.println("Got through resultant");
		//add the final second of data
		if (temp.size() > 1){
			mads.add(new Value(tStamp,Maths.calcMad(temp)));
		}
		//System.out.println("Return mads");
		return mads;
	}
	

}
