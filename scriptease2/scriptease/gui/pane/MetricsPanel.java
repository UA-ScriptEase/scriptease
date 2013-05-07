package scriptease.gui.pane;

import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import scriptease.controller.MetricAnalyzer;

@SuppressWarnings("serial")
/**
 * MetricsPanel represents the JPanel used to display story component
 * metrics. Each summary is displayed in separate tabs with their
 * respective histogram.
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
		final JFreeChart complexityChart;
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		final Map<String, Float> values = metrics.calculateComplexityMetrics();

		processValues(values, dataset);

		complexityChart = ChartFactory.createBarChart("Average"
				+ COMPLEXITY_STRING, BLOCKS_STRING, FREQUENCY_STRING, dataset,
				PlotOrientation.VERTICAL, false, false, false);

		return new ChartPanel(complexityChart);
	}

	/**
	 * Create the body for the cause blocks page. I.e. The number of times a
	 * specific cause block was used (active/inactive/always).
	 * 
	 * @return the page body.
	 */
	private JPanel createCauseBlocksPage() {
		final JFreeChart causesBlockChart;
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		final Map<String, Integer> values = metrics
				.calculateCauseBlockMetrics();

		processValues(values, dataset);

		causesBlockChart = ChartFactory.createBarChart(CAUSES_STRING + " "
				+ BLOCKS_STRING, BLOCKS_STRING, FREQUENCY_STRING, dataset,
				PlotOrientation.VERTICAL, false, false, false);

		return new ChartPanel(causesBlockChart);
	}

	/**
	 * Create the body for the favorites page. I.e. The number of times a
	 * specific cause was used.
	 * 
	 * @return the page body.
	 */
	private JTabbedPane createFavoriteCausesPage() {
		final JTabbedPane jTabbedPane = new JTabbedPane();

		final JFreeChart causesChart;
		final JFreeChart effectsChart;
		final JFreeChart knowItsChart;
		final JFreeChart askItsChart;
		final JFreeChart repeatsChart;
		final JFreeChart delaysChart;

		final DefaultCategoryDataset causesDataset = new DefaultCategoryDataset();
		final DefaultCategoryDataset effectsDataset = new DefaultCategoryDataset();
		final DefaultCategoryDataset knowItsDataset = new DefaultCategoryDataset();
		final DefaultCategoryDataset askItsDataset = new DefaultCategoryDataset();
		final DefaultCategoryDataset repeatsDataset = new DefaultCategoryDataset();
		final DefaultCategoryDataset delaysDataset = new DefaultCategoryDataset();

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

		processValues(causesValues, causesDataset);
		processValues(effectsValues, effectsDataset);
		processValues(knowItsValues, knowItsDataset);
		processValues(askItsValues, askItsDataset);
		processValues(repeatsValues, repeatsDataset);
		processValues(delaysValues, delaysDataset);

		// Create the histograms
		causesChart = ChartFactory.createBarChart(FAVOURITE_STRING + " "
				+ CAUSES_STRING, CAUSES_STRING, FREQUENCY_STRING,
				causesDataset, PlotOrientation.VERTICAL, false, false, false);

		effectsChart = ChartFactory.createBarChart(FAVOURITE_STRING + " "
				+ EFFECTS_STRING, EFFECTS_STRING, FREQUENCY_STRING,
				effectsDataset, PlotOrientation.VERTICAL, false, false, false);

		knowItsChart = ChartFactory.createBarChart(FAVOURITE_STRING + " "
				+ KNOWITS_STRING, KNOWITS_STRING, FREQUENCY_STRING,
				knowItsDataset, PlotOrientation.VERTICAL, false, false, false);

		askItsChart = ChartFactory.createBarChart(FAVOURITE_STRING + " "
				+ ASKIT_STRING, ASKIT_STRING, FREQUENCY_STRING, askItsDataset,
				PlotOrientation.VERTICAL, false, false, false);

		repeatsChart = ChartFactory.createBarChart(FAVOURITE_STRING + " "
				+ FAVOURITE_STRING, REPEATS_STRING, FREQUENCY_STRING,
				repeatsDataset, PlotOrientation.VERTICAL, false, false, false);

		delaysChart = ChartFactory.createBarChart(FAVOURITE_STRING + " "
				+ DELAY_STRING, DELAY_STRING, FREQUENCY_STRING, delaysDataset,
				PlotOrientation.VERTICAL, false, false, false);

		jTabbedPane.addTab("Causes", new ChartPanel(causesChart));
		jTabbedPane.addTab("Effects", new ChartPanel(effectsChart));
		jTabbedPane.addTab("KnowIts", new ChartPanel(knowItsChart));
		jTabbedPane.addTab("AskIts", new ChartPanel(askItsChart));
		jTabbedPane.addTab("Repeats", new ChartPanel(repeatsChart));
		jTabbedPane.addTab("Delays", new ChartPanel(delaysChart));

		return jTabbedPane;
	}

	/**
	 * Create the body for the general metrics page (number of effects, causes,
	 * AskIts, etc.)
	 * 
	 * @return the page body.
	 */
	private JPanel createGeneralPage() {
		final JPanel histogram;
		final JFreeChart chart;
		final DefaultCategoryDataset generalDataset;
		final Map<String, Integer> values;

		generalDataset = new DefaultCategoryDataset();
		values = metrics.calculateGeneralMetrics();

		processValues(values, generalDataset);

		// Create the histogram
		chart = ChartFactory.createBarChart(GENERAL_STRING + " "
				+ METRICS_STRING, STORY_COMPONENT_STRING, FREQUENCY_STRING,
				generalDataset, PlotOrientation.VERTICAL, false, false, false);

		histogram = new ChartPanel(chart);

		return histogram;
	}

	/**
	 * Process the Map values so that they can be used in the dataset.
	 * 
	 * @param values
	 * @param dataset
	 */
	private void processValues(Map<String, ? extends Number> values,
			DefaultCategoryDataset dataset) {

		for (Entry<String, ?> entry : values.entrySet()) {
			dataset.addValue((Number) entry.getValue(), "", entry.getKey());
		}
	}

}
