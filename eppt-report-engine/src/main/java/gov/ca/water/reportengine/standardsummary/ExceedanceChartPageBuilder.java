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

package gov.ca.water.reportengine.standardsummary;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import gov.ca.water.calgui.bo.MonthPeriodFilter;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.calgui.scripts.DssMissingRecordException;
import gov.ca.water.plotly.ExceedanceData;
import gov.ca.water.plotly.PlotlyChart;
import gov.ca.water.plotly.qaqc.PlotlyExceedancePage;
import gov.ca.water.reportengine.EpptReportException;
import org.w3c.dom.Document;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 08-26-2019
 */
class ExceedanceChartPageBuilder extends PlotChartBuilder
{

	private static final Logger LOGGER = Logger.getLogger(ExceedanceChartPageBuilder.class.getName());

	ExceedanceChartPageBuilder(Document document, EpptScenarioRun base, List<EpptScenarioRun> alternatives,
							   SummaryReportParameters reportParameters,
							   StandardSummaryErrors standardSummaryErrors)
	{
		super(document, base, alternatives, reportParameters, standardSummaryErrors);
	}

	@Override
	PlotlyChart buildChart(List<ChartComponent> chartComponents)
	{
		String title = getTitleForComponents(chartComponents);
		String yAxisLabel = getYAxisLabelForComponents(chartComponents);
		Map<EpptScenarioRun, PlotlyExceedancePage.ExceedanceMonthData> exceedanceData = new HashMap<>();
		exceedanceData.put(getBase(), buildMonthData(getBase(), chartComponents));
		for(EpptScenarioRun alternative : getAlternatives())
		{
			exceedanceData.put(alternative, buildMonthData(alternative, chartComponents));
		}
		return new PlotlyExceedancePage(title, yAxisLabel, exceedanceData);
	}

	private PlotlyExceedancePage.ExceedanceMonthData buildMonthData(EpptScenarioRun scenarioRun, List<ChartComponent> chartComponents)
	{

		EnumMap<Month, ExceedanceData> data = new EnumMap<>(Month.class);
		try
		{
			for(Month month : Month.values())
			{
				data.put(month, buildExceedanceData(month, scenarioRun, chartComponents));
			}
		}
		catch(EpptReportException e)
		{
			logScriptException(LOGGER, null, e);
		}
		return new PlotlyExceedancePage.ExceedanceMonthData(data);
	}

	private ExceedanceData buildExceedanceData(Month month, EpptScenarioRun scenarioRun, List<ChartComponent> chartComponents)
			throws EpptReportException
	{
		MonthPeriodFilter periodFilter = new MonthPeriodFilter(month);
		ChartComponent chartComponent = chartComponents.get(0);
		NavigableMap<Double, Double> primaryData = new TreeMap<>();
		Map<String, NavigableMap<Double, Double>> thresholdData = new HashMap<>();
		try
		{
			primaryData = createJythonValueGenerator(periodFilter, scenarioRun, chartComponent.getFunction()).generateExceedanceValues();
		}
		catch(DssMissingRecordException e)
		{
			logScriptException(LOGGER, chartComponent,
					new DssMissingRecordException(e.getMessage() + " for month: " + month.getDisplayName(TextStyle.SHORT, Locale.getDefault())));
		}
		for(int i = 1; i < chartComponents.size(); i++)
		{
			ChartComponent component = chartComponents.get(i);
			try
			{
				NavigableMap<Double, Double> threshold = createJythonValueGenerator(periodFilter, scenarioRun,
						component.getFunction()).generateExceedanceValues();
				thresholdData.put(component.getComponent(), threshold);
			}
			catch(DssMissingRecordException e)
			{
				logScriptException(LOGGER, component,
						new DssMissingRecordException(e.getMessage() + " for month: " + month.getDisplayName(TextStyle.SHORT, Locale.getDefault())));
			}
		}
		return new ExceedanceData(scenarioRun.getName(), primaryData, thresholdData);
	}
}
