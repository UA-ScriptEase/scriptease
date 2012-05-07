package scriptease.translator.codegenerator.code.fragments.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import scriptease.translator.codegenerator.CharacterRange;
import scriptease.translator.codegenerator.TranslatorKeywordManager;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * Represents a location where a sequence of related code tokens must be
 * generated. A separator string is interleaved between each item of the
 * parameter list. This is a special case of a DirectiveFragment in that it is a
 * DirectiveFragment repeated until that directive cannot be processed any more.<br>
 * <br>
 * Parameter lists can be represented by a SeriesFragment, for example.
 * 
 * @author mfchurch, jtduncan
 */
public abstract class AbstractSeriesFragment extends FormatFragment {
	private final String separator;
	private List<FormatFragment> format;
	private SeriesFilter seriesFilter;

	@Override
	public boolean equals(Object other) {
		if (other instanceof AbstractSeriesFragment)
			return this.hashCode() == other.hashCode();
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + format.hashCode() + separator.hashCode()
				+ seriesFilter.hashCode();
	}

	/**
	 * See:
	 * {@link FormatFragment#FormatFragment(String, CharacterRange, char[])}
	 * 
	 * @param data
	 *            The specific data list label.
	 * @param formatLabel
	 *            The format's label to be used in looking up the format from
	 *            the interpreter.
	 * @param separator
	 *            The string that is inserted in between each element in the
	 *            list during resolution.
	 */
	public AbstractSeriesFragment(String data, String separator,
			List<FormatFragment> format, String filter, String filterType) {
		super(data);
		this.separator = separator;
		this.format = format;
		this.seriesFilter = new SeriesFilter(filter, filterType);
	}

	public Collection<FormatFragment> getSubFragments() {
		return this.format;
	}

	public String getSeparator() {
		return this.separator;
	}

	public SeriesFilter getFilter() {
		return this.seriesFilter;
	}

	@Override
	public String resolve(Context context) {
		super.resolve(context);
		final Iterator<? extends Object> it;
		final StringBuilder code;

		code = new StringBuilder();

		it = this.buildDataIterator(context);

		while (it.hasNext()) {
			Context newContext = ContextFactory.getInstance().createContext(
					context, it.next());
			code.append(FormatFragment.resolveFormat(format, newContext));

			if (it.hasNext())
				code.append(this.separator);
		}

		return code.toString();
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
		final String dataLabel = this.getDirectiveText();

		// IF+ELSE BLOCK ( series ... data= <dataLabel> )
		if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_SCRIPTIT_EFFECT_SERIES))
			return this.seriesFilter.applyFilter(handle(context
					.getScriptItEffects()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_INCLUDES_SERIES))
			return this.seriesFilter.applyFilter(handle(context.getIncludes()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_SCRIPTIT_SERIES))
			return this.seriesFilter
					.applyFilter(handle(context.getScriptIts()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_CODEBLOCK_SERIES))
			return this.seriesFilter.applyFilter(handle((context
					.getCodeBlocks())));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_CAUSE_SERIES))
			return this.seriesFilter.applyFilter(handle(context.getCauses()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_ARGUMENT_SERIES))
			return this.seriesFilter.applyFilter(context.getArguments());
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_PARAMETER_SERIES))
			return this.seriesFilter
					.applyFilter(handle(context.getParameters()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_VARIABLE_SERIES))
			return this.seriesFilter
					.applyFilter(handle(context.getVariables()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_IMPLICIT_SERIES))
			return this.seriesFilter
					.applyFilter(handle(context.getImplicits()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_EFFECTS_SERIES))
			return this.seriesFilter.applyFilter(handle(context.getEffects()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_CHILDREN_SERIES))
			return this.seriesFilter.applyFilter(handle(context.getChildren()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_QUESTNODES_SERIES))
			return this.seriesFilter.applyFilter(handle(context.getQuestNodes()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_QUESTPOINTNODES_SERIES))
			return this.seriesFilter.applyFilter(handle(context
					.getQuestPointNodes()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_PARENT_NODES_SERIES))
			return this.seriesFilter.applyFilter(handle(context
					.getParentNodes()));
		else if (dataLabel
				.equalsIgnoreCase(TranslatorKeywordManager.XML_CHILDREN_NODES_SERIES))
			return this.seriesFilter.applyFilter(handle(context
					.getChildrenNodes()));
		// Default return
		else {
			System.err.println("Series was unable to be resolved for data: "
					+ dataLabel + " >");
			return new ArrayList<String>().iterator();
		}
	}

	/**
	 * Double dispatch for the appropriate handling of series filtering and
	 * uniquifying
	 * 
	 * @param <T>
	 * @param iterator
	 * @return
	 */
	protected abstract <T> Iterator<T> handle(Iterator<T> iterator);

	@Override
	public String toString() {
		return "<LIST:\"" + this.getDirectiveText() + "\">";
	}
}
