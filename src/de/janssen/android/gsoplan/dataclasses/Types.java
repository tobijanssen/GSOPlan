package de.janssen.android.gsoplan.dataclasses;

import java.util.ArrayList;
import java.util.List;

import de.janssen.android.gsoplan.core.Type;

public class Types
{
    public List<Type> list = new ArrayList<Type>();
    public long htmlModDate = 0;
    
    public Types clone()
    {
	Types result = new Types();
	
	for(int i=0;i<list.size();i++)
	{
	    result.list.add(list.get(i));
	}
	result.htmlModDate = this.htmlModDate;
	return result;
    }
}
