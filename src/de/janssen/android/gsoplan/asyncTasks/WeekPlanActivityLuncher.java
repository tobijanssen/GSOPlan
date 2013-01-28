/*
 * WeekPlanActivityLuncher.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.asyncTasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.view.WeekPlanActivity;

public class WeekPlanActivityLuncher extends AsyncTask<Boolean, Integer, Boolean>
{
    private WeekPlanActivity parent;

    public WeekPlanActivityLuncher(WeekPlanActivity parent)
    {
	this.parent = parent;
    }

    @Override
    protected void onPreExecute()
    {
	parent.ctxt.progressDialog = new ProgressDialog(parent.ctxt.context);
	parent.ctxt.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	parent.ctxt.progressDialog.setMessage(parent.getString(R.string.msg_start));
	parent.ctxt.progressDialog.setCancelable(true);
	parent.ctxt.progressDialog.show();
	super.onPreExecute();
    }

    protected Boolean doInBackground(Boolean... bool)
    {

	if (!parent.ctxt.selfCheckIsRunning)
	    parent.ctxt.selfCheck();
	return null;

    }

    @Override
    protected void onPostExecute(Boolean result)
    {
	if (parent.ctxt.progressDialog != null && parent.ctxt.progressDialog.isShowing())
	{
	    parent.ctxt.progressDialog.dismiss();
	}
	parent.ctxt.executor.scheduleNext();
    }
}
