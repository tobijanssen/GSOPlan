/*
 * MyContext.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan;

import java.util.Calendar;
import de.janssen.android.gsoplan.core.Stupid;
import de.janssen.android.gsoplan.runnables.AppendPage;
import de.janssen.android.gsoplan.runnables.ErrorMessage;
import de.janssen.android.gsoplan.runnables.SetupPager;
import de.janssen.android.gsoplan.view.PlanActivity;
import de.janssen.android.gsoplan.view.WeekPlanActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;

public class MyContext
{
    public Stupid[] stupid = new Stupid[2];
    public Exception exception;
    public Calendar dateBackup;
    public Handler handler;
    public Boolean selfCheckIsRunning = false;
    
    public WorkerQueue executor = new WorkerQueue(this);
    
    public Pager pager= new Pager();
    
    public Context context;
    public Activity activity;
    public Boolean weekView = false;
    public Boolean dayView = false;
    public Boolean initialLunch = true;
    public LayoutInflater inflater;
    public int[] textSizes;
    private Class<?> defaultActivity;
    public Boolean forceView = false;
    public ProgressDialog progressDialog;
    public Boolean newVersionInfo = false;
    public String newVersionMsg = "";
    private int select = 0;
    private int selectBkp = 0;
    private String elementPrefKey = "listElement1";
    private String typePrefKey = "listType1";
    public Menu appMenu;
    public Boolean mIsRunning = false;
    
    public MyContext(ContextWrapper appctxt, Activity activity)
    {
	this.stupid[0] = new Stupid();
	this.stupid[1] = new Stupid();
	this.context = appctxt;
	this.activity = activity;
    }
    public int getSelector()
    {
	return this.select;
    }

    public Class<?> getDefaultActivityClass()
    {
	return this.defaultActivity;
    }
    
    public void switchStupidBack()
    {
	if(this.selectBkp == 0)
	    switchStupidTo(0);
	if(this.selectBkp == 1)
	    switchStupidTo(1);
    }
    
    public void switchStupidTo(int arg0)
    {
	if(arg0 == 1)
	{
	    if(selectBkp!=select)
		selectBkp=select;
	    this.select=arg0;
	    elementPrefKey = "listElement2";
	    typePrefKey = "listType2";
	}
	else if(arg0 == 0)
	{
	    if(selectBkp!=select)
		selectBkp=select;
	    this.select=arg0;
	    elementPrefKey = "listElement1";
	    typePrefKey = "listType1";
	}
    }
    
    public Stupid getCurStupid()
    {
	if(select == 0)
	    return stupid[0];
	else if(select == 1)
	    return stupid[1];
	else
	    return stupid[0];
    }
    
    public void switchStupid()
    {
	if(select == 0)
	{
	    this.selectBkp=select;
	    elementPrefKey = "listElement2";
	    typePrefKey = "listType2";
	    select = 1;
	}
	else
	{
	    this.selectBkp=select;
	    elementPrefKey = "listElement1";
	    typePrefKey = "listType1";
	    select = 0;
	}
	    
    }

    /**
     * @author janssen
     * @return			String mit der Default activity
     */
    public String getDefaultActivity()
    {
	if (this.defaultActivity.equals(PlanActivity.class))
	    return "Tag";
	else if (this.defaultActivity.equals(WeekPlanActivity.class))
	    return "Woche";
	else
	{
	    try
	    {
		setDefaultActivity("Tag");
	    }
	    catch (Exception e)
	    {
		// Trifft nicht zu!
	    }
	    return "Tag";
	}

    }

    /**
     * @author Tobias Janssen 
     * @param value		String, der enweder "tag", oder "woche" enthält
     * @throws Exception	Wenn String ungültig
     */
    public void setDefaultActivity(String value) throws Exception
    {
	if (value.equalsIgnoreCase("tag"))
	    this.defaultActivity = PlanActivity.class;
	else if (value.equalsIgnoreCase("woche"))
	    this.defaultActivity = WeekPlanActivity.class;
	else
	    throw new Exception("Ungültiger Wert! Nur 'tag' & 'woche' sind gültig");
    }
    
    /**
     * Liefert den Wert der im Key übergebenen CheckboxPreference zurück
     * @param key		String, der den Preference-Key enthält
     * @return			Boolean, default false;
     */
    public Boolean getCheckboxPreference(String key)
    {
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	return prefs.getBoolean(key, false);
    }
    
    /**
     * @author Tobias Janssen 
     * lädt die einstellungen der applikation
     * 
     * @param context 	Context der Applikation
     */
    public void getPrefs(Context context)
    {
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	
	//prüfen, ob noch alte Settingsversionen vorhanden sind, wenn ja diese konvertieren und löschen
	Tools.translateOldSettings(this);

	try
	{
	    if (stupid[select].getMyElement().isEmpty())
	    {
		String element = prefs.getString(elementPrefKey, "");
		stupid[select].setMyElementValid(element);
	    }
	}
	catch (Exception e)
	{
	    // Element ist ungültig
	}
	try
	{
	    stupid[select].setMyType(prefs.getString(typePrefKey, "Klassen"));
	}
	catch (Exception e)
	{
	    // listType ist ungültig
	}
	try
	{
	    String value = prefs.getString("listResync", "10");
	    stupid[select].setMyResync(Long.parseLong(value));
	}
	catch (Exception e)
	{
	    // Resync ist ungültig
	}
	try
	{
	    setDefaultActivity(prefs.getString("listActivity", "Tag"));
	}
	catch (Exception e)
	{
	    // Resync ist ungültig
	}

	stupid[select].hideEmptyHours = prefs.getBoolean("boxHide", false);
	stupid[select].onlyWlan = prefs.getBoolean("boxWlan", false);

    }

    
    
    /**
     * @author Tobias Janssen
     * Führt die Laufzeitprüfung durch, und ergreift nötige Maßbahmen im Fehlerfall
     * 
     */
    public void selfCheck()
    {
	selfCheck(0);
    }


    /**
     * @author Tobias Janssen
     * Führt die Laufzeitprüfung durch, und ergreift nötige Maßbahmen im Fehlerfall
     * 
     * @param prevErrorCode		Integer der den vorherigen Fehler angibt
     */
    private void selfCheck(int prevErrorCode)
    {
	//wenn Profil aktiviert, prüfen, was das letzt-genutze war
	if(this.getCheckboxPreference(Const.CHECKBOXPROFILID))
	    Tools.loadProfileFile(this);
	else
	    this.switchStupidTo(0);

	selfCheckIsRunning = true;
	switch (getCurStupid().checkStructure(this))
	{
	case 0: // Alles in Ordnung
	    try
	    {
		Tools.loadAllDataFiles(this.context, getCurStupid());
	    }
	    catch (Exception e)
	    {
		handler.post(new ErrorMessage(this, e.getMessage()));
	    }
	    getCurStupid().sort();
	    initViewPager();
	    getCurStupid().checkAvailibilityOfWeek(this, Const.THISWEEK);
	    break;

	case 1: // FILEELEMENT Datei fehlt
	    if (prevErrorCode == 1) // prüfen, wie oft dieser vorgung bereit
				    // durchgeführt wurde
	    {
		// bereits das zweite Mal, dass der Fehler Auftritt
		// TODO: was sind die Gründe dafür, dass die Datei nun immer
		// noch nicht existiert?
	    }
	    else
	    {
		// Selectoren aus dem Netz laden
		getCurStupid().fetchOnlineSelectors(this, null);
		getCurStupid().saveElements(this, new Runnable()
		{
		    @Override
		    public void run()
		    {
			selfCheck(1);
		    }
		}, true);
	    }
	    break;
	case 3: // Keine Klasse ausgewählt
	    OnClickListener onClick = new OnClickListener()
	    {

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    Tools.gotoSetup(MyContext.this, Const.FIRSTSTART, true);
		}
	    };
	    String message = "";
	    if (this.newVersionInfo)
	    {
		message = this.context.getString(R.string.app_newVersion_msg);
	    }
	    else
	    {
		message = "Es ist noch kein Element festlegt!\nBitte wählen Sie in den Einstellungen Ihr Element aus!";
	    }
	    handler.post(new ErrorMessage(this, message, onClick, "Einstellungen öffnen"));
	    break;
	case 6: // Elementenordner existiert nicht
		// neuen anlegen
	    java.io.File elementDir = new java.io.File(this.context.getFilesDir(), stupid[select].getMyElement());
	    elementDir.mkdir();
	    initViewPager();
	    stupid[select].checkAvailibilityOfWeek(this, Const.THISWEEK);
	    break;
	case 7: // Keine Daten für diese Klasse vorhanden
	    initViewPager();
	    stupid[select].checkAvailibilityOfWeek(this, Const.THISWEEK);
	    break;
	}
	selfCheckIsRunning = false;
    }

    /*
     * Datum: 11.10.12
     * 
     * @author Tobias Janssen Initialisiert den viewPager, der die Tage des
     * Stundenplans darstellt
     */
    public void initViewPager()
    {

	pager.currentPage = 0;
	for (int i = 0; i < stupid[select].stupidData.size(); i++)
	{
	    handler.post(new AppendPage(stupid[select].stupidData.get(i), this));
	    // Tools.appendTimeTableToPager(stupid.stupidData.get(i), stupid,
	    // this);

	}
	handler.post(new SetupPager(this, pager.pageIndex));
    }
    
    /**
     * @author Tobias Janssen
     * 
     *         Prüft, ob eine Wlan verbindung besteht, und liefert das Ergebnis
     * 
     * @param context
     * @return
     */
    public Boolean isWifiConnected()
    {
	WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	WifiInfo wifiinfo = wifi.getConnectionInfo();

	if (wifiinfo.getNetworkId() == -1)
	{
	    return false;
	}
	else
	    return true;
    }
    
}
