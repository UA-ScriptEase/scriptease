package scriptease.gui.pane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import scriptease.controller.MetricAnalyzer;

@SuppressWarnings("serial")
/**
 * MetricsPanel represents the JPanel used to display story component
 * metrics. Each summary is displayed in separate tabs with their
 * respective histogram and pie charts.
 * 
 * @author jyuen
 */
public class MetricsPanel extends JPanel {

	private final MetricAnalyzer metrics;

	private static final String STORY_COMPONENT_STRING = "Story Component";
	private static final String COMPLEXITY_STRING = "Complexity";
	private static final String GENERAL_STRING = "General";
	private static final String METRICS_STRING = "Metrics";
	private static final String FAVOURITE_STRING = "Favourite";
	private static final String FREQUENCY_STRING = "Frequency";
	private static final String CAUSES_STRING = "Causes";
	private static final String EFFECTS_STRING = "Effects";
	private static final String KNOWITS_STRING = "Descriptions";
	private static final String ASKIT_STRING = "Questions";
	private static final String REPEATS_STRING = "Repeats";
	private static final String DELAY_STRING = "Delays";
	private static final String BLOCKS_STRING = "Blocks";

	/**
	 * Creates a new MetricsPanel with the default tabs and histograms.
	 */
	public MetricsPanel() {
		final JTabbedPane tabs = new JTabbedPane();

		this.metrics = MetricAnalyzer.getInstance();

		tabs.addTab(GENERAL_STRING, createGeneralPage());
		tabs.addTab(FAVOURITE_STRING, createFavoriteCausesPage());
		tabs.addTab(CAUSES_STRING + " " + BLOCKS_STRING, createCauseBlocksPage());
		tabs.addTab(COMPLEXITY_STRING, createComplexityPage());

		add(tabs);
	}

	private JPanel createComplexityPage() {
		final JFreeChart histogram;
		final JFreeChart pieChart;
		final Map<String, Float> values;
		final ChartManager chartManager;
		
		values = metrics.calculateComplexityMetrics();

		// Create the Histogram
		histogram = createHistogram("Average"
				+ COMPLEXITY_STRING, BLOCKS_STRING, FREQUENCY_STRING, values);

		// Create the Pie Chart
		pieChart = createPieChart("Average" + COMPLEXITY_STRING, values);
		
		chartManager = new ChartManager(new ChartPanel(histogram),
				new ChartPanel(pieChart));
		
		return chartManager.getChartPanel();
	}

	/**
	 * Create the body for the cause blocks page. I.e. The number of times a
	 * specific cause block was used (active/inactive/always).
	 * 
	 * @return the page body.
	 */
	private JPanel createCauseBlocksPage() {
		final JFreeChart histogram;
		final JFreeChart pieChart;
		final Map<String, Integer> values;
		final ChartManager chartManager;

		values = metrics.calculateCauseBlockMetrics();
		
		// Create the Histogram
		histogram = createHistogram(CAUSES_STRING + " "
				+ BLOCKS_STRING, BLOCKS_STRING, FREQUENCY_STRING, values);

		// Create the Pie Chart
		pieChart = createPieChart(CAUSES_STRING + " " + BLOCKS_STRING, values);
		
		chartManager = new ChartManager(new ChartPanel(histogram),
				new ChartPanel(pieChart));

		return chartManager.getChartPanel();
	}

	/**
	 * Create the body for the favorites page. I.e. The number of times a
	 * specific cause was used.
	 * 
	 * @return the page body.
	 */
	private JTabbedPane createFavoriteCausesPage() {
		final JTabbedPane jTabbedPane = new JTabbedPane();

		final JFreeChart causesHistogram, causesPieChart;
		final JFreeChart effectsHistogram, effectsPieChart;
		final JFreeChart knowItsHistogram, knowItsPieChart;
		final JFreeChart askItsHistogram, askItsPieChart;
		final JFreeChart repeatsHistogram, repeatsPieChart;
		final JFreeChart delaysHistogram, delaysPieChart;
		
		final ChartManager causesChartManager;
		final ChartManager effectsChartManager;
		final ChartManager knowItsChartManager;
		final ChartManager askItsChartManager;
		final ChartManager repeatsChartManager;
		final ChartManager delaysChartManager;
		
		final Map<String, Integer> causesValues = metrics
				.calculateFavouriteCauses();
		final Map<String, Integer> effectsValues = metrics
				.calculateFavouriteEffects();
		final Map<String, Integer> knowItsValues = metrics
				.calculateFavouriteDescriptions();
		final Map<String, Integer> askItsValues = metrics
				.calculateFavouriteQuestions();
		final Map<String, Integer> repeatsValues = metrics
				.calculateFavouriteRepeats();
		final Map<String, Integer> delaysValues = metrics
				.calculateFavouriteDelays();

		// Create the histograms
		causesHistogram = createHistogram(FAVOURITE_STRING + " "
				+ CAUSES_STRING, CAUSES_STRING, FREQUENCY_STRING, causesValues);
		effectsHistogram = createHistogram(FAVOURITE_STRING + " "
				+ EFFECTS_STRING, EFFECTS_STRING, FREQUENCY_STRING, effectsValues);
		knowItsHistogram = createHistogram(FAVOURITE_STRING + " "
				+ KNOWITS_STRING, KNOWITS_STRING, FREQUENCY_STRING, knowItsValues);
		askItsHistogram = createHistogram(FAVOURITE_STRING + " "
				+ ASKIT_STRING, ASKIT_STRING, FREQUENCY_STRING, askItsValues);
		repeatsHistogram = createHistogram(FAVOURITE_STRING + " "
				+ REPEATS_STRING, REPEATS_STRING, FREQUENCY_STRING, repeatsValues);
		delaysHistogram = createHistogram(FAVOURITE_STRING + " "
				+ DELAY_STRING, DELAY_STRING, FREQUENCY_STRING, delaysValues);
		
		// Create the pie charts
		causesPieChart = createPieChart(FAVOURITE_STRING + " "
				+ CAUSES_STRING, causesValues);
		effectsPieChart = createPieChart(FAVOURITE_STRING + " "
				+ EFFECTS_STRING, effectsValues);
		knowItsPieChart = createPieChart(FAVOURITE_STRING + " "
				+ KNOWITS_STRING, knowItsValues);
		askItsPieChart = createPieChart(FAVOURITE_STRING + " "
				+ ASKIT_STRING, askItsValues);
		repeatsPieChart = createPieChart(FAVOURITE_STRING + " "
				+ REPEATS_STRING, repeatsValues);
		delaysPieChart = createPieChart(FAVOURITE_STRING + " "
				+ DELAY_STRING, delaysValues);
		
		// Create chart managers
		causesChartManager = new ChartManager(new ChartPanel(causesHistogram), 
				new ChartPanel(causesPieChart));
		effectsChartManager = new ChartManager(new ChartPanel(effectsHistogram), 
				new ChartPanel(effectsPieChart));
		knowItsChartManager = new ChartManager(new ChartPanel(knowItsHistogram), 
				new ChartPanel(knowItsPieChart));
		askItsChartManager = new ChartManager(new ChartPanel(askItsHistogram), 
				new ChartPanel(askItsPieChart));
		repeatsChartManager = new ChartManager(new ChartPanel(repeatsHistogram), 
				new ChartPanel(repeatsPieChart));
		delaysChartManager = new ChartManager(new ChartPanel(delaysHistogram), 
				new ChartPanel(delaysPieChart));
		
		jTabbedPane.addTab("Causes", causesChartManager.getChartPanel());
		jTabbedPane.addTab("Effects", effectsChartManager.getChartPanel());
		jTabbedPane.addTab("KnowIts", knowItsChartManager.getChartPanel());
		jTabbedPane.addTab("AskIts", askItsChartManager.getChartPanel());
		jTabbedPane.addTab("Repeats", repeatsChartManager.getChartPanel());
		jTabbedPane.addTab("Delays", delaysChartManager.getChartPanel());

		return jTabbedPane;
	}

	/**
	 * Create the body for the general metrics page (number of effects, causes,
	 * AskIts, etc.)
	 * 
	 * @return the page body.
	 */
	private JPanel createGeneralPage() {
		final Map<String, Integer> values;

		final JFreeChart histogram;
		final JFreeChart pieChart;

		final ChartManager chartManager;

		values = metrics.calculateGeneralMetrics();

		// Create the histogram
		histogram = createHistogram(GENERAL_STRING + " "
				+ METRICS_STRING, STORY_COMPONENT_STRING, FREQUENCY_STRING, values);

		// Create the pie chart
		pieChart = createPieChart(GENERAL_STRING + " " + METRICS_STRING, values);

		chartManager = new ChartManager(new ChartPanel(histogram),
				new ChartPanel(pieChart));

		return chartManager.getChartPanel();
	}

	/**
	 * Create the Histogram
	 * 
	 * @param title
	 * @param xAxisTitle
	 * @param yAxisTitle
	 * @param values
	 * @return The Histogram
	 */
	private JFreeChart createHistogram(String title, String xAxisTitle, 
			String yAxisTitle, Map<String, ? extends Number> values) {
		
		JFreeChart histogram;
		
		histogram = ChartFactory.createBarChart(title, xAxisTitle, yAxisTitle,
				processHistogramValues(values), PlotOrientation.VERTICAL,
				false, false, false);
		
		//histogram.getXYPlot().getRangeAxis().setMinorTickCount(1);
		
		return histogram;
	}
	
	/**
	 * Create the Pie Chart
	 * 
	 * @param title
	 * @param values
	 * @return the Pie Chart
	 */
	private JFreeChart createPieChart(String title, 
			Map<String, ? extends Number> values) {
		
		JFreeChart pieChart;
		
		pieChart = ChartFactory.createPieChart(title, 
				processPieValues(values), true, true, false);
		
		// Don't show categories with 0 values.
		PiePlot plot = (PiePlot) pieChart.getPlot();
		plot.setIgnoreZeroValues(true);
		
		return pieChart;
	}
	
	/**
	 * Process the Map values so that they can be used in the histogram dataset.
	 * 
	 * @param values
	 */
	private DefaultCategoryDataset processHistogramValues(
			Map<String, ? extends Number> values) {

		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for (Entry<String, ?> entry : values.entrySet()) {
			dataset.addValue((Number) entry.getValue(), "", entry.getKey());
		}

		return dataset;
	}

	/**
	 * Process the Map values so that they can be used in the pie chart dataset.
	 * 
	 * @param values
	 */
	private DefaultPieDataset processPieValues(
			Map<String, ? extends Number> values) {

		final DefaultPieDataset dataset = new DefaultPieDataset();

		for (Entry<String, ?> entry : values.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue());
		}

		return dataset;
	}
	
	/**
	 * Inner class used to set up and listen for changes between Histogram and
	 * Pie Chart radio button clicks.
	 * 
	 * @author jyuen
	 */
	private class ChartManager {
		final JPanel panel;
		final ButtonGroup buttonGroup;
		final JRadioButton histogramButton;
		final JRadioButton pieChartButton;

		final ChartPanel histogramChart;
		final ChartPanel pieChart;

		/**
		 * Initialize the radio buttons and panels for the Charts.
		 * 
		 * @param histogramChart
		 * @param pieChart
		 */
		public ChartManager(ChartPanel histogramChart, ChartPanel pieChart) {
			this.histogramChart = histogramChart;
			this.pieChart = pieChart;

			this.panel = new JPanel();

			this.histogramButton = new JRadioButton("Histogram");
			this.pieChartButton = new JRadioButton("Pie Chart");

			this.buttonGroup = new ButtonGroup();

			setupPanel();
			initializeButtonGroup();
		}

		/**
		 * Return the panel containing the charts and radio buttons.
		 * 
		 * @return
		 */
		public JPanel getChartPanel() {
			return this.panel;
		}

		private void setupPanel() {
			this.panel.add(this.histogramChart);

			this.panel.add(this.histogramButton);
			this.panel.add(this.pieChartButton);
		}

		private void initializeButtonGroup() {
			this.buttonGroup.add(this.histogramButton);
			this.buttonGroup.add(this.pieChartButton);

			histogramButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ChartManager.this.repaintPanel(histogramChart);	
				}
			});

			pieChartButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ChartManager.this.repaintPanel(pieChart);
				}
			});
		}
		
		private void repaintPanel(ChartPanel chart) {
			panel.removeAll();
			panel.add(chart);
			panel.add(histogramButton);
			panel.add(pieChartButton);
			panel.repaint();	
		}
	}
}
