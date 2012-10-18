package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.StupidCore;

public class Download implements Runnable{
	private StupidCore stupid;
	private Boolean[] setupData=new Boolean[2];
	private String[] dateClassType=new String[3];
	
	public Download(StupidCore stupid,Boolean setup, Boolean data)
	{
		this.stupid=stupid;
		this.setupData[0]=setup;
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
	
	@Override
	public void run() 
	{
		try
	    {
			if(this.setupData[0])
			{
				//aktuelle Selectoren aus dem Netz laden:
				stupid.fetchSelectorsFromNet();
			}
			if(this.setupData[1])
			{
				stupid.fetchTimeTableFromNet(this.dateClassType[0], this.dateClassType[1], this.dateClassType[2]);
			}
	     }
	     catch(Exception e)
	     {

	     }
		
	}

}
