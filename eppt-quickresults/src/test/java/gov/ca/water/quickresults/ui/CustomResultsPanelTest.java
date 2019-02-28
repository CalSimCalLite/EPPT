/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.quickresults.ui;

import javax.swing.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 02-26-2019
 */
class CustomResultsPanelTest
{
	@Test
	void testCtor()
	{
		JFrame frame = new JFrame();
		CustomResultsPanel customResultsPanel = new CustomResultsPanel(frame);
		assertNotNull(customResultsPanel.getSwingEngine());
	}
}
