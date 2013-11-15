package scriptease.controller.observer;

import scriptease.gui.libraryeditor.codeblocks.CodeBlockPanel;

/**
 * Listens to changes for CodeBLockPanel {@link CodeBlockPanel}
 * 
 * @author jyuen
 */
public interface CodeBlockPanelObserver {
	/**
	 * Notifies the receiver that the codeBlockPanel has changed
	 */
	public void codeBlockPanelChanged();
}
