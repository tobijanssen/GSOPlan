package de.janssen.android.gsoplan;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.google.analytics.tracking.android.EasyTracker;

import de.janssen.android.gsoplan.runnables.AppendPage;
import de.janssen.android.gsoplan.runnables.ErrorMessage;
import de.janssen.android.gsoplan.runnables.SetupPager;
import de.janssen.android.gsoplan.runnables.WeekPlanActivityLuncher;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
	public MyContext ctxt = new MyContext();
	
	public WeekPlanActivity()
	{
		ctxt.context=this;
		ctxt.weekView=true;
		ctxt.activity=this;
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) 
        {
            
        	// Get data via the key
        	Long currentDate = extras.getLong("currentDate");
        	ctxt.forceView = extras.getBoolean("forceView",false);
        	if (currentDate != null) 
        	{
        		try
        		{
	        		Calendar cal = new GregorianCalendar();
	        		cal.setTimeInMillis(currentDate);
	        		ctxt.stupid.currentDate = (Calendar) cal.clone();
        		}
        		catch(Exception e){}
        	}
        	
        } 
        
        
        Resources r = ctxt.context.getResources();
		ctxt.textSizes = r.getIntArray(R.array.TextSizes);
        
        //Android Version pr�fen, wenn neuer als API11, 
        Boolean actionBarAvailable = false;
        if(android.os.Build.VERSION.SDK_INT >= 11)
        {
        	//ActionBar anfragen
        	actionBarAvailable=getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        }
         
        ctxt.inflater = LayoutInflater.from(this);
        setContentView(R.layout.activity_weekly_plan);
        //Wenn ActionBar verf�gbar ist,
        if(actionBarAvailable)
        {
        	//ActionBar hinzuf�gen
	        ActionBar actionBar = getActionBar();
	        actionBar.show();
        }
        ctxt.handler = new Handler();
        
       
        Tools.executeWithDialog(ctxt,new WeekPlanActivityLuncher(this), getString(R.string.msg_start),ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_week_plan, menu);
        return true;
    }
    
    @Override
    protected void onDestroy()
    {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_setup:
                Tools.gotoSetup(ctxt);
                return true;
            case R.id.menu_gotoDate:
            	gotoDate();
            	return true;
            /*case R.id.menu_save:
            	Tools.saveFilesWithProgressDialog(ctxt, ctxt.stupid.currentDate);
            	return true;*/
            case R.id.menu_refresh:
            	Tools.refreshWeek(ctxt);
            	return true;
            case R.id.menu_today:
            	ctxt.stupid.currentDate=new GregorianCalendar();
            	ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.THISWEEK);
            	if(ctxt.weekView)
         		{
            		 ctxt.viewPager.setCurrentItem(Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.WEEK_OF_YEAR));
         		}
         		else
         		{
         			ctxt.viewPager.setCurrentItem(Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.DAY_OF_YEAR));
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
	    if(ctxt.defaultActivity == null)
	    {
	    	Xml xml = new Xml();
	    	
	    	//Pr�fen, ob die ben�tigten Dateien existieren:
	    	File setupFile = Tools.getFileSaveSetup(ctxt);
	    	if(setupFile.exists())
	    	{
		    	//die SetupDatei Laden
		    	try
		    	{
		    		xml.container = FileOPs.readFromFile(setupFile);
		    		ctxt.stupid.clearSetup();
		    		ctxt.stupid.fetchSetupFromXml(xml,ctxt);
		    	}
		    	catch(Exception e)
		        {
		        	//Fehler beim Laden der SetupDatei
		        }
	    	}
	    }
	    
	    if(ctxt.defaultActivity != null && ctxt.defaultActivity.equals(PlanActivity.class) && !ctxt.forceView)
		{
		   	//andere Ansicht gew�hlt
			Intent intent = new Intent(ctxt.activity,ctxt.defaultActivity);
			ctxt.activity.startActivity(intent);	
		}

	    if(ctxt.stupid.dataIsDirty)
	    {
	    	ctxt.stupid.clearData();
	    	ctxt.stupid.dataIsDirty=false;
	    	ctxt.stupid.setupIsDirty=false;
	    	ctxt.handler.postDelayed(new Runnable(){
	    		@Override
				public void run() {
					WeekPlanActivity.this.selfCheck();
					
				}}, 2000);
	    }
	    else if(ctxt.stupid.setupIsDirty)
	    {
	    	ctxt.stupid.setupIsDirty=false;
    		selfCheck();

	    }
	   
	}
	
	@Override
	protected void onStop()
	{
	   	super.onStop();
	   	for(int i=0;i<ctxt.stupid.stupidData.size() && !ctxt.stupid.dataIsDirty;i++)
	   	{
	   		if(ctxt.stupid.stupidData.get(i).isDirty)
	   			ctxt.stupid.dataIsDirty=true;
	   	}
	   	if(ctxt.stupid.dataIsDirty)
		{
			try
			{
				Tools.saveFiles(ctxt);
			}
			catch(Exception e)
			{
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			
		}
	   	EasyTracker.getInstance().activityStop(this);
	}
	@Override
	public void onStart() {
		super.onStart();
	    
	    EasyTracker.getInstance().activityStart(this);
	}

	 
	 /* Datum 27.9.12
	 * Tobias Janssen
	 * �ffnet ein Datumsplugin und pr�ft, ob dieses TimeTable verf�gbar ist, wenn ja, springt er dorthin
	 * 
	 */
	 private void gotoDate()
	 {
		 ctxt.handler.post(new Runnable()
		 {
			@Override
			public void run() 
			{
				DatePickerDialog picker = new DatePickerDialog(WeekPlanActivity.this, new DatePickerDialog.OnDateSetListener() 
				{
					public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							//Backup vom Datum erstellen, falls es das neue Datum nicht gibt
							ctxt.dateBackup = (Calendar) ctxt.stupid.currentDate.clone();
							//das Ausgew�hlte Datum einstellen
							ctxt.stupid.currentDate.set(year, monthOfYear, dayOfMonth);
							//pr�fen, ob es sich dabei um wochenend tage handelt:
							switch(ctxt.stupid.currentDate.get(Calendar.DAY_OF_WEEK))
							{
								case Calendar.SATURDAY:
									ctxt.stupid.currentDate.setTimeInMillis(ctxt.stupid.currentDate.getTimeInMillis()+(1000*60*60*24*2));
									break;
								case Calendar.SUNDAY:
									ctxt.stupid.currentDate.setTimeInMillis(ctxt.stupid.currentDate.getTimeInMillis()+(1000*60*60*24*1));
									break;
								
							}
							ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.SELECTEDWEEK);
						}
				    },
				    ctxt.stupid.currentDate.get(Calendar.YEAR) ,
				    ctxt.stupid.currentDate.get(Calendar.MONTH),
				    ctxt.stupid.currentDate.get(Calendar.DAY_OF_MONTH));
					picker.show();
					
				}
	    		
	    		
	    		
	    	});
	    	
	    	
	    }
    
    /// Datum: 21.09.12
  	/// Autor: Tobias Jan�en
  	///
  	///	Beschreibung:
  	///	F�hrt die Laufzeitpr�fung durch, und ergreift n�tige Ma�bahmen im Fehlerfall
  	///	
    public void selfCheck()
    {
    	ctxt.selfCheckIsRunning=true;
    	switch(checkStructure())
        {
        	case 0:	//Alles in Ordnung
        		try
        		{
        			Tools.loadAllDataFiles(this, ctxt.stupid);
        		}
        		catch (Exception e)
        		{
        			ctxt.handler.post(new ErrorMessage(ctxt, e.getMessage()));
        		}
        		ctxt.stupid.sort();
        		initViewPager();
        		ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.THISWEEK);
        		break;
        		
        	case 1:	//FILESETUP fehlt
        		Tools.gotoSetup(ctxt,Const.FIRSTSTART,true);
        		break;
        		
        	case 2:	//FILESETUP laden fehlgeschlagen
        		Tools.gotoSetup(ctxt,Const.FIRSTSTART,true);
        		break;
        		
        	case 3:	//Keine Klasse ausgew�hlt
        		Tools.gotoSetup(ctxt,Const.FIRSTSTART,true);
        		break;
        	case 6:	//Elementenordner existiert nicht
        			//neuen anlegen
        			java.io.File elementDir = new java.io.File(this.getFilesDir(),ctxt.stupid.myElement);
        			elementDir.mkdir();
        			initViewPager();
        			ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.THISWEEK);
    		break;
        	case 7:	//Keine Daten f�r diese Klasse vorhanden
        		initViewPager();
        		ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.THISWEEK);
        		break;
        }
    	
    	ctxt.selfCheckIsRunning=false;
    }
    
    /// Datum: 20.09.12
  	/// Autor: Tobias Jan�en
  	///
  	///	Beschreibung:
  	///	pr�ft, ob alle Laufzeitbed�rfnisse erf�llt sind
  	///	
    public int checkStructure()
    {
    	Xml xml = new Xml();
    	
    	//Pr�fen, ob die ben�tigten Dateien existieren:
    	File setupFile = Tools.getFileSaveSetup(ctxt);
    	if(!setupFile.exists())
    		return 1;
    		
    	//die SetupDatei Laden
    	try
    	{
    		xml.container = FileOPs.readFromFile(setupFile);
    		ctxt.stupid.clearSetup();
    		ctxt.stupid.fetchSetupFromXml(xml,ctxt);
    	}
    	catch(Exception e)
        {
        	return 2;	//Fehler beim Laden der SetupDatei
        }
    	
    	//pr�fen, ob ein Element ausgew�hlt wurde:
        if(ctxt.stupid.myElement.equalsIgnoreCase(""))
        {
        	return 3;
        }
    	
        //Pr�fen, ob der Elementenordner existiert
        File elementDir = new File(this.getFilesDir()+"/"+ctxt.stupid.myElement);
    	if(!elementDir.exists())
    		return 6;
        
        
        //pr�fen, ob daten f�r die ausgew�hlte klasse vorhanden sind
        //z�hlt wie viele Timetables f�r die ausgew�hlt Klasse vorhanden sind
    	File[] files = elementDir.listFiles();
        
        if(files.length == 0)
        	return 7;
        
        return 0;
    } 
    
    /* Datum: 11.10.12
	 * Tobias Jan�en
	 * Initialisiert den viewPager, der die Tage des Stundenplans darstellt
	 */
    public void initViewPager()
    {
    	ctxt.currentPage=0;
        for(int i=0;i<ctxt.stupid.stupidData.size();i++)
        {
        	ctxt.handler.post(new AppendPage(ctxt.stupid.stupidData.get(i), ctxt));
        	//Tools.appendTimeTableToPager(stupid.stupidData.get(i), stupid, this);
        	
        }

        ctxt.handler.post( new SetupPager(ctxt, ctxt.pageIndex));
        
    }
}