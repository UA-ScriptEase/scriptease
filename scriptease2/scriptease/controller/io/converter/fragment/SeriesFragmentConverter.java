package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.series.AbstractSeriesFragment;
import scriptease.translator.codegenerator.code.fragments.series.SeriesFilter.FilterBy;
import scriptease.translator.codegenerator.code.fragments.series.SeriesFragment;
import scriptease.translator.codegenerator.code.fragments.series.UniqueSeriesFragment;

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
		final AbstractSeriesFragment series = (AbstractSeriesFragment) source;

		// Data Tag
		writer.addAttribute(DATA_TAG, series.getDirectiveText());

		// Unique Tag
		if (series instanceof UniqueSeriesFragment)
			writer.addAttribute(UNIQUE_TAG, "true");

		// FilterBy Tag
		String filterType = series.getFilter().getType().toString();
		if (filterType != null && !filterType.equals(FilterBy.NONE.toString()))
			writer.addAttribute(FILTER_BY_TAG, filterType);

		// Filter Tag
		String filterValue = series.getFilter().getValue();
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
		String isUnique;
		String filterBy;
		String filter;
		List<FormatFragment> subFragments = new ArrayList<FormatFragment>();
		String separator = null;
		AbstractSeriesFragment series = null;

		// Data Tag
		data = reader.getAttribute(DATA_TAG);
		// Unique Tag
		isUnique = reader.getAttribute(UNIQUE_TAG);
		// FilterBy Tag
		if (isUnique == null)
			isUnique = "false";
		filterBy = reader.getAttribute(FILTER_BY_TAG);
		if (filterBy == null)
			filterBy = "";
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
			subFragments.addAll((List<FormatFragment>) context.convertAnother(
					series, ArrayList.class));
		}

		if (isUnique.equalsIgnoreCase("true"))
			series = new UniqueSeriesFragment(data, separator, subFragments,
					filter, filterBy);
		else
			series = new SeriesFragment(data, separator, subFragments, filter,
					filterBy);

		return series;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(SeriesFragment.class)
				|| type.equals(UniqueSeriesFragment.class);
	}
}
