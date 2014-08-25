package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.FragmentVisitor;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

public class ConditionalFragment extends AbstractContainerFragment {

	public static enum Type {
		ISLASTTASK, HASMULTIPLECHILDREN
	}

	public ConditionalFragment(String conditional) {
		this(conditional, new ArrayList<AbstractFragment>());
	}

	/**
	 * Constructor with FormatFragment list specified.
	 * 
	 * @param fragments
	 *            the child fragments
	 */
	public ConditionalFragment(String conditional,
			List<AbstractFragment> fragments) {
		super(conditional, fragments);
	}

	@Override
	public String resolve(Context context) {
		super.resolve(context);

		boolean doIt = false;

		final String directiveText = this.getDirectiveText();

		boolean not = directiveText.startsWith("!");

		final Type data;

		try {
			if (not)
				data = Type.valueOf(directiveText.substring(1).toUpperCase());
			else
				data = Type.valueOf(directiveText.toUpperCase());
		} catch (IllegalArgumentException e) {
			System.out.println("Couldn't find the value of : " + directiveText);
			return null;
		}

		try {
			switch (data) {
			case ISLASTTASK:
				doIt = context.isLastTask();
				break;
			case HASMULTIPLECHILDREN:
				doIt = context.hasMultipleChildren();
				break;
			default:
				throw new CodeGenerationException(
						"Simple Data Fragment was unable to be resolved for data: "
								+ directiveText + ">");
			}
		} catch (CodeGenerationException e) {
			return "Error when inserting new simple fragment: " + directiveText
					+ " with message: " + e.getMessage();
		}

		// Do it or not!
		if ((not && !doIt) || (!not && doIt)) {
			String generated = context.getIndent();
			for (AbstractFragment fragment : this.subFragments) {
				generated += fragment.resolve(context);
			}

			return generated;
		}

		return "";
	}

	@Override
	public String toString() {
		return this.subFragments.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConditionalFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public void process(FragmentVisitor visitor) {

	}

}
