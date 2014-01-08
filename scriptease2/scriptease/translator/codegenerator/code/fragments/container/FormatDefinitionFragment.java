package scriptease.translator.codegenerator.code.fragments.container;

import java.util.List;

import scriptease.controller.FragmentVisitor;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * FormatFragment used to store a format in the Language Dictionary. TODO
 * mfchurch candidate for deletion. Is this class necessary?
 * 
 * @author mfchurch
 */
public class FormatDefinitionFragment extends AbstractContainerFragment {

	public FormatDefinitionFragment(String text, List<AbstractFragment> children) {
		super(text, children);
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
	public void process(FragmentVisitor visitor) {
		visitor.processFormatDefinitionFragment(this);
	}
}
