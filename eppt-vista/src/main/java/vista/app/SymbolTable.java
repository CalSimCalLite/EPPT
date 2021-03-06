/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2019.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */
package vista.app;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A hash table to assign a unique name to a given object. If the object has not
 * been seen before it is given a new name =
 * lowercase(object.getClass().getName()) + number_objects_of_that_type
 *
 * @author Nicky Sandhu
 * @version $Id: SymbolTable.java,v 1.1 2003/10/02 20:48:42 redwood Exp $
 */
public class SymbolTable
{
	private Hashtable _table;

	/**
	 * initializes a hash table to store the names
	 */
	public SymbolTable()
	{
		_table = new Hashtable();
	}

	/**
	 * gets the unique id for this object. If the id does not already exists a
	 * new one is created.
	 */
	public String getNameFor(Object obj)
	{
		Object name = _table.get(obj);
		if(name == null)
		{
			name = createNameFor(obj);
		}
		return (String) name;
	}

	/**
	 * creates a unique id for this object
	 */
	private Object createNameFor(Object obj)
	{
		int i = 1;
		for(Enumeration e = _table.keys(); e.hasMoreElements(); )
		{
			Object element = e.nextElement();
			if(element.getClass().equals(obj.getClass()))
			{
				i++;
			}
		}
		String name = obj.getClass().getName().toLowerCase() + i;
		_table.put(obj, name);
		return name;
	}
}
