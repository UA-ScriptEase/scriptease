package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.FragmentVisitor;
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

	public IndentFragment() {
		this(new ArrayList<AbstractFragment>());
	}

	/**
	 * Constructor. Sets the sub fragments to the passed in list of Format
	 * Fragments.
	 * 
	 * @param indentChar
	 */
	public IndentFragment(List<AbstractFragment> subFragments) {
		super("", subFragments);
	}

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
	
	@Override
	public void process(FragmentVisitor visitor) {
		visitor.processIndentFragment(this);
	}
}
