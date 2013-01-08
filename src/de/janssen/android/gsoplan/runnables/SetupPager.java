package de.janssen.android.gsoplan.runnables;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.viewpagerindicator.TitlePageIndicator;
import de.janssen.android.gsoplan.Const;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.MyPagerAdapter;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;

public class SetupPager implements Runnable{

	private MyContext ctxt;
	private List<Integer> states = new ArrayList<Integer>();
	private Integer lastPage = 0;
	
	public SetupPager(MyContext ctxt,List<Calendar> pageIndex)
	{
		this.ctxt=ctxt;
	}
	
	@Override
	public void run() {
		
		
		if(ctxt.weekView)
		{
        	ctxt.currentPage=Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.WEEK_OF_YEAR);
		}
		else
		{
			ctxt.currentPage=Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.DAY_OF_YEAR);
		}
		ctxt.pageAdapter = new MyPagerAdapter(ctxt.pages,ctxt.headlines);       
		ctxt.viewPager = (ViewPager)ctxt.activity.findViewById(R.id.pager);
		ctxt.viewPager.setAdapter(ctxt.pageAdapter);

		
		ctxt.pageIndicator = (TitlePageIndicator)ctxt.activity.findViewById(R.id.indicator);
		ctxt.pageIndicator.setViewPager(ctxt.viewPager);
		ctxt.disableNextPagerOnChangedEvent=true;
		ctxt.pageIndicator.setOnPageChangeListener(new OnPageChangeListener(){

 			@Override
 			public void onPageScrollStateChanged(int state) {
 				//die states katalogisieren
 				if(state == 1)
 					states.add(state);
 				//bei state 2 wurde ein page turn gemacht
 				if(state == 2)
 					states.add(state);
 				//bei state 0 ist das ende des evtl. turns 
 				if(state == 0)
 				{
 					//prüfen, ob der vorgänger die 1 war(denn dann wurde kein Pagetrun gemacht
 					if(states.get(states.size()-1) == 1 && states.size() == 1)
 					{
 						//kein Pageturn gemacht(also ende erreicht)
 						//prüfen, ob anfang, oder ende
 						if(ctxt.viewPager.getCurrentItem() == 0)
 						{
 							if(ctxt.headlines.size()-1==0)
 							{
	 							//anfang& ende
	 							if(ctxt.weekView)
	 		 					{
	 		 						Calendar selectedWeek = ctxt.pageIndex.get(0);
	 				 				ctxt.stupid.currentDate = (Calendar) selectedWeek.clone();
	 				 				
	 				 				ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.NEXTWEEK);
	 				 				ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.LASTWEEK);
	 		 					}
	 		 					else
	 		 					{
	 				 				Calendar selectedDay = ctxt.pageIndex.get(0);
	 				 				ctxt.stupid.currentDate = (Calendar) selectedDay.clone();
	 				 				ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.NEXTWEEK);
				 					ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.LASTWEEK);
	
	 		 					}
 							}
 							else
 							{
	 							//anfang
	 							if(ctxt.weekView)
	 		 					{
	 		 						Calendar selectedWeek = ctxt.pageIndex.get(0);
	 				 				ctxt.stupid.currentDate = (Calendar) selectedWeek.clone();
	 				 				
	 				 				//ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.NEXTWEEK);
	 				 				ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.LASTWEEK);
	 		 					}
	 		 					else
	 		 					{
	 				 				Calendar selectedDay = ctxt.pageIndex.get(0);
	 				 				ctxt.stupid.currentDate = (Calendar) selectedDay.clone();
				 					ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.LASTWEEK);
	
	 		 					}
 							}
 						}
 						else
 						{
 							//ende erreicht
 							if(ctxt.weekView)
 		 					{
 		 						Calendar selectedWeek = ctxt.pageIndex.get(ctxt.pageIndex.size()-1);
 				 				ctxt.stupid.currentDate = (Calendar) selectedWeek.clone(); 				 				
 				 				ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.NEXTWEEK);
 		 					}
 		 					else
 		 					{
 				 				Calendar selectedDay = ctxt.pageIndex.get(ctxt.pageIndex.size()-1);
 				 				ctxt.stupid.currentDate = (Calendar) selectedDay.clone();
			 					ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.NEXTWEEK);
 		 					}
 						}
 						
 					}
 					//historie löschen
 					states.clear();
 				}
 			}

 			@Override
 			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

 			}

 			@Override
 			public void onPageSelected(int position) {


 				
 				Calendar selectedDay = ctxt.pageIndex.get(position);
 				ctxt.stupid.currentDate = (Calendar) selectedDay.clone();
 				
 				//prüfen, ob dieses Event unterdrückt werden sollte
 				if(ctxt.disableNextPagerOnChangedEvent)
 				{
 					//wenn ja, den Listener wieder aktivieren
 					ctxt.disableNextPagerOnChangedEvent = false;
 				}
 				else
 				{
 					//wenn nicht, kann das event ausgeführt werden
 					if(ctxt.weekView)
 					{
		 				ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.THISWEEK);
 					}
 					else
 					{
		 				if(selectedDay.get(Calendar.DAY_OF_WEEK) == 6 && lastPage > position)
		 				{
		 					//Freitag erreicht und von einem Donnerstag
		 					ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.THISWEEK);
		 				}
		 				else if(selectedDay.get(Calendar.DAY_OF_WEEK) == 2 && lastPage < position)
		 				{
		 					//Anfang erreicht
		 					ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.THISWEEK);
		 				}
 					}
 				}
 				
 				
 				lastPage=position;
 			}
         	
         	
         });
		ctxt.viewPager.setCurrentItem(ctxt.currentPage);
		ctxt.pagerReady=true;
		
	}

}
