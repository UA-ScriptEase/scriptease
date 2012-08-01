package scriptease.controller.io.converter.fragment;

import java.util.regex.Pattern;

import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SimpleFragmentConverter implements Converter {
	private static final String LEGAL_FORMAT_TAG = "legalValues";
	private static final String DATA_TAG = "data";
	private static final String DEFAULT_TAG = "default";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final SimpleDataFragment simple = (SimpleDataFragment) source;

		// Data Tag
		writer.addAttribute(DATA_TAG, simple.getDirectiveText());

		// Legal Format Tag
		Pattern legalRange = Pattern.compile(simple.getLegalRange());
				//simple.getLegalRange();
		if (legalRange != null && !legalRange.toString().isEmpty())
			writer.addAttribute(LEGAL_FORMAT_TAG, legalRange.toString());

		// Default Tag
		String defaultText = simple.getDefaultText();
		if (defaultText != null && !defaultText.isEmpty()) {
			writer.addAttribute(DEFAULT_TAG, defaultText);
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final SimpleDataFragment simple;
		final String data;
		String pattern;
		String defaultText;

		// Data Tag
		data = reader.getAttribute(DATA_TAG);
		
		// Legal Format Tag
		pattern = reader.getAttribute(LEGAL_FORMAT_TAG);
		if (pattern == null)
			pattern = "";
		
		// Default Text Tag
		defaultText = reader.getAttribute(DEFAULT_TAG);

		simple = new SimpleDataFragment(data, pattern);

		if (defaultText != null && !defaultText.isEmpty()) {
			simple.setDefaultText(defaultText);
		}
		
		return simple;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(SimpleDataFragment.class);
	}
}
