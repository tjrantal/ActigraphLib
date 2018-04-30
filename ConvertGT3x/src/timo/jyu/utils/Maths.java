/**Static functions for basic math*/
package timo.jyu.utils;


import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Maths{
	
	public static double calcMad(ArrayList<Double> a){
		if (a.size() > 0){
			double[] b = new double[a.size()];
			for (int i = 0; i<a.size();++i){
				b[i] = a.get(i);
			}		
			return MAD(b,true);
		}else{
			return Double.NaN;
		}
	}
	//Calculate mean
	public static double mean(double[] a){
		return sumOfVector(a)/((double) a.length);
	}
	
	/*Get the sum of vector*/
	public static double sumOfVector(double[] a){
		double b = 0;
		for (int i = 0; i<a.length; ++i){
			b+=a[i];
		}
		return b;
	}
	

	
	//Calculate MaD
	public static double MAD(double[] a){
		return mean(rectify(a));			
	}
			
	//Calculate MaD
	public static double MAD(double[] a,boolean b){
		double[] c = a;
		if (b){
			c = meanSubtract(a);
		}
		return MAD(c);			
	}

	public static double[] rectify(double[] a){
		double[] b = new double[a.length];
		for (int i = 0;i<a.length;++i){
			b[i] = Math.abs(a[i]);
		}
		return b;			
	}
	
	public static double[] meanSubtract(double[] a){
		double[] b = new double[a.length];
		double meanVal = mean(a);
		for (int i = 0;i<a.length;++i){
			b[i] = a[i]-meanVal;
		}
		return b;			
	}
	
}
