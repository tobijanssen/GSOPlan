/*
 * Stupid.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import de.janssen.android.gsoplan.Convert;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.TimeTableIndex;
import de.janssen.android.gsoplan.xml.Xml;
import android.content.Context;


public class Stupid
{
    
    public long syncTime = 0;
    public List<WeekData> stupidData = new ArrayList<WeekData>();
    public Calendar currentDate = new GregorianCalendar();
    public List<TimeTableIndex> myTimetables = new ArrayList<TimeTableIndex>();

    public Stupid()
    {
	int currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);
	// den currentDay auf Montag setzten
	if (currentDayOfWeek < 2)
	{
	    // 1000*60*60*24 = 1 Tag!
	    currentDate.setTimeInMillis(currentDate.getTimeInMillis() + (1000 * 60 * 60 * 24 * (2 - currentDayOfWeek)));
	}
    }
    /**
     * @author Tobias Janssen
     * 
     *         leert die Daten im Speicher
     */
    public void clearData()
    {
	this.stupidData.clear();
    }

    /**
     * @author Tobias Janssen leert die Stundenplan Sttings
     */
    public void clear()
    {
	this.stupidData.clear();
	this.syncTime = 0;
    }


    /**
     * @author Tobias Janssen Sucht den Index im Pager für das angegebene Datum
     *         heraus
     * 
     * @param aquiredDate
     *            Calendar der das zu suchende Datum enthält
     * 
     * @return Liefert den Index der WeekData des angegebenen Datums
     *         <p>
     *         Wenn nicht vorhanden(also bereits im Speicher geladen), wird -1
     *         zurückgeliefert
     */
    public int getIndexOfWeekData(Calendar aquiredDate)
    {
	int weekOfYear = getWeekOfYear(aquiredDate);
	if (myTimetables == null)
	    return -2;
	for (int i = 0; i < myTimetables.size(); i++)
	{
	    if (weekOfYear == myTimetables.get(i).date.get(Calendar.WEEK_OF_YEAR))
	    {
		if (aquiredDate.get(Calendar.YEAR) == myTimetables.get(i).date.get(Calendar.YEAR))
		    return i;
	    }
	}
	return -1;
    }
    
    /**
     * @author janssen Ruft zu dem angegebenen Datum die entsprechende
     *         Kalenderwoche ab
     * 
     * @param aquiredDate
     * @return
     */
    public int getWeekOfYear(Calendar aquiredDate)
    {
	Calendar calcopy = (Calendar) aquiredDate.clone();
	int weekOfYear = 0;
	while (weekOfYear == 0)
	{
	    if (calcopy.get(Calendar.DAY_OF_WEEK) == 6)
		weekOfYear = calcopy.get(Calendar.WEEK_OF_YEAR);
	    else
		calcopy.setTimeInMillis(calcopy.getTimeInMillis() + (86400000 * 1));
	}
	return weekOfYear;
    }

 

    /**
     * Sortiert die WeekData-Liste chronologisch
     * @author janssen
     */
    public void sort()
    {
	List<WeekData> newList = new ArrayList<WeekData>();
	int currentObj = this.stupidData.size() - 1;
	int nextObj = currentObj - 1;
	int yearWeekIdCurrent;
	int yearWeekIdNext;
	while (this.stupidData.size() != 0)
	{
	    // prüfen, ob es ein nächstes Objekt überhaupt noch gibt
	    // das kleinste object heraussuchen
	    currentObj = this.stupidData.size() - 1;
	    nextObj = this.stupidData.size() - 2;

	    for (int i = nextObj; i >= 0; i--)
	    {
		if (this.stupidData.size() == 1)
		{
		    nextObj = -1;
		}
		else
		{
		    // pürfen, ob das nextObj größer ist als das aktuelle
		    yearWeekIdCurrent = Tools.calcIntYearDay(this.stupidData.get(currentObj).date);

		    yearWeekIdNext = Tools.calcIntYearDay(this.stupidData.get(i).date); // Integer.decode(this.stupidData.get(i).weekId);
		    if (yearWeekIdNext > yearWeekIdCurrent)
		    {
			// das nextObj ist größer, daher wird der zeiger nun
			// einen niedriger gesetzt
			nextObj = i;
		    }
		    else
		    {
			// das nextObj ist kleiner, daher nehmen wir nun das
			// Object als current
			currentObj = i;
		    }
		}
	    }

	    // liste ist durch, ablegen
	    yearWeekIdCurrent = Tools.calcIntYearDay(this.stupidData.get(currentObj).date);
	    // yearWeekIdCurrent
	    // =Integer.decode(this.stupidData.get(currentObj).weekId);
	    if (nextObj != -1)
	    {
		// yearWeekIdNext=Integer.decode(this.stupidData.get(nextObj).weekId);
		yearWeekIdNext = Tools.calcIntYearDay(this.stupidData.get(nextObj).date);
		if (yearWeekIdNext > yearWeekIdCurrent)
		{
		    // das nextObj ist größer, daher wird erst das currentObject
		    // abgelegt
		    newList.add(this.stupidData.get(currentObj));
		    this.stupidData.remove(currentObj);

		}
		else
		{
		    // das nextObj ist kleiner, daher wird erst das nextObj
		    // abgelegt
		    newList.add(this.stupidData.get(nextObj));
		    this.stupidData.remove(nextObj);
		}
	    }
	    else
	    {
		newList.add(this.stupidData.get(currentObj));
		this.stupidData.remove(currentObj);
	    }

	}

	this.stupidData = newList;

    }

    

    /**
     * Indexiert die WeekData Liste.
     * Anhand des Schlüssels kann dann der richtige Datensatz leichter aus dem
     * Bestand abgerufen werden
     *         
     * @author Tobias Janssen 
     * @throws Exception
     */
    public void timeTableIndexer() throws Exception
    {

	myTimetables.clear();

	// den gesamten geladenen Datenbestand durchsuchen
	for (int i = 0; i < this.stupidData.size(); i++)
	{
	    myTimetables.add(new TimeTableIndex(i,this.stupidData.get(i).date, this.stupidData.get(i).syncTime));
	}
    }

    /**
     * Generiert ein SaveData Object, das dann ausgeführt werden kann
     * @author Tobias Janssen 
     * 
     * @param ctxt
     * @param weekData
     * @return
     */
    public File getFileSaveData(Context ctxt, WeekData weekData)
    {

	String filename = getWeekOfYearToDisplay(weekData.date) + "_" + weekData.date.get(Calendar.YEAR) + "_"
		+ Const.FILEDATA;
	File dir = new File(ctxt.getFilesDir() + "/" + weekData.elementId);
	return new File(dir, filename);
    }
    
    /**
     * Generiert ein File Object, das dann ausgeführt werden kann
     * @author Tobias Janssen 
     * @param cal
     * @param ctxt
     * @param mProfil
     * @return
     */
    public File getFileData(Calendar cal, Context ctxt, Profil mProfil)
    {
	File dir = new File(ctxt.getFilesDir() +"/"+ mProfil.myElement );
	String filename = getWeekOfYearToDisplay(cal) + "_" + cal.get(Calendar.YEAR) + "_"+ Const.FILEDATA;
	return new File(dir, filename);
    }
    
    /**
     * Generiert ein SaveData Object, das dann ausgeführt werden kann
     * @author Tobias Janssen 
     * @param ctxt
     * @param weekData
     * @param mProfil
     * @return
     */
    public File getFileData(Context ctxt, WeekData weekData, Profil mProfil)
    {
	File dir = new File(ctxt.getFilesDir() +"/"+ mProfil.myElement );
	String filename = getWeekOfYearToDisplay(weekData.date) + "_" + weekData.date.get(Calendar.YEAR) + "_"
		+ Const.FILEDATA;
	return new File(dir, filename);
    }
    
    /**
     * Liefert die KalenderWoche des angegebenen Datums zurück
     * @author Tobias Janssen
     * @param date
     * @return
     */
    protected int getWeekOfYearToDisplay(Calendar date)
    {
	Calendar copy = (Calendar) date.clone();
	int currentDay = copy.get(Calendar.DAY_OF_WEEK);
	if (currentDay < 5)
	{
	    copy.setTimeInMillis(date.getTimeInMillis() + (86400000 * (5 - currentDay)));
	}
	else if (currentDay > 5)
	{
	    copy.setTimeInMillis(date.getTimeInMillis() - +(86400000 * (currentDay - 5)));
	}
	int result = 0;
	result = copy.get(Calendar.WEEK_OF_YEAR);
	return result;
    }
    
    /**
     * prüft, ob alle Laufzeitbedürfnisse erfüllt sind
     * @author Tobias Janssen
     * @param ctxt
     * @return			Integer mit dem Fehlercode
     */
    public int checkStructure(MyContext ctxt)
    {
	//typeList laden
	File typesFile = ctxt.mProfil.getTypesFile(ctxt.context);
	if (!typesFile.exists())
	{
	    return 1;
	}
	//die TypeDatei laden
	try
	{
	    Xml xml = new Xml("root", FileOPs.readFromFile(ctxt.logger,typesFile));
	    ctxt.mProfil.types = Convert.toTypesList(xml);
	    
	}
	catch (Exception e)
	{
	    return 1; // Fehler beim Laden der TypeDatei
	}
	if(ctxt.mProfil.myTypeName == null)
	{
	    //return kein typ festgelegt
	    return 3;
	}


	if (ctxt.mProfil.myElement.equalsIgnoreCase("")) // prüfen, ob ein Element ausgewählt wurde
	{
	    return 3;
	}

	// Prüfen, ob der Elementenordner existiert
	File elementDir = new File(ctxt.context.getFilesDir() + "/" + ctxt.mProfil.myElement);
	if (!elementDir.exists())
	    return 6;

	// prüfen, ob daten für die ausgewähltes Element vorhanden sind
	// zählt wie viele Timetables für die ausgewählt Klasse vorhanden sind
	File[] files = elementDir.listFiles();

	if (files.length == 0)
	    return 7;
		
	return 0;
    }
    
    
    /**
     * Lädt alle online verfügbaren aber lokale Daten-Datein
     * @author Tobias Janssen 
     * @param mProfil
     * @param ctxt
     * @throws Exception
     */
    public void loadAllOnlineDataFiles(Profil mProfil, Context ctxt) throws Exception
    {
	try
	{
	    Type myType = mProfil.types.list.get(mProfil.getMyTypeIndex());
	    for(int i=0;i<myType.weekList.size();i++)
	    {
		//String selectedDateIndex = stupid.getIndexOfSelectorValue(stupid.weekList, stupid.weekList.get(i).description);
		//Aus dem stupid.weekList.get(i).description Datumsangabe einen Filename erzeugen
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		cal.setTime(sdf.parse(myType.weekList.get(i).description));// all done
		File comp = getFileData(cal,ctxt,mProfil);
		
		if(comp.exists())
		    Tools.loadNAppendFile(ctxt, this,comp);
	    }
	}
	catch (Exception e)
	{
	    throw e;
	}

    }
}
