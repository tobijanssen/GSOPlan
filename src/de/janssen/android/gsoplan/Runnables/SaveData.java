package de.janssen.android.gsoplan.runnables;

import java.io.File;
import android.content.Context;
import de.janssen.android.gsoplan.FileOPs;
import de.janssen.android.gsoplan.WeekData;
import de.janssen.android.gsoplan.Xml;

public class SaveData implements Runnable{
	private Context context;
	private WeekData weekData;
	private File dataFile;
	public Exception exception;
	
	
	public SaveData(Context context,WeekData weekData,File file)
	{
		this.context=context;
		this.weekData=weekData;
		this.dataFile=file;
	}
	
	@Override
	public void run() {
		try
		{
			//den Index aktualisieren
			//stupid.timeTableIndexer();
			//int index = stupid.getIndexOfWeekData(stupid.currentDate);
			//WeekData weekData = stupid.stupidData.get(index);
			String xmlContent = Xml.convertWeekDataToXml(weekData, weekData.parent.progressDialog);
			FileOPs.saveToFile(context,xmlContent,dataFile);
			weekData.isDirty=false;
		}
		catch(Exception e)
		{
			exception = e;
		}
	}

}

