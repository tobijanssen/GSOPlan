package de.janssen.android.gsoplan.core;

import java.util.ArrayList;
import java.util.List;

import de.janssen.android.gsoplan.dataclasses.SelectOptions;

public class Type
{
    public List<SelectOptions> elementList = new ArrayList<SelectOptions>();
    public List<SelectOptions> weekList = new ArrayList<SelectOptions>();
    public String typeName;
    public String type;
    
    
    /**
     * Liefert den Index passenend zu der
     * angegebenen KW aus den Online verfügaberen Wochen zurück
     * 
     * Wenn online nicht verfügbar, wird -1 zurückgeliefert
     * @autor: @author Tobias Janssen 
     * @param weekOfYear
     * @return int
     */
    @Deprecated
    public int getIndexFromWeekList(String weekOfYear)
    {
	for (int i = 0; i < this.weekList.size(); i++)
	{
	    if (weekOfYear.equalsIgnoreCase(this.weekList.get(i).index))
		return i;
	}
	return -1;
    }
}
