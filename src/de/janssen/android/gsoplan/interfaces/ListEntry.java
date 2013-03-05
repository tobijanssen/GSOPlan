package de.janssen.android.gsoplan.interfaces;

import android.preference.ListPreference;

public class ListEntry
{
    public ListPreference list = null;
    public CharSequence[] entries = null;
    public CharSequence[] vals = null;
    public String prefKey = "";
    
    /**
     * 
     * @param entries
     * @param vals
     * @param prefKey
     */
    public ListEntry(CharSequence[] entries,CharSequence[] vals, String prefKey)
    {
	this.entries=entries;
	this.vals=vals;
	this.prefKey=prefKey;
    }
}
