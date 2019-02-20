/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package calsim.wreslcoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import calsim.wreslcoder.wresl.ParseException;
import calsim.wreslcoder.wresl.WreslParser;

/**
 * Controller class for Wresl parse, compile, link, and executions
 *
 * @author Armin Munevar
 * @author Clay Booher (version 1.3 changes)
 * @version $Id: WreslMaker.java,v 1.1.2.18 2001/12/11 23:46:19 jfenolio Exp $
 */
public class WreslMaker
{
	//CB  private static String calsimProgDir=System.getProperty("calsim.home");
	private static String calsimProgDir = System.getenv(
			"calsim.home"); //CB replaced cause Eclipse can set environment variables
	private static int _numberCycles = 1;
	private static boolean _buildDebug = false;
	private PrintWriter out;
	private String ident, tempFilePath, commonPath;
	private boolean needToMake = false;
	private boolean successfulParse = false;
	private String exeFileName;
	//  private boolean _lf90Debug = false;
	private boolean _hideWarnings; //CB added
	private boolean _hideProgressDetails; //CB added
	//  private static boolean _hasFileMappedSectionOpenProblem = false; //CB added to try to overcome the file-mapped section open error during parsing on SOME computers
	private int _parseFileDelay = 0; //CB added to try to overcome the file-mapped section open error during parsing on SOME computers

	/**
	 * Class constructor specifiying the input Wresl source and a message output stream.
	 *
	 * @param tempDir the directory where intermediate (temporary)
	 *                files should be stored
	 */
	public WreslMaker(String tempDir, String common, String wreslFile, PrintWriter output,
					  boolean buildDebug, boolean hideWarnings, boolean hideProgressDetails, int parseFileDelay)
	{
		out = output;
		tempFilePath = tempDir;
		commonPath = common;
		ident = WreslParser.nameWithExtension(wreslFile);
		exeFileName = makeExeFile(wreslFile);
		_buildDebug = buildDebug;
		//    _lf90Debug = lf90Debug;
		_hideWarnings = hideWarnings;
		_hideProgressDetails = hideProgressDetails;
		//    _hasFileMappedSectionOpenProblem = hasFileMappedSectionOpenProblem;
		_parseFileDelay = parseFileDelay;
		setProgramDirectory(); //CB added
	}

	//  public static boolean hasFileMappedSectionOpenProblem() {
	//	  return _hasFileMappedSectionOpenProblem;
	//  }

	/**
	 * Contrives a name for the output excutable filename.
	 *
	 * @param ident the input Wresl file name, with optional extension
	 */
	public static String makeExeFile(String ident)
	{
		String exeEquivalent;
		if(ident.lastIndexOf(".") < 0)
		{
			exeEquivalent = ident.concat(".exe");
		}
		else
		{
			exeEquivalent = ident.substring(0, ident.lastIndexOf(".")).concat(".exe");
		}
		return exeEquivalent;
	}

	/**
	 * CB added because batch file sets calsim.home Property, but in Eclipse it is in environment variables.
	 */
	private void setProgramDirectory()
	{
		if(System.getenv("calsim.home") != null)
		{
			//		  System.out.println("getenv did NOT return null");
			calsimProgDir = System.getenv("calsim.home"); //Eclipse project settings sets
			//		  System.out.println("calsimLibDir = " + calsimProgDir);
		}
		else
		{
			//		  System.out.println("getenv DID return null");
			calsimProgDir = System.getProperty("calsim.home"); //batch file sets
			//		  System.out.println("calsimLibDir = " + calsimProgDir);
		}
	}

	/**
	 * Parse the Wresl code.  Sets a private flag if successful.  If there is a parse
	 * error, it displays an error message on the stream specified in the constructor.
	 *
	 * @throws FileNotFoundException if the file supplied in the constructor can't be opened.
	 *                               This should be checked in the constructor rather than here.
	 */
	public void parse() throws FileNotFoundException, ParseException
	{
		try
		{
			Vector listOfFiles = parseAFile();
			long compare = new File(exeFileName).lastModified();
			successfulParse = true;
			if(compare == 0L)
			{
				if(!(new File(exeFileName)).exists())
				{
					out.println("Unknown which files were changed; executable not found");
				}
				else
				{
					out.println("Unknown which files were changed; I/O error");
				}
				needToMake = true;
			}
			else if(listOfFiles.size() > 0)
			{
				out.println("Noted changed files: " + listOfFiles.toString());
				File exeFile = new File(exeFileName);
				if(exeFile.delete())
				{
					out.println("Deleted: " + exeFileName);
				}
				needToMake = true;
			}
		}
		catch(ParseException pe)
		{
			throw pe;
		}
		catch(FileNotFoundException ee)
		{
			throw ee;
		}
		catch(IOException ee)
		{
			out.println(ee.getMessage());
			out.println("Can't open an output file!");
		}
	}

	/**
	 * Commences execution of the model.  Blocks until completion.
	 *
	 * @return 0 if no problems:  ready to execute;
	 * 1 and messages to the output PrintWriter if problems
	 */
	public int runModel()
	{
		//    String directory;
		int status = doCommand(exeFileName + " " + tempFilePath + " " + commonPath, false);
		if(status != 0)
		{
			return status;
		}
		// successful run.  Remove several temporary output files.
		if(!_buildDebug)
		{
			new File("wranglerDSS.log").delete();
			new File(".", "testxa.out").delete();
			new File("temp.bat").delete();
		}
		return 0;
	}

	/**
	 * Calls the Fortran compiler to create the object code. Also, deletes
	 * intermediate temporary files if everything went hunky-dory.
	 *
	 * @return true unless the compiler had a nonzero exit code.
	 */
	public boolean compile(String sourcePath, String cycleNum)
	{
		if(!successfulParse)
		{
			return false;
		}
		if(needToMake)
		{
			String makerString;
			if(cycleNum.length() < 2)
			{
				cycleNum = "0" + cycleNum;
			}
			if(!_hideProgressDetails)
			{
				out.println(cycleNum);
			}
			// Create code.f90 and types.f90
			makerString = calsimProgDir + "\\bin\\mkcode.exe " + tempFilePath + " " + cycleNum;
			if(doCommand(makerString, true) > 0)
			{
				return false;
			}
			if(!_buildDebug)
			{
				new File(tempFilePath, "defines" + cycleNum + ".txt").delete();
				new File(tempFilePath, "goals" + cycleNum + ".txt").delete();
				new File(tempFilePath, "weights" + cycleNum + ".txt").delete();
				new File(tempFilePath, "deflocal.txt").delete();
			}
			makerString = "lf90 -c "
					+ tempFilePath + "\\types" + cycleNum + ".f90 "
					+ " -mod " + tempFilePath + ";" + sourcePath
					+ " -win -trace -ml msvc";
			if(doCommand(makerString, true) > 0)
			{
				return false;
			}
			if(!_buildDebug)
			{
				new File(tempFilePath, "types" + cycleNum + ".f90").delete();
			}
			if(cycleNum.equals("00"))
			{
				makerString = "lf90 -c -o0 "
						+ tempFilePath + "\\code" + cycleNum + ".f90 "
						+ " -mod " + tempFilePath + ";" + sourcePath
						+ " -win -trace -ml msvc -stack 1000000";
			}
			else
			{
				makerString = "lf90 -c -o0 "
						+ tempFilePath + "\\code" + cycleNum + ".f90 "
						+ " -mod " + tempFilePath + ";" + sourcePath
						+ " -win -trace -ml msvc -stack 1000000";
			}
			if(doCommand(makerString, true) > 0)
			{
				new File(exeFileName).delete();
				return false;
			}
			if(!_buildDebug)
			{
				new File(tempFilePath, "code" + cycleNum + ".f90").delete();
			}
			//needToMake = false;
		}
		else
		{
			// no files need to be remade
			if(!_buildDebug)
			{
				new File(tempFilePath, "dss_init.f90").delete();
			}
			out.println(exeFileName + " is up to date.");
		}
		return true;
	}

	/**
	 * Calls the Fortran compiler to create the global object code. Also, deletes
	 * intermediate temporary files if everything went hunky-dory.
	 *
	 * @return true unless the compiler had a nonzero exit code.
	 */
	public boolean compileGlobal(String sourcePath)
	{
		if(!successfulParse)
		{
			return false;
		}
		if(needToMake)
		{
			String makerString;
			makerString = "lf90 -c -o0 "
					+ tempFilePath + "\\code.f90 "
					+ tempFilePath + "\\dss_init.f90 "
					+ tempFilePath + "\\report_writer.f90 "
					+ " -mod " + tempFilePath + ";" + sourcePath
					+ " -win -trace -ml msvc -stack 1000000";
			if(doCommand(makerString, true) > 0)
			{
				new File(exeFileName).delete();
				return false;
			}
			if(!_buildDebug)
			{
				new File(tempFilePath, "code.f90").delete();
				new File(tempFilePath, "dss_init.f90").delete();
				new File(tempFilePath, "report_writer.f90").delete();
			}
		}
		else
		{
		}
		return true;
	}

	public boolean link(String sourcePath, Integer numCycles)
	{
		if(!successfulParse)
		{
			return false;
		}
		if(needToMake)
		{
			String makerString;
			// Create code.f90 and types.f90
			makerString = "lf90 " + tempFilePath + "\\code00.obj ";
			// CB added numberCycles to allow linking of compiled studies after all are compiled
			int numberCycles = 0;
			if(numCycles == null)
			{
				numberCycles = _numberCycles;
			}
			else
			{
				numberCycles = numCycles.intValue();
			}
			//CB	      for (int i = 1; i <= _numberCycles; i++ )	{
			for(int i = 1; i <= numberCycles; i++)
			{
				String s = java.lang.String.valueOf(i);
				if(s.length() < 2)
				{
					s = "0" + s;
				}
				makerString = makerString + tempFilePath + "\\code" + s + ".obj ";
			}
			makerString = makerString
					+ tempFilePath + "\\code.obj "
					+ tempFilePath + "\\dss_init.obj "
					+ tempFilePath + "\\report_writer.obj "
					+ "@" + tempFilePath + "\\externals.rsp "
					+ "@" + tempFilePath + "\\externalsLib.rsp "
					+ sourcePath + "\\calsim.obj "
					+ " -exe " + exeFileName
					+ " -mod " + tempFilePath + ";" + sourcePath
					+ " -win -trace -ml msvc -stack 1000000 -wisk -nomap"
					+ " -LIBpath " + sourcePath
					+ " -lib wrangler,simsolver ";
			if(doCommand(makerString, true) > 0)
			{
				new File(exeFileName).delete();
				return false;
			}
			if(!_buildDebug)
			{
				for(int i = 0; i <= _numberCycles; i++)
				{
					String s = java.lang.String.valueOf(i);
					if(s.length() < 2)
					{
						s = "0" + s;
					}
					new File(tempFilePath, "code" + s + ".obj").delete();
					new File(tempFilePath, "types" + s + ".mod").delete();
					new File(tempFilePath, "types" + s + ".lib").delete();
				}
				new File(tempFilePath, "code.obj").delete();
				new File(tempFilePath, "modtable.txt").delete();
				new File(tempFilePath, "dss_init.obj").delete();
				new File(tempFilePath, "report_writer.obj").delete();
				new File(tempFilePath, "externals.rsp").delete();
				new File(tempFilePath, "externalsLib.rsp").delete();
			}
			//needToMake = false;
		}
		else
		{
			// no files need to be remade
			if(!_buildDebug)
			{
				new File(tempFilePath, "dss_init.f90").delete();
				new File(tempFilePath, "report_writer.f90").delete();
			}
			out.println(exeFileName + " is up to date.");
		}
		return true;
	}

	/**
	 * Calls the Fortran linker to create the executable.  Also, deletes
	 * intermediate temporary files if everything went hunky-dory.
	 *
	 * @return true unless the linker had a nonzero exit code.
	 */
	public boolean link(String sourcePath)
	{
		return link(sourcePath, null);
	}

	// Translate (parse) from Wresl into three text files and a Fortran 90
	// Returns a list of files that are newer than exeFileName
	private Vector parseAFile() throws ParseException, IOException
	{
		// There are newer files, so parse the file
		WreslParser parser = new WreslParser(new FileInputStream(ident));
		WreslParser.initParameters(tempFilePath, out, _hideWarnings, _hideProgressDetails, _parseFileDelay);
		WreslParser.openGlobalFiles();
		WreslParser.beginGlobalCode();
		parser.identify(new File(ident));
		parser.StudyUnit();
		_numberCycles = parser.getNumberOfWreslCycles();
		if(!_hideProgressDetails)
		{
			out.println("Wresl Cycles - parser " + parser.getNumberOfWreslCycles());
		}
		WreslParser.endGlobalCode();
		return parser.newerFiles(exeFileName);
	}

	/**
	 * Get number of cycles specified in Wresl file
	 */
	public int getNumberOfWreslCycles()
	{
		return _numberCycles;
	}

	/**
	 * Runs a DOS command and attempts to capture its output.
	 */
	public int doCommand(String cmd, boolean minwin)
	{
		Vector hidingDetailsMessages = null;    //CB added hiding messages code
		try
		{
			if(_hideProgressDetails)
			{
				hidingDetailsMessages = new Vector();
			}
			Process p;
			//			if (_buildDebug || _lf90Debug) {
			if(_buildDebug)
			{
				p = Runtime.getRuntime().exec(cmd);
			}
			else
			{
				PrintWriter tempWriter = new PrintWriter(new BufferedWriter(new FileWriter("temp.bat")));
				if(minwin)
				{
					tempWriter.println("start /min " + cmd);
				}
				else
				{
					tempWriter.println(cmd);
				}
				tempWriter.close();
				p = Runtime.getRuntime().exec("temp.bat");
			}
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			while(null != (line = stdout.readLine()))
			{  //CB added hiding messages code
				if(!_hideProgressDetails)
				{
					out.println(line);
				}
				else
				{
					hidingDetailsMessages.addElement(line);
				}
			}
			if(!_hideProgressDetails)
			{
				out.println("");
			}
			stdout.close();
			while(null != (line = stderr.readLine()))
			{
				//				if (!_hideProgressDetails)
				out.println(line);
				//				else hidingDetailsMessages.addElement(line);
			}
			stderr.close();
			return p.waitFor();
		}
		catch(IOException ee)
		{
			if(!_hideProgressDetails)
			{  //CB moved block here to reduce unnessary messages
				out.println();
				out.println("Invoking command: " + cmd);
			}
			else
			{
				hidingDetailsMessages.addElement("Invoking command: " + cmd);
			}

			out.println(ee.getMessage());
			if(_hideProgressDetails)
			{
				if(hidingDetailsMessages != null)
				{
					Enumeration messages = hidingDetailsMessages.elements();
					while(messages.hasMoreElements())
					{
						String message = (String) messages.nextElement();
						out.println(message);
					}
				}
			}
			return 1;
		}
		catch(InterruptedException ee)
		{
			out.println("Ahem!  You interrupted.");
			if(_hideProgressDetails)
			{
				if(hidingDetailsMessages != null)
				{
					Enumeration messages = hidingDetailsMessages.elements();
					while(messages.hasMoreElements())
					{
						String message = (String) messages.nextElement();
						out.println(message);
					}
				}
			}
			return 1;
		}
	}
}
