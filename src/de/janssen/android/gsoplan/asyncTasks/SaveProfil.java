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
import de.janssen.android.gsoplan.core.Stupid;
import de.janssen.android.gsoplan.xml.XmlOPs;
import java.io.File;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class SaveProfil extends AsyncTask<Boolean, Integer, Boolean>
{

    private MyContext ctxt;
    private File profilFile;
    public Exception exception;
    private Runnable postRun;
    private Boolean showDialog;
    private int index;

    /**
     * 
     * @param ctxt
     * @param profilFile
     * @param postRun
     * @param showDialog		Boolean ob ein Dialog erzeugt werden soll
     */
    public SaveProfil(MyContext ctxt, File profilFile, Runnable postRun, Boolean showDialog,int index)
    {
	this.ctxt = ctxt;
	this.profilFile = profilFile;
	this.postRun = postRun;
	this.showDialog = showDialog;
	this.index=index;
    }

    /**
     * 
     * @param ctxt
     * @param profilFile
     * @param showDialog		Boolean ob ein Dialog erzeugt werden soll
     * @param index			der index der gespeichert werden soll
     */
    public SaveProfil(MyContext ctxt, File profilFile, Boolean showDialog, int index)
    {
	this.ctxt = ctxt;
	this.profilFile = profilFile;
	this.postRun = null;
	this.showDialog = showDialog;
	this.index=index;
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
	    if(ctxt.mIsRunning)
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
	    String xmlContent = XmlOPs.createProfileXml(ctxt, index);
	    FileOPs.saveToFile(xmlContent,profilFile);

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
	if (ctxt.mIsRunning && ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	{
	    ctxt.progressDialog.dismiss();
	}
	this.ctxt.executor.scheduleNext();
    }

}
