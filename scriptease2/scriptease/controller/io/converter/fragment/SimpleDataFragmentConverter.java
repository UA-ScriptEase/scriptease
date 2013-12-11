package scriptease.controller.io.converter.fragment;

import java.util.regex.Pattern;

import scriptease.controller.io.XMLAttribute;
import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;
import scriptease.util.StringOp;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SimpleDataFragmentConverter implements Converter {

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final SimpleDataFragment simple = (SimpleDataFragment) source;
		final Pattern legalRange = Pattern.compile(simple.getLegalRange());
		final String defaultText = simple.getDefaultText();

		XMLAttribute.DATA.write(writer, simple.getDirectiveText());

		if (legalRange != null && !legalRange.toString().isEmpty())
			XMLAttribute.LEGALVALUES.write(writer, legalRange.toString());

		if (StringOp.exists(defaultText)) {
			XMLAttribute.DEFAULT.write(writer, defaultText);
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data = XMLAttribute.DATA.read(reader);
		final String pattern = XMLAttribute.LEGALVALUES.read(reader);
		final String defaultText = XMLAttribute.DEFAULT.read(reader);

		return new SimpleDataFragment(data, pattern, defaultText);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(SimpleDataFragment.class);
	}
}
