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
package vista.set;

/**
 * Constants needed for identifying conditions.
 *
 * @author Nicky Sandhu
 * @version $Id: Constants.java,v 1.1 2003/10/02 20:49:19 redwood Exp $
 */
public class Constants
{
	/**
	 *
	 */
	public static final double NO_SHOW = 0x7fff000000000000L;
	/**
	 * data value to signify missing data...
	 */
	public static final double MISSING = Float.MIN_VALUE;
	/**
	 * data value to signify missing data...
	 */
	public static final double MISSING_VALUE = -901;
	/**
	 * data value to signify missing data...
	 */
	public static final double MISSING_RECORD = -902;
	/**
	 *
	 */
	public static final ElementFilter DEFAULT_FILTER = new CompositeFilter(
			new ElementFilter[]{
					new MultiValueFilter(new double[]{Float.MIN_VALUE,
							vista.set.Constants.MISSING_VALUE,
							vista.set.Constants.MISSING_RECORD}),
					new NaNFilter()});
	/**
	 *
	 */
	public static final ElementFilter DEFAULT_FLAG_FILTER = new CompositeFilter(
			new ElementFilter[]{Constants.DEFAULT_FILTER,
					FlagUtils.QUESTIONABLE_FILTER, FlagUtils.REJECT_FILTER});
}
