package de.janssen.android.gsoplan.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.dataclasses.ProfilManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public class AlarmStarter extends Service
{
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      
	Intent worker = new Intent(this, MyService.class);
	
	PendingIntent pintent = PendingIntent.getService(this, 0, worker, 0);
	Calendar cal = new GregorianCalendar(); 
	AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	long resync = 60;	//default Alle 60 Minuten
	Boolean sync = false;
	MyContext ctxt = new MyContext(this);
	
	//das Profil heraussuchen, das den kürzestsen resync hat
	//und prüfen, ob überhaupt gesynct werden soll
	ProfilManager pm = new ProfilManager(ctxt);
	for(int i=0;i<pm.profiles.size();i++)
	{
	    if(pm.profiles.get(i).myResync < resync)
		resync = pm.profiles.get(i).myResync;
	    if(pm.profiles.get(i).autoSync)
		sync=true;
	}
	//den Resync um 2 Minuten verlängern
	resync+=2;
	if(sync)
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
