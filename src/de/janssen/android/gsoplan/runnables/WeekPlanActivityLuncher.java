package de.janssen.android.gsoplan.runnables;

import android.os.AsyncTask;
import de.janssen.android.gsoplan.WeekPlanActivity;

public class WeekPlanActivityLuncher extends AsyncTask<Boolean, Integer, Boolean>
{

	private WeekPlanActivity parent;
	
	public WeekPlanActivityLuncher(WeekPlanActivity parent){
		this.parent=parent;
	}
	
	protected Boolean doInBackground(Boolean... bool) {
		
        if(!parent.ctxt.selfCheckIsRunning)
        	parent.selfCheck();
		if (parent.ctxt.stupid.progressDialog != null) 
		{
			parent.ctxt.stupid.progressDialog.dismiss();
		}
		else
		{
			try 
			{
				java.lang.Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{

			}
			if (parent.ctxt.stupid.progressDialog != null) 
			{
				parent.ctxt.stupid.progressDialog.dismiss();
			}
			
		}
		return null;
		
	}

}
