package de.janssen.android.gsoplan;

import java.util.ArrayList;
import java.util.List;

import de.janssen.android.gsoplan.core.Parameter;

public class Subjects
{
    public List<Parameter> list = new ArrayList<Parameter>();
    private String type; 
    
    public Subjects(String type)
    {
	this.type=type;
    }

    public String toXml()
    {
	String result="";
	Parameter subject;
	for(int i=0;i<list.size();i++)
	{
	    subject = list.get(i);
	    result+="<+"+type+" id='"+subject.getName()+"'>"+subject.getValue()+"</"+type+"\n";
	}
	return result;
    }
    public void addSubject(Parameter parameter)
    {
	list.add(parameter);
    }
    
    
}
