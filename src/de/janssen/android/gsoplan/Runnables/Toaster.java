package de.janssen.android.gsoplan.Runnables;

import android.widget.Toast;
import de.janssen.android.gsoplan.PlanActivity;

public class Toaster implements Runnable{
	private String toastText;
	private int duration;
	private PlanActivity parent;

	public Toaster(PlanActivity parent,String toastText, int duration )
	{
		this.parent=parent;
		this.toastText=toastText;
		this.duration = duration;
	}
	
	@Override
	public void run() {
		parent.stupid.progressDialog.dismiss();
		
		Toast.makeText(parent, this.toastText, duration).show();
	}

}
