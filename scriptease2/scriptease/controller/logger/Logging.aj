package scriptease.controller.logger;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import scriptease.controller.exceptionhandler.ScriptEaseExceptionHandler;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;

/**
 * The aspect Logging handles all Logging in ScriptEase. It does this in two
 * ways, the consoleLog and the outputLog. The consoleLog displays feedback via
 * the console and by default will only show INFO and higher logs. The outputLog
 * keeps track of what the user has done and in the event of an exception will
 * push the past 1000 actions to a file and to the bugreport server (upon
 * confirmation by the user).
 * 
 * @author mfchurch
 * 
 */
public aspect Logging {
	private static Logger outputLog = Logger.getLogger("logger.output");
	private static Logger consoleLog = Logger.getLogger("logger");
	private static Level OUTPUT_LEVEL = Level.FINEST;
	private static Level CONSOLE_LEVEL = Level.INFO;
	private static String FILE_LOG = "errors.log";

	// Initialization of Logging
	static {
		// TODO: USE CONFIG FILE TO IMPORT LEVEL SETTINGS
		// create loggers
		consoleLog.setLevel(CONSOLE_LEVEL);
		consoleLog.setUseParentHandlers(false);
		outputLog.setLevel(OUTPUT_LEVEL);

		// create handlers
		Handler consoleHandler = new ConsoleHandler();
		Handler fileHandler = null;
		Handler networkHandler = null;
		try {
			fileHandler = new FileHandler(FILE_LOG);
			networkHandler = NetworkHandler.getInstance();
		} catch (Exception e) {
			// how to handle?
			System.out.println("Handler Error");
		}
		// set handlers level
		consoleHandler.setLevel(CONSOLE_LEVEL);
		fileHandler.setLevel(OUTPUT_LEVEL);
		networkHandler.setLevel(OUTPUT_LEVEL);

		List<Handler> handlers = new ArrayList<Handler>();
		handlers.add(fileHandler);
		handlers.add(networkHandler);
		MultiHandler multiHandler = new MultiHandler(handlers);
		multiHandler.setLevel(OUTPUT_LEVEL);
		ScriptEaseMemoryHandler memoryHandler = new ScriptEaseMemoryHandler(
				multiHandler, 1000, Level.SEVERE);

		// set handler formatters
		ScriptEaseFormatter formatter = new ScriptEaseFormatter();
		consoleHandler.setFormatter(formatter);
		fileHandler.setFormatter(formatter);
		networkHandler.setFormatter(formatter);
		memoryHandler.setFormatter(formatter);

		// link handlers to their logger
		consoleLog.addHandler(consoleHandler);
		outputLog.addHandler(memoryHandler);
	}

	/**************************************** POINT CUTS *************************************/
	// pointcut for system print method calls
	pointcut system(PrintStream ps, String txt, Object caller):
		call(void print*(String)) && target(ps) && args(txt) && this(caller);

	// pointcut for unimplemented context
	pointcut unimplemented(String methodName, Object caller) :
		within(Context) && (execution(* unimplemented(String))) && args (methodName) && this(caller);

	// pointcut for actionPerformed method calls
	pointcut actions(Action owner):
		within(Action+) && execution(* actionPerformed(ActionEvent))
		&& this(owner)
		;

	// pointcut for context creation calls
	pointcut context(Context pastContext, Object source) :
		within(ContextFactory) && execution(* createContext(Context ,Object)) && args (pastContext, source)
		;

	// pointcut for ScriptEaseExceptionHandler uncaughtException calls
	pointcut exception(Thread thread, Throwable exception):
		within(ScriptEaseExceptionHandler) && execution(* uncaughtException(Thread ,Throwable)) && args(thread, exception)
		;

	/**************************************** ADVICE *************************************/
	// advice for context pointcut
	after(Context pastContext, Object source) : context(pastContext, source) {
		outputLog.log(Level.FINEST,
				" CONTEXT SCOPING INTO >> " + pastContext.getIndent()
						+ GetSimpleName(source));
	}

	// advice for unimplemented pointcut
	after(String methodName, Object caller) : unimplemented(methodName, caller) {
		outputLog.log(Level.WARNING, "UNIMPLEMENTED CALL TO " + methodName
				+ " IN " + GetSimpleName(caller));
	}

	// advice for action pointcut
	before(Action owner): actions(owner){
		outputLog.log(Level.FINEST, "ACTION " + GetSimpleName(owner));
	}

	// advice for exception pointcut
	before(Thread thread, Throwable exception): exception(thread, exception) {
		outputLog.log(Level.SEVERE, "EXCEPTION " + makeTraceString(exception));
	}

	// advice for system pointcut
	void around(PrintStream ps, String txt, Object caller): system(ps,txt,caller)	{
		// String stream;
		// if (ps.equals(System.out))
		// stream = "System.out";
		// else
		// stream = "System.err";
		outputLog.log(Level.INFO, /* stream* + " - " + */txt + " FROM "
				+ GetSimpleName(caller));
	}

	/**************************************** HELPERS *************************************/
	// helper method which returns the simple name of the given object
	private String GetSimpleName(Object object) {
		return object == null ? null : object.getClass().getSimpleName();
	}

	/**
	 * helper method which creates a trace string from a throwable (taken from
	 * ScriptEaseExceptionHandler)
	 */
	private static String makeTraceString(Throwable err) {
		String trace;
		StringWriter traceWriter = new StringWriter();
		PrintWriter print = new PrintWriter(traceWriter);

		err.printStackTrace(print);

		print.flush();
		trace = traceWriter.toString();

		try {
			traceWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		print.close();

		return trace;
	}
}
