package scriptease.controller.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Extends a Normal Handler allowing the Handler to publish to multiple sub
 * handlers at once. Used by MemoryHandler's push method to allow multiple sub
 * handlers.
 * 
 * @author mfchurch
 * 
 */
public class MultiHandler extends Handler {
	private List<Handler> targets;

	public MultiHandler(List<Handler> targets) {
		this.targets = new ArrayList<Handler>(targets);
	}

	@Override
	public void close() throws SecurityException {
		for (Handler target : this.targets) {
			target.close(); 
		}
	}

	@Override
	public void flush() {
		for (Handler target : this.targets) {
			target.flush();
		}
	}

	@Override
	public void publish(LogRecord record) {
		for (Handler target : this.targets) {
			target.publish(record);
		}
	}
}
