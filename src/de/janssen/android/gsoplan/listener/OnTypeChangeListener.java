package de.janssen.android.gsoplan.listener;

import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.dataclasses.Types;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class OnTypeChangeListener implements OnPreferenceChangeListener
{
    private MyContext ctxt;
    private ListPreference list;
    private Runnable run;
    private Types typesList;
    /**
     * Gehört zu AppPreferences
     * @param ctxt	MyContext
     * @param list	ListPreference
     * @param run	Runnable die nach fetchonlineSelectors durchgeführt wird(i.d.R. AppPreferences.this)
     */
    public OnTypeChangeListener(MyContext ctxt, ListPreference list, Runnable run,Types typesList)
    {
	this.ctxt=ctxt;
	this.list=list;
	this.run=run;
	this.typesList=typesList;
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	    try
	    {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt.context);
		SharedPreferences.Editor editor = prefs.edit();
		int type = findIndexOfValueInTypeList(newValue.toString(), list);
		editor.putInt("myTypeIndex", type);
		if(typesList != null && typesList.list != null && typesList.list.size() >= type);
		{
		    String typeKey = typesList.list.get(type).type;
		    if(!typeKey.equalsIgnoreCase("null"))
		    {
			editor.putString("myTypeKey", typeKey);
			ctxt.mProfil.myTypeKey=typeKey;
		    }
		    else
			return false;
		    String typeName = typesList.list.get(type).typeName;
		    if(!typeName.equalsIgnoreCase("null"))
		    {
			editor.putString("listType", typeName);
		    	ctxt.mProfil.myTypeName=typeName;
		    }
		    else
			return false;
		}
		ctxt.mProfil.myTypeIndex=type;
		editor.apply();
	    }
	    catch (Exception e)
	    {
		// Type ist ungültig
	    }

	    run.run();
	return false;

    }
    
    private int findIndexOfValueInTypeList(String value, ListPreference list)
    {
	int selected = list.findIndexOfValue(value);
	list.setValueIndex(selected);
	return selected;
    }
    
    
}
