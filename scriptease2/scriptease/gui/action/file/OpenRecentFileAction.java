/**
 * 
 */
package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;

import scriptease.controller.FileManager;
import scriptease.controller.observer.FileManagerObserver;
import scriptease.model.StoryModel;

/**
 * Represents and performs the Open Recent File command, as well as encapsulates
 * its enabled and name display state. <br>
 * <br>
 * Open Recent File entails calling the FileManager's <code>open</code> method
 * from a saved file path.<br>
 * <br>
 * <b>Note:</b> This Action implementation <b>does not</b> implement the
 * Singleton pattern since it will only ever appear in one recentFileIndex: the
 * File menu. The other Actions are Singletons so that different JComponents can
 * access the same instance easily.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class OpenRecentFileAction extends AbstractAction implements FileManagerObserver {
	private final short recentFileIndex;

	/**
	 * Defines an <code>OpenRecentFileAction</code> object.<br>
	 * <br>
	 * The meaning of <code>freshessIndex</code> is relatively simple. The
	 * Freshness Index is a measure of how recent a file this menu item will
	 * open. A OpenRecentFileAction with a Freshness Index of 3 will open the
	 * 3rd most recent file. Freshness Index values start at 0 and increase.<br>
	 * <br>
	 * Freshness Index does not effect the order that the menu items appear in
	 * the recent file list, though by standard the most recent is often at the
	 * top of the list.
	 * 
	 * @param freshnessIndex
	 *            The freshness index of this OpenRecentFileAction. Negative
	 *            values will be reset to 0.
	 */
	public OpenRecentFileAction(short freshnessIndex) {
		super();

		this.recentFileIndex = freshnessIndex;
		this.updateName();
	}

	/**
	 * Updates the name to use the name of the file for this action's Freshness
	 * Index
	 */
	private void updateName() {
		this.putValue(Action.NAME, this.recentFileIndex
				+ 1
				+ " - ..."
				+ File.separator
				+ FileManager.getInstance().getRecentFile(this.recentFileIndex)
						.getName());
		this.putValue(Action.SHORT_DESCRIPTION, FileManager.getInstance()
				.getRecentFile(this.recentFileIndex).getAbsolutePath());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final FileManager fileSys = FileManager.getInstance();

		fileSys.openStoryModel(fileSys.getRecentFile(this.recentFileIndex));
	}
	
	@Override
	public void fileReferenced(StoryModel model, File location) {
		this.updateName();
	}
	
}
