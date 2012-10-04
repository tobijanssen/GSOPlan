package de.janssen.android.gsoplan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.janssen.android.gsoplan.Runnables.SpecialDownload;
import de.janssen.android.gsoplan.Runnables.Toaster;
import de.janssen.android.gsoplan.Runnables.UpdateTimeTableScreen;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

public class PlanActivity extends Activity implements OnGesturePerformedListener {

	
	public StupidCore stupid = new StupidCore();
	public Exception exception;
	private GestureLibrary gestureLib;
	public MyArrayAdapter adapter;
	public int weekOfYearToDisplay=0;		//Weekto display ist nach wie vor noch in gebraucht
	public Calendar currentDate = new GregorianCalendar();
	public Calendar dateBackup;
	public Handler handler;
	private AsyncTask<Integer, Void, Void> task;
	public TimeTableIndex[] myTimeTableIndex;
	private Boolean selfCheckIsRunning=false;
	public int weekDataIndexToShow;
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private SerialExecutor exec2 = new SerialExecutor(exec);

	public Future<?>[] future = new Future<?>[10];
	public int lastThreadId=0;
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        handler = new Handler();

        
        weekOfYearToDisplay=Tools.getWeekOfYearToDisplay(currentDate);
        setupGesture();
        setupviewList();
        if(!selfCheckIsRunning)
        	selfCheck();
        
    }
    @Override
	protected void onResume() 
	{
	    super.onResume();/*
	    if(stupid.dataIsDirty)
	    {
	    	stupid.clearData();
	    }
	    if(stupid.setupIsDirty)
	    {*/
	    	stupid.setupIsDirty=false;
	    	weekOfYearToDisplay=Tools.getWeekOfYearToDisplay(currentDate);
    		selfCheck();

	    //}
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode == RESULT_OK && requestCode == 1) 
      {
        if (data.hasExtra("setupIsDirty")) 
        {
        	stupid.setupIsDirty = data.getExtras().getBoolean("setupIsDirty");
        }
        if (data.hasExtra("dataIsDirty")) 
        {
        	stupid.dataIsDirty = data.getExtras().getBoolean("dataIsDirty");
        }
      }
    } 
    
    @Override
	protected void onStop()
	{
    	if(stupid.dataIsDirty)
		{
			if(task!=null)
				task.cancel(true);
			try
			{
				Tools.saveFiles(this, stupid, exec);
	    		exec.shutdown();
	    		exec.awaitTermination(120, TimeUnit.SECONDS);
	    		if(!exec.isTerminated())
	    			Toast.makeText(this,"Fehler beim Beenden des Programmes", Toast.LENGTH_LONG).show();
			}
			catch(Exception e)
			{
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			
		}
    	super.onStop();
	}
    
    @Override
    protected void onDestroy()
    {

    	try {
    		
			exec.shutdown();
			
			exec.awaitTermination(120, TimeUnit.SECONDS);
			if(!exec.isTerminated())
			{
				new AlertDialog.Builder(this)
	    	    .setTitle("Timeout")
	    	    .setMessage(this.getString(R.string.msg_error_timeout_onDestroy))
	    	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) { 
	    	            // continue with delete
	    	        }
	    	     })
	    	    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) { 
	    	            // do nothing
	    	        }
	    	     })
	    	     .show();
			}
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	super.onDestroy();
    }

    
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) 
    {
    	ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
    	for (Prediction prediction : predictions) 
    	{
    		if (prediction.score > 1.0) 
    		{
    			dateBackup=(Calendar) currentDate.clone();
    			
    			if(prediction.name.equalsIgnoreCase("left") && currentDate.get(Calendar.DAY_OF_WEEK) > 2)
    			{
    				currentDate.setTimeInMillis(currentDate.getTimeInMillis()-(86400000*1));
    				handler.post(new UpdateTimeTableScreen(this));
    			}
    			else if(prediction.name.equalsIgnoreCase("left"))
    			{
    				currentDate.setTimeInMillis(currentDate.getTimeInMillis()-(86400000*3));
    				//Prüfen, ob diese Woche existiert/on- oder offline
    				checkAvailibilityOfWeek();

    			}
    			
    			if(prediction.name.equalsIgnoreCase("right")&& currentDate.get(Calendar.DAY_OF_WEEK) < 6)
    			{
    				
    				currentDate.setTimeInMillis(currentDate.getTimeInMillis()+(86400000*1));
    				handler.post(new UpdateTimeTableScreen(this));
    			}
    			else if(prediction.name.equalsIgnoreCase("right"))
    			{
    				//Das Datum um drei Tag erhöhen
    				currentDate.setTimeInMillis(currentDate.getTimeInMillis()+(1000*60*60*24*3));
    				
    				checkAvailibilityOfWeek();
    				
    			}
    		}
    	}
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_plan, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_setup:
                gotoSetup();
                return true;
            case R.id.menu_gotoDate:
            	gotoDate();
            	return true;
            case R.id.menu_save:
            	Tools.saveFilesWithProgressDialog(this, stupid, exec);
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
    /* Datum 27.9.12
     * Tobias Janssen
     * Öffnet ein Datumsplugin und prüft, ob dieses TimeTable verfügbar ist, wenn ja, springt er dorthin
     * 
     */
    private void gotoDate()
    {
    	handler.post(new Runnable(){

			@Override
			public void run() {
				
				
				DatePickerDialog picker = new DatePickerDialog(PlanActivity.this, new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						//Backup vom Datum erstellen, falls es das neue Datum nicht gibt
						PlanActivity.this.dateBackup = (Calendar) PlanActivity.this.currentDate.clone();
						//das Ausgewählte Datum einstellen
						PlanActivity.this.currentDate.set(year, monthOfYear, dayOfMonth);
						//prüfen, ob es sich dabei um wochenend tage handelt:
						switch(PlanActivity.this.currentDate.get(Calendar.DAY_OF_WEEK))
						{
							case Calendar.SATURDAY:
								PlanActivity.this.currentDate.setTimeInMillis(currentDate.getTimeInMillis()+(1000*60*60*24*2));
								break;
							case Calendar.SUNDAY:
								PlanActivity.this.currentDate.setTimeInMillis(currentDate.getTimeInMillis()+(1000*60*60*24*1));
						}
						
						//Die KalenderWoche herausfinden:
						PlanActivity.this.weekOfYearToDisplay=Tools.getWeekOfYearToDisplay(PlanActivity.this.currentDate);
						checkAvailibilityOfWeek();
						
					}
			    },
			    PlanActivity.this.currentDate.get(Calendar.YEAR) ,
			    PlanActivity.this.currentDate.get(Calendar.MONTH),
			    PlanActivity.this.currentDate.get(Calendar.DAY_OF_MONTH));
				picker.show();
				
			}
    		
    		
    		
    	});
    	
    	
    }
    
    /*
     * 
     */
    public void getAllAvailableTimeTables()
    {
    	
    	//TODO: Ein Methode entwickeln, die alle verfügbaren Timetables nacheinander downloadet.
    }
    
    public void checkAvailibilityOfWeek()
    {
    	//eine Übersicht erstellen, welche Daten für die aktuelle Klasse überhaupt vorliegen
    	try
    	{
    		myTimeTableIndex = stupid.timeTableIndexer();
    	}
    	catch(Exception e)
    	{
    		//Keine Klasse ausgewählt!
    		gotoSetup();
    	}
    	weekOfYearToDisplay = stupid.getWeekToDisplay(currentDate);
        //aus dieser Liste mithilfer der selektierten KalenderWoche den richtigen Index heraussuchen
        weekDataIndexToShow = stupid.getIndexOfTimeTableWeekId(currentDate, myTimeTableIndex);
        //prüfen, ob diese Woche bereits im Datenbestand ist
        if(weekDataIndexToShow ==-1)
        {
        	//Woche ist nicht im Index enthalten        	
        	//Downloader starten, dieser prüft, ob diese Woche erhältlich ist und unternimmt alle weitern Maßnahmen
        	executeWithDialog(new SpecialDownload(this),getString(R.string.msg_loadingData));
        	
        }
        else
        {
        	//Woche ist im Datenbestand vorhanden
        	
            //Nun prüfen, wie alt diese Daten sind:
        	
        	if(weekOfYearToDisplay < new GregorianCalendar().get(Calendar.WEEK_OF_YEAR))
        	{
        		//diese Woche liegt bereits in der vergangenheit und muss nicht aktualisiert werden
        		handler.post(new UpdateTimeTableScreen(this));
        	}
        	else
        	{
	            Date date = new Date();
	        	if(myTimeTableIndex[weekDataIndexToShow].syncTime + (stupid.myResyncAfter*60*1000) < date.getTime())	
	        	{
	        		//veraltet sollte neu heruntergeladen werden!
	        		executeWithDialog(new SpecialDownload(this),getString(R.string.msg_searchingNewData));
	        	}
	        	else
	        	{
	        		//Datenbestand neu genug
	        		
		        	handler.post(new UpdateTimeTableScreen(this));
	        	}
        	}
        }
        
    }
    
    
    
    
    /// Datum: 21.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Führt die Laufzeitprüfung durch, und ergreift nötige Maßbahmen im Fehlerfall
  	///	
    public void selfCheck()
    {
    	selfCheckIsRunning=true;
    	switch(checkStructure())
        {
        	case 0:	//Alles in Ordnung
        		checkAvailibilityOfWeek();
        		break;
        		
        	case 1:	//FILESETUP fehlt
        		gotoSetup();
        		break;
        		
        	case 2:	//FILESETUP laden fehlgeschlagen
        		gotoSetup();
        		break;
        		
        	case 3:	//Keine Klasse ausgewählt
        		gotoSetup();
        		break;
        		
        	case 4:
        			//FILEDATA Datei fehlt & //Keine Timetable in Datei gefunden
        			executeWithDialog(new SpecialDownload(this),getString(R.string.msg_loadingData));
        		break;
        	
        	case 5:	//FILEDATA laden fehlgeschlagen
        			executeWithDialog(new SpecialDownload(this),getString(R.string.msg_loadingData));
        		break;
        	case 7:	//Keine Daten für diese Klasse vorhanden
        		checkAvailibilityOfWeek();
    			
        		break;
        }
    	selfCheckIsRunning=false;
    }
   
    
    /// Datum: 20.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	prüft, ob alle Laufzeitbedürfnisse erfüllt sind
  	///	
    public int checkStructure()
    {
    	Xml xml = new Xml();
    	
    	//Prüfen, ob die benötigten Dateien existieren:
    	java.io.File testFile = new java.io.File(this.getFilesDir(),Tools.FILESETUP);
    	if(!testFile.exists())
    		return 1;
    		
    	//die SetupDatei Laden
    	try
    	{
    		xml.container = File.readFromFile(this,Tools.FILESETUP);
    		stupid.clearSetup();
    		stupid.fetchSetupFromXml(xml);
    	}
    	catch(Exception e)
        {
        	return 2;	//Fehler beim Laden der SetupDatei
        }
    	
    	//prüfen, ob ein Element ausgewählt wurde:
        if(stupid.myElement.equalsIgnoreCase(""))
        {
        	return 3;
        }
    	
        //Prüfen, ob die DatenDatei existiert
    	testFile = new java.io.File(this.getFilesDir(),stupid.myElement+Tools.FILEDATA);
    	if(!testFile.exists())
    		return 4;
        
    	//Die DatenDatei Laden:
        try
        {
    		xml.container = File.readFromFile(this,stupid.myElement+Tools.FILEDATA);
    		stupid.clearData();
    		stupid.stupidData=xml.convertXmlToStupid(xml);
    	}
    	catch(Exception e)
        {
        	return 5;
        }
        
        //prüfen, ob daten vorhanden sind
        if(stupid.stupidData.length<1)
        {
        	//nein keine daten vorhanden
        	return 4;
        }
        
        //prüfen, ob daten für die ausgewählte klasse vorhanden sind
        //zählt wie viele Timetables für die ausgewählt Klasse vorhanden sind
        int tableCounter = 0;
        for(int i=0; i< stupid.stupidData.length;i++)
        {
        	if(stupid.stupidData[i].elementId.equalsIgnoreCase(stupid.myElement))
        		tableCounter++;
        }
        
        if(tableCounter == 0)
        	return 7;
        
        return 0;
    }
    
    
    
    
    
    /// Datum: 20.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Initialisiert das viewList
  	///	
    public void setupviewList()
    {
    	List<TimetableViewObject> mylist1 = new ArrayList<TimetableViewObject>();
    	
        mylist1.add(new TimetableViewObject("--","Keine Daten vorhanden!","#FFFFFF"));
        
        adapter = new MyArrayAdapter(this,mylist1);
        ListView listView = (ListView) findViewById(R.id.listTimetable);
        
        listView.setAdapter(adapter);
        
        
    }
    
    
    /// Datum: 20.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Initialisiert das Gesture Overlay
  	///	
    public void setupGesture()
    {
    	GestureOverlayView gestureOverlayView = (GestureOverlayView) findViewById(R.id.gestureOverlayView1);
        gestureOverlayView.addOnGesturePerformedListener(this);
        gestureOverlayView.setGestureColor(Color.TRANSPARENT);
        gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);
        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gestureLib.load()) {
          finish();
        }
    
    }


    public boolean gotoSetup() {
    	Tools.saveFilesWithProgressDialog(this,stupid,exec);
    	exec.execute(new Runnable(){

			@Override
			public void run() {
	    		Intent intent = new Intent(PlanActivity.this,SetupActivity.class);
	       		startActivityForResult(intent,1);	
				
			}

    	});
    	

        return true;
    }
    
    /// Datum: 28.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Erstellt einen neuen ProgressDialog mit übergebenem Text
  	///	
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public void executeWithDialog(Runnable run,String text)
    {
    	stupid.progressDialog =  new ProgressDialog(this);
    	stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	stupid.progressDialog.setMessage(text);
    	stupid.progressDialog.setCancelable(true);
    	stupid.progressDialog.setProgress(0);
    	stupid.progressDialog.show();
    	exec.submit(run);
    }
    
}
