package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.DownloadFeedback;
import de.janssen.android.gsoplan.MyPagerAdapter;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.Tools;

public class UpdateTimeTableList implements Runnable{

	private PlanActivity parent;
	private DownloadFeedback downloadFeedback;
	public UpdateTimeTableList(PlanActivity parent, DownloadFeedback downloadFeedback )
	{
		this.parent=parent;
		this.downloadFeedback=downloadFeedback;
	}
	
	@Override
	public void run() 
	{
		//prüfen, an welchen index die Daten gefügt wurden
    	if( downloadFeedback.indexOfData !=-1)
    	{
    		int currentPage=parent.viewPager.getCurrentItem();
    		//prüfen, ob die daten adiiert wurden, oder ob ein Datensatz aktualisiert wurde
    		if(parent.stupid.stupidData.size()-1 == downloadFeedback.indexOfData && !downloadFeedback.refreshData)
    		{
    			//append der daten
    			//
            	Tools.appendTimeTableToPager(parent.stupid.stupidData.get(downloadFeedback.indexOfData), parent.stupid, parent);
    		}
    		else
    		{
    			//davor eingefügt, oder refresh
    			
    			if(this.downloadFeedback.refreshData)
    			{

	    			//refresh der daten
	    			Tools.replaceTimeTableInPager(parent.stupid.stupidData.get(parent.weekDataIndexToShow), parent.stupid, parent);
    			}
    			else
    			{
    				Tools.appendTimeTableToPager(parent.stupid.stupidData.get(downloadFeedback.indexOfData), parent.stupid, parent);
    			}
    			
    		}
    		parent.disableNextPagerOnChangedEvent=true;
    		currentPage=Tools.getPage(parent.pageIndex,parent.stupid.currentDate);
            parent.pageAdapter = new MyPagerAdapter(parent.pages,parent.headlines);
            
            parent.viewPager.setAdapter(parent.pageAdapter);
            parent.viewPager.setCurrentItem(currentPage, false);
            
            parent.pageIndicator.setViewPager(parent.viewPager);
            parent.pageIndicator.notifyDataSetChanged();
            
    		if (parent.stupid.progressDialog != null) 
    		{
    			parent.stupid.progressDialog.dismiss();
    		}
    	}
    }

}
