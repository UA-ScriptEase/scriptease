package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.FragmentVisitor;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * This element represents a single line of code. LineFragment.resolve() calls
 * resolve() on each of the contained fragments, then adds a newline character
 * to the code.
 * 
 * @author jason
 * @author kschenk
 * 
 */
public class LineFragment extends AbstractContainerFragment {
	/**
	 * Constructor without FormatFragment list specified.
	 */
	public LineFragment() {
		this(new ArrayList<AbstractFragment>());
	}

	/**
	 * Constructor with FormatFragment list specified.
	 * 
	 * @param fragments
	 *            the child fragments
	 */
	public LineFragment(List<AbstractFragment> fragments) {
		super("", fragments);
	}

	@Override
	public String resolve(Context context) {
		super.resolve(context);
		final String lineBreak;

		lineBreak = context.getTranslator().getLanguageDictionary()
				.getLineBreak();

		String generated = context.getIndent();
		for (AbstractFragment fragment : this.subFragments) {
			generated += fragment.resolve(context);
		}

		return generated + lineBreak;
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
	public void process(FragmentVisitor visitor) {
		visitor.processLineFragment(this);
	}
}
