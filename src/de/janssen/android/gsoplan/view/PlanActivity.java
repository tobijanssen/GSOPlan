/*
 * PlanActivity.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.view;

import java.util.Calendar;
import java.util.GregorianCalendar;
import com.google.analytics.tracking.android.EasyTracker;
import de.janssen.android.gsoplan.Const;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.asyncTasks.PlanActivityLuncher;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.Toast;

public class PlanActivity extends Activity
{

    public MyContext ctxt = new MyContext(this, this);
    
    public PlanActivity()
    {
	ctxt.dayView = true;
    }

    /**
     * @author janssen
     * Öffnet ein Datumsplugin und prüft, ob dieses TimeTable verfügbar ist, wenn ja, springt er dorthin
     */
    private void gotoDate()
    {
	ctxt.handler.post(new Runnable()
	{

	    @Override
	    public void run()
	    {
		if(ctxt.mIsRunning)
		{
		    DatePickerDialog picker = new DatePickerDialog(PlanActivity.this,
			    new DatePickerDialog.OnDateSetListener()
			    {

				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
				{
				    // Backup vom Datum erstellen, falls es das
				    // neue
				    // Datum nicht gibt
				    ctxt.dateBackup = (Calendar) ctxt.getCurStupid().currentDate.clone();
				    // das Ausgewählte Datum einstellen
				    ctxt.getCurStupid().currentDate.set(year, monthOfYear, dayOfMonth);
				    // prüfen, ob es sich dabei um wochenend
				    // tage
				    // handelt:
				    switch (ctxt.getCurStupid().currentDate.get(Calendar.DAY_OF_WEEK))
				    {
				    case Calendar.SATURDAY:
					ctxt.getCurStupid().currentDate.setTimeInMillis(ctxt.getCurStupid().currentDate
						.getTimeInMillis() + (1000 * 60 * 60 * 24 * 2));
					break;
				    case Calendar.SUNDAY:
					ctxt.getCurStupid().currentDate.setTimeInMillis(ctxt.getCurStupid().currentDate
						.getTimeInMillis() + (1000 * 60 * 60 * 24 * 1));
					break;

				    }
				    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.SELECTEDWEEK);
				}
			    }, ctxt.getCurStupid().currentDate.get(Calendar.YEAR), ctxt.getCurStupid().currentDate
				    .get(Calendar.MONTH), ctxt.getCurStupid().currentDate.get(Calendar.DAY_OF_MONTH));
		    picker.show();
		}

	    }

	});

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
	
	ctxt.handler.post(new Runnable(){
		
		@Override
		public void run()
		{
		    ctxt.switchStupid();
		    Tools.saveProfilSameThread(ctxt, ctxt.getSelector());
		    
		    Intent intent = new Intent(ctxt.activity, PlanActivity.class);
		    ctxt.activity.startActivity(intent);
		    finish();
		}
	    });
    }

    @Override
    public void onStart()
    {
	super.onStart();

	EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	Bundle extras = getIntent().getExtras();
	if (extras != null)
	{

	    // Get data via the key
	    Long currentDate = extras.getLong("currentDate");
	    ctxt.forceView = extras.getBoolean("forceView", false);
	    ctxt.newVersionInfo = extras.getBoolean("newVersionInfo", false);
	    if (currentDate != null)
	    {
		try
		{
		    Calendar cal = new GregorianCalendar();
		    cal.setTimeInMillis(currentDate);
		    ctxt.getCurStupid().currentDate = (Calendar) cal.clone();
		}
		catch (Exception e)
		{
		}
	    }

	}

	Resources r = ctxt.context.getResources();
	ctxt.textSizes = r.getIntArray(R.array.TextSizes);

	// Android Version prüfen, wenn neuer als API11,
	Boolean actionBarAvailable = false;
	if (android.os.Build.VERSION.SDK_INT >= 11)
	{
	    // ActionBar anfragen
	    actionBarAvailable = getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	}

	ctxt.inflater = LayoutInflater.from(this);
	setContentView(R.layout.activity_plan);
	// Wenn ActionBar verfügbar ist,
	if (actionBarAvailable)
	{
	    // ActionBar hinzufügen
	    ActionBar actionBar = getActionBar();
	    if(ctxt.mIsRunning)
		actionBar.show();
	}
	ctxt.handler = new Handler();
	ctxt.executor.execute(new PlanActivityLuncher(this));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
	//prüfen, ob ein zweites profil genutz werden soll
	if(!ctxt.getCheckboxPreference(Const.CHECKBOXPROFILID))
	{
	    //wenn nicht die option dazu entfernen
	    menu.removeItem(R.id.menu_favorite);
	}
	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	ctxt.appMenu=menu;
	getMenuInflater().inflate(R.menu.activity_plan, menu);
	return true;
    }

    @Override
    protected void onDestroy()
    {
	// Alle Hintergrundprozesse beenden
	ctxt.executor.terminateAllThreads();
	super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	// Handle item selection
	switch (item.getItemId())
	{
	case R.id.menu_setup:
	    Tools.gotoSetup(ctxt);
	    return true;
	case R.id.menu_gotoDate:
	    gotoDate();
	    return true;
	    /*
	     * case R.id.menu_save: Tools.saveFilesWithProgressDialog(ctxt,
	     * ctxt.stupid.currentDate); return true;
	     */
	case R.id.menu_refresh:
	    Tools.refreshWeek(ctxt);
	    return true;
	case R.id.menu_today:
	    ctxt.getCurStupid().currentDate = new GregorianCalendar();
	    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.THISWEEK);
	    if (ctxt.weekView)
	    {
		ctxt.pager.viewPager.setCurrentItem(Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate,
			Calendar.WEEK_OF_YEAR));
	    }
	    else
	    {
		ctxt.pager.viewPager.setCurrentItem(Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate,
			Calendar.DAY_OF_YEAR));
	    }
	    return true;
	case R.id.menu_weekPlan:
	    Tools.gotoWeekPlan(ctxt);
	    return true;
	case R.id.menu_favorite:
	    
	    ctxt.handler.post(new Runnable(){
		
		@Override
		public void run()
		{
		    ctxt.switchStupid();
		    Tools.saveProfilSameThread(ctxt, ctxt.getSelector());
		    
		    Intent intent = new Intent(ctxt.activity, PlanActivity.class);
		    ctxt.activity.startActivity(intent);
		    finish();
		}
	    });

	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    @Override
    protected void onResume()
    {
	super.onResume();
	ctxt.mIsRunning=true;
    }
    @Override
    protected void onPause()
    {
	super.onPause();
	ctxt.mIsRunning=false;
    }
    
    @Override
    protected void onStop()
    {
	super.onStop();
	
	try
	{
	    ctxt.getCurStupid().saveFiles(ctxt);
	}
	catch (Exception e)
	{
	    if(ctxt.mIsRunning)
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
	}


	EasyTracker.getInstance().activityStop(this);
    }



}
