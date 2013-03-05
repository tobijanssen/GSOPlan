/*
 * Const.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan;

public class Const
{
    public static final int THISWEEK = 0;
    public static final int NEXTWEEK = 1;
    public static final int LASTWEEK = -1;
    public static final int SELECTEDWEEK = 2;
    public static final Boolean FORCEREFRESH = true;
    public final static String FILEELEMENT = "Elements.xml";
    public final static String FILEDATA = "Data.xml";
    public final static String FILEVERSION = "Version.xml";
    public final static String FIRSTSTART = "FirstStart";
    public final static int CONNECTIONTIMEOUT = 5000;
    public final static String CHECKBOXPROFILID = "checkboxUseFav";

    public final static String ERROR_XMLFAILURE = "Die XML Konvertierung hat nicht geklappt!\nBitte erneut versuchen!";
    public final static String ERROR_NOSERVER = "Keine Verbindung zum Server!\nEs wird eine Internetverbindung benötigt!";
    public final static String ERROR_CONNTIMEOUT = "Verbindungs-Timeout! Server nicht erreichbar!";
    public final static String ERROR_NOTIMETABLE_FOR_REFRESH = "Es existiert noch kein Stundenplan, der Aktualisiert werden kann!\nBitte kontrollieren Sie die Internetverbindung und anschließend die Einstellungen!";
}
