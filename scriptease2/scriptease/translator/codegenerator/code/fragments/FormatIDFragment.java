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
public class FormatIDFragment extends FormatFragment {

	// The list of FormatFragment contained herein.
	private List<FormatFragment> subFragments = new ArrayList<FormatFragment>();

	public FormatIDFragment(String text, List<FormatFragment> children) {
		super(text);
		this.subFragments = new ArrayList<FormatFragment>(children);
	}

	public Collection<FormatFragment> getSubFragments() {
		return this.subFragments;
	}

	@Override
	public String toString() {
		return "FormatIDFragment [" + this.getDirectiveText()
				+ this.subFragments.toString() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormatIDFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.subFragments.hashCode();
	}
}
