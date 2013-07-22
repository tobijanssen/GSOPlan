package de.janssen.android.gsoplan.dataclasses;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.Convert;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.core.Profil;
import de.janssen.android.gsoplan.xml.Xml;

public class ProfilManager
{
    public List<Profil> profiles = new ArrayList<Profil>();
    public int currentProfilIndex;
    private MyContext ctxt;
    
    public ProfilManager(MyContext ctxt)
    {
	this.ctxt=ctxt;
	inititialize();
    }
    

    
    private void loadAllProfilesFromFile()
    {
	try
	{
	    File dir = new java.io.File(ctxt.context.getFilesDir() + "/");
	    File file = new File(dir, Const.FILEPROFILES);
	    String content = FileOPs.readFromFile(ctxt.logger, file);
	    Xml xml = new Xml("root", content);
	    xml.parseXml();
	    List<Profil> list = Convert.toProfiles(xml, ctxt);
	    
	    for(int i=0;i<list.size();i++)
		profiles.add(list.get(i));
	}
	catch (Exception e)
	{

	}
	
    }
    
    
    public void inititialize()
    {
	profiles.clear();
	// Das ausgewählte Profil abfragen
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt.context);
	currentProfilIndex = prefs.getInt("profil", 0);
	if(currentProfilIndex == -1)
	    currentProfilIndex=0;
	// Alle Profile aus der Datei lesen
	loadAllProfilesFromFile();
	// Wenn keine gelesen wurden, ein neues leeres hinzufügen
	if (profiles.size() == 0)
	    profiles.add(new Profil(ctxt));
	if (currentProfilIndex > profiles.size() - 1)
	    currentProfilIndex = 0;
    }
    
    /**
     * Speichert das aktuelle Profil
     * @param ctxt
     */
    public void saveAllProfiles()
    {
	try
	{
	    File dir = new java.io.File(ctxt.context.getFilesDir() + "/");
	    File file = new File(dir, Const.FILEPROFILES);
	    String xmlContent = Convert.toXml(profiles);
	    FileOPs.saveToFile(xmlContent, file);
	}
	catch (Exception e)
	{
	    
	}
    }
    
    public void applyProfilIndex()
    {
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt.context);
	SharedPreferences.Editor edit = prefs.edit();
	edit.putInt("profil", currentProfilIndex);
	edit.apply();
	profiles.get(currentProfilIndex).setPrefs();	//setzt dieses Profil als App-Profil
    }
}
