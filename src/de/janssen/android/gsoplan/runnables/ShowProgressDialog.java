package de.janssen.android.gsoplan.runnables;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import de.janssen.android.gsoplan.MyContext;

public class ShowProgressDialog implements Runnable{

	private MyContext ctxt;
	private int style;
	private String text;
	private Runnable runThread;
	private AsyncTask<Boolean, Integer, Boolean> newTask;
	
	public ShowProgressDialog(MyContext ctxt,int style, String text, Runnable runThread){
		this.ctxt=ctxt;
		this.style=style;
		this.text=text;
		this.runThread=runThread;
	}
	
	public ShowProgressDialog(MyContext ctxt,int style, String text,AsyncTask<Boolean, Integer, Boolean> newTask){
		this.ctxt=ctxt;
		this.style=style;
		this.text=text;
		this.newTask=newTask;
		
	}
	@Override
	public void run() {
		if(ctxt.stupid.progressDialog != null && ctxt.stupid.progressDialog.isShowing())
			ctxt.stupid.progressDialog.dismiss();
		ctxt.stupid.progressDialog =  new ProgressDialog(ShowProgressDialog.this.ctxt.context);
		ctxt.stupid.progressDialog.setProgressStyle(style);
		ctxt.stupid.progressDialog.setMessage(text);
		ctxt.stupid.progressDialog.setCancelable(true);
		ctxt.stupid.progressDialog.setProgress(0);
		ctxt.stupid.progressDialog.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				if(newTask !=null)
					newTask.cancel(true);
				
			}});
		ctxt.stupid.progressDialog.show();
		
	}
	

	
	

}
