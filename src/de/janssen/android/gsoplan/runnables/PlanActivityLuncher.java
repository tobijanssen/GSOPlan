package de.janssen.android.gsoplan.runnables;

import android.os.AsyncTask;
import de.janssen.android.gsoplan.PlanActivity;

public class PlanActivityLuncher extends AsyncTask<Boolean, Integer, Boolean>{

	private PlanActivity parent;
	
	public PlanActivityLuncher(PlanActivity parent){
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
