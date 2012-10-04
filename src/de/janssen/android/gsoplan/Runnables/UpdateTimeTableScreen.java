package de.janssen.android.gsoplan.Runnables;

import java.util.Calendar;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.TimetableViewObject;
import de.janssen.android.gsoplan.Tools;

public class UpdateTimeTableScreen implements Runnable{

	private PlanActivity parent;

	public UpdateTimeTableScreen(PlanActivity parent)
	{
		this.parent=parent;
	}
	
	@Override
	public void run() 
	{
    	if( parent.weekDataIndexToShow !=-1)
    	{
    		
    		parent.adapter.clear();
	        int nullCounter=0;
	        Boolean entryFound=false;
	        int currentIndex = parent.myTimeTableIndex[parent.weekDataIndexToShow].indexKey;
	        int dayOfWeek=Tools.getSetCurrentWeekDay(parent.currentDate)-1;
			for(int y=1;y<parent.stupid.stupidData[currentIndex].timetable.length;y++)
			{	
				if(parent.stupid.stupidData[currentIndex].timetable[y][dayOfWeek].dataContent==null && !entryFound)
				{
					nullCounter++;
				}
				else if(parent.stupid.stupidData[currentIndex].timetable[y][dayOfWeek].dataContent!=null)
				{
					if(parent.stupid.stupidData[currentIndex].timetable[y][dayOfWeek].dataContent.equalsIgnoreCase("null")&& !entryFound)
					{
						nullCounter++;
					}
					else if(parent.stupid.stupidData[currentIndex].timetable[y][dayOfWeek].dataContent.equalsIgnoreCase("")&& !entryFound)
					{
						nullCounter++;
					}
					else
					{
						if(y!=0)
							entryFound=true;
						if(parent.stupid.stupidData[currentIndex].timetable[y][dayOfWeek].dataContent.equalsIgnoreCase("null"))
						{
							parent.adapter.add(new TimetableViewObject(parent.stupid.timeslots[y],"","#000000"));
						}
						else
						{
							String color=parent.stupid.stupidData[currentIndex].timetable[y][dayOfWeek].getColorParameter();
							if(color.equalsIgnoreCase("#000000"))
							{
								color="#FFFFFF";
							}
							parent.adapter.add(new TimetableViewObject(
									parent.stupid.timeslots[y],
									parent.stupid.stupidData[currentIndex].timetable[y][dayOfWeek].dataContent.replaceAll("\n"," "),
									color
									));
						}
					}
				}
				else
				{
					parent.adapter.add(new TimetableViewObject(parent.stupid.timeslots[y],"","#000000"));
				}
			}
			//prüfen, ob gar keine Stunden vorhanden sind
			if(nullCounter == 15)
			{
				parent.adapter.add(new TimetableViewObject("","kein Unterricht","#FFFFFF"));
			}
			
			parent.handler.post(new UpdateHeaderNFooter(parent));
			
			if(parent.stupid.progressDialog != null)
			{
				if(parent.stupid.progressDialog.isShowing())
					parent.stupid.progressDialog.dismiss();
			}
    	}
    }

}
