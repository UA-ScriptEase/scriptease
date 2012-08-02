package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.SeriesFilterType;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SeriesFragmentConverter implements Converter {
	private static final String DATA_TAG = "data";
	private static final String UNIQUE_TAG = "unique";
	private static final String FILTER_TAG = "filter";
	private static final String FILTER_BY_TAG = "filterBy";
	private static final String SEPARATOR_TAG = "separator";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final SeriesFragment series = (SeriesFragment) source;

		// Data Tag
		writer.addAttribute(DATA_TAG, series.getDirectiveText());

		// Unique Tag
		if (series.isUnique())
			writer.addAttribute(UNIQUE_TAG, "true");

		// FilterBy Tag
		String filterType = series.getSeriesFilter().getType().toString();
		if (filterType != null
				&& !filterType.equals(SeriesFilterType.NONE.toString()))
			writer.addAttribute(FILTER_BY_TAG, filterType);

		// Filter Tag
		String filterValue = series.getSeriesFilter().getValue();
		if (filterValue != null && !filterValue.isEmpty())
			writer.addAttribute(FILTER_TAG, filterValue);

		// Separator Tag
		String separator = series.getSeparator();
		if (separator != null && !separator.isEmpty()) {
			writer.addAttribute(SEPARATOR_TAG, separator);
		}

		// Write Sub Fragments
		context.convertAnother(series.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data;
		String uniqueString;
		String filterTypeString;
		String filter;
		List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();
		String separator = null;
		SeriesFragment series = null;

		// Data Tag
		data = reader.getAttribute(DATA_TAG);
		// Unique Tag
		uniqueString = reader.getAttribute(UNIQUE_TAG);
		// FilterBy Tag
		if (uniqueString == null)
			uniqueString = "false";
		filterTypeString = reader.getAttribute(FILTER_BY_TAG);
		if (filterTypeString == null)
			filterTypeString = "";
		// Filter Tag
		filter = reader.getAttribute(FILTER_TAG);
		if (filter == null)
			filter = "";

		separator = reader.getAttribute(SEPARATOR_TAG);
		if (separator == null)
			separator = "";

		// Separator Tag
		if (reader.hasMoreChildren()) {
			// Read Sub Fragments
			subFragments.addAll((List<AbstractFragment>) context
					.convertAnother(series, ArrayList.class));
		}

		boolean isUnique;

		if (uniqueString.equalsIgnoreCase("true"))
			isUnique = true;
		else
			isUnique = false;

		SeriesFilterType filterType;

		if (filterTypeString.equalsIgnoreCase(SeriesFilterType.NAME.name()))
			filterType = SeriesFilterType.NAME;
		else if (filterTypeString
				.equalsIgnoreCase(SeriesFilterType.SLOT.name()))
			filterType = SeriesFilterType.SLOT;
		else
			filterType = SeriesFilterType.NONE;

		series = new SeriesFragment(data, separator, subFragments, filter,
				filterType, isUnique);

		return series;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(SeriesFragment.class);
	}
}
