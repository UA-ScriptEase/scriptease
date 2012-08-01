package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.Fragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentedFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class IndentedFragmentConverter implements Converter {

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final IndentedFragment indented = (IndentedFragment) source;

		// Write sub fragments
		context.convertAnother(indented.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final List<Fragment> subFragments;
		IndentedFragment indented = null;

		// Read sub fragments
		subFragments = new ArrayList<Fragment>();
		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<Fragment>) context.convertAnother(
					indented, ArrayList.class));
		}

		indented = new IndentedFragment(subFragments);
		return indented;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(IndentedFragment.class);
	}
}
