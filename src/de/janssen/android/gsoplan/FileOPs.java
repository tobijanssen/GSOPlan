package de.janssen.android.gsoplan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;

public class FileOPs {
	public static Boolean ready = true;
	
	/// Datum: 12.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Speichert den Angegeben String in einer Datei
	///	
	///
	///	Parameter:
	///	
	/// 
	/// 
	public static void saveToFile(Context context, String fileContent,String dir, String filename, ProgressDialog pd)
	{
		ready = false;
		java.io.File theFile = new java.io.File(context.getFilesDir()+"/"+dir, filename);
		
		
		try
		{
			//FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_PRIVATE);
			FileOutputStream fos = new FileOutputStream(theFile);
			fos.write(fileContent.getBytes());
			pd.setProgress(pd.getProgress()+50);
			fos.close();
			ready = true;
		}
		catch(Exception e)
		{
			ready = true;
			//TODO:Catch Exception FileWrite
		}
		ready = true;
		
	}

	/// Datum: 12.09.12
	// / Autor: Tobias Janßen
	// /
	// / Beschreibung:
	// / Speichert den Angegeben String in einer Datei
	// /
	// /
	// / Parameter:
	// /
	// /
	// /
	public static void saveToFile(Context context, String fileContent,File file) throws Exception 
	{
		ready = false;
		
		FileOutputStream fos = new FileOutputStream(file);
		
		//java.io.File file = new java.io.File(context.getFilesDir()+"/"+dir, filename);
		try 
		{
			//FileOutputStream fos = context.openFileOutput(theFile.getName(),Context.MODE_PRIVATE);
			fos.write(fileContent.getBytes());
			fos.close();
			ready = true;
		} 
		catch (Exception e) 
		{
			ready = true;
			throw new Exception("Fehler beim schreiben der Dateien"); 
		}
		ready = true;
	}
	
	
	
	/// Datum: 12.09.12
	/// Autor: Tobias Janßen
	///
	///	Beschreibung:
	///	Speichert den Angegeben String in einer Datei
	///	
	///
	///	Parameter:
	///	
	/// 
	/// 
	public static String readFromFile(Context context, File file) throws Exception
	{
		ready = false;
		String output="";
		try
		{
			FileInputStream fis = new FileInputStream(file);
			
			long length = file.length();
			//prüfen, ob das file größer als int ist
			if(java.lang.Integer.MAX_VALUE < length)
			{
				int bytesLength = java.lang.Integer.MAX_VALUE;
				long bytesRead = 0;
				while(bytesRead < length)
				{
					byte[] buffer = new byte[bytesLength];
					fis.read(buffer,0,bytesLength);
					output+=new String(buffer);
					bytesRead+=bytesLength;
					//prüfen, ob die noch ausstehenden Daten größer als int Max sind
					if(length-bytesRead > java.lang.Integer.MAX_VALUE)
					{
						//ja, sind immer noch größer
						bytesLength = java.lang.Integer.MAX_VALUE;
						
					}
					else
					{
						//nein, nun passt es
						bytesLength = (int) (length-bytesRead);
					}
					
				}
				fis.close();
				ready = true;
			}
			else
			{
				//dies sollte der normale fall sein
				byte[] buffer = new byte[(int)length];
				fis.read(buffer);
				output=new String(buffer);
				fis.close();
				ready = true;
			}

		}
		catch(FileNotFoundException e)
		{
			ready = true;
			throw new FileNotFoundException("Laden der Datei fehlgeschlagen: Datei existiert nicht");
		}
		catch(NullPointerException e)
		{
			ready = true;
			throw new NullPointerException("Laden der Datei fehlgeschlagen: Dateiname fehlerhaft");
		}
		catch(IOException e)
		{
			ready = true;
			throw new IOException("Laden der Datei fehlgeschlagen: Dateiname fehlerhaft");
		}
		
		return output;
		
	}
}
