/*
 * MyContext.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan.core;

import java.util.Calendar;
import com.viewpagerindicator.TitlePageIndicator;
import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.WorkerQueue;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.Pager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MyContext
{
    //public List<Profil> profilList = new ArrayList<Profil>();
    public Calendar dateBackup;
    public Handler handler = new Handler();
    public Logger logger;
    public WorkerQueue executor = new WorkerQueue();
    public Pager pager;
    public Context context;
    public Activity activity;
    public Boolean initialLunch = true;
    public LayoutInflater inflater;
    public int[] textSizes;
    public Boolean newVersionReqSetup = false;
    public String newVersionMsg = "";
    public Profil mProfil;
    public Menu appMenu;
    public Boolean mIsRunning = false;
    private ViewPager vp;
    private TitlePageIndicator tpi;
    public Boolean appIsReady=false;

    
    public MyContext(Context appctxt, Activity activity)
    {
	this.context = appctxt;
	this.logger = new Logger(this.context);
	this.activity = activity;
	this.mProfil = new Profil(this);
	this.mProfil.loadPrefs();
	
	this.inflater = LayoutInflater.from(context);
	
	MyContext.this.vp = (ViewPager) MyContext.this.activity.findViewById(R.id.pager);
	MyContext.this.tpi = (TitlePageIndicator) MyContext.this.activity.findViewById(R.id.indicator);
	MyContext.this.pager = new Pager(context, vp, tpi, inflater, Const.TEXTSIZEOFHEADLINES, logger,mProfil.hideEmptyHours);
    }
    
    
    public MyContext(Context appctxt)
    {
	this.context = appctxt;
	this.logger = new Logger(this.context);
	this.mProfil = new Profil(this);
	this.mProfil.loadPrefs();

    }
    

    public Stupid getCurStupid()
    {
	return mProfil.stupid;
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
     * Datum: 11.10.12
     * 
     * @author Tobias Janssen Initialisiert den viewPager, der die Tage des
     * Stundenplans darstellt
     */
    public void initViewPager()
    {
	activity.runOnUiThread(new Runnable(){

	    @Override
	    public void run()
	    {
		pager.clear();
		//pager.currentPage = 0;
		for (int i = 0; i < mProfil.stupid.stupidData.size(); i++)
		{
		    pager.appendTimeTableToPager(mProfil.stupid.stupidData.get(i), MyContext.this);
		}
		pager.init(mProfil.stupid.currentDate);
	    }
	    
	});
    }
    
    /**
     * Datum: 11.10.12
     * 
     * @author Tobias Janssen Initialisiert den viewPager, der die Tage des
     * Stundenplans darstellt
     */
    public void initViewPagerWaiting()
    {
	activity.runOnUiThread(new Runnable(){

	    @Override
	    public void run()
	    {
		pager.clear();
		View page = inflater.inflate(R.layout.waiting_page, null);
		pager.addView(0, page, "Warte auf GSO-Daten...");
		pager.init(mProfil.stupid.currentDate);
	    }
	    
	});
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
    
    public Handler msgHandler = new Handler()
    {
	public void handleMessage(Message message)
	{
	    logger.log(Logger.Level.INFO_1, "Refresh des Pagers wurde angefordert!");
	    
	    if(!appIsReady)
		logger.log(Logger.Level.INFO_1, "App ist noch nicht fertig -> warte!");
	    //Prüfen, ob die App vollständig geladen wurde
	    for(int time = 0; !appIsReady || time == 20;time++)
	    {
		
		//wenn nicht 2 Sekunden warten
		try
		{
		    java.lang.Thread.sleep(100);
		}
		catch (InterruptedException e1)
		{
		    //nichts
		}
	    }
	    
	    
	    if(pager != null && appIsReady)
	    {
		// Object path = message.obj;
		if (message.arg1 == Activity.RESULT_OK)
		{
		    // Daten von Stupid leeren
		    mProfil.stupid.clearData();
		    mProfil.stupid.clear();
		    
		    try
		    {
			// Struktur prüfen laden
			mProfil.stupid.checkStructure(MyContext.this);
			logger.log(Logger.Level.INFO_1, "Reload aller Dateien");
			// daten neu aus Datei laden
			Tools.loadAllDataFiles(context, mProfil, mProfil.stupid);
			
		    }
		    catch (Exception e)
		    {
			logger.log(Logger.Level.ERROR, "Reload der Daten fehlgeschlagen", e);
		    }
		    refreshView();
		    logger.log(Logger.Level.INFO_1, "Pager Erfolgreich aktualisiert");
		}
		else
		{
		    Toast.makeText(context, "Download failed.", Toast.LENGTH_LONG).show();
		}
	    }
	    else
		logger.log(Logger.Level.WARNING, "Refresh des Pagers fehlgeschlagen!");

	};
    };
    
    public void refreshView()
    {
	//alle pagerDaten leeren
	int page = pager.getCurrentPage();
	pager = new Pager(context,vp, tpi, inflater,Const.TEXTSIZEOFHEADLINES,logger, mProfil.hideEmptyHours);
	pager.setPage(page);
	for (int i = 0; i < getCurStupid().stupidData.size(); i++)
	{
	    pager.appendTimeTableToPager(getCurStupid().stupidData.get(i), this);
	}
	pager.init(mProfil.stupid.currentDate);
    }
    

    
    
}
