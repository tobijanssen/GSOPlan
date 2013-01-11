/*
 * WeekData.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.core;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.janssen.android.gsoplan.ArrayOperations;
import de.janssen.android.gsoplan.xml.XmlTag;



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
	public Stupid parent;
	public XmlTag[][] timetable;				//wird für den Stundenplan benötigt
	
	private final String VERSION = "1";
	
	public WeekData(Stupid stupidCore)
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
	/// Autor: @author Tobias Janssen
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
		int index=-1;
		for(int i=0; i<parameters.length && index == -1;i++)
		{
			if(parameters[i].name.equalsIgnoreCase(parameter.name))
			{
				index=i;
			}
		}
		if(index == -1)
		{
			parameters = (Parameter[]) ArrayOperations.AppendToArray(parameters, parameter);
		}
		else
		{
			parameters[index]=parameter;
		}
		
	}
}
