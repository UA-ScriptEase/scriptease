package scriptease.translator.codegenerator.code.fragments;

import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.KnowItContext;

public class KnowItReferenceFragment extends FormatReferenceFragment {

	public KnowItReferenceFragment(String text) {
		super(text);
	}

	@Override
	public String resolve(Context context) {
		if (context instanceof KnowItContext)
			return super.resolve(context);
		else
			return "";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KnowItReferenceFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}
}
