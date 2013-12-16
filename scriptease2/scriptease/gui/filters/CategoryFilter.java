package scriptease.gui.filters;

import scriptease.controller.StoryAdapter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.ControlIt.ControlItFormat;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.behaviours.Behaviour;

/**
 * Category Filter filters elements based on if they match the acceptable
 * {@link Category} given in the constructor.
 * 
 * @author mfchurch
 * @author jyuen
 */
public class CategoryFilter extends StoryComponentFilter {
	/**
	 * Legal categories for the category filter.
	 * 
	 * @author remiller
	 * @author jyuen
	 */
	public enum Category {
		EFFECTS, DESCRIPTIONS, BEHAVIOURS, CAUSES, CONTROLS, BLOCKS, ACTIVITIES, NOTE;
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

	private class CategoryFilterVisitor extends StoryAdapter {
		public boolean acceptable = false;

		@Override
		public void processStoryComponentContainer(
				StoryComponentContainer container) {
			if (CategoryFilter.this.category.equals(Category.CONTROLS))
				this.acceptable = true;
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.EFFECTS);
		}

		@Override
		public void processCauseIt(CauseIt causeIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.CAUSES);
		}

		@Override
		public void processBehaviour(Behaviour behaviour) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.BEHAVIOURS);
		}

		@Override
		public void processActivityIt(ActivityIt activityIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.ACTIVITIES);
		}

		@Override
		public void processControlIt(ControlIt controlIt) {

			if (controlIt.getFormat().equals(ControlItFormat.BLOCK))
				this.acceptable = CategoryFilter.this.category
						.equals(Category.BLOCKS);
			else
				this.acceptable = CategoryFilter.this.category
						.equals(Category.CONTROLS);
		}

		@Override
		public void processPickIt(PickIt pickIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.CONTROLS);
		}

		@Override
		public void processAskIt(AskIt questionIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.CONTROLS);
		}

		@Override
		public void processNote(Note note) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.NOTE);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			this.acceptable = CategoryFilter.this.category
					.equals(Category.DESCRIPTIONS);
		}
	}
}
