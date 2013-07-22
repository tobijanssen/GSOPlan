/*
 * Tools.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import de.janssen.android.gsoplan.Convert;
import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.activities.AppPreferences;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.xml.Xml;
import de.janssen.android.gsoplan.xml.XmlSearch;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;

public class Tools
{
    
    /**
     * @author Tobias Janssen Prüft anhand einer Datei, welche Version zuvor
     *         installiert war. Liefert false, wenn Versionen übereinstimmen und
     *         true wenn abweichung
     * 
     * @param ctxt
     * @return
     * @throws Exception
     */
    public static Boolean isNewVersion(MyContext ctxt) throws Exception
    {
	// App Version abfragen
	Context cont = ctxt.context.getApplicationContext();
	PackageInfo pInfo = cont.getPackageManager().getPackageInfo(cont.getPackageName(), 0);
	String currentVersion = pInfo.versionName;

	// zuerst prüfen, ob versionsdatei vorhanden
	String filename = Const.FILEVERSION;
	File vFile = new File(ctxt.context.getFilesDir(), filename);
	if (!vFile.exists())
	{
	    // Datei existiert nicht!
	    // Neu anlegen

	    String fileContent = "<version>" + currentVersion + "</version>";
	    FileOPs.saveToFile(fileContent, vFile);
	    return true;
	}
	else
	{
	    Xml xml = new Xml("root", FileOPs.readFromFile(ctxt.logger,vFile));
	    xml.parseXml();
	    XmlSearch xmlSearch = new XmlSearch();
	    Xml versionTag = xmlSearch.tagCrawlerFindFirstEntryOf(xml, "version");
	    if (versionTag != null && versionTag.getDataContent() != null)
	    {
		if (currentVersion.equalsIgnoreCase(versionTag.getDataContent()))
		    return false;
		else
		{
		    String fileContent = "<version>" + currentVersion + "</version>";
		    FileOPs.saveToFile(fileContent, vFile);
		    return true;
		}

	    }
	    else
	    {
		String fileContent = "<version>" + currentVersion + "</version>";
		FileOPs.saveToFile(fileContent, vFile);
		return true;
	    }
	}

    }



    

    /**
     * @author Tobias Janssen
     * 
     *         Liefert den aktuellen Wochentag. Wochenendtage liefern den
     *         nächsten Montag und setzen das currentDate entsprechend um
     * 
     * @param date
     * @return
     */
    @Deprecated
    public static int getSetCurrentWeekDay(Calendar date)
    {
	int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
	switch (dayOfWeek)
	{
	case Calendar.SATURDAY:
	    date.setTimeInMillis(date.getTimeInMillis() + (86400000 * 2));
	    return Calendar.MONDAY;

	case Calendar.SUNDAY:
	    date.setTimeInMillis(date.getTimeInMillis() + (86400000 * 1));
	    return Calendar.MONDAY;

	default:
	    return dayOfWeek;

	}
    }



    

    /**
     * @author Tobias Janssen
     * 
     *         Rechnet das Datum einer PageIndex zu einem Vergleichbaren Wert
     * 
     * @param calendar
     * @return
     */
    public static int calcIntYearDay(Calendar calendar)
    {
	return (calendar.get(Calendar.YEAR) * 1000) + calendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * @author Tobias Janssen
     * 
     *         Rechnet das Datum einer PageIndex zu einem Vergleichbaren Wert
     * 
     * @param calendar
     * @return
     */
    public static int calcIntYearWeek(Calendar calendar)
    {
	return (calendar.get(Calendar.YEAR) * 1000) + calendar.get(Calendar.WEEK_OF_YEAR);
    }

    



    /**
     * @author Tobias Janssen Lädt alle verfügbaren Daten-Datein
     * 
     * @param context
     * @param stupid
     * @throws Exception
     */
    public static void loadAllDataFiles(Context context, Profil mProfil,Stupid stupid) throws Exception
    {
	try
	{
	    File dir = new File(context.getFilesDir() + "/" + mProfil.myElement);
	    File[] files = dir.listFiles();
	    int start = 0;
	    if(mProfil.fastLoad)
	    {
		try
		{
		    // Sortieren, damit die ältetsten abgeschnitten werden
		    files = sortFiles(files);

		    // maximal 8 Dateien(Wochen) laden
		    if (files.length > 8)
			start = files.length - 8;
		}
		catch (Exception e)
		{
		    // Sortieren hat nicht geklappt
		    start = 0;
		}
	    }
	    for (int f = start; f < files.length; f++)
	    {
		loadNAppendFile(context, stupid, files[f]);
	    }
	}
	catch (Exception e)
	{
	    throw e;
	}

    }
    
    private static File[] sortFiles(File[] files)
    {
	List<File> listOut = new ArrayList<File>();
	List<File> listIn = new ArrayList<File>();
	for(int i=0;i<files.length;i++)
	{
	    listIn.add(files[i]);
	}
	if(listIn.size() == 0)
	    return new File[0];

	int location = 0;
	File current = listIn.get(location);
	File next;
	int indexToRemove = 0;
	while(listIn.size() != 0)
	{
	    
	    while (location != listIn.size() - 1)
	    {
		location++;
		next = listIn.get(location);
		if (listIn.size() == 1)
		    listOut.add(next);
		else
		{
		    String[] file1 = current.getName().split("_");
		    String[] file2 = next.getName().split("_");
		    if (Integer.parseInt(file1[1]) > Integer.parseInt(file2[1]))
		    {
			current = next;
			indexToRemove = location;
		    }
		    else if(Integer.parseInt(file1[1]) == Integer.parseInt(file2[1]))
		    {
			if(Integer.parseInt(file1[0]) > Integer.parseInt(file2[0]))
			{
			    current = next;
			    indexToRemove = location;
			}
		    }
			
		}
	    }
	    listOut.add(current);
	    listIn.remove(indexToRemove);
	    location = 0;
	    indexToRemove = location;
	    if(listIn.size() > 0)
		current = listIn.get(location);
	    
	    
	}
	File[] result = new File[listOut.size()];
	result = listOut.toArray(result);
	return result;
    }
    


    /**
     * @author Tobias Janssen
     * 
     *         Lädt den angegebenen File und hängt diesen an die Daten im
     *         StupidCore an
     * 
     * @param context
     * @param stupid
     * @param file
     * @throws Exception
     */
    public static void loadNAppendFile(Context ctxt, Stupid stupid, File file) throws Exception
    {

	try
	{
	    WeekData[] weekData = Convert.toWeekDataArray(FileOPs.readFromFile(new Logger(ctxt),file));
	    if (weekData.length > 0)
		stupid.stupidData.add(weekData[0]);
	}
	catch (Exception e)
	{
	    throw new Exception("Beim laden der Dateien ist ein Fehler aufgetreten");
	}
    }
    
  
    /**
     * 
     * @author Tobias Janssen
     * @param ctxt
     * @return
     */
    public static boolean gotoSetup(MyContext ctxt)
    {

	Intent intent = new Intent(ctxt.activity, AppPreferences.class);
	ctxt.activity.startActivityForResult(intent, 0);
	return true;
    }

    /**
     * @author Tobias Janssen
     * @param ctxt
     * @param putExtraName
     * @param value
     * @return
     */
    public static boolean gotoSetup(MyContext ctxt, String putExtraName, Boolean value)
    {

	Intent intent = new Intent(ctxt.activity, AppPreferences.class);
	intent.putExtra(putExtraName, value);
	ctxt.activity.startActivityForResult(intent, 0);
	return true;
    }

}
