package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.StupidCore;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.WeekData;

public class AppendPage implements Runnable
{
	WeekData weekData;
	StupidCore stupid;
	PlanActivity parent;
	
	public AppendPage(WeekData weekData, StupidCore stupid, PlanActivity parent)
	{
		this.weekData=weekData;
		this.stupid=stupid;
		this.parent=parent;
	}
	
	@Override
	public void run() 
	{
		
		Tools.appendTimeTableToPager(weekData, stupid, parent);
	}

	
}
