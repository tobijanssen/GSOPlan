package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.PlanActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ErrorMessage implements Runnable{

	private PlanActivity parent;
	private String errorMessage;
	public ErrorMessage(PlanActivity parent, String errorMessage)
	{
		this.parent=parent;
		this.errorMessage=errorMessage;
	}
	
	@Override
	public void run() {
		if(parent.stupid.progressDialog != null && parent.stupid.progressDialog.isShowing())
		{
			parent.stupid.progressDialog.dismiss();
		}
		if(!errorMessage.equalsIgnoreCase(""))
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(parent);
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
