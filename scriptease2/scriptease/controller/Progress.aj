package scriptease.controller;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.SwingWorker;

import scriptease.gui.WindowManager;
import scriptease.gui.dialog.WizardDialog;
import scriptease.gui.storycomponentbuilder.StoryComponentLibraryPanel;
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
		within(StoryComponentLibraryPanel) && execution(* setTranslator(..));

	private pointcut showProgress():
		writeStory() || 
		finishWizard() ||
		openStoryModel() ||
		loadTranslator();

	void around(): showProgress() {
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				proceed();
				return null;
			}
		};
		task.execute();
		WindowManager.getInstance().showProgressBar(task);
	}
}
