package de.janssen.android.gsoplan.runnables;

import android.widget.Toast;
import de.janssen.android.gsoplan.MyContext;

public class Toaster implements Runnable{
	private String toastText;
	private int duration;
	private MyContext ctxt;

	public Toaster(MyContext ctxt,String toastText, int duration )
	{
		this.ctxt=ctxt;
		this.toastText=toastText;
		this.duration = duration;
	}
	
	@Override
	public void run() {
		ctxt.stupid.progressDialog.dismiss();
		
		Toast.makeText(ctxt.context, this.toastText, duration).show();
	}

}
