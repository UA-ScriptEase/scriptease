package scriptease.controller.logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Provides custom formatting for logging output used in ScriptEase
 * 
 * @author mfchurch
 * 
 */
public class ScriptEaseFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

		return sdf.format(Calendar.getInstance().getTime()) + " | "
				+ record.getLevel() + ": " + record.getMessage() + "\n";
	}
}
