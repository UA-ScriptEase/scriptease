package scriptease.controller.observer;

import scriptease.gui.libraryeditor.codeblocks.CodeBlockPanel;
import scriptease.model.atomic.KnowIt;

/**
 * Listens to changes for CodeBLockPanel {@link CodeBlockPanel}
 * 
 * @author jyuen
 */
public interface CodeBlockPanelObserver {
	/**
	 * Notifies the receiver that the codeBlockPanel parameter has changed
	 */
	public void codeBlockPanelChanged(KnowIt parameter);
}
