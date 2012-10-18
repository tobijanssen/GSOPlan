package de.janssen.android.gsoplan;

public class DownloadFeedback {
	public int indexOfData;
	public Boolean refreshData;
	public static final Boolean REFRESH=true;
	public static final Boolean NO_REFRESH=false;
	
	public DownloadFeedback(int indexOfData, Boolean refreshData)
	{
		this.indexOfData=indexOfData;
		this.refreshData=refreshData;
	}

}
