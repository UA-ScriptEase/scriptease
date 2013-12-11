package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.io.XMLAttribute;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ScopeFragmentConverter implements Converter {

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ScopeFragment scope = (ScopeFragment) source;
		final String nameRef = scope.getNameRef();

		XMLAttribute.DATA.write(writer, scope.getDirectiveText());
		// Ref Tag
		if (nameRef != null)
			XMLAttribute.REF.write(writer, nameRef);

		// Write Sub Fragments
		context.convertAnother(scope.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data = XMLAttribute.DATA.read(reader);
		final String ref = XMLAttribute.REF.read(reader);
		final List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();

		// Read Sub Fragments
		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<AbstractFragment>) context
					.convertAnother(null, ArrayList.class));
		}

		return new ScopeFragment(data, ref, subFragments);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ScopeFragment.class);
	}
}
