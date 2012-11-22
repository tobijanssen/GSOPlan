package de.janssen.android.gsoplan;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import android.app.ProgressDialog;
import android.graphics.Point;


public class StupidCore {
	public SelectOptions[] elementList = new SelectOptions[0];
	public SelectOptions[] typeList = new SelectOptions[0];
	public SelectOptions[] weekList = new SelectOptions[0];
	public String myElement="";
	public int myType=0;
	public Boolean onlyWlan=false;
	public Boolean hideEmptyHours=true;
	public long syncTime = 0;
	public List <WeekData> stupidData = new ArrayList<WeekData>();
	public Boolean dataIsDirty=false;
	public Boolean setupIsDirty=false;
	public ProgressDialog progressDialog;
	public String[] timeslots =new String[]{"","7.45 - 8.30","8.30 - 9.15","9.35 - 10.20","10.20 - 11.05","11.25 - 12.10","12.10 - 12.55","13.15 - 14.00","14.00 - 14.45","15.05 - 15.50","15.50 - 16.35","16.55 - 17.40","17.40 - 18.25","18.25 - 19.10","19.30 - 20.15","20.15 - 21.00"};
	public long myResyncAfter=10;
	public Calendar currentDate = new GregorianCalendar();
	public TimeTableIndex[] myTimetables;
	
	
	final String NAVBARURL = "http://stupid.gso-koeln.de/frames/navbar.htm"; 
	//final String NAVBARURL = "http://eqtain.de/stupid/frames/navbar.htm";
	final String URLSTUPID = "http://stupid.gso-koeln.de/";
	
	public StupidCore()
	{
		int currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);
		//den currentDay auf Montag setzten
    	if(currentDayOfWeek < 2)
    	{
    		//1000*60*60*24 = 1 Tag!
    		currentDate.setTimeInMillis(currentDate.getTimeInMillis()+(1000*60*60*24*(2-currentDayOfWeek)));
    	}
	}
	
	// / Datum: 25.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / leert die Daten
	public void clearData()
	{
		this.stupidData.clear();
	}
	
	// / Datum: 20.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / leert das Setup
	public void clearSetup()
	{
		this.elementList = new SelectOptions[0];
		this.typeList = new SelectOptions[0];
		this.weekList = new SelectOptions[0];
		this.myElement="";
		this.myType=0;
		this.onlyWlan=false;
	}

	/// Datum: 06.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Reduziert das Mehrdimensionale Array auf deren Wichtigen Inhalt 
	///	
	private WeekData collapseWeekDataMultiDim(WeekData weekData)
	{
		//in das erste Feld gehen
		XmlTag sourceField;
		
		//entfernt alle Doppel-Zeilen und Spalten, die durch spans entstanden sind
		weekData=removeDubColsnRows(weekData);
		
		//nun alle felder durchlaufen und die XML tag zusammen summieren
		for(int y= 0; y < weekData.timetable.length; y++)
		{
			for(int x= 0; x < weekData.timetable[y].length; x++)
			{
				sourceField = weekData.timetable[y][x];
				//eine zufalls id für dieses feld vergeben, dadurch werden alle schon besuchten tags markiert. jedoch nur für diesen suchlauf
				int rndmId = new java.util.Random().nextInt();
				XmlTag resultField= new XmlTag();
				weekData.timetable[y][x] = SummerizeField(sourceField,rndmId, sourceField,resultField);
			}
		}
		//nun noch alle leeren Zeilen und Spalten entfernen, falls vorhanden
		weekData = removeEmtyColsnRows(weekData);

		
		return weekData;
	}
	
	/// Datum: 30.08.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Konvertiert ein Html Table Tag zu einem mehrdimensionalen Array 
	///	
	///
	///	Parameter:
	///	
	/// 
	/// 
	private WeekData convertXmlTableToWeekData(XmlTag htmlTableTag)
	{
		// Größe des benötigten Arrays muss kalkuliert werden
		WeekData weekData = new WeekData(this);
		//TODO: sycnDate darf nur bei HTML gesetzt werden!!!
		weekData.setSyncDate();
		weekData.timetable = new XmlTag[0][0]; 
		// die tr heraussuchen:
		// TODO:hier muss eine andere form des crawlers her, der nicht tiefer
		// als x suchen soll
		XmlTag tr = htmlTableTag.tagCrawlerFindFirstEntryOf(htmlTableTag, Xml.TR, new XmlTag());
		// TODO: Abfangen wenn nichts gefunden wurde!

		// TODO:das hier ist noch nicht optimal:
		int rows = tr.parentTag.childTags.length;
		int cols = 0;
		Boolean colSpanFound = false;
		// jedes td prüfen:
		for (int i = 0; i < tr.childTags.length; i++) 
		{
			// auf parameter prüfen
			if (tr.childTags[i].parameters.length > 0) 
			{
				colSpanFound = false;
				// jeden parameter überprüfen:
				for (int parIndex = 0; parIndex < tr.childTags[i].parameters.length; parIndex++) 
				{
					if (tr.childTags[i].parameters[parIndex].name.equalsIgnoreCase("colspan")) 
					{
						String value = tr.childTags[i].parameters[parIndex].value;
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
		weekData.timetable = new XmlTag[rows][cols];
		
		//die tabelle erstellen
		for(int y = 0; y < tr.parentTag.childTags.length; y++)
		{
			XmlTag[] td = tr.parentTag.childTags[y].childTags;

			Point insertPoint;
			//jedes td prüfen:
			for(int i=0; i < td.length; i++)
			{				
				insertPoint = getLastFreePosition(weekData);
				//auf parameter prüfen
				if(td[i].parameters.length >0)
				{
					int colspan = 0;
					int rowspan = 0;
					//jeden parameter überprüfen:
					for(int parIndex=0;parIndex < td[i].parameters.length;parIndex++)
					{
						if(td[i].parameters[parIndex].name.equalsIgnoreCase("colspan"))
						{
							String value = td[i].parameters[parIndex].value;
							colspan = java.lang.Integer.parseInt(value);
						}
						if( td[i].parameters[parIndex].name.equalsIgnoreCase("rowspan"))
						{
							String value = td[i].parameters[parIndex].value;
							rowspan = Integer.parseInt(value);
						}
					}
					
					if(rowspan > 0 || colspan > 0)
					{
						int col = 0;
						do
						{
							insertPoint = getLastFreePosition(weekData);
							if(rowspan > 0)
							{
								for(int row = 0;row<rowspan;row++)
								{
									weekData.timetable[insertPoint.y+row][insertPoint.x]=td[i];
								}
							}
							else
							{	
								weekData.timetable[insertPoint.y][insertPoint.x]=td[i];
							}
							col++;
						}
						while(col<colspan);
					}
					else
					{
						weekData.timetable[insertPoint.y][insertPoint.x]=td[i];
					}
				}
				else
				{
					weekData.timetable[insertPoint.y][insertPoint.x]=td[i];
				}
					
			}
		}
		
		return weekData;
	}

	/*	Datum: 30.08.12
	 * 	Tobias Janßen
	 *  
	 *  Lädt die Selectoren von der GSO Seite und parsed diese in den StupidCore
	 *  
	 */
	public void fetchSelectorsFromNet() throws Exception
	{
		XmlTag[] xmlTagArray = new XmlTag[0];
		try
        {
        	URL url = new URL(NAVBARURL);
        	Xml xml = new Xml();
        	xml.container = Xml.readFromHTML(url,this.progressDialog,Const.CONNECTIONTIMEOUT);
        	xmlTagArray = Xml.xmlToArray(xml);
        }
		catch(Exception e)
        {
        	throw e;
        }
		
        //verfügbare Wochen abrufen
        XmlTag searchResult = xmlTagArray[0].tagCrawlerFindFirstOf(xmlTagArray[0], "select", "name", "week",new XmlTag()); 
        
        try
        {
        	weekList = getOptionsFromSelectTag(searchResult);
        }
        catch(Exception e)
        {
        	throw e;
        }
        
        
        //verfügbare Typen abrufen
        searchResult = xmlTagArray[0].tagCrawlerFindFirstOf(xmlTagArray[0], "select", "name", "type",new XmlTag()); 
        try
        {
        	typeList = getOptionsFromSelectTag(searchResult);
        }
        catch(Exception e)
        {
        	throw e;
        }
        
        if(typeList.length==0)
        {
        	throw new Exception("Konnte TypeList nicht extrahieren");
        }
        else
        {
        	if(typeList[this.myType].index.equalsIgnoreCase("c"))
        	{
        		//verfügbare Klassen abrufen
                searchResult = xmlTagArray[0].tagCrawlerFindFirstOf(xmlTagArray[0], "script", "var classes",new XmlTag());
                elementList = GetOptionsFromJavaScriptArray(searchResult, "classes");
        	}
        	if(typeList[this.myType].index.equalsIgnoreCase("t"))
        	{
        		//verfügbare Lehrer abrufen
                searchResult = xmlTagArray[0].tagCrawlerFindFirstOf(xmlTagArray[0], "script", "var teachers",new XmlTag());
                elementList = GetOptionsFromJavaScriptArray(searchResult, "teachers");
        	}
        	if(typeList[this.myType].index.equalsIgnoreCase("r"))
        	{
        		//verfügbare Räume abrufen
                searchResult = xmlTagArray[0].tagCrawlerFindFirstOf(xmlTagArray[0], "script", "var rooms",new XmlTag());
                elementList = GetOptionsFromJavaScriptArray(searchResult, "rooms");
        	}
        }
        
        this.progressDialog.setProgress(this.progressDialog.getProgress()+500);
       
        
        setupIsDirty=true;

		Date date = new Date();
		syncTime = date.getTime(); 
		this.progressDialog.setProgress(this.progressDialog.getProgress()+500);
	}
	
	
	// / Datum: 12.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / f[gt die SelectOptions aus dem angegebenen Xml hinzu
	// / Version: 1
	// /
	// / Parameter:
	// /
	// /
	// /
	public void fetchSetupFromXml(Xml xml) throws Exception 
	{
		XmlTag[] tagArray = Xml.xmlToArray(xml);
		XmlTag parent = new XmlTag();
		parent.type = "parent";
		parent.childTags=tagArray;
		
		XmlTag elements = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.elements), new XmlTag());
		XmlTag weeks = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.weekId), new XmlTag());
		XmlTag time = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.syncTime), new XmlTag());
		XmlTag types = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.types), new XmlTag());
		XmlTag myElementTag = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.myElement), new XmlTag());
		XmlTag myTypeTag = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.myType), new XmlTag());
		XmlTag onlyWlan = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.onlyWlan), new XmlTag());
		XmlTag resyncAfter = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.resyncAfter), new XmlTag());
		XmlTag hideEmptyHours = parent.tagCrawlerFindFirstOf(parent, new XmlTag(Xml.hideEmptyHours), new XmlTag());
		
		if(hideEmptyHours.dataContent != null)
		{
			if(hideEmptyHours.dataContent.equalsIgnoreCase("true"))
				this.hideEmptyHours = true;
			else
				this.hideEmptyHours = false;
		}
		
		if(resyncAfter.dataContent != null)
		{
			this.myResyncAfter= Long.decode(resyncAfter.dataContent);
		}
		
		if(onlyWlan.dataContent != null)
		{
			if(onlyWlan.dataContent.equalsIgnoreCase("true"))
				this.onlyWlan = true;
			else
				this.onlyWlan = false;
		}
			
		
		if(myElementTag.dataContent != null)
			myElement=myElementTag.dataContent;
		if(myTypeTag.dataContent != null)
		{
			try
			{
				myType=Integer.decode(myTypeTag.dataContent);
			}
			catch(Exception e)
			{
				//type falsch definiert, setze auf standart:
				myType=0;
			}
		}
		if(time.dataContent != null)
			syncTime = Long.parseLong(time.dataContent);
		
		if(elements.type == null)
		{
			throw new Exception("Fehler beim Konvertieren der XML Elemente aus der Datendatei. Details:\nDer Element XmlTag konnte nicht gefunden werden!");
		}
		if(weeks.type == null)
		{
			throw new Exception("Fehler beim Konvertieren der XML Wochen aus der Datendatei. Details:\nDer Wochen XmlTag konnte nicht gefunden werden!");
		}
		if(types.type == null)
		{
			throw new Exception("Fehler beim Konvertieren der XML Types aus der Datendatei. Details:\nDer Types XmlTag konnte nicht gefunden werden!");
		}
		
		try
		{
			if(elements.type.equalsIgnoreCase(Xml.elements))
			{
				for (int i=0;i<elements.parameters.length;i++)
				{
					SelectOptions option = new SelectOptions();
					option.description = elements.parameters[i].name;
					option.index = elements.parameters[i].value;
					
					elementList = (SelectOptions[]) ArrayOperations.AppendToArray(elementList, option);
				}
			}
			else
				throw new Exception("Fehler beim Erstellen der Optionen. Details:\nDer Elements XmlTag ist falsch definiert!");
			if(weeks.type.equalsIgnoreCase(Xml.weekId))
			{
				for (int i=0;i<weeks.parameters.length;i++)
				{
					SelectOptions option = new SelectOptions();
					option.description = weeks.parameters[i].name;
					option.index = weeks.parameters[i].value;
					
					weekList = (SelectOptions[]) ArrayOperations.AppendToArray(weekList, option);
				}
			}
			else
				throw new Exception("Fehler beim Erstellen der verfügbaren Wochen:\nDetails:\nDer Wochen XmlTag ist falsch definiert!");
			if(types.type.equalsIgnoreCase(Xml.types))
			{
				for (int i=0;i<types.parameters.length;i++)
				{
					SelectOptions option = new SelectOptions();
					option.description = types.parameters[i].name;
					option.index = types.parameters[i].value;
					
					typeList = (SelectOptions[]) ArrayOperations.AppendToArray(typeList, option);
				}
			}
			else
				throw new Exception("Fehler beim Erstellen der verfügbaren Typen:\nDetails:\nDer Typen XmlTag ist falsch definiert!");
		}
		catch(Exception e)
		{
			throw new Exception("Fehler beim Erstellen der Optionen");
		}
	}
	
	
	
	/*	Datum: 15.08.12
	 * 	Tobias Janßen
	 *  
	 *  Lädt den angegebenen TimeTable von der GSO Seite und parsed diesen in den StupidCore
	 *  
	 */	
	public DownloadFeedback fetchTimeTableFromNet(String selectedStringDate, String selectedElement, String selectedType) throws Exception
	{
		int dataIndex = -1;
		String selectedDateIndex = getIndexOfSelectorValue(weekList,selectedStringDate);
		String selectedClassIndex= getIndexOfSelectorValue(elementList,selectedElement);
		this.progressDialog.setProgress(this.progressDialog.getProgress()+500);
		while(selectedClassIndex.length()<5)
		{
			selectedClassIndex="0"+selectedClassIndex;
		}
		if(selectedClassIndex=="-1" || selectedDateIndex=="-1" || selectedType.equalsIgnoreCase(""))
		{
			throw new Exception("Fehler bei der URL-Generierung!");
		}

		
		XmlTag[] xmlArray = new XmlTag[0];
		try
        {
			URL url = new URL(URLSTUPID+selectedDateIndex+"/"+selectedType+"/"+selectedType+selectedClassIndex+".htm");
			Xml xml = new Xml();
			xml.container = Xml.readFromHTML(url,this.progressDialog,Const.CONNECTIONTIMEOUT);
        	xmlArray = Xml.xmlToArray(xml,this.progressDialog);
        }
		catch(Exception e)
        {
        	throw e;
        }

        
        XmlTag highTag = new XmlTag();
        highTag.type="highTag";
        highTag.childTags = xmlArray;
        
        //TODO: ein ordentliches Abfangen:
       
        //Herausfiltern des angezeigten Elements aus dem XML Array
        //Leider gibt es nicht viele wiedererkennungswerte des XML Tags
        XmlTag  elementSearchResult = highTag.tagCrawlerFindFirstOf(highTag, "font","size","5", new XmlTag());
        String shownElement = elementSearchResult.dataContent.replaceAll(" ", "");
        shownElement = shownElement.replaceAll("\n", "");
        shownElement = shownElement.replaceAll("&nbsp;", "");
        
        if(!shownElement.contains(selectedElement))
        {
        	//es kann sein, dass sich dieses Tag nicht mehr mit den angegeben suchparametern finden lässt("font","size","5")
        	//daher muss nun geprüft werden, ob das gesuchte Element überhaupt im Quelltext auftritt, also alles durchsuchen
        	try
        	{
	        	elementSearchResult = highTag.tagCrawlerFindFirstOf(highTag, "font", selectedElement, new XmlTag());
	        	shownElement = elementSearchResult.dataContent.replaceAll(" ", "");
	            shownElement = shownElement.replaceAll("\n", "");
	            shownElement = shownElement.replaceAll("&nbsp;", "");
        	}
        	catch(Exception e)
        	{
        		throw new Exception("Bei der Konvertierung des Quelltextes ist ein Fehler aufgetreten!\n Versuchen Sie es erneut, oder wenden Sie sich bei erneutem Auftreten an den Entwickler!");
        	}
            
        	if(!shownElement.contains(selectedElement))
        	{
        		//Nein, leider konnte es so auch nicht gefunden werden
        		throw new Exception("Bei der Konvertierung des Quelltextes ist ein Fehler aufgetreten!\n Versuchen Sie es erneut, oder wenden Sie sich bei erneutem Auftreten an den Entwickler!");
        	}
        }
        
        //den Timetable Tag finden
        XmlTag  xmlTimeTable = highTag.tagCrawlerFindFirstEntryOf(elementSearchResult.parentTag, "table", new XmlTag());//den HauptStundenplan-Table abrufen
        
        
        //den XmlTimeTable in das WeekData format wandeln:
        WeekData weekData = new WeekData(this);
        weekData = convertXmlTableToWeekData(xmlTimeTable);		//Konvertiert den angegebenen XmlTable in das WeekData-format.
        this.progressDialog.setProgress(this.progressDialog.getProgress()+1000);
        weekData = collapseWeekDataMultiDim(weekData);			//reduziert den Inhalt des WeekData.timetable auf den Kern, es werden doppelt einträe entfernt und leere spalten und zeilen herausgefiltert
        
        //Die aktuelle Zeit als SyncTime festhalten
        weekData.setSyncDate();
        
        //den Datums-String in ein Kalender Object wandeln

        try
        {
            //dazu erstmal den string splitten
        	String[] splitDate = selectedStringDate.split("[.]");
            
            Calendar cal = new GregorianCalendar();
            
            cal.set(Calendar.YEAR, Integer.decode(splitDate[2]));
            cal.set(Calendar.MONTH, Integer.decode(splitDate[1])-1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.decode(splitDate[0]));
            weekData.date=cal;
        }
        catch(Exception e)
        {
        	throw new Exception("Das Datum konnte nicht geparsed werden");
        }
        
        weekData.elementId = shownElement;
        weekData.addParameter("classId", shownElement);
        weekData.weekId = selectedDateIndex;
        weekData.addParameter("weekId", selectedDateIndex);
        weekData.typeId = selectedType;
        weekData.addParameter("typeId", selectedType);
        this.progressDialog.setProgress(this.progressDialog.getProgress()+1000);
        
        //prüfen, ob bereits die Woche für die Klasse un den typ vorliegt:

        long existSyncTime=0;
        WeekData exitWeekData= new WeekData(this);
        //alle bestehden Daten abrufen:
        for(int y =0;y<this.stupidData.size();y++)
        {
        	this.progressDialog.setProgress(this.progressDialog.getProgress()+50);
        	exitWeekData=this.stupidData.get(y);
        	//prüfen, ob das bestehende Element, dem neu hinzuzufügenden entspricht(klasse,KW,Typ)
        	if(exitWeekData.elementId.equalsIgnoreCase(shownElement)&& exitWeekData.weekId.equalsIgnoreCase(selectedDateIndex)&& exitWeekData.typeId.equalsIgnoreCase(selectedType))
        	{
        		//ja,es ist eine gleiche Woche bereits vorhanden
       			//prüfen, ob die alte syncTime älter ist als die neue
       			if(existSyncTime < weekData.syncTime)
       			{
       				weekData.isDirty=true;
       				this.stupidData.set(y, weekData);
       				return new DownloadFeedback(y,DownloadFeedback.REFRESH);
       			}
        	}
        }
        weekData.isDirty=true;
       	this.stupidData.add(weekData);	//fügt die geparste Woche den Hauptdaten hinzu
       	sort();
       	for(int y =0;y<this.stupidData.size() && dataIndex == -1;y++)
       	{
       		if(this.stupidData.get(y).equals(weekData))
       			dataIndex=y;
       	}
       	
       	this.progressDialog.setProgress(this.progressDialog.getProgress()+1000);

       	return new DownloadFeedback(dataIndex,DownloadFeedback.NO_REFRESH);
        
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
	@Deprecated 
	public void fetchTimetableFromXml(Xml xml) throws Exception {
		
		this.stupidData.clear();

	
		//xml Header auslesen und version abholen
		XmlTag[] xmlArray;
		try 
		{
			xmlArray = Xml.xmlToArray(xml);
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
					WeekData weekData = new WeekData(this);
					XmlTag timetable = xmlArray[weekNo].childTags[0];// den timetable abrufen
					weekData = convertXmlTableToWeekData(timetable);
					this.stupidData.add(weekData);
				}
	    	}
		} 
		catch (Exception e) 
		{
			throw new Exception(e);
		}
		
	}

	
	/// Datum: 19.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	erstellt einen Fingeprint für ein eindimensoinales Array 
	///	Die Methode returned einen String, der durch 0 und 1 angibt, ob das element an dieser Position die vorherigen gleicht
	///
	///	Parameter:
	///	
	/// 
	/// 
	private String fingerprintOfDubs(XmlTag[] array)
	{
		String fingerprint = "0";
		for(int x=1;x<array.length;x++)
		{
			
			if(array[x].equals(array[x-1]))
			{
				fingerprint+="1";
			}
			else
			{
				fingerprint+="0";
			}
		}
		return fingerprint;
	}
	
	// / Datum: 135.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Liefert den Index passenend zu der angegebenen KW aus den Online verfügaberen Wochen zurück
	// / Wenn online nicht verfügbar, wird -1 zurückgeliefert
	// /
	// / Parameter:
	// /
	@Deprecated
	public int getIndexFromWeekList(String weekOfYear)
	{
		for(int i=0;i<this.weekList.length;i++)
		{
			if(weekOfYear.equalsIgnoreCase(this.weekList[i].index))
				return i;
		}
		return -1;
	}
	
	// / Datum: 135.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Sucht den Index aus einem SelectOptionsArray
	// /
	// /
	// / Parameter:
	// /
	// /
	// /
	public String getIndexOfSelectorValue(SelectOptions[] array, String value) {
		for (int x = 0; x < array.length; x++) {
			if (array[x].description.equalsIgnoreCase(value)) {
				return String.valueOf(array[x].index);
			}
		}
		return "-1";
	}
	
	// / Datum: 135.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Liefert den Index der WeekData des angegebenen Datums
	// / Wenn nicht vorhanden(also bereits im Speicher geladen), wird -1 zurückgeliefert 
	// /
	// / Parameter:
	// /
	public int getIndexOfWeekData(Calendar aquiredDate)
	{
		int weekOfYear = getWeekOfYear(aquiredDate);
		for(int i=0;i<myTimetables.length;i++)
		{
			if(weekOfYear == myTimetables[i].date.get(Calendar.WEEK_OF_YEAR))
			{
				if(aquiredDate.get(Calendar.YEAR) == myTimetables[i].date.get(Calendar.YEAR))
						return i;
			}
		}
		return -1;
	}
	
	/// Datum: 05.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Sucht die letzte freie Position in einem mehrdimensionalen Array 
	///	
	///
	///	Parameter:
	///	
	/// 
	///
	private Point getLastFreePosition(WeekData weekData)
	{
		Point freeIndexPoint = new Point();
		Boolean success=false;
		for(int y=0; y < weekData.timetable.length && !success;y++)
		{
			for(int x=0; x < weekData.timetable[y].length && !success;x++)
			{
				if(weekData.timetable[y][x] == null)
				{
					freeIndexPoint.y=y;
					freeIndexPoint.x=x;
					success=true;
				}
			}
		}
		//TODO: exception wenn kein leerer eintrag gefunden wurde
		return freeIndexPoint;
	}
	
	private SelectOptions[] GetOptionsFromJavaScriptArray(XmlTag selectTag, String varName)
	{
		
		int startIndex = selectTag.dataContent.indexOf("var "+varName)+("var "+varName).length();
		startIndex = selectTag.dataContent.indexOf("[",startIndex)+1;
		int stopIndex = selectTag.dataContent.indexOf("]",startIndex);
		
		String vars = selectTag.dataContent.substring(startIndex, stopIndex);
		vars = vars.replaceAll("\"", "");
		String[] strgresult = vars.split(",");
		SelectOptions[] result = new SelectOptions[strgresult.length];
		for(int i=0;i<strgresult.length;i++)
		{
			result[i] = new SelectOptions();
			result[i].index = Integer.toString(i+1);
			result[i].description = strgresult[i];
		}
		
		
		return result;
	}
	
	private SelectOptions[] getOptionsFromSelectTag(XmlTag selectTag) throws Exception
	{
		SelectOptions[] result = new SelectOptions[selectTag.childTags.length];
		if(selectTag.childTags.length > 0)
		{
			for(int i=0; i<selectTag.childTags.length;i++)
			{
				if(selectTag.childTags[i].type.equalsIgnoreCase(Xml.OPTION))
				{
					if(selectTag.childTags[i].parameters.length > 0)
					{
						result[i] = new SelectOptions();
						result[i].description = selectTag.childTags[i].dataContent;
						result[i].index = selectTag.childTags[i].parameters[0].value;
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

	/* 5.10.12
	 * Tobias Janssen
	 * Ruft zu dem angegebenen Datum die entsprechende Kalenderwoche ab
	 * 
	 * 
	 */
	public int getWeekOfYear(Calendar aquiredDate)
	{
		Calendar calcopy = (Calendar) aquiredDate.clone();
		int weekOfYear=0;
		while(weekOfYear==0)
		{
			if(calcopy.get(Calendar.DAY_OF_WEEK)==6)
				weekOfYear=calcopy.get(Calendar.WEEK_OF_YEAR);
			else
				calcopy.setTimeInMillis(calcopy.getTimeInMillis()+(86400000*1));
		}
		return weekOfYear;
	}
	
	/// Datum: 19.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Entfernt doppelte Reihen und Spalten 
	///	
	///
	///	Parameter:
	///	
	/// 
	/// 
	private WeekData removeDubColsnRows(WeekData weekData)
	{
		XmlTag[][] tempTimeTable = new XmlTag[0][0];
		
		Boolean dub=false;
		
		//zuerst alle Zeilen prüfen, ob diese gleich der nächsten ist
		for (int y=0; y+1< weekData.timetable.length;y++)
		{
			dub=true;
			for(int x=0;x<weekData.timetable.length && dub;x++)
			{
				if(!weekData.timetable[y+1][x].equals(weekData.timetable[y][x]) && dub)
				{
					dub=false;
				}
			}
			if(!dub)
			{
				//alle nicht Dublicate werden dem neuen array hinzugefügt
				tempTimeTable=(XmlTag[][])ArrayOperations.AppendToArray(tempTimeTable, weekData.timetable[y]);
			}
		}
		tempTimeTable=(XmlTag[][])ArrayOperations.AppendToArray(tempTimeTable, weekData.timetable[weekData.timetable.length-1]);
		
		//fingerprints(strings aus 0 und 1) für jede zeile erstellen. 1 zeigt, dass dieses feld mit dem vorgänger gleich ist
		String[] print =new String[weekData.timetable.length];
		for (int y=0; y< weekData.timetable.length;y++)
		{
			print[y]=fingerprintOfDubs(weekData.timetable[y]);
		}
		
		//nun müssen die fingerprints aller Array zeilen zusammengefügt werden
		int sum=0;
		String printRes="";
		for(int x=0;x<print.length;x++)
		{
			sum=0;
			for(int y=0;y<print.length;y++)
			{
				sum+=Integer.decode(String.valueOf(print[y].charAt(x)));
			}
			if(sum!=0)
			{
				printRes+="1";
			}
			else
			{
				printRes+="0";
			}
		}
		//es ist eine fingerabdruck für ein zweidimensinales array entstanden, an hand diesem kann nun ein neues array erstellt werden, dass keine dublicate hat
		
		
		int count=0;
		//zählen der 0en für die länge einer zeile, denn diese sind kein dublicat.
		for(int x=0;x<printRes.length();x++)
		{
			if(String.valueOf(printRes.charAt(x)).equalsIgnoreCase("0"))
			{
				count++;
			}
		}
		//das neue array für das ergebnis erstellen
		weekData.timetable = new XmlTag[tempTimeTable.length][count];
		Point point = new Point();
		//das vorherige ergenis nutzen wir nun um mit hilfe des fingerprints das neue array zu füllen
		for(int y=0;y<tempTimeTable.length;y++)
		{
			for(int x=0;x<tempTimeTable[y].length;x++)
			{
				//nur 0en, also nicht dublicate hinzufügen
				if(String.valueOf(printRes.charAt(x)).equalsIgnoreCase("0"))
				{
					//das feld hinzufügen
					point = getLastFreePosition(weekData);
					System.arraycopy(tempTimeTable[y], x, weekData.timetable[point.y], point.x, 1);
				}
			}
			
		}

		return weekData;
	}
	/// Datum: 06.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Reduziert das Mehrdimensionale Array auf deren Inhalt 
	///	
	///
	///	Parameter:
	///	
	/// 
	/// 
	private WeekData removeEmtyColsnRows(WeekData weekData)
	{
		XmlTag[][] yResult = new XmlTag[0][0];
		
		//erst leere y-zeilen entfernen
		Boolean empty;
		for(int y=0;y<weekData.timetable.length;y++)
		{
			empty=true;
			//alle spalten dieser zeile durchgehen und prüfen, ob alle leer sind
			for(int x=0;x<weekData.timetable[y].length;x++)
			{
				if(weekData.timetable[y][x].dataContent != null && empty == true)
					empty = false;
			}
			
			//wenn davon eines nicht leer ist
			if(!empty)
			{
				//wird diese zeile dem ergebnis angefügt
				yResult=(XmlTag[][])ArrayOperations.AppendToArray(yResult, weekData.timetable[y]);
			}

		}
		
		XmlTag[][] xResult=new XmlTag[yResult.length][yResult[0].length];
		int lengthX;
		//jetzt alle x im yResult prüfen
		for(int x=0;x<yResult[0].length;x++)
		{
			empty=true;
			//alle zeilen dieser zeile durchgehen und prüfen, ob alle leer sind
			for(int y=0;y<yResult.length && empty;y++)
			{
				if(yResult[y][x].dataContent != null && empty == true)
					empty = false;
			}
			//wenn davon eines nicht leer ist
			if(!empty)
			{
				//hinzufügen
				lengthX = 0;
				Boolean positionFound = false;
				for(int i=0;i < xResult[0].length && !positionFound ;i++)
				{
					if(xResult[0][i] == null)
					{
						lengthX =i;
						positionFound=true;
					}
				}
				for(int y=0;y<yResult.length;y++)
				{
					System.arraycopy(yResult[y], x, xResult[y], lengthX, 1);
				}
			}
		}
		
		//herausfinden, ob das xResult noch leere Felder hat, wenn ja, wird diese posX zurückgeliefert
		lengthX = xResult[0].length;
		Boolean positionFound = false;
		for(int i=0;i < xResult[0].length && !positionFound ;i++)
		{
			if(xResult[0][i] == null)
			{
				lengthX =i;
				positionFound=true;
			}
		}
		XmlTag[][] endResult=new XmlTag[xResult.length][lengthX];
		for(int y=0;y<xResult.length;y++)
		{
			System.arraycopy(xResult[y], 0, endResult[y], 0, lengthX);
		}
		//nun noch alle Felder durchlaufen und die dataContent null mit "" ersetzten:
		for(int y=0;y < endResult.length ;y++)
		{
			for(int x=0;x<endResult[y].length;x++)
			{
				if(endResult[y][x].dataContent == null)
				{
					endResult[y][x].dataContent="";
				}
			}
			
		}
		
		weekData.timetable=endResult;
		return weekData;
	}
	
	/*	11.10.12
	 * 	Tobias Janssen
	 * 
	 * 	Sortiert die Daten nach Wochennummer
	 * 
	 * 
	 * 
	 */
	public void sort()
	{
		List <WeekData> newList = new ArrayList<WeekData>();
		int currentObj = this.stupidData.size()-1;
		int nextObj=currentObj-1;
		int weekIdCurrent;
		int weekIdNext;		
		while(this.stupidData.size() != 0)
		{
			//prüfen, ob es ein nächstes Objekt überhaupt noch gibt
			//das kleinste object heraussuchen
			currentObj=this.stupidData.size()-1;
			nextObj=this.stupidData.size()-2;

				for(int i=nextObj;i>=0;i--)
				{
					if(this.stupidData.size()==1)
					{
						nextObj=-1;
					}
					else
					{
						//pürfen, ob das nextObj größer ist als das aktuelle
						weekIdCurrent =Integer.decode(this.stupidData.get(currentObj).weekId);
						weekIdNext=Integer.decode(this.stupidData.get(i).weekId);
						if(weekIdNext > weekIdCurrent)
						{
							//das nextObj ist größer, daher wird der zeiger nun einen niedriger gesetzt
							nextObj=i;
						}
						else
						{
							//das nextObj ist kleiner, daher nehmen wir nun das Object als current
							currentObj=i;
						}
					}
				}

				//liste ist durch, ablegen
				weekIdCurrent =Integer.decode(this.stupidData.get(currentObj).weekId);
				if(nextObj != -1)
				{
					weekIdNext=Integer.decode(this.stupidData.get(nextObj).weekId);
					if(weekIdNext > weekIdCurrent)
					{
						//das nextObj ist größer, daher wird erst das currentObject abgelegt
						newList.add(this.stupidData.get(currentObj));
						this.stupidData.remove(currentObj);

						
					}
					else
					{
						//das nextObj ist kleiner, daher wird erst das nextObj abgelegt
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
	
	/// Datum: 06.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Reduziert jedes Feld auf deren Inhalt 
	///	
	///
	///	Parameter:
	///	
	/// 
	/// 
	private XmlTag SummerizeField(XmlTag field,int rndmId, XmlTag origin,XmlTag summerizedField)
	{
		XmlTag currentTag = field.tagCrawlerFindDeepestUnSumerizedChild(origin,rndmId);
			
		if(currentTag.dataContent != null)
		{
			if(summerizedField.dataContent == null)
			{
				summerizedField.dataContent= currentTag.dataContent;
			}
			else
			{
				summerizedField.dataContent+= currentTag.dataContent;
			}
		}
		//Parameter auslesen
		Boolean redundanz=false;
		for(int p=0;p<currentTag.parameters.length;p++)
		{
			redundanz=false;
			if(currentTag.parameters[p].name.equals("color"))
			{
				for(int i=0;i<summerizedField.parameters.length;i++)
				{
					if(summerizedField.parameters[i].name.equalsIgnoreCase(currentTag.parameters[p].name))
					{
						redundanz=true;
					}
				}
				if(!redundanz)
					summerizedField.addParameter(currentTag.parameters[p].name, currentTag.parameters[p].value);
			}
			
		}
		currentTag.sumerizeId = rndmId;
		//prüfen, ob es noch ein parent tag gibt, und ob dieses nicht dem ursprungs tag entspricht
		if(currentTag.parentTag != null && !currentTag.parentTag.equals(origin))
			 return SummerizeField(currentTag.parentTag,rndmId,origin,summerizedField);
		
		return summerizedField;
	}
	
	// / Datum: 135.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Erstellt ein Array mit einer Übersicht der lokal verfügbaren Timetable. Anhand der Schlüssel kann dann der richtige Datensatz aus dem Bestand abgerufen werden
	// /
	// /
	// / Parameter:
	// /
	// /
	// /
	public void timeTableIndexer() throws Exception
	{
		//Prüfen, ob ein Element ausgewählt wurde
		//Es ist für den Indexer essentiell wichtig, dass dieser festgelegt ist!
		if(myElement.equalsIgnoreCase(""))
			throw new Exception("Keine Element festgelegt.Indexer kann nicht gestartet werden!");
		
		myTimetables = new TimeTableIndex[0];
		
		//den gesamten geladenen Datenbestand durchsuchen
		for (int i = 0; i < this.stupidData.size(); i++) 
		{
			//prüfen, ob der aktuelle Datensatz dem eigenen Element entspricht
			if (this.stupidData.get(i).elementId.equalsIgnoreCase(myElement)) 
			{
				
				//wenn ja, einen neuen Eintrag dem Indexer hinzufügen
				myTimetables=(TimeTableIndex[]) ArrayOperations.AppendToArray(myTimetables, 
						new TimeTableIndex(i,this.stupidData.get(i).date,this.stupidData.get(i).syncTime));
			}
		}
	}
	
}
