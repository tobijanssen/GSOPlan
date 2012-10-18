package de.janssen.android.gsoplan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.text.format.Time;



public class WeekData 
{
	public String elementId = "";
	public String typeId = "";
	public String weekId = "";
	public Calendar date = new GregorianCalendar();
	public long syncTime = -1L;
	public String weekDataVersion = "";
	public Boolean isDirty = false;
	public Parameter[] parameters = new Parameter[0];
	public StupidCore parent;
	public XmlTag[][] timetable;				//wird für den Stundenplan benötigt
	
	private final String VERSION = "1";
	
	public WeekData(StupidCore stupidCore)
	{
		this.parent=stupidCore;
	}
	
	public void setSyncDate()
	{
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		syncTime = date.getTime();
		weekDataVersion = VERSION;
		addParameter("syncTime",String.valueOf(date.getTime()));
		addParameter("weekDataVersion",VERSION);
		
	}
	

	/// Datum: 12.09.12
	/// Autor: Tobias Janßen
	///
	/// Beschreibung:
	/// Fügt dem WeekData Object weitere Parameter hinzu 
	///
	///
	/// Parameter:
	/// string name = Der Name/Bezeichnung des Parameters  
	/// string value = Der Wert des Parameters
	///
	public void addParameter(String name,String value)
	{
		Parameter parameter = new Parameter();
		parameter.name = name;
		parameter.value = value;
		
		parameters = (Parameter[]) ArrayOperations.AppendToArray(parameters, parameter);
		
	}
}
