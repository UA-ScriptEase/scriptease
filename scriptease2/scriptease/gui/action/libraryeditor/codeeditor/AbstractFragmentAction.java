package scriptease.gui.action.libraryeditor.codeeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;

/**
 * AbstractFragmentAction provides a cloneFragments method which should be used
 * by inheriting classes instead of modifying CodeBlocks code fragments
 * directly.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractFragmentAction extends
		ActiveTranslatorSensitiveAction {

	protected AbstractFragmentAction(String name) {
		super(name);
	}

	/**
	 * Clones and returns the given codeFragments in a List
	 * 
	 * @param codeFragments
	 * @return
	 */
	final protected List<AbstractFragment> cloneFragments(
			final Collection<AbstractFragment> codeFragments) {
		final ArrayList<AbstractFragment> fragments = new ArrayList<AbstractFragment>(
				codeFragments.size());
		for (AbstractFragment fragment : codeFragments) {
			fragments.add(fragment.clone());
		}
		return fragments;
	}

	/**
	 * Takes the Collection of clonedFragments and returns the one equal to the
	 * currently selected fragment. Returns null if a fragment equal to the
	 * selectedFragment cannot be found in the given clonedFragments
	 * 
	 * @param clonedFragments
	 * @return
	 */
	final protected AbstractFragment getClonedSelectedFragment(
			final AbstractFragment selectedFragment,
			final Collection<AbstractFragment> clonedFragments) {
		AbstractFragment clonedSelectedFragment = null;
		if (selectedFragment != null) {
			for (AbstractFragment fragment : clonedFragments) {
				if (fragment.equals(selectedFragment)) {
					clonedSelectedFragment = fragment;
					break;
				} else if (fragment instanceof AbstractContainerFragment) {
					AbstractFragment clonedSubFragment = getClonedSelectedFragment(
							selectedFragment,
							((AbstractContainerFragment) fragment)
									.getSubFragments());
					if (clonedSubFragment != null) {
						clonedSelectedFragment = clonedSubFragment;
						break;
					}
				}
			}
		}
		return clonedSelectedFragment;
	}
}
