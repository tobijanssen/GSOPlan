package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.MyPagerAdapter;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.Tools;

public class RefreshPager implements Runnable{

	private PlanActivity parent;
	private Boolean smoothScroll;
	public RefreshPager(PlanActivity parent, Boolean smoothScroll){
		this.parent=parent;
		this.smoothScroll=smoothScroll;
	}
	
	@Override
	public void run() {
		int newPage=Tools.getPage(parent.pageIndex,parent.stupid.currentDate);
		parent.currentPage=parent.viewPager.getCurrentItem();
		if(newPage != parent.currentPage)//newPage != (parent.currentPage -1) && newPage != (parent.currentPage + 1) &&
		{

			if(newPage != 0)		//Dort scheint ein bug zu sein; bei Seite 0 wird onPageselected Event nicht gestartet, daher nicht disablen
				parent.disableNextPagerOnChangedEvent=true;
			
			parent.currentPage=newPage;
			parent.pageAdapter = new MyPagerAdapter(parent.pages,parent.headlines);
			
			
			parent.viewPager.setAdapter(parent.pageAdapter);
			parent.viewPager.setCurrentItem(parent.currentPage,smoothScroll);
	
			parent.pageIndicator.setViewPager(parent.viewPager);
			parent.pageIndicator.notifyDataSetChanged();
			
			parent.pageIndicator.setCurrentItem(newPage);
		}
		
	}

}
