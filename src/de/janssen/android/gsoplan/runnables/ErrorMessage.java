/*
 * ErrorMessage.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.MyContext;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ErrorMessage implements Runnable
{

    private MyContext ctxt;
    private String errorMessage;
    private OnClickListener onClick;
    private String positvButtonText = "Ok";

    
    /**
     * Erzeugt neue Fehlermeldung
     * @param ctxt			MyContext der Applikation
     * @param errorMessage		String der die Meldung enthält
     * @param onClick			OnClickListener für positiven Klick
     * @param positvButtonText		String Positive Button Text
     */
    public ErrorMessage(MyContext ctxt, String errorMessage, OnClickListener onClick, String positvButtonText)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onClick = onClick;
	this.positvButtonText = positvButtonText;
    }

    /**
     * Erzeugt neue Fehlermeldung
     * @param ctxt			MyContext der Applikation
     * @param errorMessage		String der die Meldung enthält
     * @param onClick			OnClickListener für positiven Klick
     */
    public ErrorMessage(MyContext ctxt, String errorMessage, OnClickListener onClick)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onClick = onClick;
    }
    /**
     * Erzeugt neue Fehlermeldung
     * @param ctxt			MyContext der Applikation
     * @param errorMessage		String der die Meldung enthält
     * @param positvButtonText		String Positive Button Text
     */
    public ErrorMessage(MyContext ctxt, String errorMessage, String positvButtonText)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onClick = null;
	this.positvButtonText = positvButtonText;
    }
    /**
     * Erzeugt neue Fehlermeldung
     * @param ctxt			MyContext der Applikation
     * @param errorMessage		String der die Meldung enthält
     */
    public ErrorMessage(MyContext ctxt, String errorMessage)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onClick = null;
    }

    @Override
    public void run()
    {
	if (ctxt.progressDialog != null && ctxt.progressDialog.isShowing())
	{
	    ctxt.progressDialog.dismiss();
	}
	if (ctxt.mIsRunning && errorMessage != null && !errorMessage.equalsIgnoreCase(""))
	{
	    AlertDialog.Builder dialog = new AlertDialog.Builder(ctxt.context);
	    dialog.setMessage(errorMessage);
	    if (onClick == null)
	    {
		dialog.setPositiveButton(positvButtonText, new OnClickListener()
		{

		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    {

		    }

		});
	    }
	    else
	    {
		dialog.setPositiveButton(positvButtonText, this.onClick);
	    }

	    dialog.show();
	}
    }
}
