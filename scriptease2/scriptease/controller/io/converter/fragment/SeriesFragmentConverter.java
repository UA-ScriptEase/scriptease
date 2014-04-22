package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.io.XMLAttribute;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;
import scriptease.util.StringOp;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SeriesFragmentConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final SeriesFragment series = (SeriesFragment) source;
		final String filterType = series.getSeriesFilter().getType().toString();
		final String filterValue = series.getSeriesFilter().getValue();
		final String separator = series.getSeparator();

		XMLAttribute.DATA.write(writer, series.getDirectiveText());

		if (series.isUnique())
			XMLAttribute.UNIQUE.write(writer, "true");

		if (StringOp.exists(filterType)
				&& !filterType
						.equals(SeriesFragment.FilterType.NONE.toString()))
			XMLAttribute.FILTERBY.write(writer, filterType);

		if (StringOp.exists(filterValue))
			XMLAttribute.FILTER.write(writer, filterValue);

		if (StringOp.exists(separator))
			XMLAttribute.SEPARATOR.write(writer, separator);

		// Write Sub Fragments
		context.convertAnother(series.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data = XMLAttribute.DATA.read(reader);
		final String uniqueString = XMLAttribute.UNIQUE.read(reader);
		final String filterTypeStr = XMLAttribute.FILTERBY.read(reader);
		final String filter = XMLAttribute.FILTER.read(reader);
		final String separator = XMLAttribute.SEPARATOR.read(reader);

		final boolean isUnique;
		final SeriesFragment.FilterType filterType;
		final List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();

		if (StringOp.exists(uniqueString)
				&& uniqueString.equalsIgnoreCase("true"))
			isUnique = true;
		else
			isUnique = false;

		if (StringOp.exists(filterTypeStr))
			filterType = SeriesFragment.FilterType.valueOf(filterTypeStr
					.toUpperCase());
		else
			filterType = SeriesFragment.FilterType.NONE;

		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<AbstractFragment>) context
					.convertAnother(null, ArrayList.class));
		}

		return new SeriesFragment(data, separator, subFragments, filter,
				filterType, isUnique);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(SeriesFragment.class);
	}
}
