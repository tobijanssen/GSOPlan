package de.janssen.android.gsoplan;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

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
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        handler = new Handler();
        initSpinners();
        
        
        //pr�fen, ob die Selectoren bereits geladen wurden:
        if(!checkSetupFiles())
        {
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
	        case R.id.clear_cache:
	        	clearCache();
	        	stupid.setupIsDirty=true;
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	public void onPause() 
	{
		super.onPause();
	}

	@Override
	public void finish() 
	{
		  // Prepare data intent 
		if(task!=null)
			task.cancel(true);
		if(stupid.setupIsDirty || stupid.dataIsDirty)
		{
			if(stupid.dataIsDirty)
			{
				//Damit die Daten nicht �berschrieben werden
				stupid.dataIsDirty=false;
				Tools.saveFilesWithProgressDialog(this, stupid, exec);
				stupid.dataIsDirty=true;
				
			}
			else
			{
				Tools.saveFilesWithProgressDialog(this, stupid, exec);
			}
			stupid.setupIsDirty=true;
			exec.shutdown();
			try {
				exec.awaitTermination(120, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			/*
			int timer=0;
			while(!File.ready && timer < 15)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
		
				}
				timer++;
			}*/
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
		super.finish();
	}

	@Override
	protected void onDestroy() 
	{
	    super.onDestroy();
	    try {
			exec.shutdown();
			exec.awaitTermination(120, TimeUnit.SECONDS);
			exec.shutdownNow();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public void clearCache() {
    	
    	new AlertDialog.Builder(SetupActivity.this)
			.setTitle("Achtung")
			.setMessage("Soll die Anwendung wirklich in den Auslieferzustand zur�ckgesetzt werden?\nDabei werden alle Daten dieser Anwendung gel�scht!")
			
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
  	/// Autor: Tobias Jan�en
  	///
  	///	Beschreibung:
  	///	Pr�ft, ob die Selectoren bereits in einer Datei vorliegen, um bei nicht vorhandensein
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
    		xml.container = File.readFromFile(this, Tools.FILESETUP);
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
  	/// Autor: Tobias Jan�en
  	///
  	///	Beschreibung:
  	///	Pr�ft, ob die Selectoren bereits in einer Datei vorliegen, um bei nicht vorhandensein
  	///	diese vom Gso Server zu laden
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public Boolean checkSetupFiles()
    {
		// Pr�fen, ob die ben�tigten Dateien existieren:
		java.io.File testFile = new java.io.File(this.getFilesDir(), Tools.FILESETUP);
		if (!testFile.exists())
			return false;

    	return true;
    }
    
    /* Datum: 26.09.12
     * Autor: Tobias Jan�en
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
    	for(int i=0;i<stupid.resyncAfterMinutes.length;i++)
    	{
    		adapterResyncAfter.add(stupid.resyncAfterStrings[i]);
    		if(stupid.myResyncAfter == stupid.resyncAfterMinutes[i])
    			spinnerResyncAfter.setSelection(i); 
    	}
    	adapterResyncAfter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        spinnerResyncAfter.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(stupid.myResyncAfter!=stupid.resyncAfterMinutes[position])
				{
					stupid.myResyncAfter=stupid.resyncAfterMinutes[position];
					stupid.setupIsDirty=true;
				}
				else
				{
					stupid.setupIsDirty=false;
				}
			}

			@Override
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

			@Override
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

			@Override
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

			@Override
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

			@Override
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
    
    /// Datum: 14.09.12
  	/// Autor: Tobias Jan�en
  	///
  	///	Beschreibung:
  	///	L�dt die Selectoren von der GSO Seite und parsed diese in die availableOnline Arrays
  	///	
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
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
   	}

	@Override
	public void run() {
		handler.post(new Runnable(){

			@Override
			public void run() {
		    	setupSpinnerElement();
		        setupSpinnerType();
		        stupid.progressDialog.dismiss();
			}
	        
		});

		
	}
	
	
    /// Datum: 28.09.12
  	/// Autor: Tobias Jan�en
  	///
  	///	Beschreibung:
  	///	Erstellt einen neuen ProgressDialog mit �bergebenem Text
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

