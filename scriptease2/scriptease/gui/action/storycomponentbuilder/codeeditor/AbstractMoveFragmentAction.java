package scriptease.gui.action.storycomponentbuilder.codeeditor;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;

import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.managers.FormatFragmentSelectionManager;
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
					final List<AbstractFragment> subFragmentsList;

					subFragments = ((AbstractContainerFragment) formatFragment)
							.getSubFragments();
					subFragmentsList = new IdentityArrayList<AbstractFragment>();

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
			final AbstractFragment selectedFragment;
			final IdentityArrayList<AbstractFragment> fragments;

			selectedFragment = FormatFragmentSelectionManager.getInstance()
					.getFormatFragment();
			fragments = new IdentityArrayList<AbstractFragment>();

			fragments.addAll(codeBlock.getCode());

			if (selectedFragment != null) {
				this.moveFragment(fragments, selectedFragment, null);
				FormatFragmentSelectionManager.getInstance().getCodeBlock()
						.setCode(fragments);
			}
		}
	}
}
