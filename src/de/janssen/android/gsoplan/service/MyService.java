package de.janssen.android.gsoplan.service;

import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.dataclasses.Const;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;

public class MyService extends IntentService implements Runnable
{
    private Intent intent;
    public MyService()
    {
	super("ServiceConnector");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	this.intent=intent;
	Bundle extras = intent.getExtras();
	Boolean fromFrontend =false;
	if (extras != null)
	{
	    fromFrontend = extras.getBoolean("fromFrontend", false);
	}
	new BackgroundSync(this,this,fromFrontend).execute(Boolean.FALSE,Boolean.FALSE,Boolean.FALSE);
    }

    @Override
    public void run()
    {
	Bundle extras = intent.getExtras();
	if (extras != null)
	{
	    Messenger messenger = (Messenger) extras.get("MESSENGER");
	    Logger logger = new Logger(this);
	    if(messenger != null)
	    {
		Message msg = Message.obtain();
		msg.arg1 = Activity.RESULT_OK;
		try
		{
		    logger.log(Logger.Level.INFO_1, "Starting MessageHandler(Refresh)");
		    messenger.send(msg);
		}
		catch (android.os.RemoteException e1)
		{
		    
		    logger.log(Logger.Level.ERROR, "Exception sending Service-Message", e1);
		}
	    }
	    else
	    {
		logger.log(Logger.Level.INFO_1, "Starting BROADCASTREFRESH");
		
		Intent intent = new Intent(Const.BROADCASTREFRESH);
		intent.putExtra("message", Activity.RESULT_OK);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	    }
	}
    }
    
    

}
