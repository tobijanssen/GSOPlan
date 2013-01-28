/*
 * WeekPlanActivity.java
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
import de.janssen.android.gsoplan.asyncTasks.WeekPlanActivityLuncher;
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

public class WeekPlanActivity extends Activity
{
    public MyContext ctxt = new MyContext(this, this);

    public WeekPlanActivity()
    {
	ctxt.weekView = true;
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
	    // ctxt.newVersionMsg = extras.getString("newVersionMsg","");
	    if (currentDate != null)
	    {
		try
		{
		    Calendar cal = new GregorianCalendar();
		    cal.setTimeInMillis(currentDate);
		    ctxt.stupid.currentDate = (Calendar) cal.clone();
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
	setContentView(R.layout.activity_weekly_plan);
	// Wenn ActionBar verfügbar ist,
	if (actionBarAvailable)
	{
	    // ActionBar hinzufügen
	    ActionBar actionBar = getActionBar();
	    actionBar.show();
	}
	ctxt.handler = new Handler();
	ctxt.executor.execute(new WeekPlanActivityLuncher(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	getMenuInflater().inflate(R.menu.activity_week_plan, menu);
	return true;
    }

    @Override
    protected void onDestroy()
    {
	ctxt.executor.terminateAllThreads();
	super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
	if (resultCode == RESULT_OK && requestCode == 1)
	{
	    if (data.hasExtra("setupIsDirty"))
	    {
		ctxt.stupid.setupIsDirty = data.getExtras().getBoolean("setupIsDirty");
	    }
	    if (data.hasExtra("dataIsDirty"))
	    {
		ctxt.stupid.dataIsDirty = data.getExtras().getBoolean("dataIsDirty");
	    }
	}
	else
	{
	    Intent intent = getIntent();
	    finish();
	    startActivity(intent);
	}

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
	    ctxt.stupid.currentDate = new GregorianCalendar();
	    ctxt.stupid.checkAvailibilityOfWeek(ctxt, Const.THISWEEK);
	    if (ctxt.weekView)
	    {
		ctxt.viewPager.setCurrentItem(Tools.getPage(ctxt.pageIndex, ctxt.stupid.currentDate,
			Calendar.WEEK_OF_YEAR));
	    }
	    else
	    {
		ctxt.viewPager.setCurrentItem(Tools.getPage(ctxt.pageIndex, ctxt.stupid.currentDate,
			Calendar.DAY_OF_YEAR));
	    }
	    return true;
	case R.id.menu_dayPlan:
	    Tools.gotoDayPlan(ctxt);
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    @Override
    protected void onResume()
    {
	super.onResume();
	ctxt.getPrefs(this.getApplicationContext());
	/*
	 * if(ctxt.defaultActivity == null) { Xml xml = new Xml();
	 * 
	 * //Prüfen, ob die benötigten Dateien existieren: File setupFile =
	 * Tools.getFileSaveSetup(ctxt); if(setupFile.exists()) { //die
	 * SetupDatei Laden try { xml.container =
	 * FileOPs.readFromFile(setupFile); ctxt.stupid.clearSetup();
	 * ctxt.stupid.fetchSetupFromXml(xml,ctxt); } catch(Exception e) {
	 * //Fehler beim Laden der SetupDatei } } }
	 */

	if (ctxt.getDefaultActivity().equalsIgnoreCase("Tag") && !ctxt.forceView)
	{
	    // andere Ansicht gewählt
	    Intent intent = new Intent(ctxt.activity, ctxt.getDefaultActivityClass());
	    ctxt.activity.startActivity(intent);
	}

	if (ctxt.stupid.dataIsDirty)
	{
	    ctxt.stupid.clearData();
	    ctxt.stupid.dataIsDirty = false;
	    ctxt.stupid.setupIsDirty = false;
	    ctxt.handler.postDelayed(new Runnable()
	    {
		@Override
		public void run()
		{
		    WeekPlanActivity.this.ctxt.selfCheck();

		}
	    }, 2000);
	}
	else if (ctxt.stupid.setupIsDirty)
	{
	    ctxt.stupid.setupIsDirty = false;
	    ctxt.selfCheck();

	}

    }

    @Override
    protected void onStop()
    {
	super.onStop();
	for (int i = 0; i < ctxt.stupid.stupidData.size() && !ctxt.stupid.dataIsDirty; i++)
	{
	    if (ctxt.stupid.stupidData.get(i).isDirty)
		ctxt.stupid.dataIsDirty = true;
	}
	if (ctxt.stupid.dataIsDirty)
	{
	    try
	    {
		Tools.saveFiles(ctxt);
	    }
	    catch (Exception e)
	    {
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
	    }

	}
	EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public void onStart()
    {
	super.onStart();

	EasyTracker.getInstance().activityStart(this);
    }

    /**
     * @author Tobias Janssen 
     * Öffnet ein Datumsplugin und prüft, ob dieses TimeTable verfügbar ist, wenn ja, springt er dorthin
     */
    private void gotoDate()
    {
	ctxt.handler.post(new Runnable()
	{
	    @Override
	    public void run()
	    {
		DatePickerDialog picker = new DatePickerDialog(WeekPlanActivity.this,
			new DatePickerDialog.OnDateSetListener()
			{
			    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			    {
				// Backup vom Datum erstellen, falls es das neue
				// Datum nicht gibt
				ctxt.dateBackup = (Calendar) ctxt.stupid.currentDate.clone();
				// das Ausgewählte Datum einstellen
				ctxt.stupid.currentDate.set(year, monthOfYear, dayOfMonth);
				// prüfen, ob es sich dabei um wochenend tage
				// handelt:
				switch (ctxt.stupid.currentDate.get(Calendar.DAY_OF_WEEK))
				{
				case Calendar.SATURDAY:
				    ctxt.stupid.currentDate.setTimeInMillis(ctxt.stupid.currentDate.getTimeInMillis()
					    + (1000 * 60 * 60 * 24 * 2));
				    break;
				case Calendar.SUNDAY:
				    ctxt.stupid.currentDate.setTimeInMillis(ctxt.stupid.currentDate.getTimeInMillis()
					    + (1000 * 60 * 60 * 24 * 1));
				    break;

				}
				ctxt.stupid.checkAvailibilityOfWeek(ctxt, Const.SELECTEDWEEK);
			    }
			}, ctxt.stupid.currentDate.get(Calendar.YEAR), ctxt.stupid.currentDate.get(Calendar.MONTH),
			ctxt.stupid.currentDate.get(Calendar.DAY_OF_MONTH));
		picker.show();
	    }
	});
    }
}
