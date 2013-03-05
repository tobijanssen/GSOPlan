/*
 * Download.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.asyncTasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.runnables.ErrorMessage;

public class Download extends AsyncTask<Boolean, Integer, Boolean>
{
    private MyContext ctxt;
    private Boolean[] setupData = new Boolean[2];
    private String[] dateClassType = new String[3];
    private Runnable postrun;
    private Exception exception;

    public Download(MyContext ctxt, Boolean setup, Boolean data, Runnable postRun)
    {
	this.ctxt = ctxt;
	this.setupData[0] = setup;
	this.setupData[1] = data;
	this.postrun = postRun;
    }

    public Download(MyContext ctxt, Boolean setup, Boolean data, String selectedDate, String selectedClass,
	    String selectedType)
    {
	this.ctxt = ctxt;
	this.setupData[0] = setup;
	this.setupData[1] = data;
	this.dateClassType[0] = selectedDate;
	this.dateClassType[1] = selectedClass;
	this.dateClassType[2] = selectedType;
    }

    @Override
    protected void onPreExecute()
    {
	if(ctxt.mIsRunning)
	{ 
	    ctxt.progressDialog = new ProgressDialog(ctxt.context);
	    ctxt.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    ctxt.progressDialog.setMessage(ctxt.context.getString(R.string.setup_message_dlElements_body));
	    ctxt.progressDialog.setCancelable(false);
	    ctxt.progressDialog.show();
	}
	super.onPreExecute();
    }

    protected Boolean doInBackground(Boolean... bool)
    {
	try
	{
	    if (this.setupData[0])
	    {
		if (isCancelled())
		    return false;
		// aktuelle Selectoren aus dem Netz laden:
		ctxt.getCurStupid().fetchSelectorsFromNet(ctxt);
	    }
	    if (this.setupData[1])
	    {
		if (isCancelled())
		    return false;
		// TODO: Wird das überhaupt noch benötigt?
		ctxt.getCurStupid().fetchTimeTableFromNet(this.dateClassType[0], this.dateClassType[1], this.dateClassType[2],
			ctxt);
	    }
	}
	catch (Exception e)
	{
	    this.exception = e;
	}
	return false;

    }

    protected void onPostExecute(Boolean bool)
    {
	//prüfen, ob ein Fehler aufgetreten ist
	if (this.exception != null)
	{
	    if (ctxt.mIsRunning && ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	    {
		ctxt.progressDialog.dismiss();
	    }
	    ctxt.handler.post(new ErrorMessage(this.ctxt, exception.getMessage()));
	    if (postrun != null)
		postrun.run();
	}
	else	//kein fehler
	{
	    if (postrun != null)
		postrun.run();
	    if (ctxt.mIsRunning && ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	    {
		ctxt.progressDialog.dismiss();
	    }
	}
	ctxt.executor.scheduleNext();
    }

}
