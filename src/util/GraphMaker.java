package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GraphMaker {

	public static JFreeChart makeChart(String title, String xAxeName, String yAxeName, XYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart(title, xAxeName, yAxeName, dataset);
		return chart;
	}

	public static void saveChartAsJPEG(String fileName, JFreeChart chart, int width, int height) {
		try {
			ChartUtilities.saveChartAsJPEG(new File(fileName), chart, width, height);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static XYSeries makeSeries(String name, ArrayList<Double> xValues, ArrayList<Double> yValues) {
		XYSeries series = new XYSeries(name);
		for (int i = 0; i < xValues.size(); i++) {
			series.add(xValues.get(i), yValues.get(i));
		}
		return series;
	}

	public static XYDataset makeDataset(XYSeries series) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		return dataset;
	}

	public static void customizeChart(JFreeChart chart) {
		XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setLegendLine(new Line2D.Double(-50.0D, 0.0D, 50.0D, 0.0D));
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.BLUE);
		renderer.setSeriesStroke(0, new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 5.0f,
				new float[] { 20.0f, 20.0f }, 2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(5.0f));
		plot.setRenderer(renderer);
		plot.setBackgroundPaint(Color.WHITE);
		Font labelFont = new Font("AxisLabelFont", Font.BOLD, 30);
		plot.getDomainAxis().setLabelFont(labelFont);
		plot.getRangeAxis().setLabelFont(labelFont);
		Font axisValuesFont = new Font("AxisValuesFont", Font.PLAIN, 25);
		plot.getDomainAxis().setTickLabelFont(axisValuesFont);
		plot.getRangeAxis().setTickLabelFont(axisValuesFont);
		Font legendTitleFont = new Font("AxisLabelFont", Font.BOLD, 30);
		chart.getLegend().setItemFont(legendTitleFont);
		chart.getTitle().setFont(legendTitleFont);
	}

}
