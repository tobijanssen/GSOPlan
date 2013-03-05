/*
 * Toaster.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.runnables;

import android.widget.Toast;
import de.janssen.android.gsoplan.MyContext;

public class Toaster implements Runnable
{
    private String toastText;
    private int duration;
    private MyContext ctxt;

    /**
     * Erstellt einen Toast mit Angefügtem Text
     * @param ctxt
     * @param toastText
     * @param duration
     */
    public Toaster(MyContext ctxt, String toastText, int duration)
    {
	this.ctxt = ctxt;
	this.toastText = toastText;
	this.duration = duration;
    }

    @Override
    public void run()
    {
	if(ctxt.mIsRunning)
	{
        	ctxt.progressDialog.dismiss();
        	Toast.makeText(ctxt.context, this.toastText, duration).show();
	}
    }

}
