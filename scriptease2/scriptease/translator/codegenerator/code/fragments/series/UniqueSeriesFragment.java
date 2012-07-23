package scriptease.translator.codegenerator.code.fragments.series;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * UniqueSeriesFragment is a template subclass of AbstractSeriesFragment,
 * meaning that most of the implementation is in AbstractSeriesFragment. It's
 * sibling class SeriesFragment handles general SeriesFragments where
 * UniqueSeriesFragments cases are handled here. Unique is specified in the
 * languageDictionary
 * 
 * @see AbstractSeriesFragment
 * @see SeriesFragment
 * @see languageDictionary.xml
 * @author mfchurch, jtduncan
 * 
 */
public class UniqueSeriesFragment extends AbstractSeriesFragment {

	public UniqueSeriesFragment(String data, String separator,
			List<FormatFragment> subFragments, String filter, String filterType) {
		super(data, separator, subFragments, filter, filterType);
	}

	/**
	 * Returns an iterator that only contains one of each item from the passed
	 * in iterator. It "Uniquifies" the passed iterator.
	 * 
	 * @param iterator
	 *            The iterator to be uniquified.
	 * @return The unique iterator.
	 */
	public <T> Iterator<T> handle(Iterator<T> iterator) {
		Collection<T> unique = new CopyOnWriteArraySet<T>();
		while (iterator.hasNext()) {
			T object = iterator.next();
			
			unique.add(object);
		}
		return unique.iterator();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UniqueSeriesFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}
}
