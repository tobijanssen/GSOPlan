package de.janssen.android.gsoplan;

public class ArrayOperations 
{
	/// Datum: 30.08.12
	/// Autor: Tobias Janßen
	///	Beschreibung:
	///	Vergrößert das angegebene Array und hängt das Object an  
	///
	///	Parameter:
	///	Object oldArray: das Array
	/// Object objectToAdd: das Object, das angefügt werden soll
	/// 
	/// Returns:
	/// Object Array vergrößert um das Object
	public static Object AppendToArray (Object oldArray, Object objectToAdd)
	{
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, oldSize+1);
		System.arraycopy(oldArray, 0, newArray, 0, oldSize);
		int endIndex = java.lang.reflect.Array.getLength(newArray)-1;
		java.lang.reflect.Array.set(newArray, endIndex, objectToAdd);
		return newArray; 
	}
	
		
	public static Object ResizeArray (Object oldArray, int newSize)
	{
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0)
		{
			System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		}
		return newArray; 
	}
}
