package de.janssen.android.gsoplan;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.ProgressDialog;


public class Xml
{
	public String container="";		//Enthält den noch zu verarbeitenden XmlText
	public static final String syncTime="syncTime";
	public static final String elements="elements";
	public static final String weekId="weekId";
	public static final String types="types";
	public static final String myElement="myElement";
	public static final String myType="myType";
	public static final String onlyWlan="onlyWlan";
	public static final String resyncAfter="resyncAfter";
	
    public static final String OPTION = "option";
    public static final String TR = "tr";
    
	private static final String VERSION="1";
	
	// / Datum: 12.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Konvertiert die SelectOptions des StupidCore zu xml
	// / Version: 1
	// /
	// / Parameter:
	// /
	// /
	// /
	public static String convertSetupToXml(StupidCore stupid) 
	{

		String result = "<" + syncTime + ">" + stupid.syncTime + "</"
				+ syncTime + ">\n";

		result += "<" + elements;
		for (int i = 0; i < stupid.elementList.length; i++) {
			result += " " + stupid.elementList[i].description + "='"
					+ stupid.elementList[i].index + "'";
		}
		result += " />\n";

		result += "<" + weekId;
		for (int i = 0; i < stupid.weekList.length; i++) {
			result += " " + stupid.weekList[i].description + "='"
					+ stupid.weekList[i].index + "'";
		}
		result += " />\n";
		
		result += "<"+types;
		for (int i = 0; i < stupid.typeList.length; i++) 
		{
			result += " " + stupid.typeList[i].description + "='"
					+ stupid.typeList[i].index + "'";
		}
		result += " />\n";
		result += "<"+myElement+">"+stupid.myElement+"</"+myElement+">";
		result += "<"+myType+">"+stupid.myType+"</"+myType+">";
		result += "<"+onlyWlan+">"+stupid.onlyWlan.toString()+"</"+onlyWlan+">";
		return result;
	}
	
	/// Datum: 12.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Konvertiert den StupidCore in einen XmlString
	// /
	// /
	// / Parameter:
	// / StupidCore, der alle Daten enthält
	// /
	// /
	
	// / Datum: 12.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Konvertiert die SelectOptions des StupidCore zu xml
	// / Version: 1
	// /
	// / Parameter:
	// /
	// /
	// /
	public static String convertSetupToXml(StupidCore stupid,ProgressDialog pd) {
		
		
		String result = "<"+syncTime+">"+stupid.syncTime+"</"+syncTime+">\n";
		
		result += "<"+elements;
		for (int i = 0; i < stupid.elementList.length; i++) 
		{
			pd.setProgress(pd.getProgress()+1);
			result += " " + stupid.elementList[i].description + "='"
					+ stupid.elementList[i].index + "'";
		}
		result += " />\n";

		result += "<"+weekId;
		for (int i = 0; i < stupid.weekList.length; i++) 
		{
			pd.setProgress(pd.getProgress()+1);
			result += " " + stupid.weekList[i].description + "='"
					+ stupid.weekList[i].index + "'";
		}
		result += " />\n";
		
		result += "<"+types;
		for (int i = 0; i < stupid.typeList.length; i++) 
		{
			pd.setProgress(pd.getProgress()+1);
			result += " " + stupid.typeList[i].description + "='"
					+ stupid.typeList[i].index + "'";
		}
		result += " />\n";
		result += "<"+myElement+">"+stupid.myElement+"</"+myElement+">";
		result += "<"+myType+">"+stupid.myType+"</"+myType+">";
		result += "<"+onlyWlan+">"+stupid.onlyWlan.toString()+"</"+onlyWlan+">";
		result += "<"+resyncAfter+">"+stupid.myResyncAfter+"</"+resyncAfter+">";
		return result;
	}
	
	
	
	/// Datum: 12.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Konvertiert den StupidCore in einen XmlString
	///	
	///
	///	Parameter:
	///	StupidCore, der alle Daten enthält
	/// 
	/// 
	public static String convertWeekDataToXml(WeekData data,ProgressDialog pd)
	{
		int pdvalue=pd.getProgress();
		String result="<xml version='"+VERSION+"'/>\n";
		pd.setProgress(pdvalue++);

		result+="<week date='"+data.date.get(Calendar.DAY_OF_MONTH)+"."+(data.date.get(Calendar.MONTH)+1)+"."+data.date.get(Calendar.YEAR)+"'";
		for(int p=0;p<data.parameters.length;p++)
		{
			result+= " "+data.parameters[p].name + "='" + data.parameters[p].value+"'";
		}
		result+=">\n\t<timetable>\n";
		for(int y=0;y<data.timetable.length;y++)
		{
			pd.setProgress(pdvalue++);
			result+="\t\t<row"+y+">\n";
			for(int x=0;x<data.timetable[y].length;x++)
			{
				pd.setProgress(pdvalue++);
				result+="\t\t\t<day"+x;
				for(int p=0;p<data.timetable[y][x].parameters.length;p++)
				{
					result+= " "+data.timetable[y][x].parameters[p].name + "='" + data.timetable[y][x].parameters[p].value+"'";
				}
				result+=">"+data.timetable[y][x].dataContent+"</day"+x+">\n";
			}
			result+="\t\t</row"+y+">\n";
		}
		result+="\t</timetable>\n</week>\n";

		return result;
	}
	
	public static String readFromHTML(URL url,ProgressDialog pd, int connectionTimeout) throws Exception 
	{
		
		InputStreamReader inStream = null;
		HttpURLConnection conn = null;
		String xmlString = "";
		try 
		{
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("content-type", "text/plain; charset=iso-8859-1");
			conn.setConnectTimeout(connectionTimeout);
			conn.connect();
			inStream = new InputStreamReader(conn.getInputStream(),"iso-8859-1");
			xmlString = readFromHtmlStream(inStream,pd);
		} 
		catch (SocketTimeoutException e) 
		{
			throw new Exception("Verbindungs-Timeout! Server nicht erreichbar!");

		} 
		catch (IOException e) 
		{
			throw new Exception("Keine Verbindung zum Server!");
		}
		catch (Exception e) 
		{
			throw new Exception("Keine Verbindung zum Server!");
		}
		finally
		{
			if(conn !=null)
				conn.disconnect();
			if(inStream != null)
				inStream.close();
		}
		
		
		return xmlString;
    }
	
	private static String readFromHtmlStream(InputStreamReader is,ProgressDialog pd) throws IOException 
	{
		int progress = pd.getProgress();
		try 
		{
			
			java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
			char data[] = new char[1024];
	        int count = 0;
	        while ((count = is.read(data)) != -1) {
	        	progress += count;
	        	pd.setProgress(progress);
	        	cw.write(data,0,count);
	        }
			return cw.toString();
		} 
		catch (IOException e) 
		{
			throw new IOException("Keine Verbindung zum Server!");
		}
	}
	

	
	
	/// <summary>
    /// Konvertiert den Quelltext aus der angegebenen Uri in ein HtmlTag-Array.
    /// 
    /// </summary>
    /// <param name="url">Die Uri zum Quelltext, der konvertiert werden soll</param>
    /// <returns>HtmlTag[]</returns>
	public static XmlTag[] xmlToArray(Xml xml) throws Exception
    {
        XmlTag[] tagArray = new XmlTag[0];    //das Array, das die Tags des Quelltextes enthalten wird
        XmlTag tag = new XmlTag();
        //jetzt alle tags auslesen, bis der Body wieder geschlossen wird:
        do
        {
            //den nächsten Tag abholen:
        	try
        	{
        		tag = XmlTag.parseNextXmlTag(xml);
        	}
        	catch(Exception e)
        	{
        		throw new Exception(e);
        	}
            tagArray = xml.AddTagToArray(tagArray, tag);

        }
        while (xml.container.length() > 1 );
        
        return tagArray;
    }
	
	/// <summary>
    /// Konvertiert den Quelltext aus der angegebenen Uri in ein HtmlTag-Array.
    /// 
    /// </summary>
    /// <param name="url">Die Uri zum Quelltext, der konvertiert werden soll</param>
    /// <returns>HtmlTag[]</returns>
	public static XmlTag[] xmlToArray(Xml xml, ProgressDialog pd)
    {
        XmlTag[] tagArray = new XmlTag[0];    //das Array, das die Tags des Quelltextes enthalten wird
        XmlTag tag = new XmlTag();
        //jetzt alle tags auslesen, bis der Body wieder geschlossen wird:
        do
        {
            //den nächsten Tag abholen:
        	try
        	{
        		
        		pd.setProgress(pd.getProgress()+50);
        		tag = XmlTag.parseNextXmlTag(xml);
        		
        	}
        	catch(Exception e)
        	{
        		//TODO:Theres an Problem reading the HTML
        	}
            tagArray = xml.AddTagToArray(tagArray, tag);

        }
        while (xml.container.length() > 1 );
        
        return tagArray;
    }
	
	private XmlTag currentTag = new XmlTag();
	
	private XmlTag[] AddTagToArray(XmlTag[] array, XmlTag tag)
    {
        //prüfen ob ein Schließer Tag vorliegt

        if (tag.isEndTag == true)
        {
    		XmlTag backupTag = new XmlTag();
    		backupTag = tag;
    		Boolean breakout = false;
            //den öffner von diesem Tag suchen und auf open=false setzen
            do
            {
                if (currentTag.type.equalsIgnoreCase(tag.type) && currentTag.isEndTag == false)
                {
                    //öffner element gefunden, nun schließen
                    currentTag.open = false;
                }
                else
                {
                	if(currentTag.parentTag == null)
                	{
                		currentTag = backupTag.parentTag;
                		breakout=true;
                	}
                	else
                	{
                		currentTag = currentTag.parentTag;
                	}
                }
            }
            while (currentTag.open && !breakout);
        }
        //ansonsten muss das Element an den letzten offenen tag angefügt werden
        else if (array.length > 0)
        {
            //den letzten offenen tag finden

            if (currentTag.open == true && currentTag.isEndTag == false)
            {
                //öffner element gefunden, nun hinzufügen
            	currentTag.childTags = (XmlTag[]) ArrayOperations.ResizeArray(currentTag.childTags, currentTag.childTags.length + 1);
                tag.parentTag = new XmlTag();
                tag.parentTag = currentTag;
                currentTag.childTags[currentTag.childTags.length - 1] = tag;
                if(tag.open)
                {
	                currentTag = new XmlTag();
	                currentTag = tag;
                }
            }
            else if(currentTag.open == false && currentTag.parentTag == null)
            {
            	//wir sind ganz oben angekommen
            	array = (XmlTag[]) ArrayOperations.ResizeArray(array, array.length + 1);
                
                array[array.length - 1] = new XmlTag();            //neues Objekt anlegen
                if (tag.isEndTag)
                {
                    tag.open = false;
                }
                array[array.length - 1] = tag;                      //tag hinzufügen
                currentTag = tag;
            }
            else if (currentTag.open == false && currentTag.isEndTag == false)
            {
            	//der parent ist bereits geschlossen, also den offenenParent suchen und als child hinzufügen
            	while (currentTag.open == false && currentTag.parentTag != null)
                {
                    currentTag = currentTag.parentTag;
                }
                currentTag.childTags = (XmlTag[]) ArrayOperations.ResizeArray(currentTag.childTags, currentTag.childTags.length + 1); 
                tag.parentTag = new XmlTag();
                tag.parentTag = currentTag;
                currentTag.childTags[currentTag.childTags.length - 1] = tag;
                currentTag = new XmlTag();
                currentTag = tag;
            }
       }
        else
        {
        	array = (XmlTag[]) ArrayOperations.ResizeArray(array, array.length + 1);//Array vergößern
            array[array.length - 1] = new XmlTag();            //neues Objekt anlegen
            if (tag.isEndTag)
            {
                tag.open = false;
            }
            array[array.length - 1] = tag;                      //tag hinzufügen
            currentTag = tag;
        }
        return array;
    }
	
	
	public WeekData[] convertXmlToStupid(Xml xml) throws Exception {
		
		WeekData[] stupidData = new WeekData[0];
		XmlTag[] xmlArray;
		try
		{
			//xml Header auslesen und version abholen
			xmlArray = xmlToArray(xml);
			String xmlVersion = "";
			for (int i = 0; i < xmlArray[0].parameters.length; i++) {
				if (xmlArray[0].parameters[i].name.equalsIgnoreCase("version")) {
					xmlVersion = xmlArray[0].parameters[i].value;
				}
	
			}
			//verarbeiten der enthaltenen weeks
			if (xmlVersion.equalsIgnoreCase("1")) 
			{
				for(int weekNo=1;weekNo<xmlArray.length;weekNo++)
				{
					WeekData weekData = new WeekData(null);
					XmlTag timetable = xmlArray[weekNo].childTags[0];// den timetable abrufen
					weekData = convertXmlVersion1ToWeekData(timetable);
					stupidData = (WeekData[]) ArrayOperations.AppendToArray(stupidData, weekData);
				}
	    	}
		}
		catch (Exception e)
		{
			throw new Exception("Fehler bei der XML Konvertierung! Code:0x002 \n"+e.getMessage());
		}
		

		return stupidData;
	}
	// / Datum: 12.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Konvertiert ein Xml Table zu einem mehrdimensionalen Array
	// / Version: 1
	// /
	// / Parameter:
	// /
	// /
	// /
	private WeekData convertXmlVersion1ToWeekData(XmlTag xmlTimeTableTag) {
		
		XmlTag week = xmlTimeTableTag.parentTag;
		WeekData weekData = new WeekData(null);

		String classId = "";
		String weekId = "";
		String typeId = "";
		String syncTime = "";
		String weekDataVersion="";
		for (int i = 0; i < week.parameters.length; i++) {
			if (week.parameters[i].name.equalsIgnoreCase("classId")) {
				classId = week.parameters[i].value;
				weekData.addParameter("classId", classId);
				weekData.elementId=classId;
			}
			if (week.parameters[i].name.equalsIgnoreCase("date")) {
				String[] splitDate = week.parameters[i].value.split("[.]");
				Calendar cal = new GregorianCalendar();
				cal.set(Calendar.DAY_OF_MONTH, Integer.decode(splitDate[0]));
				//wegen indexverschiebung im kalender ist der monat x eigentlich x-1 
				cal.set(Calendar.MONTH, Integer.decode(splitDate[1])-1);
				cal.set(Calendar.YEAR, Integer.decode(splitDate[2]));
				weekData.date=cal;
			}
			if (week.parameters[i].name.equalsIgnoreCase("weekId")) {
				weekId = week.parameters[i].value;
				weekData.addParameter("weekId", weekId);
				weekData.weekId=weekId;
			}
			if (week.parameters[i].name.equalsIgnoreCase("typeId")) {
				typeId = week.parameters[i].value;
				weekData.addParameter("typeId", typeId);
				weekData.typeId=typeId;
			}
			if (week.parameters[i].name.equalsIgnoreCase("syncTime")) {
				syncTime = week.parameters[i].value;
				weekData.addParameter("syncTime", syncTime);
				weekData.syncTime=Long.parseLong(syncTime);
			}
			if (week.parameters[i].name.equalsIgnoreCase("weekDataVersion")) {
				weekDataVersion = week.parameters[i].value;
				weekData.addParameter("weekDataVersion", weekDataVersion);
				weekData.weekDataVersion=weekDataVersion;
			}
		}

		
		XmlTag tr = xmlTimeTableTag.childTags[0];
		
		int rows = tr.parentTag.childTags.length;
		int cols = tr.childTags.length;
		
		weekData.timetable = new XmlTag[rows][cols];

		// die tabelle erstellen
		for (int y = 0; y < tr.parentTag.childTags.length; y++) 
		{
			XmlTag[] td = tr.parentTag.childTags[y].childTags;
			for (int x = 0; x < td.length; x++) 
			{
				weekData.timetable[y][x] = td[x];
			}
		}

		return weekData;
	}
}
