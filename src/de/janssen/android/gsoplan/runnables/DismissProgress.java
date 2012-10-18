package de.janssen.android.gsoplan.runnables;

import de.janssen.android.gsoplan.StupidCore;

public class DismissProgress implements Runnable {
	
	private StupidCore stupid;
	
	public DismissProgress(StupidCore stupid)
	{
		this.stupid=stupid;
	}
	
	@Override
	public void run() {
		if(stupid.progressDialog != null && stupid.progressDialog.isShowing())
			stupid.progressDialog.dismiss();
		
	}
}
