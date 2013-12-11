package scriptease.translator.codegenerator.code.fragments.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.PatternSyntaxException;

import scriptease.controller.FragmentVisitor;
import scriptease.controller.StoryAdapter;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.codegenerator.CodeGenerationConstants.SeriesFilterType;
import scriptease.translator.codegenerator.CodeGenerationConstants.SeriesType;
import scriptease.translator.codegenerator.code.contexts.AskItContext;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.contexts.KnowItContext;
import scriptease.translator.codegenerator.code.contexts.ScriptItContext;
import scriptease.translator.codegenerator.code.contexts.StoryComponentContext;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Represents a location where a sequence of related code tokens must be
 * generated. A separator string is interleaved between each item of the
 * parameter list. This is a special case of a DirectiveFragment in that it is a
 * DirectiveFragment repeated until that directive cannot be processed any more.<br>
 * <br>
 * Parameter lists can be represented by a SeriesFragment, for example.
 * 
 * @author mfchurch
 * @author jtduncan
 * @author kschenk
 * @author jyuen
 */
public class SeriesFragment extends AbstractContainerFragment {
	private String separator;
	private SeriesFilter seriesFilter;
	private boolean isUnique;
	private String filter;
	private SeriesFilterType filterType;

	public SeriesFragment() {
		this("", "", new ArrayList<AbstractFragment>(), "",
				SeriesFilterType.NONE, false);
	}

	/**
	 * See:
	 * {@link AbstractFragment#FormatFragment(String, CharacterRange, char[])}
	 * 
	 * @param data
	 *            The specific data list label.
	 * @param formatLabel
	 *            The format's label to be used in looking up the format from
	 *            the interpreter.
	 * @param separator
	 *            The string that is inserted in between each element in the
	 *            list during resolution.
	 * @param isUnique
	 *            Sets whether the SeriesFragment is unique.
	 */
	public SeriesFragment(String data, String separator,
			List<AbstractFragment> format, String filter,
			SeriesFilterType filterType, boolean isUnique) {
		super(data, format);
		if (separator != null)
			this.separator = separator;
		else
			this.separator = "";
		if (filter != null)
			this.filter = filter;
		else
			this.filter = "";

		this.filterType = filterType;
		this.seriesFilter = new SeriesFilter(filter, filterType);
		this.isUnique = isUnique;
	}

	@Override
	public SeriesFragment clone() {
		final SeriesFragment clone = (SeriesFragment) super.clone();

		clone.setSeparator(this.separator);
		clone.setUnique(this.isUnique);
		clone.setFilterType(this.filterType);
		clone.setFilter(this.filter);
		clone.setSeriesFilter(this.filter, this.filterType);

		return clone;
	}

	public String getSeparator() {
		return this.separator;
	}

	public void setSeparator(String separator) {
		this.separator = new String(separator);
	}

	public String getFilter() {
		return this.filter;
	}

	public void setFilter(String filter) {
		this.filter = new String(filter);
		setSeriesFilter(filter, this.filterType);
	}

	public void setSeriesFilter(String filter, SeriesFilterType filterType) {
		this.seriesFilter = new SeriesFilter(filter, filterType);
	}

	public SeriesFilterType getFilterType() {
		return this.filterType;
	}

	public void setFilterType(SeriesFilterType filterType) {
		this.filterType = filterType;
		this.seriesFilter = new SeriesFilter(this.filter, this.filterType);
	}

	public boolean isUnique() {
		return this.isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public SeriesFilter getSeriesFilter() {
		return this.seriesFilter;
	}

	@Override
	public String resolve(Context context) {
		super.resolve(context);

		final StringBuilder code;
		final Iterator<? extends Object> it;
		Object next;
		final ContextFactory contextFactory = ContextFactory.getInstance();
		Context newContext;

		code = new StringBuilder();

		it = this.buildDataIterator(context);

		boolean firstIteration = true;
		while (it.hasNext()) {
			next = it.next();

			newContext = contextFactory.createContext(context, next);

			// If the component for this context is disabled, we don't want to
			// include it in the code generation
			if (!this.isComponentDisabled(newContext)) {
				if (!firstIteration)
					code.append(this.separator);
				else
					firstIteration = false;

				code.append(AbstractFragment.resolveFormat(this.subFragments,
						newContext));
			}
		}

		return code.toString();
	}

	/**
	 * Whether the story component has been disabled and the code should be
	 * commented out.
	 */
	private boolean isComponentDisabled(Context context) {
		if (context instanceof KnowItContext
				|| context instanceof ScriptItContext
				|| context instanceof AskItContext) {

			final StoryComponent component;

			component = ((StoryComponentContext) context).getComponent();

			if (component != null && !component.isEnabled())
				return true;
		}

		return false;
	}

	private Collection<? extends Object> getData(Context context) {
		final String directiveText = this.getDirectiveText();

		final SeriesType series;

		try {
			series = SeriesType.valueOf(directiveText.toUpperCase());
		} catch (IllegalArgumentException e) {
			System.out.println("Couldn't find the value of : " + directiveText);
			return null;
		}

		switch (series) {
		case INCLUDES:
			return context.getIncludeFiles();
		case CODEBLOCKS:
			return context.getCodeBlocks();
		case CAUSES:
			return context.getCauses();
		case PARAMETERS:
			return context.getParameters();
		case PARAMETERSWITHSLOT:
			return context.getParametersWithSlot();
		case SLOTPARAMETERS:
			return context.getSlotParameters();
		case VARIABLES:
			return context.getVariables();
		case IMPLICITS:
			return context.getImplicits();
		case CHILDREN:
			return context.getChildren();
		case STORYNODES:
			return context.getStoryNodes();
		case STORYPOINTS:
			return context.getStoryPoints();
		case ORDEREDSTORYNODES:
			return context.getOrderedStoryNodes();
		case ORDEREDSTORYPOINTS:
			return context.getOrderedStoryPoints();
		case PARENTNODES:
			return context.getStoryPointParents();
		case CHILDRENNODES:
			return context.getStoryPointChildren();
		case IDENTICALCAUSES:
			return context.getIdenticalCauses();
		case DIALOGUEROOTS:
			return context.getDialogueRoots();
		case ORDEREDDIALOGUELINES:
			return context.getOrderedDialogueLines();
		case CHILDLINES:
			return context.getChildLines();
		case CHOICES:
			return context.getChoices();
		default:
			// Default return 'cuz they didn't tell us a real label!
			System.err.println("Series was unable to be resolved for data: "
					+ series + " >");
			return new ArrayList<String>();
		}
	}

	/**
	 * Builds an iterator that represents the data to repeat a format for.
	 * 
	 * @param context
	 *            The context that we are resolving in.
	 * @return An iterator that will iterate over the data as specified by this
	 *         series' <code>dataLabel</code>.
	 */
	private Iterator<? extends Object> buildDataIterator(Context context) {
		final Collection<? extends Object> data;
		Iterator<? extends Object> it;

		data = this.getData(context);
		it = data.iterator();

		if (this.isUnique) {
			it = uniquify(it);
		}

		return this.seriesFilter.applyFilter(it);
	}

	private <T> Iterator<T> uniquify(Iterator<T> iterator) {
		final Collection<T> unique = new CopyOnWriteArraySet<T>();
		while (iterator.hasNext()) {
			unique.add(iterator.next());
		}
		return unique.iterator();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SeriesFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.subFragments.hashCode()
				+ this.separator.hashCode() + this.seriesFilter.hashCode();
	}

	@Override
	public String toString() {
		return "<LIST:\"" + this.getDirectiveText() + "\">";
	}

	/**
	 * A filter for series.
	 * 
	 */
	public class SeriesFilter extends StoryAdapter {
		private final String value;
		private final SeriesFilterType type;
		private ArrayList<Object> filtered;

		public String getValue() {
			return this.value;
		}

		public SeriesFilterType getType() {
			return this.type;
		}

		public SeriesFilter(String value, SeriesFilterType type) {
			if (value != null)
				this.value = new String(value);
			else
				this.value = "";
			this.type = type;
		}

		/**
		 * ApplyFilter filters the given Iterator<? extends Object> based on the
		 * SeriesFilter's type and regex, then returns an Iterator<? extends
		 * Object> to the filtered results.
		 */
		public Iterator<? extends Object> applyFilter(
				Iterator<? extends Object> toFilter) {
			// Do nothing if there is no filter
			if (this.type.equals(SeriesFilterType.NONE))
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
					System.err
							.println(this + " cannot filter object " + object);
					this.filtered.add(object);
				}
			}
			return this.filtered.iterator();
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
			if (this.type.equals(SeriesFilterType.NAME))
				if (passesFilter(component.getDisplayText()))
					this.filtered.add(component);
		}

		@Override
		public void processStoryComponentContainer(
				StoryComponentContainer storyComponentContainer) {
			this.processStoryComponent(storyComponentContainer);
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			if (this.type.equals(SeriesFilterType.SLOT)) {
				for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
					if (passesFilter(codeBlock.getSlot())) {
						this.filtered.add(scriptIt);
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
		public String toString() {
			return "SeriesFilter[" + this.value + ", " + this.type + "]";
		}
	}

	@Override
	public void process(FragmentVisitor visitor) {
		visitor.processSeriesFragment(this);
	}
}
