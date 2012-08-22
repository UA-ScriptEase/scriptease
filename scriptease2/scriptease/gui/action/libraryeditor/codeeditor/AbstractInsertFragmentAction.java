package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.libraryeditor.FormatFragmentSelectionManager;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;
import sun.awt.util.IdentityArrayList;

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
	}

	/**
	 * Inserts a new FormatFragment into a Container Fragment.
	 * 
	 * @param containerFragment
	 * @return
	 */
	private void insertFragmentIntoContainerFragment(
			final AbstractContainerFragment containerFragment) {
		final ArrayList<AbstractFragment> subFragments;

		subFragments = new ArrayList<AbstractFragment>();

		subFragments.addAll(containerFragment.getSubFragments());
		subFragments.add(this.newFragment());

		containerFragment.setSubFragments(subFragments);
	}

	/**
	 * Insert a new FormatFragment into the list of FormatFragments after the
	 * other specified line fragment. This gets called recursively until the
	 * selected fragment is found. To start at the top, pass in "null" as the
	 * parent fragment.
	 * 
	 * @param topLevelFormatFragments
	 * @param selectedFragment
	 * @param parentFragment
	 * @return
	 */
	private void insertFragmentAfterFragment(
			final List<AbstractFragment> topLevelFormatFragments,
			final AbstractFragment selectedFragment,
			final AbstractContainerFragment parentFragment) {

		if (topLevelFormatFragments.contains(selectedFragment)) {
			topLevelFormatFragments.add(
					topLevelFormatFragments.indexOf(selectedFragment) + 1,
					this.newFragment());

			if (parentFragment != null)
				parentFragment.setSubFragments(topLevelFormatFragments);

		} else {
			for (AbstractFragment formatFragment : topLevelFormatFragments) {
				if (formatFragment instanceof AbstractContainerFragment) {
					final Collection<AbstractFragment> subFragments;
					final List<AbstractFragment> subFragmentsList;

					subFragments = ((AbstractContainerFragment) formatFragment)
							.getSubFragments();
					subFragmentsList = new IdentityArrayList<AbstractFragment>();

					subFragmentsList.addAll(subFragments);

					insertFragmentAfterFragment(subFragmentsList,
							selectedFragment,
							(AbstractContainerFragment) formatFragment);
				}
			}
		}
	}

	/**
	 * Inserts a new FormatFragment at the end of the list of FormatFragments.
	 * 
	 * @param fragments
	 */
	private void insertFragmentAtEnd(
			final Collection<AbstractFragment> fragments) {
		fragments.add(this.newFragment());
	}

	/**
	 * Returns the default new fragment.
	 * 
	 * @return
	 */
	protected abstract AbstractFragment newFragment();

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
				
				if (selectedFragment instanceof AbstractContainerFragment) {
					this.insertFragmentIntoContainerFragment((AbstractContainerFragment) selectedFragment);
					FormatFragmentSelectionManager.getInstance().getCodeBlock()
							.setCode(fragments);
					
				} else {
					this.insertFragmentAfterFragment(fragments,
							selectedFragment, null);
					FormatFragmentSelectionManager.getInstance().getCodeBlock()
							.setCode(fragments);
				}
			} else {
				this.insertFragmentAtEnd(fragments);
				FormatFragmentSelectionManager.getInstance().getCodeBlock()
						.setCode(fragments);
			}
		}
	}
}
