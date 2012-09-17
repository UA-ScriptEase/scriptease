package scriptease.controller.logger;

import java.util.Vector;
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
	private final Vector<String> buffer;
	private Level pushLevel;
	private final Handler target;
	private final int maxSize;
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
		this.maxSize = size;

		// this is a vector instead of ArrayList to be able to handle multi-threading.
		this.buffer = new Vector<String>();

		this.formatter = new ScriptEaseFormatter();
	}

	@Override
	public void publish(LogRecord record) {
		if (!this.isLoggable(record))
			return;

		if (this.buffer.size() >= this.maxSize)
			this.buffer.remove(0);

		this.buffer.add(this.formatter.format(record));

		if (record.getLevel().intValue() >= this.pushLevel.intValue()) {
			this.push();
		}
	}

	public synchronized void push() {
		StringBuffer msg = new StringBuffer(" ------- \n");

		for (String record : this.buffer) {
			msg.append(record);
		}

		LogRecord condensedRec = new LogRecord(this.pushLevel, msg.toString());
		this.target.publish(condensedRec);
		this.buffer.clear();
	}

	@Override
	public void close() throws SecurityException {
		this.target.close();
		this.setLevel(Level.OFF);
	}

	@Override
	public void flush() {
		this.target.flush();
	}

	public void setPushLevel(Level newLevel) throws SecurityException {
		if (newLevel == null) {
			throw new NullPointerException();
		}

		this.pushLevel = newLevel;
	}

	public synchronized Level getPushLevel() {
		return this.pushLevel;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		return super.isLoggable(record);
	}
}
