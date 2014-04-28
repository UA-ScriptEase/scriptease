package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.libraryeditor.codeblocks.CodeFragmentPanel;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * This class contains all of the methods that can insert FormatFragments into
 * code blocks using the story component editor.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractInsertFragmentAction extends
		ActiveTranslatorSensitiveAction {

	protected AbstractInsertFragmentAction(String name) {
		super(name);
		this.putValue(SHORT_DESCRIPTION, name);

		SEFocusManager.getInstance().addSEFocusObserver(new SEFocusObserver() {

			@Override
			public void gainFocus(Component oldFocus) {
				AbstractInsertFragmentAction.this.updateEnabledState();
			}

			@Override
			public void loseFocus(Component oldFocus) {
				AbstractInsertFragmentAction.this.updateEnabledState();
			}
		});
	}

	/**
	 * Returns the default new fragment.
	 * 
	 * @return
	 */
	protected abstract AbstractFragment newFragment();

	@Override
	protected boolean isLegal() {
		final Component focusOwner = SEFocusManager.getInstance().getFocus();

		return super.isLegal() && focusOwner instanceof CodeFragmentPanel
				&& ((CodeFragmentPanel) focusOwner).getCodeBlock() != null;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		final Component focus = SEFocusManager.getInstance().getFocus();
		final CodeFragmentPanel panel;

		if (focus instanceof CodeFragmentPanel)
			panel = (CodeFragmentPanel) focus;
		else
			// Sometimes we hit this for some reason. We shouldn't, but we can,
			// so we check for it instead of exceptioning.
			return;

		final CodeBlock codeBlock;
		final AbstractFragment selected;

		codeBlock = panel.getCodeBlock();
		selected = panel.getFragment();
		
		if (!UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction("Move Code Fragment");
		codeBlock.insertCodeFragment(this.newFragment(), selected);
		UndoManager.getInstance().endUndoableAction();
	}
}
