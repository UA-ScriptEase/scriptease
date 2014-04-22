package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.libraryeditor.codeblocks.CodeFragmentPanel;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;
import sun.awt.util.IdentityArrayList;

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

	/**
	 * Move the currently selected format fragment an amount determined by the
	 * delta.
	 * 
	 * @param topLevelFormatFragments
	 * @param selectedFragment
	 * @param parentFragment
	 * @return
	 */
	private void moveFragment(final List<AbstractFragment> topLevelFragments,
			final AbstractFragment selectedFragment,
			final AbstractContainerFragment parentFragment) {
		final List<AbstractFragment> topLevelFormatFragments;

		topLevelFormatFragments = new IdentityArrayList<AbstractFragment>(
				topLevelFragments);

		if (topLevelFormatFragments.contains(selectedFragment)) {
			final int currentIndex;
			final int insertIntoIndex;

			currentIndex = topLevelFormatFragments.indexOf(selectedFragment);
			insertIntoIndex = currentIndex + delta();
			if (insertIntoIndex >= 0
					&& insertIntoIndex < topLevelFormatFragments.size()) {

				topLevelFormatFragments.remove(selectedFragment);
				topLevelFormatFragments.add(currentIndex + delta(),
						selectedFragment);

				if (parentFragment != null)
					parentFragment
							.setSubFragments(new ArrayList<AbstractFragment>(
									topLevelFormatFragments));

			}
		} else {
			for (AbstractFragment formatFragment : topLevelFormatFragments) {
				if (formatFragment instanceof AbstractContainerFragment) {
					final Collection<AbstractFragment> subFragments;
					final ArrayList<AbstractFragment> subFragmentsList;

					subFragments = ((AbstractContainerFragment) formatFragment)
							.getSubFragments();
					subFragmentsList = new ArrayList<AbstractFragment>();

					subFragmentsList.addAll(subFragments);

					moveFragment(subFragmentsList, selectedFragment,
							(AbstractContainerFragment) formatFragment);
				}
			}
		}
	}

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
		final List<AbstractFragment> originalFragments;
		final List<AbstractFragment> clonedFragments;
		final AbstractFragment selectedFragment;

		panel = (CodeFragmentPanel) SEFocusManager.getInstance().getFocus();

		codeBlock = panel.getCodeBlock();
		originalFragments = codeBlock.getCode();
		clonedFragments = AbstractFragment.cloneFragments(originalFragments);

		selectedFragment = AbstractFragment.getInSamePosition(
				panel.getFragment(), originalFragments, clonedFragments);

		if (selectedFragment != null) {
			this.moveFragment(clonedFragments, selectedFragment, null);
			UndoManager.getInstance().startUndoableAction(
					"Setting CodeBlock " + codeBlock + " code to "
							+ clonedFragments);
			codeBlock.setCode(clonedFragments);
			UndoManager.getInstance().endUndoableAction();
		}
	}
}
