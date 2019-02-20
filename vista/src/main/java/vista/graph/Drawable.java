/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */
package vista.graph;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * This defines the interface for objects that can draw themselves in a given
 * region.
 *
 * @author Nicky Sandhu
 * @version $Id: Drawable.java,v 1.1 2003/10/02 20:48:54 redwood Exp $
 */
public interface Drawable
{
	/**
	 * draws with the previously set graphics context and rectangle size
	 */
	void draw();

	/**
	 * draws the component onto the graphics context
	 *
	 * @param gc The context on which graphics are being drawn
	 */
	void draw(Graphics gc);

	/**
	 * sets the bound to this Rectangle and calls the draw method also sets the
	 * clipping to bounds if required
	 *
	 * @param r The region within which to draw
	 */
	void draw(Rectangle r);

	/**
	 * draws the component onto the graphics context within the given Rectangle
	 *
	 * @param gc The context on which graphics are being drawn
	 * @param r  The region within which to draw
	 */
	void draw(Graphics gc, Rectangle r);

	/**
	 * gets the graphics context
	 */
	Graphics getGraphics();

	/**
	 * sets the graphics context
	 *
	 * @param gc The context on which graphics are being drawn
	 */
	void setGraphics(Graphics gc);

	/**
	 * gets the attributes
	 *
	 * @return The attributes of this object
	 */
	GEAttr getAttributes();

	/**
	 * Sets the attributes
	 *
	 * @param attr The attributes of the object being drawn.
	 */
	void setAttributes(GEAttr attr);

	/**
	 * is this object visible?
	 *
	 * @return true if visible
	 */
	boolean isVisible();

	/**
	 * sets the visibility flag for this object.
	 *
	 * @param visible set to true to make this object draw itself
	 */
	void setVisible(boolean visible);
}
