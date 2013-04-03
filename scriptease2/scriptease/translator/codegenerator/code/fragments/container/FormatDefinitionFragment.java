package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * FormatFragment used to store a format in the Language Dictionary.
 * 
 * @author mfchurch
 * 
 */
public class FormatDefinitionFragment extends AbstractContainerFragment {

	// The list of FormatFragment contained herein.
	private List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();

	public FormatDefinitionFragment(String text, List<AbstractFragment> children) {
		super(text);
		this.subFragments = new ArrayList<AbstractFragment>(children);
	}

	@Override
	public void setSubFragments(List<AbstractFragment> subFragments) {
		this.subFragments = subFragments;
	}

	public Collection<AbstractFragment> getSubFragments() {
		return this.subFragments;
	}

	@Override
	public String toString() {
		return "FormatIDFragment [" + this.getDirectiveText()
				+ this.subFragments.toString() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormatDefinitionFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.subFragments.hashCode();
	}
}
