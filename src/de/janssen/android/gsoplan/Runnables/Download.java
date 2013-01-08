package de.janssen.android.gsoplan.runnables;

import android.os.AsyncTask;
import de.janssen.android.gsoplan.StupidCore;

public class Download extends AsyncTask<Boolean, Integer, Boolean>{
	private StupidCore stupid;
	private Boolean[] setupData=new Boolean[2];
	private String[] dateClassType=new String[3];
	private Runnable postrun;
	
	public Download(StupidCore stupid,Boolean setup, Boolean data,Runnable postRun)
	{
		this.stupid=stupid;
		this.setupData[0]=setup;
		this.postrun=postRun;
	}
	public Download(StupidCore stupid,Boolean setup, Boolean data,String selectedDate,String selectedClass,String selectedType)
	{
		this.stupid=stupid;
		this.setupData[0]=setup;
		this.setupData[1]=data;
		this.dateClassType[0]=selectedDate;
		this.dateClassType[1]=selectedClass;
		this.dateClassType[2]=selectedType;
	}
	
	protected Boolean doInBackground(Boolean... bool) {
		try
	    {
			if(this.setupData[0])
			{
				if (isCancelled()) 
					return bool[0];
				//aktuelle Selectoren aus dem Netz laden:
				stupid.fetchSelectorsFromNet();
			}
			if(this.setupData[1])
			{
				if (isCancelled()) 
					return bool[0];
				stupid.fetchTimeTableFromNet(this.dateClassType[0], this.dateClassType[1], this.dateClassType[2]);
			}
	     }
	     catch(Exception e)
	     {

	     }
		return bool[0];
		
	}
	
	 protected void onPostExecute(Boolean bool) {
	    	if(postrun != null && bool)
	    		postrun.run();
	 }

}
