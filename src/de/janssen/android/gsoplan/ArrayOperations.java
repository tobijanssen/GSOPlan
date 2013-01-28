/*
 * ArrayOperations.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan;

public class ArrayOperations
{
    /**
     * 
     * Vergrößert das angegebene Array und hängt das Object an
     * @param oldArray		Object das Array
     * @param objectToAdd	Object das Object, das angefügt werden soll
     * @return			Object Array vergrößert um das Object
     */
    public static Object AppendToArray(Object oldArray, Object objectToAdd)
    {
	int oldSize = java.lang.reflect.Array.getLength(oldArray);
	Class<?> elementType = oldArray.getClass().getComponentType();
	Object newArray = java.lang.reflect.Array.newInstance(elementType, oldSize + 1);
	System.arraycopy(oldArray, 0, newArray, 0, oldSize);
	int endIndex = java.lang.reflect.Array.getLength(newArray) - 1;
	java.lang.reflect.Array.set(newArray, endIndex, objectToAdd);
	return newArray;
    }

    /**
     * @author Tobias Janssen
     * Verandert die Größe des angegebenen Arrays auf die neue Größe
     * @param oldArray			Object Array, das vergrößert werden soll
     * @param newSize			int die neue Größe des Arrays
     * @return				Object Arrays mit neuer Größe
     */
    public static Object ResizeArray(Object oldArray, int newSize)
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
