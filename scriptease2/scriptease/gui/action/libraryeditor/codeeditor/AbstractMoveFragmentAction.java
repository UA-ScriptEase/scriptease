package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.libraryeditor.codeblocks.CodeFragmentPanel;
import scriptease.model.CodeBlock;

/**
 * This class contains all of the methods that can move FormatFragments in code
 * blocks using the story component editor.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractMoveFragmentAction extends
		ActiveTranslatorSensitiveAction {

	protected AbstractMoveFragmentAction(String name) {
		super(name);

		this.putValue(Action.SHORT_DESCRIPTION, name);

		SEFocusManager.getInstance().addSEFocusObserver(new SEFocusObserver() {

			@Override
			public void gainFocus(Component oldFocus) {
				AbstractMoveFragmentAction.this.updateEnabledState();
			}

			@Override
			public void loseFocus(Component oldFocus) {
				AbstractMoveFragmentAction.this.updateEnabledState();
			}
		});
	}

	protected abstract int delta();

	@Override
	protected boolean isLegal() {
		final Component focusOwner = SEFocusManager.getInstance().getFocus();

		return super.isLegal() && focusOwner instanceof CodeFragmentPanel
				&& ((CodeFragmentPanel) focusOwner).getCodeBlock() != null;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		final CodeFragmentPanel panel;
		final CodeBlock codeBlock;

		panel = (CodeFragmentPanel) SEFocusManager.getInstance().getFocus();
		codeBlock = panel.getCodeBlock();

		if (!UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction("Move Code Fragment");
		codeBlock.moveCodeFragment(panel.getFragment(), delta());
		UndoManager.getInstance().endUndoableAction();
	}
}
