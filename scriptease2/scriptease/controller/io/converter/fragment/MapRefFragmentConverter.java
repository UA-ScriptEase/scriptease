package scriptease.controller.io.converter.fragment;

import scriptease.translator.codegenerator.code.fragments.MapRefFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MapRefFragmentConverter implements Converter {
	private static final String DATA_TAG = "data";
	private static final String REF_TAG = "ref";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final MapRefFragment reference = (MapRefFragment) source;

		// Data Tag
		writer.addAttribute(DATA_TAG, reference.getDirectiveText());
		// Ref Tag
		writer.addAttribute(REF_TAG, reference.getRef());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data;
		final String ref;
		// Data Tag
		data = reader.getAttribute(DATA_TAG);
		// Ref Tag
		ref = reader.getAttribute(REF_TAG);

		MapRefFragment reference = new MapRefFragment(data, ref);
		return reference;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(MapRefFragment.class);
	}
}
