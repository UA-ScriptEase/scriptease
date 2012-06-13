package scriptease.controller.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

/**
 * Similar to MemoryHandler, except it condenses all records into a single
 * record which is sent to it's target upon pushing (instead of sending record
 * by record as MemoryHandler does).
 * 
 * @author mfchurch
 * @see MemoryHandler
 * 
 */
public class ScriptEaseMemoryHandler extends Handler {
	private List<String> buffer;
	private Level pushLevel;
	private Handler target;
	private int size;
	private ScriptEaseFormatter formatter;

	public ScriptEaseMemoryHandler(Handler target, int size, Level pushLevel) {
		if (target == null || pushLevel == null) {
			throw new NullPointerException();
		}
		if (size <= 0) {
			throw new IllegalArgumentException();
		}
		this.pushLevel = pushLevel;
		this.target = target;
		this.size = size;

		init();
	}

	private void init() {
		this.buffer = new ArrayList<String>();
		formatter = new ScriptEaseFormatter();
	}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record)) {
			return;
		}
		if (buffer.size() >= this.size)
			buffer.remove(0);
		buffer.add(formatter.format(record));

		if (record.getLevel().intValue() >= pushLevel.intValue()) {
			push();
		}
	}

	public synchronized void push() {
		String msg = " ------- \n";
		for (String record : buffer) {
			msg += record;
		}
		LogRecord condensedRec = new LogRecord(this.pushLevel, msg);
		this.target.publish(condensedRec);
		buffer.clear();
	}

	@Override
	public void close() throws SecurityException {
		this.target.close();
		setLevel(Level.OFF);
	}

	@Override
	public void flush() {
		this.target.flush();
	}

	public void setPushLevel(Level newLevel) throws SecurityException {
		if (newLevel == null) {
			throw new NullPointerException();
		}
		pushLevel = newLevel;
	}

	public synchronized Level getPushLevel() {
		return pushLevel;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		return super.isLoggable(record);
	}
}
