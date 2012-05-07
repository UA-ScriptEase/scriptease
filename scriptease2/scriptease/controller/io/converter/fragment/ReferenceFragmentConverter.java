package scriptease.controller.io.converter.fragment;

import scriptease.translator.codegenerator.code.fragments.AskItReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.KnowItReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.ReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.ScriptItReferenceFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ReferenceFragmentConverter implements Converter {
	private static final String ASK_IT_TAG = "askIt";
	private static final String KNOW_IT_TAG = "knowIt";
	private static final String SCRIPT_IT_TAG = "scriptIt";
	private static final String DATA_TAG = "data";
	private static final String REF_TAG = "ref";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ReferenceFragment reference = (ReferenceFragment) source;

		// Data Tag
		if (reference instanceof ScriptItReferenceFragment)
			writer.addAttribute(DATA_TAG, SCRIPT_IT_TAG);
		else if (reference instanceof KnowItReferenceFragment)
			writer.addAttribute(DATA_TAG, KNOW_IT_TAG);
		else if (reference instanceof AskItReferenceFragment)
			writer.addAttribute(DATA_TAG, ASK_IT_TAG);

		// Ref Tag
		writer.addAttribute(REF_TAG, reference.getDirectiveText());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data;
		final String ref;

		// Ref Tag
		ref = reader.getAttribute(REF_TAG);

		// Start vanilla
		ReferenceFragment reference = new ReferenceFragment(ref);

		// Data Tag
		data = reader.getAttribute(DATA_TAG);
		// If data is specified, switch the type of ReferenceFragment
		if (data != null && !data.isEmpty()) {
			if (data.equals(SCRIPT_IT_TAG))
				reference = new ScriptItReferenceFragment(ref);
			else if (data.equals(KNOW_IT_TAG))
				reference = new KnowItReferenceFragment(ref);
			else if (data.equals(ASK_IT_TAG))
				reference = new AskItReferenceFragment(ref);
		}

		return reference;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ReferenceFragment.class)
				|| type.equals(ScriptItReferenceFragment.class)
				|| type.equals(AskItReferenceFragment.class)
				|| type.equals(KnowItReferenceFragment.class);
	}
}
