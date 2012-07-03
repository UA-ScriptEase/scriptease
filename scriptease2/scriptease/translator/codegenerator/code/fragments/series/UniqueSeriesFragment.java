package scriptease.translator.codegenerator.code.fragments.series;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import sun.awt.util.IdentityArrayList;

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

	public <T> Iterator<T> handle(Iterator<T> iterator) {
		Collection<T> unique = new IdentityArrayList<T>();
		while (iterator.hasNext()) {
			T object = iterator.next();
			
			// RIGHT HERE!!!! W00T W00T W00T
			if (!unique.contains(object)) {
				unique.add(object);
			} 
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
