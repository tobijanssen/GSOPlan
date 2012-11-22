package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.PlanActivity;

public class PlanActivityLuncher implements Runnable{

	private PlanActivity parent;
	
	public PlanActivityLuncher(PlanActivity parent){
		this.parent=parent;
	}
	@Override
	public void run() {
        if(!parent.selfCheckIsRunning)
        	parent.selfCheck();
		if (parent.stupid.progressDialog != null) 
		{
			parent.stupid.progressDialog.dismiss();
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
			if (parent.stupid.progressDialog != null) 
			{
				parent.stupid.progressDialog.dismiss();
			}
			
		}
		
	}

}
