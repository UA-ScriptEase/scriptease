package scriptease.translator.apimanagers;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.ScriptIt;
import scriptease.util.BiHashMap;
import scriptease.util.StringOp;

/**
 * A manager that contains all of the DescribeIts in an APIDictionary mapped to
 * the StoryComponent that uses it.
 * 
 * We may have to turn
 * 
 * @author kschenk
 * 
 */
public class DescribeItManager {

	private final BiHashMap<DescribeIt, StoryComponent> describeItMap;

	public DescribeItManager() {
		this.describeItMap = new BiHashMap<DescribeIt, StoryComponent>();
	}

	/**
	 * Returns all DescribeIts in the DescribeItManager.
	 * 
	 * @return
	 */
	public Collection<DescribeIt> getDescribeIts() {
		return this.describeItMap.getKeys();
	}

	/**
	 * Returns all StoryComponents with associated DescribeIts.
	 * 
	 * @return
	 */
	public Collection<StoryComponent> getStoryComponents() {
		return this.describeItMap.getValues();
	}

	/**
	 * Adds a DescribeIt to the map in addition to its attached StoryComponent.
	 * 
	 * @param describeIt
	 */
	public void addDescribeIt(DescribeIt describeIt, StoryComponent component) {
		this.describeItMap.put(describeIt, component);
	}

	/**
	 * Removes a describeIt from the DescribeIt to StoryComponent map.
	 * 
	 * @param describeIt
	 */
	public void removeDescribeIt(DescribeIt describeIt) {
		this.describeItMap.removeKey(describeIt);
	}

	/**
	 * Removes a StoryComponent from the DescribeIt to StoryComponent map.
	 */
	public void removeStoryComponent(StoryComponent component) {
		this.describeItMap.removeValue(component);
	}

	/**
	 * Returns the DescribeIt mapped to the component.
	 * 
	 * @param component
	 * @return
	 */
	public DescribeIt getDescribeIt(StoryComponent component) {
		return this.describeItMap.getKey(component);
	}

	/**
	 * Returns the StoryComponent mapped to the DescribeIt.
	 * 
	 * @param describeIt
	 * @return
	 */
	public StoryComponent getStoryComponent(DescribeIt describeIt) {
		return this.describeItMap.getValue(describeIt);
	}

	/**
	 * Generates a blank KnowIt for the selected type.
	 * 
	 * @param describeIt
	 * @return
	 */
	public KnowIt createKnowItForDescribeIt(DescribeIt describeIt) {
		final Collection<String> types;
		final Collection<String> properCaseTypes;
		final String name;
		final ScriptIt initialBinding;
		final KnowIt knowIt;

		properCaseTypes = new ArrayList<String>();
		types = describeIt.getTypes();

		for (String type : types) {
			properCaseTypes.add(StringOp.toProperCase(type));
		}

		name = StringOp.getCollectionAsString(properCaseTypes, ", ");
		knowIt = new KnowIt(name, types);
		initialBinding = describeIt.getScriptItForPath(describeIt
				.getShortestPath());

		if (initialBinding != null)
			knowIt.setBinding(initialBinding);

		return knowIt;
	}
}
