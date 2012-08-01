package scriptease.translator.codegenerator.code.fragments.series;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.translator.codegenerator.CharacterRange;
import scriptease.translator.codegenerator.code.fragments.Fragment;

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
	private final boolean isUnique;

	/**
	 * See: {@link Fragment#FormatFragment(String, CharacterRange, char[])}
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
			List<Fragment> subFragments, String filter, String filterType, boolean isUnique) {
		super(data, separator, subFragments, filter, filterType);
		this.isUnique = isUnique;
	}

	public <T> Iterator<T> handle(Iterator<T> iterator) {
		if (this.isUnique)
			return iterator;
		else {
			Collection<T> unique = new CopyOnWriteArraySet<T>();
			while (iterator.hasNext()) {
				T object = iterator.next();

				unique.add(object);
			}
			return unique.iterator();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SeriesFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}
	
	public boolean isUnique() {
		return this.isUnique;
	}
}
