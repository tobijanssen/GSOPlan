package de.janssen.android.gsoplan;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class SerialExecutor implements Executor, Runnable 
{

	final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
	final Executor executor;
	Runnable active;
	Runnable r;

	public SerialExecutor(Executor executor) 
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
			   executor.execute(active);
			   
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
		
	}


