/*
 * Xml.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import de.janssen.android.gsoplan.ArrayOperations;
import de.janssen.android.gsoplan.Const;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.core.Parameter;
import de.janssen.android.gsoplan.core.WeekData;

import android.app.ProgressDialog;

public class XmlOPs
{

    public XmlOPs()
    {
    }

    /**
     * Erstellt ein neues Xml Objekt mit Inhalt
     * @author Tobias Janssen 
     * 
     * @param container String mit XML-Inhalt, der verarbeiter werden soll
     */
    public XmlOPs(String container)
    {
	this.container = container;
    }

    public String container = ""; // Enthält den noch zu verarbeitenden XmlText


    private static final String VERSION = "1";

    
    /**
     * Konvertiert die Elemente des StupidCore zu xml
     * @author Tobias Janssen
     * @param ctxt
     * @return String in XML notification
     */
    public static String convertElementsToXml(MyContext ctxt)
    {

	String result = "<" + Xml.SYNCTIME + ">" + ctxt.getCurStupid().syncTime + "</" + Xml.SYNCTIME + ">\n";
	
	result += "<" + Xml.ELEMENTS;
	for (int i = 0; i < ctxt.getCurStupid().elementList.length; i++)
	{
	    result += " " + ctxt.getCurStupid().elementList[i].description + "='" + ctxt.getCurStupid().elementList[i].index + "'";
	}
	result += " />\n";

	result += "<" + Xml.WEEKID;
	for (int i = 0; i < ctxt.getCurStupid().weekList.length; i++)
	{
	    result += " " + ctxt.getCurStupid().weekList[i].description + "='" + ctxt.getCurStupid().weekList[i].index + "'";
	}
	result += " />\n";

	result += "<" + Xml.TYPES;
	for (int i = 0; i < ctxt.getCurStupid().typeList.length; i++)
	{
	    result += " " + ctxt.getCurStupid().typeList[i].description + "='" + ctxt.getCurStupid().typeList[i].index + "'";
	}
	result += " />\n";
	return result;
    }

    
    /**
     * @autor: @author Tobias Janssen
     * Konvertiert die Elemente des StupidCore zu xml und increased den beigefügten ProgressDialog
     * @param ctxt
     * @param pd
     * @return String in XML Notifikation
     */
    public static String convertElementsToXml(MyContext ctxt, ProgressDialog pd)
    {

	String result = "<" + Xml.SYNCTIME + ">" + ctxt.getCurStupid().syncTime + "</" + Xml.SYNCTIME + ">\n";

	
	result += "<" + Xml.ELEMENTS;
	for (int i = 0; i < ctxt.getCurStupid().elementList.length; i++)
	{
	    pd.setProgress(pd.getProgress() + 1);
	    result += " " + ctxt.getCurStupid().elementList[i].description + "='" + ctxt.getCurStupid().elementList[i].index + "'";
	}
	result += " />\n";

	result += "<" + Xml.WEEKID;
	for (int i = 0; i < ctxt.getCurStupid().weekList.length; i++)
	{
	    pd.setProgress(pd.getProgress() + 1);
	    result += " " + ctxt.getCurStupid().weekList[i].description + "='" + ctxt.getCurStupid().weekList[i].index + "'";
	}
	result += " />\n";

	result += "<" + Xml.TYPES;
	for (int i = 0; i < ctxt.getCurStupid().typeList.length; i++)
	{
	    pd.setProgress(pd.getProgress() + 1);
	    result += " " + ctxt.getCurStupid().typeList[i].description + "='" + ctxt.getCurStupid().typeList[i].index + "'";
	}
	result += " />\n";
	return result;
    }

    /**
     * @autor: @author Tobias Janssen
     * Konvertiert die WochenDaten aus dem StupidCore in einen XmlString
     * @param data
     * @param pd
     * @return String in XML Notifikation
     */
    public static String convertWeekDataToXml(WeekData data, ProgressDialog pd)
    {
	int pdvalue = pd.getProgress();
	String result = "<xml version='" + VERSION + "'/>\n";
	pd.setProgress(pdvalue++);

	result += "<week date='" + data.date.get(Calendar.DAY_OF_MONTH) + "." + (data.date.get(Calendar.MONTH) + 1)
		+ "." + data.date.get(Calendar.YEAR) + "'";
	for (int p = 0; p < data.parameters.length; p++)
	{
	    result += " " + data.parameters[p].getName() + "='" + data.parameters[p].getValue() + "'";
	}
	result += ">\n\t<timetable>\n";
	for (int y = 0; y < data.timetable.length; y++)
	{
	    pd.setProgress(pdvalue++);
	    result += "\t\t<row" + y + ">\n";
	    for (int x = 0; x < data.timetable[y].length; x++)
	    {
		pd.setProgress(pdvalue++);
		result += "\t\t\t<day" + x;
		for (int p = 0; p < data.timetable[y][x].getParameters().length; p++)
		{
		    result += " " + data.timetable[y][x].getParameterAtIndex(p).getName() + "='"
			    + data.timetable[y][x].getParameterAtIndex(p).getValue() + "'";
		}
		result += ">" + data.timetable[y][x].getDataContent() + "</day" + x + ">\n";
	    }
	    result += "\t\t</row" + y + ">\n";
	}
	result += "\t</timetable>\n</week>\n";

	return result;
    }

    
    /**
     * 
     * Liest Daten aus der URL aus, erhöht dabei den ProgressDialog
     * @author janssen
     * @param url			URL
     * @param pd			ProgressDialog
     * @param timeoutMillis		int mit angabe des Timeouts in Millis
     * @return				String mit Inhalt der URL
     * @throws Exception		wenn Verbindung fehlgeschlagen ist
     */
    public static String readFromURL(URL url, ProgressDialog pd, int timeoutMillis) throws Exception
    {

	InputStreamReader inStream = null;
	HttpURLConnection conn = null;
	String xmlString = "";
	try
	{
	    conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestProperty("content-type", "text/plain; charset=iso-8859-1");
	    conn.setConnectTimeout(timeoutMillis);
	    conn.connect();
	    inStream = new InputStreamReader(conn.getInputStream(), "iso-8859-1");
	    xmlString = readFromHtmlStream(inStream, pd);
	}
	catch (SocketTimeoutException e)
	{
	    throw new Exception(Const.ERROR_CONNTIMEOUT);

	}
	catch (IOException e)
	{
	    throw new Exception(Const.ERROR_NOSERVER);
	}
	catch (Exception e)
	{
	    throw new Exception(Const.ERROR_NOSERVER);
	}
	finally
	{
	    if (conn != null)
		conn.disconnect();
	    if (inStream != null)
		inStream.close();
	}

	return xmlString;
    }

    
    
    /**
     * @author Tobias Janssen
     * @param is		InputStreamReader
     * @param pd		ProgressDialog
     * @return			String mit Inhalt des Streams
     * @throws IOException
     */
    private static String readFromHtmlStream(InputStreamReader is, ProgressDialog pd) throws IOException
    {
	int progress = pd.getProgress();
	try
	{

	    java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
	    char data[] = new char[1024];
	    int count = 0;
	    while ((count = is.read(data)) != -1)
	    {
		progress += count;
		pd.setProgress(progress);
		cw.write(data, 0, count);
	    }
	    return cw.toString();
	}
	catch (IOException e)
	{
	    throw new IOException(Const.ERROR_NOSERVER);
	}
    }

    /**
     * @author Tobias Janssen 
     * Konvertiert den Stundenplan von Xml zu WeekData
     * @param xmlContent
     * @return
     * @throws Exception
     */
    public static WeekData[] convertXmlToWeekData(String xmlContent) throws Exception
    {

	WeekData[] stupidData = new WeekData[0];
	Xml xml = new Xml("root", xmlContent);
	try
	{
	    // xml Header auslesen und version abholen
	    xml.parseXml();
	    String xmlVersion = "";
	    XmlSearch xmlSearch = new XmlSearch();
	    Xml version = xmlSearch.tagCrawlerFindFirstOf(xml, "xml", new Parameter("version", null));
	    if (version.getParameterAtIndex(0).getName().equalsIgnoreCase("version"))
	    {
		xmlVersion = version.getParameterAtIndex(0).getValue();
	    }

	    // verarbeiten der enthaltenen weeks
	    if (xmlVersion.equalsIgnoreCase("1"))
	    {
		for (int weekNo = 1; weekNo < xml.getChildTags().length; weekNo++)
		{
		    WeekData weekData = new WeekData(null);
		    Xml timetable = xml.getChildTagAtIndex(weekNo).getChildTagAtIndex(0);// den
		    // timetable
		    // abrufen
		    weekData = convertXmlVersion1ToWeekData(timetable);
		    stupidData = (WeekData[]) ArrayOperations.AppendToArray(stupidData, weekData);
		}
	    }
	}
	catch (Exception e)
	{
	    throw new Exception(Const.ERROR_XMLFAILURE);
	}

	return stupidData;
    }

    /**
     * @autor: @author Tobias Janssen
     * Konvertiert ein Xml Table zu einem mehrdimensionalen Array
     * 
     * @param xmlTimeTableTag
     * @return
     */
    private static WeekData convertXmlVersion1ToWeekData(Xml xmlTimeTableTag)
    {

	Xml week = xmlTimeTableTag.getParentTag();
	WeekData weekData = new WeekData(null);

	String classId = "";
	String weekId = "";
	String typeId = "";
	String syncTime = "";
	String weekDataVersion = "";
	for (int i = 0; i < week.getParameters().length; i++)
	{
	    if (week.getParameterAtIndex(i).getName().equalsIgnoreCase("classId"))
	    {
		classId = week.getParameterAtIndex(i).getValue();
		weekData.addParameter("classId", classId);
		weekData.elementId = classId;
	    }
	    if (week.getParameterAtIndex(i).getName().equalsIgnoreCase("date"))
	    {
		String[] splitDate = week.getParameterAtIndex(i).getValue().split("[.]");
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_MONTH, Integer.decode(splitDate[0]));
		// wegen indexverschiebung im kalender ist der monat x
		// eigentlich x-1
		cal.set(Calendar.MONTH, Integer.decode(splitDate[1]) - 1);
		cal.set(Calendar.YEAR, Integer.decode(splitDate[2]));
		weekData.date = cal;
	    }
	    if (week.getParameterAtIndex(i).getName().equalsIgnoreCase("weekId"))
	    {
		weekId = week.getParameterAtIndex(i).getValue();
		weekData.addParameter("weekId", weekId);
		weekData.weekId = weekId;
	    }
	    if (week.getParameterAtIndex(i).getName().equalsIgnoreCase("typeId"))
	    {
		typeId = week.getParameterAtIndex(i).getValue();
		weekData.addParameter("typeId", typeId);
		weekData.typeId = typeId;
	    }
	    if (week.getParameterAtIndex(i).getName().equalsIgnoreCase("syncTime"))
	    {
		syncTime = week.getParameterAtIndex(i).getValue();
		weekData.addParameter("syncTime", syncTime);
		weekData.syncTime = Long.parseLong(syncTime);
	    }
	    if (week.getParameterAtIndex(i).getName().equalsIgnoreCase("weekDataVersion"))
	    {
		weekDataVersion = week.getParameterAtIndex(i).getValue();
		weekData.addParameter("weekDataVersion", weekDataVersion);
		weekData.weekDataVersion = weekDataVersion;
	    }
	}

	Xml tr = xmlTimeTableTag.getChildTagAtIndex(0);

	int rows = tr.getParentTag().getChildTags().length;
	int cols = tr.getChildTags().length;

	weekData.timetable = new Xml[rows][cols];

	// die tabelle erstellen
	for (int y = 0; y < tr.getParentTag().getChildTags().length; y++)
	{
	    Xml[] td = tr.getParentTag().getChildTagAtIndex(y).getChildTags();
	    for (int x = 0; x < td.length; x++)
	    {
		weekData.timetable[y][x] = td[x];
	    }
	}

	return weekData;
    }

    public static String createProfileXml(MyContext ctxt, int index)
    {
	return "<" + Xml.PROFIL +">"+ index + "</"+Xml.PROFIL +">\n";
    }
}
