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

    public ErrorMessage(MyContext ctxt, String errorMessage, OnClickListener onClick, String positvButtonText)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onClick = onClick;
	this.positvButtonText = positvButtonText;
    }

    public ErrorMessage(MyContext ctxt, String errorMessage, OnClickListener onClick)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onClick = onClick;
    }

    public ErrorMessage(MyContext ctxt, String errorMessage, String positvButtonText)
    {
	this.ctxt = ctxt;
	this.errorMessage = errorMessage;
	this.onClick = null;
	this.positvButtonText = positvButtonText;
    }

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
	if (errorMessage != null && !errorMessage.equalsIgnoreCase(""))
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
