/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */
package vista.app.commands;

import vista.app.DataTableFrame;
import vista.app.MultiDataTableFrame;
import vista.gui.Command;
import vista.gui.ExecutionException;
import vista.set.DataReference;
import vista.set.Group;

/**
 * Encapsulates commands implementing group related commands
 * 
 * @author Nicky Sandhu
 * @version $Id: TabulateDataCommand.java,v 1.1 2003/10/02 20:48:43 redwood Exp
 *          $
 */
public class TabulateDataCommand implements Command {
	private Group _group;
	private int[] _rNumbers;
	private String _filename;

	/**
	 * opens group and sets current group to
	 */
	public TabulateDataCommand(Group g, int[] referenceNumbers) {
		_group = g;
		_rNumbers = referenceNumbers;
	}

	/**
	 * executes command
	 */
	public void execute() throws ExecutionException {
		if (_rNumbers == null || _rNumbers.length == 0)
			return;
		if (_rNumbers.length == 1) {
			new DataTableFrame(_group.getDataReference(_rNumbers[0]));
		} else {
			DataReference[] refs = new DataReference[_rNumbers.length];
			for (int i = 0; i < _rNumbers.length; i++)
				refs[i] = _group.getDataReference(_rNumbers[i]);
			new MultiDataTableFrame(refs);
		}
	}

	/**
	 * unexecutes command or throws exception if not unexecutable
	 */
	public void unexecute() throws ExecutionException {
		throw new ExecutionException("Cannot undo tabulation of data");
	}

	/**
	 * checks if command is executable.
	 */
	public boolean isUnexecutable() {
		return false;
	}

	/**
	 * writes to script
	 */
	public void toScript(StringBuffer buf) {
	}
} // end of TabulateDataCommand