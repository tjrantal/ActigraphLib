package com.video4coach.gt3x;

/*Helper class to contain log record header*/
public class LogRecordHeader{
	public int separator;
	public int type;
	public int timeStamp;
	public int size;
	//Call this with 8 bytes of data
	LogRecordHeader(byte[] headerData){

		separator	=headerData[0];
		type		= headerData[1];
		timeStamp	= (int) (((0xff & headerData[5])<<24) | ((0xff & headerData[4])<<16) | ((0xff & headerData[3])<<8) | (0xff & headerData[2]));
		size		=(int) (((0xff & headerData[7])<<8) | (0xff & headerData[6]));
	}
}
