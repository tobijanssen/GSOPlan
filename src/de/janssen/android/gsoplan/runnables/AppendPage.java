package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.WeekData;

public class AppendPage implements Runnable
{
	private WeekData weekData;
	private MyContext ctxt;
	
	
	public AppendPage(WeekData weekData, MyContext ctxt )
	{
		this.weekData=weekData;
		this.ctxt=ctxt;
	}
	
	@Override
	public void run() 
	{
		
		Tools.appendTimeTableToPager(weekData, ctxt);
	}

	
}
