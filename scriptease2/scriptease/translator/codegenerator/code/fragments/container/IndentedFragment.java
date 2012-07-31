package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * This fragment wraps LineFragments which need to be presented at the same
 * indent level. IndentedFragment.resolve() adds 'indentCount'
 * 'indentCharacter's to the beginning of each line contained by the
 * IndentFragment.
 * 
 * @author jason
 * 
 */
public class IndentedFragment extends AbstractContainerFragment {

	// The list of FormatFragment contained herein.
	private List<FormatFragment> subFragments = new ArrayList<FormatFragment>();

	public IndentedFragment() {
		super("");
		this.subFragments = new ArrayList<FormatFragment>();
	}

	/**
	 * Constructor. Sets the sub fragments to the passed in list of Format
	 * Fragments.
	 * 
	 * @param indentChar
	 */
	public IndentedFragment(List<FormatFragment> subFragments) {
		super("");
		this.subFragments = new ArrayList<FormatFragment>(subFragments);
	}

	@Override
	public Collection<FormatFragment> getSubFragments() {
		return this.subFragments;
	}

	@Override
	public void setSubFragments(List<FormatFragment> subFragments) {
		this.subFragments = subFragments;
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
		String generated = "";
		// Get the indent size from the loaded LanguageDictionary
		String indent = TranslatorManager.getInstance().getActiveTranslator()
				.getLanguageDictionary().getIndent();
		context.increaseIndent(indent);

		for (FormatFragment fragment : this.subFragments) {
			generated += fragment.resolve(context);
		}

		context.reduceIndent(indent);
		return generated;
	}

	@Override
	public String toString() {
		return "IndentFragment [" + this.getDirectiveText()
				+ this.subFragments.toString() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IndentedFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.subFragments.hashCode();
	}
}
