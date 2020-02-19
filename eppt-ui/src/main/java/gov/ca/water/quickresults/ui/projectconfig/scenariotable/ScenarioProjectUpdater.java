/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2020.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */

package gov.ca.water.quickresults.ui.projectconfig.scenariotable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import calsim.app.AppUtils;
import gov.ca.water.calgui.project.EpptDssContainer;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.calgui.project.NamedDssPath;
import gov.ca.water.quickresults.ui.projectconfig.ProjectConfigurationPanel;
import vista.set.Pathname;

import hec.heclib.dss.HecDataManager;
import hec.heclib.dss.HecDss;

import static java.util.stream.Collectors.toList;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 01-31-2020
 */
public class ScenarioProjectUpdater
{
	private static final Logger LOGGER = Logger.getLogger(ScenarioProjectUpdater.class.getName());

	private ScenarioProjectUpdater()
	{
		throw new AssertionError("Utility class");
	}

	public static void updateWithAllDssFiles()
	{
		CompletableFuture.runAsync(ScenarioProjectUpdater::updateWithAllDssFilesSync);
	}

	private static void updateWithAllDssFilesSync()
	{
		Hashtable dvList = AppUtils.getCurrentProject()._dvList;
		ProjectConfigurationPanel projectConfigurationPanel = ProjectConfigurationPanel.getProjectConfigurationPanel();
		List<EpptScenarioRun> scenarioRuns = new ArrayList<>();
		scenarioRuns.add(projectConfigurationPanel.getBaseScenario());
		scenarioRuns.addAll(projectConfigurationPanel.getEpptScenarioAlternatives());
		List<String> collect = scenarioRuns.stream().map(EpptScenarioRun::getDssContainer)
										   .map(EpptDssContainer::getAllDssFiles)
										   .flatMap(Collection::stream)
										   .map(NamedDssPath::getDssPath)
										   .map(Object::toString)
										   .filter(HecDataManager::doesDSSFileExist)
										   .collect(toList());
		for(String dssPath : collect)
		{
			try
			{
				HecDss open = HecDss.open(dssPath);
				Vector<?> catalogedPathnames = open.getCatalogedPathnames();
				for(Object obj : catalogedPathnames)
				{
					Pathname pathname = Pathname.createPathname(obj.toString());
					dvList.put(pathname.getPart(Pathname.B_PART), pathname.getPart(Pathname.C_PART));
				}
			}
			catch(Exception ex)
			{
				LOGGER.log(Level.SEVERE, "Error reading DSS file: " + dssPath, ex);
			}
		}
	}
}