/*
 * PlanActivityLuncher.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.asyncTasks;

import java.io.File;
import java.util.GregorianCalendar;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.activities.PlanActivity;
import de.janssen.android.gsoplan.core.StupidOPs;
import de.janssen.android.gsoplan.core.Tools;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.runnables.ErrorMessage;

public class PlanActivityLuncher extends AsyncTask<Boolean, Integer, Boolean>
{

    private PlanActivity parent;
    private Runnable preExec = null;
    public PlanActivityLuncher(PlanActivity parent)
    {
	this.parent = parent;
    }
    public PlanActivityLuncher(PlanActivity parent, Runnable preExec)
    {
	this.parent = parent;
	this.preExec=preExec;
    }

    @Override
    protected void onPreExecute()
    {
	if(preExec != null)
	    preExec.run();
	super.onPreExecute();
    }

    protected Boolean doInBackground(Boolean... bool)
    {
	parent.ctxt.initViewPagerWaiting();
	selfCheck();
	parent.ctxt.appIsReady=true;
	parent.ctxt.executor.scheduleNext();
	return null;
    }


    
    /**
     * @author Tobias Janssen
     * Führt die Laufzeitprüfung durch, und ergreift nötige Maßbahmen im Fehlerfall
     * 
     * @param prevErrorCode		Integer der den vorherigen Fehler angibt
     */
    private void selfCheck()
    {
	//Strukturprüfung durchführen
	int errorlevel = parent.ctxt.mProfil.stupid.checkStructure(parent.ctxt);
	switch (errorlevel)
	{
	case 0: // Alles in Ordnung
	    
	    try
	    {
		parent.ctxt.mProfil.stupid.clearData();
		Tools.loadAllDataFiles(parent.ctxt.context,parent.ctxt.mProfil, parent.ctxt.mProfil.stupid);
	    }
	    catch (Exception e)
	    {
		parent.ctxt.handler.post(new ErrorMessage(parent.ctxt, e.getMessage()));
	    }
	    parent.ctxt.mProfil.stupid.sort();
	    //prüfen, ob der heutige Tag in den Daten vorhanden ist:
	    if(!parent.ctxt.mProfil.stupid.isDateAvailable(new GregorianCalendar()))
	    {
		StupidOPs.contactStupidService(parent.ctxt.context, parent.ctxt.msgHandler);
	    }
	    parent.ctxt.initViewPager();
	   
	    break;
	case 1: // TypesDatei Datei fehlt
	case 2: // FILEELEMENT Datei fehlt
		// Backend beauftragen diese herunterzu laden
	    StupidOPs.contactStupidService(parent.ctxt.context, parent.ctxt.msgHandler);
	    
	    
	case 3: // Keine Klasse ausgewählt
	    OnClickListener onClick = new OnClickListener()
	    {

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    Tools.gotoSetup(parent.ctxt, Const.FIRSTSTART, true);
		}
	    };
	    String message = "";
	    if (parent.ctxt.newVersionReqSetup)
	    {
		message = parent.ctxt.context.getString(R.string.msg_newVersionReqSetup);
	    }
	    else
	    {
		message = parent.ctxt.context.getString(R.string.msg_noElement);
	    }
	    parent.ctxt.handler.post(new ErrorMessage(parent.ctxt, message, onClick, "Einstellungen öffnen"));
	    break;
	case 6: // Elementenordner existiert nicht
		// neuen anlegen

	    File elementDir = new File(parent.getFilesDir(), parent.ctxt.mProfil.getMyElement());
	    elementDir.mkdir();
	    //Backend daten laden lassen:
	    StupidOPs.contactStupidService(parent, parent.ctxt.msgHandler);
	    break;
	case 7: // Keine Daten für diese Klasse vorhanden
	    StupidOPs.contactStupidService(parent, parent.ctxt.msgHandler);
	    break;
	}
	
    }
}
