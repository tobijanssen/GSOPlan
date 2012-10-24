package de.janssen.android.gsoplan;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.viewpagerindicator.TitlePageIndicator;
import de.janssen.android.gsoplan.runnables.ErrorMessage;
import de.janssen.android.gsoplan.runnables.MainDownloader;
import de.janssen.android.gsoplan.runnables.PlanActivityLuncher;
import de.janssen.android.gsoplan.runnables.SetupPager;
import de.janssen.android.gsoplan.runnables.ShowProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.Toast;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;



public class PlanActivity extends Activity {

	
	public StupidCore stupid = new StupidCore();
	public Exception exception;
	public MyArrayAdapter adapter;
	public Calendar dateBackup;
	public Handler handler;
	public Boolean selfCheckIsRunning=false;
	public int weekDataIndexToShow;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private SerialExecutor execQueue = new SerialExecutor(executor);
	public List<View> pages = new ArrayList<View>();
	public List<Calendar> pageIndex = new ArrayList<Calendar>();;
	public List<String> headlines = new ArrayList<String>();
	public LayoutInflater inflater;
	public PagerAdapter pageAdapter;
	public ViewPager viewPager;
	public Boolean disablePagerOnChangedListener = false;
	public TitlePageIndicator pageIndicator;
	private int currentPage;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        //Android Version prüfen, wenn neuer als API11, 
        Boolean actionBarAvailable = false;
        if(android.os.Build.VERSION.SDK_INT >= 11)
        {
        	//ActionBar anfragen
        	actionBarAvailable=getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        }
         
        inflater = LayoutInflater.from(this);
        setContentView(R.layout.activity_plan);
        //Wenn ActionBar verfügbar ist,
        if(actionBarAvailable)
        {
        	//ActionBar hinzufügen
	        ActionBar actionBar = getActionBar();
	        actionBar.show();
        }
        handler = new Handler();
        
       
        executeWithDialog(new PlanActivityLuncher(this), getString(R.string.msg_start),ProgressDialog.STYLE_SPINNER);
        
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
	    	handler.postDelayed(new Runnable(){
	    		@Override
				public void run() {
					PlanActivity.this.selfCheck();
					
				}}, 2000);
	    }
	    else if(stupid.setupIsDirty)
	    {
	    	stupid.setupIsDirty=false;
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
    	for(int i=0;i<stupid.stupidData.size() && !stupid.dataIsDirty;i++)
    	{
    		if(stupid.stupidData.get(i).isDirty)
    			stupid.dataIsDirty=true;
    	}
    	if(stupid.dataIsDirty)
		{
			try
			{
				Tools.saveFiles(this, stupid, executor);
	    		executor.shutdown();
	    		executor.awaitTermination(120, TimeUnit.SECONDS);
	    		if(!executor.isTerminated())
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
    	try 
    	{
			executor.shutdown();
			executor.awaitTermination(120, TimeUnit.SECONDS);
			if(!executor.isTerminated())
			{
				handler.post(new ErrorMessage(PlanActivity.this,"Beim Beenden ist ein Fehler aufgetreten"));
			}
		} 
    	catch (InterruptedException e) 
    	{
			//hier ist nix zu tun...
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
            	Tools.saveFilesWithProgressDialog(this, stupid, executor, stupid.currentDate);
            	return true;
            case R.id.menu_refresh:
            	refreshWeek();
            	return true;
            case R.id.menu_today:
            	stupid.currentDate=new GregorianCalendar();
            	checkAvailibilityOfWeek(Const.THISWEEK);
            	viewPager.setCurrentItem(Tools.getPage(pageIndex, stupid.currentDate));
            	
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
						checkAvailibilityOfWeek(Const.THISWEEK);
						
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
    	checkAvailibilityOfWeek(Const.FORCEREFRESH,Const.THISWEEK);
    }
    
    /*	5.10.12
     * 	Tobias Janssen
     * 
     * 	Prüft, ob die eingestellte Woche(laut stupid.currentDate) bereits verfügbar ist 
     * 	unternimmt weitere Maßnahmen, wenn nicht, oder veraltet
     * 
     */
    public void checkAvailibilityOfWeek(int weekModificator)
    {
    	checkAvailibilityOfWeek(false,weekModificator);
    }
    
    /*	5.10.12
     * 	Tobias Janssen
     * 
     * 	Prüft, ob die eingestellte Woche(laut stupid.currentDate) bereits verfügbar ist 
     * 	unternimmt weitere Maßnahmen, wenn nicht, oder veraltet
     * 
     */
    public void checkAvailibilityOfWeek(Boolean forceRefresh, int weekOffset)
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
    	
    	Calendar requestedWeek = (Calendar) stupid.currentDate.clone();
    	requestedWeek.setTimeInMillis(requestedWeek.getTimeInMillis()+(86400000*7*weekOffset));//den weekOffset umsetzen
    	
    	int currentDay = requestedWeek.get(Calendar.DAY_OF_WEEK);
    	if(currentDay != 2)
    	{
    		if(currentDay > 2)
    			requestedWeek.setTimeInMillis(requestedWeek.getTimeInMillis()-(86400000*(currentDay-2)));
    		else if(currentDay < 2)
    			requestedWeek.setTimeInMillis(requestedWeek.getTimeInMillis()+(86400000*(2-currentDay)));
     	}
    	//Fehlermeldungen für den Fehlerfall einstellen
    	String notAvail ="";
    	String loading="";
    	String refreshing="";
    	switch(weekOffset)
    	{
    		case Const.NEXTWEEK:
    			notAvail = this.getString(R.string.msg_nextWeekNotAvailable);
    			loading =  this.getString(R.string.msg_loadingData);
    			refreshing =  this.getString(R.string.msg_searchingNewDataNext);
    			break;
    		case Const.LASTWEEK:
    			notAvail = this.getString(R.string.msg_foreWeekNotAvailable);
    			loading =  this.getString(R.string.msg_loadingData);
    			refreshing =  this.getString(R.string.msg_searchingNewDataLast);
    			break;
    		case Const.THISWEEK:
    		default:
    			notAvail = this.getString(R.string.msg_weekNotAvailable);
    			loading =  this.getString(R.string.msg_loadingData);
    			refreshing =  this.getString(R.string.msg_searchingNewDataNow);
    			break;
    	}
    	
    	
        //aus dieser Liste mithilfer der selektierten KalenderWoche den richtigen Index heraussuchen
        weekDataIndexToShow = stupid.getIndexOfWeekData(requestedWeek);
        //prüfen, ob diese Woche bereits im Datenbestand ist
        if(weekDataIndexToShow ==-1)
        {
         	//Woche ist nicht lokal verfügbar       	
        	//Downloader starten, dieser prüft, ob diese Woche erhältlich ist und unternimmt alle weitern Maßnahmen
        	executeWithDialog(new MainDownloader(this,notAvail,requestedWeek),loading,ProgressDialog.STYLE_HORIZONTAL);
        }
        if(weekDataIndexToShow !=-1)
        {
        	//Woche ist im Datenbestand vorhanden
            //Nun prüfen, wie alt diese Daten sind:
        	if(stupid.getWeekOfYear(requestedWeek) < new GregorianCalendar().get(Calendar.WEEK_OF_YEAR) && !forceRefresh)
        	{
        		//diese Woche liegt bereits in der vergangenheit und muss nicht aktualisiert werden
        	}
        	else
        	{
	            Date date = new Date();
	        	if(stupid.myTimetables[weekDataIndexToShow].syncTime + (stupid.myResyncAfter*60*1000) < date.getTime() || forceRefresh)	
	        	{
	        		//veraltet neu herunterladen
	        		executeWithDialog(new MainDownloader(this,notAvail,requestedWeek),refreshing,ProgressDialog.STYLE_HORIZONTAL);
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
        		Tools.loadAllDataFiles(this, stupid);
        		stupid.sort();
        		initViewPager();
        		checkAvailibilityOfWeek(Const.THISWEEK);
        		break;
        		
        	case 1:	//FILESETUP fehlt
        		gotoSetup(Const.FIRSTSTART,true);
        		break;
        		
        	case 2:	//FILESETUP laden fehlgeschlagen
        		gotoSetup(Const.FIRSTSTART,true);
        		break;
        		
        	case 3:	//Keine Klasse ausgewählt
        		gotoSetup(Const.FIRSTSTART,true);
        		break;
        	case 6:	//Elementenordner existiert nicht
        			//neuen anlegen
        			java.io.File elementDir = new java.io.File(this.getFilesDir(),stupid.myElement);
        			elementDir.mkdir();
        			initViewPager();
        			checkAvailibilityOfWeek(Const.THISWEEK);
    		break;
        	case 7:	//Keine Daten für diese Klasse vorhanden
        		initViewPager();
        		checkAvailibilityOfWeek(Const.THISWEEK);
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
    	File[] files = elementDir.listFiles();
        
        if(files.length == 0)
        	return 7;
        
        return 0;
    }
    
    
    
	/* Datum: 11.10.12
	 * Tobias Janßen
	 * Initialisiert den viewPager, der die Tage des Stundenplans darstellt
	 */
    public void initViewPager()
    {
    	currentPage=0;
        for(int i=0;i<stupid.stupidData.size();i++)
        {
        	Tools.appendTimeTableToPager(stupid.stupidData.get(i), stupid, this);
        	
        }
        currentPage=Tools.getPage(pageIndex,stupid.currentDate);

        handler.post( new SetupPager(this, pageIndex, currentPage));
        
               
    }
    

    public boolean gotoSetup(String putExtraName, Boolean value) {
    	
    	try 
    	{
    		Tools.saveFiles(this,stupid,executor);
		} 
    	catch(Exception e) 
    	{
    		
		}
	    Intent intent = new Intent(PlanActivity.this,SetupActivity.class);
	    intent.putExtra(putExtraName, value);
	    startActivityForResult(intent,1);	
	    return true;
    }
    
    public boolean gotoSetup() {
    	
    	try 
    	{
    		Tools.saveFiles(this,stupid,executor);
		} 
    	catch(Exception e) 
    	{
    		
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
    public void executeWithDialog(Runnable run,String text, int style)
    {
    	
    	handler.post(new ShowProgressDialog(this,style,text));
		execQueue.execute(run);
		
    	
    }
    
    

}

