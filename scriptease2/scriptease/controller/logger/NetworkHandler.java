package scriptease.controller.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * NetworkHandler provides bug report handling for ScriptEase. It connects to
 * the server and then sends each log as needed using the publish method.
 * 
 * @author mfchurch
 * 
 */
public class NetworkHandler extends Handler {
	private HttpClient client;
	private HttpPost post;
	private LogRecord buffered;
	private static final int HTML_SUCESS = 200;

	private static final NetworkHandler instance = new NetworkHandler();

	private static final String CONNECTION_ERROR_MESSAGE = "The error reporting system was unable to connect to the server.\n"
			+ "Please email the latest log file to scriptease@cs.ualberta.ca.";

	@Override
	public void close() throws SecurityException {
		if (client != null) {
			client.getConnectionManager().shutdown();
		}
	}

	/**
	 * Connects to the BUG_SERVER and provides a handle to the server client
	 * 
	 * @author mfchurch
	 */
	private void connect() {
		client = new DefaultHttpClient();
		post = new HttpPost(ScriptEase.getInstance().getConfiguration(
				ConfigurationKeys.BugServer));
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
	 * sendBugReport connects to the bugserver, creates a log and sends it to
	 * Httpclient. serverlog.cgi is used serverside to handle bug reporting.
	 * 
	 * @param comment
	 *            the user comment taken from ExceptionDialog
	 * @see ExceptionDialog
	 */
	public void sendBugReport(String comment) {
		String report;

		connect();
		report = generateReport(comment);

		try {
			post.setEntity(new StringEntity(report));
			HttpResponse response = client.execute(post);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String readLine;
			String innhold = "";
			while (((readLine = br.readLine()) != null)) {
				innhold += readLine;
			}
			// tell the user if the report was sucessfully sent and clear
			// the buffer
			if (response.getStatusLine().getStatusCode() == HTML_SUCESS) {
				success();
				this.buffered = null;
			} else
				throw new IOException();
		} catch (Throwable e) {
			// inform the user of an error sending the report
			error();
		}
	}

	/**
	 * Generates and returns a String representation of the error report
	 * including the current buffered log and any user comments
	 * 
	 * @param comment
	 *            user comments
	 * @return String representing the bug report
	 */
	public String generateReport(String comment) {
		String report;
		Formatter formatter = new ScriptEaseFormatter();

		report = "ScriptEase2\n";
		report += ("<version>\n");
		report += ScriptEase.getInstance().getVersion();
		report += " (" + ScriptEase.getInstance().getSpecificVersion() + ")";
		report += ("</version>\n<log>\n");
		if (buffered != null)
			report += (formatter.format(buffered).trim() + "\n");
		report += ("</log>\n<comment>\n");
		report += comment + "\n";
		report += ("</comment>\n<system>\n");
		String sys = System.getProperties().toString();
		String[] properties = sys.split(",");
		for (String property : properties) {
			report += (property + "\n");
		}
		report += ("</system>\n</bug>\n");
		return report;
	}

	/**
	 * Displays an error window explaining a server connection problem has
	 * occured.
	 */
	private void error() {
		WindowFactory.getInstance().showProblemDialog("Error Report Failed",
				CONNECTION_ERROR_MESSAGE);
	}

	/**
	 * Displays an sucess window explaining that the error report was sent
	 * sucessfully
	 */
	private void success() {
		String msg = "The error reporting was successfully sent!\n";
		WindowFactory.getInstance().showInformationDialog(
				"Error Report Sucess", msg);
	}

	/**
	 * Gets the sole instance of NetworkHandler as per the Singleton design
	 * pattern.
	 * 
	 * @return The sole instance of ScriptEase.
	 */
	public static NetworkHandler getInstance() {
		return NetworkHandler.instance;
	}
}
