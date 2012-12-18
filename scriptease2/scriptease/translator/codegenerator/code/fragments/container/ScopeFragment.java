package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.CodeGenerationKeywordConstants;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

public class ScopeFragment extends AbstractContainerFragment {

	private String nameRef = "";
	private List<AbstractFragment> subFragments;

	public ScopeFragment() {
		super("");
		this.nameRef = "";
		this.subFragments = new ArrayList<AbstractFragment>();
	}

	/**
	 * ScopeFragment is a fragment which narrows the context to the specified
	 * dataLabel. Works similarily to SeriesFragment except on a single object
	 * instead of a series of objects.
	 * 
	 * @param data
	 * @param subFragments
	 */
	public ScopeFragment(String data, String nameRef,
			List<AbstractFragment> subFragments) {
		super(data);
		this.nameRef = nameRef;
		this.subFragments = subFragments;
	}

	@Override
	public Collection<AbstractFragment> getSubFragments() {
		return this.subFragments;
	}

	@Override
	public void setSubFragments(List<AbstractFragment> subFragments) {
		this.subFragments = subFragments;
	}

	public void setNameRef(String nameRef) {
		this.nameRef = nameRef;
	}

	public String getNameRef() {
		return this.nameRef;
	}

	@Override
	public String resolve(Context context) {
		super.resolve(context);
		final Object scope;

		scope = this.getScope(context);

		if (scope != null) {
			Context newContext = ContextFactory.getInstance().createContext(
					context, scope);
			return (AbstractFragment.resolveFormat(this.subFragments,
					newContext));
		} else
			return "< Scope was unable to be resolved for data: "
					+ this.getDirectiveText() + " >";
	}

	/**
	 * Gets the Object on which the context should narrow.
	 * 
	 * @param context
	 * @return
	 */
	private Object getScope(Context context) {
		final String dataLabel = this.getDirectiveText();

		// IF+ELSE BLOCK (scope data= <dataLabel> )
		if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.SUBJECT
						.name()))
			return context.getSubject();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.OWNER
						.name()))
			return context.getOwner();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.ARGUMENT
						.name()))
			return context.getParameter(this.nameRef);
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.MAINCODEBLOCK
						.name()))
			return context.getMainCodeBlock();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.SCRIPTIT
						.name()))
			return context.getScriptIt(this.nameRef);
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.ASKIT
						.name()))
			return context.getAskIt();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.IFCHILD
						.name()))
			return context.getIfChild();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.ELSECHILD
						.name()))
			return context.getElseChild();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.BINDING
						.name()))
			return context.getBinding();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.START
						.name()))
			return context.getStartStoryPoint();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.ACTIVECHILD
						.name()))
			return context.getActiveChild();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.ScopeTypes.INACTIVECHILD
						.name()))
			return context.getInactiveChild();
		else {
			System.err.println("Unrecognizable Scope tag : " + dataLabel);
			return null;
		}
	}

	@Override
	public String toString() {
		return "ScopeFragment: " + this.getDirectiveText();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ScopeFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if (this.nameRef != null)
			hash += this.nameRef.hashCode();
		return hash + this.subFragments.hashCode();
	}
}
