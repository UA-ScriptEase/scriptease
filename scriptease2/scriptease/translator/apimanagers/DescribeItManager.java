package scriptease.translator.apimanagers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.ScriptIt;
import scriptease.util.BiHashMap;

/**
 * A manager that contains all of the DescribeIts in a LibraryModel mapped to
 * the StoryComponents that use it. One DescribeIt can be mapped to multiple
 * story components.<br>
 * <br>
 * StoryComponents are weakly referenced so that the map does not store
 * references to non-existent ones.
 * 
 * @author kschenk
 * 
 */
public class DescribeItManager {

	// Weakly referenced to prevent memory leaks
	private final BiHashMap<DescribeIt, Collection<WeakReference<StoryComponent>>> describeItMap;

	public DescribeItManager() {
		this.describeItMap = new BiHashMap<DescribeIt, Collection<WeakReference<StoryComponent>>>();
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
	 * You likely do not need to call this if you're creating a clone of a
	 * KnowIt, since {@link KnowIt#clone()} does this for you.
	 * 
	 * @param describeIt
	 */
	public void addDescribeIt(DescribeIt describeIt, StoryComponent component) {
		Collection<WeakReference<StoryComponent>> weakReferences;
		final Collection<StoryComponent> storyComponents;

		weakReferences = this.describeItMap.getValue(describeIt);
		storyComponents = new ArrayList<StoryComponent>();

		if (weakReferences == null)
			weakReferences = new ArrayList<WeakReference<StoryComponent>>();
		else {
			// We add all of our story components to an array list so we can
			// parse
			for (WeakReference<StoryComponent> ref : weakReferences) {
				storyComponents.add(ref.get());
			}
		}

		weakReferences.add(new WeakReference<StoryComponent>(component));

		this.describeItMap.put(describeIt, weakReferences);
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
		for (Entry<DescribeIt, Collection<WeakReference<StoryComponent>>> entry : this.describeItMap
				.getEntrySet()) {
			for (WeakReference<StoryComponent> ref : entry.getValue()) {
				final StoryComponent weakComponent;

				weakComponent = ref.get();

				if (weakComponent == component)
					return entry.getKey();
			}
		}

		return null;
	}

	/**
	 * Returns true if the node is the end node in a path.
	 * 
	 * @param node
	 * @return
	 */
	public boolean isEndNode(DescribeItNode node) {
		final DescribeIt describeIt = this.getDescribeItForNode(node);

		for (Collection<DescribeItNode> path : describeIt.getPaths()) {
			if (path.isEmpty())
				continue;

			final Iterator<DescribeItNode> it = path.iterator();

			DescribeItNode nodeInPath = it.next();
			while (it.hasNext()) {
				nodeInPath = it.next();
			}

			if (nodeInPath == node)
				return true;
		}

		return false;
	}

	/**
	 * Gets the DescribeIt that the DescribeItNode belongs to.
	 * 
	 * @param node
	 * @return
	 */
	public DescribeIt getDescribeItForNode(DescribeItNode node) {
		for (DescribeIt describeIt : this.describeItMap.getKeys()) {
			if (describeIt.getStartNode().getDescendants().contains(node))
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
		final ScriptIt initialBinding;
		final KnowIt knowIt;

		knowIt = new KnowIt(describeIt.getName(), describeIt.getTypes());

		initialBinding = describeIt.getScriptItForPath(describeIt
				.getShortestPath());

		if (initialBinding != null)
			knowIt.setBinding(initialBinding);

		return knowIt;
	}

	/**
	 * Searches through the describe its to find a script it with the same
	 * display text as the one passed in. Since this is based off display text,
	 * results may not be accurate. Be warned.
	 * 
	 * @param scriptIt
	 * @return
	 */
	public DescribeIt findDescribeItWithScriptIt(ScriptIt scriptIt) {
		final String name = scriptIt.getDisplayText();

		for (DescribeIt describeIt : this.getDescribeIts()) {
			for (ScriptIt describeItScriptIt : describeIt.getScriptIts()) {
				if (describeItScriptIt.getDisplayText().equals(name))
					return describeIt;
			}
		}

		return null;
	}
}
