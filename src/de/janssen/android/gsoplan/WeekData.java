package de.janssen.android.gsoplan;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.text.format.Time;



public class WeekData {
	public String elementId = "";
	public String typeId = "";
	public String weekId = "";
	public Calendar date = new GregorianCalendar();
	public long syncTime = -1L;
	public String weekDataVersion = "";
	public Parameter[] parameters = new Parameter[0];
	public XmlTag[][] timetable;				//wird f�r den Stundenplan ben�tigt
	
	private final String VERSION = "1";
	
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
	/// Autor: Tobias Jan�en
	///
	/// Beschreibung:
	/// F�gt dem WeekData Object weitere Parameter hinzu 
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
