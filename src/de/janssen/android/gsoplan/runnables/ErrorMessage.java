/*
 * ErrorMessage.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.core.MyContext;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ErrorMessage implements Runnable
{

    private MyContext ctxt;
    private String errorMessage;
    private OnClickListener onPositiveClick;
    private String positveButtonText = "Ok";
    private OnClickListener onNegativeClick;
    private String negativeButtonText = "Abbrechen";
    private String title = "";

    
    /**
     * Erzeugt neue Fehlermeldung
     * @param ctxt			MyContext der Applikation
     * @param errorMessage		String der die Meldung enthält
     * @param onClick			OnClickListener für positiven Klick
     * @param positvButtonText		String Positive Button Text
     */
    public ErrorMessage(MyContext ctxt, String errorMessage, OnClickListener onClick, String positveButtonText)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onPositiveClick = onClick;
	this.positveButtonText = positveButtonText;
    }
    /**
     * 
     * @param ctxt
     * @param errorMessage
     * @param onPositiveClick
     * @param positvButtonText
     */
    public ErrorMessage(MyContext ctxt, String errorMessage, OnClickListener onPositiveClick, String positveButtonText,OnClickListener onNegativeClick, String negativeButtonText)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onPositiveClick = onPositiveClick;
	this.positveButtonText = positveButtonText;
	this.onNegativeClick = onNegativeClick;
	this.negativeButtonText = negativeButtonText;
    }
    public ErrorMessage(MyContext ctxt, String title, String errorMessage, OnClickListener onPositiveClick, String positveButtonText,OnClickListener onNegativeClick, String negativeButtonText)
    {
	this.ctxt = ctxt;
	this.title = title;
	this.errorMessage = errorMessage;
	this.onPositiveClick = onPositiveClick;
	this.positveButtonText = positveButtonText;
	this.onNegativeClick = onNegativeClick;
	this.negativeButtonText = negativeButtonText;
    }
    
    public ErrorMessage(MyContext ctxt, String title, String errorMessage, OnClickListener onPositiveClick, String positveButtonText)
    {
	this.ctxt = ctxt;
	this.title = title;
	this.errorMessage = errorMessage;
	this.onPositiveClick = onPositiveClick;
	this.positveButtonText = positveButtonText;
    }
    
    public ErrorMessage(MyContext ctxt, String title, String errorMessage, String positveButtonText)
    {
	this.ctxt = ctxt;
	this.title = title;
	this.errorMessage = errorMessage;
	this.positveButtonText = positveButtonText;
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
	this.onPositiveClick = onClick;
    }
    /**
     * Erzeugt neue Fehlermeldung
     * @param ctxt			MyContext der Applikation
     * @param errorMessage		String der die Meldung enthält
     * @param positvButtonText		String Positive Button Text
     */
    public ErrorMessage(MyContext ctxt, String errorMessage, String positveButtonText)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onPositiveClick = null;
	this.positveButtonText = positveButtonText;
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
	this.onPositiveClick = null;
    }

    @Override
    public void run()
    {
	if (ctxt.mIsRunning && errorMessage != null && !errorMessage.equalsIgnoreCase(""))
	{
	    try
	    {
		AlertDialog.Builder dialog = new AlertDialog.Builder(ctxt.context);
		dialog.setMessage(errorMessage);
		if(!title.equalsIgnoreCase(""))
		    dialog.setTitle(title);
		if (onPositiveClick == null)
		{
		    dialog.setPositiveButton(positveButtonText, new OnClickListener()
		    {

			@Override
			public void onClick(DialogInterface dialog, int which)
			{

			}

		    });
		}
		else
		{
		    dialog.setPositiveButton(positveButtonText, this.onPositiveClick);
		}
		if (onNegativeClick != null)
		{
		    dialog.setNegativeButton(negativeButtonText, this.onNegativeClick);
		}

		dialog.show();
	    }
	    catch(Exception e)
	    {
		ctxt.logger.log(Logger.Level.ERROR, "Dialog konnte nicht erstellt werden", e);
	    }
	}
    }
}
