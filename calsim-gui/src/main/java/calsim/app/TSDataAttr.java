/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package calsim.app;

import vista.set.DataSetAttr;

/**
 * A set of data attributes that contains original units field.
 *
 * @author Armin Munevar
 * @version $Id: TSDataAttr.java,v 1.1.2.3 2001/07/12 01:58:32 amunevar Exp $
 */

public class TSDataAttr extends DataSetAttr
{
	private String _originalUnits;

	/**
	 *
	 */
	public TSDataAttr(DataSetAttr attr)
	{
		super(attr.getGroupName(),
				attr.getLocationName(),
				attr.getTypeName(),
				attr.getSourceName(),
				attr.getType(),
				attr.getXUnits(),
				attr.getYUnits(),
				attr.getXType(),
				attr.getYType());
		setOriginalUnits(attr.getYUnits());
	}

	/**
	 *
	 */
	public String getOriginalUnits()
	{
		return _originalUnits;
	}

	/**
	 *
	 */
	private void setOriginalUnits(String units)
	{
		_originalUnits = units;
	}
}
