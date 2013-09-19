package scriptease.gui.pane;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import scriptease.controller.StoryMetrics;
import scriptease.gui.WindowFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.ScriptEaseKeywords;

@SuppressWarnings("serial")
/**
 * MetricsPanel represents the JPanel used to display story component
 * metrics. Each summary is displayed in separate tabs with their
 * respective histogram and pie charts.
 * 
 * @author jyuen
 */
public class MetricsPanel extends JPanel {

	private final StoryMetrics metrics;

	/**
	 * Creates a new MetricsPanel with the default tabs and histograms.
	 */
	public MetricsPanel(final StoryMetrics metrics) {
		final JTabbedPane tabs = new JTabbedPane();

		final String EXPORT = "Export All as CSV";

		final int WIDTH = 800;
		final int HEIGHT = 600;

		final JPanel buttonsPanel;
		final JButton exportButton = new JButton(EXPORT);

		this.metrics = metrics;

		tabs.addTab(ScriptEaseKeywords.COMPONENT_COUNT,
				createNumComponentsPage());
		tabs.addTab(ScriptEaseKeywords.FAVOURITE, createFavoriteCausesPage());
		tabs.addTab(ScriptEaseKeywords.STORY_COMPONENT + " "
				+ ScriptEaseKeywords.COMPLEXITY,
				createStoryComponentComplexityPage());
		tabs.addTab(ScriptEaseKeywords.STORY_POINT + " "
				+ ScriptEaseKeywords.COMPLEXITY,
				createStoryPointComplexityPage());

		tabs.setBorder(BorderFactory.createEmptyBorder());

		add(tabs);

		buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		buttonsPanel.add(exportButton);

		this.add(buttonsPanel);
		this.setSize(WIDTH, HEIGHT);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		exportButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final File metricsFile;

				metricsFile = WindowFactory.getInstance().showFileChooser(
						"Save", "story_metrics.csv",
						new FileNameExtensionFilter("csv", "csv"));

				metrics.exportMetrics(metricsFile);
			}
		});
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

		values = metrics.calculateStoryPointComplexity();

		// Create the Histogram
		histogram = this.createHistogram(ScriptEaseKeywords.STORY_POINT + " "
				+ ScriptEaseKeywords.COMPLEXITY, "",
				ScriptEaseKeywords.FREQUENCY, values, true);

		// Create the Pie Chart
		pieChart = this.createPieChart(ScriptEaseKeywords.STORY_POINT + " "
				+ ScriptEaseKeywords.COMPLEXITY, values);

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
		histogram = createHistogram("Average " + ScriptEaseKeywords.COMPLEXITY,
				"", ScriptEaseKeywords.FREQUENCY, complexityValues, false);

		// Create the Pie Chart
		pieChart = createPieChart("Average " + ScriptEaseKeywords.COMPLEXITY,
				complexityValues);

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
		causesHistogram = createHistogram(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.CAUSES, ScriptEaseKeywords.CAUSES,
				ScriptEaseKeywords.FREQUENCY, causesValues, true);
		effectsHistogram = createHistogram(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.EFFECTS, ScriptEaseKeywords.EFFECTS,
				ScriptEaseKeywords.FREQUENCY, effectsValues, true);
		knowItsHistogram = createHistogram(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.DESCRIPTIONS,
				ScriptEaseKeywords.DESCRIPTIONS, ScriptEaseKeywords.FREQUENCY,
				knowItsValues, true);
		askItsHistogram = createHistogram(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.QUESTIONS, ScriptEaseKeywords.QUESTIONS,
				ScriptEaseKeywords.FREQUENCY, askItsValues, true);
		repeatsHistogram = createHistogram(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.REPEATS, ScriptEaseKeywords.REPEATS,
				ScriptEaseKeywords.FREQUENCY, repeatsValues, true);
		delaysHistogram = createHistogram(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.DELAYS, ScriptEaseKeywords.DELAYS,
				ScriptEaseKeywords.FREQUENCY, delaysValues, true);

		// Create the pie charts
		causesPieChart = createPieChart(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.CAUSES, causesValues);
		effectsPieChart = createPieChart(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.EFFECTS, effectsValues);
		knowItsPieChart = createPieChart(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.DESCRIPTIONS, knowItsValues);
		askItsPieChart = createPieChart(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.QUESTIONS, askItsValues);
		repeatsPieChart = createPieChart(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.REPEATS, repeatsValues);
		delaysPieChart = createPieChart(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.DELAYS, delaysValues);

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

		jTabbedPane.addTab(ScriptEaseKeywords.CAUSES,
				causesChartManager.getChartPanel());
		jTabbedPane.addTab(ScriptEaseKeywords.EFFECTS,
				effectsChartManager.getChartPanel());
		jTabbedPane.addTab(ScriptEaseKeywords.DESCRIPTIONS,
				knowItsChartManager.getChartPanel());
		jTabbedPane.addTab(ScriptEaseKeywords.QUESTIONS,
				askItsChartManager.getChartPanel());
		jTabbedPane.addTab(ScriptEaseKeywords.REPEATS,
				repeatsChartManager.getChartPanel());
		jTabbedPane.addTab(ScriptEaseKeywords.DELAYS,
				delaysChartManager.getChartPanel());

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

		generalValues = metrics.getNumberComponents();

		// Create the histogram
		histogram = createHistogram(ScriptEaseKeywords.COMPONENT_COUNT,
				ScriptEaseKeywords.STORY_COMPONENT,
				ScriptEaseKeywords.FREQUENCY, generalValues, true);

		// Create the pie chart
		pieChart = createPieChart(ScriptEaseKeywords.COMPONENT_COUNT,
				generalValues);

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
	@SuppressWarnings("unchecked")
	private ChartPanel createHistogram(String title, String xAxisTitle,
			String yAxisTitle, Map<String, ? extends Number> values,
			boolean yAxisIntegerScale) {

		final ChartPanel histogramPanel;
		final JFreeChart histogram;
		final CategoryPlot categoryPlot;
		final CategoryAxis domainAxis;
		final List<String> categories;

		histogram = ChartFactory.createBarChart(title, xAxisTitle, yAxisTitle,
				processHistogramValues(values), PlotOrientation.VERTICAL,
				false, false, false);

		categoryPlot = histogram.getCategoryPlot();
		domainAxis = categoryPlot.getDomainAxis();
		categories = categoryPlot.getCategories();

		// Set the bar colors
		categoryPlot.getRenderer().setSeriesPaint(0,
				ScriptEaseUI.COLOUR_GAME_OBJECT);

		// Add tooltips for each category so that by hovering over it, you are
		// able to read its entirety.
		for (String category : categories) {
			domainAxis.addCategoryLabelToolTip(category, category);
		}

		// Allow only integers on y-axis if requested.
		if (yAxisIntegerScale) {
			final ValueAxis rangeAxis = categoryPlot.getRangeAxis();
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

		final PiePlot plot;

		pieChart = ChartFactory.createPieChart(title, processPieValues(values),
				true, true, false);

		// Don't show categories with 0 values.
		plot = (PiePlot) pieChart.getPlot();
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
		private final JSplitPane splitPanel;

		private final JPanel buttonPanel;

		private final ButtonGroup buttonGroup;
		private final JRadioButton histogramButton;
		private final JRadioButton pieChartButton;

		private final ChartPanel histogramChart;
		private final ChartPanel pieChart;

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

			this.setupPanel();
			this.initializeButtonGroup();
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
