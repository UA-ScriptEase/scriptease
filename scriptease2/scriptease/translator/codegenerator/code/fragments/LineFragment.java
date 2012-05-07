package scriptease.translator.codegenerator.code.fragments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * This element represents a single line of code. LineFragment.resolve() calls
 * resolve() on each of the contained fragments, then adds a newline character
 * to the code.
 * 
 * @author jason
 * 
 */
public class LineFragment extends FormatFragment {

	// These are the fragments which are contained within this line.
	private List<FormatFragment> subFragments;

	// This is a string because a new line is not necessarily expressable as a
	// single character. This is the case, for example, with HTML, which uses
	// '<br />'.
	private String newLineChar;

	/**
	 * Constructor with FormatFragment list specified.
	 * 
	 * @param nlChar
	 *            the character to mark a new line.
	 * @param fragments
	 *            the child fragments
	 */
	public LineFragment(String nlChar, List<FormatFragment> fragments) {
		super("");
		this.newLineChar = nlChar;
		this.subFragments = new ArrayList<FormatFragment>(fragments);
	}

	public Collection<FormatFragment> getSubFragments() {
		return this.subFragments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * scriptease.translator.codegenerator.code.FormatFragment#resolve(scriptease
	 * .translator.codegenerator.code.CodeGenerationContext)
	 */
	@Override
	public String resolve(Context context) {
		super.resolve(context);
		String generated = context.getIndent();
		for (FormatFragment fragment : this.subFragments) {
			generated += fragment.resolve(context);
		}
		return generated + this.newLineChar;
	}

	@Override
	public String toString() {
		return this.subFragments.toString() + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LineFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.subFragments.hashCode()
				+ this.newLineChar.hashCode();
	}
}
