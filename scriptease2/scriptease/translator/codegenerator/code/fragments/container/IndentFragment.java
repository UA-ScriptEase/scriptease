package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * This fragment wraps LineFragments which need to be presented at the same
 * indent level. IndentedFragment.resolve() adds 'indentCount'
 * 'indentCharacter's to the beginning of each line contained by the
 * IndentFragment.
 * 
 * @author jason
 * 
 */
public class IndentFragment extends AbstractContainerFragment {

	// The list of FormatFragment contained herein.
	private List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();

	public IndentFragment() {
		super("");
		this.subFragments = new ArrayList<AbstractFragment>();
	}

	/**
	 * Constructor. Sets the sub fragments to the passed in list of Format
	 * Fragments.
	 * 
	 * @param indentChar
	 */
	public IndentFragment(List<AbstractFragment> subFragments) {
		super("");
		this.subFragments = new ArrayList<AbstractFragment>(subFragments);
	}

	@Override
	public Collection<AbstractFragment> getSubFragments() {
		return this.subFragments;
	}

	@Override
	public void setSubFragments(List<AbstractFragment> subFragments) {
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

		for (AbstractFragment fragment : this.subFragments) {
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
		if (obj instanceof IndentFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.subFragments.hashCode();
	}
}
