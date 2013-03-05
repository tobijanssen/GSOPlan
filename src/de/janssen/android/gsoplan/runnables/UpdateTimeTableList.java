/*
 * UpdateTimeTableList.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.runnables;

import java.util.Calendar;

import de.janssen.android.gsoplan.DownloadFeedback;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.view.MyPagerAdapter;

public class UpdateTimeTableList implements Runnable
{

    private MyContext ctxt;
    private DownloadFeedback downloadFeedback;

    public UpdateTimeTableList(MyContext ctxt, DownloadFeedback downloadFeedback)
    {
	this.ctxt = ctxt;
	this.downloadFeedback = downloadFeedback;
    }

    @Override
    public void run()
    {
	// prüfen, an welchen index die Daten gefügt wurden
	if (downloadFeedback.indexOfData != -1)
	{
	    int currentPage = ctxt.pager.viewPager.getCurrentItem();
	    // prüfen, ob die daten adiiert wurden, oder ob ein Datensatz
	    // aktualisiert wurde
	    if (ctxt.getCurStupid().stupidData.size() - 1 == downloadFeedback.indexOfData && !downloadFeedback.refreshData)
	    {
		// append der daten
		//
		Tools.appendTimeTableToPager(ctxt.getCurStupid().stupidData.get(downloadFeedback.indexOfData), ctxt);

	    }
	    else
	    {
		// davor eingefügt, oder refresh

		if (this.downloadFeedback.refreshData)
		{

		    // refresh der daten
		    if (ctxt.pager.weekDataIndexToShow == -1)
		    {
			ctxt.pager.weekDataIndexToShow = downloadFeedback.indexOfData;
		    }

		    Tools.replaceTimeTableInPager(ctxt.getCurStupid().stupidData.get(ctxt.pager.weekDataIndexToShow), ctxt);
		}
		else
		{
		    Tools.appendTimeTableToPager(ctxt.getCurStupid().stupidData.get(downloadFeedback.indexOfData), ctxt);
		}

	    }
	    ctxt.pager.disableNextPagerOnChangedEvent = true;
	    if (ctxt.weekView)
	    {
		currentPage = Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate, Calendar.WEEK_OF_YEAR);
	    }
	    else
	    {
		currentPage = Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate, Calendar.DAY_OF_YEAR);
	    }

	    ctxt.pager.pageAdapter = new MyPagerAdapter(ctxt.pager.pages, ctxt.pager.headlines);

	    ctxt.pager.viewPager.setAdapter(ctxt.pager.pageAdapter);
	    ctxt.pager.viewPager.setCurrentItem(currentPage, false);

	    ctxt.pager.pageIndicator.setViewPager(ctxt.pager.viewPager);
	    ctxt.pager.pageIndicator.notifyDataSetChanged();

	    if (ctxt.progressDialog != null)
	    {
		ctxt.progressDialog.dismiss();
	    }
	}
    }

}
