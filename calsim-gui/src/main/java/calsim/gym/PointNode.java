/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package calsim.gym;

/**
 * Represents a point node in the system. Such a node is usually invisible in
 * a schematic and is to avoid so called hanging arcs.
 *
 * @author Nicky Sandhu
 */
public class PointNode extends Node
{
	private String _name;

	/**
	 * a node with a given id
	 */
	public PointNode(String nm)
	{
		super(-1);
		_name = nm;
	}

	/**
	 * gets name
	 */
	public String getName()
	{
		return _name;
	}
}
