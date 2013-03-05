/*
 * MainActivity.java
 * 
 * Tobias Janssen, 2013
 * Einstiegtpunkt für die App, lädt die Settings und startet anschließend die entsprechende Activity 
 * 
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity
{

    public MyContext ctxt = new MyContext(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	getMenuInflater().inflate(R.menu.activity_main, menu);
	return true;
    }

    public void loadData()
    {
	// die Settings Laden
	ctxt.getPrefs(this.getApplicationContext());
	try
	{
	    if (!Tools.isNewVersion(ctxt))
	    {
		// version bereits bekannt
		// default activity starten
		continueAppStart();
	    }
	}
	catch (Exception e)
	{
	    // fehler beim lesen der Versionsdatei
	    // egal, laden fortsetzen
	    continueAppStart();

	}
    }

    public void continueAppStart(View view)
    {
	Intent intent = new Intent(ctxt.activity, ctxt.getDefaultActivityClass());
	ctxt.activity.startActivity(intent);
	this.finish();
    }

    public void continueAppStart()
    {
	Intent intent = new Intent(ctxt.activity, ctxt.getDefaultActivityClass());
	ctxt.activity.startActivity(intent);
	this.finish();
    }

}
