package de.janssen.android.gsoplan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.Button;

public class SetupActivity extends Activity implements Runnable{
	public StupidCore stupid = new StupidCore();;
	private Spinner spinnerElement;
	private Spinner spinnerType;
	private Spinner spinnerResyncAfter;
	private Handler handler;
	private AsyncTask<Void, Void, Void> task;
	private ArrayAdapter<String> adapterElement;
	private ArrayAdapter<String> adapterType;
	private ArrayAdapter<String> adapterResyncAfter;
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private String[] resyncAfterStrings=new String[]{"sofort","10min","30min","1h","2h","3h","5h","24h","nie"};
	private long[] resyncAfterMinutes=new long[]      {0,10,30,60,120,180,300,1440,5256000};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) 
        {
        	if(extras.getBoolean(Const.FIRSTSTART))
        	{
             View readyButton = (Button) findViewById(R.id.readyButton);
             readyButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					SetupActivity.this.finish();
				}
            	 
             });
             readyButton.setVisibility(Button.VISIBLE);
            } 
        }
        
        
        
        handler = new Handler();
        initSpinners();
        
        
        //prüfen, ob die Selectoren bereits geladen wurden:
        if(!checkSetupFiles())
        {
        	stupid.dataIsDirty=true;
        	stupid.setupIsDirty=true;
        	Tools.fetchOnlineSelectors(this, stupid, exec,this);
        }
        else
        {
            loadData();
            setupSpinnerElement();
            setupSpinnerType();
            setupSpinnerResyncAfter();
            setupToggleSwitch();
        }
        
    	
        
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.activity_setup, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        //case R.id.clear_cache:
	        	//clearCache();
	        	//stupid.setupIsDirty=true;
	          //  return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	/*
	@Override
	public void onPause() 
	{
		super.onPause();
	}*/

	@Override
	public void finish() 
	{
		super.finish();

		  // Prepare data intent 
		if(task!=null)
			task.cancel(true);
		if(stupid.setupIsDirty || stupid.dataIsDirty)
		{
			try {
				Tools.saveSetup(this, stupid, exec);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Tools.saveSetupWithProgressDialog(this, stupid, exec);

			exec.shutdown();
			try 
			{
				exec.awaitTermination(120, TimeUnit.SECONDS);
				
			} 
			catch (InterruptedException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			stupid.setupIsDirty=true;
		}
		
		Intent returnData = new Intent();
		returnData.putExtra("dataIsDirty", stupid.dataIsDirty);
		returnData.putExtra("setupIsDirty", stupid.setupIsDirty);
		if (getParent() == null) 
		{
		    setResult(Activity.RESULT_OK, returnData);
		} 
		else 
		{
		    getParent().setResult(Activity.RESULT_OK, returnData);
		}
		stupid.setupIsDirty=false;
		
	}
/*
	@Override
	protected void onDestroy() 
	{
	    super.onDestroy();
	    /*
	    try {
			exec.shutdown();
			exec.awaitTermination(120, TimeUnit.SECONDS);
			exec.shutdownNow();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}*/
	
	public void clearCache() {
    	
    	new AlertDialog.Builder(SetupActivity.this)
			.setTitle("Achtung")
			.setMessage("Soll die Anwendung wirklich in den Auslieferzustand zurückgesetzt werden?\nDabei werden alle Daten dieser Anwendung gelöscht!")
			
			.setPositiveButton("Ja",new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog,	int which) 
				{
					java.io.File appDir = SetupActivity.this.getFilesDir();
					String[] files = appDir.list();
					for(int i=0; i<files.length;i++)
					{
						java.io.File delFile = new java.io.File(appDir, files[i]);
				    	delFile.delete();
					}
					stupid.clearSetup();
					setupSpinnerElement();
					setupSpinnerType();
					setupToggleSwitch();
					Tools.fetchOnlineSelectors(SetupActivity.this, stupid, exec,SetupActivity.this);
				}
			})
    		.setNegativeButton("Abbruch", new DialogInterface.OnClickListener(){
    				public void onClick(DialogInterface dialog,	int which) 
    				{
    					//Do nothing
    				}
    			}).show();

    }
	
	/// Datum: 14.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Prüft, ob die Selectoren bereits in einer Datei vorliegen, um bei nicht vorhandensein
  	///	diese vom Gso Server zu laden
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public Boolean loadData()
    {
    	// die SetupDatei Laden
    	try {
    		Xml xml = new Xml();
    		File setupFile = Tools.getFileSaveSetup(this, stupid);
    		xml.container = FileOPs.readFromFile(this,setupFile);
    		stupid.clearSetup();
    		stupid.fetchSetupFromXml(xml);
    		return true;
    	} 
    	catch (Exception e) 
    	{
    		new AlertDialog.Builder(SetupActivity.this)
    				.setTitle("Fehler")
    				.setMessage("Beim laden der SetupXml Datei ist ein Fehler aufgetreten: "+ e.getMessage())
    				.setPositiveButton("Ja",new DialogInterface.OnClickListener() 
    				{
    					public void onClick(DialogInterface dialog,	int which) 
    					{
   						}
  					});
  			return false;
    	}
    	
    }
    
    /// Datum: 14.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Prüft, ob die Selectoren bereits in einer Datei vorliegen, um bei nicht vorhandensein
  	///	diese vom Gso Server zu laden
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public Boolean checkSetupFiles()
    {
		// Prüfen, ob die benötigten Dateien existieren:
		File setupFile = Tools.getFileSaveSetup(this, stupid);
		if (!setupFile.exists())
			return false;

    	return true;
    }
    
    /* Datum: 26.09.12
     * Autor: Tobias Janßen
     * 
     * 
     * 
     * 
     * 
     * 
     */
    private void initSpinners()
    {
        List<String> mylist1 = new ArrayList<String>();
        mylist1.add("keine Daten vorhanden!");
        List<String> mylist2 = new ArrayList<String>();
        mylist2.add("keine Daten vorhanden!");
        List<String> mylist3 = new ArrayList<String>();
        mylist3.add("keine Daten vorhanden!");
        
        adapterElement = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1,mylist1);
        adapterType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1,mylist2);
        adapterResyncAfter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1,mylist3);
        
        spinnerElement = (Spinner) findViewById(R.id.spinnerElement);
        spinnerElement.setAdapter(adapterElement);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        spinnerType.setAdapter(adapterType);
        spinnerResyncAfter = (Spinner) findViewById(R.id.spinnerResyncAfter);
        spinnerResyncAfter.setAdapter(adapterResyncAfter);
    }
    
    public void setupSpinnerResyncAfter()
    {
    	adapterResyncAfter.clear();
    	for(int i=0;i<resyncAfterMinutes.length;i++)
    	{
    		adapterResyncAfter.add(resyncAfterStrings[i]);
    		if(stupid.myResyncAfter == resyncAfterMinutes[i])
    			spinnerResyncAfter.setSelection(i); 
    	}
    	adapterResyncAfter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        spinnerResyncAfter.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(stupid.myResyncAfter!=resyncAfterMinutes[position])
				{
					stupid.myResyncAfter=resyncAfterMinutes[position];
					stupid.setupIsDirty=true;
				}
				else
				{
					stupid.setupIsDirty=false;
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
        	
        });
		

    }
    
    public void setupSpinnerElement()
    {
    	adapterElement.clear();
    	int selected=0;
		for (int i = 0; i < stupid.elementList.length; i++)
		{
			adapterElement.add(stupid.elementList[i].description);
			if(stupid.myElement.equalsIgnoreCase(stupid.elementList[i].description))
			{
				selected=i;
			}
		}
		spinnerElement.setSelection(selected);
		stupid.myElement=String.valueOf(spinnerElement.getSelectedItem());
		adapterElement.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerElement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					
					String selectedClass = String.valueOf(spinnerElement.getSelectedItem());
					if(!stupid.myElement.equalsIgnoreCase(selectedClass))
					{
						stupid.myElement=selectedClass;
						stupid.setupIsDirty=true;
						stupid.dataIsDirty=true;
					}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    public void setupSpinnerType()
    {
    	adapterType.clear();
    	int selected=0;
		for (int i = 0; i < stupid.typeList.length; i++)
		{
			adapterType.add(stupid.typeList[i].description);
			if(i == stupid.myType)
			{
				selected=i;
			}
		}
		spinnerType.setSelection(selected);
		stupid.myType=spinnerType.getSelectedItemPosition();
		adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					
					int selectedType = spinnerType.getSelectedItemPosition();
					if(stupid.myType != selectedType)
					{
						stupid.myType=selectedType;
						Tools.fetchOnlineSelectors(SetupActivity.this, stupid, exec,SetupActivity.this);
						stupid.setupIsDirty=true;
						stupid.dataIsDirty=true;
					}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    

    private void setupToggleSwitch()
    {
    	ToggleButton wlanSwitch = (ToggleButton) findViewById(R.id.toggleButtonWlan);
    	wlanSwitch.setChecked(stupid.onlyWlan);

    	wlanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	    	stupid.setupIsDirty=true;
    	        if (isChecked) {
    	            stupid.onlyWlan = true;
    	        } else {
    	        	stupid.onlyWlan = false;
    	        }
    	    }
    	});
    }
    
    
    //TODO: Remove:
    /// Datum: 14.09.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Lädt die Selectoren von der GSO Seite und parsed diese in die availableOnline Arrays
  	///	
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    /*
    public void fetchOnlineSelectorsDepricated()
    {

       	try
    	{
    		//aktuelle Daten aus dem Netz laden:
            if(stupid.onlyWlan)
            {
            	if(Tools.isWifiConnected(this))
            	{
	            	stupid.progressDialog = ProgressDialog.show(this, getString(R.string.setup_message_dlElements_title), getString(R.string.setup_message_dlElements_body), true,false);
	            	stupid.setupIsDirty=true;
            	}
            	else
                {
                	Toast.makeText(this, "Keine Wlan Verbindung!", Toast.LENGTH_SHORT).show();
                }
            }
            else
        	{
            	stupid.progressDialog = ProgressDialog.show(this, getString(R.string.setup_message_dlElements_title), getString(R.string.setup_message_dlElements_body), true,false);
            	stupid.setupIsDirty=true;
        	}
            
    	}
    	catch(Exception e)
    	{
    		new AlertDialog.Builder(SetupActivity.this)
    	    .setTitle("Fehler")
    	    .setMessage(getString(R.string.setup_message_error_dlElements_1))
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
   	}*/

	public void run() {
		handler.post(new Runnable(){

			public void run() {
		    	setupSpinnerElement();
		        setupSpinnerType();
		        setupSpinnerResyncAfter();
		        stupid.progressDialog.dismiss();
			}
	        
		});

		
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

