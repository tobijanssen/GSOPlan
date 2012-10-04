package de.janssen.android.gsoplan;

import java.util.Calendar;

public class TimeTableIndex {
	public int indexKey;
	public Calendar date;
	public long syncTime = 0;
	
	public TimeTableIndex(int indexKey,Calendar date,long syncTime)
	{
		this.indexKey=indexKey;
		this.date=date;
		this.syncTime = syncTime;
	}
	

}
