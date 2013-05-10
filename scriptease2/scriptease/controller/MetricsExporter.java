package scriptease.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.controller.io.FileIO;
import scriptease.gui.WindowFactory;

/**
 * Responsible for exporting metric data to .cvs file so that it can be used in
 * Excel etc.
 * 
 * @author jyuen
 */
public class MetricsExporter {

	// Singleton
	private static MetricsExporter instance = null;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static MetricsExporter getInstance() {
		if (instance == null) {
			instance = new MetricsExporter();
		}

		return MetricsExporter.instance;
	}

	/**
	 * Creates a new file filter for export
	 * 
	 * @return
	 */
	private javax.swing.filechooser.FileFilter createMetricsFilter() {
		final javax.swing.filechooser.FileFilter filter;

		filter = new FileNameExtensionFilter(".csv", ".csv");

		return filter;
	}

	/**
	 * Export the .csv file to the user's requested directory.
	 */
	public void exportMetrics() {
		final MetricAnalyzer metricAnalyzer;
		final File metricsFile;
		final Collection<ArrayList<String>> data;

		final String STORY_COMPONENTS = "Story Components";
		final String FREQUENCY = "Frequency";
		final String AVERAGE = "Average";
		final String COMPLEXITY = "Complexity";
		final String CAUSE_BLOCK = "Cause Block";
		final String FAVOURITE = "Favourite";
		final String CAUSES = "Causes";
		final String EFFECTS = "Effects";
		final String DESCRIPTIONS = "Descriptions";
		final String QUESTIONS = "Questions";
		final String REPEATS = "Repeats";
		final String DELAYS = "Delays";
		final String STORY_POINT_COMPLEXITY = "Story Point Complexity";

		metricAnalyzer = MetricAnalyzer.getInstance();

		data = new ArrayList<ArrayList<String>>();

		metricsFile = WindowFactory.getInstance().showFileChooser("Save",
				"story_metrics.csv", createMetricsFilter());

		processDataToCSV(STORY_COMPONENTS, FREQUENCY,
				metricAnalyzer.getNumStoryComponents(), data);

		processDataToCSV(COMPLEXITY, AVERAGE + " " + FREQUENCY,
				metricAnalyzer.getStoryComponentComplexity(), data);

		processDataToCSV(CAUSE_BLOCK, FREQUENCY,
				metricAnalyzer.getCauseBlockMetrics(), data);

		processDataToCSV(FAVOURITE + " " + CAUSES, FREQUENCY,
				metricAnalyzer.getFavouriteCauses(), data);

		processDataToCSV(FAVOURITE + " " + EFFECTS, FREQUENCY,
				metricAnalyzer.getFavouriteEffects(), data);

		processDataToCSV(FAVOURITE + " " + DESCRIPTIONS, FREQUENCY,
				metricAnalyzer.getFavouriteDescriptions(), data);

		processDataToCSV(FAVOURITE + " " + QUESTIONS, FREQUENCY,
				metricAnalyzer.getFavouriteQuestions(), data);

		processDataToCSV(FAVOURITE + " " + REPEATS, FREQUENCY,
				metricAnalyzer.getFavouriteRepeats(), data);

		processDataToCSV(FAVOURITE + " " + DELAYS, FREQUENCY,
				metricAnalyzer.getFavouriteDelays(), data);

		processDataToCSV(STORY_POINT_COMPLEXITY, FREQUENCY,
				metricAnalyzer.getStoryPointComplexity(), data);

		FileIO.getInstance().saveCSV(data, metricsFile);
	}

	private void processDataToCSV(String xComponent, String yComponent,
			Map<String, ? extends Number> values,
			final Collection<ArrayList<String>> data) {

		ArrayList<String> tempRow = new ArrayList<String>();

		tempRow.add(xComponent);
		tempRow.add(yComponent);
		data.add(tempRow);

		for (Entry<String, ?> entry : values.entrySet()) {
			tempRow = new ArrayList<String>();
			tempRow.add(entry.getKey());

			if (entry.getValue() instanceof Float)
				tempRow.add(Float.toString((Float) entry.getValue()));
			else if (entry.getValue() instanceof Integer)
				tempRow.add(Integer.toString((Integer) entry.getValue()));

			data.add(tempRow);
		}

		tempRow = new ArrayList<String>();
		tempRow.add("");
		data.add(tempRow);
	}
}
