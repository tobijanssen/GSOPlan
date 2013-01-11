/*
 * MainActivity.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan;

import java.io.File;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.xml.Xml;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;


public class MainActivity extends Activity {

	public MyContext ctxt = new MyContext(this, this);
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loadData();	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	
	 public void loadData()
	 {
		// die SetupDatei Laden
	    try 
	    {
	    	Xml xml = new Xml();
	    	File setupFile = Tools.getFileSaveElement(ctxt);
	    	xml.container = FileOPs.readFromFile(setupFile);
	    	ctxt.stupid.clearElements();
	    	ctxt.stupid.fetchElementsFromXml(xml,ctxt);
	    		
	    } 
	    catch (Exception e) 
	    {
	    	try 
	    	{
				ctxt.setDefaultActivity("Tag");
			}
	    	catch (Exception e1) 
	    	{
				// kommt nicht vor
			}
	    }
	    ctxt.getPrefs(this.getApplicationContext());
	    try
	    {
		    if(!Tools.isNewVersion(ctxt))
		    {
		    	//version bereits bekannt
		    	//default activity starten
		    	continueAppStart();
		    }
	    }
	    catch(Exception e)
	    {
	    	//fehler beim lesen der Versionsdatei
	    	//egal, laden fortsetzen
	    	continueAppStart();
	   	 
	    }
	 }
	 
	 public void continueAppStart(View view)
	 {
			Intent intent = new Intent(ctxt.activity,ctxt.getDefaultActivityClass());
			//intent.putExtra("newVersionInfo", true);
			//intent.putExtra("newVersionMsg", );
			ctxt.activity.startActivity(intent);
			this.finish();
	 }
	 
	 public void continueAppStart()
	 {
		Intent intent = new Intent(ctxt.activity,ctxt.getDefaultActivityClass());
		ctxt.activity.startActivity(intent);
		this.finish();
	 }

	
}
