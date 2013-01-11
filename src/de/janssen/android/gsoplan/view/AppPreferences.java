/*
 * AppPreferences.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.view;

import java.io.File;

import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.xml.Xml;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;


 
public class AppPreferences extends PreferenceActivity implements Runnable{
	public MyContext ctxt = new MyContext(this, this);

	private CharSequence[] activityListEntries = new CharSequence[]{"Tag","Woche"};
    private CharSequence[] activityListEntryValues = new CharSequence[]{"Tag","Woche"};
    private CharSequence[] elmentListEntries = null;
    private CharSequence[] elementListEntryValues = null;
    private CharSequence[] typeListEntries = null;
    private CharSequence[] typeListEntryValues = null;
    private CharSequence[] resyncListEntries = new CharSequence[]{"sofort","10min","30min","1h","2h","3h","5h","24h","nie"};
    private CharSequence[] resyncListEntryValues = new CharSequence[]{"1","10","30","60","120","180","300","1440","5256000"};
    
    
    ListPreference typeList;
    ListPreference elementList;
    ListPreference resyncList;
    ListPreference activityList;
    CheckBoxPreference hidePref;
    CheckBoxPreference wlanPref;
    
	public AppPreferences()
	{
		ctxt.context=this;
		ctxt.activity=this;
	}
	
	
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

   
        addPreferencesFromResource(R.xml.preferences);
        ctxt.handler = new Handler();

        if(!loadData())
        {
        	//Online Selectoren laden und anschließend die Runnable(this) ausführen 
        	Tools.fetchOnlineSelectors(ctxt,this);
        }
        else
        {
        	this.run();
        }
       	
       	
               
    }
    
	/// Datum: 14.09.12
  	/// Autor: @author Tobias Janssen
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
    	// die ElementDatei Laden
    	try {
    		Xml xml = new Xml();
    		File elementFile = Tools.getFileSaveElement(ctxt);
    		xml.container = FileOPs.readFromFile(elementFile);
    		ctxt.stupid.clearElements();
    		ctxt.stupid.fetchElementsFromXml(xml,ctxt);
    		return true;
    	} 
    	catch (Exception e) 
    	{
  			return false;
    	}
    	
    }
    
    @SuppressWarnings("deprecation")
	public void setupType()
    {
    	typeListEntries = new CharSequence[ctxt.stupid.typeList.length];
    	typeListEntryValues = new CharSequence[ctxt.stupid.typeList.length];
    	for (int i = 0; i < ctxt.stupid.typeList.length; i++)
		{
    		typeListEntries[i] = ctxt.stupid.typeList[i].description;
    		typeListEntryValues[i] = ctxt.stupid.typeList[i].description;
		}
    	typeList = (ListPreference) findPreference("listType");
    	typeList.setEntryValues(typeListEntryValues);
    	typeList.setEntries(typeListEntries);
    	
    	typeList.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,Object newValue) {
				
				try
				{
					ctxt.stupid.setMyType(findIndexOfValueInTypeList(newValue.toString()));
				}
				catch (Exception e)
				{
					//Type ist ungültig
				}
				Tools.fetchOnlineSelectors(ctxt,AppPreferences.this);
				
				return false;
			}
    		
    	});
    	if(typeListEntries.length >= 0)
    		typeList.setValueIndex(ctxt.stupid.getMyType());
    }
    public int findIndexOfValueInTypeList(String value)
    {
    	int selected = typeList.findIndexOfValue(value);
    	typeList.setValueIndex(selected);
    	return selected;
    }
    
    @SuppressWarnings("deprecation")
	public void setupElement()
    {
    	elmentListEntries = new CharSequence[ctxt.stupid.elementList.length];
    	elementListEntryValues = new CharSequence[ctxt.stupid.elementList.length];
    	for (int i = 0; i < ctxt.stupid.elementList.length; i++)
		{
    		elmentListEntries[i] = ctxt.stupid.elementList[i].description;
    		elementListEntryValues[i] = ctxt.stupid.elementList[i].description;
		}
    	
    	elementList = (ListPreference) findPreference("listElement");
    	elementList.setEntryValues(elementListEntryValues);
        elementList.setEntries(elmentListEntries);
        elementList.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,Object newValue) {
				
				try
				{
					ctxt.stupid.setMyElementValid(newValue.toString());
					elementList.setValue(newValue.toString());
				}
				catch (Exception e)
				{
					//Type ist ungültig
				}
				Tools.saveSetupWithProgressDialog(ctxt);
				return false;
			}
    		
    	});
        elementList.setValue(ctxt.stupid.getMyElement());
    }
    
    @SuppressWarnings("deprecation")
	public void setupResync()
    {
    	resyncList = (ListPreference) findPreference("listResync");
    	resyncList.setEntryValues(resyncListEntryValues);
    	resyncList.setEntries(resyncListEntries);
    	resyncList.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,Object newValue) {
				
				try
				{
					int index = resyncList.findIndexOfValue(newValue.toString());
					if(index >= 0 && index < resyncListEntryValues.length)
					{
						ctxt.stupid.setMyResync(Long.parseLong((String) resyncListEntryValues[index]));
						resyncList.setValue(newValue.toString());
					}
					else
						throw new Exception("Resync ist ungültig");
				}
				catch (Exception e)
				{
					//Resync ist ungültig
				}
				Tools.saveSetupWithProgressDialog(ctxt);
				return false;
			}
    		
    	});
    	long myResync = ctxt.stupid.getMyResync(); 
    	int index = 1;
    	for(int i=0;i<resyncListEntryValues.length;i++)
    	{
    		if(myResync == Long.parseLong((String) resyncListEntryValues[i]))
    		{
    			index = i;
    			break;
    		}
    	}
    	resyncList.setValueIndex(index);
    }
    
    @SuppressWarnings("deprecation")
	public void setupActivity()
    {
    	activityList = (ListPreference) findPreference("listActivity");
    	activityList.setEntryValues(activityListEntryValues);
    	activityList.setEntries(activityListEntries);
    	activityList.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,Object newValue) {
				
				try
				{
					ctxt.setDefaultActivity(newValue.toString());
					activityList.setValue(newValue.toString());
					
				}
				catch (Exception e)
				{
					//Resync ist ungültig
				}
				Tools.saveSetupWithProgressDialog(ctxt);
				return false;
			}
    		
    	});
    	
    	activityList.setValue(ctxt.getDefaultActivity());
    }
    
    @SuppressWarnings("deprecation")
	public void setupHide()
    {
    	hidePref = (CheckBoxPreference) findPreference("boxHide");
    	
    	hidePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,Object newValue) {
				
				try
				{
					if(newValue.toString().equalsIgnoreCase("true"))
						ctxt.stupid.hideEmptyHours=true;
					else
						ctxt.stupid.hideEmptyHours=false;
					
					hidePref.setChecked(ctxt.stupid.hideEmptyHours);
				}
				catch (Exception e)
				{
					//Resync ist ungültig
				}
				Tools.saveSetupWithProgressDialog(ctxt);
				return false;
			}
    		
    	});
    	
    	hidePref.setChecked(ctxt.stupid.hideEmptyHours);
    }
    
    @SuppressWarnings("deprecation")
	public void setupWlanPref()
    {
    	wlanPref = (CheckBoxPreference) findPreference("boxWlan");
    	
    	wlanPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,Object newValue) {
				
				try
				{
					if(newValue.toString().equalsIgnoreCase("true"))
						ctxt.stupid.onlyWlan=true;
					else
						ctxt.stupid.onlyWlan=false;
					
					wlanPref.setChecked(ctxt.stupid.onlyWlan);
				}
				catch (Exception e)
				{
					//Resync ist ungültig
				}
				Tools.saveSetupWithProgressDialog(ctxt);
				return false;
			}
    		
    	});
    	
    	wlanPref.setChecked(ctxt.stupid.onlyWlan);
    }
    
    @Override
	public void finish() 
	{
		super.finish();

		try 
		{
			ctxt.executor.awaitTermination(30 * 1000);
		} 
		catch (Exception e) 
		{
				
				
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
    
	public void run() {
		ctxt.handler.post(new Runnable(){

			public void run() {
				ctxt.getPrefs(ctxt.context.getApplicationContext());
	            setupElement();
	            setupType();
	            setupResync();
	            setupActivity();
	            setupHide();
	            setupWlanPref();
	            Tools.saveElements(ctxt,true);
	            
	            ctxt.progressDialog.dismiss();
			}
	        
		});

		
	}
    
}

