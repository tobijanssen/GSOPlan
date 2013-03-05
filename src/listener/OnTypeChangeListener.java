package listener;

import de.janssen.android.gsoplan.MyContext;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public class OnTypeChangeListener implements OnPreferenceChangeListener,Runnable
{
    private MyContext ctxt;
    private ListPreference list;
    private int index;
    private Runnable run;
    private int selectorBkp;
    
    /**
     * Gehört zu AppPreferences
     * @param ctxt	MyContext
     * @param list	ListPreference
     * @param index	Interger, der den StupidCore index angibt
     * @param run	Runnable die nach fetchonlineSelectors durchgeführt wird(i.d.R. AppPreferences.this)
     */
    public OnTypeChangeListener(MyContext ctxt, ListPreference list, int index, Runnable run)
    {
	this.ctxt=ctxt;
	this.index=index;
	this.list=list;
	this.run=run;
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	    try
	    {
		ctxt.switchStupidTo(index);
		int type = findIndexOfValueInTypeList(newValue.toString(), list);
		ctxt.stupid[index].setMyType(type);
	    }
	    catch (Exception e)
	    {
		// Type ist ungültig
	    }
	    ctxt.getCurStupid().fetchOnlineSelectors(ctxt,this);
	return false;

    }
    
    private int findIndexOfValueInTypeList(String value, ListPreference list)
    {
	int selected = list.findIndexOfValue(value);
	list.setValueIndex(selected);
	return selected;
    }

    @Override
    public void run()
    {
	ctxt.switchStupidBack();
	run.run();
    }
    
    
}
