package scriptease.gui.action.storycomponentbuilder.codeeditor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.managers.FormatFragmentSelectionManager;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;
import sun.awt.util.IdentityArrayList;

/**
 * This class contains all of the methods that can insert FormatFragments into
 * code blocks using the story component editor. Subclasses should have an a
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
		final ArrayList<FormatFragment> subFragments;

		subFragments = new ArrayList<FormatFragment>();

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
			final List<FormatFragment> topLevelFormatFragments,
			final FormatFragment selectedFragment,
			final AbstractContainerFragment parentFragment) {

		if (topLevelFormatFragments.contains(selectedFragment)) {
			topLevelFormatFragments.add(
					topLevelFormatFragments.indexOf(selectedFragment) + 1,
					this.newFragment());

			if (parentFragment != null)
				parentFragment.setSubFragments(topLevelFormatFragments);

		} else {
			for (FormatFragment formatFragment : topLevelFormatFragments) {
				if (formatFragment instanceof AbstractContainerFragment) {
					final Collection<FormatFragment> subFragments;
					final List<FormatFragment> subFragmentsList;

					subFragments = ((AbstractContainerFragment) formatFragment)
							.getSubFragments();
					subFragmentsList = new IdentityArrayList<FormatFragment>();

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
			final Collection<FormatFragment> fragments) {
		fragments.add(this.newFragment());
	}

	/**
	 * Returns the default new fragment.
	 * 
	 * @return
	 */
	protected abstract FormatFragment newFragment();

	@Override
	public void actionPerformed(ActionEvent arg0) {
		final CodeBlock codeBlock;

		codeBlock = FormatFragmentSelectionManager.getInstance().getCodeBlock();

		if (codeBlock != null) {
			final FormatFragment selectedFragment;
			final Collection<FormatFragment> code;
			final ArrayList<FormatFragment> fragments;

			selectedFragment = FormatFragmentSelectionManager.getInstance()
					.getFormatFragment();
			code = codeBlock.getCode();
			fragments = new ArrayList<FormatFragment>();

			fragments.addAll(code);

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
