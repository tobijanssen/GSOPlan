/*
 * Xml.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.HtmlResponse;

public class XmlOPs
{

    public XmlOPs()
    {
    }

    /**
     * Erstellt ein neues Xml Objekt mit Inhalt
     * @author Tobias Janssen 
     * 
     * @param container String mit XML-Inhalt, der verarbeiter werden soll
     */
    public XmlOPs(String container)
    {
	this.container = container;
    }

    public String container = ""; // Enthält den noch zu verarbeitenden XmlText


   
    
    /**
     * 
     * Liest Daten aus der URL aus, erhöht dabei den ProgressDialog
     * @author janssen
     * @param url			URL
     * @param timeoutMillis		int mit angabe des Timeouts in Millis
     * @return				String mit Inhalt der URL
     * @throws Exception		wenn Verbindung fehlgeschlagen ist
     */
    public static String readFromURL(URL url, int timeoutMillis) throws Exception
    {
	
	InputStreamReader inStream = null;
	HttpURLConnection conn = null;
	String xmlString = "";
	try
	{
	    conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestProperty("content-type", "text/plain; charset=iso-8859-1");
	    conn.setConnectTimeout(timeoutMillis);
	    conn.connect();
	    inStream = new InputStreamReader(conn.getInputStream(), "iso-8859-1");
	    xmlString = readFromHtmlStream(inStream);
	}
	catch (SocketTimeoutException e)
	{
	    throw new Exception(Const.ERROR_CONNTIMEOUT);

	}
	catch (IOException e)
	{
	    throw new Exception(Const.ERROR_NOSERVER);
	}
	catch (Exception e)
	{
	    throw new Exception(Const.ERROR_NOSERVER);
	}
	finally
	{
	    if (conn != null)
		conn.disconnect();
	    if (inStream != null)
		inStream.close();
	}

	return xmlString;
    }
    
    /**
     * 
     * Liest Daten aus der URL aus, erhöht dabei den ProgressDialog
     * @author janssen
     * @param url			URL
     * @param timeoutMillis		int mit angabe des Timeouts in Millis
     * @return				String mit Inhalt der URL
     * @throws Exception		wenn Verbindung fehlgeschlagen ist
     */
    public static void readFromURLIfModified(URL url, int timeoutMillis, HtmlResponse lastResponse, HtmlResponse htmlResponse) throws Exception
    {
	
	InputStreamReader inStream = null;
	HttpURLConnection conn = null;
	try
	{
	    conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestProperty("content-type", "text/plain; charset=iso-8859-1");
	    conn.setConnectTimeout(timeoutMillis);
	    conn.connect();
	    htmlResponse.dataReceived = false;
	    htmlResponse.lastModified = conn.getLastModified();
	    htmlResponse.contentLength = conn.getContentLength();
	    if(htmlResponse.lastModified != lastResponse.lastModified)
	    {
		inStream = new InputStreamReader(conn.getInputStream(), "iso-8859-1");
		htmlResponse.xmlContent = readFromHtmlStream(inStream);
		htmlResponse.dataReceived = true;
	    }
	}
	catch (SocketTimeoutException e)
	{
	    throw new Exception(Const.ERROR_CONNTIMEOUT);

	}
	catch (IOException e)
	{
	    throw new Exception(Const.ERROR_NOSERVER);
	}
	catch (Exception e)
	{
	    throw new Exception(Const.ERROR_NOSERVER);
	}
	finally
	{
	    if (conn != null)
		conn.disconnect();
	    if (inStream != null)
		inStream.close();
	}
    }
    
    
    
    
    /**
     * @author Tobias Janssen
     * @param is		InputStreamReader
     * @return			String mit Inhalt des Streams
     * @throws IOException
     */
    private static String readFromHtmlStream(InputStreamReader is) throws IOException
    {
	try
	{

	    java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
	    char data[] = new char[1024];
	    int count = 0;
	    while ((count = is.read(data)) != -1)
	    {
		cw.write(data, 0, count);
	    }
	    return cw.toString();
	}
	catch (IOException e)
	{
	    throw new IOException(Const.ERROR_NOSERVER);
	}
    }

    
}
