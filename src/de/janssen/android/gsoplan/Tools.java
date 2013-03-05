/*
 * Tools.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.viewpagerindicator.TitlePageIndicator;

import de.janssen.android.gsoplan.asyncTasks.SaveProfil;
import de.janssen.android.gsoplan.core.FileOPs;
import de.janssen.android.gsoplan.core.Stupid;
import de.janssen.android.gsoplan.core.WeekData;
import de.janssen.android.gsoplan.runnables.ShowProgressDialog;
import de.janssen.android.gsoplan.view.AppPreferences;
import de.janssen.android.gsoplan.view.LinearLayoutBordered;
import de.janssen.android.gsoplan.view.MyArrayAdapter;
import de.janssen.android.gsoplan.view.PlanActivity;
import de.janssen.android.gsoplan.view.TimetableViewObject;
import de.janssen.android.gsoplan.view.WeekPlanActivity;
import de.janssen.android.gsoplan.xml.XmlOPs;
import de.janssen.android.gsoplan.xml.Xml;
import de.janssen.android.gsoplan.xml.XmlSearch;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Tools
{
    /** 
     * fügt der Liste der Pages und Headlines den übergebenen TimeTable hinzu
     * @author Tobias Janssen
     * 
     * @param weekData
     * @param ctxt
     */
    public static void appendTimeTableToPager(WeekData weekData, MyContext ctxt)
    {
	// eine Kopie des Stundenplan-Datums erstellen
	Calendar currentDay = new GregorianCalendar();
	currentDay = (Calendar) weekData.date.clone();

	// den aktuellen Wochentag abrufen
	int currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);

	// den currentDay auf Montag setzten
	if (currentDayOfWeek > 2)
	{
	    // 1000*60*60*24 = 1 Tag!
	    currentDay.setTimeInMillis(currentDay.getTimeInMillis() - (1000 * 60 * 60 * 24 * (currentDayOfWeek - 2)));
	}
	if (ctxt.weekView)
	{
	    // List<TimetableViewObject> list =
	    // createTimetableWeekViewObject(weekData, ctxt, currentDay);

	    View page = createWeekPage(weekData, ctxt);
	    insertWeekPage(ctxt, currentDay, page, createWeekHeader(weekData, currentDay), 0, ctxt.pager.pageIndex.size());

	    // currentDay.roll(Calendar.WEEK_OF_YEAR,true);
	    currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);

	}
	else
	{
	    for (int x = 1; x < weekData.timetable[0].length; x++)
	    {

		List<TimetableViewObject> list = createTimetableDayViewObject(weekData, ctxt, currentDay);

		View page = createPage(weekData, ctxt, list);
		insertDayPage(ctxt, currentDay, page, createDayHeader(weekData, currentDay), 0, ctxt.pager.pageIndex.size());

		// currentDay.roll(Calendar.DAY_OF_YEAR,1);
		currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);

	    }
	}

    }


    /**
     * 
     * 
     * Erstellt den Überschriften String
     * 
     * @author Tobias Janssen
     * @param weekData
     * @param currentDay
     * @return
     */
    private static String createDayHeader(WeekData weekData, Calendar currentDay)
    {
	int x = currentDay.get(Calendar.DAY_OF_WEEK) - 1;
	String dayName = "";
	if (weekData.timetable[0][x].getDataContent().length() > 3)
	{
	    dayName = weekData.timetable[0][x].getDataContent().replace("\n", "").substring(0, 2);
	}
	return dayName + ", " + currentDay.get(Calendar.DAY_OF_MONTH) + "." + (currentDay.get(Calendar.MONTH) + 1)
		+ "." + currentDay.get(Calendar.YEAR);
    }

    /**
     * @author Tobias Janssen
     * 
     *         Erstellt den Überschriften String
     * @param weekData
     * @param currentWeek
     * @return
     */
    private static String createWeekHeader(WeekData weekData, Calendar currentWeek)
    {
	int firstDay = currentWeek.get(Calendar.DAY_OF_MONTH);
	Calendar cal = (Calendar) currentWeek.clone();
	// den aktuellen Wochentag abrufen
	int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

	// den currentDay auf Montag setzten
	if (currentDayOfWeek < 6)
	{
	    // 1000*60*60*24 = 1 Tag!
	    cal.setTimeInMillis(cal.getTimeInMillis() + (1000 * 60 * 60 * 24 * (6 - currentDayOfWeek)));
	}
	int lastDay = cal.get(Calendar.DAY_OF_MONTH);
	return firstDay + "." + (currentWeek.get(Calendar.MONTH) + 1) + " - " + lastDay + "."
		+ (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR);
    }

    /**
     * @author Tobias Janssen Erstellt eine Seite des ViewPagers, inkl Header
     *         und Footer
     * 
     * @param weekData
     * @param ctxt
     * @param list
     * @return
     */
    private static View createPage(WeekData weekData, MyContext ctxt, List<TimetableViewObject> list)
    {
	View page = ctxt.inflater.inflate(R.layout.daylayout, null);

	ListView listView = (ListView) page.findViewById(R.id.listTimetable);
	MyArrayAdapter adapter = new MyArrayAdapter(ctxt, list);
	listView.setAdapter(adapter);

	TextView syncTime = (TextView) page.findViewById(R.id.syncTime);
	Calendar sync = new GregorianCalendar();
	sync.setTimeInMillis(weekData.syncTime);

	String minute = String.valueOf(sync.get(Calendar.MINUTE));
	if (minute.length() == 1)
	    minute = "0" + minute;

	syncTime.setText(weekData.elementId + " | Stand vom " + sync.get(Calendar.DAY_OF_MONTH) + "."
		+ (sync.get(Calendar.MONTH) + 1) + "." + sync.get(Calendar.YEAR) + " " + sync.get(Calendar.HOUR_OF_DAY)
		+ ":" + minute + " Uhr");
	return page;
    }

    /**
     * Erstellt eine Stundeplan Seite des ViewPagers, inkl Header und Footer<p>
     * Hier wird die Wochenansicht generiert
     * @author Tobias Janssen 
     * 
     * @param weekData
     * @param ctxt
     * @return
     */
    private static View createWeekPage(WeekData weekData, MyContext ctxt)
    {
	// in die Page kommen alle Elemente dieser Ansicht
	View page = ctxt.inflater.inflate(R.layout.weeklayout, null);

	TableLayout tl = (TableLayout) page.findViewById(R.id.weekTimetable);
	LinearLayoutBordered ll = new LinearLayoutBordered(ctxt.context);

	// Tagesüberschrift erstellen:
	TableRow tr = new TableRow(ctxt.context);

	for (int x = 0; x < weekData.timetable[0].length; x++)
	{
	    // einen neuen Rahmen für das Tabellenfeld vorbereiten
	    ll = new LinearLayoutBordered(ctxt.context);
	    ll.setBorderRight(true);
	    ll.setBorderBottom(true);
	    ll.setBorderTop(true);
	    ll.setBorderSize(1);
	    ll.setBackgroundColor(Color.WHITE);

	    View textview = ctxt.inflater.inflate(R.layout.textview, null);
	    TextView tv = (TextView) textview.findViewById(R.id.textview);
	    // Überschriftentextgröße einstellen

	    tv.setTextSize(ctxt.textSizes[0]);
	    if (x == 0)
	    {
		tv.setText(ctxt.getCurStupid().timeslots[0]);
		tv.setTextColor(Color.parseColor("#3A599A"));

	    }
	    else
	    {
		if (weekData.timetable[0][x].getDataContent() != null)
		{
		    String colorString = weekData.timetable[0][x].getColorParameter();
		    tv.setTextColor(Color.parseColor(colorString));
		    tv.setText(weekData.timetable[0][x].getDataContent().replace("\n", " ") + "\t");
		}
	    }
	    ll.addView(tv);
	    tr.addView(ll);
	}
	tl.addView(tr);

	int start = 1;
	Boolean rowIsEmpty = true;
	// herausfinden ab wann die stunden beginnen, dies nur durchführen, wenn
	// leestunden entfernt werden sollen
	if (ctxt.getCurStupid().hideEmptyHours)
	{
	    for (int y = start; y < weekData.timetable.length && rowIsEmpty; y++)
	    {
		for (int x = 1; x < weekData.timetable[y].length && rowIsEmpty; x++)
		{
		    if (weekData.timetable[y][x].getDataContent() == null
			    || weekData.timetable[y][x].getDataContent().equalsIgnoreCase("null")
			    || weekData.timetable[y][x].getDataContent().equalsIgnoreCase(""))
		    {

		    }
		    else
		    {
			rowIsEmpty = false;
		    }
		}
		if (rowIsEmpty)
		    start = y + 1;
	    }
	}
	int stop = weekData.timetable.length - 1;
	rowIsEmpty = true;
	// herausfinden ab wann die stunden beginnen, dies nur durchführen, wenn
	// leestunden entfernt werden sollen
	if (ctxt.getCurStupid().hideEmptyHours)
	{
	    for (int y = stop; y > 0 && rowIsEmpty; y--)
	    {
		for (int x = 1; x < weekData.timetable[y].length && rowIsEmpty; x++)
		{
		    if (weekData.timetable[y][x].getDataContent() == null
			    || weekData.timetable[y][x].getDataContent().equalsIgnoreCase("null")
			    || weekData.timetable[y][x].getDataContent().equalsIgnoreCase(""))
		    {

		    }
		    else
		    {
			rowIsEmpty = false;
		    }
		}
		if (rowIsEmpty)
		    stop = y;
	    }
	}
	if (stop == 0) // Stundeplan ist leer
	{
	    stop = weekData.timetable.length - 1;
	}

	if (start == weekData.timetable.length) // Stundeplan ist leer
	{
	    start = 1;
	}
	//den Stundenplan zusammensetzten
	for (int y = start; y <= stop; y++)
	{
	    tr = new TableRow(ctxt.context);

	    for (int x = 0; x < weekData.timetable[y].length; x++)
	    {
		ll = new LinearLayoutBordered(ctxt.context);
		ll.setBorderRight(true);
		ll.setBorderBottom(true);
		ll.setBorderSize(1);
		ll.setBackgroundColor(Color.WHITE);
		View textview = ctxt.inflater.inflate(R.layout.textview, null);
		TextView tv = (TextView) textview.findViewById(R.id.textview);
		
		// TextView tv = new TextView(ctxt.context);

		tv.setTextSize(10);
		if (x == 0)
		{
		    tv.setText(ctxt.getCurStupid().timeslots[y]);
		    tv.setTextColor(Color.parseColor("#3A599A"));

		}
		else
		{
		    if (weekData.timetable[y][x].getDataContent() != null)
		    {
			String colorString = weekData.timetable[y][x].getColorParameter();
			tv.setTextColor(Color.parseColor(colorString));
			tv.setText(weekData.timetable[y][x].getDataContent().replace("\n", " ")) ;
		    }
		}
		ll.addView(tv);
		tr.addView(ll);
	    }
	    tl.addView(tr);
	}

	TextView syncTime = (TextView) page.findViewById(R.id.syncTime);
	Calendar sync = new GregorianCalendar();
	sync.setTimeInMillis(weekData.syncTime);

	String minute = String.valueOf(sync.get(Calendar.MINUTE));
	if (minute.length() == 1)
	    minute = "0" + minute;

	syncTime.setText(weekData.elementId + " | Stand vom " + sync.get(Calendar.DAY_OF_MONTH) + "."
		+ (sync.get(Calendar.MONTH) + 1) + "." + sync.get(Calendar.YEAR) + " " + sync.get(Calendar.HOUR_OF_DAY)
		+ ":" + minute + " Uhr");

	return page;
    }

    /**
     * @author Tobias Janssen Erstellt eine Seite des ViewPagers, inkl Header
     *         und Footer
     * 
     * @param weekData
     * @param ctxt
     * @param currentDay
     * @return
     */
    private static List<TimetableViewObject> createTimetableDayViewObject(WeekData weekData, MyContext ctxt,
	    Calendar currentDay)
    {
	Stupid stupid = ctxt.getCurStupid();
	int x = currentDay.get(Calendar.DAY_OF_WEEK) - 1;
	List<TimetableViewObject> list = new ArrayList<TimetableViewObject>();

	int nullCounter = 0;
	Boolean entryFound = false;
	for (int y = 1; y < weekData.timetable.length; y++)
	{

	    if (weekData.timetable[y][x].getDataContent() == null && !entryFound && stupid.hideEmptyHours)
	    {
		nullCounter++;
	    }
	    else if (weekData.timetable[y][x].getDataContent() != null)
	    {
		if (weekData.timetable[y][x].getDataContent().equalsIgnoreCase("null") && !entryFound
			&& stupid.hideEmptyHours)
		{
		    nullCounter++;
		}
		else if (weekData.timetable[y][x].getDataContent().equalsIgnoreCase("") && !entryFound
			&& stupid.hideEmptyHours)
		{
		    nullCounter++;
		}
		else
		{
		    if (y != 0)
			entryFound = true;
		    if (weekData.timetable[y][x].getDataContent().equalsIgnoreCase("null"))
		    {
			list.add(new TimetableViewObject(stupid.timeslots[y], "", "#000000"));
		    }
		    else
		    {
			String color = weekData.timetable[y][x].getColorParameter();
			list.add(new TimetableViewObject(stupid.timeslots[y], weekData.timetable[y][x].getDataContent()
				.replaceAll("\n", " "), color));
		    }
		}
	    }
	    else
	    {
		list.add(new TimetableViewObject(stupid.timeslots[y], "", "#000000"));
	    }
	}

	if (!stupid.hideEmptyHours)
	{
	    // prüfen, ob gar keine Stunden vorhanden sind
	    for (int i = 0; i < list.size(); i++)
	    {
		if (list.get(i).row2.equalsIgnoreCase(""))
		    nullCounter++;
	    }
	}

	// prüfen, ob gar keine Stunden vorhanden sind
	if (nullCounter == 15)
	{
	    list.clear();
	    list.add(new TimetableViewObject("", "kein Unterricht", "#000000"));
	}

	// nun von hinten aufrollen und alle leeren Stunden entfernen
	TimetableViewObject lineObject;
	for (int i = list.size() - 1; i >= 0; i--)
	{
	    lineObject = list.get(i);
	    if (lineObject.row2.equalsIgnoreCase(""))
		list.remove(i);
	    else
		break;
	}
	return list;
    }

    



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
	    Xml xml = new Xml("root", FileOPs.readFromFile(vFile));
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
     *         Liefert den pageindex des angegebenen Datums und des Angegebenen
     *         Feldes
     * 
     * @param pageIndex
     * @param currentDate
     * @param calendarField
     * @return
     */
    public static int getPage(List<Calendar> pageIndex, Calendar currentDate, int calendarField)
    {
	int currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);
	// den currentDay auf den folge Montag setzten
	if (currentDayOfWeek < 2)
	{
	    // 1000*60*60*24 = 1 Tag!
	    currentDate.setTimeInMillis(currentDate.getTimeInMillis() + (1000 * 60 * 60 * 24 * (2 - currentDayOfWeek)));
	}
	if (currentDayOfWeek > 6)
	{
	    // 1000*60*60*24 = 1 Tag!
	    currentDate.setTimeInMillis(currentDate.getTimeInMillis() + (1000 * 60 * 60 * 24 * 2));
	}

	int dayOfYearcurrent = 0;
	int weekOfYearcurrent = 0;
	int dayOfYearpage = 0;
	int weekOfYearpage = 0;
	int yearCurrent = 0;
	int yearPage = 0;

	int nextPage = 0;
	// alle Seiten des Pages durchlaufen und das Datum abfragen und mit dem
	// gewünschten Datum vergleichen
	for (int i = 0; i < pageIndex.size(); i++)
	{
	    dayOfYearcurrent = currentDate.get(Calendar.DAY_OF_YEAR);
	    weekOfYearcurrent = currentDate.get(Calendar.WEEK_OF_YEAR);
	    yearCurrent = currentDate.get(Calendar.YEAR);
	    dayOfYearpage = pageIndex.get(i).get(Calendar.DAY_OF_YEAR);
	    weekOfYearpage = pageIndex.get(i).get(Calendar.WEEK_OF_YEAR);
	    yearPage = pageIndex.get(i).get(Calendar.YEAR);

	    // prüfen auf was getestet werden soll(wochenansicht, oder
	    // tagesansicht)
	    if (calendarField == Calendar.DAY_OF_YEAR)
	    {
		// Tagesansicht
		// Issue #12(wenn Seite nicht existiert, wird immer der erste
		// Tag angezeigt) behoben
		if (yearPage < yearCurrent)
		{
		    nextPage = i;
		    if (dayOfYearpage < dayOfYearcurrent)
			nextPage = i;
		}
		if ((dayOfYearcurrent == dayOfYearpage) && (yearCurrent == yearPage))
		    return i;
	    }
	    else if (calendarField == Calendar.WEEK_OF_YEAR)
	    {
		// Wochenansicht
		// Issue #12(wenn Seite nicht existiert, wird immer der erste
		// Tag angezeigt) behoben
		if (yearPage < yearCurrent)
		{
		    nextPage = i;
		    if (dayOfYearpage < dayOfYearcurrent)
			nextPage = i;
		}
		if ((weekOfYearcurrent == weekOfYearpage) && (yearCurrent == yearPage))
		    return i;
	    }
	}

	// dies kommt nur vor, wenn die Seite nicht gefunden wurde. dann wird
	// die nächst kleinere Seite zurückgeliefert
	return nextPage;

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
     *         Fügt die Page an die richtige Position im pager an
     * 
     * @param ctxt
     * @param currentWeek
     * @param page
     * @param header
     * @param startIndex
     * @param stopIndex
     */
    private static void insertWeekPage(MyContext ctxt, Calendar currentWeek, View page, String header, int startIndex,
	    int stopIndex)
    {
	// prüfen, an welche stelle die page gehört
	// dazu die mitte aller bestehenden pages nehmen
	int midPos = ((stopIndex - startIndex) / 2) + startIndex;

	if (midPos == 0)
	{
	    // es existiert keiner, oder max ein eintrag
	    // daher prüfen, ob ein eintrag besteht
	    if (ctxt.pager.pageIndex.size() >= 1)
	    {
		// ja, einen eintrag gibt es bereits
		int pageDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos));
		int currentDate = calcIntYearDay(currentWeek);

		// prüfen, ob die bestehende seite "älter" als die
		// hinzuzufügende ist
		if (pageDate < currentDate)
		{
		    // die page indexieren
		    ctxt.pager.pageIndex.add(midPos + 1, (Calendar) currentWeek.clone());
		    ctxt.pager.pages.add(midPos + 1, page);
		    ctxt.pager.headlines.add(midPos + 1, header);
		}
		else
		{
		    // die page indexieren
		    ctxt.pager.pageIndex.add(midPos, (Calendar) currentWeek.clone());
		    ctxt.pager.pages.add(midPos, page);
		    ctxt.pager.headlines.add(midPos, header);
		}
	    }
	    else
	    {
		// nein es ist alles leer, daher einfach einfügen
		// die page indexieren
		ctxt.pager.pageIndex.add(midPos, (Calendar) currentWeek.clone());
		ctxt.pager.pages.add(midPos, page);
		ctxt.pager.headlines.add(midPos, header);
	    }
	}
	else
	{

	    int pageDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos));
	    int currentDate = calcIntYearDay(currentWeek);

	    // prüfen, ob die bestehende seite "älter" als die hinzuzufügende
	    // ist
	    if (pageDate < currentDate)
	    {
		// ja, ist älter, daher muss die page auf jeden fall dahinder
		// eingefügt werden
		// prüfen, ob direkte nachbarschaft besteht
		// dazu erstmal prüfen, ob der nächste nachbar überhaupt
		// existiert
		if (midPos + 1 >= ctxt.pager.pageIndex.size())
		{
		    // existiert gar keiner mehr; daher page hinzufügen

		    // die page indexieren
		    ctxt.pager.pageIndex.add(midPos + 1, (Calendar) currentWeek.clone());
		    ctxt.pager.pages.add(midPos + 1, page);
		    ctxt.pager.headlines.add(midPos + 1, header);
		}
		else
		{
		    // es ist ein nachbar vorhanden
		    int pageNeighborDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos + 1));
		    // prüfen, ob dieser näher dran liegt als die currentPage
		    if (pageNeighborDate < currentDate)
		    {
			// ja alte page ist ein näherer nachbar
			insertWeekPage(ctxt, currentWeek, page, header, midPos, stopIndex);
		    }
		    else
		    {
			// nein, currentPage ist näher
			// also dazwischen einfügen
			// die page indexieren
			ctxt.pager.pageIndex.add(midPos + 1, (Calendar) currentWeek.clone());
			ctxt.pager.pages.add(midPos + 1, page);
			ctxt.pager.headlines.add(midPos + 1, header);

		    }
		}

	    }
	    else
	    {
		// nein,die bestehende seite ist hat ein jüngers Datum als die
		// hinzuzufügende, daher muss die neue page auf jeden fall davor
		// eingefügt werden

		if (midPos == 0)
		{
		    // existiert gar kein eintrag; daher page hinzufügen

		    // die page indexieren
		    ctxt.pager.pageIndex.add((Calendar) currentWeek.clone());
		    ctxt.pager.pages.add(page);
		    ctxt.pager.headlines.add(header);
		}
		else
		{
		    // prüfen, ob der vorgänger Nachbar kleiner ist
		    int pageNeighborDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos - 1));

		    if (pageNeighborDate < currentDate)
		    {
			// ja davorige page ist kleiner
			// also dazwischen einfügen
			// die page indexieren
			ctxt.pager.pageIndex.add(midPos, (Calendar) currentWeek.clone());
			ctxt.pager.pages.add(midPos, page);
			ctxt.pager.headlines.add(midPos, header);

		    }
		    else
		    {
			insertWeekPage(ctxt, currentWeek, page, header, 0, midPos);
		    }
		}
	    }

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
     * @author Tobias Janssen
     * 
     *         Fügt die Page an die richtige Position im pager an
     * 
     * @param ctxt
     * @param currentDay
     * @param page
     * @param header
     * @param startIndex
     * @param stopIndex
     */
    private static void insertDayPage(MyContext ctxt, Calendar currentDay, View page, String header, int startIndex,
	    int stopIndex)
    {

	// prüfen, an welche stelle die page gehört
	// dazu die mitte aller bestehenden pages nehmen
	int midPos = ((stopIndex - startIndex) / 2) + startIndex;

	if (midPos == 0)
	{
	    // es existiert keiner, oder max ein eintrag
	    // daher prüfen, ob ein eintrag besteht
	    if (ctxt.pager.pageIndex.size() >= 1)
	    {
		// ja, einen eintrag gibt es bereits
		int pageDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos));
		int currentDate = calcIntYearDay(currentDay);

		// prüfen, ob die bestehende seite "älter" als die
		// hinzuzufügende ist
		if (pageDate < currentDate)
		{
		    // die page indexieren
		    ctxt.pager.pageIndex.add(midPos + 1, (Calendar) currentDay.clone());
		    ctxt.pager.pages.add(midPos + 1, page);
		    ctxt.pager.headlines.add(midPos + 1, header);
		}
		else
		{
		    // die page indexieren
		    ctxt.pager.pageIndex.add(midPos, (Calendar) currentDay.clone());
		    ctxt.pager.pages.add(midPos, page);
		    ctxt.pager.headlines.add(midPos, header);
		}
	    }
	    else
	    {
		// nein es ist alles leer, daher einfach einfügen
		// die page indexieren
		ctxt.pager.pageIndex.add(midPos, (Calendar) currentDay.clone());
		ctxt.pager.pages.add(midPos, page);
		ctxt.pager.headlines.add(midPos, header);
	    }
	}
	else
	{
	    // daten Tag des Jahres abrufen
	    int pageDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos));
	    int currentDate = calcIntYearDay(currentDay);

	    // prüfen, ob die bestehende seite "älter" als die hinzuzufügende
	    // ist
	    if (pageDate < currentDate)
	    {
		// ja, ist älter, daher muss die page auf jeden fall dahinder
		// eingefügt werden
		// prüfen, ob direkte nachbarschaft besteht
		// dazu erstmal prüfen, ob der nächste nachbar überhaupt
		// existiert
		if (midPos + 1 >= ctxt.pager.pageIndex.size())
		{
		    // existiert gar keiner mehr; daher page hinzufügen

		    // die page indexieren
		    ctxt.pager.pageIndex.add(midPos + 1, (Calendar) currentDay.clone());
		    ctxt.pager.pages.add(midPos + 1, page);
		    ctxt.pager.headlines.add(midPos + 1, header);
		}
		else
		{
		    // es ist ein nachbar vorhanden
		    // prüfen, ob dieser näher dran liegt als die currentPage
		    int pageNeighborDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos + 1));
		    if (pageNeighborDate < currentDate)
		    {
			// ja alte page ist ein näherer nachbar
			insertDayPage(ctxt, currentDay, page, header, midPos, stopIndex);
		    }
		    else
		    {
			// nein, currentPage ist näher
			// also dazwischen einfügen
			// die page indexieren
			ctxt.pager.pageIndex.add(midPos + 1, (Calendar) currentDay.clone());
			ctxt.pager.pages.add(midPos + 1, page);
			ctxt.pager.headlines.add(midPos + 1, header);

		    }
		}

	    }
	    else
	    {
		// nein,die bestehende seite ist hat ein jüngers Datum als die
		// hinzuzufügende, daher muss die neue page auf jeden fall davor
		// eingefügt werden

		if (midPos == 0)
		{
		    // existiert gar kein eintrag; daher page hinzufügen

		    // die page indexieren
		    ctxt.pager.pageIndex.add((Calendar) currentDay.clone());
		    ctxt.pager.pages.add(page);
		    ctxt.pager.headlines.add(header);
		}
		else
		{
		    // prüfen, ob der vorgänger Nachbar kleiner ist
		    int pageNeighborDate = calcIntYearDay(ctxt.pager.pageIndex.get(midPos - 1));
		    if (pageNeighborDate < currentDate)
		    {
			// ja davorige page ist kleiner
			// also dazwischen einfügen
			// die page indexieren
			ctxt.pager.pageIndex.add(midPos, (Calendar) currentDay.clone());
			ctxt.pager.pages.add(midPos, page);
			ctxt.pager.headlines.add(midPos, header);

		    }
		    else
		    {
			insertDayPage(ctxt, currentDay, page, header, 0, midPos);
		    }
		}
	    }

	}
    }



    /**
     * @author Tobias Janssen Lädt alle verfügbaren Daten-Datein
     * 
     * @param context
     * @param stupid
     * @throws Exception
     */
    public static void loadAllDataFiles(Context context, Stupid stupid) throws Exception
    {
	try
	{
	    File elementDir = new java.io.File(context.getFilesDir() + "/" + stupid.getMyElement());
	    File[] files = elementDir.listFiles();
	    for (int f = 0; f < files.length; f++)
	    {
		loadNAppendFile(context, stupid, files[f]);
	    }
	}
	catch (Exception e)
	{
	    throw e;
	}

    }
    /**
     * Lädt die Profildatei. Sie enthält den zuletzt gewählten Index des StupidCores
     * 
     * 
     * @param ctxt
     */
    public static void loadProfileFile(MyContext ctxt)
    {
	try
	{
	    File dir = new java.io.File(ctxt.context.getFilesDir() + "/");
	    Xml xml = new Xml("root", FileOPs.readFromFile(new File(dir, "profil.xml")));
	    xml.parseXml();
	    XmlSearch xmlSearch = new XmlSearch();
	    Xml profil = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml(Xml.PROFIL));
	    if (profil != null)
	    {
		if (profil.getType().equalsIgnoreCase(Xml.PROFIL))
		{
		    String value = profil.getDataContent();
		    if (value.equalsIgnoreCase("1"))
			ctxt.switchStupidTo(1);
		    if (value.equalsIgnoreCase("0"))
			ctxt.switchStupidTo(0);
		}
	    }
	}
	catch (Exception e)
	{
	    
	}
    }
    
    /**
     * Speichert das aktuelle Profil
     * @param ctxt
     */
    public static void saveProfilSameThread(MyContext ctxt, int index)
    {
	try
	{
	    File dir = new java.io.File(ctxt.context.getFilesDir() + "/");
	    File profilFile = new File(dir, "profil.xml");
	    String xmlContent = XmlOPs.createProfileXml(ctxt, index);
	    FileOPs.saveToFile(xmlContent, profilFile);
	}
	catch (Exception e)
	{
	    
	}
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
    private static void loadNAppendFile(Context context, Stupid stupid, File file) throws Exception
    {

	try
	{
	    WeekData[] weekData = XmlOPs.convertXmlToWeekData(FileOPs.readFromFile(file));
	    if (weekData.length > 0)
		stupid.stupidData.add(weekData[0]);
	}
	catch (Exception e)
	{
	    throw new Exception("Beim laden der Dateien ist ein Fehler aufgetreten");
	}
    }

    /**
     * @author Tobias Janssen
     * 
     *         erst in der Liste der Pages und Headlines den übergebenen
     *         TimeTable
     * 
     * @param weekData
     * @param ctxt
     */
    public static void replaceTimeTableInPager(WeekData weekData, MyContext ctxt)
    {
	Calendar currentDay = new GregorianCalendar();
	currentDay = (Calendar) weekData.date.clone();
	int currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
	while (currentDayOfWeek != 2)
	{
	    // currentDay.roll(Calendar.DAY_OF_YEAR, false);
	    currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);
	}

	if (ctxt.weekView)
	{
	    View page = createWeekPage(weekData, ctxt);
	    String header = createWeekHeader(weekData, currentDay);
	    // location suchen
	    int location = -1;
	    for (int i = 0; i < ctxt.pager.headlines.size() && location == -1; i++)
	    {
		if (ctxt.pager.headlines.get(i).equals(header))
		    location = i;
	    }
	    if (location == -1)
		location = 0;
	    ctxt.pager.pages.set(location, page);
	    ctxt.pager.headlines.set(location, header);

	    // currentDay.roll(Calendar.WEEK_OF_YEAR,true);
	    currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);

	}
	else
	{
	    for (int x = 1; x < weekData.timetable[0].length; x++)
	    {
		List<TimetableViewObject> list = createTimetableDayViewObject(weekData, ctxt, currentDay);
		View page = createPage(weekData, ctxt, list);
		String header = createDayHeader(weekData, currentDay);

		// location suchen
		int location = -1;
		for (int i = 0; i < ctxt.pager.headlines.size() && location == -1; i++)
		{
		    if (ctxt.pager.headlines.get(i).equals(header))
			location = i;
		}
		if (location == -1)
		    location = 0;
		ctxt.pager.pages.set(location, page);
		ctxt.pager.headlines.set(location, header);

		currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);
		// currentDay.roll(Calendar.DAY_OF_YEAR,true);
	    }
	}
    }



    
    /**
     * @author Tobias Janssen Erstellt einen neuen ProgressDialog mit
     *         übergebenem Text
     * 
     * @param ctxt
     * @param newTask
     * @param text
     * @param style
     */
    @Deprecated
    public static void executeWithDialog(MyContext ctxt, AsyncTask<Boolean, Integer, Boolean> newTask, String text,
	    int style)
    {

	ctxt.handler.post(new ShowProgressDialog(ctxt, style, text, newTask));
	ctxt.executor.execute(newTask);
    }

    /**
     * 
     * @author Tobias Janssen
     * @param ctxt
     * @return
     */
    public static boolean gotoSetup(MyContext ctxt)
    {

	try
	{
	    ctxt.getCurStupid().saveFiles(ctxt);
	}
	catch (Exception e)
	{

	}
	ctxt.forceView = false;
	// Intent intent = new Intent(ctxt.activity,SetupActivity.class);
	Intent intent = new Intent(ctxt.activity, AppPreferences.class);

	ctxt.activity.startActivityForResult(intent, 1);
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

	try
	{
	    ctxt.getCurStupid().saveFiles(ctxt);
	}
	catch (Exception e)
	{

	}
	ctxt.forceView = false;
	// Intent intent = new Intent(ctxt.activity,SetupActivity.class);
	Intent intent = new Intent(ctxt.activity, AppPreferences.class);
	intent.putExtra(putExtraName, value);
	ctxt.activity.startActivityForResult(intent, 1);
	return true;
    }

    /**
     * @author Tobias Janssen
     * @param ctxt
     * @return
     */
    public static boolean gotoWeekPlan(MyContext ctxt)
    {

	try
	{
	    ctxt.getCurStupid().saveFiles(ctxt);
	}
	catch (Exception e)
	{

	}
	Intent intent = new Intent(ctxt.activity, WeekPlanActivity.class);
	intent.putExtra("currentDate", ctxt.getCurStupid().currentDate.getTimeInMillis());
	intent.putExtra("forceView", true);
	ctxt.activity.startActivity(intent);
	return true;
    }

    /**
     * @author Tobias Janssen
     * @param ctxt
     * @return
     */
    public static boolean gotoDayPlan(MyContext ctxt)
    {

	try
	{
	    ctxt.getCurStupid().saveFiles(ctxt);
	}
	catch (Exception e)
	{

	}
	Intent intent = new Intent(ctxt.activity, PlanActivity.class);
	intent.putExtra("currentDate", ctxt.getCurStupid().currentDate.getTimeInMillis());
	intent.putExtra("forceView", true);
	ctxt.activity.startActivity(intent);
	return true;
    }

    /**
     * @author Tobias Janssen Aktualisiert die aktuelle Woche
     * 
     * @param ctxt
     */
    public static void refreshWeek(MyContext ctxt)
    {
	ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.FORCEREFRESH, Const.THISWEEK);
    }

    /**
     * @author Tobias Janssen übersetzt die Settings aus den alten Versionen in
     *         das neue Format und löscht anschließend die alte datei
     * 
     * @param ctxt
     *            MyContext der Applikation
     */
    public static void translateOldSettings(MyContext ctxt)
    {
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt.context);

	// dies ist für die umstellung auf version 1.03 beta
	// prüfen, ob die datei noch existiert
	File oldFile = new File(ctxt.context.getFilesDir(), "gsoStupidSetup.xml");
	if (oldFile.exists())
	{
	    try
	    {

		String content = FileOPs.readFromFile(oldFile); // Content aus
								// File laden
		Xml xml = new Xml("root", content); // xml-Objekt erzeugen
		xml.parseXml(); // und den Content konvertiern
		ctxt.getCurStupid().clearElements();
		ctxt.getCurStupid().fetchElementsFromXml(xml, ctxt); // Daten
							     // in
							     // den
							     // Stupid-Core
							     // laden
		String xmlContent = XmlOPs.convertElementsToXml(ctxt); // Daten
								       // in das
								       // neue
								       // Format
								       // konvertieren

		FileOPs.saveToFile(xmlContent, ctxt.getCurStupid().getFileSaveElement(ctxt)); // Daten
										// speichern

		// Einstellungen umsetzten
		XmlSearch xmlSearch = new XmlSearch();
		Xml myElement = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml("myElement"));
		xmlSearch = new XmlSearch();
		Xml myType = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml("myType"));
		xmlSearch = new XmlSearch();
		Xml onlyWlan = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml("onlyWlan"));
		xmlSearch = new XmlSearch();
		Xml resyncAfter = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml("resyncAfter"));
		xmlSearch = new XmlSearch();
		Xml hideEmptyHours = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml("hideEmptyHours"));
		xmlSearch = new XmlSearch();
		Xml defaultActivity = xmlSearch.tagCrawlerFindFirstOf(xml, new Xml("defaultActivity"));

		Editor editPrefs = prefs.edit();
		if (myElement.getDataContent() != null)
		    editPrefs.putString("listElement1", myElement.getDataContent());
		if (myType.getDataContent() != null)
		{
		    if (myType.getDataContent().equalsIgnoreCase("0"))
		    {
			editPrefs.putString("listType1", "Klassen");
		    }
		    if (myType.getDataContent().equalsIgnoreCase("1"))
		    {
			editPrefs.putString("listType1", "Lehrer");
		    }
		    if (myType.getDataContent().equalsIgnoreCase("2"))
		    {
			editPrefs.putString("listType1", "Räume");
		    }
		}
		if (onlyWlan.getDataContent() != null)
		{
		    if (onlyWlan.getDataContent().equalsIgnoreCase("true"))
			editPrefs.putBoolean("boxWlan", true);
		    else
			editPrefs.putBoolean("boxWlan", false);
		}
		if (hideEmptyHours.getDataContent() != null)
		{
		    if (hideEmptyHours.getDataContent().equalsIgnoreCase("true"))
			editPrefs.putBoolean("boxHide", true);
		    else
			editPrefs.putBoolean("boxHide", false);
		}
		if (resyncAfter.getDataContent() != null)
		    editPrefs.putString("listResync", resyncAfter.getDataContent());
		if (resyncAfter.getDataContent() != null)
		    editPrefs.putString("listResync", resyncAfter.getDataContent());
		if (defaultActivity.getDataContent() != null)
		{
		    if (defaultActivity.getDataContent().equalsIgnoreCase(
			    "class de.janssen.android.gsoplan.WeekPlanActivity"))
			editPrefs.putString("listActivity", "Woche");
		    if (defaultActivity.getDataContent().equalsIgnoreCase(
			    "class de.janssen.android.gsoplan.PlanActivity"))
			editPrefs.putString("listActivity", "Tag");
		}
		editPrefs.apply();

		String element = prefs.getString("listElement1", "");
		ctxt.getCurStupid().setMyElement(element);

		// löschen der alten Datei
		oldFile.delete();

	    }
	    catch (Exception e)
	    {

	    }

	}
	//Dies ist für die Umstellung von Version 1.03b auf 1.04
	//Der ElementsDateiname hat sich geändert und heißt nun 0Elements.xml 
	oldFile = new File(ctxt.context.getFilesDir(), "Elements.xml");
	if(oldFile.exists())
	{
	    oldFile.renameTo(new File(ctxt.context.getFilesDir(),"0Elements.xml"));
	    Editor editPrefs = prefs.edit();
	    String oldElement = prefs.getString("listElement", "");
	    editPrefs.remove("listElement");
	    editPrefs.putString("listElement1", oldElement);
	    String oldType = prefs.getString("listType", "Klassen");
	    editPrefs.putString("listType1", oldType);
	    editPrefs.remove("listType");
	    editPrefs.apply();
	}
    }
    


 
    
    


}
