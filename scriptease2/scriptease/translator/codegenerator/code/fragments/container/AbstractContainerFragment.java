package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * An abstract class for fragments that can contain sub fragments.
 * 
 * @author kschenk
 * 
 */
public abstract class AbstractContainerFragment extends AbstractFragment {
	protected List<AbstractFragment> subFragments;

	public AbstractContainerFragment(String text,
			List<AbstractFragment> subFragments) {
		super(text);
		this.subFragments = new ArrayList<AbstractFragment>(subFragments);
	}

	public List<AbstractFragment> getSubFragments() {
		return new ArrayList<AbstractFragment>(this.subFragments);
	}

	public void setSubFragments(List<AbstractFragment> subFragments) {
		this.subFragments = new ArrayList<AbstractFragment>(subFragments);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.subFragments.hashCode();
	}

	@Override
	public AbstractContainerFragment clone() {
		final AbstractContainerFragment clone = (AbstractContainerFragment) super
				.clone();

		final List<AbstractFragment> clonedSubFragments = new ArrayList<AbstractFragment>(
				this.subFragments.size());
		for (AbstractFragment fragment : this.subFragments) {
			clonedSubFragments.add(fragment.clone());
		}
		clone.setSubFragments(clonedSubFragments);

		return clone;
	}

	/**
	 * Move the currently selected format fragment an amount determined by the
	 * delta.
	 * 
	 * @param topLevelFormatFragments
	 * @param subFragment
	 * @param parentFragment
	 * @return
	 */
	public boolean moveSubFragment(AbstractFragment subFragment, int delta) {
		return AbstractContainerFragment.moveFragmentInList(subFragment,
				this.subFragments, delta);
	}

	public boolean deleteSubFragment(AbstractFragment fragment) {
		return AbstractContainerFragment.deleteFragmentFromList(fragment,
				this.subFragments);
	}

	public boolean insertSubFragment(AbstractFragment subFragment,
			AbstractFragment previousFragment) {
		return AbstractContainerFragment.insertFragmentIntoList(subFragment,
				previousFragment, this.subFragments);
	}

	/**
	 * Moves the fragment in a list of fragments. Returns true if it was found.
	 * Note that the list is directly edited by this method.
	 * 
	 * @param subFragment
	 * @param list
	 * @param delta
	 * @return
	 */
	public static boolean moveFragmentInList(AbstractFragment subFragment,
			List<AbstractFragment> list, int delta) {
		boolean found = false;
		int index = 0;
		for (AbstractFragment fragment : list) {
			if (fragment == subFragment) {
				found = true;
				break;
			}
			index++;
		}

		if (found) {
			final int newIndex = index + delta;

			if (newIndex >= 0 && newIndex < list.size()) {
				list.remove(index);
				list.add(newIndex, subFragment);

			}
		} else {
			for (AbstractFragment formatFragment : list) {
				if (formatFragment instanceof AbstractContainerFragment) {
					if (((AbstractContainerFragment) formatFragment)
							.moveSubFragment(subFragment, delta)) {
						found = true;
						break;
					}
				}
			}
		}
		return found;
	}

	/**
	 * Deletes the fragment from a list of fragments. Returns true if it was
	 * found. Note that the list is directly edited by this method.
	 * 
	 * @param subFragment
	 * @param list
	 * @param delta
	 * @return
	 */
	public static boolean deleteFragmentFromList(AbstractFragment subFragment,
			List<AbstractFragment> list) {
		boolean found = false;
		int index = 0;
		for (AbstractFragment fragment : list) {
			if (fragment == subFragment) {
				found = true;
				break;
			}
			index++;
		}

		if (found) {
			if (index < list.size()) {
				list.remove(index);
			}
		} else {
			for (AbstractFragment formatFragment : list) {
				if (formatFragment instanceof AbstractContainerFragment) {
					if (((AbstractContainerFragment) formatFragment)
							.deleteSubFragment(subFragment)) {
						found = true;
						break;
					}
				}
			}
		}
		return found;
	}

	public static boolean insertFragmentIntoList(AbstractFragment subFragment,
			AbstractFragment previousFragment, List<AbstractFragment> list) {
		boolean found = false;
		int index = 0;

		// First check if we can just add it without going through the 
		// recursion spiel.
		if (previousFragment == null) {
			list.add(subFragment);
			return true;
		} else if (previousFragment instanceof AbstractContainerFragment) {
			((AbstractContainerFragment) previousFragment).subFragments
					.add(subFragment);
			return true;
		}

		for (AbstractFragment fragment : list) {
			if (fragment == previousFragment) {
				found = true;
				break;
			}
			index++;
		}

		if (found) {
			if (index < list.size()) {
				list.add(index + 1, subFragment);
			}
		} else {
			for (AbstractFragment formatFragment : list) {
				if (formatFragment instanceof AbstractContainerFragment) {
					if (((AbstractContainerFragment) formatFragment)
							.insertSubFragment(subFragment, previousFragment)) {
						found = true;
						break;
					}
				}
			}
		}
		return found;
	}
}
