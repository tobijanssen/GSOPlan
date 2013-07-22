/*
 * XmlTag.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.xml;

import android.app.ProgressDialog;
import de.janssen.android.gsoplan.ArrayOperations;
import de.janssen.android.gsoplan.dataclasses.Const;
import de.janssen.android.gsoplan.dataclasses.Parameter;

public class Xml
{
    public static final String SYNCTIME = "syncTime";
    public static final String ELEMENTS = "elements";
    public static final String ELEMENT = "element";
    public static final String WEEK = "week";
    public static final String WEEKID = "weekId";
    public static final String TYPES = "types";
    public static final String TYPE = "type";
    public static final String UNSET = "unset";
    public static final String OPTION = "option";
    public static final String TR = "tr";
    public static final String SCRIPT = "script";
    public static final String FONT = "font";
    public static final String TABLE = "table";
    public static final String PROFILES = "profiles";
    public static final String PROFIL = "profil";
    public static final String HTMLMOD = "htmlmodified";
    
    
    
    private String type;
    private String dataContent;
    private Boolean open = true;
    private Parameter[] parameters = new Parameter[0];
    private Boolean isEndTag = false;
    private Xml[] childTags = new Xml[0];
    private Xml parentTag;
    private int sumerizeId = 0;
    private Xml currentTag;

    public Xml(String type, String dataContent)
    {
	this.type = type;
	this.dataContent = dataContent;
    }

    public Xml(String type)
    {
	this.type = type;
    }

    public void setType(String type)
    {
	this.type = type;
    }

    public String getType()
    {
	return this.type;
    }

    public void setDataContent(String dataContent)
    {
	this.dataContent = dataContent;
    }

    public String getDataContent()
    {
	return this.dataContent;
    }

    // public Boolean open = true;
    public void setParameters(Parameter[] parameters)
    {
	this.parameters = parameters;
    }

    public Parameter[] getParameters()
    {
	return this.parameters;
    }

    public Parameter getParameterAtIndex(int i)
    {
	return this.parameters[i];
    }

    public void setIsEndTag(Boolean value)
    {
	this.isEndTag = value;
    }

    public Boolean setIsEndTag()
    {
	return this.isEndTag;
    }

    public void setChildTags(Xml[] xmlChilds)
    {
	this.childTags = xmlChilds;
    }

    public Xml[] getChildTags()
    {
	return this.childTags;
    }

    public Xml getChildTagAtIndex(int i)
    {
	return this.childTags[i];
    }

    public void setParentTag(Xml parentXml)
    {
	this.parentTag = parentXml;
    }

    public Xml getParentTag()
    {
	return this.parentTag;
    }

    public int getRandomId()
    {
	return this.sumerizeId;
    }

    public void setRandomId(int id)
    {
	this.sumerizeId = id;
    }

    /*
     * @author Tobias Janssen Parsed den String dataContent des xml zu
     * Xml-ChildTags
     * 
     * 
     * @return XmlTag-Array das die konvertierten Daten enthält
     */
    public void parseXml() throws Exception
    {
	if (this.dataContent.isEmpty())
	    throw new Exception(Const.ERROR_XMLFAILURE + "dataContent is Empty");

	this.childTags = new Xml[0]; // das Array, das die Tags des Quelltextes
				     // enthalten wird

	
	// jetzt alle tags auslesen, bis alle Tags wieder geschlossen sind:
	do
	{
	    // den nächsten Tag abholen & hinzufügen:
	    try
	    {
		this.childTags = AddTagToArray(this.childTags, parseNextXmlTag());
	    }
	    catch(Exception e)
	    {
		//Fehler im XML: hier abbrechen
		break;
	    }

	} while (this.dataContent.length() > 1);
    }

    /*
     * @author Tobias Janssen Parsed den String dataContent des xml zu
     * Xml-ChildTags mit einem ProgressDialog
     * 
     * 
     * @return XmlTag-Array das die konvertierten Daten enthält
     */
    public void parseXml(ProgressDialog pd) throws Exception
    {
	if (this.dataContent.isEmpty())
	    throw new Exception("Keine Daten gefunden");

	this.childTags = new Xml[0]; // das Array, das die Tags des Quelltextes
	// enthalten wird

	// jetzt alle tags auslesen, bis der Body wieder geschlossen wird:
	do
	{
	    // den nächsten Tag abholen:
	    try
	    {

		pd.setProgress(pd.getProgress() + 50);
		this.childTags = AddTagToArray(this.childTags, parseNextXmlTag());

	    }
	    catch (Exception e)
	    {
		// TODO:Theres an Problem reading the HTML
	    }

	} while (this.dataContent.length() > 1);
    }

    /*
     * @author Tobias Janssen
     * 
     * Sucht sich im Xml-Quelltext den nächsten Tag und löst diesen in ein
     * Tag-Objekt auf.
     * 
     * @param xml Xml Objekt indem gesucht werden soll
     * 
     * @return das Suchergebnis
     */
    private Xml parseNextXmlTag() throws Exception
    {
	// variablen deklarieren:
	int startPoint = 0;
	int endTagPoint = 0;

	// Den Beginn eines Tags suchen:
	startPoint = dataContent.indexOf("<", 0);

	// prüfen, ob überhaupt ein Tag vorliegt:
	if (startPoint == -1)
	{
	    throw new Exception(Const.ERROR_XMLFAILURE);
	}

	// der Text davor wird verworfen:
	dataContent = dataContent.substring(startPoint);

	// StartPoint korrigieren, da der Text abgeschnitten wurde:
	startPoint = 0;

	// ende des StartTags herausfinden:
	endTagPoint = dataContent.indexOf(">", startPoint) + 1;
	// prüfen, ob das tag valid ist, indem geprüft wird, ob bereits ein
	// neues tag geöffnet wurde
	int nextStartPoint = dataContent.indexOf("<", 1);
	if (nextStartPoint != -1 && (nextStartPoint < endTagPoint))
	{
	    // hier muss ein quelltext syntax-fehler vorliegen
	    // dieses tag verwerfen
	    dataContent = dataContent.substring(nextStartPoint);
	    return parseNextXmlTag();
	}

	if (endTagPoint == -1)
	{
	    // Tag ist nicht vernüftig geclosed worden
	    throw new Exception(Const.ERROR_XMLFAILURE);
	}

	String tagContent = dataContent.substring(0, endTagPoint);
	//escape-zeichen aus dem Tag entfernen
	tagContent=tagContent.replaceAll("\n", " ");
	tagContent=tagContent.replaceAll("\t", " ");
	tagContent=tagContent.replaceAll("\r", " ");
	dataContent = dataContent.substring(endTagPoint);

	Xml tag;
	try
	{
	    tag = decodeXmlTag(tagContent);
	}
	catch (Exception e)
	{
	    throw e;
	}

	// prüfen, ob daten vorhanden sind
	// sonderfall javascript berücksichtigen:
	int nextTag;
	if (tag.type.equalsIgnoreCase("script") && !tag.isEndTag)
	{
	    nextTag = dataContent.indexOf("</script>", 0);
	}
	else
	{
	    nextTag = dataContent.indexOf("<", 0);
	}
	if (tag.type.equalsIgnoreCase("meta"))
	    tag.open = false;
	if (tag.type.equalsIgnoreCase("link"))
	    tag.open = false;
	if (tag.type.equalsIgnoreCase("br"))
	    tag.open = false;

	if (nextTag > 0)
	{
	    // daten vorhanden
	    tag.dataContent = dataContent.substring(0, nextTag);
	}
	if (nextTag != -1)
	{
	    dataContent = dataContent.substring(nextTag);
	}
	else
	{
	    if (dataContent.length() > 0)
		dataContent = dataContent.substring(1);
	}
	return tag;
    }

    /*
     * @author Tobias Janssen Fügt dem XmlArray das übergebene xml-tag zu.
     * 
     * @param tag XmlTag, das dem array hinzugefügt werden soll
     * 
     * @return XmlTag-Array mit der XmlStruktur
     */
    private Xml[] AddTagToArray(Xml[] array, Xml tag)
    {
	// prüfen ob ein Schließer Tag vorliegt

	if (tag.isEndTag == true)
	{
	    Xml backupTag = this.clone();
	    Boolean breakout = false;
	    // den öffner von diesem Tag suchen und auf open=false setzen
	    do
	    {
		if (currentTag.type.equalsIgnoreCase(tag.type) && currentTag.isEndTag == false)
		{
		    // öffner element gefunden, nun schließen
		    currentTag.open = false;
		}
		else
		{
		    if (currentTag.parentTag == null)
		    {
			currentTag = backupTag.parentTag;
			breakout = true;
		    }
		    else
		    {
			currentTag = currentTag.parentTag;
		    }
		}
	    } while (currentTag.open && !breakout);
	}
	// ansonsten muss das Element an den letzten offenen tag angefügt werden
	else if (array.length > 0)
	{
	    // den letzten offenen tag finden

	    if (currentTag.open == true && currentTag.isEndTag == false)
	    {
		// öffner element gefunden, nun hinzufügen
		currentTag.childTags = (Xml[]) ArrayOperations.ResizeArray(currentTag.childTags, currentTag.childTags.length + 1);
		tag.parentTag = currentTag;
		currentTag.childTags[currentTag.childTags.length - 1] = tag;
		if (tag.open)
		{
		    currentTag = tag;
		}
	    }
	    else if (currentTag.open == false && currentTag.parentTag == null)
	    {
		// wir sind ganz oben angekommen
		array = (Xml[]) ArrayOperations.ResizeArray(array, array.length + 1);

		// array[array.length - 1] = new Xml(); // neues Objekt anlegen
		if (tag.isEndTag)
		{
		    tag.open = false;
		}
		array[array.length - 1] = tag; // tag hinzufügen
		currentTag = tag;
	    }
	    else if (currentTag.open == false && currentTag.isEndTag == false)
	    {
		// der parent ist bereits geschlossen, also den offenenParent
		// suchen und als child hinzufügen
		while (currentTag.open == false && currentTag.parentTag != null)
		{
		    currentTag = currentTag.parentTag;
		}
		currentTag.childTags = (Xml[]) ArrayOperations.ResizeArray(currentTag.childTags,
			currentTag.childTags.length + 1);
		
		tag.parentTag = currentTag;
		currentTag.childTags[currentTag.childTags.length - 1] = tag;
		currentTag = tag;
	    }
	}
	else
	{
	    array = (Xml[]) ArrayOperations.ResizeArray(array, array.length + 1);// Array
										 // vergößern
	    if (tag.isEndTag)
	    {
		tag.open = false;
	    }
	    array[array.length - 1] = tag; // tag hinzufügen
	    currentTag = tag;
	}
	return array;
    }

    /*
     * @author Tobias Janssen
     */
    private static Xml decodeXmlTag(String xmlCode) throws Exception
    {
	Xml tag = new Xml("unknownType");
	try
	{
	    if (xmlCode.substring(1, 2).equalsIgnoreCase("/"))
	    {
		tag.isEndTag = true;
		xmlCode = "<" + xmlCode.substring(2);
	    }
	    String c = xmlCode.substring(0, 3);
	    if (c.equalsIgnoreCase("<!-") && xmlCode.length() > 6)
	    {
		tag.open = false;
		tag.type = "Comment";
		tag.dataContent = xmlCode.substring(3, xmlCode.indexOf("-->", 1));

	    }
	    c = xmlCode.substring(0, 2);
	    if (c.equalsIgnoreCase("<!") && tag.type.equalsIgnoreCase("unknownType"))
	    {
		tag.open = false;
		tag.type = "Doctype";
		tag.dataContent = xmlCode.substring(2, xmlCode.indexOf(">", 1));

	    }
	}
	catch (Exception e)
	{
	    throw new Exception(Const.ERROR_XMLFAILURE);
	}
	int parameterPoint = xmlCode.indexOf("=", 0);
	if (parameterPoint == -1 && tag.type.equalsIgnoreCase("unknownType"))
	{
	    // keine parameter
	    tag.type = xmlCode.substring(1, xmlCode.length() - 1);
	}
	else if (parameterPoint > 0)
	{
	    // parametername:
	    Boolean endReached = false;
	    int spacer = 0;
	    int parameterEnd = 0;
	    spacer = xmlCode.indexOf(" ", 0);
	    tag.type = xmlCode.substring(1, spacer);
	    // abscheniden:
	    xmlCode = xmlCode.substring(spacer);

	    do
	    {
		String testChar = xmlCode.substring(0, 1);
		while (testChar.equalsIgnoreCase(" "))
		{
		    xmlCode = xmlCode.substring(1);
		    testChar = xmlCode.substring(0, 1);
		}

		parameterPoint = xmlCode.indexOf("=", 0);
		// parameternamen auslesen:

		String name = xmlCode.substring(0, parameterPoint);
		// abschneidne
		xmlCode = xmlCode.substring(parameterPoint + 1);
		// Anführungszeichen lesen:
		Parameter parameter;
		try
		{
		    String quote = xmlCode.substring(0, 1);
		    String value = "";
		    if (quote.equalsIgnoreCase("'") || quote.equalsIgnoreCase("\""))
		    {
			parameterEnd = xmlCode.indexOf(quote, 1);
			value = xmlCode.substring(1, parameterEnd);
		    }
		    else
		    // parameter ist nicht gequotet!
		    {
			parameterEnd = xmlCode.indexOf(" ", 1);
			if (parameterEnd == -1)
			{
			    parameterEnd = xmlCode.length() - 1;
			}
			value = xmlCode.substring(0, parameterEnd);
		    }
		    parameter = new Parameter(name, value);
		}
		catch (Exception e)
		{
		    throw new Exception(Const.ERROR_XMLFAILURE);
		}

		// abschneiden:
		xmlCode = xmlCode.substring(parameterEnd);

		// dem array hinzufügen:
		tag.parameters = (Parameter[]) ArrayOperations.ResizeArray(tag.parameters, tag.parameters.length + 1);
		tag.parameters[tag.parameters.length - 1] = parameter;

		// nächstes Zeichen prüfen:
		testChar = xmlCode.substring(0, 1);
		while (testChar.equalsIgnoreCase(" ") || testChar.equalsIgnoreCase("\"")
			|| testChar.equalsIgnoreCase("'"))
		{
		    xmlCode = xmlCode.substring(1);
		    testChar = xmlCode.substring(0, 1);
		}
		if (testChar.equalsIgnoreCase("/"))
		{
		    tag.open = false;
		}
		if (testChar.equalsIgnoreCase(">"))
		{
		    endReached = true;
		}
	    } while (!endReached && tag.open);

	}
	return tag;
    }

    /*
     * @author Tobias Janssen Cloned das Xml objekt
     */
    public Xml clone()
    {
	Xml clone = new Xml("clone");
	clone.setChildTags(this.childTags);
	clone.setDataContent(this.dataContent);
	clone.setIsEndTag(this.isEndTag);
	clone.setParameters(this.parameters);
	clone.setParentTag(this.parentTag);
	clone.setType(this.type);

	return clone;
    }

    /*
     * @author Tobias Janssen Fügt den Parametern ein weteres Element hinzu
     * 
     * @param name String Bezeichnung für den Parameter
     * 
     * @param value String Value für den Parameter
     */
    public void addParameter(String name, String value)
    {
	Parameter parameter = new Parameter(name, value);

	this.parameters = (Parameter[]) ArrayOperations.AppendToArray(this.parameters, parameter);

    }

    /*
     * @author Tobias Janssen Sucht in dem Xml Objekt nach einem ColorParameter
     */
    public String getColorParameter()
    {
	for (int x = 0; x < this.parameters.length; x++)
	{
	    if (this.parameters[x] != null && this.parameters[x].getName().equalsIgnoreCase("color"))
	    {
		return this.parameters[x].getValue();
	    }
	}
	return "#000000";
    }
}