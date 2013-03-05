package listener;

import de.janssen.android.gsoplan.MyContext;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public class OnElementChangeListener implements OnPreferenceChangeListener
{
    private MyContext ctxt;
    private ListPreference list;
    private int fav;
    
    /**
     * Gehört zu AppPreferences
     * @param ctxt	MyContext
     * @param list	ListPreference
     * @param fav	Interger, der den StupidCore index angibt
     */
    public OnElementChangeListener(MyContext ctxt, ListPreference list, int fav)
    {
	this.ctxt=ctxt;
	this.fav=fav;
	this.list=list;
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	try
	{
	    ctxt.stupid[fav].setMyElementValid(newValue.toString());
	    list.setValue(newValue.toString());
	}
	catch (Exception e)
	{
	    // ist ungültig
	}
	return false;

    }

}
