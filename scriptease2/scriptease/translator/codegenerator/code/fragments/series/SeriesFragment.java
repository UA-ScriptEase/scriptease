package scriptease.translator.codegenerator.code.fragments.series;

import java.util.Iterator;
import java.util.List;

import scriptease.translator.codegenerator.CharacterRange;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * SeriesFragments is a template subclass of AbstractSeriesFragment, meaning
 * that most of the implementation is in AbstractSeriesFragment. It's sibling
 * class UniqueSeriesFragments handles unique SeriesFragments where general
 * SeriesFragments cases are handled here. Unique is specified in the
 * languageDictionary
 * 
 * @see AbstractSeriesFragment
 * @see SeriesFragment
 * @see languageDictionary.xml
 * @author mfchurch, jtduncan
 * 
 */
public class SeriesFragment extends AbstractSeriesFragment {

	/**
	 * See:
	 * {@link FormatFragment#FormatFragment(String, CharacterRange, char[])}
	 * 
	 * @param data
	 *            The specific data list label.
	 * @param formatLabel
	 *            The format's label to be used in looking up the format from
	 *            the interpreter.
	 * @param separator
	 *            The string that is inserted in between each element in the
	 *            list during resolution.
	 */
	public SeriesFragment(String data, String separator,
			List<FormatFragment> subFragments, String filter, String filterType) {
		super(data, separator, subFragments, filter, filterType);
	}

	public <T> Iterator<T> handle(Iterator<T> iterator) {
		return iterator;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SeriesFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}
}
