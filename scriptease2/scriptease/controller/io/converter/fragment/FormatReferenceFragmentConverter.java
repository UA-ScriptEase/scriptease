package scriptease.controller.io.converter.fragment;

import scriptease.controller.io.XMLAttribute;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;
import scriptease.util.StringOp;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FormatReferenceFragmentConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final FormatReferenceFragment reference = (FormatReferenceFragment) source;

		XMLAttribute.DATA.write(writer, reference.getType().name());
		XMLAttribute.REF.write(writer, reference.getDirectiveText());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String ref = XMLAttribute.REF.read(reader);
		final String data = XMLAttribute.DATA.read(reader);

		final FormatReferenceFragment reference;

		if (StringOp.exists(data)) {
			reference = new FormatReferenceFragment(ref,
					FormatReferenceFragment.Type.valueOf(data.toUpperCase()));
		} else
			reference = new FormatReferenceFragment(ref);

		return reference;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(FormatReferenceFragment.class);
	}
}
