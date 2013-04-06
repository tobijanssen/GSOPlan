/*
 * AboutGSOPlan.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.activities;

import com.google.analytics.tracking.android.EasyTracker;

import de.janssen.android.gsoplan.R;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class AboutGSOPlan extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_about_gsoplan);
    }

    public void openPlayStore(View view)
    {
	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=de.janssen.android.gsoplan"));
	startActivity(intent);
    }

    @Override
    public void onStart()
    {
	super.onStart();

	EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop()
    {
	super.onStop();
	EasyTracker.getInstance().activityStop(this);
    }

}
