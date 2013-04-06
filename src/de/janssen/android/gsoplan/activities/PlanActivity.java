/*
 * PlanActivity.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.activities;

import java.util.Calendar;
import java.util.GregorianCalendar;
import com.google.analytics.tracking.android.EasyTracker;
import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.asyncTasks.PlanActivityLuncher;
import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.core.StupidOPs;
import de.janssen.android.gsoplan.core.Tools;
import de.janssen.android.gsoplan.dataclasses.Const;
import android.os.Bundle;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.Toast;

public class PlanActivity extends Activity
{

    public MyContext ctxt;
    

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
		    Calendar cal = ctxt.pager.getDateOfCurrentPage();
		    DatePickerDialog picker = new DatePickerDialog(PlanActivity.this,
			    new DatePickerDialog.OnDateSetListener()
			    {

				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
				{
				    // das Ausgewählte Datum einstellen
				    Calendar newDate = new GregorianCalendar();
				    newDate.set(year, monthOfYear, dayOfMonth);
				    // prüfen, ob es sich dabei um wochenend tage handelt:
				    switch (newDate.get(Calendar.DAY_OF_WEEK))
				    {
				    case Calendar.SATURDAY:
					newDate.setTimeInMillis(newDate.getTimeInMillis() + (1000 * 60 * 60 * 24 * 2));
					break;
				    case Calendar.SUNDAY:
					newDate.setTimeInMillis(newDate.getTimeInMillis() + (1000 * 60 * 60 * 24 * 1));
					break;
				    }
				    int page = ctxt.pager.getPage(newDate,-1);
				    if(page != -1)
				    {
					//gefunden
					ctxt.pager.setPage(page);
				    }
				    else
				    {
					Toast.makeText(PlanActivity.this, "Dieses Datum ist nicht verfügbar!", Toast.LENGTH_SHORT).show();
				    }
				    
				}
			    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		    picker.show();
		}

	    }

	});

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
	//TODO: refactor this:
	ctxt.handler.post(new Runnable(){
		
		@Override
		public void run()
		{
		    //ctxt.switchStupid();
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
	ctxt.mIsRunning=true;
	EasyTracker.getInstance().activityStart(this);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() 
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    // Get extra data included in the Intent
	    int message = intent.getIntExtra("message",Activity.RESULT_CANCELED);
	    ctxt.logger.log(Logger.Level.INFO_2,"WeekPlanActivity received message, (-1: Refresh) Message: " + message);
	    if(message == Activity.RESULT_OK )
	    {
		Message msg = new Message();
		msg.arg1 = message;
		ctxt.msgHandler.handleMessage(msg);
	    }
	}
    };
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	// Android Version prüfen, wenn neuer als API11,
	Boolean actionBarAvailable = false;
	if (android.os.Build.VERSION.SDK_INT >= 11)
	{
	    // ActionBar anfragen
	    actionBarAvailable = getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	}

	setContentView(R.layout.activity_plan);
	this.ctxt =  new MyContext(this, this);
	LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
	lbm.registerReceiver(mMessageReceiver, new IntentFilter(Const.BROADCASTREFRESH));
	Bundle extras = getIntent().getExtras();
	if (extras != null)
	{
	    int noticationId = extras.getInt("notificationId");
	    
	    if(noticationId !=0 )
	    {
		extras.remove("notificationId");
		//notication aus taskbar entfernen
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(noticationId);
		String weekIndex = extras.getString("weekIndex");
		extras.remove("weekIndex");
		ctxt.mProfil.stupid.currentDate = new GregorianCalendar();
		if(weekIndex !=null)
		{
		    int week = Integer.parseInt(weekIndex);
		    
		    if(week < ctxt.mProfil.stupid.currentDate.get(Calendar.WEEK_OF_YEAR))
		    {
			//---
			while(week != ctxt.mProfil.stupid.currentDate.get(Calendar.WEEK_OF_YEAR))
			{
			    Long millis = ctxt.mProfil.stupid.currentDate.getTimeInMillis();
			    ctxt.mProfil.stupid.currentDate.setTimeInMillis(millis - 1000 * (60 * 60 * 24 * 7));
			}
		    }
		    else if(week > ctxt.mProfil.stupid.currentDate.get(Calendar.WEEK_OF_YEAR))
		    {
			//+++
			while(week != ctxt.mProfil.stupid.currentDate.get(Calendar.WEEK_OF_YEAR))
			{
			    Long millis = ctxt.mProfil.stupid.currentDate.getTimeInMillis();
			    ctxt.mProfil.stupid.currentDate.setTimeInMillis(millis + (1000 * 60 * 60 * 24 * 7));
			}
		    }
		}
		
		//den Tag einstellen
		int dayIndex = extras.getInt("dayIndex");
		extras.remove("dayIndex");
		if(dayIndex != 0)
		{
		    //Anpassen damit die Wochentagzählung mit den des Indexes übereinstimmen
		    dayIndex++;
		    if(dayIndex < ctxt.mProfil.stupid.currentDate.get(Calendar.DAY_OF_WEEK))
		    {
			//---
			while(dayIndex != ctxt.mProfil.stupid.currentDate.get(Calendar.DAY_OF_WEEK))
			{
			    Long millis = ctxt.mProfil.stupid.currentDate.getTimeInMillis();
			    ctxt.mProfil.stupid.currentDate.setTimeInMillis(millis - 1000 * (60 * 60 * 24));
			}
		    }
		    else if(dayIndex > ctxt.mProfil.stupid.currentDate.get(Calendar.DAY_OF_WEEK))
		    {
			//+++
			while(dayIndex != ctxt.mProfil.stupid.currentDate.get(Calendar.DAY_OF_WEEK))
			{
			    Long millis = ctxt.mProfil.stupid.currentDate.getTimeInMillis();
			    ctxt.mProfil.stupid.currentDate.setTimeInMillis(millis + (1000 * 60 * 60 * 24));
			}
		    }
		}
		
	    }
	    ctxt.newVersionReqSetup = extras.getBoolean("newVersionInfo", false);
	}
	// Wenn ActionBar verfügbar ist,
	if (actionBarAvailable)
	{
	    // ActionBar hinzufügen
	    ActionBar actionBar = getActionBar();
	    if(ctxt.mIsRunning)
		actionBar.show();
	}
	ctxt.executor.post(new PlanActivityLuncher(this));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
	//TODO: refactor this:
	//prüfen, ob ein zweites mProfil genutz werden soll
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
	case R.id.menu_refresh:
	    StupidOPs.contactStupidService(this, ctxt.msgHandler);
	    return true;
	case R.id.menu_today:
	    ctxt.getCurStupid().currentDate = new GregorianCalendar();
	    ctxt.pager.setPage(ctxt.getCurStupid().currentDate);
	    return true;
	case R.id.menu_favorite:
	    
	    ctxt.handler.post(new Runnable(){
		
		@Override
		public void run()
		{
		   //ctxt.switchStupid();
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
	
	if(ctxt.appIsReady)
	{
	    ctxt.mIsRunning = true;

	    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
	    lbm.registerReceiver(mMessageReceiver, new IntentFilter(Const.BROADCASTREFRESH));
	    Message msg = new Message();
	    msg.arg1 = Activity.RESULT_OK;
	    ctxt.msgHandler.handleMessage(msg);
	}
	
	
	
    }
    @Override
    protected void onPause()
    {
	super.onPause();
	LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	ctxt.mIsRunning=false;
    }
    
    @Override
    protected void onStop()
    {
	super.onStop();
	
	EasyTracker.getInstance().activityStop(this);
    }
    



}
