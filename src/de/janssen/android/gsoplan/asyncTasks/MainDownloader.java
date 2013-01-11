/*
 * MainDownloader.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.asyncTasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.widget.Toast;
import de.janssen.android.gsoplan.DownloadFeedback;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.runnables.ErrorMessage;
import de.janssen.android.gsoplan.runnables.Toaster;
import de.janssen.android.gsoplan.runnables.UpdateTimeTableList;

public class MainDownloader extends AsyncTask<Boolean, Integer, Boolean>
{


	private final int BOTH=0;
	private final int ONLYTIMETABLE=1;
	private final int ONLYSELECTORS=2;
	private MyContext ctxt;
	private String errorMessage;
	private String message;
	private Calendar requestedDate;
	private Runnable postExec;
	private Boolean forcePageTurn;
	

	
	public MainDownloader(MyContext ctxt,String errorMessage,Calendar requestedDate, Runnable postExec,Boolean forcePageTurn,String message)
	{
		this.ctxt=ctxt;
		this.errorMessage=errorMessage;
		this.message=message;
		this.requestedDate=requestedDate;
		this.postExec=postExec;
		this.forcePageTurn=forcePageTurn;
		
	}
	public MainDownloader(MyContext ctxt,String errorMessage,Calendar requestedDate,Boolean forcePageTurn,String message)
	{
		this.ctxt=ctxt;
		this.errorMessage=errorMessage;
		this.message=message;
		this.requestedDate=requestedDate;
		this.forcePageTurn=forcePageTurn;
	}
	
	@Override
	protected void onPreExecute() {
		ctxt.progressDialog =  new ProgressDialog(ctxt.context);
		ctxt.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		ctxt.progressDialog.setMessage(message);
		ctxt.progressDialog.setCancelable(true);
		ctxt.progressDialog.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				ctxt.executor.terminateActiveThread();
			}});
		ctxt.progressDialog.show();
		this.ctxt.stupid.dataIsDirty=true;
		super.onPreExecute();
	}
	@Override
	protected Boolean doInBackground(Boolean... bool) {
		
		//Erst die Ressourcen Auffrischen
		//Dazu die Selectoren downloaden(quasi prüfen, ob neue Wochen verfügbar sind)
		try
		{
			if (isCancelled()) 
				return false;
			downloader(ONLYSELECTORS);
		}
		catch (Exception e)
		{
			ctxt.handler.post(new ErrorMessage(ctxt,e.getMessage()));
			return false;
		}
		
		int reqWeekOfYear=ctxt.stupid.getWeekOfYear(requestedDate);
		//Erstmal davon ausgehen, dass der TimeTable nicht verfügbar ist
		int isOnlineAvailableIndex=-1;
		//die neue Liste der verügbaren Wochen durchgehen
    	for(int i=0;i<ctxt.stupid.weekList.length && isOnlineAvailableIndex == -1 ;i++)
    	{
    		//und prüfen, ob die gesuchte Woche dabei ist
    		if(reqWeekOfYear == Integer.decode(ctxt.stupid.weekList[i].index))
    		{
    			//Woche ist vorhanden
    			//ist das Jahr auch gleich?
    			//Dazu muss die Description geparsed werden
    			
    			try
    			{
    				DateFormat dateFormat =  new SimpleDateFormat ("dd.MM.yyyy");
    				Date date = dateFormat.parse(ctxt.stupid.weekList[i].description);
	    			Calendar cal = new GregorianCalendar();
	    			cal.setTimeInMillis(date.getTime());
	    			if(requestedDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
	    				isOnlineAvailableIndex=i;
    			}
    			catch (Exception e)
    			{
    				//Datum konnte nicht geparsed werden
    			}
    			
    		}
    	}
    	//wenn diese online verfügbar ist
    	if(isOnlineAvailableIndex!=-1)
    	{
    		//Prüfen, ob eine verbindung aufgebaut werden darf
    		Boolean connectionAllowed=false;
    		if(ctxt.stupid.onlyWlan)
            {
    			//es darf nur über wlan eine Verbindung hergestellt werden
    			//prüfen, ob so eine Verbindung besteht
             	if(Tools.isWifiConnected(ctxt.context))
             	{
             		//es besteht verbindung
             		connectionAllowed=true;
             	}
             	else
                {
             		//Keine Verbindung einen kleinen Hinweis ausgeben, dass keine Verbindung besteht
             		ctxt.handler.post(new Toaster(ctxt,ctxt.activity.getString(R.string.msg_noWlan),Toast.LENGTH_SHORT));
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
    			if (isCancelled()) 
    				return false;
    			DownloadFeedback downloadFeedback = new DownloadFeedback(-1,DownloadFeedback.NO_REFRESH);
    			try
        		{
        			//Jetzt den Stundplan mit der gesuchten Wochennummer downloaden
    				downloadFeedback=downloader(isOnlineAvailableIndex,ONLYTIMETABLE);
    				if (isCancelled()) 
    					return false;
    				//prüfen ob ein pageturn gemacht werden soll
    				if(this.forcePageTurn)
    				{
	    				Calendar now = new GregorianCalendar();
	    				if(ctxt.stupid.currentDate.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR))
	    					ctxt.stupid.currentDate=(Calendar) this.requestedDate.clone();
    				}
	    		}
	    		catch (Exception e)
	    		{
	    			//parent.disablePagerOnChangedListener=false;
	    			ctxt.handler.post(new ErrorMessage(ctxt,e.getMessage()));
	    			return false;
	    		}
        		
        		try
        		{
        			//den Index neu erstellen lassen
        			ctxt.stupid.timeTableIndexer();
        		}
        		catch(Exception e)
        		{
        			//Keine Klasse ausgewählt!       
        			Tools.gotoSetup(ctxt);
        			return false;
        		}
        		
        		ctxt.handler.post(new UpdateTimeTableList(ctxt, downloadFeedback));
    		}


    	}
    	else
    	{
    		if(ctxt.progressDialog != null)
    			ctxt.progressDialog.dismiss();
    		
    		if(ctxt.stupid.onlyWlan)
            {
             	if(Tools.isWifiConnected(ctxt.context))
             	{
             		ctxt.handler.post(new Toaster(ctxt,this.errorMessage, Toast.LENGTH_SHORT));
             	}
             	else
                {
             		ctxt.handler.post(new Toaster(ctxt,ctxt.activity.getString(R.string.msg_noWlan), Toast.LENGTH_SHORT));
                }
            }
            else
         	{
            	ctxt.handler.post(new Toaster(ctxt,this.errorMessage, Toast.LENGTH_SHORT));
         	}
    		
    			
    		
    	}
		return false;
    	
	}
	
	
	private DownloadFeedback downloader(int params) throws Exception 
	{
		return downloader(0, params);
	}
	
	private DownloadFeedback downloader(int weekIndex, int params) throws Exception {
    	try
    	{
    		Boolean connectionAllowed=false;
    		if(ctxt.stupid.onlyWlan)
            {
             	if(Tools.isWifiConnected(ctxt.context))
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
	    				
	    				ctxt.progressDialog.setMax(80000);
	    				ctxt.stupid.fetchSelectorsFromNet(ctxt);
	    				ctxt.progressDialog.setProgress(15000);
	    				downloadFeedback = ctxt.stupid.fetchTimeTableFromNet(ctxt.stupid.weekList[weekIndex].description, ctxt.stupid.getMyElement(), ctxt.stupid.typeList[ctxt.stupid.getMyType()].index ,ctxt);
	    				ctxt.progressDialog.setProgress(80000);
	    				ctxt.handler.post(new UpdateTimeTableList(ctxt,downloadFeedback));
	    				
	    				return downloadFeedback;
	    			case ONLYTIMETABLE:
	    				int prgsLength = calculateProgress();
	    				ctxt.progressDialog.setMax(prgsLength);
	    				downloadFeedback = ctxt.stupid.fetchTimeTableFromNet(ctxt.stupid.weekList[weekIndex].description, ctxt.stupid.getMyElement(), ctxt.stupid.typeList[ctxt.stupid.getMyType()].index,ctxt);
	    				ctxt.progressDialog.setProgress(prgsLength);
	    				return downloadFeedback;
	    			case ONLYSELECTORS:
	    				ctxt.progressDialog.setMax(15000);
	    				ctxt.stupid.fetchSelectorsFromNet(ctxt);
	    				ctxt.progressDialog.setProgress(15000);
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
	
	private int calculateProgress()
	{
		int result=0;
		result+=12585; //ca. für readhtml. kann nur ca angegeben werden, da nicht berechenbar müsste aber immer ungefähr gleichbleiben
		result+=46700; //ca. für xmltoArray. ebenfalls unbekannt müsste aber immer ungefähr gleichbleiben
		result+=2000;	//ca. für convertXMLTableToWeekData
		result+=1000;	//ca. für conerttoMultiDim
		result+=100*ctxt.stupid.stupidData.size();
		result+=500;
		return result;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }
	@Override
    protected void onPostExecute(Boolean bool) {
    	if(this.postExec != null)
    		postExec.run();
    	ctxt.progressDialog.dismiss();
    	ctxt.executor.scheduleNext();
    }
	
}
