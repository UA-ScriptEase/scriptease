package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.libraryeditor.FormatFragmentSelectionManager;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;
import sun.awt.util.IdentityArrayList;

/**
 * Represents and performs the Delete Fragment command, as well as encapsulating
 * its enabled and name display state. This command will delete the currently
 * selected Format Fragment from the model.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class DeleteFragmentAction extends ActiveTranslatorSensitiveAction {
	private static final String DELETE_FRAGMENT_TEXT = "Delete";

	private static final DeleteFragmentAction instance = new DeleteFragmentAction();

	private DeleteFragmentAction() {
		super(DELETE_FRAGMENT_TEXT);

		this.putValue(SHORT_DESCRIPTION, DELETE_FRAGMENT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static DeleteFragmentAction getInstance() {
		return DeleteFragmentAction.instance;
	}

	/**
	 * Delete the selected FormatFragment in the list of FormatFragments after
	 * the other specified line fragment. This gets called recursively until the
	 * selected fragment is found. To start at the top, pass in "null" as the
	 * parent fragment.
	 * 
	 * @param topLevelFormatFragments
	 * @param selectedFragment
	 * @param parentFragment
	 * @return
	 */
	private void deleteFragment(
			final List<AbstractFragment> topLevelFormatFragments,
			final AbstractFragment selectedFragment,
			final AbstractContainerFragment parentFragment) {

		if (topLevelFormatFragments.remove(selectedFragment)) {
			if (parentFragment != null)
				parentFragment.setSubFragments(topLevelFormatFragments);

		} else {
			for (AbstractFragment formatFragment : topLevelFormatFragments) {
				if (formatFragment instanceof AbstractContainerFragment) {
					final Collection<AbstractFragment> subFragments;
					final List<AbstractFragment> subFragmentsList;

					subFragments = ((AbstractContainerFragment) formatFragment)
							.getSubFragments();
					subFragmentsList = new ArrayList<AbstractFragment>();

					subFragmentsList.addAll(subFragments);

					deleteFragment(subFragmentsList, selectedFragment,
							(AbstractContainerFragment) formatFragment);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final CodeBlock codeBlock;

		codeBlock = FormatFragmentSelectionManager.getInstance().getCodeBlock();

		if (codeBlock != null) {
			final AbstractFragment selectedFragment;
			final Collection<AbstractFragment> code;
			final IdentityArrayList<AbstractFragment> fragments;

			selectedFragment = FormatFragmentSelectionManager.getInstance()
					.getFormatFragment();
			code = codeBlock.getCode();
			fragments = new IdentityArrayList<AbstractFragment>();

			fragments.addAll(code);

			if (selectedFragment != null) {
				this.deleteFragment(fragments, selectedFragment, null);
				UndoManager.getInstance().startUndoableAction(
						"Setting CodeBlock " + codeBlock + " code to "
								+ fragments);
				codeBlock.setCode(fragments);
				UndoManager.getInstance().endUndoableAction();
			}
		}
	}
}
