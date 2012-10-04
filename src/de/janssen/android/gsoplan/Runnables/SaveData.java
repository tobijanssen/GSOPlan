package de.janssen.android.gsoplan.Runnables;

import android.content.Context;
import de.janssen.android.gsoplan.File;
import de.janssen.android.gsoplan.StupidCore;
import de.janssen.android.gsoplan.Xml;

public class SaveData implements Runnable{
	private Context context;
	private StupidCore stupid;
	private String fileDataFile;
	public Exception exception;
	
	
	public SaveData(Context context,StupidCore stupid,String fileSetupFile)
	{
		this.context=context;
		this.stupid=stupid;
		this.fileDataFile=fileSetupFile;
	}
	
	@Override
	public void run() {
		try
		{
			File.saveToFile(context, Xml.convertStupidToXml(stupid,stupid.progressDialog), fileDataFile);
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

