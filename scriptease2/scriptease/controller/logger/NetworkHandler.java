package scriptease.controller.logger;

import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import scriptease.ScriptEase;
import scriptease.ScriptEase.ConfigurationKeys;
import scriptease.gui.ExceptionDialog;
import scriptease.gui.WindowFactory;

/**
 * NetworkHandler provides report handling for ScriptEase. It connects to the
 * server and then sends each log as needed using the publish method.
 * 
 * @author mfchurch
 * 
 */
public class NetworkHandler extends Handler {
	private static final int HTML_SUCCESS = 200;

	private HttpClient client;
	private HttpPost post;
	private LogRecord buffered;

	private static final NetworkHandler instance = new NetworkHandler();

	/**
	 * Gets the sole instance of NetworkHandler as per the Singleton design
	 * pattern.
	 * 
	 * @return The sole instance of ScriptEase.
	 */
	public static NetworkHandler getInstance() {
		return NetworkHandler.instance;
	}

	@Override
	public void close() throws SecurityException {
		if (this.client != null) {
			this.client.getConnectionManager().shutdown();
		}
	}

	/**
	 * Connects to the server and provides a handle to the server client
	 * 
	 * @author mfchurch
	 */
	private void connect() {
		this.client = new DefaultHttpClient();
		this.post = new HttpPost(ScriptEase.getInstance().getConfiguration(
				ConfigurationKeys.BugServer));

		System.out.println(this.post + " < > " + this.client);
	}

	@Override
	public void flush() {
	}

	/**
	 * Saves the record into a buffer which will only send after the user allows
	 * (in ExceptionDialog)
	 */
	@Override
	public void publish(LogRecord record) {
		this.buffered = record;
	}

	/**
	 * This method does not contain system information. If you need system
	 * information, use {@link #sendReport(String)}
	 * 
	 * @param comment
	 */
	public void sendFeedback(String comment, String email) {
		final String feedback;

		feedback = "From: " + email + "\n\nFeedback:\n\n" + comment;

		this.connect();
		this.sendToServer(feedback);
	}

	/**
	 * Sends a report to the server.
	 * 
	 * @param comment
	 *            the user comment taken from ExceptionDialog
	 * @see ExceptionDialog
	 */
	public void sendReport(String comment) {
		final String report;

		this.connect();
		report = this.generateReport(comment);

		this.sendToServer(report);
	}

	/**
	 * Connects to the server, creates a log and sends it to Httpclient.
	 * serverlog.cgi is used serverside to handle reporting.
	 * 
	 * @param servable
	 */
	private void sendToServer(String servable) {
		try {
			this.post.setEntity(new StringEntity(servable));
			HttpResponse response = this.client.execute(this.post);

			// tell the user if the report was successfully sent and clear
			// the buffer
			if (response.getStatusLine().getStatusCode() == NetworkHandler.HTML_SUCCESS) {
				this.success();
				this.buffered = null;
			} else
				throw new IOException();
		} catch (Throwable e) {
			// inform the user of an error sending the report
			this.error();
		}
	}

	/**
	 * Generates and returns a String representation of the error report
	 * including the current buffered log and any user comments
	 * 
	 * @param comment
	 *            user comments
	 * @return String representing the report
	 */
	public String generateReport(String comment) {
		String report;

		report = "=== ScriptEase2\n\n";
		report += ("=== Version: ");
		report += ScriptEase.getInstance().getVersion();
		report += " (" + ScriptEase.getInstance().getSpecificVersion() + ")\n";
		report += ("\n=== Comment:\n\n");
		report += comment + "\n";
		if (this.buffered != null) {
			final String formattedText;
			final Formatter formatter;

			formatter = new ScriptEaseFormatter();
			formattedText = formatter.format(this.buffered).trim();

			if (!formattedText.isEmpty()) {
				report += ("\n=== Log:\n\n");
				report += formattedText + "\n";
			}
		}
		report += ("\n=== System:\n\n");
		String sys = System.getProperties().toString();
		String[] properties = sys.split(",");
		for (String property : properties) {
			report += (property + "\n");
		}
		report += ("\n");
		return report;
	}

	/**
	 * Displays an error window explaining a server connection problem has
	 * occurred.
	 */
	private void error() {
		final String errorMessage;

		errorMessage = "The reporting system was unable to connect to the server.\n"
				+ "Please email the latest log file to scriptease@cs.ualberta.ca.";

		WindowFactory.getInstance().showProblemDialog("Error Report Failed",
				errorMessage);
	}

	/**
	 * Displays an success window explaining that the error report was sent
	 * successfully
	 */
	private void success() {
		final String successMessage;

		successMessage = "The report was successfully sent!\n";

		WindowFactory.getInstance().showInformationDialog("Report Success",
				successMessage);
	}
}
