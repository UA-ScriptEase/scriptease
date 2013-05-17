package scriptease.gui.pane;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import scriptease.controller.MetricsAnalyzer;

@SuppressWarnings("serial")
/**
 * MetricsPanel represents the JPanel used to display story component
 * metrics. Each summary is displayed in separate tabs with their
 * respective histogram and pie charts.
 * 
 * @author jyuen
 */
public class MetricsPanel extends JPanel {

	private final MetricsAnalyzer metrics;

	private static final String STORY_COMPONENT = "Story Component";
	private static final String STORY_POINT = "Story Point";
	private static final String COMPLEXITY = "Complexity";
	private static final String COMPONENT_COUNT = "Component Count";
	private static final String FAVOURITE = "Favourite";
	private static final String FREQUENCY = "Frequency";
	private static final String CAUSES = "Causes";
	private static final String EFFECTS = "Effects";
	private static final String DESCRIPTIONS = "Descriptions";
	private static final String QUESTIONS = "Questions";
	private static final String REPEATS = "Repeats";
	private static final String DELAYS = "Delays";
	private static final String CAUSE_BLOCKS = "Cause Blocks";

	/**
	 * Creates a new MetricsPanel with the default tabs and histograms.
	 */
	public MetricsPanel() {
		final JTabbedPane tabs = new JTabbedPane();

		this.metrics = MetricsAnalyzer.getInstance();

		tabs.addTab(COMPONENT_COUNT, createNumComponentsPage());
		tabs.addTab(FAVOURITE, createFavoriteCausesPage());
		tabs.addTab(CAUSE_BLOCKS, createCauseBlocksPage());
		tabs.addTab(STORY_COMPONENT + " " + COMPLEXITY,
				createStoryComponentComplexityPage());
		tabs.addTab(STORY_POINT + " " + COMPLEXITY,
				createStoryPointComplexityPage());

		tabs.setBorder(BorderFactory.createEmptyBorder());

		add(tabs);
	}

	/**
	 * Create the body for the story point complexity related page. I.e. The
	 * longest branch, number of child story points ...
	 * 
	 * @return the page body.
	 */
	private JSplitPane createStoryPointComplexityPage() {
		final Map<String, Integer> values;

		final ChartPanel histogram;
		final ChartPanel pieChart;
		final ChartManager chartManager;

		values = metrics.getStoryPointComplexity();

		// Create the Histogram
		histogram = createHistogram("StoryPoint Complexity", "", FREQUENCY,
				values, true);

		// Create the Pie Chart
		pieChart = createPieChart("StoryPoint Complexity", values);

		chartManager = new ChartManager(histogram, pieChart);

		return chartManager.getChartPanel();
	}

	/**
	 * Create the body for the story component complexity page I.e. The average
	 * number of effects in all causes. The Average number of causes in all
	 * story points ...
	 * 
	 * @return the page body.
	 */
	private JSplitPane createStoryComponentComplexityPage() {
		final Map<String, Float> complexityValues;

		final ChartPanel histogram;
		final ChartPanel pieChart;
		final ChartManager chartManager;

		complexityValues = metrics.getStoryComponentComplexity();

		// Create the Histogram
		histogram = createHistogram("Average " + COMPLEXITY, "", FREQUENCY,
				complexityValues, false);

		// Create the Pie Chart
		pieChart = createPieChart("Average " + COMPLEXITY, complexityValues);

		chartManager = new ChartManager(histogram, pieChart);

		return chartManager.getChartPanel();
	}

	/**
	 * Create the body for the cause blocks page. I.e. The number of times a
	 * specific cause block was used (active/inactive/always).
	 * 
	 * @return the page body.
	 */
	private JSplitPane createCauseBlocksPage() {
		final Map<String, Integer> causeBlockValues;

		final ChartPanel histogram;
		final ChartPanel pieChart;
		final ChartManager chartManager;

		causeBlockValues = metrics.getCauseBlockMetrics();

		// Create the Histogram
		histogram = createHistogram(CAUSE_BLOCKS, CAUSE_BLOCKS, FREQUENCY,
				causeBlockValues, true);

		// Create the Pie Chart
		pieChart = createPieChart(CAUSE_BLOCKS, causeBlockValues);

		chartManager = new ChartManager(histogram, pieChart);

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

		final ChartPanel causesHistogram, causesPieChart;
		final ChartPanel effectsHistogram, effectsPieChart;
		final ChartPanel knowItsHistogram, knowItsPieChart;
		final ChartPanel askItsHistogram, askItsPieChart;
		final ChartPanel repeatsHistogram, repeatsPieChart;
		final ChartPanel delaysHistogram, delaysPieChart;

		final ChartManager causesChartManager;
		final ChartManager effectsChartManager;
		final ChartManager knowItsChartManager;
		final ChartManager askItsChartManager;
		final ChartManager repeatsChartManager;
		final ChartManager delaysChartManager;

		final Map<String, Integer> causesValues;
		final Map<String, Integer> effectsValues;
		final Map<String, Integer> knowItsValues;
		final Map<String, Integer> askItsValues;
		final Map<String, Integer> repeatsValues;
		final Map<String, Integer> delaysValues;

		causesValues = metrics.getFavouriteCauses();
		effectsValues = metrics.getFavouriteEffects();
		knowItsValues = metrics.getFavouriteDescriptions();
		askItsValues = metrics.getFavouriteQuestions();
		repeatsValues = metrics.getFavouriteRepeats();
		delaysValues = metrics.getFavouriteDelays();

		// Create the histograms
		causesHistogram = createHistogram(FAVOURITE + " " + CAUSES, CAUSES,
				FREQUENCY, causesValues, true);
		effectsHistogram = createHistogram(FAVOURITE + " " + EFFECTS, EFFECTS,
				FREQUENCY, effectsValues, true);
		knowItsHistogram = createHistogram(FAVOURITE + " " + DESCRIPTIONS,
				DESCRIPTIONS, FREQUENCY, knowItsValues, true);
		askItsHistogram = createHistogram(FAVOURITE + " " + QUESTIONS,
				QUESTIONS, FREQUENCY, askItsValues, true);
		repeatsHistogram = createHistogram(FAVOURITE + " " + REPEATS, REPEATS,
				FREQUENCY, repeatsValues, true);
		delaysHistogram = createHistogram(FAVOURITE + " " + DELAYS, DELAYS,
				FREQUENCY, delaysValues, true);

		// Create the pie charts
		causesPieChart = createPieChart(FAVOURITE + " " + CAUSES, causesValues);
		effectsPieChart = createPieChart(FAVOURITE + " " + EFFECTS,
				effectsValues);
		knowItsPieChart = createPieChart(FAVOURITE + " " + DESCRIPTIONS,
				knowItsValues);
		askItsPieChart = createPieChart(FAVOURITE + " " + QUESTIONS,
				askItsValues);
		repeatsPieChart = createPieChart(FAVOURITE + " " + REPEATS,
				repeatsValues);
		delaysPieChart = createPieChart(FAVOURITE + " " + DELAYS, delaysValues);

		// Create chart managers
		causesChartManager = new ChartManager(causesHistogram, causesPieChart);
		effectsChartManager = new ChartManager(effectsHistogram,
				effectsPieChart);
		knowItsChartManager = new ChartManager(knowItsHistogram,
				knowItsPieChart);
		askItsChartManager = new ChartManager(askItsHistogram, askItsPieChart);
		repeatsChartManager = new ChartManager(repeatsHistogram,
				repeatsPieChart);
		delaysChartManager = new ChartManager(delaysHistogram, delaysPieChart);

		jTabbedPane.addTab("Causes", causesChartManager.getChartPanel());
		jTabbedPane.addTab("Effects", effectsChartManager.getChartPanel());
		jTabbedPane.addTab("KnowIts", knowItsChartManager.getChartPanel());
		jTabbedPane.addTab("AskIts", askItsChartManager.getChartPanel());
		jTabbedPane.addTab("Repeats", repeatsChartManager.getChartPanel());
		jTabbedPane.addTab("Delays", delaysChartManager.getChartPanel());

		jTabbedPane.setBorder(BorderFactory.createEmptyBorder());

		return jTabbedPane;
	}

	/**
	 * Create the body for the general metrics page (number of effects, causes,
	 * AskIts, etc.)
	 * 
	 * @return the page body.
	 */
	private JSplitPane createNumComponentsPage() {
		final Map<String, Integer> generalValues;
		final ChartPanel histogram;
		final ChartPanel pieChart;
		final ChartManager chartManager;

		generalValues = metrics.getNumStoryComponents();

		// Create the histogram
		histogram = createHistogram(COMPONENT_COUNT, STORY_COMPONENT,
				FREQUENCY, generalValues, true);

		// Create the pie chart
		pieChart = createPieChart(COMPONENT_COUNT, generalValues);

		chartManager = new ChartManager(histogram, pieChart);

		return chartManager.getChartPanel();
	}

	/**
	 * Create the Histogram
	 * 
	 * @param title
	 * @param xAxisTitle
	 * @param yAxisTitle
	 * @param values
	 * @param yAxisIntegerScale
	 *            Set to true if this histogram requires integer only scaling.
	 * @return The Histogram
	 */
	private ChartPanel createHistogram(String title, String xAxisTitle,
			String yAxisTitle, Map<String, ? extends Number> values,
			boolean yAxisIntegerScale) {

		final ChartPanel histogramPanel;
		final JFreeChart histogram;

		histogram = ChartFactory.createBarChart(title, xAxisTitle, yAxisTitle,
				processHistogramValues(values), PlotOrientation.VERTICAL,
				false, false, false);

		// Allow only integers on y-axis if requested.
		if (yAxisIntegerScale) {
			NumberAxis rangeAxis = (NumberAxis) histogram.getCategoryPlot()
					.getRangeAxis();
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		
		histogramPanel = new ChartPanel(histogram);
		histogramPanel.setPopupMenu(null);
		histogramPanel.setDomainZoomable(false);
		histogramPanel.setRangeZoomable(false);

		return histogramPanel;
	}

	/**
	 * Create the Pie Chart
	 * 
	 * @param title
	 * @param values
	 * @return the Pie Chart
	 */
	private ChartPanel createPieChart(String title,
			Map<String, ? extends Number> values) {

		final ChartPanel piePanel;
		final JFreeChart pieChart;

		pieChart = ChartFactory.createPieChart(title, processPieValues(values),
				true, true, false);

		// Don't show categories with 0 values.
		PiePlot plot = (PiePlot) pieChart.getPlot();
		plot.setIgnoreZeroValues(true);

		piePanel = new ChartPanel(pieChart);
		piePanel.setPopupMenu(null);

		return piePanel;
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
		final JSplitPane splitPanel;

		final JPanel buttonPanel;

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

			this.splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			this.buttonPanel = new JPanel();

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
		public JSplitPane getChartPanel() {
			return this.splitPanel;
		}

		private void setupPanel() {
			this.buttonPanel.add(this.histogramButton);
			this.buttonPanel.add(this.pieChartButton);

			this.histogramButton.setBackground(Color.WHITE);
			this.pieChartButton.setBackground(Color.WHITE);
			this.buttonPanel.setBackground(Color.WHITE);

			this.splitPanel.setTopComponent(this.histogramChart);
			this.splitPanel.setBottomComponent(this.buttonPanel);
			this.splitPanel.setEnabled(false);
			this.splitPanel.setDividerLocation(0.95);
			this.splitPanel.setDividerSize(0);
			this.splitPanel.setBorder(BorderFactory.createEmptyBorder());
		}

		private void initializeButtonGroup() {
			this.buttonGroup.add(this.histogramButton);
			this.buttonGroup.add(this.pieChartButton);

			this.histogramButton.setSelected(true);

			this.histogramButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ChartManager.this.repaintPanel(histogramChart);
				}
			});

			this.pieChartButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ChartManager.this.repaintPanel(pieChart);
				}
			});
		}

		private void repaintPanel(ChartPanel chart) {
			this.splitPanel.setTopComponent(chart);
			this.splitPanel.repaint();
		}
	}
}
