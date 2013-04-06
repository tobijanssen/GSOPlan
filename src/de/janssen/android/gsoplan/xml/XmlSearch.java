package de.janssen.android.gsoplan.xml;

import de.janssen.android.gsoplan.ArrayOperations;
import de.janssen.android.gsoplan.dataclasses.Parameter;

public class XmlSearch
{
    private Xml tagCrawlerResult;
    private Xml[] tagCrawlerResultArray;

    /**
     * @author Tobias Janssen Sucht in dem angegebenen XmlTag abwärts nach dem
     *         gewünschten tagType
     * @param xmlResource
     *            Xml in dem gesucht werden soll
     * @param xmlSearch
     *            Xml der den zu suchenden Tag-Typen enthält
     * @return Xml mit Suchergebnis
     */
    public Xml tagCrawlerFindFirstOf(Xml xmlResource, Xml xmlSearch)
    {
	return tagCrawlerFindFirstOf(xmlResource, xmlSearch, false);
    }

    /**
     * @author Tobias Janssen Sucht in dem angegebenen XmlTag abwärts nach dem
     *         gewünschten tagType
     * @param xmlResource
     *            Xml in dem gesucht werden soll
     * @param xmlSearch
     *            Xml der den zu suchenden Tag-Typen enthält
     * @return Xml mit Suchergebnis
     */
    public Xml tagCrawlerFindFirstOf(Xml xmlResource, Xml xmlSearch, Boolean upwards)
    {
	// prüfen, ob das resultArray bereits initialisiert wurde:
	if (tagCrawlerResult != null)
	{
	    // resultArray initialisieren
	    return tagCrawlerResult;
	}

	// prüfen, ob der aktuelle HtmlTag dem zu suchenden Tag entspricht
	if (compareTypes(xmlResource, xmlSearch) && compareContent(xmlResource, xmlSearch)
		&& compareParameter(xmlResource, xmlSearch))
	{
	    // fund
	    return xmlResource;
	}
	if (!upwards)
	{
	    // prüfen, ob es noch untergeordnete Tags gibt:
	    if (xmlResource.getChildTags().length > 0)
	    {
		// alle childTags per Rekursion prüfen:
		for (int i = 0; i < xmlResource.getChildTags().length && tagCrawlerResult == null; i++)
		{
		    tagCrawlerResult = tagCrawlerFindFirstOf(xmlResource.getChildTagAtIndex(i), xmlSearch, upwards);
		}
	    }
	}
	else
	{
	    if(xmlResource.getParentTag() != null)
	    {
	    // den parentTags per Rekursion prüfen:
		tagCrawlerResult = tagCrawlerFindFirstOf(xmlResource.getParentTag(), xmlSearch, upwards);
	    }
	}
	return tagCrawlerResult;
    }

    /**
     * Vergleicht die beiden Xml Objekte anhand des Typs.
     * <p>
     * gibt ein true aus, wenn das suchobjekt einen ungesetzen typ hat,
     * <p>
     * oder eine übereinstimming vorhanden ist
     * 
     * @author Tobias Janssen
     * @param xmlResource
     *            Xml 1
     * @param xmlSearch
     *            Xml 2
     * @return true, wenn gleicher Typ
     */
    private Boolean compareTypes(Xml xmlResource, Xml xmlSearch)
    {
	// prüfen, ob der aktuelle xmlResourceTag dem zu suchenden Tag
	// entspricht
	if (xmlSearch.getType() == null)
	    // nein, soll nicht geprüpft werden, daher wird ein match ausgegeben
	    return true;
	else if (xmlSearch.getType().equalsIgnoreCase(Xml.UNSET))
	    // nein, soll nicht geprüpft werden, daher wird ein match ausgegeben
	    return true;
	else if (xmlResource.getType().equalsIgnoreCase(xmlSearch.getType()))
	    return true;

	return false;
    }

    /**
     * Vergleicht die beiden Xml Objekte anhand des Contents.
     * <p>
     * gibt ein true aus, wenn das suchobjekt einen ungesetzen content hat,
     * <p>
     * oder eine übereinstimming vorhanden ist
     * 
     * @author Tobias Janssen
     * @param xmlResource
     * @param xmlSearch
     * @return
     */
    private Boolean compareContent(Xml xmlResource, Xml xmlSearch)
    {
	// prüfen, ob der der DataContent geprüft werden soll
	if (xmlSearch.getDataContent() == null)
	    // nein, soll nicht geprüpft werden, daher wird ein match ausgegeben
	    return true;
	else if (xmlSearch.getDataContent().equalsIgnoreCase(""))
	    // nein, soll nicht geprüpft werden, daher wird ein match ausgegeben
	    return true;
	else if (xmlResource.getDataContent() != null
		&& xmlResource.getDataContent().contains(xmlSearch.getDataContent()))
	    return true;

	return false;
    }

    /**
     * Vergleicht die beiden Xml Objekte anhand des Parameters.
     * <p>
     * gibt ein true aus, wenn das suchobjekt einen ungesetzen content hat,
     * <p>
     * oder eine übereinstimming vorhanden ist
     * 
     * @author Tobias Janssen
     * @param xmlResource
     * @param xmlSearch
     * @return
     */
    private Boolean compareParameter(Xml xmlResource, Xml xmlSearch)
    {
	// prüfen, ob der der DataContent geprüft werden soll
	if (xmlSearch.getParameters() == null)
	    // nein, soll nicht geprüpft werden, daher wird ein match ausgegeben
	    return true;
	else if (xmlSearch.getParameters().length == 0)
	    // nein, soll nicht geprüpft werden, daher wird ein match ausgegeben
	    return true;
	else
	{

	    if (xmlResource.getParameters() != null && xmlSearch.getParameterAtIndex(1) != null)
	    {
		Parameter parameter = xmlSearch.getParameterAtIndex(1);
		// alle vorhandenen parameter abrufen...
		for (int i = 0; i < xmlResource.getParameters().length; i++)
		{
		    // ...und überprüfen, ob der Parametername mit Value mit den
		    // Suchanforderungen übereinstimmt:
		    Parameter para = xmlResource.getParameterAtIndex(i);
		    if (para.getName().equalsIgnoreCase(parameter.getName()))
		    {
			if (parameter.getValue() != null && para.getValue().equalsIgnoreCase(parameter.getValue()))
			{
			    // Fund
			    return true;
			}
			else if (parameter.getValue() == null)
			{
			    return true;
			}
		    }
		}
	    }
	}

	return false;
    }

    /**
     * @author Tobias Janssen Sucht in dem angegebenen XmlTag abwärts nach dem
     *         gewünschten tagType, parameter & parameter value
     * @param xmlResource
     *            Xml in dem gesucht werden soll
     * @param tagType
     *            String der den zu suchenden Tag-Typen enthält
     * @param parameter
     *            Parameter, der den zu suchenden Parameter enthält
     * @return Xml mit Suchergebnis
     */
    public Xml tagCrawlerFindFirstOf(Xml xmlResource, String tagType, Parameter parameter)
    {
	// prüfen, ob das resultArray bereits initialisiert wurde:
	if (tagCrawlerResult != null)
	{
	    // resultArray initialisieren
	    return tagCrawlerResult;
	}

	// prüfen, ob der aktuelle HtmlTag dem zu suchenden Tag entspricht und
	// ob dieser überhaupt Parameter enthält
	if (xmlResource.getType().equalsIgnoreCase(tagType) && xmlResource.getParameters().length > 0)
	{
	    // alle vorhandenen parameter abrufen...
	    for (int i = 0; i < xmlResource.getParameters().length; i++)
	    {
		// ...und überprüfen, ob der Parametername mit Value mit den
		// Suchanforderungen übereinstimmt:
		Parameter para = xmlResource.getParameterAtIndex(i);
		if (para.getName().equalsIgnoreCase(parameter.getName()))
		{
		    if (parameter.getValue() != null && para.getValue().equalsIgnoreCase(parameter.getValue()))
		    {
			// Fund
			return xmlResource;
		    }
		    else if (parameter.getValue() == null)
		    {
			return xmlResource;
		    }
		}
	    }
	}
	// prüfen, ob es noch untergeordnete Tags gibt:
	if (xmlResource.getChildTags().length > 0)
	{
	    // alle childTags per Rekursion prüfen:
	    for (int i = 0; i < xmlResource.getChildTags().length; i++)
	    {
		tagCrawlerResult = tagCrawlerFindFirstOf(xmlResource.getChildTagAtIndex(i), tagType, parameter);
	    }
	}
	return tagCrawlerResult;
    }

    /**
     * @author Tobias Janssen Sucht in dem angegebenen XmlTag abwärts nach dem
     *         gewünschten tagType mit entsprechendem Content
     * 
     * @param xmlResource
     *            Xml in dem gesucht werden soll
     * @param tagType
     *            String der den zu suchenden Tag-Typen enthält
     * @param contentContains
     *            String, der den zu suchenden Content enthält
     * @return Xml mit Suchergebnis
     * @deprecated
     */
    public Xml tagCrawlerFindFirstOf(Xml xmlResource, String tagType, String contentContains)
    {
	// prüfen, ob das resultArray bereits initialisiert wurde:
	if (tagCrawlerResult != null)
	{
	    return tagCrawlerResult;
	}
	// prüfen, ob der tagType stimmt, und ob der Content überhaupt daten
	// enthält
	if (xmlResource.getType().equalsIgnoreCase(tagType) && xmlResource.getDataContent() != null)
	{
	    // den Datencontent auf den Suchstring überprüfen:
	    if (xmlResource.getDataContent().contains(contentContains))
	    {
		// bei einem Fund diesen zum resultArray hinzufügen
		// resultArray =
		// (XmlTag[])ArrayOperations.AppendToArray(resultArray,currentTag);
		return xmlResource;
	    }

	}
	// prüfen, ob es noch untergeordnete Tags gibt:
	if (xmlResource.getChildTags().length > 0)
	{
	    // alle childTags per Rekursion prüfen:
	    for (int i = 0; i < xmlResource.getChildTags().length; i++)
	    {
		tagCrawlerResult = tagCrawlerFindFirstOf(xmlResource.getChildTagAtIndex(i), tagType, contentContains);

	    }
	}
	return tagCrawlerResult;
    }

    /**
     * @author Tobias Janssen Sucht in dem angegebenen XmlTag abwärts nach dem
     *         gewünschten tagType
     * 
     * @param xmlResource
     *            Xml in dem gesucht werden soll
     * @param tagType
     *            String der den zu suchenden Tag-Typen enthält
     */
    public void tagCrawlerFindAllOf(Xml xmlResource, String tagType)
    {
	// prüfen, ob das resultArray bereits initialisiert wurde:
	if (tagCrawlerResultArray == null)
	{
	    // resultArray initialisieren
	    tagCrawlerResultArray = new Xml[0];
	}
	// prüfen, ob der tagType stimmt
	if (xmlResource.getType().equalsIgnoreCase(tagType))
	{
	    // bei einem Fund diesen zum resultArray hinzufügen
	    tagCrawlerResultArray = (Xml[]) ArrayOperations.AppendToArray(tagCrawlerResultArray, xmlResource);
	}
	// prüfen, ob es noch untergeordnete Tags gibt:
	if (xmlResource.getChildTags().length > 0)
	{
	    // alle childTags per Rekursion prüfen:
	    for (int i = 0; i < xmlResource.getChildTags().length; i++)
	    {
		tagCrawlerFindAllOf(xmlResource.getChildTagAtIndex(i), tagType);
	    }
	}
    }

    /**
     * @author Tobias Janssen Sucht in dem angegebenen XmlTag abwärts nach dem
     *         gewünschten tagType, parameter & parameter value
     * 
     * @param xmlResource
     *            Xml in dem gesucht werden soll
     * @param tagType
     *            String der den zu suchenden Tag-Typen enthält
     * @return Xml mit Suchergebnis
     */
    public Xml tagCrawlerFindFirstEntryOf(Xml xmlResource, String tagType)
    {
	// prüfen, ob das resultArray bereits initialisiert wurde:
	if (tagCrawlerResult != null)
	{
	    // resultArray initialisieren
	    return tagCrawlerResult;
	}
	// prüfen, ob der tagType stimmt
	if (xmlResource.getType().equalsIgnoreCase(tagType))
	{
	    // bei einem Fund diesen zum resultArray hinzufügen
	    return xmlResource;
	}
	// prüfen, ob es noch untergeordnete Tags gibt und ob bereits ein
	// Ergebnis vorliegt:
	if (xmlResource.getChildTags().length > 0)
	{
	    // alle childTags per Rekursion prüfen:
	    for (int i = 0; i < xmlResource.getChildTags().length; i++)
	    {
		tagCrawlerResult = tagCrawlerFindFirstEntryOf(xmlResource.getChildTagAtIndex(i), tagType);
	    }
	}
	return tagCrawlerResult;
    }

    /**
     * @author Tobias Janssen Sucht nach dem untersten Child eines Xml tags
     * 
     * @param xmlResource
     *            Xml in dem gesucht werden soll
     * @return Xml mit Suchergebnis
     */
    public Xml tagCrawlerFindDeepestChild(Xml xmlResource)
    {
	if (xmlResource.getChildTags().length > 0)
	{
	    return tagCrawlerFindDeepestChild(xmlResource.getChildTagAtIndex(0));
	}
	else
	{
	    return xmlResource;
	}
    }

    /**
     * @author Tobias Janssen Sucht nach dem untersten Child eines Xml tags,
     *         dass noch keine suchId erhalten hat
     */
    public Xml tagCrawlerFindDeepestUnSumerizedChild(Xml xmlResource, int rndmId)
    {
	for (int i = 0; i < xmlResource.getChildTags().length; i++)
	{
	    if (xmlResource.getChildTagAtIndex(i).getRandomId() != rndmId)
	    {
		return tagCrawlerFindDeepestUnSumerizedChild(xmlResource.getChildTagAtIndex(i), rndmId);
	    }
	}
	return xmlResource;
    }
    
    
}
