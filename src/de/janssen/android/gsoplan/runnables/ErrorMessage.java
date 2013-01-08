package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.MyContext;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ErrorMessage implements Runnable{

	private MyContext ctxt;
	private String errorMessage;
	public ErrorMessage(MyContext ctxt, String errorMessage)
	{
		this.ctxt=ctxt;
		this.errorMessage=errorMessage;
	}
	
	@Override
	public void run() {
		if(ctxt.stupid.progressDialog != null && ctxt.stupid.progressDialog.isShowing())
		{
			ctxt.stupid.progressDialog.dismiss();
		}
		if(!errorMessage.equalsIgnoreCase(""))
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(ctxt.context);
			dialog.setMessage(errorMessage);
			dialog.setPositiveButton("Ok", new OnClickListener(){
	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
				
			});
			dialog.show();
		}
	}
}
