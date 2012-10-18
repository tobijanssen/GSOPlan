package de.janssen.android.gsoplan;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class MyPagerAdapter extends PagerAdapter
{

    	
    	List<View> pages = null;
    	List<String> headlines = null;
    	
    	public MyPagerAdapter(List<View> pages, List<String> headlines)
    	{
            this.pages = pages;
            this.headlines = headlines;
        }

    	@Override
        public Object instantiateItem(View collection, int position){
            View v = pages.get(position);
            ((ViewPager) collection).addView(v, 0);
            return v;
        }
        
        @Override
        public void destroyItem(View collection, int position, Object view){
            ((ViewPager) collection).removeView((View) view);
        }
        
        public CharSequence getPageTitle(int position) {
            return headlines.get(position);
          }
        
        @Override
        public int getCount(){
            return pages.size();
        }
        
        @Override
        public boolean isViewFromObject(View view, Object object){
            return view.equals(object);
        }

        @Override
        public void finishUpdate(View arg0){
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1){
        }

        @Override
        public Parcelable saveState(){
            return null;
        }

        @Override
        public void startUpdate(View arg0){
        }
}
