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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * @author Nicky Sandhu
 * @version $Id: TableHeaderToolTipRenderer.java,v 1.1 2003/10/02 20:48:43
 * redwood Exp $
 */
public class TableHeaderToolTipRenderer extends DefaultTableCellRenderer
		implements MouseMotionListener
{
	private JTable _table;

	public TableHeaderToolTipRenderer(JTable table, String name)
	{
		super();
		super.setValue(name);
		super.setBackground(Color.lightGray);
		_table = table;
		setBorder(BorderFactory.createRaisedBevelBorder());
		ToolTipManager.sharedInstance().setDismissDelay(100000);
		ToolTipManager.sharedInstance().setInitialDelay(1000);
		ToolTipManager.sharedInstance().setReshowDelay(2000);
		ToolTipManager.sharedInstance().registerComponent(
				_table.getTableHeader());
		ToolTipManager.sharedInstance().setEnabled(true);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		// setToolTipText(name);
	}

	/**
	 * Invoked when the mouse button has been moved on a component (with no
	 * buttons no down).
	 */
	public void mouseMoved(MouseEvent e)
	{
		TableColumnModel columnModel = _table.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(e.getX());
		int column = _table.convertColumnIndexToModel(viewColumn);
		if(column != -1)
		{
			_table.getTableHeader()
				  .setToolTipText(_table.getColumnName(column));
		}
		else
		{
			_table.getTableHeader().setToolTipText(null);
		}
	}

	public void mouseDragged(MouseEvent e)
	{
	}
}
