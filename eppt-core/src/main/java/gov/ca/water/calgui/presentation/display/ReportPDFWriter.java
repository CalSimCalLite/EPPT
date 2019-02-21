/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.calgui.presentation.display;

import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.TextField;
import com.itextpdf.text.pdf.*;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.presentation.Report.Writer;
import gov.ca.water.calgui.tech_service.IDialogSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.DialogSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RectangleInsets;
import org.swixml.SwingEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportPDFWriter implements Writer
{
	private static Logger LOG = Logger.getLogger(ReportPDFWriter.class.getName());
	private static IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	Document document;
	private PdfWriter writer;
	private PdfPTable summaryTable;
	private Font bigFont;
	private Font subtitleFont;
	private Font smallBoldFont;
	private String dateStr;
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private Font tableFont;
	private Font tableBoldFont;
	private IDialogSvc dialogSvc = DialogSvcImpl.getDialogSvcInstance();

	public ReportPDFWriter()
	{

	}

	public ReportPDFWriter(String filename)
	{
		startDocument(filename);
	}

	@Override
	public void setTableFontSize(String tableFontSize)
	{

		int fontSize = 9;
		try
		{
			fontSize = Integer.parseInt(tableFontSize.trim());
		}
		catch(NumberFormatException nfe)
		{
			errorHandlingSvc.validationeErrorHandler("Number format exception in font size", nfe.getMessage(), null);
		}

		tableFont = FontFactory.getFont("Arial", fontSize);
		tableBoldFont = FontFactory.getFont("Arial", fontSize);
		tableBoldFont.setStyle(Font.BOLD);

	}

	@Override
	public void startDocument(String filename)
	{

		// Check if file is already open

		bigFont = FontFactory.getFont("Arial", 12);
		smallBoldFont = FontFactory.getFont("Arial", 10);
		smallBoldFont.setStyle(Font.BOLD);
		subtitleFont = FontFactory.getFont("Arial", 10);
		subtitleFont.setStyle(Font.BOLD);
		document = new Document();
		document.setPageSize(PageSize.A4.rotate());
		document.addCreationDate();
		try
		{
			writer = PdfWriter.getInstance(
					// that listens to the document
					document,
					// and directs a PDF-stream to a file
					new FileOutputStream(filename));
			document.open();
		}
		catch(DocumentException de)
		{
			LOG.debug(de.getMessage());
		}
		catch(IOException ioe)
		{
			LOG.debug(ioe.getMessage());
			// JOptionPane.showMessageDialog(null, "Please close the file " +
			// (new File(filename).getName()) + " if it is open.",
			// "Warning!", JOptionPane.WARNING_MESSAGE);
			// ImageIcon icon = new
			// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
			// Object[] options = { "OK" };
			// JOptionPane optionPane = new JOptionPane(
			// "Please close the file " + (new File(filename).getName()) + " if
			// it is open.",
			// JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION, null, options,
			// options[0]);
			// JDialog dialog =
			// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
			// "CalLite");
			// dialog.setIconImage(icon.getImage());
			// dialog.setResizable(false);
			// dialog.setVisible(true);
			dialogSvc.getOK("Please close the file " + (new File(filename).getName()) + " if it is open.",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
	}

	@Override
	public void addTitlePage(String compareInfo, String author, String fileBase, String fileAlt)
	{
		document.newPage();
		try
		{
			Paragraph title = new Paragraph("\n\n\n\n" + compareInfo,
					FontFactory.getFont("Arial", 24f, Font.BOLD, BaseColor.BLUE));
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);
			Paragraph pauthor = new Paragraph("\n\n" + "Author: " + author,
					FontFactory.getFont("Arial", 16, Font.BOLD));
			pauthor.setAlignment(Element.ALIGN_CENTER);
			document.add(pauthor);
			dateStr = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
			Paragraph pdate = new Paragraph("\n" + "Generated on " + dateStr);
			pdate.setAlignment(Element.ALIGN_CENTER);
			document.add(pdate);
			try
			{
				TextField tf1 = new TextField(writer, new Rectangle(40, 100, 800, 150), "fox");
				tf1.setBackgroundColor(BaseColor.WHITE);
				tf1.setBorderColor(BaseColor.RED);
				tf1.setBorderWidth(1);
				tf1.setRotation(90);
				// tf1.setBorderStyle(PdfBorderDictionary.STYLE_BEVELED);
				tf1.setText("Alternative 1 DSS file: " + fileBase + "\n\n" + "Alternative 2 DSS file: " + fileAlt);
				tf1.setAlignment(Element.ALIGN_LEFT);
				tf1.setOptions(TextField.REQUIRED | TextField.READ_ONLY | TextField.MULTILINE);
				writer.addAnnotation(tf1.getTextField());
			}
			catch(Exception ex)
			{
				LOG.error(ex.getMessage());
			}

		}
		catch(DocumentException e)
		{
			LOG.error(e.getMessage());
		}
	}

	@Override
	public void setAuthor(String author)
	{
		/*
		 * footer = new HeaderFooter(new Phrase(author), new Phrase(dateStr));
		 * document.setFooter(footer); document.addAuthor(author);
		 */
	}

	public void addNewPage()
	{
		document.newPage();
	}

	public void writeParagraph(String text) throws DocumentException
	{
		document.newPage();
		document.add(new Paragraph(text, bigFont));
	}

	protected void drawChart(JFreeChart chart)
	{
		document.newPage();
		new PdfOutline(writer.getRootOutline(), new PdfDestination(PdfDestination.FITH), chart.getTitle().getText());
		PdfContentByte cb = writer.getDirectContent();
		Graphics2D graphics2D = cb.createGraphics(PageSize.A4.getHeight(), PageSize.A4.getWidth());
		Rectangle2D r2d2 = new Rectangle2D.Double(36, 36, (double) PageSize.A4.getHeight() - 72, (double) PageSize.A4.getWidth() - 72);
		chart.draw(graphics2D, r2d2);
		graphics2D.dispose();
	}

	@Override
	public void addTableHeader(ArrayList<String> headerRow, int[] columnSpans)
	{
		if(summaryTable == null)
		{
			summaryTable = new PdfPTable(new float[]{3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
			summaryTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			summaryTable.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
			summaryTable.setWidthPercentage(100);
			new PdfOutline(writer.getRootOutline(), new PdfDestination(PdfDestination.FITH), "Summary Table");
		}
		addTableRow(headerRow, columnSpans, BOLD, true);
		PdfPRow row = summaryTable.getRow(summaryTable.getRows().size() - 1);
		PdfPCell[] cells = row.getCells();
		for(PdfPCell cell : cells)
		{
			if(cell != null)
			{
				cell.setGrayFill(0.6f);
				cell.setPaddingLeft(1);
			}
		}
	}

	@Override
	public void addTableRow(List<String> rowData, int[] columnSpans, int style, boolean centered)
	{
		for(int i = 0; i < rowData.size(); i++)
		{

			PdfPCell cell = new PdfPCell(new Phrase(rowData.get(i), style == BOLD ? tableBoldFont : tableFont));

			if(centered)
			{
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			}
			else
			{
				if(i != 0)
				{
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				}
				else
				{
					if(style == BOLD)
					{
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					}
					else
					{
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					}
				}
			}
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			if(columnSpans != null)
			{
				cell.setColspan(columnSpans[i]);
			}
			summaryTable.addCell(cell);
		}
	}

	@Override
	public void addTableTitle(String title)
	{
		try
		{
			document.newPage();
			document.add(new Paragraph(title, bigFont));
		}
		catch(DocumentException ex)
		{
			LOG.debug(ex.getMessage());
			throw new RuntimeException("Write Error: Close the PDF report file");
		}
	}

	@Override
	public void addTableSubTitle(String subtitle)
	{
		try
		{
			document.add(new Paragraph(subtitle + "\n", subtitleFont));
		}
		catch(DocumentException ex)
		{
			LOG.debug(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void endDocument()
	{
		document.close();
	}

	@Override
	public void endTable()
	{
		try
		{
			PdfPCell[] cells = summaryTable.getRow(0).getCells();
			setRightAndLeftBorders(cells);
			setColumnBoundaries(cells, 1);
			for(PdfPCell cell : cells)
			{
				if(cell != null)
				{
					cell.setBorderWidthTop(2);
				}
			}
			ArrayList<PdfPRow> rows = summaryTable.getRows();
			for(int i = 1; i < rows.size() - 1; i++)
			{
				PdfPCell[] dataCells = rows.get(i).getCells();
				setRightAndLeftBorders(dataCells);
				setColumnBoundaries(dataCells, 4);
				setCellPadding(dataCells, 3, 2);
			}
			PdfPRow lastRow = rows.get(rows.size() - 1);
			cells = lastRow.getCells();
			setRightAndLeftBorders(cells);
			setColumnBoundaries(cells, 4);
			setCellPadding(cells, 3, 4);
			for(PdfPCell cell : cells)
			{
				if(cell != null)
				{
					cell.setBorderWidthBottom(2);
				}
			}
			document.add(summaryTable);
		}
		catch(DocumentException ex)
		{
			LOG.debug(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	private void setCellPadding(PdfPCell[] cells, int padding, int bottomPadding)
	{
		for(PdfPCell cell : cells)
		{
			if(cell != null)
			{
				cell.setPadding(padding);
				cell.setPaddingBottom(bottomPadding);
			}
		}
	}

	private void setColumnBoundaries(PdfPCell[] cells, int span)
	{
		for(int i = 0; i < cells.length; i++)
		{
			if((i % span == 0) && (cells[i] != null))
			{
				cells[i].setBorderWidthRight(2);
			}
		}
	}

	private void setRightAndLeftBorders(PdfPCell[] cells)
	{
		if(cells == null)
		{
			return;
		}
		if(cells[0] != null)
		{
			cells[0].setBorderWidthLeft(2);
		}
		int i = cells.length - 1;
		while((cells[i] == null) && (i >= 0))
		{
			i--;
		}
		if(i >= 0)
		{
			cells[i].setBorderWidthRight(2);
		}
	}

	@Override
	public void addExceedancePlot(ArrayList<double[]> buildDataArray, String title, String[] seriesName,
								  String xAxisLabel, String yAxisLabel)
	{
		DefaultXYDataset dataset = new DefaultXYDataset();
		for(int i = seriesName.length - 1; i >= 0; i--)
		{
			double[][] seriesData = new double[2][buildDataArray.size()];
			for(int j = 0; j < buildDataArray.size(); j++)
			{
				double[] data = buildDataArray.get(j);
				seriesData[0][j] = data[0];
				seriesData[1][j] = data[i + 1];
			}
			dataset.addSeries(seriesName[i], seriesData);
		}

		final JFreeChart xyLineChart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset);
		XYPlot xyPlot = xyLineChart.getXYPlot();
		ValueAxis domainAxis = xyPlot.getDomainAxis();
		domainAxis.setInverted(true);
		xyPlot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		xyPlot.setBackgroundPaint(null);
		xyPlot.setRangeGridlinePaint(Color.lightGray);
		xyPlot.setDomainGridlinesVisible(false);
		xyPlot.getDomainAxis().setRange(0, 100);
		drawChart(xyLineChart);
	}

	@Override
	public void addTimeSeriesPlot(ArrayList<double[]> buildDataArray, String title, String[] seriesName,
								  String xAxisLabel, String yAxisLabel)
	{
		TimeSeriesCollection datasets = new TimeSeriesCollection();
		for(int i = seriesName.length - 1; i >= 0; i--)
		{
			TimeSeries ts = new TimeSeries(seriesName[i]);
			datasets.addSeries(ts);
		}
		for(int j = 0; j < buildDataArray.size(); j++)
		{
			double[] dataArray = buildDataArray.get(j);
			Month m = new Month(new Date(Math.round(dataArray[0])));
			for(int i = 0; i < seriesName.length; i++)
			{
				datasets.getSeries(i).add(m, dataArray[i + 1], false);
			}
		}
		final JFreeChart tsChart = ChartFactory.createTimeSeriesChart(title, xAxisLabel, yAxisLabel, datasets);
		XYPlot xyPlot = tsChart.getXYPlot();
		xyPlot.setRenderer(new XYStepRenderer());
		xyPlot.setBackgroundPaint(null);
		xyPlot.setDomainGridlinesVisible(false);
		xyPlot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		drawChart(tsChart);
	}

}