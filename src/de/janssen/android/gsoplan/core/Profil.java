package de.janssen.android.gsoplan.core;

import java.io.File;
import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.Types;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Profil
{
    public String myElement = "";
    public int myTypeIndex = 0;
    public String myTypeKey = "";
    public String myTypeName = "";
    public Boolean onlyWlan = false;
    public Boolean hideEmptyHours = true;
    public Boolean autoSync = false;
    public Boolean notificate = false;
    public Boolean vibrate = false;
    public Boolean sound = false;
    public Types types = new Types();
    public Boolean isDirty = false;
    public long myResync = 10;
    public long mylastResync = 0;
    private MyContext ctxt;
    public Stupid stupid;
    
    public Profil(MyContext ctxt)
    {
	this.ctxt=ctxt;
	this.stupid = new Stupid();
	this.loadPrefs();
    }

    
    /**
     * Liefert die Types Datei
     * @param ctxt
     * @return
     */
    public File getTypesFile(Context ctxt)
    {
	File file = new File(ctxt.getFilesDir() , Const.FILETYPE );
	return file;
    }
    public Type currType()
    {
	return types.list.get(myTypeIndex); 
    }
    
    
    /**
     * @return
     */
    public String getMyElement()
    {
	return this.myElement;
    }

    /**
     * @param value
     * 
     */
    public void setMyElement(String value)
    {
	this.myElement = value;
    }
    
    
    /**
     * @author Tobias Janssen 
     * lädt die einstellungen der applikation
     * 
     * @param context 	Context der Applikation
     */
    public void loadPrefs()
    {
	SharedPreferences prefs;
	try
	{
	    prefs = PreferenceManager.getDefaultSharedPreferences(ctxt.context);
	}
	catch (Exception e)
	{
	    ctxt.logger.log(Logger.Level.ERROR, "Error loading Preferences", e);
	    return;
	}
	
	//prüfen, ob noch alte Settingsversionen vorhanden sind, wenn ja diese konvertieren und löschen
//	Tools.translateOldSettings(ctxt);

	try
	{
	    myElement=prefs.getString("listElement", "");
	    if(myElement.equalsIgnoreCase("Verbindungsfehler"))
		myElement="";
	}
	catch (Exception e)
	{
	    // Element ist ungültig
	}
	try
	{
	    myTypeName = prefs.getString("listType", "Klassen");
	    if(myTypeName.equalsIgnoreCase("Verbindungsfehler"))
		myTypeName="Klassen";
	    myTypeIndex = prefs.getInt("myTypeIndex", 0);
	    myTypeKey = prefs.getString("myTypeKey", "c");;
	}
	catch (Exception e)
	{
	    // listType ist ungültig
	}
	try
	{
	    String value = prefs.getString("listResync", "60");
	    myResync=Long.parseLong(value);
	}
	catch (Exception e)
	{
	    // Resync ist ungültig
	}

	hideEmptyHours = prefs.getBoolean("boxHide", false);
	onlyWlan = prefs.getBoolean("boxWlan", false);
	autoSync = prefs.getBoolean("boxAutoSync", false);
	notificate = prefs.getBoolean("boxNotify", true);
	vibrate = prefs.getBoolean("boxVibrate", true);
	sound = prefs.getBoolean("boxSound", true);

    }
    
    
    public int getMyTypeIndex()
    {
	if(myTypeName == null)
	    return 0;
	for(int i=0;i<types.list.size();i++)
	{
	    if(types.list.get(i).typeName.equalsIgnoreCase(myTypeName))
	    {
		return i;
	    }
	}
	return 0;
    }
}
