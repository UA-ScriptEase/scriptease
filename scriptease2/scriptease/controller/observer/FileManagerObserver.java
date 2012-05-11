package scriptease.controller.observer;

import java.io.File;

import scriptease.model.StoryModel;

/**
 * A class can implement the FileManagerObserver interface if it needs to be
 * informed when FileManager saves/loads models.
 * 
 * @author graves
 */
public interface FileManagerObserver {
	/**
	 * This method is called whenever the FileManager notifies observers of
	 * changes they should be aware of.
	 * 
	 * @param model
	 *            The model that was referenced.
	 * @param location
	 *            The disk location where the model is stored.
	 */
	public void fileReferenced(StoryModel model, File location);
}
