package scriptease.translator.codegenerator.code.fragments.series;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

/**
 * A Series Filter is used to filter the context sensetive results of a series
 * fragment based on FilterBy and a regex. This can be used in codegeneration to
 * specify a more limited series by filtering them on a specific regex. Example:
 * A series of StartIts which have a specified slot.
 * 
 * @author mfchurch
 * 
 */
public class SeriesFilter extends AbstractNoOpStoryVisitor {
	private final String value;
	private final FilterBy type;
	private ArrayList<Object> filtered;

	/*
	 * Filter types must stay consistent with filtering options avaliable in the
	 * languageDictionary.dtd. Any changes must be handled in this class.
	 */
	public enum FilterBy {
		NONE, NAME, SLOT
	}

	public String getValue() {
		return this.value;
	}

	public FilterBy getType() {
		return this.type;
	}

	private FilterBy covertToFilterBy(String type) {
		if (type.equals("name"))
			return FilterBy.NAME;
		else if (type.equals("slot"))
			return FilterBy.SLOT;
		return FilterBy.NONE;
	}

	public SeriesFilter(String value, String type) {
		this.value = value;
		this.type = covertToFilterBy(type);
	}

	/**
	 * ApplyFilter filters the given Iterator<? extends Object> based on the
	 * SeriesFilter's type and regex, then returns an Iterator<? extends Object>
	 * to the filtered results.
	 */
	public Iterator<? extends Object> applyFilter(
			Iterator<? extends Object> toFilter) {
		// Do nothing if there is no filter
		if (this.type.equals(FilterBy.NONE))
			return toFilter;

		this.filtered = new ArrayList<Object>();

		while (toFilter.hasNext()) {
			Object object = toFilter.next();

			if (object instanceof StoryComponent) {
				((StoryComponent) object).process(this);
			} else if (object instanceof String) {
				if (passesFilter((String) object))
					this.filtered.add(object);
			} else {
				System.err.println(this + " cannot filter object " + object);
				this.filtered.add(object);
			}
		}
		return filtered.iterator();
	}

	/**
	 * Checks if the given String matches the filter regex.
	 * 
	 * @param text
	 * @return
	 */
	private boolean passesFilter(String text) {
		try {
			return text.matches(this.value);
		} catch (PatternSyntaxException e) {
			System.err.println(this + " has an invalid regex : " + e);
		}
		return false;
	}

	private void processStoryComponent(StoryComponent component) {
		if (this.type.equals(FilterBy.NAME))
			if (passesFilter(component.getDisplayText()))
				filtered.add(component);
	}

	@Override
	public void processStoryComponentContainer(
			StoryComponentContainer storyComponentContainer) {
		this.processStoryComponent(storyComponentContainer);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		if (this.type.equals(FilterBy.SLOT)) {
			for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
				if (passesFilter(codeBlock.getSlot())) {
					filtered.add(scriptIt);
					return;
				}
			}
		} else
			this.processStoryComponent(scriptIt);
	}

	@Override
	public void processKnowIt(KnowIt knowIt) {
		this.processStoryComponent(knowIt);
	}

	@Override
	public void processAskIt(AskIt askIt) {
		this.processStoryComponent(askIt);
	}

	@Override
	public void processStoryItemSequence(StoryItemSequence sequence) {
		this.processStoryComponent(sequence);
	}

	@Override
	public String toString() {
		return "SeriesFilter[" + this.value + ", " + this.type + "]";
	}
}
