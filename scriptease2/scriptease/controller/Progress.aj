package scriptease.controller;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.SwingWorker;

import scriptease.gui.PanelFactory;
import scriptease.gui.WindowFactory;
import scriptease.gui.dialog.WizardDialog;
import scriptease.model.StoryModel;

/**
 * Progress.aj is used to define methods which should be wrapped with a progress
 * bar.
 * 
 * @author mfchurch
 * 
 */
public aspect Progress {
	public pointcut writeStory():
		within(FileManager) && execution(* writeStoryModelFile(StoryModel, File));

	public pointcut finishWizard():
		within(WizardDialog) && execution(* actionPerformed(ActionEvent));

	public pointcut openStoryModel():
		within(FileManager) && execution(* openStoryModel(File));

	public pointcut loadTranslator():
		within(PanelFactory) && execution(* setTranslator(..));
	
	private pointcut showProgress():
		writeStory() || 
		finishWizard() ||
		openStoryModel() ||
		loadTranslator();

	void around(): showProgress() {
		// this is being marked as unused even though it appears below.
		// Java/AspectJ compiler bug, I guess. - remiller
		@SuppressWarnings("unused")
		final Thread mainThread = Thread.currentThread();
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>() {
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
