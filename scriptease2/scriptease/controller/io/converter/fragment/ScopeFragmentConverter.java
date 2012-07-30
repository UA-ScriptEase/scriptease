package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ScopeFragmentConverter implements Converter {
	private static final String DATA_TAG = "data";
	private static final String REF_TAG = "ref";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ScopeFragment scope = (ScopeFragment) source;

		// Data Tag
		writer.addAttribute(DATA_TAG, scope.getDirectiveText());

		// Ref Tag
		final String nameRef = scope.getNameRef();
		if (nameRef != null)
		writer.addAttribute(REF_TAG, nameRef);

		// Write Sub Fragments
		context.convertAnother(scope.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data;
		final String ref;
		final List<FormatFragment> subFragments = new ArrayList<FormatFragment>();
		ScopeFragment scope = null;

		// Data Tag
		data = reader.getAttribute(DATA_TAG);

		// Ref Tag
		ref = reader.getAttribute(REF_TAG);

		// Read Sub Fragments
		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<FormatFragment>) context.convertAnother(
					scope, ArrayList.class));
		}

		scope = new ScopeFragment(data, ref, subFragments);
		return scope;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ScopeFragment.class);
	}
}
