package de.janssen.android.gsoplan;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import de.janssen.android.gsoplan.runnables.DismissProgress;
import de.janssen.android.gsoplan.runnables.Download;
import de.janssen.android.gsoplan.runnables.SaveData;
import de.janssen.android.gsoplan.runnables.SaveSetup;
import de.janssen.android.gsoplan.runnables.ShowProgressDialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Tools{
	

	
	/*	11.10.12
     * 	Tobias Janssen
     * 
     * 	fügt der Liste der Pages und Headlines den übergebenen TimeTable hinzu 
     */
    public static void appendTimeTableToPager(WeekData weekData, MyContext ctxt )
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
    	if(ctxt.weekView)
		{
			//List<TimetableViewObject> list = createTimetableWeekViewObject(weekData, ctxt, currentDay);
			
			View page = createWeekPage(weekData, ctxt);
			insertWeekPage(ctxt, currentDay, page, createWeekHeader(weekData, currentDay),0,ctxt.pageIndex.size());
			
			//currentDay.roll(Calendar.WEEK_OF_YEAR,true);
			currentDay.setTimeInMillis(currentDay.getTimeInMillis()+86400000);
			
		}
		else
		{
			for (int x = 1; x < weekData.timetable[0].length; x++)
			{
				
				
					List<TimetableViewObject> list = createTimetableDayViewObject(weekData, ctxt, currentDay);
		
					View page = createPage(weekData, ctxt, list);
					insertDayPage(ctxt, currentDay, page, createDayHeader(weekData, currentDay),0,ctxt.pageIndex.size());
					
					//currentDay.roll(Calendar.DAY_OF_YEAR,1);
					currentDay.setTimeInMillis(currentDay.getTimeInMillis()+86400000);
				
			}
		}

	}
	  
	
	/* 5.10.12
     * Tobias Janssen
     * Generiert ein SaveData Object, das dann ausgeführt werden kann
     * 
     * 
     */
    private static SaveData buildSaveData(MyContext ctxt,WeekData weekData)
    {
    	File file = getFileSaveData(ctxt,weekData);   	
		return new SaveData(weekData,file);
    }
    
    /* 5.10.12
     * Tobias Janssen
     * Generiert ein SaveSetup Object, das dann ausgeführt werden kann
     * 
     * 
     */
    private static SaveSetup buildSaveSetup(MyContext ctxt)
    {
    	File file = getFileSaveSetup(ctxt);
		return new SaveSetup(ctxt, file);
    }
    
    /*	24.10.12
	 * 	Tobias Janssen
	 * 
	 * 	Erstellt den Überschriften String 
	 * 
	 */
	private static String createDayHeader(WeekData weekData, Calendar currentDay)
	{
		int x = currentDay.get(Calendar.DAY_OF_WEEK)-1;
		String dayName ="";
		if(weekData.timetable[0][x].dataContent.length() > 3)
		{
			dayName = weekData.timetable[0][x].dataContent.replace("\n", "").substring(0, 2);
		}
		return dayName + ", " + currentDay.get(Calendar.DAY_OF_MONTH) + "." + (currentDay.get(Calendar.MONTH)+1) + "."	+ currentDay.get(Calendar.YEAR);
	}
    
    /*	29.11.12
	 * 	Tobias Janssen
	 * 
	 * 	Erstellt den Überschriften String 
	 * 
	 */
	private static String createWeekHeader(WeekData weekData, Calendar currentWeek)
	{
		int firstDay = currentWeek.get(Calendar.DAY_OF_MONTH);
		Calendar cal = (Calendar) currentWeek.clone();
		//den aktuellen Wochentag abrufen
    	int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
    	
    	//den currentDay auf Montag setzten
    	if(currentDayOfWeek < 6)
    	{
    		//1000*60*60*24 = 1 Tag!
    		cal.setTimeInMillis(cal.getTimeInMillis()+(1000*60*60*24*(6-currentDayOfWeek)));
    	}		
    	int lastDay = cal.get(Calendar.DAY_OF_MONTH);
		return firstDay+"."+(currentWeek.get(Calendar.MONTH)+1)+" - "+lastDay+"."+(cal.get(Calendar.MONTH)+1) + "."	+ cal.get(Calendar.YEAR);
	}
	
    /*	24.10.12
     * 	Tobias Janssen
     * 	Erstellt eine Seite des ViewPagers, inkl Header und Footer
     * 
     * 
     */
    private static View createPage(WeekData weekData, MyContext ctxt, List<TimetableViewObject> list)
    {
    	//TODO execption: issue #7: HTC One kann das Layout nicht inflaten:
    	//Wahrscheinlich ein Handler Problem gewesen, ist mitlerweile wahrscheinlich zufällig behoben
    	//android.view.InflateException: Binary XML file line #4: Error inflating class android.widget.ListView
    	View page = ctxt.inflater.inflate(R.layout.daylayout, null);
    	
		ListView listView = (ListView) page.findViewById(R.id.listTimetable);
		MyArrayAdapter adapter = new MyArrayAdapter(ctxt, list);
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
    
    /*	02.12.12
     * 	Tobias Janssen
     * 	Erstellt eine Stundeplan Seite des ViewPagers, inkl Header und Footer
     * 	Hier wird die Wochenansicht generiert
     * 
     */
    private static View createWeekPage(WeekData weekData, MyContext ctxt)
    {
    	//in die Page kommen alle Elemente dieser Ansicht
    	View page = ctxt.inflater.inflate(R.layout.weeklayout, null);
    	
		TableLayout tl = (TableLayout) page.findViewById(R.id.weekTimetable);
		LinearLayoutBordered ll = new LinearLayoutBordered(ctxt.context);
		
		//Tagesüberschrift erstellen:
		TableRow tr = new TableRow(ctxt.context);
			
		for(int x=0;x<weekData.timetable[0].length;x++)
		{
			//einen neuen Rahmen für das Tabellenfeld vorbereiten
			ll = new LinearLayoutBordered(ctxt.context);
			ll.setBorderRight(true);
			ll.setBorderBottom(true);
			ll.setBorderTop(true);
			ll.setBorderSize(1);
			ll.setBackgroundColor(Color.WHITE);
			
			View textview = ctxt.inflater.inflate(R.layout.textview, null);
			TextView tv = (TextView) textview.findViewById(R.id.textview);
			//Überschriftentextgröße einstellen
			
			tv.setTextSize(ctxt.textSizes[0]);
			if(x==0)
			{
				tv.setText(ctxt.stupid.timeslots[0]);
				tv.setTextColor(Color.parseColor("#3A599A"));
					
			}
			else
			{
			    if(weekData.timetable[0][x].dataContent!=null)
			    {
			       	String colorString = weekData.timetable[0][x].getColorParameter();
			       	tv.setTextColor(Color.parseColor(colorString));
			       	tv.setText(weekData.timetable[0][x].dataContent.replace("\n", " ")+"\t");
			    }
			}
			ll.addView(tv);
		    tr.addView(ll);
		}
		tl.addView(tr);
		
		int start=1;
		Boolean rowIsEmpty=true;
		//herausfinden ab wann die stunden beginnen, dies nur durchführen, wenn leestunden entfernt werden sollen
		if(ctxt.stupid.hideEmptyHours)
		{
			for(int y=start;y<weekData.timetable.length && rowIsEmpty;y++)
			{
				for(int x=1;x<weekData.timetable[y].length && rowIsEmpty;x++)
				{
					if(weekData.timetable[y][x].dataContent == null || weekData.timetable[y][x].dataContent.equalsIgnoreCase("null") || weekData.timetable[y][x].dataContent.equalsIgnoreCase(""))
			        {
						
			        }
					else
					{
						rowIsEmpty=false;
					}
				}
				if(rowIsEmpty)
					start=y+1;
			}
		}
		int stop=weekData.timetable.length-1;
		rowIsEmpty=true;
		//herausfinden ab wann die stunden beginnen, dies nur durchführen, wenn leestunden entfernt werden sollen
		if(ctxt.stupid.hideEmptyHours)
		{
			for(int y=stop;y>0 && rowIsEmpty;y--)
			{
				for(int x=1;x<weekData.timetable[y].length && rowIsEmpty;x++)
				{
					if(weekData.timetable[y][x].dataContent == null || weekData.timetable[y][x].dataContent.equalsIgnoreCase("null") || weekData.timetable[y][x].dataContent.equalsIgnoreCase(""))
			        {
						
			        }
					else
					{
						rowIsEmpty=false;
					}
				}
				if(rowIsEmpty)
					stop=y;
			}
		}
		if(stop == 0)	//Stundeplan ist leer
		{
			stop=weekData.timetable.length-1;
		}
		
		
		
		if(start == weekData.timetable.length)	//Stundeplan ist leer
		{
			start=1;
		}
		for(int y=start;y<=stop;y++)
		{
			tr = new TableRow(ctxt.context);
			
			for(int x=0;x<weekData.timetable[y].length;x++)
			{
				ll = new LinearLayoutBordered(ctxt.context);
				ll.setBorderRight(true);
				ll.setBorderBottom(true);
				ll.setBorderSize(1);
				ll.setBackgroundColor(Color.WHITE);
				View textview = ctxt.inflater.inflate(R.layout.textview, null);
				TextView tv = (TextView) textview.findViewById(R.id.textview);
				//TextView tv = new TextView(ctxt.context);
				
				tv.setTextSize(10);
				if(x==0)
				{
					tv.setText(ctxt.stupid.timeslots[y]);
					tv.setTextColor(Color.parseColor("#3A599A"));
					
				}
				else
				{
			        if(weekData.timetable[y][x].dataContent!=null)
			        {
			        	String colorString = weekData.timetable[y][x].getColorParameter();
			        	tv.setTextColor(Color.parseColor(colorString));
			        	tv.setText(weekData.timetable[y][x].dataContent.replace("\n", " ")+"\t");
			        }
				}
				ll.addView(tv);
			    tr.addView(ll);
			}
			tl.addView(tr);
		}
	
		
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
    private static List<TimetableViewObject> createTimetableDayViewObject(WeekData weekData, MyContext ctxt, Calendar currentDay)
    {
    	StupidCore stupid = ctxt.stupid;
    	int x = currentDay.get(Calendar.DAY_OF_WEEK)-1;
		List<TimetableViewObject> list = new ArrayList<TimetableViewObject>();

		int nullCounter = 0;
		Boolean entryFound = false;
		for (int y = 1; y < weekData.timetable.length; y++) 
		{

			if (weekData.timetable[y][x].dataContent == null && !entryFound && stupid.hideEmptyHours) 
			{
				nullCounter++;
			} 
			else if (weekData.timetable[y][x].dataContent != null) 
			{
				if (weekData.timetable[y][x].dataContent.equalsIgnoreCase("null")&& !entryFound && stupid.hideEmptyHours) 
				{
					nullCounter++;
				} 
				else if (weekData.timetable[y][x].dataContent.equalsIgnoreCase("")&& !entryFound && stupid.hideEmptyHours) 
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
		
		if(!stupid.hideEmptyHours)
		{
			// prüfen, ob gar keine Stunden vorhanden sind
			for(int i=0;i<list.size();i++)
			{
				if(list.get(i).row2.equalsIgnoreCase(""))
					nullCounter++;
			}
		}
		
		// prüfen, ob gar keine Stunden vorhanden sind
		if (nullCounter == 15) 
		{
			list.clear();
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
    public static void fetchOnlineSelectors(MyContext ctxt,Runnable postRun)
    {
    	StupidCore stupid = ctxt.stupid;
    	
       	try
    	{
    		//prüfen ob Datenübertragung nur über Wlan zulässig ist:
            if(stupid.onlyWlan)
            {
            	//Es dürfen Daten nur bei bestehender Wlan verbindung geladen werden
            	//Prüfen, ob Wlan verbindung besteht
            	if(Tools.isWifiConnected(ctxt.context))
            	{
            		//Verbindung vorhanden
            		stupid.progressDialog = ProgressDialog.show(ctxt.context, ctxt.context.getString(R.string.setup_message_dlElements_title), ctxt.context.getString(R.string.setup_message_dlElements_body), true,false);
	            	stupid.setupIsDirty=true;
	            	Download download = new Download(stupid,true,false,postRun);
	            	ctxt.executor.execute(download,true);
            	}
            	else
                {
            		//Keine Wlan Verbindung vorhanden, Fehler-Meldung ausgeben
                	Toast.makeText(ctxt.context, "Keine Wlan Verbindung!", Toast.LENGTH_SHORT).show();
                }
            }
            else
        	{
            	//Es dürfen Daten auch ohne Wlan geladen werden
            	stupid.progressDialog = ProgressDialog.show(ctxt.context, ctxt.context.getString(R.string.setup_message_dlElements_title), ctxt.context.getString(R.string.setup_message_dlElements_body), true,false);
            	stupid.setupIsDirty=true;
            	Download download = new Download(stupid,true,false,postRun);
            	ctxt.executor.execute(download,true);
        	}
            
    	}
    	catch(Exception e)
    	{
    		new AlertDialog.Builder(ctxt.context)
    	    .setTitle("Fehler")
    	    .setMessage(ctxt.context.getString(R.string.setup_message_error_dlElements_1))
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
    private static File getFileSaveData(MyContext ctxt,WeekData weekData)
    {
    	
    	String filename=Tools.getWeekOfYearToDisplay(weekData.date)+"_"+weekData.date.get(Calendar.YEAR)+"_"+Const.FILEDATA;
    	return new File(ctxt.context.getFilesDir()+"/"+weekData.elementId,filename);
    }
    
    /* 5.10.12
     * Tobias Janssen
     * Generiert ein SaveData Object, das dann ausgeführt werden kann
     * 
     * 
     */
    public static File getFileSaveSetup(MyContext ctxt)
    {
    	String filename=Const.FILESETUP;
    	return new File(ctxt.context.getFilesDir(),filename);
    }
    
    /*	16.10.12
     * 	Tobias Janssen
     * 
     * 	Liefert den pageindex des angegebenen Datums und des Angegebenen Feldes
     */
    public static int getPage(List<Calendar> pageIndex,Calendar currentDate, int calendarField)
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
    	int weekOfYearcurrent=0;
    	int dayOfYearpage=0;
    	int weekOfYearpage=0;
    	int yearCurrent=0;
    	int yearPage=0;
    	
    	int nextPage=0;
    	//alle Seiten des Pages durchlaufen und das Datum abfragen und mit dem gewünschten Datum vergleichen
    	for (int i=0;i<pageIndex.size();i++)
    	{
    		dayOfYearcurrent = currentDate.get(Calendar.DAY_OF_YEAR);
    		weekOfYearcurrent = currentDate.get(Calendar.WEEK_OF_YEAR);
    		yearCurrent = currentDate.get(Calendar.YEAR);
    		dayOfYearpage = pageIndex.get(i).get(Calendar.DAY_OF_YEAR);
    		weekOfYearpage = pageIndex.get(i).get(Calendar.WEEK_OF_YEAR);
    		yearPage = pageIndex.get(i).get(Calendar.YEAR);
    		
    		
    		//prüfen auf was getestet werden soll(wochenansicht, oder tagesansicht)
    		if(calendarField == Calendar.DAY_OF_YEAR)
    		{
    			//Tagesansicht
    			//Issue #12(wenn Seite nicht existiert, wird immer der erste Tag angezeigt) behoben 
    			if(yearPage < yearCurrent)
    			{
    				nextPage = i;
    				if(dayOfYearpage < dayOfYearcurrent)
    					nextPage = i;
    			}
    			if((dayOfYearcurrent == dayOfYearpage)&& (yearCurrent == yearPage))
    				return i;
    		}
    		else if(calendarField == Calendar.WEEK_OF_YEAR)
    		{
    			//Wochenansicht
    			//Issue #12(wenn Seite nicht existiert, wird immer der erste Tag angezeigt) behoben
    			if(yearPage < yearCurrent)
    			{
    				nextPage = i;
    				if(dayOfYearpage < dayOfYearcurrent)
    					nextPage = i;
    			}
    			if((weekOfYearcurrent == weekOfYearpage)&& (yearCurrent == yearPage))
    				return i;
    		}
    	}
    	
    	//dies kommt nur vor, wenn die Seite nicht gefunden wurde. dann wird die nächst kleinere Seite zurückgeliefert
    	return nextPage;
    	
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
	private static void insertWeekPage(MyContext ctxt, Calendar currentWeek,View page,String header,int startIndex,int stopIndex)
	{
		//prüfen, an welche stelle die page gehört
		//dazu die mitte aller bestehenden pages nehmen
		int midPos=((stopIndex-startIndex)/2)+startIndex;
		
		if(midPos == 0)
		{
			//es existiert keiner, oder max ein eintrag
			//daher prüfen, ob ein eintrag besteht
			if(ctxt.pageIndex.size() >=1)
			{
				//ja, einen eintrag gibt es bereits
				int pageDate = calcIntYearDay(ctxt.pageIndex.get(midPos));
				int currentDate = calcIntYearDay(currentWeek);
				
				//prüfen, ob die bestehende seite "älter" als die hinzuzufügende ist
				if(pageDate < currentDate)
				{
					//die page indexieren
					ctxt.pageIndex.add(midPos+1,(Calendar) currentWeek.clone());
					ctxt.pages.add(midPos+1,page);
					ctxt.headlines.add(midPos+1,header);
				}
				else
				{
					//die page indexieren
					ctxt.pageIndex.add(midPos,(Calendar) currentWeek.clone());
					ctxt.pages.add(midPos,page);
					ctxt.headlines.add(midPos,header);
				}
			}
			else
			{
				//nein es ist alles leer, daher einfach einfügen
				//die page indexieren
				ctxt.pageIndex.add(midPos,(Calendar) currentWeek.clone());
				ctxt.pages.add(midPos,page);
				ctxt.headlines.add(midPos,header);
			}
		}
		else
			{
			
			int pageDate = calcIntYearDay(ctxt.pageIndex.get(midPos));
			int currentDate = calcIntYearDay(currentWeek);
				
			//prüfen, ob die bestehende seite "älter" als die hinzuzufügende ist
			if(pageDate < currentDate)
			{
				//ja, ist älter, daher muss die page auf jeden fall dahinder eingefügt werden
				//prüfen, ob direkte nachbarschaft besteht
				//dazu erstmal prüfen, ob der nächste nachbar überhaupt existiert
				if(midPos+1 >= ctxt.pageIndex.size())
				{
					//existiert gar keiner mehr; daher page hinzufügen 
	
					//die page indexieren
					ctxt.pageIndex.add(midPos+1,(Calendar) currentWeek.clone());
					ctxt.pages.add(midPos+1,page);
					ctxt.headlines.add(midPos+1,header);
				}
				else
				{
					//es ist ein nachbar vorhanden
					int pageNeighborDate = calcIntYearDay(ctxt.pageIndex.get(midPos+1));
					//prüfen, ob dieser näher dran liegt als die currentPage
					if(pageNeighborDate < currentDate)
					{
						//ja alte page ist ein näherer nachbar
						insertWeekPage(ctxt, currentWeek,page,header,midPos,stopIndex);
					}
					else
					{
						//nein, currentPage ist näher
						//also dazwischen einfügen
						//die page indexieren
						ctxt.pageIndex.add(midPos+1,(Calendar) currentWeek.clone());
						ctxt.pages.add(midPos+1,page);
						ctxt.headlines.add(midPos+1,header);
						
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
					ctxt.pageIndex.add((Calendar) currentWeek.clone());
					ctxt.pages.add(page);
					ctxt.headlines.add(header);
				}
				else
				{
					//prüfen, ob der vorgänger Nachbar kleiner ist
					int pageNeighborDate = calcIntYearDay(ctxt.pageIndex.get(midPos-1));
					
					if(pageNeighborDate < currentDate)
					{
						//ja davorige page ist kleiner
						//also dazwischen einfügen
						//die page indexieren
						ctxt.pageIndex.add(midPos,(Calendar) currentWeek.clone());
						ctxt.pages.add(midPos,page);
						ctxt.headlines.add(midPos,header);
						
					}
					else
					{
						insertWeekPage(ctxt, currentWeek,page,header,0,midPos);
					}
				}
			}
	
		}

		
		
		
	}
    
	/* 11.12.12
	* Tobias Janssen
	* 
	* Rechnet das Datum einer PageIndex zu einem Vergleichbaren Wert
	*/ 
	public static int calcIntYearDay(Calendar calendar)
	{
		 return (calendar.get(Calendar.YEAR)*1000)+calendar.get(Calendar.DAY_OF_YEAR); 
	}
	 
	/* 11.12.12
	 * Tobias Janssen
	 * 
	 * Rechnet das Datum einer PageIndex zu einem Vergleichbaren Wert
	 */ 
	public static int calcIntYearWeek(Calendar calendar)
	{
		 return (calendar.get(Calendar.YEAR)*1000)+calendar.get(Calendar.WEEK_OF_YEAR); 
	}
	
	
	
    /*	15.10.12
	 * 	Tobias Janssen
	 * 
	 * 	Fügt die Page an die richtige Position im pager an 
	 * 
	 */
	private static void insertDayPage(MyContext ctxt, Calendar currentDay,View page,String header,int startIndex,int stopIndex)
	{
		
		//prüfen, an welche stelle die page gehört
		//dazu die mitte aller bestehenden pages nehmen
		int midPos=((stopIndex-startIndex)/2)+startIndex;

		if(midPos == 0)
		{
			//es existiert keiner, oder max ein eintrag
			//daher prüfen, ob ein eintrag besteht
			if(ctxt.pageIndex.size() >=1)
			{
				//ja, einen eintrag gibt es bereits
				int pageDate = calcIntYearDay(ctxt.pageIndex.get(midPos));
				int currentDate = calcIntYearDay(currentDay);
		
				//prüfen, ob die bestehende seite "älter" als die hinzuzufügende ist
				if(pageDate < currentDate)
				{
					//die page indexieren
					ctxt.pageIndex.add(midPos+1,(Calendar) currentDay.clone());
					ctxt.pages.add(midPos+1,page);
					ctxt.headlines.add(midPos+1,header);
				}
				else
				{
					//die page indexieren
					ctxt.pageIndex.add(midPos,(Calendar) currentDay.clone());
					ctxt.pages.add(midPos,page);
					ctxt.headlines.add(midPos,header);
				}
			}
			else
			{
				//nein es ist alles leer, daher einfach einfügen
				//die page indexieren
				ctxt.pageIndex.add(midPos,(Calendar) currentDay.clone());
				ctxt.pages.add(midPos,page);
				ctxt.headlines.add(midPos,header);
			}
		}
		else
		{
			//daten Tag des Jahres abrufen
			int pageDate = calcIntYearDay(ctxt.pageIndex.get(midPos));
			int currentDate = calcIntYearDay(currentDay);

			//prüfen, ob die bestehende seite "älter" als die hinzuzufügende ist
			if(pageDate < currentDate)
			{
				//ja, ist älter, daher muss die page auf jeden fall dahinder eingefügt werden
				//prüfen, ob direkte nachbarschaft besteht
				//dazu erstmal prüfen, ob der nächste nachbar überhaupt existiert
				if(midPos+1 >= ctxt.pageIndex.size())
				{
					//existiert gar keiner mehr; daher page hinzufügen
	
					//die page indexieren
					ctxt.pageIndex.add(midPos+1,(Calendar) currentDay.clone());
					ctxt.pages.add(midPos+1,page);
					ctxt.headlines.add(midPos+1,header);
				}
				else
				{
					//es ist ein nachbar vorhanden
					//prüfen, ob dieser näher dran liegt als die currentPage
					int pageNeighborDate = calcIntYearDay(ctxt.pageIndex.get(midPos+1));
					if(pageNeighborDate < currentDate)
					{
						//ja alte page ist ein näherer nachbar
						insertDayPage(ctxt, currentDay,page,header,midPos,stopIndex);
					}
					else
					{
						//nein, currentPage ist näher
						//also dazwischen einfügen
						//die page indexieren
						ctxt.pageIndex.add(midPos+1,(Calendar) currentDay.clone());
						ctxt.pages.add(midPos+1,page);
						ctxt.headlines.add(midPos+1,header);
			
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
					ctxt.pageIndex.add((Calendar) currentDay.clone());
					ctxt.pages.add(page);
					ctxt.headlines.add(header);
				}
				else
				{
					//prüfen, ob der vorgänger Nachbar kleiner ist
					int pageNeighborDate = calcIntYearDay(ctxt.pageIndex.get(midPos-1));
					if(pageNeighborDate < currentDate)
					{
						//ja davorige page ist kleiner
						//also dazwischen einfügen
						//die page indexieren
						ctxt.pageIndex.add(midPos,(Calendar) currentDay.clone());
						ctxt.pages.add(midPos,page);
						ctxt.headlines.add(midPos,header);
	
					}
					else
					{
						insertDayPage(ctxt, currentDay,page,header,0,midPos);
					}
				}
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
    public static void loadAllDataFiles(Context context,StupidCore stupid) throws Exception
    {
    	try
    	{
	    	File elementDir = new java.io.File(context.getFilesDir()+"/"+stupid.myElement);
	    	File[] files = elementDir.listFiles();
	        for(int f=0;f<files.length;f++)
	        {
	        	loadNAppendFile(context,stupid, files[f]);
	        }
    	}
    	catch (Exception e) 
    	{
			throw e;
		}
    	
    }
    
    /*	12.10.12
     * 	Tobias Janssen
     * 
     * 	Lädt den angegebenen File und hängt diesen an die Daten im StupidCore an
     * 
     */
    private static void loadNAppendFile(Context context,StupidCore stupid, File file) throws Exception
    {
    	
    	try 
    	{
    		Xml xml = new Xml();
			xml.container = FileOPs.readFromFile(file);
			WeekData[] weekData = xml.convertXmlToStupid(xml);
			if(weekData.length >0)
				stupid.stupidData.add(weekData[0]);
		} 
    	catch (Exception e) 
    	{
			throw new Exception("Beim laden der Dateien ist ein Fehler aufgetreten");
		}
		
    }


	/*	12.10.12
	 * 	Tobias Janssen
	 * 
	 * 	erst in der Liste der Pages und Headlines den übergebenen TimeTable 
	 */
	public static void replaceTimeTableInPager(WeekData weekData, MyContext ctxt)
	{
		Calendar currentDay = new GregorianCalendar();
		currentDay = (Calendar) weekData.date.clone();
		int currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
		while(currentDayOfWeek!=2)
		{
			//currentDay.roll(Calendar.DAY_OF_YEAR, false);
			currentDay.setTimeInMillis(currentDay.getTimeInMillis()+86400000);
		}

		
		if(ctxt.weekView)
		{
			View page = createWeekPage(weekData, ctxt);
			String header = createWeekHeader(weekData, currentDay);
			//location suchen
			int location=-1;
			for(int i=0;i<ctxt.headlines.size()&& location == -1;i++)
			{
				if(ctxt.headlines.get(i).equals(header))
					location=i;
			}
			if(location == -1)
				location = 0;
			ctxt.pages.set(location, page);
			ctxt.headlines.set(location,header);
			
			//currentDay.roll(Calendar.WEEK_OF_YEAR,true);
			currentDay.setTimeInMillis(currentDay.getTimeInMillis()+86400000);
			
		}
		else
		{
			for (int x = 1; x < weekData.timetable[0].length; x++)
			{
				List<TimetableViewObject> list = createTimetableDayViewObject(weekData, ctxt, currentDay);
				View page = createPage(weekData, ctxt, list);
				String header = createDayHeader(weekData, currentDay);
				
				//location suchen
				int location=-1;
				for(int i=0;i<ctxt.headlines.size()&& location == -1;i++)
				{
					if(ctxt.headlines.get(i).equals(header))
						location=i;
				}
				if(location == -1)
					location = 0;
				ctxt.pages.set(location, page);
				ctxt.headlines.set(location,header);
		
				currentDay.setTimeInMillis(currentDay.getTimeInMillis()+86400000);
				//currentDay.roll(Calendar.DAY_OF_YEAR,true);
			}
		}
	}
	
	/* 	2.10.12
  	 *	Tobias Janßen
  	 *
  	 *	Prüft, ob welche Daten im StupidCore dirty sind, und speichert diese
  	 */ 
    public static void saveFiles(MyContext ctxt) throws Exception
    {
    	StupidCore stupid = ctxt.stupid;
		SaveSetup saveSetup = buildSaveSetup(ctxt);
		SaveData saveData;
    	
    	if(stupid.setupIsDirty)
    	{
    		ctxt.executor.execute(saveSetup,false);
    	}
    	if(saveSetup.exception!=null)
		{
			throw saveSetup.exception;
		}
    	
    	
    	WeekData weekData;
    	for(int d=0;d<stupid.stupidData.size();d++)
    	{
    		weekData = stupid.stupidData.get(d);
    		saveData = buildSaveData(ctxt,weekData);
    		if(weekData.isDirty)
    		{
    			ctxt.executor.execute(saveData,false);
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
    public static void saveFilesWithProgressDialog(MyContext ctxt, Calendar currentDate )
    {
    	StupidCore stupid = ctxt.stupid;
    	
    	//ProgressDialog initialisieren
    	
    	stupid.progressDialog =  new ProgressDialog(ctxt.context);
    	stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	stupid.progressDialog.setMessage(ctxt.context.getString(R.string.msg_saving));
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
    		SaveSetup saveSetup = buildSaveSetup(ctxt);
    		ctxt.executor.execute(saveSetup,false);
    	}
    	
    	SaveData saveData;
    	WeekData weekData;
    	for(int d=0;d<stupid.stupidData.size();d++)
    	{
    		weekData = stupid.stupidData.get(d);
    		saveData = buildSaveData(ctxt, weekData);
    		if(weekData.isDirty)
    		{
    			ctxt.executor.execute(saveData,false);
    		}
    	}
    	ctxt.executor.execute(new DismissProgress(stupid),false);
    }
	
	/* 	14.09.12
  	 *	Tobias Janßen
  	 *
  	 *	Speichert das aktuelle StupidCore-Setup ohne ProgressDialog
  	 */ 
    public static void saveSetup(MyContext ctxt)
    {
    	StupidCore stupid = ctxt.stupid;
    	if(stupid.setupIsDirty)
    	{
    		SaveSetup saveSetup = buildSaveSetup(ctxt);
    		ctxt.executor.execute(saveSetup,false);
    	}
    }

    /* 	14.09.12
  	 * 	Tobias Janßen
  	 *
  	 *	Speichert das aktuelle StupidCore-Setup mit einem ProgressDialog
  	 */	
    public static void saveSetupWithProgressDialog(MyContext ctxt)
    {
    	StupidCore stupid = ctxt.stupid;
    	
    	//ProgressDialog initialisieren
    	stupid.progressDialog =  new ProgressDialog(ctxt.context);
    	stupid.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	stupid.progressDialog.setMessage(ctxt.context.getString(R.string.msg_saving));
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
    		SaveSetup saveSetup = buildSaveSetup(ctxt);
    		ctxt.executor.execute(saveSetup,false);
    	}

    	if(!stupid.setupIsDirty)
    	{
    		stupid.progressDialog.dismiss();
    	}
   		
    }
    
    /// Datum: 10.12.12
  	/// Autor: Tobias Janßen
  	///
  	///	Beschreibung:
  	///	Erstellt einen neuen ProgressDialog mit übergebenem Text
  	///	
  	///
  	///	Parameter:
  	///	
  	/// 
  	/// 
    public static void executeWithDialog(MyContext ctxt, AsyncTask<Boolean, Integer, Boolean> newTask,String text, int style)
    {
    	
    	ctxt.handler.post(new ShowProgressDialog(ctxt,style,text,newTask));
    	ctxt.executor.execute(newTask,false);
    }
    
    public static boolean gotoSetup(MyContext ctxt) {
    	
    	try 
    	{
    		Tools.saveFiles(ctxt);
		} 
    	catch(Exception e) 
    	{
    		
		}
    	ctxt.forceView=false;
	    Intent intent = new Intent(ctxt.activity,SetupActivity.class);
	    ctxt.activity.startActivityForResult(intent,1);	
	    return true;
    }
	
    public static boolean gotoSetup(MyContext ctxt,String putExtraName, Boolean value) {
    	
    	try 
    	{
    		Tools.saveFiles(ctxt);
		} 
    	catch(Exception e) 
    	{
    		
		}
    	ctxt.forceView=false;
	    Intent intent = new Intent(ctxt.activity,SetupActivity.class);
	    intent.putExtra(putExtraName, value);
	    ctxt.activity.startActivityForResult(intent,1);	
	    return true;
    }
    
    public static boolean gotoWeekPlan(MyContext ctxt) {
    	
    	try 
    	{
    		Tools.saveFiles(ctxt);
		} 
    	catch(Exception e) 
    	{
    		
		}
	    Intent intent = new Intent(ctxt.activity,WeekPlanActivity.class);
	    intent.putExtra("currentDate", ctxt.stupid.currentDate.getTimeInMillis());
	    intent.putExtra("forceView", true);
	    ctxt.activity.startActivity(intent);
	    return true;
    }
    
    public static boolean gotoDayPlan(MyContext ctxt) {
    	
    	try 
    	{
    		Tools.saveFiles(ctxt);
		} 
    	catch(Exception e) 
    	{
    		
		}
	    Intent intent = new Intent(ctxt.activity,PlanActivity.class);
	    intent.putExtra("currentDate", ctxt.stupid.currentDate.getTimeInMillis());
	    intent.putExtra("forceView", true);
	    ctxt.activity.startActivity(intent);
	    return true;
    }
    
    /*	4.10.12
     * 	Tobias Janssen
     * 	Aktualisiert die aktuelle Woche
     */
    public static void refreshWeek(MyContext ctxt)
    {
    	ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.FORCEREFRESH,Const.THISWEEK);
    }
}
