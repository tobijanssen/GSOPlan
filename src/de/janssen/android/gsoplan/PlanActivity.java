package de.janssen.android.gsoplan;

import java.io.File;
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
import android.app.AlertDialog.Builder;
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
	public int indexOfWeekIdToDisplay=0;		//Weekto display ist nach wie vor noch in gebraucht
	public Calendar dateBackup;
	public Handler handler;
	private AsyncTask<Integer, Void, Void> task;
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

        
        indexOfWeekIdToDisplay=Tools.getWeekOfYearToDisplay(stupid.currentDate);
        setupGesture();
        setupviewList();
        if(!selfCheckIsRunning)
        	selfCheck();
        
    }
    @Override
	protected void onResume() 
	{
	    super.onResume();

	    if(stupid.dataIsDirty)
	    {
	    	stupid.clearData();
	    	stupid.dataIsDirty=false;
	    	stupid.setupIsDirty=false;
	    	indexOfWeekIdToDisplay=Tools.getWeekOfYearToDisplay(stupid.currentDate);
	    	handler.postDelayed(new Runnable(){

				@Override
				public void run() {
					PlanActivity.this.selfCheck();
					
				}}, 2000);
	    }
	    else if(stupid.setupIsDirty)
	    {
	    	stupid.setupIsDirty=false;
	    	indexOfWeekIdToDisplay=Tools.getWeekOfYearToDisplay(stupid.currentDate);
    		selfCheck();

	    }
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
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
    	else
    	{
    	    Intent intent = getIntent();
    	    finish();
    	    startActivity(intent);
    	}
    
    } 
    
    @Override
	protected void onStop()
	{
    	super.onStop();
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
    	
	}
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
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
    	
    }

    
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) 
    {
    	ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
    	for (Prediction prediction : predictions) 
    	{
    		if (prediction.score > 1.0) 
    		{
    			dateBackup=(Calendar) stupid.currentDate.clone();
    			
    			if(prediction.name.equalsIgnoreCase("left") && stupid.currentDate.get(Calendar.DAY_OF_WEEK) > 2)
    			{
    				stupid.currentDate.setTimeInMillis(stupid.currentDate.getTimeInMillis()-(86400000*1));
    				handler.post(new UpdateTimeTableScreen(this));
    			}
    			else if(prediction.name.equalsIgnoreCase("left"))
    			{
    				//die aktuelle Woche abspeichern
    				try 
    				{
						Tools.saveFiles(PlanActivity.this, stupid, exec);
						
					} 
    				catch (Exception e) 
    				{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				stupid.currentDate.setTimeInMillis(stupid.currentDate.getTimeInMillis()-(86400000*3));
    				//Prüfen, ob diese Woche existiert/on- oder offline
    				checkAvailibilityOfWeek();

    			}
    			
    			if(prediction.name.equalsIgnoreCase("right")&& stupid.currentDate.get(Calendar.DAY_OF_WEEK) < 6)
    			{
    				
    				stupid.currentDate.setTimeInMillis(stupid.currentDate.getTimeInMillis()+(86400000*1));
    				handler.post(new UpdateTimeTableScreen(this));
    			}
    			else if(prediction.name.equalsIgnoreCase("right"))
    			{
    				//die aktuelle Woche abspeichern
    				try {
						Tools.saveFiles(PlanActivity.this, stupid, exec);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				//Das Datum um drei Tag erhöhen
    				stupid.currentDate.setTimeInMillis(stupid.currentDate.getTimeInMillis()+(1000*60*60*24*3));
    				
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
            	Tools.saveFilesWithProgressDialog(this, stupid, exec, stupid.currentDate);
            	return true;
            case R.id.menu_refresh:
            	//selfCheck();
            	refreshWeek();
            	return true;
            case R.id.menu_today:
            	stupid.currentDate=new GregorianCalendar();
            	indexOfWeekIdToDisplay=Tools.getWeekOfYearToDisplay(stupid.currentDate);
            	checkAvailibilityOfWeek();
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
						PlanActivity.this.dateBackup = (Calendar) PlanActivity.this.stupid.currentDate.clone();
						//das Ausgewählte Datum einstellen
						PlanActivity.this.stupid.currentDate.set(year, monthOfYear, dayOfMonth);
						//prüfen, ob es sich dabei um wochenend tage handelt:
						switch(PlanActivity.this.stupid.currentDate.get(Calendar.DAY_OF_WEEK))
						{
							case Calendar.SATURDAY:
								PlanActivity.this.stupid.currentDate.setTimeInMillis(stupid.currentDate.getTimeInMillis()+(1000*60*60*24*2));
								break;
							case Calendar.SUNDAY:
								PlanActivity.this.stupid.currentDate.setTimeInMillis(stupid.currentDate.getTimeInMillis()+(1000*60*60*24*1));
						}
						
						//Die KalenderWoche herausfinden:
						PlanActivity.this.indexOfWeekIdToDisplay=Tools.getWeekOfYearToDisplay(PlanActivity.this.stupid.currentDate);
						checkAvailibilityOfWeek();
						
					}
			    },
			    PlanActivity.this.stupid.currentDate.get(Calendar.YEAR) ,
			    PlanActivity.this.stupid.currentDate.get(Calendar.MONTH),
			    PlanActivity.this.stupid.currentDate.get(Calendar.DAY_OF_MONTH));
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
    
    
    /*	4.10.12
     * 	Tobias Janssen
     * 	Aktualisiert die aktuelle Woche
     */
    public void refreshWeek()
    {
    	//eine Übersicht erstellen, welche Daten für die aktuelle Klasse überhaupt vorliegen
    	try
    	{
    		stupid.timeTableIndexer();
    	}
    	catch(Exception e)
    	{
    		//Keine Klasse ausgewählt!
    		gotoSetup();
    	}
    	indexOfWeekIdToDisplay = stupid.getWeekOfYear(stupid.currentDate);
        //aus dieser Liste mithilfer der selektierten KalenderWoche den richtigen Index heraussuchen
        weekDataIndexToShow = stupid.getIndexOfWeekData(stupid.currentDate);
        //prüfen, ob diese Woche bereits im Datenbestand ist
        if(weekDataIndexToShow ==-1)
        {
        	//Woche ist nicht im Index enthalten        	
        	//Downloader starten, dieser prüft, ob diese Woche erhältlich ist und unternimmt alle weitern Maßnahmen
        	executeWithDialog(new SpecialDownload(this),getString(R.string.msg_loadingData));
        	
        }
        else
        {
        	executeWithDialog(new SpecialDownload(this),getString(R.string.msg_searchingNewData));
        }
        
    }
    
    /*	5.10.12
     * 	Tobias Janssen
     * 
     * 	Prüft, ob die eingestellte Woche(laut stupid.currentDate) bereits verfügbar ist 
     * 	unternimmt weitere Maßnahmen, wenn nicht, oder veraltet
     * 
     */
    public void checkAvailibilityOfWeek()
    {
    	//eine Übersicht erstellen, welche Daten für die aktuelle Klasse überhaupt vorliegen
    	try
    	{
    		stupid.timeTableIndexer();
    	}
    	catch(Exception e)
    	{
    		//Keine Klasse ausgewählt!
    		gotoSetup();
    	}
    	indexOfWeekIdToDisplay = stupid.getWeekOfYear(stupid.currentDate);
        //aus dieser Liste mithilfer der selektierten KalenderWoche den richtigen Index heraussuchen
        weekDataIndexToShow = stupid.getIndexOfWeekData(stupid.currentDate);
        //prüfen, ob diese Woche bereits im Datenbestand ist
        if(weekDataIndexToShow ==-1)
        {
        	//prüfen, ob es eine Datei dazu gibt
        	File myDirectory = new File(this.getFilesDir()+"/"+stupid.myElement);
        	File[] myDirectoryFileList = myDirectory.listFiles();
        	String actualFileName="";
        	String weekId="";
        	String year="";
        	Boolean fileFound =false;
        	for(int i=0;i<myDirectoryFileList.length && !fileFound;i++)
        	{
        		actualFileName =	myDirectoryFileList[i].getName();
        		int endPosWeek = actualFileName.indexOf("_");
        		int endPosYear = actualFileName.indexOf("_",endPosWeek+1);
        		if(endPosWeek !=-1 && endPosYear != -1)
        		{
	        		weekId = actualFileName.substring(0,endPosWeek);
	        		year = actualFileName.substring(endPosWeek+1,endPosYear);
	        		if(String.valueOf(indexOfWeekIdToDisplay).equalsIgnoreCase(weekId) && String.valueOf(stupid.currentDate.get(Calendar.YEAR)).equalsIgnoreCase(year))
	        		{
	        			//passende Datei gefunden, Datei nun dazuladen
	        			Tools.loadNAppendFile(this, stupid, new File(myDirectory,actualFileName));
	        			fileFound=true;
	        		}
        		}
//        		else
//        		{	//TODO: dies ist nur debug bereinigung und dieser else weg muss wieder entfernt werden
//        			myDirectoryFileList[i].delete();
//        			
//        		}
        		
        	}
        	if(fileFound)
        	{
        		try 
        		{
					stupid.timeTableIndexer();
					weekDataIndexToShow = stupid.getIndexOfWeekData(stupid.currentDate);
					//prüfen, ob die benötigten daten nun geladen sind
					if(weekDataIndexToShow ==-1)
					{
						//nein sind nicht im speicher
						executeWithDialog(new SpecialDownload(this),getString(R.string.msg_loadingData));
					}
				} 
        		catch (Exception e) 
        		{
        			executeWithDialog(new SpecialDownload(this),getString(R.string.msg_loadingData));
				}
        	}
        	else
        	{
	        	//Woche ist nicht lokal verfügbar       	
	        	//Downloader starten, dieser prüft, ob diese Woche erhältlich ist und unternimmt alle weitern Maßnahmen
	        	executeWithDialog(new SpecialDownload(this),getString(R.string.msg_loadingData));
        	}
        	
        }
        if(weekDataIndexToShow !=-1)
        {
        	//Woche ist im Datenbestand vorhanden
        	
            //Nun prüfen, wie alt diese Daten sind:
        	
        	if(indexOfWeekIdToDisplay < new GregorianCalendar().get(Calendar.WEEK_OF_YEAR))
        	{
        		//diese Woche liegt bereits in der vergangenheit und muss nicht aktualisiert werden
        		handler.post(new UpdateTimeTableScreen(this));
        	}
        	else
        	{
	            Date date = new Date();
	        	if(stupid.myTimetables[weekDataIndexToShow].syncTime + (stupid.myResyncAfter*60*1000) < date.getTime())	
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
        	case 6:	//Elementenordner existiert nicht
        			//neuen anlegen
        			java.io.File elementDir = new java.io.File(this.getFilesDir(),stupid.myElement);
        			elementDir.mkdir();
        			checkAvailibilityOfWeek();
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
    	File setupFile = Tools.getFileSaveSetup(this, stupid);
    	if(!setupFile.exists())
    		return 1;
    		
    	//die SetupDatei Laden
    	try
    	{
    		xml.container = FileOPs.readFromFile(this,setupFile);
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
    	
        //Prüfen, ob der Elementenordner existiert
        File elementDir = new java.io.File(this.getFilesDir()+"/"+stupid.myElement);
    	if(!elementDir.exists())
    		return 6;
        
        
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
    	Tools.saveFilesWithProgressDialog(this,stupid,exec, stupid.currentDate);
    	exec.shutdown();
    	try {
			exec.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Intent intent = new Intent(PlanActivity.this,SetupActivity.class);
	    startActivityForResult(intent,1);	
    	
    	

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
    	stupid.progressDialog =  new ProgressDialog(PlanActivity.this);
    	stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	stupid.progressDialog.setMessage(text);
    	stupid.progressDialog.setCancelable(true);
    	stupid.progressDialog.setProgress(0);
    	stupid.progressDialog.show();
    	
		exec2.execute(run);
		
    	
    }
    
}
