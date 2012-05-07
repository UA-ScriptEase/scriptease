package scriptease.translator.codegenerator.code.fragments;

import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.TranslatorKeywordManager;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;

public class ScopeFragment extends FormatFragment {

	private String nameRef = "";
	private List<FormatFragment> subFragments;

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

	public Collection<FormatFragment> getSubFragments() {
		return this.subFragments;
	}

	public void setSubFragments(List<FormatFragment> subFragments) {
		this.subFragments = subFragments;
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
		if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_SUBJECT))
			return context.getSubject();
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_OWNER_SCOPE))
			return context.getOwner();
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_ARGUMENT))
			return context.getParameter(this.nameRef);
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.MAIN_CODEBLOCK))
			return context.getMainCodeBlock();
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_SCRIPTIT))
			return context.getScriptIt(this.nameRef);
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_ASKIT))
			return context.getAskIt();
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_IFCHILD_SCOPE))
			return context.getIfChild();
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_ELSECHILD_SCOPE))
			return context.getElseChild();
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_BINDING))
			return context.getBinding();
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_START))
			return context.getStartPoint(); 
		else if (dataLabel.equalsIgnoreCase(TranslatorKeywordManager.XML_END))
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
