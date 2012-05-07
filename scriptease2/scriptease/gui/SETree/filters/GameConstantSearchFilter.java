package scriptease.gui.SETree.filters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.translator.io.model.GameConstant;

public class GameConstantSearchFilter extends GameConstantFilter {
	final private Map<GameConstant, Integer> keyCountPerComponent;
	private String searchText;

	public GameConstantSearchFilter(String searchText) {
		this.searchText = searchText;
		this.keyCountPerComponent = new HashMap<GameConstant, Integer>();
	}

	@Override
	public void addRule(Filter newFilter) {
		if (newFilter instanceof GameConstantSearchFilter) {
			this.searchText = ((GameConstantSearchFilter) newFilter).searchText;
			this.keyCountPerComponent.clear();
		} else
			super.addRule(newFilter);
	}

	/**
	 * Counts the number of key matches for each component and returns a Map of
	 * components with counts greater than zero
	 * 
	 * @param panels
	 * @param key
	 * @return
	 */
	private int search(GameConstant object, String key) {
		final Collection<String> searchableData;
		Integer count = 1;

		if (key != null && !key.trim().isEmpty()) {
			searchableData = getSearchDataForGameObject(object);
			count = SearchFilterHelper.countKeyMatches(searchableData, key);
		}

		return count;
	}

	/**
	 * Creates a Collection of String tokens which can be compared with the key
	 * 
	 * @param component
	 * @return
	 */
	private Collection<String> getSearchDataForGameObject(
			GameConstant gameObject) {
		Collection<String> searchData = new CopyOnWriteArraySet<String>();
		searchData.add(gameObject.getName());
		for (String type : gameObject.getTypes()) {
			searchData.add(type);
		}
		return searchData;
	}

	@Override
	protected int getMatchCount(GameConstant object) {
		return this.search(object, searchText);
	}
}
