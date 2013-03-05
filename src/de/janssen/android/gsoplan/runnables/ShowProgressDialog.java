/*
 * ShowProgressDialog.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.runnables;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import de.janssen.android.gsoplan.MyContext;

public class ShowProgressDialog implements Runnable
{

    private MyContext ctxt;
    private int style;
    private String text;
    private AsyncTask<Boolean, Integer, Boolean> newTask;

    public ShowProgressDialog(MyContext ctxt, int style, String text)
    {
	this.ctxt = ctxt;
	this.style = style;
	this.text = text;
    }

    public ShowProgressDialog(MyContext ctxt, int style, String text, AsyncTask<Boolean, Integer, Boolean> newTask)
    {
	this.ctxt = ctxt;
	this.style = style;
	this.text = text;
	this.newTask = newTask;

    }

    @Override
    public void run()
    {
	if (ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	    ctxt.progressDialog.dismiss();
	ctxt.progressDialog = new ProgressDialog(ShowProgressDialog.this.ctxt.context);
	ctxt.progressDialog.setProgressStyle(style);
	ctxt.progressDialog.setMessage(text);
	ctxt.progressDialog.setCancelable(true);
	ctxt.progressDialog.setProgress(0);
	ctxt.progressDialog.setOnCancelListener(new OnCancelListener()
	{

	    @Override
	    public void onCancel(DialogInterface dialog)
	    {
		if (newTask != null)
		    newTask.cancel(true);

	    }
	});
	if(ctxt.mIsRunning)
	    ctxt.progressDialog.show();

    }

}
