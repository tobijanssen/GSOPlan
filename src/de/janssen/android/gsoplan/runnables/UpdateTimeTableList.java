package de.janssen.android.gsoplan.runnables;

import java.util.Calendar;

import de.janssen.android.gsoplan.DownloadFeedback;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.MyPagerAdapter;
import de.janssen.android.gsoplan.Tools;

public class UpdateTimeTableList implements Runnable{

	private MyContext ctxt;
	private DownloadFeedback downloadFeedback;
	
	public UpdateTimeTableList(MyContext ctxt, DownloadFeedback downloadFeedback)
	{
		this.ctxt=ctxt;
		this.downloadFeedback=downloadFeedback;
	}
	
	@Override
	public void run() 
	{
		//prüfen, an welchen index die Daten gefügt wurden
    	if( downloadFeedback.indexOfData !=-1)
    	{
    		int currentPage=ctxt.viewPager.getCurrentItem();
    		//prüfen, ob die daten adiiert wurden, oder ob ein Datensatz aktualisiert wurde
    		if(ctxt.stupid.stupidData.size()-1 == downloadFeedback.indexOfData && !downloadFeedback.refreshData)
    		{
    			//append der daten
    			//
            	Tools.appendTimeTableToPager(ctxt.stupid.stupidData.get(downloadFeedback.indexOfData), ctxt);

    		}
    		else
    		{
    			//davor eingefügt, oder refresh
    			
    			if(this.downloadFeedback.refreshData)
    			{

	    			//refresh der daten
    				//TODO: ISSUE# 11 ArrayList.get() ArrayIndexOutOfBoundsException :weil  ctxt.weekDataIndexToShow = -1
    				if(ctxt.weekDataIndexToShow == -1)
    				{
    					ctxt.weekDataIndexToShow = downloadFeedback.indexOfData;	
    				}
    				
	    			Tools.replaceTimeTableInPager(ctxt.stupid.stupidData.get(ctxt.weekDataIndexToShow), ctxt);
    			}
    			else
    			{
    				Tools.appendTimeTableToPager(ctxt.stupid.stupidData.get(downloadFeedback.indexOfData),ctxt);
    			}
    			
    		}
    		ctxt.disableNextPagerOnChangedEvent=true;
    		if(ctxt.weekView)
    		{
    			currentPage=Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.WEEK_OF_YEAR);
    		}
    		else
    		{
    			currentPage=Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.DAY_OF_YEAR);
    		}
    		
    		ctxt.pageAdapter = new MyPagerAdapter(ctxt.pages,ctxt.headlines);
            
    		ctxt.viewPager.setAdapter(ctxt.pageAdapter);
    		ctxt.viewPager.setCurrentItem(currentPage, false);
            
    		ctxt.pageIndicator.setViewPager(ctxt.viewPager);
    		ctxt.pageIndicator.notifyDataSetChanged();
            
    		if (ctxt.stupid.progressDialog != null) 
    		{
    			ctxt.stupid.progressDialog.dismiss();
    		}
    	}
    }

}
