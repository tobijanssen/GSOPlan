package de.janssen.android.gsoplan;

public class XmlTag {
	public String type;
    public String dataContent;
    public Boolean open = true;
    public Parameter[] parameters = new Parameter[0];
    public Boolean isEndTag = false;
    public XmlTag[] childTags = new XmlTag[0];
    public XmlTag parentTag;
    public int sumerizeId=0;

 
    
    public XmlTag(){}
    public XmlTag(String type,String dataContent,Parameter[] parameters){
    	this.type=type;
    	this.dataContent=dataContent;
    	this.parameters=parameters;
    }
    public XmlTag(String type,String dataContent){
    	this.type=type;
    	this.dataContent=dataContent;
    }
    public XmlTag(String type,Parameter[] parameters){
    	this.type=type;
    	this.parameters=parameters;
    }
    public XmlTag(String type){
    	this.type=type;
    }
    
        /// <summary>
        /// Sucht sich im Html-Quelltext den nächsten Tag und löst diesen in ein Tag-Objekt auf.
        /// 
        /// </summary>
        /// <param name="htmlCode">Der Html-Quelltext, in dem gesucht werden soll</param>
        /// <param name="offsetPoint">Der Startpunkt im Quelltext, an dem die Suche beginnen soll</param>
        /// <param name="cutoffReadedCode">Entfernt den gelesenen Quelltext aus dem htmlCode</param>
        /// <returns></returns>
        public static XmlTag parseNextXmlTag(Xml xml) throws Exception
        {
        	//variablen deklarieren:
        	int startPoint = 0; 
        	int endTagPoint = 0;

            XmlTag tag = new XmlTag();

            //Den Beginn eines Tags suchen:
            startPoint = xml.container.indexOf("<", 0);
            
            //prüfen, ob überhaupt ein Tag vorliegt:
            if(startPoint == -1)
            {
            	throw new Exception("Fehler bei der XML Konvertierung! Code: 0x001");
            }
            
            //der Text davor wird verworfen:
            xml.container = xml.container.substring(startPoint);
            
            //StartPoint korrigieren, da der Text abgeschnitten wurde:
            startPoint = 0;
            
            //ende des StartTags herausfinden:
            endTagPoint = xml.container.indexOf(">", startPoint)+1;
            if(endTagPoint == -1)
            {
            	//Tag ist nicht vernüftig geclosed worden
            	throw new Exception("Fehler bei der XML Konvertierung! Code: 0x002");
            }

            String tagContent = xml.container.substring(0,endTagPoint);
            xml.container = xml.container.substring(endTagPoint);
            
            try
            {
            	tag = decodeXmlTag(tagContent);
            }
            catch(Exception e)
            {
            	throw new Exception(e);
            	
            }

            //prüfen, ob daten vorhanden sind
            //sonderfall javascript berücksichtigen:
            int nextTag;
            if(tag.type.equalsIgnoreCase("script") && !tag.isEndTag)
            {
            	nextTag = xml.container.indexOf("</script>", 0);	
            }
            else
            {
            	nextTag = xml.container.indexOf("<", 0);	
            }
            if(tag.type.equalsIgnoreCase("meta"))
            	tag.open=false;
            if(tag.type.equalsIgnoreCase("link"))
            	tag.open=false;
            if(tag.type.equalsIgnoreCase("br"))
            	tag.open=false;
            		
            if (nextTag > 0)
            {
                //daten vorhanden
                tag.dataContent = xml.container.substring(0, nextTag);
            }
           	if(nextTag != -1)
           	{
           		xml.container = xml.container.substring(nextTag) ;
           	}
           	else
           	{
           		if(xml.container.length()>0)
           			xml.container = xml.container.substring(1);
           	}
            return tag;
        }
        
        
        
        private static XmlTag decodeXmlTag(String xmlCode) throws Exception
        {
        	XmlTag tag = new XmlTag();
        	try
        	{
	        	if(xmlCode.substring(1,2).equalsIgnoreCase("/"))
	        	{
	        		tag.isEndTag = true;
	        		xmlCode = "<"+ xmlCode.substring(2);
	        	}
	        	String c = xmlCode.substring(0,3);
	        	if(c.equalsIgnoreCase("<!-") && xmlCode.length() > 6)
	        	{
	        		tag.open = false;
	        		tag.type = "Comment";
	        		tag.dataContent = xmlCode.substring(3,xmlCode.indexOf("-->",1));
	        		
	        	}
	        	c = xmlCode.substring(0,2);
	        	if(c.equalsIgnoreCase("<!")&& tag.type == null)
	        	{
	        		tag.open = false;
	        		tag.type = "Doctype";
	        		tag.dataContent = xmlCode.substring(2,xmlCode.indexOf(">",1));
	        		
	        	}
        	}
        	catch(Exception e)
        	{
        		 throw new Exception("Fehler bei der XML Decodierung! Code: 0x201");
        	}
        	int parameterPoint = xmlCode.indexOf("=", 0);
        	if(parameterPoint == -1 && tag.type == null)
        	{
        		//keine parameter
        		tag.type = xmlCode.substring(1,xmlCode.length()-1);
        	}
        	else if(parameterPoint >0)
        	{
        		//parametername:
        		Boolean endReached=false; 
        		int spacer=0;
        		int parameterEnd = 0;
        		spacer = xmlCode.indexOf(" ", 0);
        		tag.type = xmlCode.substring(1,spacer);
        		//abscheniden:
        		xmlCode = xmlCode.substring(spacer);
        		
        		do
        		{
	        		String testChar = xmlCode.substring(0,1);
	        		while(testChar.equalsIgnoreCase(" "))
	        		{
		        			xmlCode = xmlCode.substring(1);
		        			testChar = xmlCode.substring(0,1);
	        		}
	        		
	        		parameterPoint = xmlCode.indexOf("=", 0);
	        		//parameternamen auslesen:
	        		Parameter parameter = new Parameter();
	        		parameter.name = xmlCode.substring(0,parameterPoint);
	        		//abschneidne
	        		xmlCode = xmlCode.substring(parameterPoint+1);
	        		//Anführungszeichen lesen:
	        		try
	        		{
		        		String quote = xmlCode.substring(0,1);
		        		if(quote.equalsIgnoreCase("'") || quote.equalsIgnoreCase("\""))
		        		{
		        			parameterEnd = xmlCode.indexOf(quote,1);
		        			parameter.value = xmlCode.substring(1,parameterEnd);
		        		}
		        		else	//parameter ist nicht gequotet!
		        		{
		        			parameterEnd = xmlCode.indexOf(" ",1);
		        			if(parameterEnd == -1)
		        			{
		        				parameterEnd = xmlCode.length()-1;
		        			}
		        			parameter.value = xmlCode.substring(0,parameterEnd);
		        		}
	        		}
	        		catch(Exception e)
	        		{
	        			throw new Exception("Fehler bei der XML Decodierung! Code: 0x202");
	        		}
	        		
	        		
	        		//abschneiden:
	        		xmlCode = xmlCode.substring(parameterEnd);
	        		
	        		//dem array hinzufügen:
	        		tag.parameters = (Parameter[]) ArrayOperations.ResizeArray(tag.parameters, tag.parameters.length + 1);
                    tag.parameters[tag.parameters.length - 1] = parameter;
	        		
	        		//nächstes Zeichen prüfen:
	        		testChar = xmlCode.substring(0,1);
	        		while(testChar.equalsIgnoreCase(" ") || testChar.equalsIgnoreCase("\"") || testChar.equalsIgnoreCase("'"))
	        		{
		        			xmlCode = xmlCode.substring(1);
		        			testChar = xmlCode.substring(0,1);
	        		}
	        		if(testChar.equalsIgnoreCase("/"))
	        		{
	        			tag.open = false;
	        		}
	        		if(testChar.equalsIgnoreCase(">"))
	        		{
	        			endReached = true;
	        		}
        		}
        		while(!endReached && tag.open);
        		
        	}
        	return tag;
        }
        
        /// Datum: 14.09.12
    	/// Autor: Tobias Janßen
    	///
    	///	Beschreibung:
    	///	Sucht in dem angegebenen XmlTag abwärts(besucht alle untergebenen childTags) nach dem gewünschten tagType, und returned diesen
    	/// 
    	///	
    	///
    	///	Parameter:
    	///	XmlTag htmlTag: der htmlTag in dem gesucht werden soll
    	/// HtmlStupidInterpreter interpreter: das Ergebnis-Object/enthält das Suchergebnis
    	/// String tagType:	String der den zu suchenden Tag angibt
    	/// String parameterName: String der den zu suchenden Parameternamen angibt
    	/// String parameterValue: String der die zum Parameternamen gehörende Value angibt
    	public XmlTag tagCrawlerFindFirstOf(XmlTag currentPositionXmlTag, XmlTag searchFor, XmlTag result)
    	{
    		//prüfen, ob das resultArray bereits initialisiert wurde:
    		if(result.type != null)
    		{
    			//resultArray initialisieren
    			return result;
    		}
    		
    		//prüfen, ob der aktuelle HtmlTag dem zu suchenden Tag entspricht
    		if(currentPositionXmlTag.type.equalsIgnoreCase(searchFor.type))
    		{
    			return currentPositionXmlTag;		
    		}
    		//prüfen, ob es noch untergeordnete Tags gibt:
    		if(currentPositionXmlTag.childTags.length >0)
    		{
    			//alle childTags per Rekursion prüfen:
    			for(int i=0; i<currentPositionXmlTag.childTags.length;i++)
    			{
    				result = tagCrawlerFindFirstOf(currentPositionXmlTag.childTags[i], searchFor,result);
    			}
    		}
    		return result;
    	}
        
        
        /// Datum: 30.08.12
    	/// Autor: Tobias Janßen
    	///
    	///	Beschreibung:
    	///	Sucht in dem angegebenen HtmlTag abwärts(besucht alle untergebenen childTags) nach dem gewünschten tagType, 
    	/// das den parameterName und die parameterValue enthält und  
    	///	fügt alle Funde dem interpreter.resultArray hinzu
    	///
    	///	Parameter:
    	///	HtmlTag htmlTag: der htmlTag in dem gesucht werden soll
    	/// HtmlStupidInterpreter interpreter: das Ergebnis-Object/enthält das Suchergebnis
    	/// String tagType:	String der den zu suchenden Tag angibt
    	/// String parameterName: String der den zu suchenden Parameternamen angibt
    	/// String parameterValue: String der die zum Parameternamen gehörende Value angibt
    	public XmlTag tagCrawlerFindFirstOf(XmlTag htmlTag, String tagType, String parameterName, String parameterValue, XmlTag result)
    	{
    		XmlTag currentTag = htmlTag;
    		
    		//prüfen, ob das resultArray bereits initialisiert wurde:
    		if(result.type != null)
    		{
    			//resultArray initialisieren
    			return result;
    		}
    		
    		//prüfen, ob der aktuelle HtmlTag dem zu suchenden Tag entspricht und ob dieser überhaupt Parameter enthält 
    		if(currentTag.type.equalsIgnoreCase(tagType) && currentTag.parameters.length > 0)
    		{
    			//alle vorhandenen parameter abrufen...
    			for(int i = 0; i < currentTag.parameters.length;i++)
    			{
    				//...und überprüfen, ob der Parametername mit Value mit den Suchanforderungen übereinstimmt:
    				if(currentTag.parameters[i].name.equalsIgnoreCase(parameterName) && currentTag.parameters[i].value.equalsIgnoreCase(parameterValue))
    				{	
    					//bei einem Fund diesen result
    					//resultArray = (XmlTag[])ArrayOperations.AppendToArray(resultArray,currentTag);
    					return currentTag;
    				}
    			}			
    		}
    		//prüfen, ob es noch untergeordnete Tags gibt:
    		if(currentTag.childTags.length >0)
    		{
    			//alle childTags per Rekursion prüfen:
    			for(int i=0; i<currentTag.childTags.length;i++)
    			{
    				result = tagCrawlerFindFirstOf(currentTag.childTags[i], tagType, parameterName, parameterValue,result);
    			}
    		}
    		return result;
    	}
    	
    	/// Datum: 30.08.12
    	/// Autor: Tobias Janßen
    	///
    	///	Beschreibung:
    	///	Sucht in dem angegebenen HtmlTag abwärts(besucht alle untergebenen childTags) nach dem gewünschten tagType, 
    	/// bei dem der Content den SuchString enthält  
    	///	fügt alle Funde dem interpreter.resultArray hinzu
    	///
    	///	Parameter:
    	///	HtmlTag htmlTag: der htmlTag in dem gesucht werden soll
    	/// HtmlStupidInterpreter interpreter: das Ergebnis-Object/enthält das Suchergebnis
    	/// String tagType:	String der den zu suchenden Tag angibt
    	/// String contentContains: der Such-String der den zu suchenden text angibt
    	public XmlTag tagCrawlerFindFirstOf(XmlTag htmlTag, String tagType, String contentContains, XmlTag result)
    	{
    		XmlTag currentTag = htmlTag;
    		//prüfen, ob das resultArray bereits initialisiert wurde:
    		if(result.type != null)
    		{
    			return result;
    		}
    		//prüfen, ob der tagType stimmt, und ob der Content überhaupt daten enthält
    		if(currentTag.type.equalsIgnoreCase(tagType) && currentTag.dataContent != null)
    		{
    			//den Datencontent auf den Suchstring überprüfen:
    			if(currentTag.dataContent.contains(contentContains))
    			{
    				//bei einem Fund diesen zum resultArray hinzufügen
    				//resultArray = (XmlTag[])ArrayOperations.AppendToArray(resultArray,currentTag);
    				return currentTag;
    			}

    		}
    		//prüfen, ob es noch untergeordnete Tags gibt:
    		if(currentTag.childTags.length >0)
    		{
    			//alle childTags per Rekursion prüfen:
    			for(int i=0; i<currentTag.childTags.length;i++)
    			{
    				result = tagCrawlerFindFirstOf(currentTag.childTags[i],tagType, contentContains, result);
    			}
    		}
    		return result;
    	}
    	
    	/// Datum: 30.08.12
    	/// Autor: Tobias Janßen
    	///
    	///	Beschreibung:
    	///	Sucht in dem angegebenen HtmlTag abwärts(besucht alle untergebenen childTags) nach dem gewünschten tagType 
    	///	fügt alle Funde dem interpreter.resultArray hinzu
    	///
    	///	Parameter:
    	///	HtmlTag htmlTag: der htmlTag in dem gesucht werden soll
    	/// HtmlStupidInterpreter interpreter: das Ergebnis-Object/enthält das Suchergebnis
    	/// String tagType:	String der den zu suchenden Tag angibt
    	public void tagCrawlerFindAllOf(XmlTag htmlTag, String tagType, XmlTag[] resultArray)
    	{
    		XmlTag currentTag = htmlTag;
    		//prüfen, ob das resultArray bereits initialisiert wurde:
    		if(resultArray == null)
    		{
    			//resultArray initialisieren
    			resultArray = new XmlTag[0];
    		}
    		//prüfen, ob der tagType stimmt
    		if(currentTag.type.equalsIgnoreCase(tagType))
    		{
    			//bei einem Fund diesen zum resultArray hinzufügen
    			resultArray = (XmlTag[])ArrayOperations.AppendToArray(resultArray,currentTag);
    		}
    		//prüfen, ob es noch untergeordnete Tags gibt:
    		if(currentTag.childTags.length >0)
    		{
    			//alle childTags per Rekursion prüfen:
    			for(int i=0; i<currentTag.childTags.length;i++)
    			{
    				tagCrawlerFindAllOf(currentTag.childTags[i],tagType, resultArray);
    			}
    		}
    	}
    	
    	/// Datum: 30.08.12
    	/// Autor: Tobias Janßen
    	///
    	///	Beschreibung:
    	///	Sucht in dem angegebenen HtmlTag abwärts(besucht alle untergebenen childTags bis zum Fund) nach dem gewünschten tagType 
    	///	fügt den ersten Fund dem interpreter.resultArray hinzu
    	///
    	///	Parameter:
    	///	HtmlTag htmlTag: der htmlTag in dem gesucht werden soll
    	/// HtmlStupidInterpreter interpreter: das Ergebnis-Object/enthält das Suchergebnis
    	/// String tagType:	String der den zu suchenden Tag angibt
    	public XmlTag tagCrawlerFindFirstEntryOf(XmlTag htmlTag, String tagType, XmlTag result)
    	{
    		XmlTag currentTag = htmlTag;
    		//prüfen, ob das resultArray bereits initialisiert wurde:
    		if(result.type != null)
    		{
    			//resultArray initialisieren
    			return result;
    		}
    		//prüfen, ob der tagType stimmt
    		if(currentTag.type.equalsIgnoreCase(tagType))
    		{
    			//bei einem Fund diesen zum resultArray hinzufügen
    			return currentTag;
    		}
    		//prüfen, ob es noch untergeordnete Tags gibt und ob bereits ein Ergebnis vorliegt:
    		if(currentTag.childTags.length >0)
    		{
    			//alle childTags per Rekursion prüfen:
    			for(int i=0; i<currentTag.childTags.length;i++)
    			{
    				result = tagCrawlerFindFirstEntryOf(currentTag.childTags[i], tagType, result);
    			}
    		}
    		return result;
    	}
    	
    	/// Datum: 30.08.12
    	/// Autor: Tobias Janßen
    	///
    	///	Beschreibung:
    	///	Sucht in dem angegebenen HtmlTag abwärts nach dem tiefsten ChildTag 
    	///
    	///	Parameter:
    	///	HtmlTag htmlTag: der htmlTag in dem gesucht werden soll
    	public XmlTag tagCrawlerFindDeepestChild(XmlTag htmlTag)
    	{
    		if(htmlTag.childTags.length > 0)
    		{
    			return tagCrawlerFindDeepestChild(htmlTag.childTags[0]);
    		}
    		else
    		{
    			return htmlTag;
    		}
    	}
    	
    	/// Datum: 30.08.12
    	/// Autor: Tobias Janßen
    	///
    	///	Beschreibung:
    	///	Sucht in dem angegebenen HtmlTag abwärts nach dem tiefsten ChildTag 
    	///
    	///	Parameter:
    	///	HtmlTag htmlTag: der htmlTag in dem gesucht werden soll
    	public XmlTag tagCrawlerFindDeepestUnSumerizedChild(XmlTag htmlTag,int rndmId)
    	{
    		for(int i=0;i<htmlTag.childTags.length;i++)
    		{
	    		if(htmlTag.childTags[i].sumerizeId != rndmId)
	    		{
	    			return tagCrawlerFindDeepestUnSumerizedChild(htmlTag.childTags[i], rndmId);
	    		}
    		}
    		return htmlTag;
    	}
    	
    	/// Datum: 12.09.12
    	/// Autor: Tobias Janßen
    	///
    	/// Beschreibung:
    	/// Fügt dem Parameter Array weitere Parameter hinzu 
    	///
    	///
    	/// Parameter:
    	/// string name = Der Name/Bezeichnung des Parameters  
    	/// string value = Der Wert des Parameters
    	///
    	public void addParameter(String name,String value)
    	{
    		Parameter parameter = new Parameter();
    		parameter.name = name;
    		parameter.value = value;
    		
    		parameters = (Parameter[]) ArrayOperations.AppendToArray(parameters, parameter);
    		
    	}

    	// / Datum: 22.09.12
    	// / Autor: Tobias Janßen
    	// /
    	// / Beschreibung:
    	// / Liefert den ColorParameter Wert des angegeben Tags zurück
    	// / Wenn nicht vorhanden, dann Schawrz 
    	// /
    	// / Parameter:
    	// /
    	// /
    	// /
    	public String getColorParameter() {
    		for (int x = 0; x < this.parameters.length; x++) {
    			if (this.parameters[x].name.equalsIgnoreCase("color"))
    			{
    				return this.parameters[x].value;
    			}
    		}
    		return "#000000";
    	}
}