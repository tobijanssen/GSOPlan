package de.janssen.android.gsoplan.Runnables;

import java.util.Calendar;
import java.util.Date;

import android.widget.TextView;
import de.janssen.android.gsoplan.PlanActivity;
import de.janssen.android.gsoplan.R;

public class UpdateHeaderNFooter implements Runnable {

	private PlanActivity parent;

	public UpdateHeaderNFooter(PlanActivity parent)
	{
		this.parent=parent;
	}
	@Override
	public void run() {

		//Calendar weekDate = myTimeTableIndex[weekDataIndexToShow].date;
		int actDay = parent.stupid.currentDate.get(Calendar.DAY_OF_MONTH);
		int actMonth = parent.stupid.currentDate.get(Calendar.MONTH)+1;
		int actYear = parent.stupid.currentDate.get(Calendar.YEAR);

		int currentIndex = parent.stupid.myTimetables[parent.weekDataIndexToShow].indexKey;
		String dayName = parent.stupid.stupidData[currentIndex].timetable[0][parent.stupid.currentDate.get(Calendar.DAY_OF_WEEK)-1].dataContent
				.replace("\n", "");
		Date time = new Date(
				parent.stupid.stupidData[currentIndex].syncTime);
		TextView bottom = (TextView) parent.findViewById(R.id.bottom);
		bottom.setText(dayName + ", " + actDay + "." + actMonth
				+ "." + actYear + "\n"
				+ parent.stupid.stupidData[currentIndex].elementId);
		TextView syncTime = (TextView) parent.findViewById(R.id.syncTime);
		syncTime.setText("Stand vom: " + time.toString());
	}

}
