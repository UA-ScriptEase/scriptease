package scriptease.controller;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.SwingWorker;

import scriptease.controller.io.FileIO;
import scriptease.gui.WindowFactory;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;
import scriptease.translator.APIDictionary;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameModule;

/**
 * Progress.aj is used to define methods which should be wrapped with a progress
 * bar.
 * 
 * We should make the progress bar appear at the lowest level possible to
 * prevent multiple progress bars from appearing on top of each other.
 * Especially watch out when adding a pointcut for a GUI class. This caused a
 * problem once where the model creation dialog would get a progress bar, then
 * it would try to load an APIDictionary within that which would call another
 * progress bar, which separated those two operations into separate stacks. The
 * dialog stack would return a null APIDictionary, and the APIDictionary stack
 * would throw an exception because it looks like we're loading the
 * APIDictionary twice. Watch out for that. - kschenk
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public aspect Progress {
	public pointcut writeStory():
		within(FileManager) && execution(* writeStoryModelFile(StoryModel, File));

	public pointcut writeAPIDictionary():
		within(FileIO) && execution(* writeAPIDictionary(APIDictionary, File));

	public pointcut openStoryModel():
		within(FileManager) && execution(* openStoryModel(File));

	public pointcut loadAPIDictionary():
		within(Translator) && execution(* loadAPIDictionary());

	public pointcut loadModule(): 
		within(Translator) && execution(* loadModule(GameModule));

	public pointcut modelActivated():
		within(SEModelManager) && execution(* activate(SEModel));

	private pointcut showProgress():
		writeStory() || 
		writeAPIDictionary() || 
		openStoryModel() ||
		modelActivated() ||
		loadModule() ||
		loadAPIDictionary();

	void around(): showProgress() {
		// this is being marked as unused even though it appears below.
		final Thread mainThread = Thread.currentThread();
		final SwingWorker<Void, Void> task = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					proceed();
				} catch (Throwable t) {
					final UncaughtExceptionHandler handler = Thread
							.getDefaultUncaughtExceptionHandler();
					handler.uncaughtException(mainThread, t);
				}
				return null;
			}
		};
		task.execute();
		// TODO We should somehow pass in loading text here.
		WindowFactory.getInstance().showProgressBar(task, "Loading...");
	}
}
