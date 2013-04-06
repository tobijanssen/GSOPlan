package de.janssen.android.gsoplan.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemStartService extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
	Intent serviceIntent = new Intent(context, AlarmStarter.class);
	context.startService(serviceIntent);

    }
}
