package scriptease.gui.filters;

import scriptease.model.StoryComponent;
import scriptease.translator.io.model.GameConstant;

/**
 * The Filter system implements the chain of responsibility and decorater design
 * pattern. StoryComponents are passed into the Filter which calls the
 * isAcceptable(StoryComponent) method to filter the given collection.
 * isAcceptable checks if the StoryComponent meets the requirement of it's
 * filter, then passes it down to the next filter until all filters have been
 * passed. <br>
 * <br>
 * Updating or changing a filter works by adding new filters as the 'nextFilter'
 * unless a filter of the same type already exists in the chain, in which case
 * it amends that filter to match the new requirements.
 * 
 * @author mfchurch
 * @author remiller
 */
public abstract class Filter {
	private Filter nextRule;

	/**
	 * Determines if the given StoryComponent is acceptable by this rule and all
	 * other rules under it.
	 * 
	 * @param component
	 * @return <code>true</code> if the given component passes all rules.
	 * @see #getMatchCount(Object)
	 */
	public final boolean isAcceptable(GameConstant gameObject) {
		return (this.nextRule == null ? true : this.nextRule
				.isAcceptable(gameObject))
				&& this.getMatchCount(gameObject) > 0;
	}

	/**
	 * Determines if the given GameObject satisfies <i>only</i> this rule alone.
	 * To test for this rule including all decorated rules, use
	 * {@link #isAcceptable(Object)} instead.
	 * 
	 * @param component
	 * @return <code>true</code> if the given element passes this rule.
	 * @see #isAcceptable(Object)
	 */
	protected abstract int getMatchCount(GameConstant element);

	/**
	 * Determines if the given StoryComponent is acceptable by this rule and all
	 * other rules under it.
	 * 
	 * @param component
	 * @return <code>true</code> if the given component passes all rules.
	 * @see #getMatchCount(Object)
	 */
	public final boolean isAcceptable(StoryComponent element) {
		return (this.nextRule == null ? true : this.nextRule
				.isAcceptable(element)) && this.getMatchCount(element) > 0;
	}

	/**
	 * Determines if the given StoryComponent satisfies <i>only</i> this rule
	 * alone. To test for this rule including all decorated rules, use
	 * {@link #isAcceptable(Object)} instead.
	 * 
	 * @param component
	 * @return <code>true</code> if the given element passes this rule.
	 * @see #isAcceptable(Object)
	 */
	protected abstract int getMatchCount(StoryComponent element);

	/**
	 * Adds the given rule to this stack of rules. If a similar rule exists, it
	 * may be merged or overwritten.
	 * 
	 * @param newRule
	 *            The new rule to add.
	 */
	public void addRule(Filter newRule) {
		if (this.nextRule == null)
			this.nextRule = newRule;
		else
			this.nextRule.addRule(newRule);
	}

	/**
	 * Gets the next filter rule to be applied.
	 * 
	 * @return the net filter rule.
	 */
	protected Filter getNextRule() {
		return this.nextRule;
	}

	@Override
	public String toString() {
		return "(" + this.nextRule.toString() + ")";
	}
}
