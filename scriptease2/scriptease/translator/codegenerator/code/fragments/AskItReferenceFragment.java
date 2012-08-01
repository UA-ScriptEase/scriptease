package scriptease.translator.codegenerator.code.fragments;

import scriptease.translator.codegenerator.code.contexts.AskItContext;
import scriptease.translator.codegenerator.code.contexts.Context;

public class AskItReferenceFragment extends FormatReferenceFragment {

	public AskItReferenceFragment(String text) {
		super(text);
	}

	@Override
	public String resolve(Context context) {
		if (context instanceof AskItContext)
			return super.resolve(context);
		else
			return "";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AskItReferenceFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}
}
