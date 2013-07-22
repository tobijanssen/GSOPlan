package de.janssen.android.gsoplan.activities;

import java.util.ArrayList;
import java.util.List;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.core.MyContext;
import de.janssen.android.gsoplan.core.Profil;
import de.janssen.android.gsoplan.dataclasses.ProfilManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.janssen.android.gsoplan.runnables.ErrorMessage;

public class ProfilActivity extends Activity
{
    private MyContext ctxt;
    private int selectorBkP;
    private ProfilManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_profil);
	ctxt = new MyContext(this);
	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
	Boolean profilesFirstView = sp.getBoolean("profilesFirstView", true);
	ctxt.mIsRunning = true;
	if(profilesFirstView)
	{
	    ctxt.handler.post(new ErrorMessage(ctxt, "Info" , this.getString(R.string.msg_profilesFirstView),"OK"));
	    Editor ed = sp.edit();
	    ed.putBoolean("profilesFirstView", false);
	    ed.apply();
	}
	pm = new ProfilManager(ctxt);
	pm.profiles.get(pm.currentProfilIndex).setPrefs();
	createListView();

    }

    @Override
    protected void onPause()
    {
	super.onPause();
	ctxt.mIsRunning = false;
    }

    /**
     * Erstellt/befüllt die ListView, die alle Profile auf dem Bildshirm anzeigt
     */
    private void createListView()
    {
	ListView listview = (ListView) findViewById(R.id.profil_listview);
	List<String> list = new ArrayList<String>();
	for (int i = 0; i < pm.profiles.size(); i++)
	    list.add(pm.profiles.get(i).myElement);

	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, list);
	listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	listview.setAdapter(adapter);
	listview.setItemChecked(pm.currentProfilIndex, true);
	listview.setOnItemLongClickListener(new OnItemLongClickListener()
	{

	    @Override
	    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	    {
		selectorBkP = pm.currentProfilIndex;
		pm.currentProfilIndex = position;
		List<String> choices = new ArrayList<String>();
		choices.add(ProfilActivity.this.getString(R.string.profil_context_btn_edit));
		choices.add(ProfilActivity.this.getString(R.string.profil_context_btn_del));
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfilActivity.this, android.R.layout.simple_list_item_1, choices);
		AlertDialog.Builder builder = new AlertDialog.Builder(ProfilActivity.this);
		builder.setTitle(ProfilActivity.this.getString(R.string.profil_context_title));
		builder.setAdapter(adapter, new OnClickListener()
		{

		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    {
			switch (which)
			{
			case 0:
			    // edit
			    // saveAllProfiles();
			    pm.applyProfilIndex();
			    Intent intent = new Intent(ProfilActivity.this, AppPreferences.class);
			    startActivityForResult(intent, 0);
			    break;
			case 1:
			    // löschen
			    if (pm.profiles.size() > 1)
			    {
				new ErrorMessage(ctxt, pm.profiles.get(pm.currentProfilIndex).myElement + " "+ ctxt.context.getString(R.string.profil_delete_Title), ctxt.context
					.getString(R.string.profil_delete_Text), new OnClickListener()
				{
				    // onPositiveClick
				    @Override
				    public void onClick(DialogInterface dialog, int which)
				    {
					pm.profiles.remove(pm.currentProfilIndex);
					if (pm.currentProfilIndex <= selectorBkP)
					    selectorBkP--;
					pm.currentProfilIndex = selectorBkP;
					createListView();
					pm.applyProfilIndex();
					pm.saveAllProfiles();
				    }

				}, ctxt.context.getString(R.string.profil_delete_btn_positive), new OnClickListener()
				{
				    // onNegativeClick
				    @Override
				    public void onClick(DialogInterface dialog, int which)
				    {
					// nichts zu tun
				    }

				}, ctxt.context.getString(R.string.profil_delete_btn_negative)).run();
			    }
			    else
			    {
				new ErrorMessage(ctxt, ctxt.context.getString(R.string.profil_delete_error_Title), ctxt.context
					.getString(R.string.profil_delete_error_Text), ctxt.context.getString(R.string.profil_delete_error_btn_positive)).run();
			    }
			    break;
			}
		    }
		});

		builder.show();
		return false;
	    }

	});
	listview.setOnItemClickListener(new OnItemClickListener()
	{

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	    {
		selectorBkP = pm.currentProfilIndex;
		pm.currentProfilIndex = position;
		pm.applyProfilIndex();
		finish();
	    }
	});

    }

    /**
     * Wird vom "+" Button aufgerufen
     * 
     * @param arg0
     */
    public void onButtonClick(View arg0)
    {
	if (pm.profiles.size() >= 2)
	{
	    new ErrorMessage(ctxt, ctxt.context.getString(R.string.profil_warning_Title), ctxt.context.getString(R.string.profil_warning_Text),
		    new OnClickListener()
		    {
			// onPositiveClick
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			    addProfil();
			}

		    }, ctxt.context.getString(R.string.profil_warning_btn_positive), new OnClickListener()
		    {
			// onNegativeClick
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			    // nichts zu tun
			}

		    }, ctxt.context.getString(R.string.profil_warning_btn_negative)).run();
	}
	else
	{
	    addProfil();
	}

    }

    /**
     * Fügt den Profilen ein neues hinzu und Startet die Preferences
     */
    private void addProfil()
    {
	pm.profiles.add(new Profil(ctxt));
	pm.currentProfilIndex = pm.profiles.size() - 1;
	pm.applyProfilIndex();
	pm.saveAllProfiles();
	Intent intent = new Intent(ProfilActivity.this, AppPreferences.class);
	startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
	ctxt = new MyContext(this);
	ctxt.mIsRunning = true;
	pm.profiles.get(pm.currentProfilIndex).loadPrefs();
	pm.applyProfilIndex();
	pm.saveAllProfiles();
	createListView();
    }

}
