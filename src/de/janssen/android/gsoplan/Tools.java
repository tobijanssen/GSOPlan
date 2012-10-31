package de.janssen.android.gsoplan;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import de.janssen.android.gsoplan.runnables.DismissProgress;
import de.janssen.android.gsoplan.runnables.Download;
import de.janssen.android.gsoplan.runnables.SaveData;
import de.janssen.android.gsoplan.runnables.SaveSetup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Tools{
	

	
	/*	11.10.12
     * 	Tobias Janssen
     * 
     * 	fügt der Liste der Pages und Headlines den übergebenen TimeTable hinzu 
     */
    public static void appendTimeTableToPager(WeekData weekData, StupidCore stupid, PlanActivity parent)
    {
    	//eine Kopie des Stundenplan-Datums erstellen
    	Calendar currentDay = new GregorianCalendar();
    	currentDay = (Calendar) weekData.date.clone();

    	//den aktuellen Wochentag abrufen
    	int currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
    	
    	//den currentDay auf Montag setzten
    	if(currentDayOfWeek > 2)
    	{
    		//1000*60*60*24 = 1 Tag!
    		currentDay.setTimeInMillis(currentDay.getTimeInMillis()-(1000*60*60*24*(currentDayOfWeek-2)));
    	}
    		
		for (int x = 1; x < weekData.timetable[0].length; x++)
		{
			List<TimetableViewObject> list = createTimetableViewObject(weekData, stupid, parent, currentDay);

			View page = createPage(weekData, parent, list);
			insertPage(parent, currentDay, page, createHeader(weekData, currentDay),0,parent.pageIndex.size());
			
			currentDay.roll(Calendar.DAY_OF_YEAR,true);
		}

	}
	
	
	/* 5.10.12
     * Tobias Janssen
     * Generiert ein SaveData Object, das dann ausgeführt werden kann
     * 
     * 
     */
    private static SaveData buildSaveData(Context context,WeekData weekData)
    {
    	File file = getFileSaveData(context,weekData);   	
		return new SaveData(context,weekData,file);
    }
    
    /* 5.10.12
     * Tobias Janssen
     * Generiert ein SaveSetup Object, das dann ausgeführt werden kann
     * 
     * 
     */
    private static SaveSetup buildSaveSetup(Context context,StupidCore stupid)
    {
    	File file =getFileSaveSetup(context,stupid);
		return new SaveSetup(context,stupid, file);
    }
    
    /*	24.10.12
	 * 	Tobias Janssen
	 * 
	 * 	Erstellt den Überschriften String 
	 * 
	 */
	private static String createHeader(WeekData weekData, Calendar currentDay)
	{
		int x = currentDay.get(Calendar.DAY_OF_WEEK)-1;
		String dayName = weekData.timetable[0][x].dataContent.replace("\n", "").substring(0, 2);
		return dayName + ", " + currentDay.get(Calendar.DAY_OF_MONTH) + "." + (currentDay.get(Calendar.MONTH)+1) + "."	+ currentDay.get(Calendar.YEAR);
	}
    
    /*	24.10.12
     * 	Tobias Janssen
     * 	Erstellt eine Seite des ViewPagers, inkl Header und Footer
     * 
     * 
     */
    private static View createPage(WeekData weekData, PlanActivity parent, List<TimetableViewObject> list)
    {
    	View page = parent.inflater.inflate(R.layout.daylayout, null);
		ListView listView = (ListView) page.findViewById(R.id.listTimetable);
		MyArrayAdapter adapter = new MyArrayAdapter(parent, list);
		listView.setAdapter(adapter);
		
		TextView syncTime = (TextView) page.findViewById(R.id. syncTime);
		Calendar sync = new GregorianCalendar();
		sync.setTimeInMillis(weekData.syncTime);
		
		String minute = String.valueOf(sync.get(Calendar.MINUTE));
		if(minute.length()==1)
			minute="0"+minute;
		
		syncTime.setText(weekData.elementId+" | Stand vom "+sync.get(Calendar.DAY_OF_MONTH)+"."+(sync.get(Calendar.MONTH)+1)+"."+sync.get(Calendar.YEAR)+" "+sync.get(Calendar.HOUR_OF_DAY)+":"+minute+" Uhr");
		return page;
    }
    
    
    /*	24.10.12
     * 	Tobias Janssen
     * 	Erstellt eine Seite des ViewPagers, inkl Header und Footer
     * 
     * 
     */
    private static List<TimetableViewObject> createTimetableViewObject(WeekData weekData, StupidCore stupid, PlanActivity parent, Calendar currentDay)
    {
    	int x = currentDay.get(Calendar.DAY_OF_WEEK)-1;
		List<TimetableViewObject> list = new ArrayList<TimetableViewObject>();

		int nullCounter = 0;
		Boolean entryFound = false;
		for (int y = 1; y < weekData.timetable.length; y++) 
		{

			if (weekData.timetable[y][x].dataContent == null && !entryFound) 
			{
				nullCounter++;
			} 
			else if (weekData.timetable[y][x].dataContent != null) 
			{
				if (weekData.timetable[y][x].dataContent.equalsIgnoreCase("null")&& !entryFound) 
				{
					nullCounter++;
				} 
				else if (weekData.timetable[y][x].dataContent.equalsIgnoreCase("")&& !entryFound) 
				{
					nullCounter++;
				}
				else 
				{
					if (y != 0)
						entryFound = true;
					if (weekData.timetable[y][x].dataContent.equalsIgnoreCase("null")) 
					{
						list.add(new TimetableViewObject(stupid.timeslots[y], "", "#000000"));
					} 
					else 
					{
						String color = weekData.timetable[y][x].getColorParameter();
						list.add(new TimetableViewObject(
								stupid.timeslots[y],
								weekData.timetable[y][x].dataContent.replaceAll(
										"\n", " "), color));
					}
				}
			}
			else 
			{
				list.add(new TimetableViewObject(stupid.timeslots[y], "","#000000"));
			}
		}
		// prüfen, ob gar keine Stunden vorhanden sind
		if (nullCounter == 15) 
		{
			list.add(new TimetableViewObject("", "kein Unterricht","#000000"));
		}
		//nun von hinten aufrollen und alle leeren Stunden entfernen
		TimetableViewObject lineObject;
		for(int i=list.size()-1;i >= 0;i--)
		{
			lineObject = list.get(i);
			if(lineObject.row2.equalsIgnoreCase(""))
				list.remove(i);
			else
				break;
		}
		return list;
    }
    
    /*	14.09.12
  	 *	Tobias Janßen
  	 *
  	 *	Lädt die Selectoren von der GSO-Seite und parsed diese in die availableOnline Arrays
  	 */ 
    public static void fetchOnlineSelectors(Context context,StupidCore stupid,Executor exec, Runnable postrun)
    {
       	try
    	{
    		//prüfen ob Datenübertragung nur über Wlan zulässig ist:
            if(stupid.onlyWlan)
            {
            	//Es dürfen Daten nur bei bestehender Wlan verbindung geladen werden
            	//Prüfen, ob Wlan verbindung besteht
            	if(Tools.isWifiConnected(context))
            	{
            		//Verbindung vorhanden
            		stupid.progressDialog = ProgressDialog.show(context, context.getString(R.string.setup_message_dlElements_title), context.getString(R.string.setup_message_dlElements_body), true,false);
	            	stupid.setupIsDirty=true;
	            	Download download = new Download(stupid,true,false);
	            	exec.execute(download);
	            	exec.execute(postrun);
            	}
            	else
                {
            		//Keine Wlan Verbindung vorhanden, Fehler-Meldung ausgeben
                	Toast.makeText(context, "Keine Wlan Verbindung!", Toast.LENGTH_SHORT).show();
                }
            }
            else
        	{
            	//Es dürfen Daten auch ohne Wlan geladen werden
            	stupid.progressDialog = ProgressDialog.show(context, context.getString(R.string.setup_message_dlElements_title), context.getString(R.string.setup_message_dlElements_body), true,false);
            	stupid.setupIsDirty=true;
            	Download download = new Download(stupid,true,false);
            	exec.execute(download);
            	exec.execute(postrun);
        	}
            
    	}
    	catch(Exception e)
    	{
    		new AlertDialog.Builder(context)
    	    .setTitle("Fehler")
    	    .setMessage(context.getString(R.string.setup_message_error_dlElements_1))
    	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) { 
    	            // continue with delete
    	        }
    	     })
    	    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) { 
    	            // do nothing
    	        }
    	     })
    	     .show();

    	}
   	}
    
    
    /* 5.10.12
     * Tobias Janssen
     * Generiert ein SaveData Object, das dann ausgeführt werden kann
     * 
     * 
     */
    private static File getFileSaveData(Context context,WeekData weekData)
    {
    	
    	String filename=Tools.getWeekOfYearToDisplay(weekData.date)+"_"+weekData.date.get(Calendar.YEAR)+"_"+Const.FILEDATA;
    	return new File(context.getFilesDir()+"/"+weekData.elementId,filename);
    }
    
    /* 5.10.12
     * Tobias Janssen
     * Generiert ein SaveData Object, das dann ausgeführt werden kann
     * 
     * 
     */
    public static File getFileSaveSetup(Context context,StupidCore stupid)
    {
    	String filename=Const.FILESETUP;
    	return new File(context.getFilesDir(),filename);
    }
    
    /*	16.10.12
     * 	Tobias Janssen
     * 
     * 	Liefert die page des angegebene Datums
     */
    public static int getPage(List<Calendar> pageIndex,Calendar currentDate)
    {
    	int currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);
		//den currentDay auf den folge Montag setzten
    	if(currentDayOfWeek < 2)
    	{
    		//1000*60*60*24 = 1 Tag!
    		currentDate.setTimeInMillis(currentDate.getTimeInMillis()+(1000*60*60*24*(2-currentDayOfWeek)));
    	}
    	if(currentDayOfWeek > 6)
    	{
    		//1000*60*60*24 = 1 Tag!
    		currentDate.setTimeInMillis(currentDate.getTimeInMillis()+(1000*60*60*24*2));
    	}
    	
    	int dayOfYearcurrent=0;
    	int dayOfYearpage=0;
    	int yearCurrent=0;
    	int yearPage=0;
    	for (int i=0;i<pageIndex.size();i++)
    	{
    		dayOfYearcurrent = currentDate.get(Calendar.DAY_OF_YEAR);
    		yearCurrent = currentDate.get(Calendar.YEAR);
    		dayOfYearpage = pageIndex.get(i).get(Calendar.DAY_OF_YEAR);
    		yearPage = pageIndex.get(i).get(Calendar.YEAR);    		
    		if((dayOfYearcurrent == dayOfYearpage)&& (yearCurrent == yearPage))
    			return i;
    	}
    	//TODO: -1 nur in allerletzter Not, ansonsten die Page ausgeben, die am nächsten liegt
    	return -1;
    	
    }
    
    /*	02.10.12
  	 *	Tobias Janßen
  	 *
  	 *	Liefert den aktuellen Wochentag.
  	 *	Wochenendtage liefern den nächsten Montag und setzen das currentDate entsprechend um
  	 */	
    @Deprecated
    public static int getSetCurrentWeekDay(Calendar date)
    {
    	int dayOfWeek=date.get(Calendar.DAY_OF_WEEK);
    	switch(dayOfWeek)
    	{
	    	case Calendar.SATURDAY:
	    		date.setTimeInMillis(date.getTimeInMillis()+(86400000*2));
	    		return Calendar.MONDAY;

	    	case Calendar.SUNDAY:
	    		date.setTimeInMillis(date.getTimeInMillis()+(86400000*1));
	    		return Calendar.MONDAY;
	    		
	    	default:
	    		return dayOfWeek;
	    		
    	}
    }

    /*	24.09.12
  	 *  Tobias Janßen
  	 *  
  	 *	Liefert die KalenderWoche des angegebenen Datums zurück
  	 */	
    private static int getWeekOfYearToDisplay(Calendar date)
    {
    	Calendar copy = (Calendar) date.clone();
    	int currentDay = copy.get(Calendar.DAY_OF_WEEK);
   		if(currentDay<5)
   		{
   			copy.setTimeInMillis(date.getTimeInMillis()+(86400000*(5-currentDay)));
   		}
   		else if(currentDay>5)
   		{
   			copy.setTimeInMillis(date.getTimeInMillis()-+(86400000*(currentDay-5)));
   		}
    	int result = 0;
	    result=copy.get(Calendar.WEEK_OF_YEAR);
	    return result;
    }
    
    
    /*	15.10.12
	 * 	Tobias Janssen
	 * 
	 * 	Fügt die Page an die richtige Position im pager an 
	 * 
	 */
	private static void insertPage(PlanActivity parent, Calendar currentDay,View page,String header,int startIndex,int stopIndex)
	{
		//prüfen, an welche stelle die page gehört
		//dazu die mitte aller bestehenden pages nehmen
		int midPos=((stopIndex-startIndex)/2)+startIndex;
		
		if(midPos == 0)
		{
			//es existiert keiner, oder max ein eintrag
			//daher prüfen, ob ein eintrag besteht
			if(parent.pageIndex.size() >=1)
			{
				//ja, einen eintrag gibt es bereits
				int pageDayOfYear = parent.pageIndex.get(midPos).get(Calendar.DAY_OF_YEAR);
				int pageYear = parent.pageIndex.get(midPos).get(Calendar.YEAR);
				
				//prüfen, ob die bestehende seite "älter" als die hinzuzufügende ist
				if(pageDayOfYear < currentDay.get(Calendar.DAY_OF_YEAR) && pageYear <= currentDay.get(Calendar.YEAR))
				{
					//die page indexieren
					parent.pageIndex.add(midPos+1,(Calendar) currentDay.clone());
					parent.pages.add(midPos+1,page);
					parent.headlines.add(midPos+1,header);
				}
				else
				{
					//die page indexieren
					parent.pageIndex.add(midPos,(Calendar) currentDay.clone());
					parent.pages.add(midPos,page);
					parent.headlines.add(midPos,header);
				}
			}
			else
			{
				//nein es ist alles leer, daher einfach einfügen
				//die page indexieren
				parent.pageIndex.add(midPos,(Calendar) currentDay.clone());
				parent.pages.add(midPos,page);
				parent.headlines.add(midPos,header);
			}
		}
		else
			{
			//daten Tag des Jahres abrufen
			int pageDayOfYear = parent.pageIndex.get(midPos).get(Calendar.DAY_OF_YEAR);
			int pageYear = parent.pageIndex.get(midPos).get(Calendar.YEAR);
				
			//prüfen, ob die bestehende seite "älter" als die hinzuzufügende ist
			if(pageDayOfYear < currentDay.get(Calendar.DAY_OF_YEAR) && pageYear <= currentDay.get(Calendar.YEAR))
			{
				//ja, ist älter, daher muss die page auf jeden fall dahinder eingefügt werden
				//prüfen, ob direkte nachbarschaft besteht
				//dazu erstmal prüfen, ob der nächste nachbar überhaupt existiert
				if(midPos+1 >= parent.pageIndex.size())
				{
					//existiert gar keiner mehr; daher page hinzufügen 
	
					//die page indexieren
					parent.pageIndex.add(midPos+1,(Calendar) currentDay.clone());
					parent.pages.add(midPos+1,page);
					parent.headlines.add(midPos+1,header);
				}
				else
				{
					//es ist ein nachbar vorhanden
					//prüfen, ob dieser näher dran liegt als die currentPage
					if(parent.pageIndex.get(midPos+1).get(Calendar.DAY_OF_YEAR)< currentDay.get(Calendar.DAY_OF_YEAR))
					{
						//ja alte page ist ein näherer nachbar
						insertPage(parent, currentDay,page,header,midPos,stopIndex);
					}
					else
					{
						//nein, currentPage ist näher
						//also dazwischen einfügen
						//die page indexieren
						parent.pageIndex.add(midPos+1,(Calendar) currentDay.clone());
						parent.pages.add(midPos+1,page);
						parent.headlines.add(midPos+1,header);
						
					}
				}
				
			}
			else
			{
				//nein,die bestehende seite ist hat ein jüngers Datum als die hinzuzufügende, daher muss die neue page auf jeden fall davor eingefügt werden
				
				if(midPos == 0)
				{
					//existiert gar kein eintrag; daher page hinzufügen 
	
					//die page indexieren
					parent.pageIndex.add((Calendar) currentDay.clone());
					parent.pages.add(page);
					parent.headlines.add(header);
				}
				else
				{
					//prüfen, ob der vorgänger Nachbar kleiner ist
					if(parent.pageIndex.get(midPos-1).get(Calendar.DAY_OF_YEAR)< currentDay.get(Calendar.DAY_OF_YEAR) && parent.pageIndex.get(midPos-1).get(Calendar.YEAR)< currentDay.get(Calendar.YEAR))
					{
						//ja davorige page ist kleiner
						//also dazwischen einfügen
						//die page indexieren
						parent.pageIndex.add(midPos,(Calendar) currentDay.clone());
						parent.pages.add(midPos,page);
						parent.headlines.add(midPos,header);
						
					}
					else
					{
						insertPage(parent, currentDay,page,header,0,midPos);
					}
				}
				
				//insertPage(parent, currentDay,listView,header,midPos,stopIndex);
			}
	
		}

		
		
		
	}
    
    /*	25.9.12
	 * 	Tobias Janssen
	 *  
	 *  Prüft, ob eine Wlan verbindung besteht, und liefert das Ergebnis
	 */
	public static Boolean isWifiConnected(Context context)
	{
    	WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	WifiInfo wifiinfo = wifi.getConnectionInfo();
        
       
        if(wifiinfo.getNetworkId() == -1)
        {
        	return false;
        }
        else
        	return true;
	}
    
    /* 11.10.12
     * Tobias Janssen
     * Lädt alle verfügbaren Daten-Datein
     * 
     * 
     */
    public static void loadAllDataFiles(Context context,StupidCore stupid)
    {
    	File elementDir = new java.io.File(context.getFilesDir()+"/"+stupid.myElement);
    	File[] files = elementDir.listFiles();
        for(int f=0;f<files.length;f++)
        {
        	loadNAppendFile(context,stupid, files[f]);
        }
    	
    }
    
    /*	12.10.12
     * 	Tobias Janssen
     * 
     * 	Lädt den angegebenen File und hängt diesen an die Daten im StupidCore an
     * 
     */
    private static void loadNAppendFile(Context context,StupidCore stupid, File file)
    {
    	
    	try 
    	{
    		Xml xml = new Xml();
			xml.container = FileOPs.readFromFile(context,file);
			WeekData[] weekData = xml.convertXmlToStupid(xml);
			if(weekData.length >0)
				stupid.stupidData.add(weekData[0]);
		} 
    	catch (Exception e) 
    	{
			// TODO Exception loadNappendFile 
			e.printStackTrace();
		}
		
    }


	/*	12.10.12
	 * 	Tobias Janssen
	 * 
	 * 	erst in der Liste der Pages und Headlines den übergebenen TimeTable 
	 */
	public static void replaceTimeTableInPager(WeekData weekData, StupidCore stupid, PlanActivity parent)
	{
		Calendar currentDay = new GregorianCalendar();
		currentDay = (Calendar) weekData.date.clone();
		int currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
		while(currentDayOfWeek!=2)
		{
			currentDay.roll(Calendar.DAY_OF_YEAR, false);
		}
			
		for (int x = 1; x < weekData.timetable[0].length; x++)
		{
			List<TimetableViewObject> list = createTimetableViewObject(weekData, stupid, parent, currentDay);
			View page = createPage(weekData, parent, list);
			String header = createHeader(weekData, currentDay);
			
			//location suchen
			int location=0;
			for(int i=0;i<parent.headlines.size();i++)
			{
				if(parent.headlines.get(i).equals(header))
					location=i;
			}
			parent.pages.set(location, page);
			parent.headlines.set(location,header);
	
			currentDay.roll(Calendar.DAY_OF_YEAR,true);
		}
	}
	
	/* 	2.10.12
  	 *	Tobias Janßen
  	 *
  	 *	Prüft, ob welche Daten im StupidCore dirty sind, und speichert diese
  	 */ 
    public static void saveFiles(Context context,StupidCore stupid,ExecutorService exec ) throws Exception
    {
    	
		SaveSetup saveSetup = buildSaveSetup(context,stupid);
		SaveData saveData;
    	
    	if(stupid.setupIsDirty)
    	{
    		exec.execute(saveSetup); 
    	}
    	if(saveSetup.exception!=null)
		{
			throw saveSetup.exception;
		}
    	
    	
    	WeekData weekData;
    	for(int d=0;d<stupid.stupidData.size();d++)
    	{
    		weekData = stupid.stupidData.get(d);
    		saveData = buildSaveData(context,weekData);
    		if(weekData.isDirty)
    		{
    			exec.execute(saveData);
    			if(saveData.exception!=null)
    			{
    				throw saveData.exception;
    			}
    		}
    		stupid.dataIsDirty=false;
    	}
	
    }
	
	/* 	14.09.12
  	 *	Tobias Janßen
  	 *
  	 *	Speichert den aktuellen StupidCore
  	 */	
    public static void saveFilesWithProgressDialog(Context context,StupidCore stupid,ExecutorService exec, Calendar currentDate )
    {
    	//ProgressDialog initialisieren
    	
    	stupid.progressDialog =  new ProgressDialog(context);
    	stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	stupid.progressDialog.setMessage(context.getString(R.string.msg_saving));
    	stupid.progressDialog.setCancelable(false);
    	stupid.progressDialog.setProgress(0);
		
		//Prüfen, welche Aufgaben zu erledigen sind, dementsprechend den maximal Wert einstellen
    	if(stupid.elementList.length>0 && stupid.setupIsDirty)
    	{
    		stupid.progressDialog.setMax(stupid.elementList.length+stupid.weekList.length+50);
    	}
    	int dataToSaveCounter=0;
    	for(int d=0;d<stupid.stupidData.size();d++)
    	{
    		if(stupid.stupidData.get(d).isDirty)
    		{
    			dataToSaveCounter++;
    		}
    	}
    	if(dataToSaveCounter>0)
    	{
    		stupid.progressDialog.setMax(stupid.progressDialog.getMax()+dataToSaveCounter*
    				(stupid.stupidData.get(0).timetable.length*stupid.stupidData.get(0).timetable[0].length+stupid.stupidData.get(0).timetable.length)
    				+dataToSaveCounter);
    	}
    	
    	stupid.progressDialog.show();

   		
    	if(stupid.setupIsDirty)
    	{
    		SaveSetup saveSetup = buildSaveSetup(context,stupid);
    		exec.execute(saveSetup);    		
    	}
    	
    	SaveData saveData;
    	WeekData weekData;
    	for(int d=0;d<stupid.stupidData.size();d++)
    	{
    		weekData = stupid.stupidData.get(d);
    		saveData = buildSaveData(context,weekData);
    		if(weekData.isDirty)
    			exec.execute(saveData);
    	}
    	exec.execute(new DismissProgress(stupid));
    }
	
	/* 	14.09.12
  	 *	Tobias Janßen
  	 *
  	 *	Speichert das aktuelle StupidCore-Setup ohne ProgressDialog
  	 */ 
    public static void saveSetup(Context context,StupidCore stupid,ExecutorService exec)
    {
    	if(stupid.setupIsDirty)
    	{
    		SaveSetup saveSetup = buildSaveSetup(context,stupid);
    		exec.execute(saveSetup);    		
    	}
    }

    /* 	14.09.12
  	 * 	Tobias Janßen
  	 *
  	 *	Speichert das aktuelle StupidCore-Setup mit einem ProgressDialog
  	 */	
    public static void saveSetupWithProgressDialog(Context context,StupidCore stupid,ExecutorService exec)
    {
    	//ProgressDialog initialisieren
    	stupid.progressDialog =  new ProgressDialog(context);
    	stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	stupid.progressDialog.setMessage(context.getString(R.string.msg_saving));
    	stupid.progressDialog.setCancelable(false);
    	stupid.progressDialog.setProgress(0);
		
		//Prüfen, welche Aufgaben zu erledigen sind, dementsprechend den maximal Wert einstellen
    	if(stupid.setupIsDirty)
    	{
    		stupid.progressDialog.setMax(stupid.elementList.length+stupid.weekList.length+50);
    	}
    	stupid.progressDialog.show();

   		
    	if(stupid.setupIsDirty)
    	{
    		SaveSetup saveSetup = buildSaveSetup(context,stupid);
    		exec.execute(saveSetup);    		
    	}

    	if(!stupid.setupIsDirty)
    	{
    		stupid.progressDialog.dismiss();
    	}
   		
    }
    
    
    
}
