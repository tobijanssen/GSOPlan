/*
 * SelectOptions.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.dataclasses;

public class SelectOptions
{
    public String index = "";
    public String description = "";

    /**
     * 
     * @param index		
     * @param description
     */
    public SelectOptions(String index, String description )
    {
	this.description=description;
	this.index=index;
    }
    
    public SelectOptions()
    {

    }
}
