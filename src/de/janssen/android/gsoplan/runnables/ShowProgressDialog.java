package de.janssen.android.gsoplan.runnables;

import android.app.ProgressDialog;
import de.janssen.android.gsoplan.PlanActivity;

public class ShowProgressDialog implements Runnable{

	private PlanActivity parent;
	private int style;
	private String text;
	
	public ShowProgressDialog(PlanActivity parent,int style, String text){
		this.parent=parent;
		this.style=style;
		this.text=text;
	}
	@Override
	public void run() {
		if(parent.stupid.progressDialog != null && parent.stupid.progressDialog.isShowing())
			parent.stupid.progressDialog.dismiss();
		parent.stupid.progressDialog =  new ProgressDialog(ShowProgressDialog.this.parent);
		parent.stupid.progressDialog.setProgressStyle(style);
		parent.stupid.progressDialog.setMessage(text);
		parent.stupid.progressDialog.setCancelable(true);
		parent.stupid.progressDialog.setProgress(0);
		parent.stupid.progressDialog.show();
		
	}
	

	
	

}
