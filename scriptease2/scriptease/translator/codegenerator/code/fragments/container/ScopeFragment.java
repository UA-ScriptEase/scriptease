package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.AbstractFragmentVisitor;
import scriptease.translator.codegenerator.CodeGenerationConstants.ScopeType;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

public class ScopeFragment extends AbstractContainerFragment {
	private String nameRef = "";

	public ScopeFragment() {
		super("", new ArrayList<AbstractFragment>());
		this.nameRef = "";
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
		super(data, subFragments);
		this.nameRef = nameRef;
	}

	@Override
	public ScopeFragment clone() {
		final ScopeFragment clone = (ScopeFragment) super.clone();
		clone.setNameRef(this.nameRef);
		return clone;
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
		final Object scope = this.getScope(context);

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
		final ScopeType scope;

		final String directiveText = this.getDirectiveText();
		try {
			scope = ScopeType.valueOf(directiveText.toUpperCase());
		} catch (IllegalArgumentException e) {
			System.out.println("Couldn't find the value of : " + directiveText);
			return null;
		}

		// IF+ELSE BLOCK (scope data= <dataLabel> )
		switch (scope) {
		case SUBJECT:
			return context.getSubject();
		case TEMPLATEID:
			return context.getTemplateID();
		case OWNER:
			return context.getOwner();
		case ARGUMENT:
			return context.getParameter(this.nameRef);
		case SLOTPARAMETER:
			return context.getSlotParameter(this.nameRef);
		case MAINCODEBLOCK:
			return context.getMainCodeBlock();
		case SCRIPTIT:
			return context.getScriptIt(this.nameRef);
		case ASKIT:
			return context.getAskIt();
		case IFCHILD:
			return context.getIfChild();
		case ELSECHILD:
			return context.getElseChild();
		case BINDING:
			return context.getBinding();
		case START:
			return context.getStartStoryPoint();
		case CAUSE:
			return context.getCause();
		case FIRSTCAUSE:
			return context.getFirstCause();
		case AUDIO:
			return context.getAudio();
		case IMAGE:
			return context.getImage();
		default:
			System.err.println("Unrecognizable Scope tag : " + scope.name());
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

	@Override
	public void process(AbstractFragmentVisitor visitor) {
		visitor.processScopeFragment(this);
	}
}
