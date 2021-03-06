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

package gov.ca.water.calgui.presentation.display;

import java.awt.Container;
import java.awt.HeadlessException;
import java.util.List;
import javax.swing.*;

import gov.ca.water.calgui.presentation.DisplayHelper;
import gov.ca.water.calgui.techservice.IErrorHandlingSvc;
import gov.ca.water.calgui.techservice.impl.ErrorHandlingSvcImpl;
import org.apache.log4j.Logger;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 03-06-2019
 */
public class DefaultPlotHandler implements DisplayHelper.PlotHandler
{
	private static final Logger LOG = Logger.getLogger(DefaultPlotHandler.class.getName());
	private static final int displayDeltaY = 20;
	private static final int displayDeltaX = 200;
	private static final IErrorHandlingSvc ERROR_HANDLING_SVC = new ErrorHandlingSvcImpl();
	private static int displayLocationN = 0;

	/**
	 * Returns coordinates for upper left corner of display frame. Coordinates
	 * move diagonally down the page then shift over 20 pixels, and eventually
	 * restart.
	 *
	 * @return
	 */
	private static java.awt.Point displayLocationPoint()
	{

		try
		{
			// Increment frame counter

			displayLocationN++;

			// Calculate number of rows and columns in grid, accounting for
			// bottom
			// and left margin and for diagonal organization

			int verticalSteps = (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - displayDeltaY - 200)
					/ displayDeltaY;
			int horizontalSteps = (java.awt.Toolkit.getDefaultToolkit().getScreenSize().width - 20 * verticalSteps)
					/ displayDeltaX;

			// If bottom right of grid is reached, start over

			if(displayLocationN >= verticalSteps * horizontalSteps)
			{
				displayLocationN = 0;
			}

			int displayLocationColumn = displayLocationN / verticalSteps;
			int displayLocationRow = displayLocationN - verticalSteps * displayLocationColumn;

			java.awt.Point p = new java.awt.Point();
			p.y = displayDeltaY * displayLocationRow;
			p.x = displayDeltaX * displayLocationColumn + p.y;

			return p;
		}
		catch(HeadlessException e)
		{
			LOG.error(e.getMessage());
			String messageText = "Unable to display frame.";
			ERROR_HANDLING_SVC.businessErrorHandler(messageText, e);
		}
		return null;

	}

	@Override
	public void openPlots(List<JTabbedPane> tabbedPanes)
	{
		for(JTabbedPane tabbedpane : tabbedPanes)
		{
			JFrame frame = new JFrame();

			Container container = frame.getContentPane();
			container.add(tabbedpane);

			frame.pack();
			frame.setTitle("Results - " + tabbedpane.getName());
			// CalLite icon
			//			java.net.URL imgURL = Thread.currentThread().getContextClassLoader().getResource("/images/CalLiteIcon.png");
			//			frame.setIconImage(Toolkit.getDefaultToolkit().getImage(imgURL));

			//			if(!(doTimeSeries || doExceedance || doMonthlyTable || doSummaryTable))
			//			{
			//				if(dssGrabber.getMissingList() == null)
			//				{
			//					container.add(new JLabel("Nothing to show!"));
			//				}
			//			}
			//			else
			//			{
			tabbedpane.setSelectedIndex(0);
			//			}

			frame.setSize(980, 700);
			frame.setLocation(displayLocationPoint());
			frame.setVisible(true);
		}
	}
}
