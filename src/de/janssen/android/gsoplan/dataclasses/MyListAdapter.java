/*
 * MyListAdapter.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.dataclasses;

import java.util.List;
import de.janssen.android.gsoplan.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyListAdapter extends ArrayAdapter<TimetableViewObject>
{
    private final Context ctxt;
    private final List<TimetableViewObject> values1;

    public MyListAdapter(Context ctxt, List<TimetableViewObject> values1)
    {
	super(ctxt, R.layout.rowlayout, values1);
	this.ctxt = ctxt;
	this.values1 = values1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
	LayoutInflater inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View rowView = inflater.inflate(R.layout.rowlayout, null);
	TextView textView = (TextView) rowView.findViewById(R.id.label);

	TextView textView2 = (TextView) rowView.findViewById(R.id.text);

	TimetableViewObject content = (TimetableViewObject) values1.get(position);
	textView.setText(content.row1);
	textView2.setText(content.row2);
	if (content.color.length() == 7)
	{
	    // TODO: prüfen, ob der string eine valide Farbe enthält

	    textView2.setTextColor(Color.parseColor(content.color));
	}
	else
	{
	    // wrong color declaration
	    textView2.setTextColor(Color.BLACK);
	}

	return rowView;
    }

}
