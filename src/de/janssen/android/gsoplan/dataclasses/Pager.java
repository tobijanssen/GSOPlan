/*
 * Pager.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.dataclasses;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.viewpagerindicator.TitlePageIndicator;
import de.janssen.android.gsoplan.Logger;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.core.WeekData;


public class Pager
{
    private ViewPager viewPager;
    private List<View> pages = new ArrayList<View>();
    private List<Calendar> pageIndex = new ArrayList<Calendar>();
    private List<String> headlines = new ArrayList<String>();
    private PagerAdapter pageAdapter;
    private TitlePageIndicator pageIndicator;
    
    private Boolean isPagerInit = false;
    private final String[] timeslots = new String[] { "", "7.45 - 8.30", "8.30 - 9.15", "9.35 - 10.20", "10.20 - 11.05",
	    "11.25 - 12.10", "12.10 - 12.55", "13.15 - 14.00", "14.00 - 14.45", "15.05 - 15.50", "15.50 - 16.35",
	    "16.55 - 17.40", "17.40 - 18.25", "18.25 - 19.10", "19.30 - 20.15", "20.15 - 21.00" };
    
    private int textSize;
    
    private LayoutInflater inflater;
    private Context context;
    private Logger logger;
    private Boolean hideEmptyHours;
    
    
    

    
    /**
     * Erstellt ein Pager-Object
     * 
     * @param context		Context der Applikation
     * @param viewPager		ViewPager Referenz
     * @param pageIndicator	TitlePageIndicaor Referenz
     * @param inflater		LayoutInflater der Applikation
     * @param textSize		Integer der zu verwendenen Schriftgröße
     * @param logger		Logger object zum Fehler-logging
     * @param hideEmptyHours	Boolean der angibt, ob Freistunden angezeigt werden, oder nicht
     */
    public Pager(Context context, ViewPager viewPager,TitlePageIndicator pageIndicator, LayoutInflater inflater,int textSize,Logger logger, Boolean hideEmptyHours)
    {
	this.context=context;
	this.viewPager=viewPager;
	this.pageIndicator=pageIndicator;
	this.inflater=inflater;
	this.textSize=textSize;
	this.logger=logger;
	this.hideEmptyHours=hideEmptyHours;
    }
    
    public int size()
    {
	return pages.size();
    }
    
    /**
     * Initialisiert den Pager mit dem übergebenem Datum
     * @param date
     */
    public void init(Calendar date)
    {
	try
	{
	    int currentPage = getPage(date);
	    pageAdapter = new MyPagerAdapter(pages, headlines);
	    viewPager.setAdapter(pageAdapter);
	    
	    pageIndicator.setViewPager(viewPager);
	    pageIndicator.invalidate();
	    viewPager.setCurrentItem(currentPage, false);
	    
	}
	catch (Exception e)
	{
	    logger.log(Logger.Level.ERROR, "Error creating Pager", e);
	}
	
	this.isPagerInit = true;
    }
    
    /**
     * Leert alle Daten des Pagers
     * @author janssen
     */
    public void clear()
    {
	pages = new ArrayList<View>();
	pageIndex = new ArrayList<Calendar>();
	headlines = new ArrayList<String>();
	pageAdapter = new MyPagerAdapter(pages, headlines);
	
	viewPager.setAdapter(pageAdapter);
	pageIndicator.setViewPager(viewPager);
	
    }
    
    /**
     * Gibt an, ob der Pager inititalisiert wurde, die sist nach ausführen der Methode init() der Fall
     * @return
     */
    public Boolean isPagerInitialised()
    {
	return this.isPagerInit;
    }
    
    /**
     * Fügt die Page an die richtige Position im pager an
     * @author Tobias Janssen
     * @param currentWeek
     * @param page
     * @param header
     * @param startIndex
     * @param stopIndex
     */
    private void insertWeekPage(Calendar currentWeek, View page, String header, int startIndex,
	    int stopIndex)
    {
	// prüfen, an welche stelle die page gehört
	// dazu die mitte aller bestehenden pages nehmen
	int midPos = ((stopIndex - startIndex) / 2) + startIndex;

	if (midPos == 0)
	{
	    // es existiert keiner, oder max ein eintrag
	    // daher prüfen, ob ein eintrag besteht
	    if (pageIndex.size() >= 1)
	    {
		// ja, einen eintrag gibt es bereits
		int pageDate = calcIntYearDay(pageIndex.get(midPos));
		int currentDate = calcIntYearDay(currentWeek);

		// prüfen, ob die bestehende seite "älter" als die
		// hinzuzufügende ist
		if (pageDate < currentDate)
		{
		    // die page indexieren
		    pageIndex.add(midPos + 1, (Calendar) currentWeek.clone());
		    pages.add(midPos + 1, page);
		    headlines.add(midPos + 1, header);
		}
		else
		{
		    // die page indexieren
		    pageIndex.add(midPos, (Calendar) currentWeek.clone());
		    pages.add(midPos, page);
		    headlines.add(midPos, header);
		}
	    }
	    else
	    {
		// nein es ist alles leer, daher einfach einfügen
		// die page indexieren
		pageIndex.add(midPos, (Calendar) currentWeek.clone());
		pages.add(midPos, page);
		headlines.add(midPos, header);
	    }
	}
	else
	{

	    int pageDate = calcIntYearDay(pageIndex.get(midPos));
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
		if (midPos + 1 >= pageIndex.size())
		{
		    // existiert gar keiner mehr; daher page hinzufügen

		    // die page indexieren
		    pageIndex.add(midPos + 1, (Calendar) currentWeek.clone());
		    pages.add(midPos + 1, page);
		    headlines.add(midPos + 1, header);
		}
		else
		{
		    // es ist ein nachbar vorhanden
		    int pageNeighborDate = calcIntYearDay(pageIndex.get(midPos + 1));
		    // prüfen, ob dieser näher dran liegt als die currentPage
		    if (pageNeighborDate < currentDate)
		    {
			// ja alte page ist ein näherer nachbar
			insertWeekPage(currentWeek, page, header, midPos, stopIndex);
		    }
		    else
		    {
			// nein, currentPage ist näher
			// also dazwischen einfügen
			// die page indexieren
			pageIndex.add(midPos + 1, (Calendar) currentWeek.clone());
			pages.add(midPos + 1, page);
			headlines.add(midPos + 1, header);

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
		    pageIndex.add((Calendar) currentWeek.clone());
		    pages.add(page);
		    headlines.add(header);
		}
		else
		{
		    // prüfen, ob der vorgänger Nachbar kleiner ist
		    int pageNeighborDate = calcIntYearDay(pageIndex.get(midPos - 1));

		    if (pageNeighborDate < currentDate)
		    {
			// ja davorige page ist kleiner
			// also dazwischen einfügen
			// die page indexieren
			pageIndex.add(midPos, (Calendar) currentWeek.clone());
			pages.add(midPos, page);
			headlines.add(midPos, header);

		    }
		    else
		    {
			insertWeekPage(currentWeek, page, header, 0, midPos);
		    }
		}
	    }

	}

    }
    public void addView(int pos, View page, String headline)
    {
	pages.add(pos, page);
	headlines.add(pos, headline);
    }
    
    /**
     * Fügt die Page an die richtige Position im pager an
     * @author Tobias Janssen
     * @param currentDay
     * @param page
     * @param header
     * @param startIndex
     * @param stopIndex
     */
    private void insertDayPage(Calendar currentDay, View page, String header, int startIndex, int stopIndex)
    {

	// prüfen, an welche stelle die page gehört
	// dazu die mitte aller bestehenden pages nehmen
	int midPos = ((stopIndex - startIndex) / 2) + startIndex;

	if (midPos == 0)
	{
	    // es existiert keiner, oder max ein eintrag
	    // daher prüfen, ob ein eintrag besteht
	    if (pageIndex.size() >= 1)
	    {
		// ja, einen eintrag gibt es bereits
		int pageDate = calcIntYearDay(pageIndex.get(midPos));
		int currentDate = calcIntYearDay(currentDay);

		// prüfen, ob die bestehende seite "älter" als die
		// hinzuzufügende ist
		if (pageDate < currentDate)
		{
		    // die page indexieren
		    pageIndex.add(midPos + 1, (Calendar) currentDay.clone());
		    pages.add(midPos + 1, page);
		    headlines.add(midPos + 1, header);
		}
		else
		{
		    // die page indexieren
		    pageIndex.add(midPos, (Calendar) currentDay.clone());
		    pages.add(midPos, page);
		    headlines.add(midPos, header);
		}
	    }
	    else
	    {
		// nein es ist alles leer, daher einfach einfügen
		// die page indexieren
		pageIndex.add(midPos, (Calendar) currentDay.clone());
		pages.add(midPos, page);
		headlines.add(midPos, header);
	    }
	}
	else
	{
	    // daten Tag des Jahres abrufen
	    int pageDate = calcIntYearDay(pageIndex.get(midPos));
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
		if (midPos + 1 >= pageIndex.size())
		{
		    // existiert gar keiner mehr; daher page hinzufügen

		    // die page indexieren
		    pageIndex.add(midPos + 1, (Calendar) currentDay.clone());
		    pages.add(midPos + 1, page);
		    headlines.add(midPos + 1, header);
		}
		else
		{
		    // es ist ein nachbar vorhanden
		    // prüfen, ob dieser näher dran liegt als die currentPage
		    int pageNeighborDate = calcIntYearDay(pageIndex.get(midPos + 1));
		    if (pageNeighborDate < currentDate)
		    {
			// ja alte page ist ein näherer nachbar
			insertDayPage(currentDay, page, header, midPos, stopIndex);
		    }
		    else
		    {
			// nein, currentPage ist näher
			// also dazwischen einfügen
			// die page indexieren
			pageIndex.add(midPos + 1, (Calendar) currentDay.clone());
			pages.add(midPos + 1, page);
			headlines.add(midPos + 1, header);

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
		    pageIndex.add((Calendar) currentDay.clone());
		    pages.add(page);
		    headlines.add(header);
		}
		else
		{
		    // prüfen, ob der vorgänger Nachbar kleiner ist
		    int pageNeighborDate = calcIntYearDay(pageIndex.get(midPos - 1));
		    if (pageNeighborDate < currentDate)
		    {
			// ja davorige page ist kleiner
			// also dazwischen einfügen
			// die page indexieren
			pageIndex.add(midPos, (Calendar) currentDay.clone());
			pages.add(midPos, page);
			headlines.add(midPos, header);

		    }
		    else
		    {
			insertDayPage(currentDay, page, header, 0, midPos);
		    }
		}
	    }

	}
    }
    
    /**
     * Ersetzt in der Liste der Pages und Headlines die Seiten die zu der übergebenen WeekData passen 
     * @author Tobias Janssen
     * 
     * @param weekData
     */
    public void replaceTimeTableInPager(WeekData weekData)
    {
	Calendar currentDay = new GregorianCalendar();
	currentDay = (Calendar) weekData.date.clone();
	int currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
	while (currentDayOfWeek != 2)
	{
	    // currentDay.roll(Calendar.DAY_OF_YEAR, false);
	    currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);
	}

	if (context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
	{
	    View page = createWeekPage(weekData);
	    String header = createWeekHeader(weekData, currentDay);
	    // location suchen
	    int location = -1;
	    for (int i = 0; i < headlines.size() && location == -1; i++)
	    {
		if (headlines.get(i).equals(header))
		    location = i;
	    }
	    if (location == -1)
		location = 0;
	    pages.set(location, page);
	    headlines.set(location, header);
	    currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);

	}
	else
	{
	    for (int x = 1; x < weekData.timetable[0].length; x++)
	    {
		List<TimetableViewObject> list = createTimetableDayViewObject(weekData, currentDay);
		View page = createPage(weekData, list);
		String header = createDayHeader(weekData, currentDay);

		// location suchen
		int location = -1;
		for (int i = 0; i < headlines.size() && location == -1; i++)
		{
		    if (headlines.get(i).equals(header))
			location = i;
		}
		if (location == -1)
		    location = 0;
		pages.set(location, page);
		headlines.set(location, header);

		currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);
		// currentDay.roll(Calendar.DAY_OF_YEAR,true);
	    }
	}
    }
    
    
    /**
     * Erstellt den Überschriften String
     * 
     * @author Tobias Janssen
     * @param weekData
     * @param currentDay
     * @return
     */
    private String createDayHeader(WeekData weekData, Calendar currentDay)
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
     * Erstellt eine Seite des ViewPagers, inkl Header und Footer
     * @author Tobias Janssen 
     * 
     * @param weekData
     * @param ctxt
     * @param list
     * @return
     */
    private View createPage(WeekData weekData, List<TimetableViewObject> list)
    {

	try
	{
	    View page = inflater.inflate(R.layout.daylayout, null);
	    ListView listView = (ListView) page.findViewById(R.id.listTimetable);
	    MyListAdapter adapter = new MyListAdapter(context, list);
	    listView.setAdapter(adapter);

	    TextView syncTime = (TextView) page.findViewById(R.id.syncTime);
	    Calendar sync = new GregorianCalendar();
	    sync.setTimeInMillis(weekData.syncTime);

	    String minute = String.valueOf(sync.get(Calendar.MINUTE));
	    if (minute.length() == 1)
		minute = "0" + minute;

	    syncTime.setText(weekData.elementId + " | Stand vom " + sync.get(Calendar.DAY_OF_MONTH) + "."
		    + (sync.get(Calendar.MONTH) + 1) + "." + sync.get(Calendar.YEAR) + " "
		    + sync.get(Calendar.HOUR_OF_DAY) + ":" + minute + " Uhr");
	    return page;
	}
	catch (Exception e)
	{
	    logger.log(Logger.Level.ERROR, "Error inflating Page", e);
	}
	return null;
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
    private View createWeekPage(WeekData weekData)
    {
	// in die Page kommen alle Elemente dieser Ansicht
	View page = inflater.inflate(R.layout.weeklayout, null);

	TableLayout tl = (TableLayout) page.findViewById(R.id.weekTimetable);
	LinearLayoutBordered ll = new LinearLayoutBordered(context);

	// Tagesüberschrift erstellen:
	TableRow tr = new TableRow(context);

	for (int x = 0; x < weekData.timetable[0].length; x++)
	{
	    // einen neuen Rahmen für das Tabellenfeld vorbereiten
	    ll = new LinearLayoutBordered(context);
	    ll.setBorderRight(true);
	    ll.setBorderBottom(true);
	    ll.setBorderTop(true);
	    ll.setBorderSize(1);
	    ll.setBackgroundColor(Color.WHITE);

	    View textview = inflater.inflate(R.layout.textview, null);
	    TextView tv = (TextView) textview.findViewById(R.id.textview);
	    // Überschriftentextgröße einstellen

	    tv.setTextSize(textSize);
	    if (x == 0)
	    {
		tv.setText(timeslots[0]);
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
	if (hideEmptyHours)
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
	if (hideEmptyHours)
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
	    tr = new TableRow(context);

	    for (int x = 0; x < weekData.timetable[y].length; x++)
	    {
		ll = new LinearLayoutBordered(context);
		ll.setBorderRight(true);
		ll.setBorderBottom(true);
		ll.setBorderSize(1);
		ll.setBackgroundColor(Color.WHITE);
		View textview = inflater.inflate(R.layout.textview, null);
		TextView tv = (TextView) textview.findViewById(R.id.textview);
		
		// TextView tv = new TextView(ctxt.context);

		tv.setTextSize(10);
		if (x == 0)
		{
		    tv.setText(timeslots[y]);
		    tv.setTextColor(Color.parseColor("#3A599A"));

		}
		else
		{
		    if (weekData.timetable[y][x].getDataContent() != null && !weekData.timetable[y][x].getDataContent().equalsIgnoreCase("null"))
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
     * @author Tobias Janssen
     * 
     *         Erstellt den Überschriften String
     * @param weekData
     * @param currentWeek
     * @return
     */
    private String createWeekHeader(WeekData weekData, Calendar currentWeek)
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
     * fügt der Liste der Pages und Headlines den übergebenen TimeTable hinzu
     * @author Tobias Janssen
     * 
     * @param weekData
     * @param ctxt
     */
    public void appendTimeTableToPager(WeekData weekData, MyContext ctxt)
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
	if(context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
	{
	    View page = createWeekPage(weekData);
	    insertWeekPage(currentDay, page, createWeekHeader(weekData, currentDay), 0, ctxt.pager.pageIndex.size());

	    // currentDay.roll(Calendar.WEEK_OF_YEAR,true);
	    currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);

	}
	else
	{
	    for (int x = 1; x < weekData.timetable[0].length; x++)
	    {

		List<TimetableViewObject> list = createTimetableDayViewObject(weekData, currentDay);

		View page = createPage(weekData, list);
		insertDayPage( currentDay, page, createDayHeader(weekData, currentDay), 0, ctxt.pager.pageIndex.size());

		// currentDay.roll(Calendar.DAY_OF_YEAR,1);
		currentDay.setTimeInMillis(currentDay.getTimeInMillis() + 86400000);

	    }
	}

    }
    
    /**
     * Kombiniert Jahr und dem DAY_OF_YEAR des übergebenen Calendars einen Integer , der leichter zu vergleichen ist
     * @author Tobias Janssen
     * 
     * @param calendar		Calendar aus dem der Wert erzeugt werden soll
     * @return			Integer mit dem umgerechneten Wert (z.B.: aus Jahr 2013 und dem Tag 300 wird 2013300)
     */
    private int calcIntYearDay(Calendar calendar)
    {
	return (calendar.get(Calendar.YEAR) * 1000) + calendar.get(Calendar.DAY_OF_YEAR);
    }

    
    /**
     * * Erzeugt aus dem WeekData-Objekt eine List aus TimeTableViewObject 
     * @author Tobias Janssen 
     * @param weekData
     * @param currentDay
     * @return
     */
    private List<TimetableViewObject> createTimetableDayViewObject(WeekData weekData, Calendar currentDay)
    {
	
	int x = currentDay.get(Calendar.DAY_OF_WEEK) - 1;
	List<TimetableViewObject> list = new ArrayList<TimetableViewObject>();

	int nullCounter = 0;
	Boolean entryFound = false;
	for (int y = 1; y < weekData.timetable.length; y++)
	{

	    if (weekData.timetable[y][x].getDataContent() == null && !entryFound && hideEmptyHours)
	    {
		nullCounter++;
	    }
	    else if (weekData.timetable[y][x].getDataContent() != null)
	    {
		if (weekData.timetable[y][x].getDataContent().equalsIgnoreCase("null") && !entryFound && hideEmptyHours)
		{
		    nullCounter++;
		}
		else if (weekData.timetable[y][x].getDataContent().equalsIgnoreCase("") && !entryFound && hideEmptyHours)
		{
		    nullCounter++;
		}
		else
		{
		    if (y != 0)
			entryFound = true;
		    if (weekData.timetable[y][x].getDataContent().equalsIgnoreCase("null"))
		    {
			list.add(new TimetableViewObject(timeslots[y], "", "#000000"));
		    }
		    else
		    {
			String color = weekData.timetable[y][x].getColorParameter();
			list.add(new TimetableViewObject(timeslots[y], weekData.timetable[y][x].getDataContent()
				.replaceAll("\n", " "), color));
		    }
		}
	    }
	    else
	    {
		list.add(new TimetableViewObject(timeslots[y], "", "#000000"));
	    }
	}

	if (!hideEmptyHours)
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
     * Gibt den Wert der aktuellen Seitenzahl zurück
     * @return
     */
    public int getCurrentPage()
    {
	return viewPager.getCurrentItem();
    }
    
    /**
     * Gibt das Datum der aktuellen Seite zurück
     * @return
     */
    public Calendar getDateOfCurrentPage()
    {
	return pageIndex.get(getCurrentPage());
    }
    
    /**
     * Setzt den Pager auf die übergebene Seitenzahl
     * @param page
     */
    public void setPage(int page)
    {
	viewPager.setCurrentItem(page);
	viewPager.refreshDrawableState();
    }
    
    public int getPage(Calendar currentDate)
    {
	return getPage(currentDate, size()-1);
    }
    
    /**
     * Liefert den pageIndex des übergegebenen Datums
     * @author janssen
     * @param currentDate
     * @return
     */
    public int getPage(Calendar currentDate, int defaultReturn)
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

	int nextPage = defaultReturn;
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
	    if (context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT)
	    {
		// Tagesansicht
		if (yearPage < yearCurrent)
		{
		    nextPage = i;
		    if (dayOfYearpage < dayOfYearcurrent)
			nextPage = i;
		}
		if ((dayOfYearcurrent == dayOfYearpage) && (yearCurrent == yearPage))
		    return i;
	    }
	    else if (context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
	    {
		// Wochenansicht
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
     * Setzt den Pager auf die Seite mit dem übergebenen Datum
     * @param date
     */
    public void setPage(Calendar date)
    {
	int page = getPage(date);
	viewPager.setCurrentItem(page);
	viewPager.refreshDrawableState();
    }
    
    
}
