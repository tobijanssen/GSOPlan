package de.janssen.android.gsoplan.runnables;

import android.os.AsyncTask;
import de.janssen.android.gsoplan.StupidCore;

public class DismissProgress extends AsyncTask<Boolean, Integer, Boolean> {
	
	private StupidCore stupid;
	
	public DismissProgress(StupidCore stupid)
	{
		this.stupid=stupid;
	}
	
	protected Boolean doInBackground(Boolean... bool) {
		if(stupid.progressDialog != null && stupid.progressDialog.isShowing())
			stupid.progressDialog.dismiss();
		return null;
		
	}
}
