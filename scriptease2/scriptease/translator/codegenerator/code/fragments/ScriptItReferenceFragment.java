package scriptease.translator.codegenerator.code.fragments;

import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ScriptItContext;

public class ScriptItReferenceFragment extends FormatReferenceFragment {

	public ScriptItReferenceFragment(String text) {
		super(text);
	}

	@Override
	public String resolve(Context context) {
		if (context instanceof ScriptItContext)
			return super.resolve(context);
		else
			return "";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ScriptItReferenceFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}
}
