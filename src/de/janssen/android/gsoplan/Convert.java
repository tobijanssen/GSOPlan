/*
 * Convert.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.core.Profil;
import de.janssen.android.gsoplan.core.Type;
import de.janssen.android.gsoplan.core.WeekData;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.Parameter;
import de.janssen.android.gsoplan.dataclasses.SelectOptions;
import de.janssen.android.gsoplan.dataclasses.Types;
import de.janssen.android.gsoplan.xml.Xml;
import de.janssen.android.gsoplan.xml.XmlSearch;

public class Convert
{
    /**
     * Konvertiert aus dem übergebenem Types-Objekt ein XML String
     * @param types	Das Types Objekt
     * @return		Der XML String
     */
    public static String toXml(Types types)
    {
	String result="";
	result += "<" + Xml.HTMLMOD + ">" + types.htmlModDate + "</" + Xml.HTMLMOD + ">\n";
	result += "<" + Xml.TYPES + ">\n";
	for(int i=0;i<types.list.size();i++)
	{
	    result += "\t<" + Xml.TYPE;
	    	result += " name='" + types.list.get(i).typeName + "'";
	    	result += " key='" + types.list.get(i).type + "'";
	    result += ">\n";
	    
	    for(int y=0;y<types.list.get(i).elementList.size();y++)
	    {
        	    result += "\t\t<" + Xml.ELEMENT;
        	    	result += " "+types.list.get(i).elementList.get(y).description+"='" + types.list.get(i).elementList.get(y).index + "'";
        	    result += ">";
        	    result += "</" + Xml.ELEMENT + ">\n";
	    }
	    for(int y=0;y<types.list.get(i).weekList.size();y++)
	    {
        	    result += "\t\t<" + Xml.WEEK;
        	    	result += " "+types.list.get(i).weekList.get(y).description+"='" + types.list.get(i).weekList.get(y).index + "'";
        	    result += ">";
        	    result += "</" + Xml.WEEK + ">\n";
	    }
	    result +="\t</"+Xml.TYPE+">\n";
	}
	result +="</"+Xml.TYPES+">";
	return result;
    }

    /**
     * Konvertiert aus dem übergebenem Xml String ein Types-Objekt
     * @author Tobias Janssen 
     * @param xml
     * @return
     * @throws Exception
     */
    public static Types toTypesList(Xml xml) throws Exception
    {
	Types result = new Types();
	
	xml.parseXml();
	Exception exception = new Exception();
	XmlSearch xmlSearch = new XmlSearch();
	Xml types = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.TYPES));
	
	xmlSearch = new XmlSearch();
	Xml lastHtmlMod = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.HTMLMOD));
	
	if (lastHtmlMod != null && lastHtmlMod.getDataContent() != null)
	{
	    result.htmlModDate = Long.parseLong(lastHtmlMod.getDataContent());
	}
	
	if (types == null)
	{
	    throw new Exception(
		    "Fehler beim Konvertieren der XML Elemente aus der Datendatei. Details:\nDer Element XmlTag konnte nicht gefunden werden!");
	}
	List<Type> listResult = new ArrayList<Type>();
	try
	{
	    //TypeListe
	    if (types.getType().equalsIgnoreCase(Xml.TYPES))
	    {
		
		Xml[] xmlChilds = types.getChildTags();
		for( int i= 0;i<xmlChilds.length;i++)
		{
		    //jedes einzelne type(in der regel 3 stück)
		    Type newType = new Type();
		    if (xmlChilds[i].getType().equalsIgnoreCase(Xml.TYPE))
		    {
			Parameter[] para = xmlChilds[i].getParameters();
			for(int p=0;p<para.length;p++)
			{
			    if(para[p].getName().equalsIgnoreCase("name"))
			    {
				newType.typeName = para[p].getValue();
			    }
			    if(para[p].getName().equalsIgnoreCase("key"))
			    {
				newType.type = para[p].getValue();
			    }
			}
			
			Xml[] childs = xmlChilds[i].getChildTags();
			for( int y= 0;y<childs.length;y++)
			{
			    if (childs[y].getType().equalsIgnoreCase(Xml.ELEMENT))
			    {
				para = childs[y].getParameters();
				for(int p=0;p<para.length;p++)
				{
				    newType.elementList.add(new SelectOptions(para[p].getValue(),para[p].getName()));
				}
			    }
			    if (childs[y].getType().equalsIgnoreCase(Xml.WEEK))
			    {
				para = childs[y].getParameters();
				for(int p=0;p<para.length;p++)
				{
				    newType.weekList.add(new SelectOptions(para[p].getValue(),para[p].getName()));
				}
			    }
			}
			
		    }
		    listResult.add(newType);
		    
		}
	    }
	    else
		throw new Exception(
			"Fehler beim Erstellen der Optionen. Details:\nDer Elements XmlTag ist falsch definiert!");
	}
	catch (Exception e)
	{
	    throw e;
	}
	if(exception.getMessage() != null)
	    throw exception;
	
	
	result.list=listResult;
	return result;
	
    }
    
    /**
     * Konvertiert aus dem übergebenem WeekData Objekt einen XML String
     * @author Tobias Janssen
     * @param data	WeekData Objekt das konvertiert werden soll 
     * @return		XML String
     */
    public static String toXml(WeekData data)
    {
	
	String result = "<xml version='" + Const.XMLVERSION + "'/>\n";
	

	result += "<week date='" + data.date.get(Calendar.DAY_OF_MONTH) + "." + (data.date.get(Calendar.MONTH) + 1)
		+ "." + data.date.get(Calendar.YEAR) + "'";
	for (int p = 0; p < data.parameters.length; p++)
	{
	    result += " " + data.parameters[p].getName() + "='" + data.parameters[p].getValue() + "'";
	}
	result += ">\n\t<timetable>\n";
	for (int y = 0; y < data.timetable.length; y++)
	{
	
	    result += "\t\t<row" + y + ">\n";
	    for (int x = 0; x < data.timetable[y].length; x++)
	    {
	
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
     * Konvertiert aus dem übergebenem Xml String ein WeekData-Array
     * 
     * @author Tobias Janssen 
     * @param xmlContent
     * @return
     * @throws Exception	Wenn übergebener Xml-String ungültig ist
     */
    public static WeekData[] toWeekDataArray(String xmlContent) throws Exception
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
		    weekData = toWeekData(timetable);
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
     * Konvertiert aus dem übergebenem Xml String ein WeekData Objekt
     * @author Tobias Janssen
     * 
     * @param xmlTimeTableTag
     * @return
     */
    private static WeekData toWeekData(Xml xmlTimeTableTag)
    {

	Xml week = xmlTimeTableTag.getParentTag();
	WeekData weekData = new WeekData(null);

	String classId = "";
	String weekId = "";
	String typeId = "";
	String syncTime = "";
	String lastHtmlMod = "";
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
	    if (week.getParameterAtIndex(i).getName().equalsIgnoreCase("lastHtmlMod"))
	    {
		lastHtmlMod = week.getParameterAtIndex(i).getValue();
		weekData.addParameter("lastHtmlMod", lastHtmlMod);
		weekData.lastHtmlModified = Long.parseLong(lastHtmlMod);
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
    
    /**
     * Konvertiert die übergebene Profil-Liste in einen XML String
     * @param ctxt
     * @param index
     * @return
     */
    public static String toXml(List<Profil> profiles)
    {
	String result = "<" + Xml.PROFILES +">\n";
	for(int i=0;i<profiles.size();i++)
	{
        	result += "\t<" + Xml.PROFIL +"\n";
        	result += "\t\tmyElement='"+profiles.get(i).myElement+"'\n";
        	result += "\t\tmyTypeIndex='"+profiles.get(i).myTypeIndex+"'\n";
        	result += "\t\tmyTypeKey='"+profiles.get(i).myTypeKey+"'\n";
        	result += "\t\tmyTypeName='"+profiles.get(i).myTypeName+"'\n";
        	result += "\t\tonlyWlan='"+profiles.get(i).onlyWlan+"'\n";
        	result += "\t\thideEmptyHours='"+profiles.get(i).hideEmptyHours+"'\n";
        	result += "\t\tautoSync='"+profiles.get(i).autoSync+"'\n";
        	result += "\t\tnotificate='"+profiles.get(i).notificate+"'\n";
        	result += "\t\tvibrate='"+profiles.get(i).vibrate+"'\n";
        	result += "\t\tsound='"+profiles.get(i).sound+"'\n";
        	result += "\t\tmyResync='"+profiles.get(i).myResync+"'\n";
        	result += "\t\tmylastResync='"+profiles.get(i).mylastResync+"'\n";
        	result += "\t>\n";
        	result +="\t</"+Xml.PROFIL +">\n";
	}
	result += "</"+Xml.PROFILES+">\n";
	return result;
    }
    
    /**
     * Konvertiert einen Xml String zu einer Profiles Liste
     * @param xml
     * @return
     */
    public static List<Profil> toProfiles(Xml xml, MyContext ctxt)
    {
	XmlSearch search = new XmlSearch();
	xml = search.tagCrawlerFindFirstEntryOf(xml, Xml.PROFILES);   

	List<Profil> profiles = new ArrayList<Profil>();

	String myElement,myTypeKey,myTypeName;
	int myTypeIndex;
	Boolean onlyWlan,hideEmptyHours,autoSync,notificate,vibrate,sound;
	long myResync, mylastResync;
	Xml child;
	
	for (int ci = 0; ci < xml.getChildTags().length; ci++)
	{
	    child = xml.getChildTagAtIndex(ci);
	    myElement = "";
	    myTypeKey = "";
	    myTypeName = "";
	    myTypeIndex = 0;
	    onlyWlan = false;
	    hideEmptyHours = false;
	    autoSync = false;
	    notificate = false;
	    vibrate = false;
	    sound = false;
	    myResync = 10;
	    mylastResync = 0;
	
	    for (int i = 0; i < child.getParameters().length; i++)
	    {
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("myElement"))
		    myElement = child.getParameterAtIndex(i).getValue();
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("myTypeKey"))
		    myTypeKey = child.getParameterAtIndex(i).getValue();
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("myTypeName"))
		    myTypeName = child.getParameterAtIndex(i).getValue();

		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("myTypeIndex"))
		{
		    try
		    {
			myTypeIndex = Integer.parseInt(child.getParameterAtIndex(i).getValue());
		    }
		    catch (Exception e)
		    {
			myTypeIndex = 0;
		    }
		}

		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("onlyWlan"))
		{
		    if (child.getParameterAtIndex(i).getValue().equalsIgnoreCase("true"))
			onlyWlan = true;
		    else
			onlyWlan = false;
		}
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("hideEmptyHours"))
		{
		    if (child.getParameterAtIndex(i).getValue().equalsIgnoreCase("true"))
			hideEmptyHours = true;
		    else
			hideEmptyHours = false;
		}
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("autoSync"))
		{
		    if (child.getParameterAtIndex(i).getValue().equalsIgnoreCase("true"))
			autoSync = true;
		    else
			autoSync = false;
		}
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("notificate"))
		{
		    if (child.getParameterAtIndex(i).getValue().equalsIgnoreCase("true"))
			notificate = true;
		    else
			notificate = false;
		}
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("vibrate"))
		{
		    if (child.getParameterAtIndex(i).getValue().equalsIgnoreCase("true"))
			vibrate = true;
		    else
			vibrate = false;
		}
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("sound"))
		{
		    if (child.getParameterAtIndex(i).getValue().equalsIgnoreCase("true"))
			sound = true;
		    else
			sound = false;
		}
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("myResync"))
		{
		    try
		    {
			myResync = Long.parseLong(child.getParameterAtIndex(i).getValue());
		    }
		    catch (Exception e)
		    {
			myResync = 60;
		    }
		}
		if (child.getParameterAtIndex(i).getName().equalsIgnoreCase("myLastResync"))
		{
		    try
		    {
			mylastResync = Long.parseLong(child.getParameterAtIndex(i).getValue());
		    }
		    catch (Exception e)
		    {
			mylastResync = 0;
		    }
		}
	    }

	    Profil profil = new Profil(myElement, myTypeIndex, myTypeKey, myTypeName, onlyWlan, hideEmptyHours, autoSync,
		    notificate, vibrate, sound, myResync, mylastResync, ctxt);
	    profiles.add(profil);
	
	}
	return profiles;
    }
}
