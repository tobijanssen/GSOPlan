/*
 * SaveElement.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.asyncTasks;

import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.xml.XmlOPs;
import java.io.File;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class SaveElement extends AsyncTask<Boolean, Integer, Boolean>
{

    private MyContext ctxt;
    private File elementFile;
    public Exception exception;
    private Runnable postRun;
    private Boolean showDialog;

    public SaveElement(MyContext ctxt, File elementFile, Runnable postRun, Boolean showDialog)
    {
	this.ctxt = ctxt;
	this.elementFile = elementFile;
	this.postRun = postRun;
	this.showDialog = showDialog;
    }

    public SaveElement(MyContext ctxt, File elementFile, Boolean showDialog)
    {
	this.ctxt = ctxt;
	this.elementFile = elementFile;
	this.postRun = null;
	this.showDialog = showDialog;
    }

    @Override
    protected void onPreExecute()
    {
	if (this.showDialog)
	{
	    ctxt.progressDialog = new ProgressDialog(ctxt.context);
	    ctxt.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    ctxt.progressDialog.setMessage(ctxt.context.getString(R.string.msg_saving));
	    ctxt.progressDialog.setCancelable(false);
	    ctxt.progressDialog.show();
	}
	super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Boolean... bool)
    {
	try
	{
	    if (isCancelled())
	    {
		return null;
	    }
	    String xmlContent = XmlOPs.convertElementsToXml(ctxt);

	    FileOPs.saveToFile(xmlContent, elementFile);
	    ctxt.stupid.setupIsDirty = false;

	}
	catch (Exception e)
	{
	    this.exception = e;
	}

	return true;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
	if (this.postRun != null)
	    postRun.run();
	if (ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	{
	    ctxt.progressDialog.dismiss();
	}
	this.ctxt.executor.scheduleNext();
    }

}
