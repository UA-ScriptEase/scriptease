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

	/**
	 * Creates a new MetricsPanel with the default tabs and histograms.
	 */
	public MetricsPanel() {
		final JTabbedPane tabs = new JTabbedPane();

		tabs.addTab("General", createGeneralPage());

		add(tabs);
	}

	/**
	 * Create the body for the general metrics page (number of effects, causes,
	 * AskIts, etc.)
	 */
	private JPanel createGeneralPage() {
		JPanel histogram;
		JFreeChart chart;
		DefaultCategoryDataset generalDataset;
		StoryComponentMetricsAction metrics;
		Map<String, Integer> values;

		generalDataset = new DefaultCategoryDataset();
		metrics = StoryComponentMetricsAction.getInstance();
		values = metrics.calculateMetrics();
		
		processValues(values, generalDataset);

		// Create the histogram
		chart = ChartFactory.createBarChart("Metrics Chart",
				"Story Component", "Frequency", generalDataset,
				PlotOrientation.VERTICAL, false, false, false);

		histogram = new ChartPanel(chart);

		return histogram;
	}

	/**
	 * Process the Map values so that they can be used in the dataset.
	 * 
	 * @param values
	 */
	public void processValues(Map<String, Integer> values,
			DefaultCategoryDataset dataset) {

		for (Entry<String, Integer> entry : values.entrySet()) {
			dataset.addValue(entry.getValue(), "", entry.getKey());
		}
	}
}
