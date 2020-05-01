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

package gov.ca.water.trendreporting;


import java.awt.Dimension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.ca.water.calgui.bo.WaterYearDefinition;
import gov.ca.water.calgui.bo.WaterYearPeriodRangesFilter;
import gov.ca.water.calgui.busservice.impl.EpptParameter;
import gov.ca.water.calgui.busservice.impl.EpptStatistic;
import gov.ca.water.calgui.busservice.impl.MonthPeriod;
import gov.ca.water.calgui.project.EpptConfigurationController;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.quickresults.ui.global.EpptParametersPane;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import static java.util.stream.Collectors.toList;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 05-21-2019
 */
public class TrendReportPanel extends JFXPanel
{
	private static final Logger LOGGER = Logger.getLogger(TrendReportPanel.class.getName());

	private final List<EpptScenarioRun> _scenarioRuns = new ArrayList<>();
	private final EpptConfigurationController _epptConfigurationController;
	private TrendReportFlowPane _javascriptPane;
	private EpptParametersPane _parametersPane;
	private ProgressBar _progressBar;
	private Label _progressTextLabel;
	private Button _applyBtn;
	private TrendReportPagesPane _trendReportPagesPane;

	public TrendReportPanel(EpptConfigurationController epptConfigurationController)
	{
		_epptConfigurationController = epptConfigurationController;
		setMinimumSize(new Dimension(1300, 800));
		setPreferredSize(new Dimension(1300, 800));
		Platform.setImplicitExit(false);
		Platform.runLater(this::init);
	}

	private void init()
	{
		_javascriptPane = new TrendReportFlowPane();
		_parametersPane = new EpptParametersPane();
		_trendReportPagesPane = new TrendReportPagesPane(this::inputsChanged);
		_applyBtn = new Button("Refresh");

		_applyBtn.setOnAction(e -> inputsChanged());
		ImageView imageView = new ImageView("gov/ca/water/trendreporting/refresh.png");
		imageView.setFitHeight(16);
		imageView.setFitWidth(16);
		_applyBtn.setGraphic(imageView);

		Insets insets = new Insets(4);
		BorderPane mainPane = new BorderPane();
		mainPane.setStyle("-fx-font-size: 11");
		mainPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		BorderPane center = buildJavascriptPane();
		BorderPane.setMargin(center, insets);
		mainPane.setCenter(center);
		Pane left = buildControls();
		BorderPane flowPane = new BorderPane(null, null, _applyBtn, null, left);
		flowPane.setPrefHeight(40);
		BorderPane.setMargin(left, insets);
		BorderPane.setMargin(_applyBtn, insets);
		mainPane.setTop(flowPane);
		left.setMaxWidth(Double.MAX_VALUE);
		flowPane.setMaxWidth(Double.MAX_VALUE);
		Node bottom = buildProgressControls();
		BorderPane.setMargin(bottom, insets);
		mainPane.setBottom(bottom);
		Scene scene = new Scene(mainPane);
		setScene(scene);
	}

	private Node buildProgressControls()
	{
		FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
		_progressBar = new ProgressBar();
		_progressBar.setVisible(false);
		_progressTextLabel = new Label();
		flowPane.setHgap(10);
		flowPane.getChildren().addAll(_progressTextLabel, _progressBar);
		flowPane.setAlignment(Pos.BOTTOM_LEFT);
		return flowPane;
	}

	private Pane buildControls()
	{
		Insets insets = new Insets(0, 4, 0, 4);
		BorderPane borderPane = new BorderPane();
		FlowPane center = new FlowPane(Orientation.VERTICAL, 5.0, 0, _trendReportPagesPane);
		borderPane.setLeft(_parametersPane);
		borderPane.setCenter(center);
		BorderPane.setMargin(center, insets);
		return borderPane;
	}

	private BorderPane buildJavascriptPane()
	{
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(_javascriptPane.getDashboardPane());
		BorderPane.setMargin(borderPane, new Insets(5.0, 0, 0, 0));
		return borderPane;
	}

	private void inputsChanged()
	{
		TrendReportTabConfig trendReportTabConfig = _trendReportPagesPane.getTrendReportTabConfig();
		if(trendReportTabConfig != null)
		{
			_scenarioRuns.clear();
			_epptConfigurationController.getEpptScenarioBase().ifPresent(_scenarioRuns::add);
			_scenarioRuns.addAll(_epptConfigurationController.getEpptScenarioAlternatives());
			int startYear = _epptConfigurationController.getStartYear();
			int endYear = _epptConfigurationController.getEndYear();
			boolean taf = _epptConfigurationController.isTaf();
			List<EpptStatistic> statistic = _epptConfigurationController.getSelectedStatistics();
			List<MonthPeriod> monthPeriod = _epptConfigurationController.getSelectedMonthlyPeriods();
			WaterYearDefinition waterYearDefinition = _epptConfigurationController.getWaterYearDefinition();
			List<EpptParameter> guiLink = new ArrayList<>(_parametersPane.getSelectedItems());
			List<EpptScenarioRun> scenarioRuns = _scenarioRuns.stream().filter(Objects::nonNull).collect(toList());
			boolean difference = _epptConfigurationController.isDifference();
			List<Map<EpptScenarioRun, WaterYearPeriodRangesFilter>> waterYearPeriodRanges = _epptConfigurationController.getWaterYearPeriodRanges();
			TrendReportDataLoader trendReportDataLoader = new TrendReportDataLoader(scenarioRuns, guiLink, startYear, endYear, taf, difference,
					waterYearDefinition, statistic, monthPeriod, waterYearPeriodRanges);
			loadJavascript(trendReportTabConfig.getPath(),
					trendReportDataLoader);
		}
	}

	private void loadJavascript(Path path, TrendReportDataLoader trendReportDataLoader)
	{
		CompletableFuture.runAsync(this::setUiLoading, Platform::runLater)
						 .thenApplyAsync(e -> trendReportDataLoader.computeScenarios())
						 .whenCompleteAsync((jsonObjects, t) -> handleWhenComplete(path, jsonObjects, t), Platform::runLater);
	}

	private void handleWhenComplete(Path path, List<JSONObject> jsonObjects, Throwable t)
	{
		if(t != null)
		{
			if(t.getCause() instanceof CancellationException)
			{
				LOGGER.log(Level.FINE, "Plotting canceled", t);
			}
			else if(t.getCause() instanceof TrendReportDataLoader.TrendReportException)
			{
				_progressTextLabel.setText(t.getCause().getMessage());
			}
			else
			{
				String msg = "Error plotting Trend Report";
				LOGGER.log(Level.SEVERE, msg, t);
				_progressTextLabel.setText(msg);
			}
		}
		if(jsonObjects != null)
		{
			_javascriptPane.removeDashboardPanes();
			try
			{
				for(JSONObject jsonObject : jsonObjects)
				{
					_javascriptPane.addDashboardPane(path, "plot(" + jsonObject.toString(0) + ");");
					_progressTextLabel.setText("");
				}
			}
			catch(RuntimeException e)
			{
				LOGGER.log(Level.SEVERE, "Error plotting Trend Report with serialized JSON object", e);
			}
		}
		_applyBtn.setDisable(false);
		_progressBar.setVisible(false);
	}

	private void setUiLoading()
	{
		_progressBar.setVisible(true);
		_progressTextLabel.setText("Loading...");
		_applyBtn.setDisable(true);
	}

}
