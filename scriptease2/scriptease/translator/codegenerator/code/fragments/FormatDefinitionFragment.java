package scriptease.translator.codegenerator.code.fragments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * FormatFragment used to store a format in the Language Dictionary.
 * 
 * @author mfchurch
 * 
 */
public class FormatDefinitionFragment extends Fragment {

	// The list of FormatFragment contained herein.
	private List<Fragment> subFragments = new ArrayList<Fragment>();

	public FormatDefinitionFragment(String text, List<Fragment> children) {
		super(text);
		this.subFragments = new ArrayList<Fragment>(children);
	}

	public Collection<Fragment> getSubFragments() {
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
