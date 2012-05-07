package scriptease.controller.io.converter.fragment;

import scriptease.translator.codegenerator.code.fragments.LiteralFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LiteralFragmentConverter implements Converter {

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LiteralFragment literal = (LiteralFragment) source;
		writer.setValue(literal.getDirectiveText());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final LiteralFragment literal;
		final String text = reader.getValue();

		literal = new LiteralFragment(text);
		return literal;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LiteralFragment.class);
	}
}
