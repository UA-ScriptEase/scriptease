package scriptease.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.controller.io.FileIO;
import scriptease.gui.WindowFactory;

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
		final File metricsFile;
		final Collection<ArrayList<String>> data;

		data = new ArrayList<ArrayList<String>>();

		metricsFile = WindowFactory.getInstance().showFileChooser("Save",
				"story_metrics.csv", createMetricsFilter());

//		processDataToCSV(GENERAL_STRING, FREQUENCY_STRING, this.generalValues,
//				data);
//		processDataToCSV(COMPLEXITY_STRING, FREQUENCY_STRING,
//				this.complexityValues, data);
//		processDataToCSV(CAUSES_STRING + " " + BLOCKS_STRING, FREQUENCY_STRING,
//				this.causeBlockValues, data);
//		processDataToCSV(FAVOURITE_STRING + " " + CAUSES_STRING,
//				FREQUENCY_STRING, this.causesValues, data);
//		processDataToCSV(FAVOURITE_STRING + " " + KNOWITS_STRING,
//				FREQUENCY_STRING, this.knowItsValues, data);
//		processDataToCSV(FAVOURITE_STRING + " " + ASKIT_STRING,
//				FREQUENCY_STRING, this.askItsValues, data);
//		processDataToCSV(FAVOURITE_STRING + " " + REPEATS_STRING,
//				FREQUENCY_STRING, this.repeatsValues, data);
//		processDataToCSV(FAVOURITE_STRING + " " + DELAY_STRING,
//				FREQUENCY_STRING, this.delaysValues, data);

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
	}

}
