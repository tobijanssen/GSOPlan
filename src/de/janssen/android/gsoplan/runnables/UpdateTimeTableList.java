package de.janssen.android.gsoplan.runnables;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.viewpagerindicator.TitlePageIndicator;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.janssen.android.gsoplan.DownloadFeedback;
import de.janssen.android.gsoplan.MyArrayAdapter;
import de.janssen.android.gsoplan.MyPagerAdapter;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.TimetableViewObject;
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
		//pr�fen, an welchen index die Daten gef�gt wurden
    	if( downloadFeedback.indexOfData !=-1)
    	{
    		int currentPage=parent.viewPager.getCurrentItem();
    		//pr�fen, ob die daten adiiert wurden, oder ob ein Datensatz aktualisiert wurde
    		if(parent.stupid.stupidData.size()-1 == downloadFeedback.indexOfData && !downloadFeedback.refreshData)
    		{
    			//append der daten
    			//
            	Tools.appendTimeTableToPager(parent.stupid.stupidData.get(downloadFeedback.indexOfData), parent.stupid, parent);
    		}
    		else
    		{
    			//davor eingef�gt, oder refresh
    			
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
    		currentPage=Tools.getPage(parent.pageIndex,parent.stupid.currentDate);
            parent.pageAdapter = new MyPagerAdapter(parent.pages,parent.headlines);
            
            //parent.viewPager = (ViewPager)parent.findViewById(R.id.pager);
            parent.viewPager.setAdapter(parent.pageAdapter);
            parent.viewPager.setCurrentItem(currentPage);

            
            //parent.pageIndicator = (TitlePageIndicator)parent.findViewById(R.id.indicator);
            parent.pageIndicator.setViewPager(parent.viewPager);
            parent.pageIndicator.notifyDataSetChanged();
            
    		if (parent.stupid.progressDialog != null) 
    		{
    			parent.stupid.progressDialog.dismiss();
    		}
    	}
    }

}