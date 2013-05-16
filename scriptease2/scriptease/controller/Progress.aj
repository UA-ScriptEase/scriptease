package scriptease.controller;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.SwingWorker;

import scriptease.gui.WindowFactory;
import scriptease.gui.action.library.OpenAPIDictionaryEditorAction;
import scriptease.gui.dialog.DialogBuilder.WizardDialog;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;
import scriptease.translator.Translator;

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
		within(WizardDialog) && execution(* finishWizard(Runnable));

	public pointcut openStoryModel():
		within(FileManager) && execution(* openStoryModel(File));

	public pointcut loadAPIDictionary():
		within(Translator) && execution(* loadAPIDictionary());
	
	public pointcut modelActivated():
		within(SEModelManager) && execution(* activate(SEModel));
	
	private pointcut showProgress():
		writeStory() || 
		finishWizard() ||
		openStoryModel() ||
		modelActivated() ||
		loadAPIDictionary();

	void around(): showProgress() {
		// this is being marked as unused even though it appears below.
		// Java/AspectJ compiler bug, I guess. - remiller
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
