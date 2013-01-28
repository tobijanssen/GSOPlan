/*
 * WorkerQueue.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;

public class WorkerQueue implements Runnable
{

    final Queue<AsyncTask<Boolean, Integer, Boolean>> tasks = new ArrayDeque<AsyncTask<Boolean, Integer, Boolean>>();
    final Queue<Boolean> tasksBools = new ArrayDeque<Boolean>();
    private ExecutorService monit;
    private MyContext ctxt;
    private AsyncTask<Boolean, Integer, Boolean> currentTask;
    private Boolean exitMonitThread;
    private Calendar born;
    private Boolean terminated=false;

    /**
     * @author janssen
     * @param ctxt		MyContext der Anwendung
     */
    public WorkerQueue(MyContext ctxt)
    {
	this.ctxt = ctxt;
	this.monit = Executors.newSingleThreadExecutor();
	this.exitMonitThread = false;
	this.monit.execute(this);
    }

    /**
     * @author janssen
     * @param newTask		AsyncTask der ausgeführt werden soll
     */
    public synchronized void execute(final AsyncTask<Boolean, Integer, Boolean> newTask)
    {
	if(!terminated)
	    tasks.add(newTask);
	if (currentTask == null)
	{
	    scheduleNext();
	}
	else if (currentTask != null)
	{
	    if (this.currentTask.isCancelled())
	    {
		this.currentTask = null;
		scheduleNext();
	    }
	}
    }

    /**
     * @author janssen
     * Führt den nächsten Task in der Queue aus
     */
    public synchronized void scheduleNext()
    {

	if (currentTask != null)
	{
	    currentTask = tasks.poll();
	    if (currentTask != null)
	    {
		try
		{
		    currentTask = currentTask.execute();
		    born = new GregorianCalendar();
		}
		catch (Exception e)
		{
		    Boolean debugMe = true;
		}
	    }
	}
	else
	{
	    currentTask = tasks.poll();
	    if (currentTask != null)
	    {
		currentTask = currentTask.execute();
		born = new GregorianCalendar();
	    }
	}

    }

    public void run()
    {

	while (!this.exitMonitThread)
	{
	    try
	    {

		Thread.sleep(1000);
		if (currentTask != null)
		{
		    Status status = currentTask.getStatus();
		    if (status.equals(Status.FINISHED))
		    {
			scheduleNext();
		    }
		    else
		    {
			// Task läuft noch
			// prüfen wie alt der ist
			if (born.getTimeInMillis() + (30 * 1000) < new GregorianCalendar().getTimeInMillis())
			{
			    // ist älter als 30 sekunden
			    this.currentTask.cancel(true);
			}

		    }
		}
	    }
	    catch (Exception e)
	    {

	    }

	}

    }

    /**
     * @author Tobias Janssen
     * Wartet die angegebene Zeit auf die Terminierung des aktivien Tasks<p> 
     * Wenn weitere Tasks anstehen, werden diese anschliesend ausgeführt
     * @param timeInMillisToTerminate
     */
    public void awaitTermination(int timeInMillisToTerminate)
    {
	try
	{

	    while (true)
	    {
		if(this.currentTask != null)
		    Thread.sleep(500);

		if (tasks.isEmpty())
		{
		    if(this.currentTask != null)
			this.currentTask.get(timeInMillisToTerminate, TimeUnit.MILLISECONDS);
		    return;
		}
		else
		    scheduleNext();
	    }
	}
	catch (Exception e)
	{

	}
    }

    /**
     * 14.11.12
     * 
     * @author Tobias Janssen 
     * Terminiert den aktiven Task im SerialExecutor
     */
    public void terminateActiveThread()
    {
	if (ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	{
	    ctxt.progressDialog.dismiss();
	}
	if(this.currentTask != null)
	    this.currentTask.cancel(true);
	
    }

    /**
     * 16.01.13
     * 
     * @author Tobias Janssen 
     * Terminiert alle anstehenden Threads im SerialExecutor und sperrt diesen, damit keine weiteren angenommen werden
     */
    public void terminateAllThreads()
    {
	tasks.clear();
	this.terminated = true;
	terminateActiveThread();
	awaitTermination(2000);
	ctxt.progressDialog = null;
	this.exitMonitThread = true;
    }
}
