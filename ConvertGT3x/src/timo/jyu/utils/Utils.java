package timo.jyu.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.text.ParsePosition;

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
}
