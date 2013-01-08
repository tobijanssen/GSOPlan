package de.janssen.android.gsoplan;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;

public class WorkerQueue implements Runnable{

	
	final Queue<AsyncTask<Boolean, Integer, Boolean>> tasks = new ArrayDeque<AsyncTask<Boolean, Integer, Boolean>>();
	final Queue<Boolean> tasksBools = new ArrayDeque<Boolean>();
	public ExecutorService monit; 	
	private AsyncTask<Boolean, Integer, Boolean> currentTask;
	private Boolean curentTaskPostrun;
	private Boolean exitMonitThread;
	private Calendar born;
	
	public WorkerQueue()
	{
		this.monit = Executors.newSingleThreadExecutor();
		this.exitMonitThread=false;
		this.monit.execute(this);
	}
	
	public synchronized void execute(final AsyncTask<Boolean, Integer, Boolean> newTask, Boolean postRun) 
	{
		
		tasks.add(newTask);
		tasksBools.add(postRun);
		if (currentTask == null) 
		{
			scheduleNext();
		}
		else if(currentTask != null)
		{
			if(this.currentTask.isCancelled())
			{
			
				this.currentTask = null;
				scheduleNext();
			}
		}
	}
	
	
	
	
		
	protected synchronized void scheduleNext() 
	{

		currentTask = tasks.poll();
		curentTaskPostrun = this.tasksBools.poll();
		
		if (currentTask != null) 
		{
			try
			{
				currentTask.execute(curentTaskPostrun);
			    born = new GregorianCalendar();
			}
			catch(Exception e)
			{
				Boolean debugMe=true;
			}
		}
		else if(!tasks.isEmpty())
		{
			Boolean debugMe=true;
		}
			
	}
	
	public void run() 
	{
		while(!this.exitMonitThread)
		{
			try
			{
				
				Thread.sleep(1000);
				if(currentTask != null)
				{
					Status status = currentTask.getStatus();
					if(status.equals(Status.FINISHED))
					{
						scheduleNext();
					}
					else
					{
						//Task läuft noch
						//prüfen wie alt der ist
						if(born.getTimeInMillis() + (30 * 1000) < new GregorianCalendar().getTimeInMillis())
						{
							//ist älter als 30 sekunden
							this.currentTask.cancel(true);
						}
												
					}
				}
			}
			catch(Exception e)
			{
				
			}
			
			
		}
		
	}
	
	/*	14.11.12
	 * 	Tobias Janssen
	 * 	Terminiert den aktiven Thread im SerialExecutor
	 */
	public void terminateActiveThread()
	{

			//this.currentThread.interrupt();
		this.currentTask.cancel(true);
		
		
		
	}
}
