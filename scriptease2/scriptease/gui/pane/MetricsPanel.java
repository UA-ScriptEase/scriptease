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

import scriptease.gui.action.metrics.StoryComponentMetricsAction;

@SuppressWarnings("serial")
/**
 * MetricsPanel represents the JPanel used to display story component
 * metrics. Each summary is displayed in separate tabs with their
 * respective histogram.
 * 
 * @author jyuen
 */
public class MetricsPanel extends JPanel {

	private final StoryComponentMetricsAction metrics;

	/**
	 * Creates a new MetricsPanel with the default tabs and histograms.
	 */
	public MetricsPanel() {
		final JTabbedPane tabs = new JTabbedPane();

		this.metrics = StoryComponentMetricsAction.getInstance();

		tabs.addTab("General", createGeneralPage());
		tabs.addTab("Favorites", createFavoriteCausesPage());

		add(tabs);
	}

	/**
	 * Create the body for the favorites page. I.e. The number of times a
	 * specific cause was used.
	 * 
	 * @return the page body.
	 */
	private JTabbedPane createFavoriteCausesPage() {
		final String favouriteString = "Favourite";
		final String frequencyString = "Frequency";
		final String causesString = "Causes";
		final String effectsString = "Effects";
		final String knowItsString = "Descriptions";
		final String askItsString = "Questions";
		final String repeatsString = "Repeats";
		final String delaysString = "Delays";

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
				.calculateFavouriteKnowIts();
		final Map<String, Integer> askItsValues = metrics
				.calculateFavouriteAskIts();
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
		causesChart = ChartFactory.createBarChart(favouriteString + " "
				+ causesString, causesString, frequencyString, causesDataset,
				PlotOrientation.VERTICAL, false, false, false);
		effectsChart = ChartFactory.createBarChart(favouriteString + " "
				+ effectsString, effectsString, frequencyString,
				effectsDataset, PlotOrientation.VERTICAL, false, false, false);
		knowItsChart = ChartFactory.createBarChart(favouriteString + " "
				+ knowItsString, knowItsString, frequencyString,
				knowItsDataset, PlotOrientation.VERTICAL, false, false, false);
		askItsChart = ChartFactory.createBarChart(favouriteString + " "
				+ askItsString, askItsString, frequencyString, askItsDataset,
				PlotOrientation.VERTICAL, false, false, false);
		repeatsChart = ChartFactory.createBarChart(favouriteString + " "
				+ favouriteString, repeatsString, frequencyString,
				repeatsDataset, PlotOrientation.VERTICAL, false, false, false);
		delaysChart = ChartFactory.createBarChart(favouriteString + " "
				+ delaysString, delaysString, frequencyString, delaysDataset,
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
		chart = ChartFactory.createBarChart("Metrics Chart", "Story Component",
				"Frequency", generalDataset, PlotOrientation.VERTICAL, false,
				false, false);

		histogram = new ChartPanel(chart);

		return histogram;
	}

	/**
	 * Process the Map values so that they can be used in the dataset.
	 * 
	 * @param values
	 * @param dataset
	 */
	private void processValues(Map<String, Integer> values,
			DefaultCategoryDataset dataset) {

		for (Entry<String, Integer> entry : values.entrySet()) {
			dataset.addValue(entry.getValue(), "", entry.getKey());
		}
	}
}
