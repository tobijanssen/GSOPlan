/*
 * Stupid.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import de.janssen.android.gsoplan.ArrayOperations;
import de.janssen.android.gsoplan.Const;
import de.janssen.android.gsoplan.DownloadFeedback;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.asyncTasks.Download;
import de.janssen.android.gsoplan.asyncTasks.MainDownloader;
import de.janssen.android.gsoplan.asyncTasks.SaveData;
import de.janssen.android.gsoplan.asyncTasks.SaveElement;
import de.janssen.android.gsoplan.asyncTasks.SaveProfil;
import de.janssen.android.gsoplan.runnables.ErrorMessage;
import de.janssen.android.gsoplan.runnables.RefreshPager;
import de.janssen.android.gsoplan.xml.XmlOPs;
import de.janssen.android.gsoplan.xml.Xml;
import de.janssen.android.gsoplan.xml.XmlSearch;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.widget.Toast;

public class Stupid
{
    public SelectOptions[] elementList = new SelectOptions[0];
    public SelectOptions[] typeList = new SelectOptions[0];
    public SelectOptions[] weekList = new SelectOptions[0];
    private String myElement = "";
    private int myType = 0;
    public Boolean onlyWlan = false;
    public Boolean hideEmptyHours = true;
    public long syncTime = 0;
    public List<WeekData> stupidData = new ArrayList<WeekData>();
    private Boolean isDirty = false;
    public final String[] timeslots = new String[] { "", "7.45 - 8.30", "8.30 - 9.15", "9.35 - 10.20", "10.20 - 11.05",
	    "11.25 - 12.10", "12.10 - 12.55", "13.15 - 14.00", "14.00 - 14.45", "15.05 - 15.50", "15.50 - 16.35",
	    "16.55 - 17.40", "17.40 - 18.25", "18.25 - 19.10", "19.30 - 20.15", "20.15 - 21.00" };
    private long myResync = 10;
    public Calendar currentDate = new GregorianCalendar();
    public TimeTableIndex[] myTimetables;

    final String NAVBARURL = "http://stupid.gso-koeln.de/frames/navbar.htm";
    final String URLSTUPID = "http://stupid.gso-koeln.de/";

    /**
     * Setzt isDirty auf den übergebenen Parameter
     * 
     * @param arg
     */
    public void setIsDirty(Boolean arg)
    {
	this.isDirty=arg;
    }
    
    /**
     * @return Long
     */
    public long getMyResync()
    {
	return this.myResync;
    }

    /**
     * @param value
     * @throws Exception
     */
    public void setMyResync(long value) throws Exception
    {
	if (value < java.lang.Long.MAX_VALUE && value >= 1)
	{
	    this.myResync = value;
	    return;
	}
	throw new Exception("ResyncAfter ist ungültig");
    }

    /**
     * @return
     * 
     */
    public int getMyType()
    {
	return this.myType;
    }

    /**
     * @param value
     * @throws Exception
     */
    public void setMyType(int value) throws Exception
    {
	if (value < this.typeList.length && value >= 0)
	{
	    this.myType = value;
	    return;
	}
	throw new Exception("Type ist ungültig");
    }

    /**
     * @param value
     */
    public void setMyType(String value)
    {
	for (int i = 0; i < typeList.length; i++)
	{
	    if (typeList[i].description.equals(value))
	    {
		this.myType = i;
		break;
	    }
	}

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
     * @param value
     * @throws Exception
     */
    public void setMyElementValid(String value) throws Exception
    {
	// prüft, ob die Value gültig ist
	// dazu muss die elementList durchlaufen werden
	for (int i = 0; i < this.elementList.length; i++)
	{
	    if (value.equals(elementList[i].description))
	    {
		this.myElement = value;
		return;
	    }
	}
	// Element nicht gefunden
	throw new Exception("Element ist ungültig");
    }

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
    public void clearElements()
    {
	this.elementList = new SelectOptions[0];
	this.typeList = new SelectOptions[0];
	this.weekList = new SelectOptions[0];
	this.myElement = "";
	this.myType = 0;
	this.onlyWlan = false;
    }

    /**
     * @author Tobias Janssen Reduziert das Mehrdimensionale Array auf deren
     *         Wichtigen Inhalt
     * 
     * @param weekData
     *            WeekData, das reduzuert werden soll
     */
    private WeekData collapseWeekDataMultiDim(WeekData weekData)
    {
	// in das erste Feld gehen
	Xml sourceField;

	// entfernt alle Doppel-Zeilen und Spalten, die durch spans entstanden
	// sind
	weekData = removeDubColsnRows(weekData);

	// nun alle felder durchlaufen und die XML tag zusammen summieren
	for (int y = 0; y < weekData.timetable.length; y++)
	{
	    for (int x = 0; x < weekData.timetable[y].length; x++)
	    {
		sourceField = weekData.timetable[y][x];
		// eine zufalls id für dieses feld vergeben, dadurch werden alle
		// schon besuchten tags markiert. jedoch nur für diesen suchlauf
		int rndmId = new java.util.Random().nextInt();
		Xml resultField = new Xml("result");
		weekData.timetable[y][x] = SummerizeField(sourceField, rndmId, sourceField, resultField);
	    }
	}
	// nun noch alle leeren Zeilen und Spalten entfernen, falls vorhanden
	weekData = removeEmtyColsnRows(weekData);

	return weekData;
    }

    /**
     * @author Tobias Janssen Konvertiert ein XmlTag zu einem mehrdimensionalen
     *         Array und steuert dabei einen progress dialog an
     * 
     * @param htmlTableTag
     *            XmlTag, aus dem das Array erstellt werden soll
     * @param pd
     *            ProgressDialog, der Angesteuert werden soll
     * 
     * @return WeekData ergebnis, des XmlTag
     */
    private WeekData convertXmlTableToWeekData(Xml htmlTableTag, ProgressDialog pd)
    {
	WeekData weekData = new WeekData(this);
	weekData.setSyncDate();
	weekData.timetable = new Xml[0][0];

	// das Xml Tag heraussuchen, in dem "Montag" steht
	XmlSearch xmlSearch = new XmlSearch();
	Xml position = xmlSearch.tagCrawlerFindFirstOf(htmlTableTag, new Xml(Xml.UNSET, "Montag"));
	Xml tuesday;
	Xml table;
	do
	{
	    xmlSearch = new XmlSearch();
	    table = xmlSearch.tagCrawlerFindFirstOf(position, new Xml(Xml.TABLE), true);

	    xmlSearch = new XmlSearch();
	    tuesday = xmlSearch.tagCrawlerFindFirstOf(table, new Xml(Xml.UNSET, "Dienstag"));
	    if (tuesday == null || tuesday.getType() == null)
		position = table.getParentTag();
	} while ((tuesday == null || tuesday.getType() == null)
		&& (position != null && position.getParentTag() != null));

	xmlSearch = new XmlSearch();
	Xml tr = xmlSearch.tagCrawlerFindFirstEntryOf(table, Xml.TR);
	int rows = tr.getParentTag().getChildTags().length;
	int cols = 0;
	Boolean colSpanFound = false;
	// jedes td prüfen:
	for (int i = 0; i < tr.getChildTags().length; i++)
	{
	    pd.incrementProgressBy(50);
	    // auf parameter prüfen
	    if (tr.getChildTagAtIndex(i).getParameters().length > 0)
	    {
		colSpanFound = false;
		// jeden parameter überprüfen:
		for (int parIndex = 0; parIndex < tr.getChildTagAtIndex(i).getParameters().length; parIndex++)
		{
		    if (tr.getChildTagAtIndex(i).getParameterAtIndex(parIndex).getName().equalsIgnoreCase("colspan"))
		    {
			String value = tr.getChildTagAtIndex(i).getParameterAtIndex(parIndex).getValue();
			int num = java.lang.Integer.parseInt(value);
			cols += num;
			colSpanFound = true;
		    }
		}
		if (!colSpanFound)
		{
		    cols++;
		}
	    }
	    else
	    {
		// kein colspan vorhanden, daher nur eine col dazuzählen:
		cols++;
	    }

	}
	weekData.timetable = new Xml[rows][cols];

	// die tabelle erstellen
	for (int y = 0; y < tr.getParentTag().getChildTags().length; y++)
	{
	    pd.incrementProgressBy(50);

	    Xml[] td = tr.getParentTag().getChildTagAtIndex(y).getChildTags();

	    Point insertPoint;
	    // jedes td prüfen:
	    for (int i = 0; i < td.length; i++)
	    {
		insertPoint = getLastFreePosition(weekData);
		// auf parameter prüfen
		if (td[i].getParameters().length > 0)
		{
		    int colspan = 0;
		    int rowspan = 0;
		    // jeden parameter überprüfen:
		    for (int parIndex = 0; parIndex < td[i].getParameters().length; parIndex++)
		    {
			if (td[i].getParameterAtIndex(parIndex).getName().equalsIgnoreCase("colspan"))
			{
			    String value = td[i].getParameterAtIndex(parIndex).getValue();
			    colspan = java.lang.Integer.parseInt(value);
			}
			if (td[i].getParameterAtIndex(parIndex).getName().equalsIgnoreCase("rowspan"))
			{
			    String value = td[i].getParameterAtIndex(parIndex).getValue();
			    rowspan = Integer.parseInt(value);
			}
		    }

		    if (rowspan > 0 || colspan > 0)
		    {
			int col = 0;
			do
			{
			    insertPoint = getLastFreePosition(weekData);
			    if (rowspan > 0)
			    {
				for (int row = 0; row < rowspan; row++)
				{
				    weekData.timetable[insertPoint.y + row][insertPoint.x] = td[i];
				}
			    }
			    else
			    {
				weekData.timetable[insertPoint.y][insertPoint.x] = td[i];
			    }
			    col++;
			} while (col < colspan);
		    }
		    else
		    {
			weekData.timetable[insertPoint.y][insertPoint.x] = td[i];
		    }
		}
		else
		{
		    weekData.timetable[insertPoint.y][insertPoint.x] = td[i];
		}

	    }
	}

	return weekData;
    }

    /**
     * @author Tobias Janssen Konvertiert ein XmlTag zu einem mehrdimensionalen
     *         Array
     * 
     * @param htmlTableTag
     *            XmlTag, aus dem das Array erstellt werden soll
     * 
     * @return WeekData ergebnis, des XmlTag
     */
    private WeekData convertXmlTableToWeekData(Xml htmlTableTag)
    {
	// Größe des benötigten Arrays muss kalkuliert werden
	WeekData weekData = new WeekData(this);
	weekData.setSyncDate();
	weekData.timetable = new Xml[0][0];

	// das Xml Tag heraussuchen, in dem "Montag" steht
	XmlSearch xmlSearch = new XmlSearch();
	Xml position = xmlSearch.tagCrawlerFindFirstOf(htmlTableTag, new Xml(Xml.UNSET, "Montag"));
	Xml tuesday;
	Xml table;
	do
	{
	    xmlSearch = new XmlSearch();
	    table = xmlSearch.tagCrawlerFindFirstOf(position, new Xml(Xml.TABLE), true);

	    xmlSearch = new XmlSearch();
	    tuesday = xmlSearch.tagCrawlerFindFirstOf(table, new Xml(Xml.UNSET, "Dienstag"));
	    if (tuesday == null || tuesday.getType() == null)
		position = table.getParentTag();
	} while ((tuesday == null || tuesday.getType() == null)
		&& (position != null && position.getParentTag() != null));

	xmlSearch = new XmlSearch();
	Xml tr = xmlSearch.tagCrawlerFindFirstEntryOf(table, Xml.TR);
	int rows = tr.getParentTag().getChildTags().length;
	int cols = 0;
	Boolean colSpanFound = false;
	// jedes td prüfen:
	for (int i = 0; i < tr.getChildTags().length; i++)
	{
	    // auf parameter prüfen
	    if (tr.getChildTagAtIndex(i).getParameters().length > 0)
	    {
		colSpanFound = false;
		// jeden parameter überprüfen:
		for (int parIndex = 0; parIndex < tr.getChildTagAtIndex(i).getParameters().length; parIndex++)
		{
		    if (tr.getChildTagAtIndex(i).getParameterAtIndex(parIndex).getName().equalsIgnoreCase("colspan"))
		    {
			String value = tr.getChildTagAtIndex(i).getParameterAtIndex(parIndex).getValue();
			int num = java.lang.Integer.parseInt(value);
			cols += num;
			colSpanFound = true;
		    }
		}
		if (!colSpanFound)
		{
		    cols++;
		}
	    }
	    else
	    {
		// kein colspan vorhanden, daher nur eine col dazuzählen:
		cols++;
	    }

	}
	weekData.timetable = new Xml[rows][cols];

	// die tabelle erstellen
	for (int y = 0; y < tr.getParentTag().getChildTags().length; y++)
	{
	    Xml[] td = tr.getParentTag().getChildTagAtIndex(y).getChildTags();

	    Point insertPoint;
	    // jedes td prüfen:
	    for (int i = 0; i < td.length; i++)
	    {
		insertPoint = getLastFreePosition(weekData);
		// auf parameter prüfen
		if (td[i].getParameters().length > 0)
		{
		    int colspan = 0;
		    int rowspan = 0;
		    // jeden parameter überprüfen:
		    for (int parIndex = 0; parIndex < td[i].getParameters().length; parIndex++)
		    {
			if (td[i].getParameterAtIndex(parIndex).getName().equalsIgnoreCase("colspan"))
			{
			    String value = td[i].getParameterAtIndex(parIndex).getValue();
			    colspan = java.lang.Integer.parseInt(value);
			}
			if (td[i].getParameterAtIndex(parIndex).getName().equalsIgnoreCase("rowspan"))
			{
			    String value = td[i].getParameterAtIndex(parIndex).getValue();
			    rowspan = Integer.parseInt(value);
			}
		    }

		    if (rowspan > 0 || colspan > 0)
		    {
			int col = 0;
			do
			{
			    insertPoint = getLastFreePosition(weekData);
			    if (rowspan > 0)
			    {
				for (int row = 0; row < rowspan; row++)
				{
				    weekData.timetable[insertPoint.y + row][insertPoint.x] = td[i];
				}
			    }
			    else
			    {
				weekData.timetable[insertPoint.y][insertPoint.x] = td[i];
			    }
			    col++;
			} while (col < colspan);
		    }
		    else
		    {
			weekData.timetable[insertPoint.y][insertPoint.x] = td[i];
		    }
		}
		else
		{
		    weekData.timetable[insertPoint.y][insertPoint.x] = td[i];
		}

	    }
	}

	return weekData;
    }

    /**
     * @author Tobias Janssen
     * 
     *         Lädt die Selectoren von der GSO Seite und parsed diese in den
     *         StupidCore
     * 
     * @param ctxt
     *            MyContext der Applikation
     */
    public void fetchSelectorsFromNet(MyContext ctxt) throws Exception
    {
	Xml xml = new Xml("root");
	try
	{
	    URL url = new URL(NAVBARURL);
	    xml.setDataContent((XmlOPs.readFromURL(url, ctxt.progressDialog, Const.CONNECTIONTIMEOUT)));
	    xml.parseXml();
	}
	catch (Exception e)
	{
	    throw e;
	}

	// verfügbare Wochen abrufen
	XmlSearch xmlSearch = new XmlSearch();
	Xml searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, "select", new Parameter("name", "week"));

	try
	{
	    weekList = getOptionsFromSelectTag(searchResult);
	}
	catch (Exception e)
	{
	    throw e;
	}

	// verfügbare Typen abrufen
	xmlSearch = new XmlSearch();
	searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, "select", new Parameter("name", "type"));
	try
	{
	    typeList = getOptionsFromSelectTag(searchResult);
	}
	catch (Exception e)
	{
	    throw e;
	}

	if (typeList.length == 0)
	{
	    throw new Exception("Konnte TypeList nicht extrahieren");
	}
	else
	{

	    if (typeList[this.myType].index.equalsIgnoreCase("c"))
	    {
		// verfügbare Klassen abrufen
		xmlSearch = new XmlSearch();
		searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.SCRIPT, "var classes"));
		elementList = GetOptionsFromJavaScriptArray(searchResult, "classes");
	    }
	    if (typeList[this.myType].index.equalsIgnoreCase("t"))
	    {
		// verfügbare Lehrer abrufen
		xmlSearch = new XmlSearch();
		searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.SCRIPT, "var teachers"));
		elementList = GetOptionsFromJavaScriptArray(searchResult, "teachers");
	    }
	    if (typeList[this.myType].index.equalsIgnoreCase("r"))
	    {
		// verfügbare Räume abrufen
		xmlSearch = new XmlSearch();
		searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.SCRIPT, "var rooms"));
		elementList = GetOptionsFromJavaScriptArray(searchResult, "rooms");
	    }
	}

	ctxt.progressDialog.incrementProgressBy(500);

	this.isDirty = true;

	Date date = new Date();
	syncTime = date.getTime();
	ctxt.progressDialog.incrementProgressBy(500);
    }

    /**
     * @author Tobias Janssen Liest die Elemente aus dem Xml, und fügt diese dem
     *         Stupid-Objekt hinzu
     * 
     * @param xml
     *            Xml das die Elemente enthält
     * 
     * @param ctxt
     *            MyContext der Applikation
     * 
     * @throws Exception
     *             Wenn xml üngültig ist
     */
    public void fetchElementsFromXml(Xml xml, MyContext ctxt) throws Exception
    {
	xml.parseXml();

	XmlSearch xmlSearch = new XmlSearch();
	Xml elements = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.ELEMENTS));
	xmlSearch = new XmlSearch();
	Xml weeks = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.WEEKID));
	xmlSearch = new XmlSearch();
	Xml time = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.SYNCTIME));
	xmlSearch = new XmlSearch();
	Xml types = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.TYPES));
	
	if (time.getDataContent() != null)
	    syncTime = Long.parseLong(time.getDataContent());

	if (elements.getType() == null)
	{
	    throw new Exception(
		    "Fehler beim Konvertieren der XML Elemente aus der Datendatei. Details:\nDer Element XmlTag konnte nicht gefunden werden!");
	}
	if (weeks.getType() == null)
	{
	    throw new Exception(
		    "Fehler beim Konvertieren der XML Wochen aus der Datendatei. Details:\nDer Wochen XmlTag konnte nicht gefunden werden!");
	}
	if (types.getType() == null)
	{
	    throw new Exception(
		    "Fehler beim Konvertieren der XML Types aus der Datendatei. Details:\nDer Types XmlTag konnte nicht gefunden werden!");
	}

	try
	{
	    if (elements.getType().equalsIgnoreCase(Xml.ELEMENTS))
	    {
		for (int i = 0; i < elements.getParameters().length; i++)
		{
		    SelectOptions option = new SelectOptions();
		    option.description = elements.getParameterAtIndex(i).getName();
		    option.index = elements.getParameterAtIndex(i).getValue();

		    elementList = (SelectOptions[]) ArrayOperations.AppendToArray(elementList, option);
		}
	    }
	    else
		throw new Exception(
			"Fehler beim Erstellen der Optionen. Details:\nDer Elements XmlTag ist falsch definiert!");
	    if (weeks.getType().equalsIgnoreCase(Xml.WEEKID))
	    {
		for (int i = 0; i < weeks.getParameters().length; i++)
		{
		    SelectOptions option = new SelectOptions();
		    option.description = weeks.getParameterAtIndex(i).getName();
		    option.index = weeks.getParameterAtIndex(i).getValue();

		    weekList = (SelectOptions[]) ArrayOperations.AppendToArray(weekList, option);
		}
	    }
	    else
		throw new Exception(
			"Fehler beim Erstellen der verfügbaren Wochen:\nDetails:\nDer Wochen XmlTag ist falsch definiert!");
	    if (types.getType().equalsIgnoreCase(Xml.TYPES))
	    {
		for (int i = 0; i < types.getParameters().length; i++)
		{
		    SelectOptions option = new SelectOptions();
		    option.description = types.getParameterAtIndex(i).getName();
		    option.index = types.getParameterAtIndex(i).getValue();

		    typeList = (SelectOptions[]) ArrayOperations.AppendToArray(typeList, option);
		}
	    }
	    else
		throw new Exception(
			"Fehler beim Erstellen der verfügbaren Typen:\nDetails:\nDer Typen XmlTag ist falsch definiert!");
	}
	catch (Exception e)
	{
	    throw new Exception("Fehler beim Erstellen der Elemente");
	}
    }

    /**
     * @author Tobias Janssen Lädt den angegebenen TimeTable von der GSO Seite
     *         und parsed diesen in den StupidCore
     * 
     * @param selectedStringDate	String der das Datum enthält
     * @param selectedElement		String der das Element enthält
     * @param selectedType		String der den Type enthält
     * @param ctxt			MyContext der Applikation
     * @throws Exception		Wenn xml üngültig ist
     */
    public DownloadFeedback fetchTimeTableFromNet(String selectedStringDate, String selectedElement,
	    String selectedType, MyContext ctxt) throws Exception
    {
	int dataIndex = -1;
	String selectedDateIndex = getIndexOfSelectorValue(weekList, selectedStringDate);
	String selectedClassIndex = getIndexOfSelectorValue(elementList, selectedElement);
	ctxt.progressDialog.incrementProgressBy(500);
	while (selectedClassIndex.length() < 5)
	{
	    selectedClassIndex = "0" + selectedClassIndex;
	}
	if (selectedClassIndex == "-1" || selectedDateIndex == "-1" || selectedType.equalsIgnoreCase(""))
	{
	    throw new Exception("Fehler bei der URL-Generierung!");
	}

	Xml xml = new Xml("root");
	try
	{
	    URL url = new URL(URLSTUPID + selectedDateIndex + "/" + selectedType + "/" + selectedType
		    + selectedClassIndex + ".htm");

	    xml.setDataContent(XmlOPs.readFromURL(url, ctxt.progressDialog, Const.CONNECTIONTIMEOUT));
	    xml.parseXml(ctxt.progressDialog);

	}
	catch (Exception e)
	{
	    throw e;
	}
	// TODO: ein ordentliches Abfangen:

	// Herausfiltern des angezeigten Elements aus dem XML Array
	// Leider gibt es nicht viele wiedererkennungswerte des XML Tags
	XmlSearch xmlSearch = new XmlSearch();
	Xml elementSearchResult = xmlSearch.tagCrawlerFindFirstOf(xml, "font", new Parameter("size", "5"));
	String shownElement = elementSearchResult.getDataContent().replaceAll(" ", "");
	shownElement = shownElement.replaceAll("\n", "");
	shownElement = shownElement.replaceAll("&nbsp;", "");

	if (!shownElement.contains(selectedElement))
	{
	    // es kann sein, dass sich dieses Tag nicht mehr mit den angegeben
	    // suchparametern finden lässt("font","size","5")
	    // daher muss nun geprüft werden, ob das gesuchte Element überhaupt
	    // im Quelltext auftritt, also alles durchsuchen
	    try
	    {
		xmlSearch = new XmlSearch();
		elementSearchResult = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.FONT, selectedElement));
		shownElement = elementSearchResult.getDataContent().replaceAll(" ", "");
		shownElement = shownElement.replaceAll("\n", "");
		shownElement = shownElement.replaceAll("&nbsp;", "");
	    }
	    catch (Exception e)
	    {
		throw new Exception(
			"Bei der Konvertierung des Quelltextes ist ein Fehler aufgetreten!\n Versuchen Sie es erneut, oder wenden Sie sich bei erneutem Auftreten an den Entwickler!");
	    }

	    if (!shownElement.contains(selectedElement))
	    {
		// Nein, leider konnte es so auch nicht gefunden werden
		throw new Exception(
			"Bei der Konvertierung des Quelltextes ist ein Fehler aufgetreten!\n Versuchen Sie es erneut, oder wenden Sie sich bei erneutem Auftreten an den Entwickler!");
	    }
	}

	// den Timetable Tag finden
	xmlSearch = new XmlSearch();
	Xml xmlTimeTable = xmlSearch.tagCrawlerFindFirstEntryOf(elementSearchResult.getParentTag(), "table"); // den
													      // HauptStundenplan-Table
													      // abrufen

	// den XmlTimeTable in das WeekData format wandeln:
	WeekData weekData = new WeekData(this);
	weekData = convertXmlTableToWeekData(xmlTimeTable, ctxt.progressDialog); // Konvertiert
										 // den
										 // angegebenen
										 // XmlTable
										 // in
										 // das
										 // WeekData-format.
	weekData = collapseWeekDataMultiDim(weekData); // reduziert den Inhalt
						       // des WeekData.timetable
						       // auf den Kern, es
						       // werden doppelt einträe
						       // entfernt und leere
						       // spalten und zeilen
						       // herausgefiltert
	ctxt.progressDialog.incrementProgressBy(1000);
	// Die aktuelle Zeit als SyncTime festhalten
	weekData.setSyncDate();

	// den Datums-String in ein Kalender Object wandeln

	try
	{
	    // dazu erstmal den string splitten
	    String[] splitDate = selectedStringDate.split("[.]");

	    Calendar cal = new GregorianCalendar();

	    cal.set(Calendar.YEAR, Integer.decode(splitDate[2]));
	    cal.set(Calendar.MONTH, Integer.decode(splitDate[1]) - 1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.decode(splitDate[0]));
	    weekData.date = cal;
	}
	catch (Exception e)
	{
	    throw new Exception("Das Datum konnte nicht geparsed werden");
	}

	weekData.elementId = shownElement;
	weekData.addParameter("classId", shownElement);
	weekData.weekId = selectedDateIndex;
	weekData.addParameter("weekId", selectedDateIndex);
	weekData.typeId = selectedType;
	weekData.addParameter("typeId", selectedType);

	// prüfen, ob bereits die Woche für die Klasse un den typ vorliegt:

	long existSyncTime = 0;
	WeekData existWeekData = new WeekData(this);
	// alle bestehden Daten abrufen:
	for (int y = 0; y < this.stupidData.size(); y++)
	{
	    ctxt.progressDialog.incrementProgressBy(100);
	    existWeekData = this.stupidData.get(y);
	    // prüfen, ob das bestehende Element, dem neu hinzuzufügenden
	    // entspricht(klasse,KW,Typ)
	    if (existWeekData.elementId.equalsIgnoreCase(shownElement)
		    && existWeekData.weekId.equalsIgnoreCase(selectedDateIndex)
		    && existWeekData.typeId.equalsIgnoreCase(selectedType))
	    {
		// ja,es ist eine gleiche Woche bereits vorhanden
		// prüfen, ob die alte syncTime älter ist als die neue
		if (existSyncTime < weekData.syncTime)
		{
		    weekData.isDirty = true;
		    this.stupidData.set(y, weekData);
		    return new DownloadFeedback(y, DownloadFeedback.REFRESH);
		}
	    }
	}
	weekData.isDirty = true;
	this.isDirty=true;
	this.stupidData.add(weekData); // fügt die geparste Woche den Hauptdaten
				       // hinzu
	sort();
	ctxt.progressDialog.incrementProgressBy(100);
	for (int y = 0; y < this.stupidData.size() && dataIndex == -1; y++)
	{
	    if (this.stupidData.get(y).equals(weekData))
		dataIndex = y;
	}
	return new DownloadFeedback(dataIndex, DownloadFeedback.NO_REFRESH);

    }

    /**
     * @author Tobias Janssen erstellt einen Fingeprint für ein eindimensoinales
     *         Array
     * 
     * @param array
     *            XmlTag-Array von dem der Fingeprint erstellt werden soll
     * 
     * @return ein String, der durch 0 und 1 angibt, ob das element an dieser
     *         Position den vorherigen gleicht
     */
    private String fingerprintOfDubs(Xml[] array)
    {
	String fingerprint = "0";
	for (int x = 1; x < array.length; x++)
	{

	    if (array[x].equals(array[x - 1]))
	    {
		fingerprint += "1";
	    }
	    else
	    {
		fingerprint += "0";
	    }
	}
	return fingerprint;
    }

    /**
     * @autor: @author Tobias Janssen Liefert den Index passenend zu der
     *         angegebenen KW aus den Online verfügaberen Wochen zurück
     *         <p>
     *         Wenn online nicht verfügbar, wird -1 zurückgeliefert
     * @param weekOfYear
     * @return int
     */
    @Deprecated
    public int getIndexFromWeekList(String weekOfYear)
    {
	for (int i = 0; i < this.weekList.length; i++)
	{
	    if (weekOfYear.equalsIgnoreCase(this.weekList[i].index))
		return i;
	}
	return -1;
    }

    /**
     * @author Tobias Janssen Sucht den Index aus einem SelectOptionsArray
     * 
     * @param array
     *            SelectOptions-Array in dem nach dem Wert gesucht werden soll
     * 
     * @param value
     *            String nach dem gesucht werden soll
     */
    public String getIndexOfSelectorValue(SelectOptions[] array, String value)
    {
	for (int x = 0; x < array.length; x++)
	{
	    if (array[x].description.equalsIgnoreCase(value))
	    {
		return String.valueOf(array[x].index);
	    }
	}
	return "-1";
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
	for (int i = 0; i < myTimetables.length; i++)
	{
	    if (weekOfYear == myTimetables[i].date.get(Calendar.WEEK_OF_YEAR))
	    {
		if (aquiredDate.get(Calendar.YEAR) == myTimetables[i].date.get(Calendar.YEAR))
		    return i;
	    }
	}
	return -1;
    }

    /**
     * @author Tobias Janssen Sucht in dem WeekData den letzten freien Platz
     * 
     * @param weekData
     *            WeekData in dem gesucht werden soll
     * 
     * @return Liefert den Point, der frei ist
     */
    private Point getLastFreePosition(WeekData weekData)
    {
	Point freeIndexPoint = new Point();
	Boolean success = false;
	for (int y = 0; y < weekData.timetable.length && !success; y++)
	{
	    for (int x = 0; x < weekData.timetable[y].length && !success; x++)
	    {
		if (weekData.timetable[y][x] == null)
		{
		    freeIndexPoint.y = y;
		    freeIndexPoint.x = x;
		    success = true;
		}
	    }
	}
	// TODO: exception wenn kein leerer eintrag gefunden wurde
	return freeIndexPoint;
    }

    /**
     * @param selectTag
     * @param varName
     * @return
     */
    private SelectOptions[] GetOptionsFromJavaScriptArray(Xml selectTag, String varName)
    {

	int startIndex = selectTag.getDataContent().indexOf("var " + varName) + ("var " + varName).length();
	startIndex = selectTag.getDataContent().indexOf("[", startIndex) + 1;
	int stopIndex = selectTag.getDataContent().indexOf("]", startIndex);

	String vars = selectTag.getDataContent().substring(startIndex, stopIndex);
	vars = vars.replaceAll("\"", "");
	String[] strgresult = vars.split(",");
	SelectOptions[] result = new SelectOptions[strgresult.length];
	for (int i = 0; i < strgresult.length; i++)
	{
	    result[i] = new SelectOptions();
	    result[i].index = Integer.toString(i + 1);
	    result[i].description = strgresult[i];
	}

	return result;
    }

    /**
     * @param selectTag
     * @return
     * @throws Exception
     */
    private SelectOptions[] getOptionsFromSelectTag(Xml selectTag) throws Exception
    {
	SelectOptions[] result = new SelectOptions[selectTag.getChildTags().length];
	if (selectTag.getChildTags().length > 0)
	{
	    for (int i = 0; i < selectTag.getChildTags().length; i++)
	    {
		if (selectTag.getChildTagAtIndex(i).getType().equalsIgnoreCase(Xml.OPTION))
		{
		    if (selectTag.getChildTagAtIndex(i).getParameters().length > 0)
		    {
			result[i] = new SelectOptions();
			result[i].description = selectTag.getChildTagAtIndex(i).getDataContent();
			result[i].index = selectTag.getChildTagAtIndex(i).getParameterAtIndex(0).getValue();
		    }
		}
	    }
	}
	else
	{
	    throw new Exception("Keine Elemente im Html gefunden!");
	}
	return result;
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
     * @author Tobias Janssen Entfernt doppelte Reihen und Spalten
     * 
     * @param weekData
     *            WeekData, das bereinigt werden soll
     * @return WeekData, das bereinigt wurde
     */
    private WeekData removeDubColsnRows(WeekData weekData)
    {
	Xml[][] tempTimeTable = new Xml[0][0];

	Boolean dub = false;

	// zuerst alle Zeilen prüfen, ob diese gleich der nächsten ist
	for (int y = 0; y + 1 < weekData.timetable.length; y++)
	{
	    dub = true;
	    for (int x = 0; x < weekData.timetable.length && dub; x++)
	    {
		if (!weekData.timetable[y + 1][x].equals(weekData.timetable[y][x]) && dub)
		{
		    dub = false;
		}
	    }
	    if (!dub)
	    {
		// alle nicht Dublicate werden dem neuen array hinzugefügt
		tempTimeTable = (Xml[][]) ArrayOperations.AppendToArray(tempTimeTable, weekData.timetable[y]);
	    }
	}
	tempTimeTable = (Xml[][]) ArrayOperations.AppendToArray(tempTimeTable,
		weekData.timetable[weekData.timetable.length - 1]);

	// fingerprints(strings aus 0 und 1) für jede zeile erstellen. 1 zeigt,
	// dass dieses feld mit dem vorgänger gleich ist
	String[] print = new String[weekData.timetable.length];
	for (int y = 0; y < weekData.timetable.length; y++)
	{
	    print[y] = fingerprintOfDubs(weekData.timetable[y]);
	}

	// nun müssen die fingerprints aller Array zeilen zusammengefügt werden
	int sum = 0;
	String printRes = "";
	for (int x = 0; x < print.length; x++)
	{
	    sum = 0;
	    for (int y = 0; y < print.length; y++)
	    {
		sum += Integer.decode(String.valueOf(print[y].charAt(x)));
	    }
	    if (sum != 0)
	    {
		printRes += "1";
	    }
	    else
	    {
		printRes += "0";
	    }
	}
	// es ist eine fingerabdruck für ein zweidimensinales array entstanden,
	// an hand diesem kann nun ein neues array erstellt werden, dass keine
	// dublicate hat

	int count = 0;
	// zählen der 0en für die länge einer zeile, denn diese sind kein
	// dublicat.
	for (int x = 0; x < printRes.length(); x++)
	{
	    if (String.valueOf(printRes.charAt(x)).equalsIgnoreCase("0"))
	    {
		count++;
	    }
	}
	// das neue array für das ergebnis erstellen
	weekData.timetable = new Xml[tempTimeTable.length][count];
	Point point = new Point();
	// das vorherige ergenis nutzen wir nun um mit hilfe des fingerprints
	// das neue array zu füllen
	for (int y = 0; y < tempTimeTable.length; y++)
	{
	    for (int x = 0; x < tempTimeTable[y].length; x++)
	    {
		// nur 0en, also nicht dublicate hinzufügen
		if (String.valueOf(printRes.charAt(x)).equalsIgnoreCase("0"))
		{
		    // das feld hinzufügen
		    point = getLastFreePosition(weekData);
		    System.arraycopy(tempTimeTable[y], x, weekData.timetable[point.y], point.x, 1);
		}
	    }

	}

	return weekData;
    }

    /**
     * @author Tobias Janssen Prüft und entfernt vollständig leere Zeilen und
     *         Spalten
     * @param weekData
     *            WeekData, das zu bereinigen ist
     * @return WeekData, das bereinigt wurde
     */
    private WeekData removeEmtyColsnRows(WeekData weekData)
    {
	Xml[][] yResult = new Xml[0][0];

	// erst leere y-zeilen entfernen
	Boolean empty;
	for (int y = 0; y < weekData.timetable.length; y++)
	{
	    empty = true;
	    // alle spalten dieser zeile durchgehen und prüfen, ob alle leer
	    // sind
	    for (int x = 0; x < weekData.timetable[y].length; x++)
	    {
		if (weekData.timetable[y][x].getDataContent() != null && empty == true)
		    empty = false;
	    }

	    // wenn davon eines nicht leer ist
	    if (!empty)
	    {
		// wird diese zeile dem ergebnis angefügt
		yResult = (Xml[][]) ArrayOperations.AppendToArray(yResult, weekData.timetable[y]);
	    }

	}

	Xml[][] xResult = new Xml[yResult.length][yResult[0].length];
	int lengthX;
	// jetzt alle x im yResult prüfen
	for (int x = 0; x < yResult[0].length; x++)
	{
	    empty = true;
	    // alle zeilen dieser zeile durchgehen und prüfen, ob alle leer sind
	    for (int y = 0; y < yResult.length && empty; y++)
	    {
		if (yResult[y][x].getDataContent() != null && empty == true)
		    empty = false;
	    }
	    // wenn davon eines nicht leer ist
	    if (!empty)
	    {
		// hinzufügen
		lengthX = 0;
		Boolean positionFound = false;
		for (int i = 0; i < xResult[0].length && !positionFound; i++)
		{
		    if (xResult[0][i] == null)
		    {
			lengthX = i;
			positionFound = true;
		    }
		}
		for (int y = 0; y < yResult.length; y++)
		{
		    System.arraycopy(yResult[y], x, xResult[y], lengthX, 1);
		}
	    }
	}

	// herausfinden, ob das xResult noch leere Felder hat, wenn ja, wird
	// diese posX zurückgeliefert
	lengthX = xResult[0].length;
	Boolean positionFound = false;
	for (int i = 0; i < xResult[0].length && !positionFound; i++)
	{
	    if (xResult[0][i] == null)
	    {
		lengthX = i;
		positionFound = true;
	    }
	}
	Xml[][] endResult = new Xml[xResult.length][lengthX];
	for (int y = 0; y < xResult.length; y++)
	{
	    System.arraycopy(xResult[y], 0, endResult[y], 0, lengthX);
	}
	// nun noch alle Felder durchlaufen und die dataContent null mit ""
	// ersetzten:
	for (int y = 0; y < endResult.length; y++)
	{
	    for (int x = 0; x < endResult[y].length; x++)
	    {
		if (endResult[y][x].getDataContent() == null)
		{
		    endResult[y][x].setDataContent("");
		}
	    }

	}

	weekData.timetable = endResult;
	return weekData;
    }

    /**
     * 11.10.12 überarbeitet 11.12.12 Tobias Janssen
     * 
     * Sortiert die Daten nach Wochennummer und Jahren
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
     * @author Tobias Janssen Löst Xml alle Child-Vererbungen auf und vereint
     *         diese in ein Xml objekt
     * 
     * @param xml
     *            Xml, mit den zusammenzuführenden Childs
     * @param rndmId
     *            int der die Feld ID angibt
     * @param origin
     * @param summerizedField
     *            Xml mit allen zusammen geführten childs
     * @return
     */
    private Xml SummerizeField(Xml xml, int rndmId, Xml origin, Xml summerizedField)
    {
	XmlSearch xmlSearch = new XmlSearch();
	Xml currentTag = xmlSearch.tagCrawlerFindDeepestUnSumerizedChild(origin, rndmId);

	if (currentTag.getDataContent() != null)
	{
	    if (summerizedField.getDataContent() == null)
	    {
		summerizedField.setDataContent(currentTag.getDataContent());
	    }
	    else
	    {
		summerizedField.setDataContent(summerizedField.getDataContent() + currentTag.getDataContent());
	    }
	}
	// Parameter auslesen
	Boolean redundanz = false;
	for (int p = 0; p < currentTag.getParameters().length; p++)
	{
	    redundanz = false;
	    if (currentTag.getParameterAtIndex(p).getName().equals("color"))
	    {
		for (int i = 0; i < summerizedField.getParameters().length; i++)
		{
		    if (summerizedField.getParameterAtIndex(i).getName()
			    .equalsIgnoreCase(currentTag.getParameterAtIndex(p).getName()))
		    {
			redundanz = true;
		    }
		}
		if (!redundanz)
		    summerizedField.addParameter(currentTag.getParameterAtIndex(p).getName(), currentTag
			    .getParameterAtIndex(p).getValue());
	    }

	}
	currentTag.setRandomId(rndmId);
	// prüfen, ob es noch ein parent tag gibt, und ob dieses nicht dem
	// ursprungs tag entspricht
	if (currentTag.getParentTag() != null && !currentTag.getParentTag().equals(origin))
	    return SummerizeField(currentTag.getParentTag(), rndmId, origin, summerizedField);

	return summerizedField;
    }

    /**
     * @author Tobias Janssen Indexiert die lokal verfügbaren Timetable.
     *         <p>
     *         Anhand des Schlüssels kann dann der richtige Datensatz aus dem
     *         Bestand abgerufen werden
     * 
     * @throws Exception
     */
    public void timeTableIndexer() throws Exception
    {
	// Prüfen, ob ein Element ausgewählt wurde
	// Es ist für den Indexer essentiell wichtig, dass dieser festgelegt
	// ist!
	if (myElement.equalsIgnoreCase(""))
	    throw new Exception("Keine Element festgelegt.Indexer kann nicht gestartet werden!");

	myTimetables = new TimeTableIndex[0];

	// den gesamten geladenen Datenbestand durchsuchen
	for (int i = 0; i < this.stupidData.size(); i++)
	{
	    // prüfen, ob der aktuelle Datensatz dem eigenen Element entspricht
	    if (this.stupidData.get(i).elementId.equalsIgnoreCase(myElement))
	    {

		// wenn ja, einen neuen Eintrag dem Indexer hinzufügen
		myTimetables = (TimeTableIndex[]) ArrayOperations.AppendToArray(myTimetables, new TimeTableIndex(i,
			this.stupidData.get(i).date, this.stupidData.get(i).syncTime));
	    }
	}
    }

    /**
     * 5.10.12 Tobias Janssen
     * 
     * Prüft, ob die eingestellte Woche(laut stupid.currentDate) bereits
     * verfügbar ist unternimmt weitere Maßnahmen, wenn nicht, oder veraltet
     */
    public void checkAvailibilityOfWeek(MyContext ctxt, Boolean forceRefresh, int weekOffset)
    {
	// eine Übersicht erstellen, welche Daten für die aktuelle Klasse
	// überhaupt vorliegen
	try
	{
	    timeTableIndexer();
	}
	catch (Exception e)
	{
	    // Keine Klasse ausgewählt!
	    // disablePagerOnChangedListener=false;
	    Tools.gotoSetup(ctxt);
	}

	// Fehlermeldungen für den Fehlerfall einstellen und den weekOffset ggfl
	// anpassen
	String notAvail = "";
	String loading = "";
	String refreshing = "";
	Boolean smoothScroll = false;
	switch (weekOffset)
	{
	case Const.SELECTEDWEEK:
	    notAvail = ctxt.context.getString(R.string.msg_weekNotAvailable);
	    loading = ctxt.context.getString(R.string.msg_loadingData);
	    refreshing = ctxt.context.getString(R.string.msg_searchingNewDataSelected);
	    smoothScroll = true;
	    weekOffset = Const.THISWEEK;
	    break;
	case Const.NEXTWEEK:
	    notAvail = ctxt.context.getString(R.string.msg_nextWeekNotAvailable);
	    loading = ctxt.context.getString(R.string.msg_loadingData);
	    refreshing = ctxt.context.getString(R.string.msg_searchingNewDataNext);
	    break;
	case Const.LASTWEEK:
	    notAvail = ctxt.context.getString(R.string.msg_foreWeekNotAvailable);
	    loading = ctxt.context.getString(R.string.msg_loadingData);
	    refreshing = ctxt.context.getString(R.string.msg_searchingNewDataLast);
	    break;
	case Const.THISWEEK:
	default:
	    notAvail = ctxt.context.getString(R.string.msg_weekNotAvailable);
	    loading = ctxt.context.getString(R.string.msg_loadingData);
	    refreshing = ctxt.context.getString(R.string.msg_searchingNewDataNow);
	    break;
	}

	Calendar requestedWeek = (Calendar) currentDate.clone();
	requestedWeek.setTimeInMillis(requestedWeek.getTimeInMillis() + (86400000 * 7 * weekOffset));// den
												     // weekOffset
												     // umsetzen

	int currentDay = requestedWeek.get(Calendar.DAY_OF_WEEK);
	if (currentDay != 2)
	{
	    if (currentDay > 2)
		requestedWeek.setTimeInMillis(requestedWeek.getTimeInMillis() - (86400000 * (currentDay - 2)));
	    else if (currentDay < 2)
		requestedWeek.setTimeInMillis(requestedWeek.getTimeInMillis() + (86400000 * (2 - currentDay)));
	}

	// aus dieser Liste mithilfer der selektierten KalenderWoche den
	// richtigen Index heraussuchen
	ctxt.pager.weekDataIndexToShow = getIndexOfWeekData(requestedWeek);
	// prüfen, ob diese Woche bereits im Datenbestand ist
	if (ctxt.pager.weekDataIndexToShow == -1)
	{
	    // Woche ist nicht lokal verfügbar
	    // Downloader starten, dieser prüft, ob diese Woche erhältlich ist
	    // und unternimmt alle weitern Maßnahmen
	    ctxt.executor.execute(new MainDownloader(ctxt, notAvail, requestedWeek, !forceRefresh, loading));
	}
	else if (ctxt.pager.weekDataIndexToShow == -2)
	{
	    ctxt.handler.post(new ErrorMessage(ctxt, Const.ERROR_NOTIMETABLE_FOR_REFRESH));
	}
	else
	{
	    // Woche ist im Datenbestand vorhanden
	    // Nun prüfen, wie alt diese Daten sind:
	    int dateCode = Tools.calcIntYearDay(requestedWeek);
	    Calendar now = new GregorianCalendar();
	    // den now kalender ebenfalls auf montag setzten
	    currentDay = now.get(Calendar.DAY_OF_WEEK);
	    if (currentDay != 2)
	    {
		if (currentDay > 2)
		    now.setTimeInMillis(now.getTimeInMillis() - (86400000 * (currentDay - 2)));
		else if (currentDay < 2)
		    now.setTimeInMillis(now.getTimeInMillis() + (86400000 * (2 - currentDay)));
	    }

	    int nowCode = Tools.calcIntYearDay(now);
	    if (dateCode < nowCode && !forceRefresh)
	    {
		// diese Woche liegt bereits in der vergangenheit und muss nicht
		// aktualisiert werden
		ctxt.handler.post(new RefreshPager(ctxt, smoothScroll));
	    }
	    else
	    {
		Date date = new Date();
		if (myTimetables[ctxt.pager.weekDataIndexToShow].syncTime + (myResync * 60 * 1000) < date
			.getTime() || forceRefresh)
		{
		    // veraltet neu herunterladen
		    ctxt.executor.execute(new MainDownloader(ctxt, notAvail, requestedWeek, !forceRefresh, refreshing));
		}
		else
		{
		    ctxt.handler.post(new RefreshPager(ctxt, smoothScroll));
		}
	    }
	}

    }
    
    /**
     * @author Tobias Janssen
     * 
     *         Prüft, ob die eingestellte Woche(laut stupid.currentDate) bereits
     *         verfügbar ist unternimmt weitere Maßnahmen, wenn nicht, oder
     *         veraltet
     * 
     * @param ctxt
     * @param weekModificator
     */
    public void checkAvailibilityOfWeek(MyContext ctxt, int weekModificator)
    {
	checkAvailibilityOfWeek(ctxt, false, weekModificator);
    }

    
    /**
     * 14.09.12
     * 
     * @author Tobias Janssen
     * 
     *         Speichert das aktuelle StupidCore-Setup ohne ProgressDialog
     * 
     * @param ctxt
     * @param showDialog
     */
    public void saveElements(MyContext ctxt, Boolean showDialog)
    {
	SaveElement saveElement = buildSaveElement(ctxt, showDialog);
	ctxt.executor.execute(saveElement);
    }

    /**
     * 14.09.12
     * 
     * @author Tobias Janssen
     * 
     *         Speichert das aktuelle StupidCore-Setup ohne ProgressDialog
     * 
     * @param ctxt
     * @param postRun
     * @param showDialog
     */
    public void saveElements(MyContext ctxt, Runnable postRun, Boolean showDialog)
    {
	SaveElement saveElement = buildSaveElement(ctxt, postRun, showDialog);
	ctxt.executor.execute(saveElement);
    }
    /**
     * @author Tobias Janssen Generiert ein SaveData Object, das dann ausgeführt
     *         werden kann
     */
    private SaveData buildSaveData(MyContext ctxt, WeekData weekData)
    {
	File file = getFileSaveData(ctxt, weekData);
	return new SaveData(weekData, file, ctxt);
    }
    
    /**
     * @author Tobias Janssen Generiert ein SaveSetup Object, das dann
     *         ausgeführt werden kann
     */
    private SaveElement buildSaveElement(MyContext ctxt, Boolean showDialog)
    {
	File file = getFileSaveElement(ctxt);
	return new SaveElement(ctxt, file, showDialog);
    }

    /**
     * @author Tobias Janssen Generiert ein SaveSetup Object, das dann
     *         ausgeführt werden kann
     */
    private SaveElement buildSaveElement(MyContext ctxt, Runnable postRun, Boolean showDialog)
    {
	File file = getFileSaveElement(ctxt);
	return new SaveElement(ctxt, file, postRun, showDialog);
    }
    
    /**
     * @author Tobias Janssen Generiert ein SaveData Object, das dann ausgeführt
     *         werden kann
     * 
     * @param ctxt
     * @return
     */
    public File getFileSaveElement(MyContext ctxt)
    {
	String filename = ctxt.getSelector()+Const.FILEELEMENT;
	return new File(ctxt.context.getFilesDir(), filename);
    }
    /**
     * @author Tobias Janssen Generiert ein SaveData Object, das dann ausgeführt
     *         werden kann
     * 
     * @param ctxt
     * @param weekData
     * @return
     */
    private File getFileSaveData(MyContext ctxt, WeekData weekData)
    {

	String filename = getWeekOfYearToDisplay(weekData.date) + "_" + weekData.date.get(Calendar.YEAR) + "_"
		+ Const.FILEDATA;
	return new File(ctxt.context.getFilesDir() + "/" + weekData.elementId, filename);
    }
    
    /**
     * @author Tobias Janssen
     * 
     *         Liefert die KalenderWoche des angegebenen Datums zurück
     * 
     * @param date
     * @return
     */
    private int getWeekOfYearToDisplay(Calendar date)
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
     * @author Tobias Janssen
     * 
     * @param ctxt
     * @throws Exception
     */
    public void saveFiles(MyContext ctxt) throws Exception
    {
	saveFiles(ctxt, false);
    }

    /**
     * 
     * @author Tobias Janssen
     * 
     *         Prüft, ob welche Daten im StupidCore dirty sind, und speichert
     *         diese
     * 
     * @param ctxt
     * @param showDialog
     * @throws Exception
     */
    public void saveFiles(MyContext ctxt, Boolean showDialog) throws Exception
    {
	SaveElement saveElements = buildSaveElement(ctxt, showDialog);
	SaveData saveData;
	
	if (this.isDirty)
	{
	    ctxt.executor.execute(saveElements);
	}
	WeekData weekData;
	for (int d = 0; d < stupidData.size(); d++)
	{
	    weekData = stupidData.get(d);
	    if (weekData.isDirty)
	    {
		saveData = buildSaveData(ctxt, weekData);
		ctxt.executor.execute(saveData);
	    }
	}
    }
  
    
    /**
     * @author Tobias Janssen
     * prüft, ob alle Laufzeitbedürfnisse erfüllt sind
     * 
     * @return			Integer mit dem Fehlercode
     */
    public int checkStructure(MyContext ctxt)
    {

	// Prüfen, ob die benötigten Dateien existieren:
	// Elementen Datei beinhaltet die Listen elemente/typ/wochen
	File elementFile = getFileSaveElement(ctxt);
	if (!elementFile.exists())
	{
	    return 1;
	}
	// die ElementDatei Laden
	try
	{
	    
	    Xml xml = new Xml("root", FileOPs.readFromFile(elementFile));
	    clearElements(); // Alle bisherigen Daten entfernen
	    fetchElementsFromXml(xml, ctxt); // Daten aus dem
						    // xml.contaner wandeln
	    ctxt.getPrefs(ctxt.context.getApplicationContext()); // Settings laden
	}
	catch (Exception e)
	{
	    return 1; // Fehler beim Laden der ElementDatei
	}

	if (getMyElement().equalsIgnoreCase("")) // prüfen, ob ein Element ausgewählt wurde
	{
	    return 3;
	}

	// Prüfen, ob der Elementenordner existiert
	File elementDir = new File(ctxt.context.getFilesDir() + "/" + getMyElement());
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
     * 
     * @author Tobias Janssen
     * 
     *         Lädt die Selectoren von der GSO-Seite und parsed diese in die
     *         availableOnline Arrays
     * 
     * @param ctxt
     * @param postRun
     */
    public void fetchOnlineSelectors(MyContext ctxt, Runnable postRun)
    {
	try
	{
	    // prüfen ob Datenübertragung nur über Wlan zulässig ist:
	    if (onlyWlan)
	    {
		// Es dürfen Daten nur bei bestehender Wlan verbindung geladen
		// werden
		// Prüfen, ob Wlan verbindung besteht
		if (ctxt.isWifiConnected())
		{
		    // Verbindung vorhanden
		    Download download = new Download(ctxt, true, false, postRun);
		    ctxt.executor.execute(download);
		}
		else
		{
		    // Keine Wlan Verbindung vorhanden, Fehler-Meldung ausgeben
		    if(ctxt.mIsRunning)
			Toast.makeText(ctxt.context, "Keine Wlan Verbindung!", Toast.LENGTH_SHORT).show();
		}
	    }
	    else
	    {
		// Es dürfen Daten auch ohne Wlan geladen werden
		// ctxt.progressDialog = ProgressDialog.show(ctxt.context,
		// ctxt.context.getString(R.string.setup_message_dlElements_title),
		// ctxt.context.getString(R.string.setup_message_dlElements_body),
		// true,false);
		// stupid.setupIsDirty=true;
		Download download = new Download(ctxt, true, false, postRun);
		ctxt.executor.execute(download);
	    }

	}
	catch (Exception e)
	{
	    if(ctxt.mIsRunning)
	    {
		new AlertDialog.Builder(ctxt.context).setTitle("Fehler")
			.setMessage(ctxt.context.getString(R.string.setup_message_error_dlElements_1))
			.setPositiveButton("OK", new DialogInterface.OnClickListener()
			{
			    public void onClick(DialogInterface dialog, int which)
			    {
				// continue with delete
			    }
			}).setNegativeButton("Abbrechen", new DialogInterface.OnClickListener()
			{
			    public void onClick(DialogInterface dialog, int which)
			    {
				// do nothing
			    }
			}).show();
	    }

	}
    }


}
