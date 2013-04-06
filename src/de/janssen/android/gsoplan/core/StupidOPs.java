/*
 * StupidOps.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan.core;

import java.io.SyncFailedException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.os.Messenger;
import de.janssen.android.gsoplan.ArrayOperations;
import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.HtmlResponse;
import de.janssen.android.gsoplan.dataclasses.Parameter;
import de.janssen.android.gsoplan.dataclasses.SelectOptions;
import de.janssen.android.gsoplan.service.MyService;
import de.janssen.android.gsoplan.xml.Xml;
import de.janssen.android.gsoplan.xml.XmlOPs;
import de.janssen.android.gsoplan.xml.XmlSearch;

public class StupidOPs
{
    /**
     * Lädt die Selectoren(wochen/elemente/typen) von der GSO Seite und parsed diese in den StupidCore
     * @author Tobias Janssen
     * @param logger
     * @param htmlResponse
     * @param mProfil
     * @throws Exception
     */
    public static void syncTypeList(Logger logger, HtmlResponse htmlResponse,Profil mProfil) throws Exception
    {
	Xml xml = new Xml("root");
	HtmlResponse lastResponse = new HtmlResponse();
	try
	{
	    URL url = new URL(Const.NAVBARURL);

	    lastResponse.lastModified = mProfil.types.htmlModDate;
	    
	    XmlOPs.readFromURLIfModified(url, Const.CONNECTIONTIMEOUT,lastResponse,htmlResponse);
	    if(!htmlResponse.dataReceived)
	    {
		logger.log(Logger.Level.INFO_1,"Up2Date! "+ new Date(htmlResponse.lastModified).toString() + " ",true);
		return;
	    
	    }
	    logger.log(Logger.Level.INFO_2,"new Elements downloaded!",true);
	    xml.setDataContent(htmlResponse.xmlContent);
	    mProfil.types.htmlModDate = htmlResponse.lastModified;
	    xml.parseXml();
	    
	}
	catch (Exception e)
	{
	    throw new SyncFailedException(e.getMessage());
	}

	// verfügbare Typen abrufen
	XmlSearch xmlSearch = new XmlSearch();
	Xml searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, "select", new Parameter("name", "type"));
	
	List <SelectOptions> tempTypeList;
	try
	{
	    tempTypeList = getOptionsFromSelectTag(searchResult);
	}
	catch (Exception e)
	{
	    throw e;
	}

	if (tempTypeList == null)
	{
	    throw new Exception("Konnte TypeList nicht extrahieren");
	}
	else
	{
	    Type type;
	    for(int i=0; i< tempTypeList.size();i++)
	    {
		type = new Type();
		if (tempTypeList.get(i).index.equalsIgnoreCase("c"))
		{
		    // verfügbare Klassen abrufen
		    xmlSearch = new XmlSearch();
		    searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.SCRIPT, "var classes"));
		    type.elementList = getOptionsFromJavaScriptArray(searchResult, "classes");
		    type.typeName=tempTypeList.get(i).description;
		    type.type=tempTypeList.get(i).index;
		}
		if (tempTypeList.get(i).index.equalsIgnoreCase("t"))
		{
		    // verfügbare Lehrer abrufen
		    xmlSearch = new XmlSearch();
		    searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.SCRIPT, "var teachers"));
		    type.elementList = getOptionsFromJavaScriptArray(searchResult, "teachers");
		    type.typeName=tempTypeList.get(i).description;
		    type.type=tempTypeList.get(i).index;
		}
		if (tempTypeList.get(i).index.equalsIgnoreCase("r"))
		{
		    // verfügbare Räume abrufen
		    xmlSearch = new XmlSearch();
		    searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.SCRIPT, "var rooms"));
		    type.elementList = getOptionsFromJavaScriptArray(searchResult, "rooms");
		    type.typeName=tempTypeList.get(i).description;
		    type.type=tempTypeList.get(i).index;
		}
		//TODO: nicht einfach adden...erst schauen, ob bereits dieses element existiert!
		//prüfen, ob es dieses type schon gibt
		Type replace = new Type();
		for(int z=0;z<mProfil.types.list.size() && replace.type == null;z++)
		{
		    if(mProfil.types.list.get(z).type.equalsIgnoreCase(type.type))
		    {
			//ja, übereinstimmung gefunden
			replace = mProfil.types.list.get(z);
		    }
		    
		}
		if(replace.type == null)
		{
		    mProfil.types.list.add(type);
		}
		else
		{
		    //vorhandenen Datenbestand ersetzten
		    replace = type;
		}
	    }
	}
	// verfügbare Wochen abrufen
	xmlSearch = new XmlSearch();
	searchResult = xmlSearch.tagCrawlerFindFirstOf(xml, "select", new Parameter("name", "week"));

	try
	{
	    List <SelectOptions> weekList = getOptionsFromSelectTag(searchResult);
	    for(int i=0;i<mProfil.types.list.size();i++)
	    {
		mProfil.types.list.get(i).weekList = weekList;
	    }
	}
	catch (Exception e)
	{
	    throw e;
	}
	mProfil.isDirty=true;


    }
    
    /**
     * Synchronisiert alle verfügbaren WeekDatas im Stupid
     * @param logger
     * @param selectedStringDate
     * @param selectedElement
     * @param myType
     * @param htmlResponse
     * @param stupid
     * @return
     * @throws Exception
     */
    public static List<Point> syncWeekData(Logger logger, String selectedStringDate, String selectedElement,Type myType, HtmlResponse htmlResponse,Stupid stupid) throws Exception
    {
	List<Point> result = new ArrayList<Point>();
	
	int dataIndex = -1;
	String selectedType = myType.type;
	String selectedDateIndex = getIndexOfSelectorValue(myType.weekList, selectedStringDate);
	String selectedClassIndex = getIndexOfSelectorValue(myType.elementList, selectedElement);
	if (selectedClassIndex == "-1" || selectedDateIndex == "-1" || selectedType.equalsIgnoreCase(""))
	{
	    throw new Exception("Fehler bei der URL-Generierung!");
	}
	while (selectedClassIndex.length() < 5)
	{
	    selectedClassIndex = "0" + selectedClassIndex;
	}
	
	
	HtmlResponse lastResponse = new HtmlResponse();
	Xml xml = new Xml("root");
	try
	{
	    //URL setzten
	    URL url = new URL(Const.URLSTUPID + selectedDateIndex + "/" + selectedType + "/" + selectedType + selectedClassIndex + ".htm");
	    
	    //einen künstlichen Response erzeugen, die die alten aus der datei geladenen Daten enthält
	    WeekData wd = new WeekData(null);
	    for(int i=0;i<stupid.stupidData.size();i++)
	    {
		if(stupid.stupidData.get(i).weekId.equalsIgnoreCase(selectedDateIndex))
		{
		    wd=stupid.stupidData.get(i);
		    break;
		}
	    }
	    if(wd.lastHtmlModified != 0)
	    {
		lastResponse.lastModified=wd.lastHtmlModified;
		htmlResponse.alreadyInCache=true;
	    }
	    htmlResponse.dataReceived=false;
	    XmlOPs.readFromURLIfModified(url, Const.CONNECTIONTIMEOUT,lastResponse , htmlResponse);
	    wd.setSyncDate();
	    if(!htmlResponse.dataReceived)
	    {
		logger.log(Logger.Level.INFO_2,"No Data received!");
		return result;
	    
	    }
	    logger.log(Logger.Level.INFO_2,"Week downloaded!");
	    xml.setDataContent(htmlResponse.xmlContent);	    
	    xml.parseXml();

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
	WeekData weekData = new WeekData(stupid);
	weekData = convertXmlTableToWeekData(stupid,xmlTimeTable); // Konvertiert
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
	
	// Die aktuelle Zeit als SyncTime festhalten
	weekData.setSyncDate();
	weekData.addParameter("lastHtmlMod", String.valueOf(htmlResponse.lastModified));

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

	// prüfen, ob bereits die Woche für die Klasse und den typ vorliegt:
	
	List<Point> r = new ArrayList<Point>();
	long existSyncTime = 0;
	WeekData existWeekData = new WeekData(stupid);
	// alle bestehden Daten abrufen:
	for (int y = 0; y < stupid.stupidData.size(); y++)
	{
	    existWeekData = stupid.stupidData.get(y);
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
		    //die alte Woche mit der Neuen vergleichen um über Änderung zu informieren:
		    r = stupid.stupidData.get(y).compare(weekData);
		    stupid.stupidData.set(y, weekData);
		}
	    }
	}
	for(int i=0;i<r.size();i++)
	    result.add(r.get(i));
	    
	weekData.isDirty = true;
	stupid.stupidData.add(weekData); // fügt die geparste Woche den Hauptdaten
				       // hinzu
	stupid.sort();

	for (int y = 0; y < stupid.stupidData.size() && dataIndex == -1; y++)
	{
	    if (stupid.stupidData.get(y).equals(weekData))
		dataIndex = y;
	}
	return result;
    }
    
    /**
     * Sucht den Index aus einem SelectOptionsArray
     * @author Tobias Janssen
     * @param array
     * @param value
     * @return
     */
    public static String getIndexOfSelectorValue(List <SelectOptions> array, String value)
    {
	for (int x = 0; x < array.size(); x++)
	{
	    if (array.get(x).description.equalsIgnoreCase(value))
	    {
		return String.valueOf(array.get(x).index);
	    }
	}
	return "-1";
    }

   
    
    
    
    /**
     * Reduziert ein WeekData Objekt auf deren wichtigen Inhalt
     * @author Tobias Janssen 
     * 
     * @param weekData	WeekData, das reduzuert werden soll
     */
    private static WeekData collapseWeekDataMultiDim(WeekData weekData)
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
    private static Xml SummerizeField(Xml xml, int rndmId, Xml origin, Xml summerizedField)
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
     * @author Tobias Janssen Konvertiert ein XmlTag zu einem mehrdimensionalen
     *         Array
     * 
     * @param htmlTableTag
     *            XmlTag, aus dem das Array erstellt werden soll
     * 
     * @return WeekData ergebnis, des XmlTag
     */
    private static WeekData convertXmlTableToWeekData(Stupid stupid, Xml htmlTableTag)
    {
	// Größe des benötigten Arrays muss kalkuliert werden
	WeekData weekData = new WeekData(stupid);
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
     * @author Tobias Janssen Sucht in dem WeekData den letzten freien Platz
     * 
     * @param weekData
     *            WeekData in dem gesucht werden soll
     * 
     * @return Liefert den Point, der frei ist
     */
    private static Point getLastFreePosition(WeekData weekData)
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
    public static List <SelectOptions> getOptionsFromJavaScriptArray(Xml selectTag, String varName)
    {

	int startIndex = selectTag.getDataContent().indexOf("var " + varName) + ("var " + varName).length();
	startIndex = selectTag.getDataContent().indexOf("[", startIndex) + 1;
	int stopIndex = selectTag.getDataContent().indexOf("]", startIndex);

	String vars = selectTag.getDataContent().substring(startIndex, stopIndex);
	vars = vars.replaceAll("\"", "");
	String[] strgresult = vars.split(",");
	List <SelectOptions> result = new ArrayList<SelectOptions>();
	for (int i = 0; i < strgresult.length; i++)
	{
	    result.add(new SelectOptions(Integer.toString(i + 1),strgresult[i]));
	}

	return result;
    }


    /**
     * @param selectTag
     * @return
     * @throws Exception
     */
    public static List <SelectOptions> getOptionsFromSelectTag(Xml selectTag) throws Exception
    {
	List <SelectOptions> result = new ArrayList<SelectOptions>();
	if (selectTag.getChildTags().length > 0)
	{
	    for (int i = 0; i < selectTag.getChildTags().length; i++)
	    {
		if (selectTag.getChildTagAtIndex(i).getType().equalsIgnoreCase(Xml.OPTION))
		{
		    if (selectTag.getChildTagAtIndex(i).getParameters().length > 0)
		    {
			SelectOptions so = new SelectOptions();
			so.description = selectTag.getChildTagAtIndex(i).getDataContent();
			so.index = selectTag.getChildTagAtIndex(i).getParameterAtIndex(0).getValue();
			result.add(so);
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
     * @author Tobias Janssen Entfernt doppelte Reihen und Spalten
     * 
     * @param weekData
     *            WeekData, das bereinigt werden soll
     * @return WeekData, das bereinigt wurde
     */
    private static WeekData removeDubColsnRows(WeekData weekData)
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
     * @author Tobias Janssen erstellt einen Fingeprint für ein eindimensoinales
     *         Array
     * 
     * @param array
     *            XmlTag-Array von dem der Fingeprint erstellt werden soll
     * 
     * @return ein String, der durch 0 und 1 angibt, ob das element an dieser
     *         Position den vorherigen gleicht
     */
    private static String fingerprintOfDubs(Xml[] array)
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
     * @author Tobias Janssen Prüft und entfernt vollständig leere Zeilen und
     *         Spalten
     * @param weekData
     *            WeekData, das zu bereinigen ist
     * @return WeekData, das bereinigt wurde
     */
    private static WeekData removeEmtyColsnRows(WeekData weekData)
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
     * 
     * @param ctxt
     * @param handler
     */
    public static void contactStupidService(Context ctxt, Handler handler)
    {
	Intent intent = new Intent(ctxt, MyService.class);
	if(handler != null)
	{
	    Messenger messenger = new Messenger(handler);
	    intent.putExtra("MESSENGER", messenger);
	}
	ctxt.startService(intent);
    }
}
