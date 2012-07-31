package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.CodeGenerationKeywordConstants;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

public class ScopeFragment extends AbstractContainerFragment {

	private String nameRef = "";
	private List<FormatFragment> subFragments;

	public ScopeFragment() {
		super("");
		this.nameRef = "";
		this.subFragments = new ArrayList<FormatFragment>();
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
			List<FormatFragment> subFragments) {
		super(data);
		this.nameRef = nameRef;
		this.subFragments = subFragments;
	}

	@Override
	public Collection<FormatFragment> getSubFragments() {
		return this.subFragments;
	}

	@Override
	public void setSubFragments(List<FormatFragment> subFragments) {
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
			return (FormatFragment.resolveFormat(this.subFragments, newContext));
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
		if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.SUBJECT.name()))
			return context.getSubject();
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.OWNER.name()))
			return context.getOwner();
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.ARGUMENT.name()))
			return context.getParameter(this.nameRef);
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.MAINCODEBLOCK.name()))
			return context.getMainCodeBlock();
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.SCRIPTIT.name()))
			return context.getScriptIt(this.nameRef);
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.ASKIT.name()))
			return context.getAskIt();
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.IFCHILD.name()))
			return context.getIfChild();
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.ELSECHILD.name()))
			return context.getElseChild();
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.BINDING.name()))
			return context.getBinding();
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.START.name()))
			return context.getStartPoint(); 
		else if (dataLabel.equalsIgnoreCase(CodeGenerationKeywordConstants.Scope.END.name()))
			return context.getEndPoint(); 
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
