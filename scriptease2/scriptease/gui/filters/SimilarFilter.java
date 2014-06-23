package scriptease.gui.filters;

import scriptease.model.StoryComponent;
import scriptease.model.semodel.librarymodel.LibraryModel;

public class SimilarFilter extends StoryComponentFilter {
	public enum SimilarFilterType {
		NONE, SIMILAR_WITHIN, SIMILAR_BETWEEN, DIFFERENCE
	}

	private SimilarFilterType type;
	private LibraryModel library;

	public static SimilarFilter buildEmptySimilarFilter() {
		return new SimilarFilter(null, SimilarFilterType.NONE);
	}

	public static SimilarFilter buildSimilarWithinFilter(LibraryModel library) {
		return new SimilarFilter(library, SimilarFilterType.SIMILAR_WITHIN);
	}

	public static SimilarFilter buildSimilarBetweenFilter(LibraryModel library) {
		return new SimilarFilter(library, SimilarFilterType.SIMILAR_BETWEEN);
	}

	public static SimilarFilter buildDifferenceFilter(LibraryModel library) {
		return new SimilarFilter(library, SimilarFilterType.DIFFERENCE);
	}

	private SimilarFilter(LibraryModel library, SimilarFilterType type) {
		this.library = library;
		this.type = type;
	}

	@Override
	public void addRule(Filter newFilter) {
		if (newFilter instanceof SimilarFilter) {
			final SimilarFilter filter = (SimilarFilter) newFilter;

			this.library = filter.library;
			this.type = filter.type;
		} else
			super.addRule(newFilter);
	}

	@Override
	protected int getMatchCount(StoryComponent element) {
		int matchCount = 0;

		switch (this.type) {
		case SIMILAR_WITHIN:
			matchCount = -1;
		case SIMILAR_BETWEEN:
			for (StoryComponent comp : this.library.getAllStoryComponents()) {
				if (comp.getDisplayText().equals(element.getDisplayText())) {
					matchCount++;
				}
			}
			break;
		case DIFFERENCE:
			matchCount = 1;
			for (StoryComponent comp : this.library.getAllStoryComponents()) {
				if (comp.getDisplayText().equals(element.getDisplayText())) {
					matchCount--;
				}
			}
			break;
		case NONE:
			matchCount = 1;
			break;
		}

		return matchCount;
	}

}
