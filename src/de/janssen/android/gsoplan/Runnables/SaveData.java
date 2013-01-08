package de.janssen.android.gsoplan.runnables;

import java.io.File;

import android.os.AsyncTask;
import de.janssen.android.gsoplan.FileOPs;
import de.janssen.android.gsoplan.WeekData;
import de.janssen.android.gsoplan.Xml;

public class SaveData extends AsyncTask<Boolean, Integer, Boolean>{
	private WeekData weekData;
	private File dataFile;
	public Exception exception;
	
	
	public SaveData(WeekData weekData,File file)
	{
		this.weekData=weekData;
		this.dataFile=file;
	}
	
	protected Boolean doInBackground(Boolean... bool) {
		try
		{
			if (isCancelled()) 
				return null;
			
			String xmlContent = Xml.convertWeekDataToXml(weekData, weekData.parent.progressDialog);
			FileOPs.saveToFile(xmlContent,dataFile);
			weekData.isDirty=false;
		}
		catch(Exception e)
		{
			exception = e;
		}
		return null;
	}

}

