/*
 * SaveElement.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.asyncTasks;

import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.core.MyContext;
import java.io.File;
import android.os.AsyncTask;

public class AsyncSaveToFile extends AsyncTask<Boolean, Integer, Boolean>
{

    private String stringContent;
    private File file;
    public Exception exception;
    private Runnable postRun;
    private MyContext ctxt;

    /**
     * 
     * @param ctxt
     * @param mProfilFile
     * @param postRun
     * @param showDialog		Boolean ob ein Dialog erzeugt werden soll
     */
    public AsyncSaveToFile(String stringContent, File file, Runnable postRun, MyContext ctxt)
    {
	this.file=file;
	this.stringContent=stringContent;
	this.postRun = postRun;
	this.ctxt=ctxt;

    }

    /**
     * 
     * @param stringContent
     * @param file
     * @param postRun
     */
    public AsyncSaveToFile(String stringContent, File file, Runnable postRun)
    {
	this.file=file;
	this.stringContent=stringContent;
	this.postRun = postRun;
    }
    
    /**
     * 
     * @param stringContent
     * @param file
     * @param postRun
     */
    public AsyncSaveToFile(String stringContent, File file)
    {
	this.file=file;
	this.stringContent=stringContent;
    }

    @Override
    protected Boolean doInBackground(Boolean... bool)
    {
	try
	{
	    if (isCancelled())
	    {
		return null;
	    }
	    FileOPs.saveToFile(stringContent,file);

	}
	catch (Exception e)
	{
	    this.exception = e;
	}

	return true;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
	if (this.postRun != null)
	    postRun.run();
	this.ctxt.executor.scheduleNext();
    }

}
