package scriptease.translator.codegenerator.code.contexts.storycomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.PickIt;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.FileContext;

/**
 * StoryComponentContext is Context for a StoryComponent. Story Component
 * contexts can only be created from another context, with the final parent
 * usually being a {@link FileContext}.
 * 
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class StoryComponentContext extends Context {
	private final StoryComponent component;

	/**
	 * Creates a new StoryComponentContext from another Context.
	 * 
	 * @param other
	 *            The context to base this one off of.
	 * @param source
	 *            The source StoryComponent associated with the context.
	 */
	public StoryComponentContext(Context other, StoryComponent source) {
		super(other);

		this.setLocationInfo(other.getLocationInfo());
		this.component = source;
	}

	/**
	 * Get the StoryComponent's generated name.
	 * 
	 * @see getGeneratedName()
	 */
	@Override
	public String getName() {
		return this.getNameOf(this.component);
	}

	/**
	 * Gets the StoryComponent's display text
	 */
	@Override
	public String getDisplayText() {
		return component.getDisplayText();
	}

	/**
	 * Get the StoryComponent's owner returns null if no owner
	 */
	@Override
	public StoryComponent getOwner() {
		return this.component.getOwner();
	}

	/**
	 * Get the owned StoryComponent's unique name.
	 * 
	 * @see getGeneratedName()
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		return this.getNamifier().getUniqueName(this.component, legalFormat);
	}

	/**
	 * Get the passed in StoryComponent's unique name.
	 * 
	 * @see getUniqueName()
	 */
	@Override
	public String getNameOf(StoryComponent component) {
		return this.getNamifier().getUniqueName(component, null);
	}

	/**
	 * Returns the component associated with the context.
	 * 
	 * @return
	 */
	public StoryComponent getComponent() {
		return this.component;
	}

	@Override
	public Collection<KnowIt> getVariables() {
		// We return nothing by default, such as for notes.
		return new ArrayList<KnowIt>();
	}

	@Override
	public String getIndex() {
		final StoryComponent owner;

		owner = this.component.getOwner();

		if (owner instanceof ComplexStoryComponent) {
			final int index;

			index = ((ComplexStoryComponent) owner)
					.getChildIndex(this.component);

			return Integer.toString(index);
		}

		return "";
	}

	@Override
	public String getChoiceProbabilityLowerBound() {
		int lowerBound = 0;

		final StoryComponent owner = this.component.getOwner();

		if (owner instanceof PickIt) {
			final PickIt pickIt = (PickIt) owner;

			// Loop through every child before this one and add up their
			// probabilities.
			for (StoryComponent child : pickIt.getChildren()) {
				if (child == this.component)
					break;

				lowerBound += pickIt.getChoices().get(child);
			}
		}

		return Integer.toString(lowerBound);
	}

	@Override
	public String getChoiceProbabilityUpperBound() {
		final int lowerBound = Integer.parseInt(this
				.getChoiceProbabilityLowerBound());

		final StoryComponent owner = this.component.getOwner();

		if (owner instanceof PickIt) {
			final PickIt pickIt = (PickIt) owner;

			return Integer.toString(pickIt.getChoices().get(this.component)
					+ lowerBound);
		}

		return Integer.toString(lowerBound);
	}
}
