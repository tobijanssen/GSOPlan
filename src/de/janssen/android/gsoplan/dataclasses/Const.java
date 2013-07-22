/*
 * Const.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */

package de.janssen.android.gsoplan.dataclasses;

public class Const
{
    public static final int THISWEEK = 0;
    public static final int NEXTWEEK = 1;
    public static final int LASTWEEK = -1;
    public static final int SELECTEDWEEK = 2;
    public static final int TEXTSIZEOFHEADLINES = 12;
    public static final Boolean FORCEREFRESH = true;
    public final static String FILEPROFILES = "Profiles.xml";
    public final static String FILETYPE = "Type.xml";
    public final static String FILEDATA = "Data.xml";
    public final static String FILEVERSION = "Version.xml";
    public final static String FIRSTSTART = "FirstStart";
    public final static int CONNECTIONTIMEOUT = 5000;
    public static final String XMLVERSION = "1";
    public final static String CHECKBOXPROFILID = "checkboxUseFav";
    public static final String NAVBARURL = "http://stupid.gso-koeln.de/frames/navbar.htm";
    public static final String URLSTUPID = "http://stupid.gso-koeln.de/";
    public static final String BROADCASTREFRESH = "broadcast_refresh";
    public static final String NOTIFICATESYNC = "aktualisiert Stundenpl‰ne";
    public static final String NOTIFICATESYNCHEAD = "GSOPlan";
    public static final String NOTIFICATESYNCSHORT = " wird synchronisiert";
    

    public final static String ERROR_XMLFAILURE = "Fehler bei der XML Konvertierung!";
    public final static String ERROR_NOSERVER = "Es konnte keine Verbindung zum Server hergestellt werden!";
    public final static String ERROR_NONET = "Fehler beim Verbindungsaufbau!";
    public final static String ERROR_CONNTIMEOUT = "Verbindungs-Timeout! Server nicht erreichbar!";
    public final static String ERROR_NOTIMETABLE_FOR_REFRESH = "Es existiert noch kein Stundenplan, der Aktualisiert werden kann!\nBitte kontrollieren Sie die Internetverbindung und anschlieﬂend die Einstellungen!";
}
