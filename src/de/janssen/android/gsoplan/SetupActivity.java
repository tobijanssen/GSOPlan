package de.janssen.android.gsoplan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
	
	private Spinner spinnerElement;
	private Spinner spinnerType;
	private Spinner spinnerActivity;
	private Spinner spinnerResyncAfter;
	private AsyncTask<Void, Void, Void> task;
	private ArrayAdapter<String> adapterElement;
	private ArrayAdapter<String> adapterType;
	private ArrayAdapter<String> adapterActivity;
	private ArrayAdapter<String> adapterResyncAfter;
	private String[] resyncAfterStrings=new String[]{"sofort","10min","30min","1h","2h","3h","5h","24h","nie"};
	private long[] resyncAfterMinutes=new long[]      {0,10,30,60,120,180,300,1440,5256000};
	public MyContext ctxt = new MyContext();	
	
	public SetupActivity()
	{
		ctxt.context=this;
		ctxt.activity=this;
	}
	
	
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
        
        
        
        ctxt.handler = new Handler();
        initSpinners();
        
        
        //prüfen, ob die Selectoren bereits geladen wurden:
        if(!checkSetupFiles())
        {
        	ctxt.stupid.dataIsDirty=true;
        	ctxt.stupid.setupIsDirty=true;
        	setupDefaultActivity();
        	Tools.fetchOnlineSelectors(ctxt,this);
        }
        else
        {
            loadData();
            setupSpinnerElement();
            setupSpinnerType();
            setupSpinnerResyncAfter();
            setupToggleSwitch();
            setupDefaultActivity();
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
		if(ctxt.stupid.setupIsDirty || ctxt.stupid.dataIsDirty)
		{
			try 
			{
				Tools.saveSetup(ctxt);
			} 
			catch (Exception e) 
			{
				
				
			}
			//Tools.saveSetupWithProgressDialog(ctxt);
			ctxt.stupid.setupIsDirty=true;
		}
		
		Intent returnData = new Intent();
		returnData.putExtra("dataIsDirty", ctxt.stupid.dataIsDirty);
		returnData.putExtra("setupIsDirty", ctxt.stupid.setupIsDirty);
		if (getParent() == null) 
		{
		    setResult(Activity.RESULT_OK, returnData);
		} 
		else 
		{
		    getParent().setResult(Activity.RESULT_OK, returnData);
		}
		ctxt.stupid.setupIsDirty=false;
		
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
					ctxt.stupid.clearSetup();
					setupSpinnerElement();
					setupSpinnerType();
					setupToggleSwitch();
					Tools.fetchOnlineSelectors(ctxt,SetupActivity.this);
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
    		File setupFile = Tools.getFileSaveSetup(ctxt);
    		xml.container = FileOPs.readFromFile(setupFile);
    		ctxt.stupid.clearSetup();
    		ctxt.stupid.fetchSetupFromXml(xml,ctxt);
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
		File setupFile = Tools.getFileSaveSetup(ctxt);
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
        List<String> mylist4 = new ArrayList<String>();
        mylist4.add("keine Daten vorhanden!");
        
        
        adapterElement = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1,mylist1);
        adapterType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1,mylist2);
        adapterResyncAfter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1,mylist3);
        adapterActivity = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1,mylist4);
        
        spinnerElement = (Spinner) findViewById(R.id.spinnerElement);
        spinnerElement.setAdapter(adapterElement);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        spinnerType.setAdapter(adapterType);
        spinnerResyncAfter = (Spinner) findViewById(R.id.spinnerResyncAfter);
        spinnerResyncAfter.setAdapter(adapterResyncAfter);
        this.spinnerActivity = (Spinner) findViewById(R.id.spinnerDefaultAytivity);
        this.spinnerActivity.setAdapter(this.adapterActivity);
    }
    
    public void setupSpinnerResyncAfter()
    {
    	adapterResyncAfter.clear();
    	for(int i=0;i<resyncAfterMinutes.length;i++)
    	{
    		adapterResyncAfter.add(resyncAfterStrings[i]);
    		if(ctxt.stupid.myResyncAfter == resyncAfterMinutes[i])
    			spinnerResyncAfter.setSelection(i); 
    	}
    	adapterResyncAfter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        spinnerResyncAfter.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(ctxt.stupid.myResyncAfter!=resyncAfterMinutes[position])
				{
					ctxt.stupid.myResyncAfter=resyncAfterMinutes[position];
					ctxt.stupid.setupIsDirty=true;
				}
				else
				{
					ctxt.stupid.setupIsDirty=false;
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
		for (int i = 0; i < ctxt.stupid.elementList.length; i++)
		{
			adapterElement.add(ctxt.stupid.elementList[i].description);
			if(ctxt.stupid.myElement.equalsIgnoreCase(ctxt.stupid.elementList[i].description))
			{
				selected=i;
			}
		}
		spinnerElement.setSelection(selected);
		ctxt.stupid.myElement=String.valueOf(spinnerElement.getSelectedItem());
		adapterElement.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerElement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					
					String selectedClass = String.valueOf(spinnerElement.getSelectedItem());
					if(!ctxt.stupid.myElement.equalsIgnoreCase(selectedClass))
					{
						ctxt.stupid.myElement=selectedClass;
						ctxt.stupid.setupIsDirty=true;
						ctxt.stupid.dataIsDirty=true;
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
		for (int i = 0; i < ctxt.stupid.typeList.length; i++)
		{
			adapterType.add(ctxt.stupid.typeList[i].description);
			if(i == ctxt.stupid.myType)
			{
				selected=i;
			}
		}
		spinnerType.setSelection(selected);
		ctxt.stupid.myType=spinnerType.getSelectedItemPosition();
		adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					
					int selectedType = spinnerType.getSelectedItemPosition();
					if(ctxt.stupid.myType != selectedType)
					{
						ctxt.stupid.myType=selectedType;
						Tools.fetchOnlineSelectors(ctxt, SetupActivity.this);
						ctxt.stupid.setupIsDirty=true;
						ctxt.stupid.dataIsDirty=true;
					}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    public void setupDefaultActivity()
    {
    	adapterActivity.clear();
    	int selected =0;
    	
    	adapterActivity.add("Tag");
    	adapterActivity.add("Woche");
    	
    	if(ctxt.defaultActivity != null)
    	{
    		if(ctxt.defaultActivity.equals(PlanActivity.class))
    		{
    			selected = 0;
    		}
    		else if(ctxt.defaultActivity.equals(WeekPlanActivity.class))
    		{
    			selected = 1;
    		}
    		
    	}
    	
		spinnerActivity.setSelection(selected);
		
		adapterActivity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					
					int selectedActivity = spinnerActivity.getSelectedItemPosition();
					if(selectedActivity == 0)
		    		{
						ctxt.defaultActivity = PlanActivity.class;
						
		    		}
		    		else if(selectedActivity == 1)
		    		{
		    			ctxt.defaultActivity = WeekPlanActivity.class;
		    		}
					ctxt.stupid.setupIsDirty=true;
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    

    private void setupToggleSwitch()
    {
    	ToggleButton wlanSwitch = (ToggleButton) findViewById(R.id.toggleButtonWlan);
    	wlanSwitch.setChecked(ctxt.stupid.onlyWlan);

    	wlanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	    	ctxt.stupid.setupIsDirty=true;
    	        if (isChecked) {
    	        	ctxt.stupid.onlyWlan = true;
    	        } else {
    	        	ctxt.stupid.onlyWlan = false;
    	        }
    	    }
    	});
    	
    	ToggleButton hideEmptyHoursSwitch = (ToggleButton) findViewById(R.id.toggleButtonHideEmptyHours);
    	hideEmptyHoursSwitch.setChecked(ctxt.stupid.hideEmptyHours);

    	hideEmptyHoursSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	    	ctxt.stupid.setupIsDirty=true;
    	        if (isChecked) {
    	        	ctxt.stupid.hideEmptyHours = true;
    	        } else {
    	        	ctxt.stupid.hideEmptyHours = false;
    	        }
    	    }
    	});
    }


	public void run() {
		ctxt.handler.post(new Runnable(){

			public void run() {
		    	setupSpinnerElement();
		        setupSpinnerType();
		        setupSpinnerResyncAfter();
		        ctxt.stupid.progressDialog.dismiss();
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
    public void executeWithDialog(AsyncTask<Boolean, Integer, Boolean> task,String text)
    {
    	ctxt.stupid.progressDialog =  new ProgressDialog(this);
    	ctxt.stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	ctxt.stupid.progressDialog.setMessage(text);
    	ctxt.stupid.progressDialog.setCancelable(false);
    	ctxt.stupid.progressDialog.setProgress(0);
    	ctxt.stupid.progressDialog.show();
    	
    	ctxt.executor.execute(task,false);
    }
    
    
 }

