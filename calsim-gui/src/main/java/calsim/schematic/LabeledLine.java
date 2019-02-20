/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */
package calsim.schematic;

import java.awt.*;

import vista.graph.GEAttr;
import vista.graph.GEContainer;
import vista.graph.GEOverlayLayout;
import vista.graph.LineElement;
import vista.graph.LineElementAttr;
import vista.graph.ScaledElement;
import vista.graph.TextLine;
//import java.util.Properties;

/**
 * This element draws a line with the direction symbol of a triangle and
 * a label. The line is scalable by GEScaledLayout and is drawn from
 * the left-upper corner of the rectangle to the right lower corner of
 * the rectangle.
 *
 * @author Nicky Sandhu
 * @version $Id: LabeledLine.java,v 1.1.2.5 1999/07/18 20:57:55 nsandhu Exp $
 * @see vista.graph.GEScaledLayout
 */
public class LabeledLine extends GEContainer implements ScaledElement
{
	/**
	 * for debugging
	 */
	public static final boolean DEBUG = false;
	public TextLine _textLine;
	private Polygon _filledArrow = new Polygon(new int[]{0, 0, 0}, new int[]{0, 0, 0}, 3);
	private LineElement _le;

	/**
	 * constructor.
	 */
	public LabeledLine(String label)
	{
		_textLine = new TextLine(label);
		//  _textLine.setFont(new Font("Times Roman",Font.PLAIN,10));
		_le = new LineElement();
		LineElementAttr lea = (LineElementAttr) _le.getAttributes();
		lea.setOrientation(-1);
		lea.setThickness(3);
		setLayout(new GEOverlayLayout());
		//  add(_textLine);
		add(_le);
	}

	/**
	 *
	 */
	public String getText()
	{
		return _textLine.getText();
	}

	/**
	 *
	 */
	public void setText(String text)
	{
		_textLine.setText(text == null ? "" : text);
	}

	/**
	 *
	 */
	public TextLine getTextLine()
	{
		return _textLine;
	}

	/**
	 *
	 */
	public LineElement getLineElement()
	{
		return _le;
	}

	/**
	 * true if x,y hits the drawing. More precise than contains(x,y)
	 */
	public boolean hitsDrawing(int x, int y)
	{
		return (_le.hitsDrawing(x, y) ||
				_filledArrow.contains(x, y));
	}

	/**
	 * draws horizontal or vertical line with reference to starting point
	 */
	public void Draw()
	{
		Rectangle drawingArea = getDrawBounds();
		Graphics gc = getGraphics();
		// draws line across diagonal from top right to bottom left
		double x1 = drawingArea.x;
		double y1 = drawingArea.y;
		double x2 = drawingArea.x + drawingArea.width;
		double y2 = drawingArea.y + drawingArea.height;
		//
		int trs = 10; // size of triangle
		// get mid-point
		double xm = (x2 + x1) / 2.0;
		double ym = (y2 + y1) / 2.0;
		//draw line
		_le.draw(gc, drawingArea);
		// get angle of line
		double theta = Math.atan((y2 - y1) / (x2 - x1));
		// rotate by 90 degres
		//  theta = theta + Math.PI/2;
		double ct = Math.cos(theta), st = Math.sin(theta);
		//    double l = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		// make a triangle
		//  _filledArrow = new Polygon();
		int hz = 1;
		if(x2 == x1)
		{
			hz = 1;
		}
		else
		{
			hz = (int) Math.round((x2 - x1) / Math.abs(x2 - x1));
		}
		_filledArrow.xpoints[0] = (int) (xm - trs * st);
		_filledArrow.ypoints[0] = (int) (ym + trs * ct);
		_filledArrow.xpoints[1] = (int) (xm + trs * st);
		_filledArrow.ypoints[1] = (int) (ym - trs * ct);
		_filledArrow.xpoints[2] = (int) (xm + hz * trs * ct);
		_filledArrow.ypoints[2] = (int) (ym + hz * trs * st);
		if(DEBUG)
		{
			System.out.println(drawingArea);
			System.out.println("theta = " + theta);
			System.out.println("hz = " + hz);
			for(int i = 0; i < 3; i++)
			{
				System.out.println("_filledArrow[" + i + "] = " +
						_filledArrow.xpoints[i] + " , " + _filledArrow.ypoints[i]);
			}
		}
		// draw the triangle
		Color orgColor = gc.getColor();
		gc.setColor(_le.getForegroundColor());
		gc.fillPolygon(_filledArrow);
		gc.setColor(orgColor);
		// draw text around mid-point and a little to the right and up
		Rectangle tr = _textLine.getBounds();
		tr.x = (int) Math.round(xm - trs * st * 2);
		tr.y = (int) Math.round(ym + trs * ct * 2);
		tr.width = Math.abs(trs * 2) / 2;
		tr.height = 0;
		_textLine.draw(gc, tr);
		//gc.drawRect(tr.x,tr.y,tr.width, tr.height);
	}

	/**
	 * calculates the preferred size of this element
	 *
	 * @return The preferred size
	 */
	public Dimension getPreferredSize()
	{
		GEAttr attr = getAttributes();
		if(attr._orientation == GEAttr.HORIZONTAL)
		{
			return new Dimension(25, 10);
		}

		return new Dimension(10, 25);
	}

	/**
	 * calculates the minimum size of this element
	 *
	 * @return the minimum size
	 */
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}
}
