package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
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

	public Collection<AbstractFragment> getSubFragments() {
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
}
