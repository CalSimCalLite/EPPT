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

import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 */
public class FluxInput
{
	GenericFileInput input;
	float value;
	String _pathname, _filename;

	/**
	 *
	 */
	public FluxInput(String filename, String pathname)
	{
		_filename = filename;
		_pathname = pathname;
		try
		{
			input = new AsciiFileInput();
			input.initializeInput(filename);
			while(input.readString().compareTo(pathname) != 0)
			{
			}
			input.readString();
			input.readString();
		}
		catch(IOException ioe)
		{
			System.out.println("Error initiatializing flux input " + filename
					+ "::" + pathname);
			System.out.println(ioe);
		}
		catch(Exception e)
		{
			System.out.println("Error initiatializing flux input " + filename
					+ "::" + pathname);
			System.out.println(e);
		}
	}

	/**
	 *
	 */
	public float getNextValue()
	{
		String line = null;
		try
		{
			line = input.readString();
		}
		catch(IOException ioe)
		{
			System.out.println("Error reading flux " + ioe);
		}
		try
		{
			StringTokenizer st = new StringTokenizer(line);
			st.nextToken();
			st.nextToken();
			value = new Float(st.nextToken()).floatValue();
		}
		catch(Exception nse)
		{
			System.out.println("nse caught");
		}
		return value;
	}

	/**
	 *
	 */
	public void resetInput()
	{
		try
		{
			input.closeStream();
		}
		catch(IOException ioe)
		{
			System.out.println("Error closing " + _filename);
		}
		try
		{
			input = new AsciiFileInput();
			input.initializeInput(_filename);
			while(input.readString().compareTo(_pathname) != 0)
			{
			}
			input.readString();
			input.readString();
		}
		catch(IOException ioe)
		{
			System.out.println("Error resetting flux input " + _filename + "::"
					+ _pathname);
			System.out.println(ioe);
		}
		catch(Exception e)
		{
			System.out.println("Error resetting flux input " + _filename + "::"
					+ _pathname);
			System.out.println(e);
		}
	}
}
