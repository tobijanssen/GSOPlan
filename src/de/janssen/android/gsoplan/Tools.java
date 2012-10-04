package de.janssen.android.gsoplan;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import de.janssen.android.gsoplan.Runnables.Download;
import de.janssen.android.gsoplan.Runnables.SaveData;
import de.janssen.android.gsoplan.Runnables.SaveSetup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.widget.Toast;

public class Tools{
	
	final static String FILESETUP="gsoStupidSetup.xml";
	final static String FILEDATA="gsoStupidData.xml";
	
	/// Datum 25.9.12
	public static Boolean isWifiConnected(Context context)
	{
    	WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	WifiInfo wifiinfo = wifi.getConnectionInfo();
        
       
        if(wifiinfo.getNetworkId() == -1)
        {
        	return false;
        }
        else
        	return true;
	}
	
	
	 /// Datum: 14.09.12
  	/// Autor: Tobias Janﬂen
  	///
  	///	Beschreibung:
  	///	Speichert den aktuellen StupidCore
  	///	
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public static void saveFilesWithProgressDialog(Context context,StupidCore stupid,ExecutorService exec )
    {
    	//ProgressDialog initialisieren
    	stupid.progressDialog =  new ProgressDialog(context);
    	stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	stupid.progressDialog.setMessage(context.getString(R.string.msg_saving));
    	stupid.progressDialog.setCancelable(false);
    	stupid.progressDialog.setProgress(0);
		
		//Pr¸fen, welche Aufgaben zu erledigen sind, dementsprechend den maximal Wert einstellen
    	if(stupid.elementList.length>0 && stupid.setupIsDirty)
    	{
    		stupid.progressDialog.setMax(stupid.elementList.length+stupid.weekList.length+50);
    	}
    	if(stupid.stupidData.length>0 && stupid.dataIsDirty)
    	{
    		stupid.progressDialog.setMax(stupid.progressDialog.getMax()+stupid.stupidData.length*
    				(stupid.stupidData[0].timetable.length*stupid.stupidData[0].timetable[0].length+stupid.stupidData[0].timetable.length)
    				+stupid.stupidData.length);
    	}
    	stupid.progressDialog.show();

   		
    	if(stupid.setupIsDirty)
    	{
    		SaveSetup saveSetup = new SaveSetup(context,stupid, FILESETUP);
    		exec.execute(saveSetup);    		
    	}
    	if(stupid.dataIsDirty)
    	{
    		SaveData saveData = new SaveData(context,stupid, stupid.myElement+FILEDATA);
    		exec.execute(saveData);
    		
    	}
    	
    	if(!stupid.dataIsDirty && !stupid.setupIsDirty)
    	{
    		stupid.progressDialog.dismiss();
    	}
   		
    }
    
    /// Datum: 2.10.12
  	/// Autor: Tobias Janﬂen
  	///
  	///	Beschreibung:
  	///	Speichert den aktuellen StupidCore
  	///	
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public static void saveFiles(Context context,StupidCore stupid,ExecutorService exec ) throws Exception
    {
    	SaveSetup saveSetup= new SaveSetup(context,stupid, FILESETUP);
    	SaveData saveData= new SaveData(context,stupid, stupid.myElement+FILEDATA);
    	
    	if(stupid.setupIsDirty)
    	{
    		exec.execute(saveSetup); 
    	}
    	if(stupid.dataIsDirty)
    	{
    		exec.execute(saveData);
    	}

		if(saveData.exception!=null)
		{
			throw saveData.exception;
		}
		if(saveSetup.exception!=null)
		{
			throw saveSetup.exception;
		}
    }
    
    
    /// Datum: 24.09.12
  	/// Autor: Tobias Janﬂen
  	///
  	///	Beschreibung:
  	///	Initialisiert die Zeit und die Wochentage
  	///	
    public static int getWeekOfYearToDisplay(Calendar date)
    {
    	
    	// create a Pacific Standard Time time zone
    	String[] ids = TimeZone.getAvailableIDs(1 * 60 * 60 * 1000);
    	SimpleTimeZone pdt = new SimpleTimeZone(1 * 60 * 60 * 1000, ids[0]);

    	// set up rules for daylight savings time
    	pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
    	pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
    	date = new GregorianCalendar(pdt); 
    	
	    //Die Aktuelle KalenderWoche abholen
    	int result = 0;
	    result=date.get(Calendar.WEEK_OF_YEAR);
	    return result;
    }
    
    /// Datum: 02.10.12
  	/// Autor: Tobias Janﬂen
  	///
  	///	Beschreibung:
  	///	Liefert den aktuellen Wochentag.Wochenendtage liefern den n‰chsten Montag. Und setzt das currentDate entsprechend um
  	///	
    public static int getSetCurrentWeekDay(Calendar date)
    {
    	int dayOfWeek=date.get(Calendar.DAY_OF_WEEK);
    	switch(dayOfWeek)
    	{
	    	case Calendar.SATURDAY:
	    		date.setTimeInMillis(date.getTimeInMillis()+(86400000*2));
	    		return Calendar.MONDAY;

	    	case Calendar.SUNDAY:
	    		date.setTimeInMillis(date.getTimeInMillis()+(86400000*1));
	    		return Calendar.MONDAY;
	    		
	    	default:
	    		return dayOfWeek;
	    		
    	}
    }
    
    
    /// Datum: 14.09.12
  	/// Autor: Tobias Janﬂen
  	///
  	///	Beschreibung:
  	///	L‰dt die Selectoren von der GSO Seite und parsed diese in die availableOnline Arrays
  	///	
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public static void fetchOnlineSelectors(Context context,StupidCore stupid,Executor exec, Runnable postrun)
    {
       	try
    	{
    		//aktuelle Daten aus dem Netz laden:
            if(stupid.onlyWlan)
            {
            	if(Tools.isWifiConnected(context))
            	{
            		stupid.progressDialog = ProgressDialog.show(context, context.getString(R.string.setup_message_dlElements_title), context.getString(R.string.setup_message_dlElements_body), true,false);
	            	stupid.setupIsDirty=true;
	            	Download download = new Download(stupid,true,false);
	            	exec.execute(download);
	            	exec.execute(postrun);
            	}
            	else
                {
                	Toast.makeText(context, "Keine Wlan Verbindung!", Toast.LENGTH_SHORT).show();
                }
            }
            else
        	{
            	stupid.progressDialog = ProgressDialog.show(context, context.getString(R.string.setup_message_dlElements_title), context.getString(R.string.setup_message_dlElements_body), true,false);
            	stupid.setupIsDirty=true;
            	Download download = new Download(stupid,true,false);
            	exec.execute(download);
            	exec.execute(postrun);
        	}
            
    	}
    	catch(Exception e)
    	{
    		new AlertDialog.Builder(context)
    	    .setTitle("Fehler")
    	    .setMessage(context.getString(R.string.setup_message_error_dlElements_1))
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



}
