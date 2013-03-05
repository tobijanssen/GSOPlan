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

    private final int BOTH = 0;
    private final int ONLYTIMETABLE = 1;
    private final int ONLYSELECTORS = 2;
    private MyContext ctxt;
    private String errorMessage;
    private String message;
    private Calendar requestedDate;
    private Runnable postExec;
    private Boolean forcePageTurn;

    public MainDownloader(MyContext ctxt, String errorMessage, Calendar requestedDate, Runnable postExec,
	    Boolean forcePageTurn, String message)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.message = message;
	this.requestedDate = requestedDate;
	this.postExec = postExec;
	this.forcePageTurn = forcePageTurn;

    }

    public MainDownloader(MyContext ctxt, String errorMessage, Calendar requestedDate, Boolean forcePageTurn,
	    String message)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.message = message;
	this.requestedDate = requestedDate;
	this.forcePageTurn = forcePageTurn;
    }

    @Override
    protected void onPreExecute()
    {
	ctxt.progressDialog = new ProgressDialog(ctxt.context);
	ctxt.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	ctxt.progressDialog.setMessage(message);
	ctxt.progressDialog.setCancelable(true);
	ctxt.progressDialog.setOnCancelListener(new OnCancelListener()
	{

	    @Override
	    public void onCancel(DialogInterface dialog)
	    {
		ctxt.executor.terminateActiveThread();
	    }
	});
	if(ctxt.mIsRunning)
	    ctxt.progressDialog.show();
	super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Boolean... bool)
    {

	// Erst die Ressourcen Auffrischen
	// Dazu die Selectoren downloaden(quasi pr�fen, ob neue Wochen verf�gbar
	// sind)
	try
	{
	    if (isCancelled())
		return false;
	    downloader(ONLYSELECTORS);
	}
	catch (Exception e)
	{
	    ctxt.handler.post(new ErrorMessage(ctxt, e.getMessage()));
	    return false;
	}
	int isOnlineAvailableIndex = -1;
	try
	{

	    int reqWeekOfYear = ctxt.getCurStupid().getWeekOfYear(requestedDate);
	    // Erstmal davon ausgehen, dass der TimeTable nicht verf�gbar ist
	    
	    // die neue Liste der ver�gbaren Wochen durchgehen
	    for (int i = 0; i < ctxt.getCurStupid().weekList.length && isOnlineAvailableIndex == -1; i++)
	    {
		// und pr�fen, ob die gesuchte Woche dabei ist
		String index = ctxt.getCurStupid().weekList[i].index;
		if(index.startsWith("0"))
		    index=index.substring(1);
		if (reqWeekOfYear == Integer.decode(index))
		{
		    // Woche ist vorhanden
		    // ist das Jahr auch gleich?
		    // Dazu muss die Description geparsed werden

		    try
		    {
			DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			Date date = dateFormat.parse(ctxt.getCurStupid().weekList[i].description);
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(date.getTime());
			if (requestedDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
			    isOnlineAvailableIndex = i;
		    }
		    catch (Exception e)
		    {
			// Datum konnte nicht geparsed werden
		    }

		}
	    }
	}
	catch (Exception e)
	{
	    return false;
	}
	// wenn diese online verf�gbar ist
	if (isOnlineAvailableIndex != -1)
	{
	    // Pr�fen, ob eine verbindung aufgebaut werden darf
	    Boolean connectionAllowed = false;
	    if (ctxt.getCurStupid().onlyWlan)
	    {
		// es darf nur �ber wlan eine Verbindung hergestellt werden
		// pr�fen, ob so eine Verbindung besteht
		if (ctxt.isWifiConnected())
		{
		    // es besteht verbindung
		    connectionAllowed = true;
		}
		else
		{
		    // Keine Verbindung einen kleinen Hinweis ausgeben, dass
		    // keine Verbindung besteht
		    ctxt.handler.post(new Toaster(ctxt, ctxt.activity.getString(R.string.msg_noWlan),
			    Toast.LENGTH_SHORT));
		}
	    }
	    else
	    {
		// Es darf auch ohne Wlan eine Verbindung aufgebaut werden
		connectionAllowed = true;
	    }

	    // nun pr�fen, ob die verbindung hergestellt werden darf
	    if (connectionAllowed)
	    {
		if (isCancelled())
		    return false;
		DownloadFeedback downloadFeedback = new DownloadFeedback(-1, DownloadFeedback.NO_REFRESH);
		try
		{
		    // Jetzt den Stundplan mit der gesuchten Wochennummer
		    // downloaden
		    downloadFeedback = downloader(isOnlineAvailableIndex, ONLYTIMETABLE);
		    if (isCancelled())
			return false;
		    // pr�fen ob ein pageturn gemacht werden soll
		    if (this.forcePageTurn)
		    {
			Calendar now = new GregorianCalendar();
			if (ctxt.getCurStupid().currentDate.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR))
			    ctxt.getCurStupid().currentDate = (Calendar) this.requestedDate.clone();
		    }
		}
		catch (Exception e)
		{
		    // parent.disablePagerOnChangedListener=false;
		    ctxt.handler.post(new ErrorMessage(ctxt, e.getMessage()));
		    return false;
		}

		try
		{
		    // den Index neu erstellen lassen
		    ctxt.getCurStupid().timeTableIndexer();
		}
		catch (Exception e)
		{
		    // Keine Klasse ausgew�hlt!
		    Tools.gotoSetup(ctxt);
		    return false;
		}

		ctxt.handler.post(new UpdateTimeTableList(ctxt, downloadFeedback));
	    }

	}
	else
	{
	    if (ctxt.progressDialog != null)
		ctxt.progressDialog.dismiss();

	    if (ctxt.getCurStupid().onlyWlan)
	    {
		if (ctxt.isWifiConnected())
		{
		    ctxt.handler.post(new Toaster(ctxt, this.errorMessage, Toast.LENGTH_SHORT));
		}
		else
		{
		    ctxt.handler.post(new Toaster(ctxt, ctxt.activity.getString(R.string.msg_noWlan),
			    Toast.LENGTH_SHORT));
		}
	    }
	    else
	    {
		ctxt.handler.post(new Toaster(ctxt, this.errorMessage, Toast.LENGTH_SHORT));
	    }

	}
	return false;

    }

    /**
     * @author Tobias Janssen
     * @param params
     * @return
     * @throws Exception
     */
    private DownloadFeedback downloader(int params) throws Exception
    {
	return downloader(0, params);
    }

    /**
     * @author Tobias Janssen
     * @param weekIndex
     * @param params
     * @return
     * @throws Exception
     */
    private DownloadFeedback downloader(int weekIndex, int params) throws Exception
    {
	try
	{
	    Boolean connectionAllowed = false;
	    if (ctxt.getCurStupid().onlyWlan)
	    {
		if (ctxt.isWifiConnected())
		{
		    connectionAllowed = true;
		}
		else
		{
		    connectionAllowed = false;
		}
	    }
	    else
	    {
		connectionAllowed = true;
	    }

	    if (connectionAllowed)
	    {
		// aktuelle Daten aus dem Netz laden:
		DownloadFeedback downloadFeedback;

		switch (params)
		{
		case BOTH:

		    ctxt.progressDialog.setMax(80000);
		    ctxt.getCurStupid().fetchSelectorsFromNet(ctxt);
		    ctxt.progressDialog.setProgress(15000);
		    downloadFeedback = ctxt.getCurStupid().fetchTimeTableFromNet(ctxt.getCurStupid().weekList[weekIndex].description,
			    ctxt.getCurStupid().getMyElement(), ctxt.getCurStupid().typeList[ctxt.getCurStupid().getMyType()].index, ctxt);
		    ctxt.progressDialog.setProgress(80000);
		    ctxt.handler.post(new UpdateTimeTableList(ctxt, downloadFeedback));

		    return downloadFeedback;
		case ONLYTIMETABLE:
		    int prgsLength = calculateProgress();
		    ctxt.progressDialog.setMax(prgsLength);
		    downloadFeedback = ctxt.getCurStupid().fetchTimeTableFromNet(ctxt.getCurStupid().weekList[weekIndex].description,
			    ctxt.getCurStupid().getMyElement(), ctxt.getCurStupid().typeList[ctxt.getCurStupid().getMyType()].index, ctxt);
		    ctxt.progressDialog.setProgress(prgsLength);
		    return downloadFeedback;
		case ONLYSELECTORS:
		    ctxt.progressDialog.setMax(15000);
		    ctxt.getCurStupid().fetchSelectorsFromNet(ctxt);
		    ctxt.progressDialog.setProgress(15000);
		    return new DownloadFeedback(-1, DownloadFeedback.NO_REFRESH);
		}
	    }
	    return new DownloadFeedback(-1, DownloadFeedback.NO_REFRESH);
	}
	catch (Exception e)
	{
	    throw e;
	}
    }

    /**
     * @author Tobias Janssen Sch�tzt ab, wieviele Progress-Punkte ben�tigt
     *         werden
     * @return
     */
    private int calculateProgress()
    {
	int result = 0;
	result += 29411; // ca. f�r readhtml. kann nur ca angegeben werden, da
			 // nicht berechenbar m�sste aber immer ungef�hr
			 // gleichbleiben
	result += 51400; // ca. f�r xmltoArray. ebenfalls unbekannt m�sste aber
			 // immer ungef�hr gleichbleiben

	result += 2000; // ca. f�r convertXMLTableToWeekData
	result += 1000; // ca. f�r conerttoMultiDim
	result += 100 * ctxt.getCurStupid().stupidData.size();
	result += 5000;
	return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
	// setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean bool)
    {
	try
	{
	    ctxt.getCurStupid().saveFiles(ctxt);
	}
	catch (Exception e)
	{

	}
	if (this.postExec != null)
	    postExec.run();
	if (ctxt.mIsRunning && ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	{
	    ctxt.progressDialog.dismiss();
	}
	ctxt.executor.scheduleNext();
    }

}
