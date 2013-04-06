package de.janssen.android.gsoplan.listener;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public class OnCheckboxChangeListener implements OnPreferenceChangeListener
{
    private Object obj;
    private CheckBoxPreference checkbox;
    private Runnable runOnTrue;
    private Runnable runOnFalse; 
    public OnCheckboxChangeListener(Object objRef, CheckBoxPreference checkbox)
    {
	this.obj = objRef;
	this.checkbox = checkbox;
    }
    public OnCheckboxChangeListener(Object objRef, CheckBoxPreference checkbox, Runnable runOnTrue,Runnable runOnFalse)
    {
	this.obj = objRef;
	this.checkbox = checkbox;
	this.runOnTrue=runOnTrue;
	this.runOnFalse=runOnFalse;
    }
    
  
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	try
	{
	    obj = (Boolean)newValue;
	    checkbox.setChecked((Boolean) obj);
	    if(runOnTrue!=null && (Boolean) obj)
		runOnTrue.run();
	    if(runOnFalse!=null && !(Boolean) obj)
		runOnFalse.run();

	}
	
	catch (Exception e)
	{

	}
	return false;
    }

}
