package scriptease.translator.apimanagers;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;

/**
 * A manager that contains all of the DescribeIts in an APIDictionary.
 * 
 * @author kschenk
 * 
 */
public class DescribeItManager {

	private final Collection<DescribeIt> describeIts;

	public DescribeItManager() {
		this.describeIts = new ArrayList<DescribeIt>();
	}

	public Collection<DescribeIt> getDescribeIts() {
		return this.describeIts;
	}

	/**
	 * Add a describeIt to the collection of DescribeIts.
	 * 
	 * @param describeIt
	 */
	public void addDescribeIt(DescribeIt describeIt) {
		this.describeIts.add(describeIt);
	}

	/**
	 * Adds all describeIts to the collection in the manager.
	 * 
	 * @param describeIts
	 */
	public void addDescribeIts(Collection<DescribeIt> describeIts) {
		this.describeIts.addAll(describeIts);
	}

	/**
	 * Removes a describeIt from the collection of DescribeIts.
	 * 
	 * @param describeIt
	 */
	public void removeDescribeIt(DescribeIt describeIt) {
		this.describeIts.remove(describeIt);
	}

	/**
	 * Returns the first DescribeIt matching the passed in types. If no
	 * DescribeIt is found, returns null.
	 * 
	 * @param types
	 * @return
	 */
	public DescribeIt findDescribeItForTypes(Collection<String> types) {
		for (DescribeIt describeIt : this.describeIts) {
			final Collection<String> describeItTypes;

			describeItTypes = describeIt.getTypes();

			if (describeItTypes.containsAll(types)
					&& describeItTypes.size() == types.size())
				return describeIt;
		}

		return null;
	}

	/**
	 * Generates a blank KnowIt for the selected type.
	 * 
	 * @param describeIt
	 * @return
	 */
	public KnowIt createKnowItForDescribeIt(DescribeIt describeIt) {
		final Collection<String> types;

		types = describeIt.getTypes();

		String name = "";

		for (String type : types) {
			if (!name.isEmpty())
				name += ", ";
			name += type;
		}

		return new KnowIt(name, types);
	}
}
