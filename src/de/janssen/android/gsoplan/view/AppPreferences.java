/*
 * AppPreferences.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.view;

import java.io.File;
import listener.OnElementChangeListener;
import listener.OnTypeChangeListener;
import de.janssen.android.gsoplan.interfaces.ListEntry;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.Menu;
import android.view.MenuItem;
import de.janssen.android.gsoplan.Const;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.xml.Xml;

public class AppPreferences extends PreferenceActivity implements Runnable
{
    public MyContext ctxt = new MyContext(this, this);
    private ListEntry element1Pref = new ListEntry(new CharSequence[]{ "keine Einträge" },new CharSequence[]{ "keine Einträge" },"listElement1");
    private ListEntry element2Pref = new ListEntry(new CharSequence[]{ "keine Einträge" },new CharSequence[]{ "keine Einträge" },"listElement2");
    private ListEntry type1Pref = new ListEntry(new CharSequence[]{ "keine Einträge" },new CharSequence[]{ "keine Einträge" },"listType1");
    private ListEntry type2Pref = new ListEntry(new CharSequence[]{ "keine Einträge" },new CharSequence[]{ "keine Einträge" },"listType2");
    private ListEntry resyncPref = new ListEntry(new CharSequence[] { "sofort", "10min", "30min", "1h", "2h", "3h", "5h","24h", "nie" },
	    		new CharSequence[] { "1", "10", "30", "60", "120", "180", "300","1440", "5256000" }, "listResync");
    private ListEntry activitiesPref = new ListEntry(new CharSequence[] { "Tag", "Woche" },new CharSequence[] { "Tag", "Woche" },"listActivity");
    private CheckBoxPreference hidePref = null;
    private CheckBoxPreference useFav = null;
    private CheckBoxPreference wlanPref = null;
    private PreferenceCategory cat2ndProfil;
    
    @Override
    protected void onResume()
    {
	super.onResume();
	ctxt.mIsRunning=true;
    }
    @Override
    protected void onPause()
    {
	super.onPause();
	ctxt.mIsRunning=false;
    }
    
    @Override
    protected void onDestroy()
    {
	ctxt.executor.terminateAllThreads();
	super.onDestroy();
    }

    public AppPreferences()
    {
	ctxt.context = this;
	ctxt.activity = this;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	
	addPreferencesFromResource(R.xml.preferences);
	ctxt.handler = new Handler();
	if(ctxt.getCheckboxPreference(Const.CHECKBOXPROFILID))
	{
	    ctxt.switchStupidTo(1);
	    if (!loadData())
	    {
		ctxt.getCurStupid().fetchOnlineSelectors(ctxt,new Runnable(){

		    @Override
		    public void run()
		    {
			if (!loadData())
			{
				    // Online Selectoren laden und anschließend die Runnable(this)
				    // ausführen
			    ctxt.getCurStupid().fetchOnlineSelectors(ctxt, AppPreferences.this);
			}
			else
			{
			    this.run();
			}
		    }
		    
		});
	    }
	    else
	    {
		ctxt.switchStupidTo(0);
		if (!loadData())
		{
        		    // Online Selectoren laden und anschließend die Runnable(this)
        		    // ausführen
		    ctxt.getCurStupid().fetchOnlineSelectors(ctxt, AppPreferences.this);
        	}
        	else
        	{
        	    this.run();
        	}
	    }
	}
	else
	{
	    ctxt.switchStupidTo(0);
	    if (!loadData())
	    {
		// Online Selectoren laden und anschließend die Runnable(this)
		    // ausführen
		ctxt.getCurStupid().fetchOnlineSelectors(ctxt, this);
	    }
	    else
	    {
		this.run();
	    }
	}
	

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	getMenuInflater().inflate(R.menu.activity_preferences, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	switch (item.getItemId())
	{
	case R.id.menu_favorite:

	    try
	    {
		ctxt.getCurStupid().saveFiles(ctxt);
	    }
	    catch (Exception e)
	    {

	    }

	    Intent intent = new Intent(ctxt.activity, this.getClass());
	    ctxt.activity.startActivity(intent);
	    this.finish();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    /**
     * Prüft, ob die Selectoren bereits in einer Datei vorliegen, um bei nicht
     * vorhandensein
     * <p>
     * diese vom Gso Server zu laden
     * 
     * @author Tobias Janssen
     * @return
     */
    public Boolean loadData()
    {
	// die ElementDatei Laden
	try
	{
	    File elementFile = ctxt.getCurStupid().getFileSaveElement(ctxt);
	    Xml xml = new Xml("root", FileOPs.readFromFile(elementFile));
	    ctxt.getCurStupid().clearElements();
	    ctxt.getCurStupid().fetchElementsFromXml(xml, ctxt);
	    return true;
	}
	catch (Exception e)
	{
	    return false;
	}

    }

    /**
     * @author Tobias Janssen
     */
    @SuppressWarnings("deprecation")
    public void setupType(ListEntry pref,int index)
    {
	pref.entries = new CharSequence[0];
	pref.vals = new CharSequence[0];

	if(ctxt.stupid[index].typeList.length > 0)
	{
	    pref.entries = new CharSequence[ctxt.stupid[index].typeList.length];
	    pref.vals = new CharSequence[ctxt.stupid[index].typeList.length];
	}
	    
	for (int i = 0; i < ctxt.stupid[index].typeList.length; i++)
	{
	    pref.entries[i] = ctxt.stupid[index].typeList[i].description;
	    pref.vals[i] = ctxt.stupid[index].typeList[i].description;
	}

	if (pref.entries.length == 0 || pref.vals.length == 0)
	{
	    //TODO: hier darf nicht keine Einträge stehen!?
	    pref.entries = new CharSequence[1];
	    pref.vals = new CharSequence[1];
	    pref.entries[0] = "keine Einträge";
	    pref.vals[0] = "keine Einträge";
	    pref.list = (ListPreference) findPreference(pref.prefKey);
	    pref.list.setEntryValues(pref.vals);
	    pref.list.setEntries(pref.entries);
	}
	else
	{
	    pref.list = (ListPreference) findPreference(pref.prefKey);
	    pref.list.setEntryValues(pref.vals);
	    pref.list.setEntries(pref.entries);
	    pref.list.setOnPreferenceChangeListener(new OnTypeChangeListener(ctxt, pref.list, index, this));

	    if (pref.entries.length >= ctxt.stupid[index].getMyType())
	    {
		pref.list.setValueIndex(ctxt.stupid[index].getMyType());
	    }
	}

    }

    /**
     * @author Tobias Janssen
     */
    @SuppressWarnings("deprecation")
    public void setupElement(ListEntry pref,int index)
    {
	
	pref.entries = new CharSequence[0];
	pref.vals = new CharSequence[0];

	if(ctxt.stupid[index].elementList.length > 0)
	{
	    pref.entries = new CharSequence[ctxt.stupid[index].elementList.length];
	    pref.vals = new CharSequence[ctxt.stupid[index].elementList.length];
	}
	for (int i = 0; i < ctxt.stupid[index].elementList.length; i++)
	{
	    pref.entries[i] = ctxt.stupid[index].elementList[i].description;
	    pref.vals[i] = ctxt.stupid[index].elementList[i].description;
	}
	pref.list = (ListPreference) findPreference(pref.prefKey);
	if (pref.entries.length == 0 || pref.vals.length == 0)
	{
	    pref.entries = new CharSequence[1];
	    pref.vals = new CharSequence[1];
	    pref.entries[0] = "bitte erst Typ festlegen";
	    pref.vals[0] = "bitte erst Typ festlegen";
	    pref.list.setEntryValues(pref.vals);
	    pref.list.setEntries(pref.entries);
	}
	else
	{
	    pref.list.setEntryValues(pref.vals);
	    pref.list.setEntries(pref.entries);
	    pref.list.setOnPreferenceChangeListener(new OnElementChangeListener(ctxt, pref.list, index));
	    pref.list.setValue(ctxt.stupid[index].getMyElement());
	}

    }

    /**
     * @author Tobias Janssen
     */
    @SuppressWarnings("deprecation")
    public void setupResync()
    {
	resyncPref.list = (ListPreference) findPreference(resyncPref.prefKey);
	resyncPref.list.setEntryValues(resyncPref.vals);
	resyncPref.list.setEntries(resyncPref.entries);
	resyncPref.list.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	{

	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue)
	    {

		try
		{
		    int index = resyncPref.list.findIndexOfValue(newValue.toString());
		    if (index >= 0 && index < resyncPref.vals.length)
		    {
			ctxt.getCurStupid().setMyResync(Long.parseLong((String) resyncPref.vals[index]));
			resyncPref.list.setValue(newValue.toString());
		    }
		    else
			throw new Exception("Resync ist ungültig");
		}
		catch (Exception e)
		{
		    // Resync ist ungültig
		}
		// Tools.saveSetupWithProgressDialog(ctxt);
		return false;
	    }

	});
	long myResync = ctxt.getCurStupid().getMyResync();
	int index = 1;
	for (int i = 0; i < resyncPref.vals.length; i++)
	{
	    if (myResync == Long.parseLong((String) resyncPref.vals[i]))
	    {
		index = i;
		break;
	    }
	}
	resyncPref.list.setValueIndex(index);
    }

    /**
     * @author Tobias Janssen
     */
    @SuppressWarnings("deprecation")
    public void setupActivity()
    {
	activitiesPref.list = (ListPreference) findPreference(activitiesPref.prefKey);
	if(activitiesPref.vals.length != 0 && activitiesPref.entries.length != 0)
	{
	    activitiesPref.list.setEntryValues(activitiesPref.vals);
	    activitiesPref.list.setEntries(activitiesPref.entries);
	    activitiesPref.list.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	    {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue)
		{

		    try
		    {
			ctxt.setDefaultActivity(newValue.toString());
			activitiesPref.list.setValue(newValue.toString());

		    }
		    catch (Exception e)
		    {
			// Resync ist ungültig
		    }
		    // Tools.saveSetupWithProgressDialog(ctxt);
		    return false;
		}

	    });

	    activitiesPref.list.setValue(ctxt.getDefaultActivity());
	}
    }

    /**
     * @author Tobias Janssen
     */
    @SuppressWarnings("deprecation")
    public void setupHide()
    {
	hidePref = (CheckBoxPreference) findPreference("boxHide");

	hidePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	{

	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue)
	    {

		try
		{
		    if (newValue.toString().equalsIgnoreCase("true"))
			ctxt.getCurStupid().hideEmptyHours = true;
		    else
			ctxt.getCurStupid().hideEmptyHours = false;

		    hidePref.setChecked(ctxt.getCurStupid().hideEmptyHours);
		}
		catch (Exception e)
		{
		    // Resync ist ungültig
		}
		// Tools.saveSetupWithProgressDialog(ctxt);
		return false;
	    }

	});

	hidePref.setChecked(ctxt.getCurStupid().hideEmptyHours);
    }

    /**
     * @author Tobias Janssen
     */
    @SuppressWarnings("deprecation")
    public void setupWlanPref()
    {
	wlanPref = (CheckBoxPreference) findPreference("boxWlan");

	wlanPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	{

	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue)
	    {

		try
		{
		    if (newValue.toString().equalsIgnoreCase("true"))
			ctxt.getCurStupid().onlyWlan = true;
		    else
			ctxt.getCurStupid().onlyWlan = false;

		    wlanPref.setChecked(ctxt.getCurStupid().onlyWlan);
		}
		catch (Exception e)
		{
		    // Resync ist ungültig
		}
		// Tools.saveSetupWithProgressDialog(ctxt);
		return false;
	    }

	});

	wlanPref.setChecked(ctxt.getCurStupid().onlyWlan);
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
	if (getParent() == null)
	{
	    setResult(Activity.RESULT_OK, returnData);
	}
	else
	{
	    getParent().setResult(Activity.RESULT_OK, returnData);
	}
    }

    private void setupCheckBoxFav()
    {
	useFav = (CheckBoxPreference) findPreference(Const.CHECKBOXPROFILID);

	if (!useFav.isChecked() && type2Pref.list != null && element2Pref.list != null)
	{
	    cat2ndProfil.removePreference(type2Pref.list);
	    cat2ndProfil.removePreference(element2Pref.list);
	}
	useFav.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	{

	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue)
	    {

		try
		{
		    if (newValue.toString().equalsIgnoreCase("true"))
		    {
			cat2ndProfil.addPreference(type2Pref.list);
			cat2ndProfil.addPreference(element2Pref.list);
			useFav.setChecked(true);
			ctxt.switchStupidTo(1);
			if (!loadData())
			{
			    ctxt.getCurStupid().fetchOnlineSelectors(ctxt, AppPreferences.this);
			}
			else
			{
			    AppPreferences.this.run();
			}
		    }
		    else
		    {
			cat2ndProfil.removePreference(type2Pref.list);
			cat2ndProfil.removePreference(element2Pref.list);
			useFav.setChecked(false);
		    }
		}
		catch (Exception e)
		{

		}
		return false;
	    }

	});
    }

    public void run()
    {
	ctxt.handler.post(new Runnable()
	{

	    public void run()
	    {
		cat2ndProfil = (PreferenceCategory) findPreference("2ndProfil");
		type2Pref.list = (ListPreference) findPreference(type2Pref.prefKey);
		element2Pref.list = (ListPreference) findPreference(element2Pref.prefKey);
		ctxt.switchStupidTo(0);
		ctxt.getPrefs(ctxt.context.getApplicationContext());
		// Je nachdem ein Häkchen für 2.Profil gesetzt Optionen
		// hinzufügen
		setupCheckBoxFav();
		// Hauptprofil initialisieren
		setupElement(element1Pref, 0);
		setupType(type1Pref,0);
		if(ctxt.getCheckboxPreference(Const.CHECKBOXPROFILID))
		{
		    ctxt.switchStupidTo(1);
		    ctxt.getPrefs(ctxt.context.getApplicationContext());
		    setupElement(element2Pref,1);
		    setupType(type2Pref,1);
		}
		setupResync();
		setupActivity();
		setupHide();
		setupWlanPref();
		
		ctxt.getCurStupid().saveElements(ctxt, true);
		
		if(ctxt.getCheckboxPreference(Const.CHECKBOXPROFILID))
		{
		   
		    ctxt.getCurStupid().saveElements(ctxt, true);
		}

		 ctxt.progressDialog.dismiss();
	    }

	});

    }

}
