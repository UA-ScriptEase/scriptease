package scriptease.translator.apimanagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.ScriptIt;
import scriptease.util.BiHashMap;

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

	private final BiHashMap<DescribeIt, Collection<StoryComponent>> describeItMap;

	public DescribeItManager() {
		this.describeItMap = new BiHashMap<DescribeIt, Collection<StoryComponent>>();
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
	 * Adds a DescribeIt to the map in addition to its attached StoryComponent.
	 * 
	 * @param describeIt
	 */
	public void addDescribeIt(DescribeIt describeIt, StoryComponent component) {
		Collection<StoryComponent> components;

		components = this.describeItMap.getValue(describeIt);

		if(components == null)
			components = new ArrayList<StoryComponent>();
		
		if (!components.contains(component))
			components.add(component);

		this.describeItMap.put(describeIt, components);
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
	 * Returns the DescribeIt mapped to the component.
	 * 
	 * @param component
	 * @return
	 */
	public DescribeIt getDescribeIt(StoryComponent component) {
		for(Entry<DescribeIt, Collection<StoryComponent>> entry : this.describeItMap.getEntrySet()) {
			if(entry.getValue().contains(component))
				return entry.getKey();
		}
		
		return null;
	}

	/**
	 * Returns the StoryComponent mapped to the DescribeIt.
	 * 
	 * @param describeIt
	 * @return
	 *//*
	public StoryComponent getStoryComponent(DescribeIt describeIt) {
		return this.describeItMap.getValue(describeIt);
	}*/

	/**
	 * Generates a blank KnowIt for the selected type.
	 * 
	 * @param describeIt
	 * @return
	 */
	public KnowIt createKnowItForDescribeIt(DescribeIt describeIt) {
		final ScriptIt initialBinding;
		final KnowIt knowIt;

		knowIt = new KnowIt(describeIt.getName(), describeIt.getTypes());

		initialBinding = describeIt.getScriptItForPath(describeIt
				.getShortestPath());

		if (initialBinding != null)
			knowIt.setBinding(initialBinding);

		return knowIt;
	}
}
