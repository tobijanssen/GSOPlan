package de.janssen.android.gsoplan.runnables;

import java.util.Calendar;
import android.widget.Toast;
import de.janssen.android.gsoplan.DownloadFeedback;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;

public class MainDownloader implements Runnable{
	private final int BOTH=0;
	private final int ONLYTIMETABLE=1;
	private final int ONLYSELECTORS=2;
	private PlanActivity parent;
	private String errorMessage;
	private Calendar requestedDate;

	
	public MainDownloader(PlanActivity parent,String errorMessage,Calendar requestedDate)
	{
		this.parent=parent;
		this.errorMessage=errorMessage;
		this.requestedDate=requestedDate;
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
			parent.handler.post(new ErrorMessage(parent,e.getMessage()));
			return;
		}
		
		int reqWeekOfYear=parent.stupid.getWeekOfYear(requestedDate);
		//Erstmal davon ausgehen, dass der TimeTable nicht verfügbar ist
		int isOnlineAvailableIndex=-1;
		//die neue Liste der verügbaren Wochen durchgehen
    	for(int i=0;i<parent.stupid.weekList.length && isOnlineAvailableIndex == -1 ;i++)
    	{
    		//und prüfen, ob die gesuchte Woche dabei ist
    		if(reqWeekOfYear == Integer.decode(parent.stupid.weekList[i].index))
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
    			DownloadFeedback downloadFeedback = new DownloadFeedback(-1,DownloadFeedback.NO_REFRESH);
    			try
        		{
        			//Jetzt den Stundplan mit der gesuchten Wochennummer downloaden
    				downloadFeedback=downloader(isOnlineAvailableIndex,ONLYTIMETABLE);
    			
	    		}
	    		catch (Exception e)
	    		{
	    			//parent.disablePagerOnChangedListener=false;
	    			parent.handler.post(new ErrorMessage(parent,e.getMessage()));
	    			return;
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
        			return;
        		}
        		
        		parent.handler.post(new UpdateTimeTableList(parent, downloadFeedback));
    		}


    	}
    	else
    	{
    		parent.stupid.progressDialog.dismiss();
    		
    		if(parent.stupid.onlyWlan)
            {
             	if(Tools.isWifiConnected(parent))
             	{
             		parent.handler.post(new Toaster(parent,this.errorMessage, Toast.LENGTH_LONG));
             	}
             	else
                {
             		parent.handler.post(new Toaster(parent,parent.getString(R.string.msg_noWlan), Toast.LENGTH_LONG));
                }
            }
            else
         	{
            	parent.handler.post(new Toaster(parent,this.errorMessage, Toast.LENGTH_LONG));
         	}
    		
    			
    		
    	}
    	
	}
	
	
	private DownloadFeedback downloader(int params) throws Exception 
	{
		return downloader(0, params);
	}
	
	private DownloadFeedback downloader(int weekIndex, int params) throws Exception {
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
    			DownloadFeedback downloadFeedback;

	    		switch(params)
	    		{
	    			case BOTH:
	    				parent.stupid.progressDialog.setMax(80000);
	    				parent.stupid.fetchSelectorsFromNet();
	    				parent.stupid.progressDialog.setProgress(15000);
	    				downloadFeedback = parent.stupid.fetchTimeTableFromNet(parent.stupid.weekList[weekIndex].description, parent.stupid.myElement, parent.stupid.typeList[parent.stupid.myType].index );
	    				parent.stupid.progressDialog.setProgress(80000);
	    				parent.handler.post(new UpdateTimeTableList(parent,downloadFeedback));
	    				return downloadFeedback;
	    			case ONLYTIMETABLE:
	    				parent.stupid.progressDialog.setMax(65000);
	    				downloadFeedback = parent.stupid.fetchTimeTableFromNet(parent.stupid.weekList[weekIndex].description, parent.stupid.myElement, parent.stupid.typeList[parent.stupid.myType].index);
	    				parent.stupid.progressDialog.setProgress(65000);
	    				return downloadFeedback;
	    			case ONLYSELECTORS:
	    				parent.stupid.progressDialog.setMax(15000);
	    				parent.stupid.fetchSelectorsFromNet();
	    				parent.stupid.progressDialog.setProgress(15000);
	    				return new DownloadFeedback(-1,DownloadFeedback.NO_REFRESH);
	    		}
	    	}
    		return new DownloadFeedback(-1,DownloadFeedback.NO_REFRESH);
    	}
    	catch(Exception e) 
    	{
    		throw e;			
		}
    }
	
}
