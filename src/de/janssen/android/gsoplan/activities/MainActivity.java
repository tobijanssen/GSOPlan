/*
 * MainActivity.java
 * 
 * Tobias Janssen, 2013
 * Einstiegtpunkt für die App, lädt die Settings und startet anschließend die entsprechende Activity 
 * 
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan.activities;

import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.core.StupidOPs;
import de.janssen.android.gsoplan.core.Tools;
import de.janssen.android.gsoplan.runnables.ErrorMessage;
import de.janssen.android.gsoplan.service.AlarmStarter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import de.janssen.android.gsoplan.dataclasses.ProfilManager;

public class MainActivity extends Activity
{

    public MyContext ctxt;
    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	ctxt = new MyContext(this);
	ctxt.mIsRunning = true;
	// Extra Daten abholen
	extras = getIntent().getExtras();
	setContentView(R.layout.activity_main);
	loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	return true;
    }

    public void loadData()
    {
	try
	{
	    if (!Tools.isNewVersion(ctxt))
	    {
		// version bereits bekannt
		// default activity starten
		continueAppStart();
	    }
	    else
	    {
		StupidOPs.contactStupidService(ctxt.context, null);
	    }
	}
	catch (Exception e)
	{
	    // fehler beim lesen der Versionsdatei
	    // egal, laden fortsetzen
	    continueAppStart();

	}

    }

    @Override
    protected void onStop()
    {
	ctxt.mIsRunning = false;
	super.onStop();
    }

    public void continueAppStart(View view)
    {
	boolean resync = false;
	try
	{
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt.context);
	    resync = prefs.getBoolean("boxAutoSync", false);
	}
	catch (Exception e)
	{
	}

	if (!resync)
	{
	    new ErrorMessage(ctxt, this.getString(R.string.msg_newFunktionAvailable) + ctxt.context.getString(R.string.msg_AutoSync),
		    new OnClickListener()
		    {

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			    // ja, bitte aktivieren
			    SharedPreferences prefs;
			    try
			    {
				ProfilManager pm = new ProfilManager(ctxt);
				prefs = PreferenceManager.getDefaultSharedPreferences(ctxt.context);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("boxAutoSync", true);
				editor.putString("listResync", "60");
				editor.apply();
				ctxt.mProfil.autoSync = true;
				ctxt.mProfil.myResync = 60;
				pm.profiles.get(pm.currentProfilIndex).loadPrefs();
				pm.saveAllProfiles();
			    }
			    catch (Exception e)
			    {
				ctxt.logger.log(Logger.Level.ERROR, "Error loading Preferences", e);
			    }
			    Intent service = new Intent(MainActivity.this, AlarmStarter.class);
			    MainActivity.this.startService(service);

			    Intent intent = new Intent(MainActivity.this, de.janssen.android.gsoplan.activities.PlanActivity.class);
			    MainActivity.this.startActivity(intent);
			    MainActivity.this.finish();

			}

		    }, "Ja, bitte!", new OnClickListener()
		    {

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			    Intent intent = new Intent(MainActivity.this, de.janssen.android.gsoplan.activities.PlanActivity.class);
			    MainActivity.this.startActivity(intent);
			    MainActivity.this.finish();
			}
		    }, "Nein, jetzt noch nicht!").run();
	}
	else
	{
	    Intent intent = new Intent(MainActivity.this, de.janssen.android.gsoplan.activities.PlanActivity.class);
	    MainActivity.this.startActivity(intent);
	    MainActivity.this.finish();
	}

    }

    public void continueAppStart()
    {
	Intent intent = new Intent(this, de.janssen.android.gsoplan.activities.PlanActivity.class);

	if (extras != null)
	{
	    intent.putExtras(extras);
	}
	else
	{
	    Intent service = new Intent(MainActivity.this, AlarmStarter.class);
	    MainActivity.this.startService(service);
	}
	MainActivity.this.startActivity(intent);

	MainActivity.this.finish();

    }

}
