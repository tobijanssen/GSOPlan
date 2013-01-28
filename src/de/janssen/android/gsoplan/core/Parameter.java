/*
 * Parameter.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.core;

public class Parameter
{
    private String name;
    private String value;

    public Parameter(String name, String value)
    {
	this.name = name;
	this.value = value;
    }

    public String getName()
    {
	return this.name;
    }

    public String getValue()
    {
	return this.value;
    }
}
