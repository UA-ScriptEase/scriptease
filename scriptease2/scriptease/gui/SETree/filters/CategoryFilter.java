package scriptease.gui.SETree.filters;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

/**
 * Category Filter filters elements based on if they match the acceptable
 * {@link Category} given in the constructor.
 * 
 * @author mfchurch
 */
public class CategoryFilter extends StoryComponentFilter {
	/**
	 * Legal categories for the category filter.
	 * 
	 * @author remiller
	 */
	public enum Category {
		EFFECTS, DESCRIPTIONS, CAUSES, FOLDERS;
	}

	private Category category;

	/**
	 * Builds a new category filter for the given category.
	 * 
	 * @param category
	 *            The category to filter to. Must be one of {@link Category}.
	 */
	public CategoryFilter(Category category) {
		this.category = category;
	}

	@Override
	public void addRule(Filter newFilter) {
		if (newFilter instanceof CategoryFilter) {
			this.category = ((CategoryFilter) newFilter).category;
		} else
			super.addRule(newFilter);
	}

	@Override
	protected int getMatchCount(StoryComponent component) {
		final CategoryFilterVisitor visitor;

		visitor = new CategoryFilterVisitor();

		component.process(visitor);

		return visitor.acceptable ? 1 : 0;
	}

	private class CategoryFilterVisitor extends AbstractNoOpStoryVisitor {
		public boolean acceptable = false;

		@Override
		public void processStoryComponentContainer(
				StoryComponentContainer container) {
			if (CategoryFilter.this.category.equals(Category.FOLDERS))
				this.acceptable = true;
		}

		@Override
		public void processStoryItemSequence(StoryItemSequence sequence) {
			if (CategoryFilter.this.category.equals(Category.FOLDERS))
				this.acceptable = true;
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			if (CategoryFilter.this.category.equals(Category.CAUSES))
				this.acceptable = scriptIt.isCause();
			else if (CategoryFilter.this.category.equals(Category.EFFECTS))
				this.acceptable = !scriptIt.isCause();
		}

		@Override
		public void processAskIt(AskIt questionIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.EFFECTS);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.DESCRIPTIONS);
		}
	}
}
