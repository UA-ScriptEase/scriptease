package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import sun.awt.util.IdentityArrayList;

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
	public boolean moveSubFragment(final AbstractFragment subFragment, int delta) {
		boolean found = false;
		int index = 0;
		for (AbstractFragment fragment : this.subFragments) {
			if (fragment == subFragment) {
				found = true;
				break;
			}
			index++;
		}

		if (found) {
			final int newIndex = index + delta;
			
			if (newIndex >= 0 && newIndex < this.subFragments.size()) {
				this.subFragments.remove(index);
				this.subFragments.add(newIndex, subFragment);

			}
			return true;
		} else {
			for (AbstractFragment formatFragment : this.subFragments) {
				if (formatFragment instanceof AbstractContainerFragment) {
					if (((AbstractContainerFragment) formatFragment)
							.moveSubFragment(subFragment, delta))
						break;
				}
			}
		}

		return false;
	}

}
