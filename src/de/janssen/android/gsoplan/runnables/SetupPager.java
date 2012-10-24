package de.janssen.android.gsoplan.runnables;

import java.util.Calendar;
import java.util.List;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.viewpagerindicator.TitlePageIndicator;
import de.janssen.android.gsoplan.Const;
import de.janssen.android.gsoplan.MyPagerAdapter;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.R;

public class SetupPager implements Runnable{

	private PlanActivity parent;
	private int page;
	
	public SetupPager(PlanActivity parent,List<Calendar> pageIndex,int page)
	{
		this.parent=parent;
		this.page=page;
	}
	
	@Override
	public void run() {
		
		parent.pageAdapter = new MyPagerAdapter(parent.pages,parent.headlines);       
		parent.viewPager = (ViewPager)parent.findViewById(R.id.pager);
		parent.viewPager.setAdapter(parent.pageAdapter);

		parent.viewPager.setCurrentItem(page);
		parent.pageIndicator = (TitlePageIndicator)parent.findViewById(R.id.indicator);
		parent.pageIndicator.setViewPager(parent.viewPager);
		parent.pageIndicator.setOnPageChangeListener(new OnPageChangeListener(){

 			@Override
 			public void onPageScrollStateChanged(int state) {
 				
 			}

 			@Override
 			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

 				
 			}

 			@Override
 			public void onPageSelected(int position) {

 				Calendar selectedDay = parent.pageIndex.get(position);
 				parent.stupid.currentDate = (Calendar) selectedDay.clone();
 				if(selectedDay.get(Calendar.DAY_OF_WEEK) == 6 && !parent.disablePagerOnChangedListener)
 				{
 					//ende erreicht
 					parent.disablePagerOnChangedListener=true;
 					try
 					{
 						parent.checkAvailibilityOfWeek(Const.NEXTWEEK);
 					}
 					finally
 					{
 						parent.disablePagerOnChangedListener=false;
 					}
 					
 				}
 				else if(selectedDay.get(Calendar.DAY_OF_WEEK) == 2 && !parent.disablePagerOnChangedListener)
 				{
 					//Anfang erreicht
 					parent.disablePagerOnChangedListener=true;
 					try
 					{
 						parent.checkAvailibilityOfWeek(Const.LASTWEEK);
 					}
 					finally
 					{
 						parent.disablePagerOnChangedListener=false;
 					}
 				}
 			}
         	
         	
         });
		
	}

}
