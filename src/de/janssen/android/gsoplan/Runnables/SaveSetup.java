package de.janssen.android.gsoplan.Runnables;

import android.content.Context;
import de.janssen.android.gsoplan.File;
import de.janssen.android.gsoplan.StupidCore;
import de.janssen.android.gsoplan.Xml;

public class SaveSetup implements Runnable{
	private Context context;
	private StupidCore stupid;
	private String fileSetupFile;
	public Exception exception;
	
	
	public SaveSetup(Context context,StupidCore stupid,String fileSetupFile)
	{
		this.context=context;
		this.stupid=stupid;
		this.fileSetupFile=fileSetupFile;
	}
	
	@Override
	public void run() {
		try
		{
			File.saveToFile(context, Xml.convertSetupToXml(stupid,stupid.progressDialog), fileSetupFile);
	   		stupid.setupIsDirty=false;
	   		if(!stupid.setupIsDirty && !stupid.dataIsDirty)
	   		{
	   			stupid.progressDialog.dismiss();
	   		}
		}
		catch(Exception e)
		{
			this.exception = e;
		}
	}

}
