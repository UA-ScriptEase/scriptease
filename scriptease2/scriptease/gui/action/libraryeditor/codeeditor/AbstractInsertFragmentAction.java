package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	 * Inserts a new FormatFragment into a Container Fragment.
	 * 
	 * @param container
	 * @return
	 */
	private void insertFragmentIntoContainerFragment(
			AbstractContainerFragment container) {
		final ArrayList<AbstractFragment> fragments;

		fragments = new ArrayList<AbstractFragment>(container.getSubFragments());

		fragments.add(this.newFragment());
		container.setSubFragments(fragments);
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
		final List<AbstractFragment> fragments;
		final AbstractFragment selectedFragment;

		codeBlock = panel.getCodeBlock();

		fragments = AbstractFragment.cloneFragments(codeBlock.getCode());
		selectedFragment = AbstractFragment.getClonedSelectedFragment(
				panel.getFragment(), fragments);

		if (selectedFragment != null) {
			if (selectedFragment instanceof AbstractContainerFragment) {
				this.insertFragmentIntoContainerFragment((AbstractContainerFragment) selectedFragment);
			} else {
				this.insertFragmentAfterFragment(fragments, selectedFragment,
						null);
			}
		} else {
			this.insertFragmentAtEnd(fragments);
		}

		UndoManager.getInstance().startUndoableAction(
				"Setting CodeBlock " + codeBlock + " code to " + fragments);
		codeBlock.setCode(fragments);
		UndoManager.getInstance().endUndoableAction();
	}
}
