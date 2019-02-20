/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package calsim.gui;

import vista.gui.CursorChangeListener;
//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.*;

/**
 * Task listener for the Gui package which determines cursor changes and status messages
 *
 * @author Nicky Sandhu ,Armin Munevar
 * @version $Id: GuiTaskListener.java,v 1.1.2.8 2001/07/12 01:59:38 amunevar Exp $
 */
public abstract class GuiTaskListener extends CursorChangeListener
{
	//  private Frame _fr;
	//  private Component _glass;
	//  private Cursor _oldCursor;
	private String _preMsg, _postMsg;

	//  private MouseListener _ml = new MouseAdapter(){
	//    public void mousePressed(MouseEvent e) {}
	//  };
	public GuiTaskListener(String preMsg)
	{
		this(preMsg, "Done.");
	}

	public GuiTaskListener(String preMsg, String postMsg)
	{
		_preMsg = preMsg;
		_postMsg = postMsg;
		// set this to false if you don't want to use threads. Sets this
		// so for all GuiTaskListener instances. Also does it dynamically.
		//    USE_THREADS=true;
	}

	/**
	 *
	 */
	public void doPreWork()
	{
		super.doPreWork();
		GuiUtils.setStatus(_preMsg);
	}

	/**
	 *
	 */
	public void doPostWork()
	{
		super.doPostWork();
		GuiUtils.setStatus(_postMsg);
	}
}


