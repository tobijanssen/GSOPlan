package de.janssen.android.gsoplan.runnables;

import android.content.Context;
import de.janssen.android.gsoplan.FileOPs;
import de.janssen.android.gsoplan.StupidCore;
import de.janssen.android.gsoplan.Xml;
import java.io.File;

public class SaveSetup implements Runnable{
	private Context context;
	private StupidCore stupid;
	private File setupFile;
	public Exception exception;
	
	
	public SaveSetup(Context context,StupidCore stupid,File setupFile)
	{
		this.context=context;
		this.stupid=stupid;
		this.setupFile=setupFile;
	}
	
	@Override
	public void run() {
		try
		{
			String xmlContent = Xml.convertSetupToXml(stupid,stupid.progressDialog);
			FileOPs.saveToFile(context,xmlContent ,setupFile);
	   		stupid.setupIsDirty=false;
   			stupid.progressDialog.dismiss();
		}
		catch(Exception e)
		{
			this.exception = e;
		}
	}

}
