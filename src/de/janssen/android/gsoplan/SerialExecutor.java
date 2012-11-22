package de.janssen.android.gsoplan;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


public class SerialExecutor implements Executor, Runnable 
{

	final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
	final ExecutorService executor;
	Runnable active;
	Runnable r;

	public SerialExecutor(ExecutorService executor) 
	{
		this.executor = executor;
	}

	
	public synchronized void execute(final Runnable r) 
	{
		this.r=r;
		tasks.add(this);
			
		if (active == null) 
		{
			scheduleNext();
		}
	}

	protected synchronized void scheduleNext() 
	{

		active = tasks.poll();
		   
		if (active != null) 
		{
			try
			{
			   executor.execute(active);
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
		try 
		{
			r.run();
			
		} 
		finally 
		{
			scheduleNext();
		}
		
	}
		
	

	/*	14.11.12
	 * 	Tobias Janssen
	 * 	Terminiert den aktiven Thread im SerialExecutor
	 */
	public void terminateActiveThread(Runnable runThread)
	{
		if (active != null) 
		{
			//TODO: Implementieren eines Thread terminators
		}
		
	}

}
