/*
 * TimeTableIndex.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.dataclasses;

import java.util.Calendar;

public class TimeTableIndex
{
    public int indexKey;
    public Calendar date;
    public long syncTime = 0;

    public TimeTableIndex(int indexKey, Calendar date, long syncTime)
    {
	this.indexKey = indexKey;
	this.date = date;
	this.syncTime = syncTime;
    }

}
