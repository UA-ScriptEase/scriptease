package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.libraryeditor.FormatFragmentSelectionManager;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;

/**
 * This class contains all of the methods that can move FormatFragments in code
 * blocks using the story component editor.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractMoveFragmentAction extends AbstractFragmentAction {

	protected AbstractMoveFragmentAction(String name) {
		super(name);

		this.putValue(Action.SHORT_DESCRIPTION, name);
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
	private void moveFragment(
			final List<AbstractFragment> topLevelFormatFragments,
			final AbstractFragment selectedFragment,
			final AbstractContainerFragment parentFragment) {

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
					parentFragment.setSubFragments(topLevelFormatFragments);

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
	public void actionPerformed(ActionEvent arg0) {
		final CodeBlock codeBlock;

		codeBlock = FormatFragmentSelectionManager.getInstance().getCodeBlock();

		if (codeBlock != null) {
			final List<AbstractFragment> fragments = cloneFragments(codeBlock
					.getCode());
			final AbstractFragment selectedFragment = getClonedSelectedFragment(
					FormatFragmentSelectionManager.getInstance()
							.getFormatFragment(), fragments);
			if (selectedFragment != null) {
				this.moveFragment(fragments, selectedFragment, null);
				UndoManager.getInstance().startUndoableAction(
						"Setting CodeBlock " + codeBlock + " code to "
								+ fragments);
				codeBlock.setCode(fragments);
				UndoManager.getInstance().endUndoableAction();
			}
		}
	}
}
