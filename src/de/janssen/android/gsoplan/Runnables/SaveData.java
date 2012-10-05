package de.janssen.android.gsoplan.Runnables;

import java.io.File;
import java.util.Calendar;

import android.content.Context;
import de.janssen.android.gsoplan.FileOPs;
import de.janssen.android.gsoplan.StupidCore;
import de.janssen.android.gsoplan.WeekData;
import de.janssen.android.gsoplan.Xml;

public class SaveData implements Runnable{
	private Context context;
	private StupidCore stupid;
	private File dataFile;
	public Exception exception;
	
	
	public SaveData(Context context,StupidCore stupid,File file)
	{
		this.context=context;
		this.stupid=stupid;
		this.dataFile=file;
	}
	
	@Override
	public void run() {
		try
		{
			//den Index aktualisieren
			stupid.timeTableIndexer();
			int index = stupid.getIndexOfWeekData(stupid.currentDate);
			WeekData weekData = stupid.stupidData[index];
			String xmlContent = Xml.convertWeekDataToXml(weekData,stupid.progressDialog);
			FileOPs.saveToFile(context,xmlContent,dataFile);
	   		stupid.dataIsDirty=false;

	   		if(!stupid.setupIsDirty && !stupid.dataIsDirty)
	   		{
	   			stupid.progressDialog.dismiss();
	   		}
		}
		catch(Exception e)
		{
			exception = e;
		}
	}

}

