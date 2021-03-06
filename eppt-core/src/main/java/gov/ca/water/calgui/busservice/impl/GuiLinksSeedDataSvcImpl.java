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

package gov.ca.water.calgui.busservice.impl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import gov.ca.water.calgui.EpptInitializationException;
import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.GUILinksAllModelsBO;
import gov.ca.water.calgui.busservice.IGuiLinksSeedDataSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.techservice.IFileSystemSvc;
import gov.ca.water.calgui.techservice.impl.FilePredicates;
import gov.ca.water.calgui.techservice.impl.FileSystemSvcImpl;
import org.apache.log4j.Logger;

import static gov.ca.water.calgui.constant.Constant.CONFIG_DIR;
import static gov.ca.water.calgui.constant.Constant.CSV_EXT;

/**
 * This class holds key required data for the application from the GUI_Links_AllModels.csv file.
 *
 * @author Mohan
 */
public final class GuiLinksSeedDataSvcImpl implements IGuiLinksSeedDataSvc
{
	private static final Logger LOG = Logger.getLogger(GuiLinksSeedDataSvcImpl.class.getName());
	private static final String GUI_LINKS_ALL_MODELS_FILENAME = CONFIG_DIR + "/GUILinks" + CSV_EXT;
	private static IGuiLinksSeedDataSvc seedDataSvc;
	private final NavigableMap<Integer, GUILinksAllModelsBO> _guiLinksAllModels = new TreeMap<>();

	/**
	 * This will read the gui_links.csv files and build the list and maps of {@link GUILinksAllModelsBO}
	 */
	private GuiLinksSeedDataSvcImpl() throws EpptInitializationException
	{
		LOG.debug("Building SeedDataSvcImpl Object.");
		initGuiLinksAllModels();
	}

	/**
	 * This method is for implementing the singleton. It will return the
	 * instance of this class if it is empty it will create one.
	 *
	 * @return Will return the instance of this class if it is empty it will
	 * create one.
	 */
	public static IGuiLinksSeedDataSvc getSeedDataSvcImplInstance()
	{
		if(seedDataSvc == null)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			return seedDataSvc;
		}
	}

	public List<GUILinksAllModelsBO> getAllGuiLinks()
	{
		return new ArrayList<>(_guiLinksAllModels.values());
	}

	public static void createSeedDataSvcImplInstance() throws EpptInitializationException
	{
		if(seedDataSvc == null)
		{
			seedDataSvc = new GuiLinksSeedDataSvcImpl();
		}
	}

	private void initGuiLinksAllModels() throws EpptInitializationException
	{
		IFileSystemSvc fileSystemSvc = new FileSystemSvcImpl();
		String errorStr = "";
		try
		{
			List<String> guiLinkStrings = fileSystemSvc.getFileData(Paths.get(GUI_LINKS_ALL_MODELS_FILENAME), true,
					FilePredicates.commentFilter());
			for(String guiLinkString : guiLinkStrings)
			{
				errorStr = guiLinkString;
				//don't count comment rows
				if(guiLinkString.length() > 0)
				{
					if(guiLinkString.trim().charAt(0) == '\uFEFF' || guiLinkString.trim().charAt(0) == '!' || guiLinkString.trim().charAt(0) == '#')
					{
						continue;
					}
				}
				else
				{
					continue;
				}


				String[] list = guiLinkString.split(Constant.DELIMITER);
				int checkboxId = Integer.parseInt(list[0].trim());
				GUILinksAllModelsBO guiLinksAllModelsBO = _guiLinksAllModels.computeIfAbsent(checkboxId,
						id -> createGuiLinksAllModels(list, id));
				String model = list[1];
				String primary = null;
				if(list.length > 2)
				{
					primary = list[2].trim();
				}
				String secondary = null;
				if(list.length > 3)
				{
					secondary = list[3].trim();
				}
				guiLinksAllModelsBO.addModelMapping(model, primary, secondary);
			}
		}
		catch(ArrayIndexOutOfBoundsException ex)
		{
			String errorMessage = "In file \"" + GUI_LINKS_ALL_MODELS_FILENAME + "\" has corrupted data at line \"" + errorStr + "\""
					+ Constant.NEW_LINE + "The column number which the data is corrupted is " + ex.getMessage();
			LOG.error(errorMessage, ex);
			throw new EpptInitializationException(errorMessage);
		}
		catch(NumberFormatException ex)
		{
			String errorMessage = "In file \"" + GUI_LINKS_ALL_MODELS_FILENAME + "\" has corrupted data at line \"" + errorStr + "\""
					+ Constant.NEW_LINE + "The checkbox id is not an Integer " + ex.getMessage();
			LOG.error(errorMessage, ex);
			throw new EpptInitializationException(errorMessage);
		}
		catch(IllegalArgumentException ex)
		{
			String errorMessage = "In file \"" + GUI_LINKS_ALL_MODELS_FILENAME + "\" has corrupted data at line \"" + errorStr + "\""
					+ Constant.NEW_LINE + "The model could not be parsed " + ex.getMessage();
			LOG.error(errorMessage, ex);
			throw new EpptInitializationException(errorMessage);
		}
		catch(CalLiteGUIException ex)
		{
			LOG.error(ex.getMessage(), ex);
			throw new EpptInitializationException("Failed to get file data for file: " + GUI_LINKS_ALL_MODELS_FILENAME, ex);
		}
	}

	private GUILinksAllModelsBO createGuiLinksAllModels(String[] list, int checkboxId)
	{
		String yTitle = "";
		if(list.length > 4)
		{
			yTitle = list[4].replace("%2C", ",");
		}
		String title = "";
		if(list.length > 5)
		{
			title = list[5].replace("%2C", ",");
		}
		String sLegend = "";
		if(list.length > 6)
		{
			sLegend = list[6].replace("%2C", ",");
		}
		String type = "";
		if(list.length > 7)
		{
			type = list[7].replace("%2C", ",");
		}
		String id = extractCheckboxId(checkboxId);
		return new GUILinksAllModelsBO(id, yTitle, title, sLegend, type);
	}

	private String extractCheckboxId(int checkboxId)
	{
		String id;
		try
		{
			id = String.format("ckbp%03d", checkboxId);
		}
		catch(NumberFormatException e)
		{
			id = Integer.toString(checkboxId);
		}
		return id;
	}

	private int extractCheckboxId(String checkboxId)
	{
		int id;
		try
		{
			id = Integer.parseInt(checkboxId.replace("ckbp", ""));
		}
		catch(NumberFormatException e)
		{
			id = Integer.parseInt(checkboxId);
		}
		return id;
	}

	public GUILinksAllModelsBO getGuiLink(String id)
	{
		GUILinksAllModelsBO guiLinks3BO = null;
		try
		{
			guiLinks3BO = _guiLinksAllModels.get(extractCheckboxId(id));
			if(guiLinks3BO == null)
			{
				LOG.debug("There is no GUI Links data for this id = " + id);
			}
		}
		catch(RuntimeException e)
		{
			LOG.debug("Error obtaining GUI Link", e);
		}
		return guiLinks3BO;
	}

}
