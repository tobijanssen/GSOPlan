package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.FileOPs;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.Xml;
import java.io.File;

import android.os.AsyncTask;

public class SaveSetup extends AsyncTask<Boolean, Integer, Boolean>{
	private MyContext ctxt;
	private File setupFile;
	public Exception exception;
	
	
	public SaveSetup(MyContext ctxt,File setupFile)
	{
		this.ctxt=ctxt;
		this.setupFile=setupFile;
	}
	
	protected Boolean doInBackground(Boolean... bool) {
		try
		{
			if (isCancelled())
				return null;
			
			String xmlContent = Xml.convertSetupToXml(ctxt);
			FileOPs.saveToFile(xmlContent ,setupFile);
			ctxt.stupid.setupIsDirty=false;
			ctxt.stupid.progressDialog.dismiss();
		}
		catch(Exception e)
		{
			this.exception = e;
		}
		return null;
	}

}
