/*
 * Logger.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;
import android.util.Log;

public class Logger
{
    private Context ctxt;
    private Calendar cal;
    private Level debugLogLevel = Level.OFF;	//Hier festlegen in welchem LogLevel sich die Anwendung befindet 
    private Boolean stackTraces = true;
    
    public enum Level{DEBUG,INFO_1,INFO_2,WARNING,ERROR, OFF};
    
    public Logger(Context ctxt)
    {
	this.ctxt=ctxt;
    }
    /**
     * @param msgCatLogLevel
     * @param message
     */
    public void log(Level level, String message)
    {
	if(level.compareTo(debugLogLevel) >= 0)
	{
	    try
	    {
		File logFile = new File(ctxt.getFilesDir(), "log.txt");
		FileWriter fw;
		fw = new FileWriter(logFile, true);
		fw.append(getCurrentDate() + message + "\n");
		fw.close();
	    }
	    catch (IOException e)
	    {

	    }

	}
    }
    
    /**
     * @param msgCatLogLevel
     * @param message
     * @param suppressDate	Unterdürckt Datumsangaben
     */
    public void log(Level level, String message,Boolean suppressDate)
    {
	if(level.compareTo(debugLogLevel) >= 0)
	{
	    try
	    {
		File logFile = new File(ctxt.getFilesDir(), "log.txt");
		FileWriter fw;
		fw = new FileWriter(logFile, true);
		if (suppressDate)
		    fw.append(message + "\n");
		else
		    fw.append(getCurrentDate() + message + "\n");
		fw.close();
	    }
	    catch (IOException e)
	    {

	    }
	}
    }
    /**
     * @param msgCatLogLevel
     * @param message
     * @param exception
     */
    public void log(Level level, String message,Exception exception)
    {
	if(level.compareTo(debugLogLevel) >= 0)
	{
	    try
	    {

		File logFile = new File(ctxt.getFilesDir(), "log.txt");
		FileWriter fw;
		fw = new FileWriter(logFile, true);
		fw.append(getCurrentDate() + message+" ");
		fw.append(exception.getMessage() + "\n");
		if(this.stackTraces)
		    fw.append("Stack-Trace:\n\n"+Log.getStackTraceString(exception)+"\n");
		fw.close();
	    }
	    catch (IOException e)
	    {

	    }
	}
	
    }
    
    /**
     * 
     * @return
     */
    private String getCurrentDate()
    {
	cal = new GregorianCalendar();
	
	String minute = java.lang.String.valueOf(cal.get(Calendar.MINUTE));
	if (minute.length() == 1)
	    minute = "0" + minute;
	String second = java.lang.String.valueOf(cal.get(Calendar.SECOND));
	if (second.length() == 1)
	    second = "0" + second;
	String hour = java.lang.String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	if (hour.length() == 1)
	    hour = "0" + hour;
	String month = java.lang.String.valueOf(cal.get(Calendar.MONTH)+1);
	if (month.length() == 1)
	    month = "0" + month;
	String day = java.lang.String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	if (day.length() == 1)
	    day = "0" + day;
	
	
	return day+"."+month+". "+hour+"h"+minute+"m"+second+"s ";
    }

}
