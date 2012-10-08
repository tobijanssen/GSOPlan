package de.janssen.android.gsoplan.Runnables;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;

public class SpecialDownload implements Runnable{
	private final int BOTH=0;
	private final int ONLYTIMETABLE=1;
	private final int ONLYSELECTORS=2;
	private PlanActivity parent;
	public Exception  exception = null;
	
	public SpecialDownload(PlanActivity parent)
	{
		this.parent=parent;
	}
	
	@Override
	public void run() {
		
		//Erst die Ressourcen Auffrischen
		//Dazu die Selectoren downloaden(quasi prüfen, ob neue Wochen verfügbar sind)
		try
		{
			downloader(ONLYSELECTORS);
		}
		catch (Exception e)
		{
			exception = e;
		}
		
		//Erstmal davon ausgehen, dass der TimeTable nicht verfügbar ist
		int isOnlineAvailableIndex=-1;
		//die neue Liste der verügbaren Wochen durchgehen
    	for(int i=0;i<parent.stupid.weekList.length && isOnlineAvailableIndex == -1 ;i++)
    	{
    		//und prüfen, ob die gesuchte Woche dabei ist
    		if(parent.indexOfWeekIdToDisplay == Integer.decode(parent.stupid.weekList[i].index))
    			isOnlineAvailableIndex=i;
    	}
    	//wenn diese online verfügbar ist
    	if(isOnlineAvailableIndex!=-1)
    	{
    		//Prüfen, ob eine verbindung aufgebaut werden darf
    		Boolean connectionAllowed=false;
    		if(parent.stupid.onlyWlan)
            {
    			//es darf nur über wlan eine Verbindung hergestellt werden
    			//prüfen, ob so eine Verbindung besteht
             	if(Tools.isWifiConnected(parent))
             	{
             		//es besteht verbindung
             		connectionAllowed=true;
             	}
             	else
                {
             		//Keine Verbindung einen kleinen Hinweis ausgeben, dass keine Verbindung besteht
             		parent.handler.post(new Toaster(parent,parent.getString(R.string.msg_noWlan),Toast.LENGTH_SHORT));
                }
            }
            else
         	{
            	//Es darf auch ohne Wlan eine Verbindung aufgebaut werden
            	connectionAllowed=true;
         	}
    		
    		//nun prüfen, ob die verbindung hergestellt werden darf
    		if(connectionAllowed)
    		{
    			try
        		{
        			//Jetzt den Stundplan mit der gesuchten Wochennummer downloaden
        			downloader(isOnlineAvailableIndex,ONLYTIMETABLE);
    			
	    		}
	    		catch (Exception e)
	    		{
	    			exception = e;
	    		}
        		
        		try
        		{
        			//den Index neu erstellen lassen
        			parent.stupid.timeTableIndexer();
        		}
        		catch(Exception e)
        		{
        			//Keine Klasse ausgewählt!        			
        			parent.gotoSetup();
        		}
        		//Den neuen Index der angeforderten Woche heraussuchen
        		parent.weekDataIndexToShow = parent.stupid.getIndexOfWeekData(parent.stupid.currentDate);
        		
        		parent.handler.post(new UpdateTimeTableScreen(parent));
    		}

    	}
    	else
    	{
    		//es gibt dieses Datum noch nicht
    		//oder Daten konnten nich abgerufen werden
    		//alles wieder zurück
    		parent.stupid.currentDate=(Calendar) parent.dateBackup.clone();
    		parent.indexOfWeekIdToDisplay = parent.stupid.getWeekOfYear(parent.stupid.currentDate);

    		parent.weekDataIndexToShow = parent.stupid.getIndexOfWeekData(parent.stupid.currentDate);
    		parent.stupid.progressDialog.dismiss();
    		
    		if(parent.stupid.onlyWlan)
            {
             	if(Tools.isWifiConnected(parent))
             	{
             		parent.handler.post(new Toaster(parent,parent.getString(R.string.msg_weekNotAvailable), Toast.LENGTH_LONG));
             	}
             	else
                {
             		parent.handler.post(new Toaster(parent,parent.getString(R.string.msg_noWlan), Toast.LENGTH_LONG));
                }
            }
            else
         	{
            	parent.handler.post(new Toaster(parent,parent.getString(R.string.msg_weekNotAvailable), Toast.LENGTH_LONG));
         	}
    		
    			
    		
    	}
    	
	}
	
	
	private void downloader(int params) throws Exception 
	{
		downloader(0, params);
	}
	
	private void downloader(int weekIndex, int params) throws Exception {
    	try
    	{
    		Boolean connectionAllowed=false;
    		if(parent.stupid.onlyWlan)
            {
             	if(Tools.isWifiConnected(parent))
             	{
             		connectionAllowed=true;
             	}
             	else
                {
                	connectionAllowed=false;
                }
            }
            else
         	{
            	connectionAllowed=true;
         	}
    		
    		if(connectionAllowed)
	    	{
	    		//aktuelle Daten aus dem Netz laden:
    		
	    		switch(params)
	    		{
	    			case BOTH:
	    				parent.stupid.progressDialog.setMax(80000);
	    				parent.stupid.fetchSelectorsFromNet();
	    				parent.stupid.progressDialog.setProgress(15000);
	    				parent.stupid.fetchTimeTableFromNet(parent.stupid.weekList[weekIndex].description, parent.stupid.myElement, parent.stupid.typeList[parent.stupid.myType].index );
	    				parent.stupid.progressDialog.setProgress(80000);
	    				parent.handler.post(new UpdateTimeTableScreen(parent));
	            		break;
	    			case ONLYTIMETABLE:
	    				parent.stupid.progressDialog.setMax(65000);
	    				parent.stupid.fetchTimeTableFromNet(parent.stupid.weekList[weekIndex].description, parent.stupid.myElement, parent.stupid.typeList[parent.stupid.myType].index);
	    				parent.stupid.progressDialog.setProgress(65000);
	            		break;
	    			case ONLYSELECTORS:
	    				parent.stupid.progressDialog.setMax(15000);
	    				parent.stupid.fetchSelectorsFromNet();
	    				parent.stupid.progressDialog.setProgress(15000);
	            		break;
	    		}
	    	}
    	}
    	catch(Exception e) 
    	{
    		throw e;			
		}
    }
	
}
