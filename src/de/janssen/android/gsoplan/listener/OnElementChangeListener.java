package de.janssen.android.gsoplan.listener;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public class OnElementChangeListener implements OnPreferenceChangeListener
{
    private ListPreference list;
    
    /**
     * Gehört zu AppPreferences
     * @param list	ListPreference
     */
    public OnElementChangeListener(ListPreference list)
    {
	this.list=list;
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	try
	{
	    list.setValue(newValue.toString());
	    
	}
	catch (Exception e)
	{
	    // ist ungültig
	}
	return false;

    }

}
