/*
 * SaveData.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.asyncTasks;

import java.io.File;

import android.os.AsyncTask;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.core.WeekData;
import de.janssen.android.gsoplan.xml.Xml;

public class SaveData extends AsyncTask<Boolean, Integer, Boolean>{
	private WeekData weekData;
	private File dataFile;
	private MyContext ctxt;
	public Exception exception;
	
	
	public SaveData(WeekData weekData,File file,MyContext ctxt)
	{
		this.weekData=weekData;
		this.dataFile=file;
		this.ctxt = ctxt;
	}
	
	protected Boolean doInBackground(Boolean... bool) {
		try
		{
			if (isCancelled()) 
				return null;
			
			String xmlContent = Xml.convertWeekDataToXml(weekData, ctxt.progressDialog);
			FileOPs.saveToFile(xmlContent,dataFile);
			weekData.isDirty=false;
		}
		catch(Exception e)
		{
			exception = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		ctxt.executor.scheduleNext();
	}

}

