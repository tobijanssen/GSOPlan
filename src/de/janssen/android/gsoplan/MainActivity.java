package de.janssen.android.gsoplan;

import java.io.File;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity {

	public MyContext ctxt = new MyContext();
	
	
	public MainActivity()
	{
		ctxt.context=this;
		ctxt.activity=this;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loadData();
		Intent intent = new Intent(ctxt.activity,ctxt.defaultActivity);
		ctxt.activity.startActivity(intent);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
	    
	    EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() 
	{
		super.onStop();
	    EasyTracker.getInstance().activityStop(this);
	}

	   
	@Override
	protected void onResume() 
	{
	    super.onResume();
	    this.finish();
	}
	
	 public void loadData()
	    {
	    	// die SetupDatei Laden
	    	try {
	    		Xml xml = new Xml();
	    		File setupFile = Tools.getFileSaveSetup(ctxt);
	    		xml.container = FileOPs.readFromFile(setupFile);
	    		ctxt.stupid.clearSetup();
	    		ctxt.stupid.fetchSetupFromXml(xml,ctxt);
	    		
	    	} 
	    	catch (Exception e) 
	    	{
	    		ctxt.defaultActivity=PlanActivity.class;
	    	}
	    }
	
}
