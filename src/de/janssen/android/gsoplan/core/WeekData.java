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
import de.janssen.android.gsoplan.xml.Xml;

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
    public Xml[][] timetable; // wird für den Stundenplan benötigt

    private final String VERSION = "1";

    public WeekData(Stupid stupidCore)
    {
	this.parent = stupidCore;
    }

    /**
     * @author Tobias Janssen
     */
    public void setSyncDate()
    {
	Calendar cal = Calendar.getInstance();
	Date date = cal.getTime();
	syncTime = date.getTime();
	weekDataVersion = VERSION;
	addParameter("syncTime", String.valueOf(date.getTime()));
	addParameter("weekDataVersion", VERSION);

    }

    /**
     * @author Tobias Janssen
     * Fügt dem WeekData Object weitere Parameter hinzu
     * @param name		String der Name/Bezeichnung des Parameters
     * @param value		String der Wert des Parameters
     */
    public void addParameter(String name, String value)
    {
	Parameter parameter = new Parameter(name, value);
	int index = -1;
	for (int i = 0; i < parameters.length && index == -1; i++)
	{
	    if (parameters[i].getName().equalsIgnoreCase(parameter.getName()))
	    {
		index = i;
	    }
	}
	if (index == -1)
	{
	    parameters = (Parameter[]) ArrayOperations.AppendToArray(parameters, parameter);
	}
	else
	{
	    parameters[index] = parameter;
	}

    }
}
