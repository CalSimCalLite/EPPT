/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */
package vista.set;

/**
 * @author Nicky Sandhu
 * @version $Id: PartNamePredicate.java,v 1.1 2003/10/02 20:49:28 redwood Exp $
 */
public class PartNamePredicate implements SortMechanism<DataReference>
{
	/**
	 *
	 */
	private int _partId;
	/**
	 *
	 */
	private int _sortOrder;

	/**
	 *
	 */
	public PartNamePredicate(int partId, int sortOrder)
	{
		_partId = partId;
		_sortOrder = sortOrder;
	}

	/**
	 * true if sort order is ascending
	 */
	public boolean isAscendingOrder()
	{
		return (_sortOrder == SortMechanism.INCREASING);
	}

	/**
	 * sets the order to ascending or descending.
	 */
	public void setAscendingOrder(boolean ascending)
	{
		_sortOrder = ascending ? SortMechanism.INCREASING
				: SortMechanism.DECREASING;
	}

	/**
	 *
	 */
	public int getPartId()
	{
		return _partId;
	}

	/**
	 * method for collections Comapartor interface
	 */
	public int compare(DataReference first, DataReference second)
	{
		// check instanceof first and second
		Pathname fPath = first.getPathname();
		Pathname sPath = second.getPathname();
		int lexicalValue = 0;
		if(_sortOrder == SortMechanism.INCREASING)
		{
			lexicalValue = sPath.getPart(_partId).compareTo(
					fPath.getPart(_partId));
		}
		else if(_sortOrder == SortMechanism.DECREASING)
		{
			lexicalValue = fPath.getPart(_partId).compareTo(
					sPath.getPart(_partId));
		}
		else
		{
			lexicalValue = 0;
		}
		return lexicalValue;
	}
}
