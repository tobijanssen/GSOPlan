package de.janssen.android.gsoplan.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;


public class AlarmStarter extends Service
{
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      
	Intent worker = new Intent(this, MyService.class);
	
	PendingIntent pintent = PendingIntent.getService(this, 0, worker, 0);
	Calendar cal = new GregorianCalendar(); 
	AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	
	
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	long resync = 30;	//Default-Resync
	Boolean autoSync = false;
	try
	{
	    String value = prefs.getString("listResync", "10");
	    resync = Long.parseLong(value);
	    autoSync = prefs.getBoolean("boxAutoSync", false);
	}
	catch (Exception e)
	{
	    // Resync ist ungültig
	}
	// Starte wie in Einstellunegen festgelegt
	if(autoSync)
	    alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), resync*60*1000, pintent);
	else
	    alarm.cancel(pintent);
	
	return Service.START_NOT_STICKY;
    }
    
    
    @Override
    public IBinder onBind(Intent arg0)
    {
	return null;
    }
}
