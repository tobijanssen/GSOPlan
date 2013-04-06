package de.janssen.android.gsoplan.service;

import java.io.File;
import java.io.SyncFailedException;
import java.util.List;
import de.janssen.android.gsoplan.Convert;
import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.activities.MainActivity;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.core.StupidOPs;
import de.janssen.android.gsoplan.core.Profil;
import de.janssen.android.gsoplan.core.Stupid;
import de.janssen.android.gsoplan.core.WeekData;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.HtmlResponse;
import de.janssen.android.gsoplan.dataclasses.SelectOptions;
import de.janssen.android.gsoplan.xml.Xml;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;


public class BackgroundSync  extends AsyncTask<Boolean, Integer, Boolean>
{
    private Stupid stupid;
    private Logger logger;
    private Runnable postRun;
    private MyContext ctxt;
    private NotificationManager nm;
    private int notificationId = 2;
    private int syncNoteId = 1;
    private Profil mProfil;
    
    public BackgroundSync(Context ctxt, Runnable run)
    {
	this.ctxt=new MyContext(ctxt);
	this.logger = new Logger(ctxt);
	this.postRun = run;
	this.mProfil=this.ctxt.mProfil;
	this.nm = (NotificationManager) this.ctxt.context.getSystemService(Context.NOTIFICATION_SERVICE); 
    }

    
    @Override
    protected Boolean doInBackground(Boolean... bool)
    {
	if ((mProfil.onlyWlan && ctxt.isWifiConnected()) || !mProfil.onlyWlan)
	{
	    try
	    {
		// Log starten
		this.logger.log(Logger.Level.DEBUG, "---------------------------------", true);
		this.logger.log(Logger.Level.DEBUG, "starting Sync: ");

		stupid = new Stupid();
		// die typedaten laden
		File typesFile = mProfil.getTypesFile(ctxt.context);

		if (typesFile.exists())
		{
		    // die ElementDatei Laden
		    try
		    {
			Xml xml = new Xml("root", FileOPs.readFromFile(this.logger, typesFile));
			// Temporär Daten umwandeln
			Stupid temp = new Stupid();
			mProfil.types = Convert.toTypesList(xml);
			// Wenn das geklappt hat Elements leeren
			stupid.clear();
			stupid = temp;
		    }
		    catch (Exception e)
		    {
			// Fehler beim Laden der ElementDatei
			logger.log(Logger.Level.ERROR, "Error loading Element File!", e);
		    }
		}
		else
		{
		    // nichts vorhanden neu laden und anlegen
		    StupidOPs.syncTypeList(logger, new HtmlResponse(), mProfil);
		    // Typen zu XML konvertieren
		    String xml = Convert.toXml(mProfil.types);
		    FileOPs.saveToFile(xml, typesFile);
		}

		// Synchronisation durchführen
		HtmlResponse html = new HtmlResponse();
		Exception e = syncAllDataInBackground(html, logger);
		if (e != null)
		    logger.log(Logger.Level.ERROR, "Error while syncing Data:", e);
	    }
	    catch (SyncFailedException e)
	    {
		logger.log(Logger.Level.WARNING, "Error fetching Element File", e);
		notifcateError(new SyncFailedException(Const.ERROR_NONET));
	    }
	    catch (Exception e)
	    {
		logger.log(Logger.Level.WARNING, "Error loading Element File: doesn't exist");
		;
	    }
	    if (this.postRun != null)
		this.postRun.run();
	}
	else
	{
	    // Keine WIFI-Verbindung
	   this.notifcateError(new SyncFailedException(ctxt.context.getString(R.string.msg_noWlan)),true);
	}
	
	return null;
    }
    
    private Exception syncAllDataInBackground(HtmlResponse htmlResponse, Logger logger)
    {
	notifcateSync();
	this.logger = logger;
	// Bedingungen prüfen
	// Element muss gewählt sein
	mProfil.loadPrefs(); // Settings laden
	if (!mProfil.myElement.isEmpty())
	{

	    // die Elemente/Wochen herunterladen
	    try
	    {
		logger.log(Logger.Level.INFO_2, "syncing Elements");
		StupidOPs.syncTypeList(logger, htmlResponse, mProfil);

	    }
	    catch (SyncFailedException e)
	    {
		logger.log(Logger.Level.ERROR, "Error fetching Type List from Server", e);
		notifcateError(new SyncFailedException(Const.ERROR_NONET));
	    }
	    catch (Exception e)
	    {
		nm.cancel(syncNoteId);
		return e;
	    }

	    // Alle online verfügbaren Wochen aus dem lokalen Datenbestand laden
	    try
	    {
		stupid.loadAllOnlineDataFiles(mProfil, ctxt.context);
	    }
	    catch (Exception e)
	    {
		nm.cancel(syncNoteId);
		logger.log(Logger.Level.ERROR, "Fehler beim lesen der Daten Dateien ", e);
	    }
	    logger.log(Logger.Level.INFO_2, "syncing WeekData");
	    // jede verfügbare Woche herunterladen
	    List<SelectOptions> weekList = mProfil.currType().weekList;
	    String[] weekDays = new String[]{"So","Mo","Di","Mi","Do","Fr","Sa"};
	    for (int i = 0; i < weekList.size(); i++)
	    {
		try
		{
		    htmlResponse = new HtmlResponse();
		    logger.log(Logger.Level.INFO_1, "Week " + mProfil.currType().weekList.get(i).description + ":", true);
		    List <Point> changes = StupidOPs.syncWeekData(logger,weekList.get(i).description, mProfil.getMyElement(), mProfil.currType(),htmlResponse,stupid);

		    // wurden Daten heruntergeladen && Es bestand vorhern ein
		    // Datenbestand
		    if (htmlResponse.dataReceived && htmlResponse.alreadyInCache)
		    {
			if(!changes.isEmpty())
			{
			    for(int c=0;c<changes.size();c++)
			    {
				// notification ausführen
				logger.log(Logger.Level.WARNING, "KW " + weekList.get(i).index + ": " + weekDays[changes.get(c).x] + " enthält Änderungen!");
				if(this.mProfil.notificate)
				{
				    notifcate("KW " + weekList.get(i).index + ": " + weekDays[changes.get(c).x] + "\nenthält Änderungen!",
					weekList.get(i).index,changes.get(c).x , MainActivity.class, true);
				}
			    }
    			}
		    }

		}
		catch (Exception e)
		{
		    nm.cancel(syncNoteId);
		    return e;
		}
	    }

	    // type speichern
	    String xmlContent;
	    
	    logger.log(Logger.Level.INFO_2, "Saving Types");
	    try
	    {
		xmlContent = Convert.toXml(mProfil.types);
		File typesFile = ctxt.mProfil.getTypesFile(ctxt.context);
		FileOPs.saveToFile(xmlContent, typesFile);
		ctxt.mProfil.isDirty=false;
	    }
	    catch (Exception e)
	    {
		nm.cancel(syncNoteId);
		return e;
	    }
	    
	     
	    // daten speichern
	    WeekData wd;
	    File dataFile;
	    logger.log(Logger.Level.INFO_2, "Saving WeekData");
	    for (int i = 0; i < stupid.stupidData.size(); i++)
	    {

		try
		{
		    wd = stupid.stupidData.get(i);
		    dataFile = stupid.getFileSaveData(ctxt.context,wd);
		    xmlContent = Convert.toXml(wd);
		    FileOPs.saveToFile(xmlContent, dataFile);
		    wd.isDirty = false;
		}
		catch (Exception e)
		{
		    nm.cancel(syncNoteId);
		    return e;
		}

	    }
	}
	else
	{
	    logger.log(Logger.Level.WARNING, "Element is empty");
	    // die Elemente/Wochen herunterladen
	    try
	    {

		logger.log(Logger.Level.INFO_2, "syncing Elements");
		StupidOPs.syncTypeList(logger, htmlResponse, mProfil);

	    }
	    catch (Exception e)
	    {
		nm.cancel(syncNoteId);
		return e;
	    }
	}
	nm.cancel(syncNoteId);
	return null;
    }

    private void notifcate(String message, String weekIndex,int dayIndex, Class<?> cls, Boolean vibrate)
    {
	CharSequence text = message;
	CharSequence name = ctxt.context.getString(R.string.app_name);
	Vibrator v = (Vibrator) ctxt.context.getSystemService(Context.VIBRATOR_SERVICE);
	if (notificationId + 1 == Integer.MAX_VALUE)
	    notificationId = 1;
	else
	    notificationId++;
	
	//Startet die MainActivity
	Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
	Intent intent = new Intent(ctxt.context, cls);
	
	intent.putExtra("notificationId", notificationId);
	intent.putExtra("weekIndex", weekIndex);
	intent.putExtra("dayIndex", dayIndex);
	PendingIntent contentIntent = PendingIntent.getActivity(ctxt.context, notificationId, intent, 0);
	notification.setLatestEventInfo(ctxt.context, name, text, contentIntent);

	if (vibrate && mProfil.vibrate)
	    v.vibrate(new long[] { 200, 1200 }, -1);

	nm.notify(notificationId, notification);
	
	//Klingelton
	if (mProfil.sound)
	{
	    try
	    {
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(ctxt.context, uri);
		r.play();
	    }
	    catch (Exception e)
	    {
	    }
	}
	try
	{
	    java.lang.Thread.sleep(5000);
	}
	catch (InterruptedException e)
	{

	}
	
	// nm.cancel(0);
    }

    /**
     * Zeigt eine Meldung in der NotificationBar
     * 
     * @param message
     *            String der angezeigt wird
     * @param cls
     *            Klasse, die aufgerufen werden soll
     * @param vibrate
     *            true um Vibration auszulösen
     */
    private void notifcateSync()
    {
	try
	{
	    Notification notification = new Notification(android.R.drawable.stat_notify_sync, Const.NOTIFICATESYNCHEAD+ Const.NOTIFICATESYNCSHORT, System.currentTimeMillis());

	    PendingIntent contentIntent = PendingIntent.getActivity(ctxt.context.getApplicationContext(), 0, new Intent(), 0);
	    notification.setLatestEventInfo(ctxt.context.getApplicationContext(), Const.NOTIFICATESYNCHEAD, Const.NOTIFICATESYNC, contentIntent);

	    nm.notify(1, notification);
	    
	}
	catch (Exception e)
	{
	    ctxt.logger.log(Logger.Level.ERROR, "Notification konnte nicht erstellt werden",e);
	}

    }
    
    /**
     * Zeigt eine FehlerMeldung in der NotificationBar
     * @param exception
     */
    private void notifcateError(SyncFailedException exception, Boolean cancel)
    {
	try
	{
	    Notification notification = new Notification(android.R.drawable.stat_notify_error, exception.getMessage(), System.currentTimeMillis());

	    PendingIntent contentIntent = PendingIntent.getActivity(ctxt.context.getApplicationContext(), 0, new Intent(), 0);
	    notification.setLatestEventInfo(ctxt.context.getApplicationContext(), Const.NOTIFICATESYNCHEAD, exception.getMessage(), contentIntent);

	    nm.notify(1, notification);
	    if(cancel)
		nm.cancel(1);
	}
	catch (Exception e)
	{
	    ctxt.logger.log(Logger.Level.ERROR, "Notification konnte nicht erstellt werden",e);
	}

    }
    private void notifcateError(SyncFailedException exception)
    {
	notifcateError(exception, false);
    }

}
