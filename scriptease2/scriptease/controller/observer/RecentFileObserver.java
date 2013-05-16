package scriptease.controller.observer;

/**
 * Observes for changes to recent files including recent files that no longer
 * exist.
 * 
 * @author jyuen
 */
public interface RecentFileObserver {

	/**
	 * This method is called whenever the FileManager notifies recent file
	 * observers of changes they should be aware of.
	 */
	public void updateRecentFiles();
}
