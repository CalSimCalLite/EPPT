/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package calsim.app;

import javax.swing.table.AbstractTableModel;

/**
 * A table model for derived time series
 *
 * @author Nicky Sandhu
 * @version $Id: MTSTableModel.java,v 1.1.4.6 2001/04/18 21:07:35 jfenolio Exp $
 * @see DerivedTimeSeries
 */
public class MTSTableModel extends AbstractTableModel
{
	private static final String[] TABLE_HEADERS =
			{
					"Derived Time Series", "Dvar/Svar", "B part", "C part"
			};
	/**
	 *
	 */
	private MultipleTimeSeries _mts;

	/**
	 * creates a table model from the given MTS
	 */
	public MTSTableModel(MultipleTimeSeries mts)
	{
		_mts = mts;
	}

	/**
	 * Number of data references used in MTS calculations
	 */
	public int getRowCount()
	{
		return _mts.getNumberOfDataReferences();
	}

	/**
	 * Number of columns in the table
	 */
	public int getColumnCount()
	{
		return 4;
	}

	/**
	 * Returns the name of the column at <i>columnIndex</i>.
	 *
	 * @param columnIndex the index of column
	 * @return the name of the column
	 */
	@Override
	public String getColumnName(int columnIndex)
	{
		return TABLE_HEADERS[columnIndex];
	}

	/**
	 * Returns true if the cell at <I>rowIndex</I> and <I>columnIndex</I>
	 * is editable.  Otherwise, setValueAt() on the cell will not change
	 * the value of that cell.
	 *
	 * @param rowIndex    the row whose value is to be looked up
	 * @param columnIndex the column whose value is to be looked up
	 * @return true if the cell is editable.
	 * @see #setValueAt
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		String dtsname = _mts.getDTSNameAt(rowIndex);
		if(columnIndex > 1)
		{
			if(dtsname == null)
			{
				return true;
			}
			else
			{
				return dtsname.isEmpty();
			}
		}
		else
		{
			return true;
		}
	}

	/**
	 * Returns an attribute value for the cell at <I>columnIndex</I>
	 * and <I>rowIndex</I>.
	 *
	 * @param rowIndex    the row whose value is to be looked up
	 * @param columnIndex the column whose value is to be looked up
	 * @return the value Object at the specified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if(columnIndex == 0)
		{
			String dtsname = _mts.getDTSNameAt(rowIndex);
			if(dtsname == null)
			{
				return "";
			}
			else
			{
				return dtsname;
			}
		}
		else if(columnIndex == 1)
		{
			String dtsname = _mts.getDTSNameAt(rowIndex);
			if(dtsname != null &&
					!dtsname.isEmpty())
			{
				return "";
			}
			String varType = _mts.getVarTypeAt(rowIndex);
			if(varType == null)
			{
				return "";
			}
			else
			{
				return varType;
			}
		}
		else if(columnIndex == 2)
		{
			String dtsname = _mts.getDTSNameAt(rowIndex);
			if(dtsname != null &&
					!dtsname.isEmpty())
			{
				return "";
			}
			String bpart = _mts.getBPartAt(rowIndex);
			if(bpart == null)
			{
				return "";
			}
			else
			{
				return bpart;
			}
		}
		else if(columnIndex == 3)
		{
			String dtsname = _mts.getDTSNameAt(rowIndex);
			if(dtsname != null &&
					!dtsname.isEmpty())
			{
				return "";
			}
			String cpart = _mts.getCPartAt(rowIndex);
			if(cpart == null)
			{
				return "";
			}
			else
			{
				return cpart;
			}
		}
		else
		{
			throw new InternalError("Invalid column access");
		}
	}

	/**
	 * Sets an attribute value for the record in the cell at
	 *
	 * @param aValue      the new value
	 * @param rowIndex    the row whose value is to be changed
	 * @param columnIndex the column whose value is to be changed
	 * @see #getValueAt
	 * @see #isCellEditable
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(columnIndex == 0)
		{
			String dtsname = (String) aValue;
			if(dtsname.length() > 0)
			{
				_mts.setDTSNameAt(rowIndex, dtsname);
			}
		}
		else if(columnIndex == 1)
		{
			String varType = (String) aValue;
			_mts.setVarTypeAt(rowIndex, varType);
		}
		else if(columnIndex == 2)
		{
			String bpart = (String) aValue;
			_mts.setBPartAt(rowIndex, bpart);
		}
		else if(columnIndex == 3)
		{
			String cpart = (String) aValue;
			_mts.setCPartAt(rowIndex, cpart);
		}
		else
		{
			throw new InternalError("Invalid column access");
		}
	}
}
