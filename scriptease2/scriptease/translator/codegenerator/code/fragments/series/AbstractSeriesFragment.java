package scriptease.translator.codegenerator.code.fragments.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import scriptease.translator.codegenerator.CharacterRange;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.Fragment;

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
public abstract class AbstractSeriesFragment extends Fragment {
	private final String separator;
	private List<Fragment> format;
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
	 * {@link Fragment#FormatFragment(String, CharacterRange, char[])}
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
			List<Fragment> format, String filter, String filterType) {
		super(data);
		this.separator = separator;
		this.format = format;
		this.seriesFilter = new SeriesFilter(filter, filterType);
	}

	public Collection<Fragment> getSubFragments() {
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

		final StringBuilder code;
		final Iterator<? extends Object> it;
		Object next;
		final ContextFactory contextFactory = ContextFactory.getInstance();
		Context newContext;

		code = new StringBuilder();

		it = this.buildDataIterator(context);

		while (it.hasNext()) {
			next = it.next();

			newContext = contextFactory.createContext(context, next);
			code.append(Fragment.resolveFormat(format, newContext));

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
		final Iterator<? extends Object> it;

		// IF+ELSE BLOCK ( series ... data= <dataLabel> )
		if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.SCRIPTITEFFECTS
						.name()))
			it = context.getScriptItEffects();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.INCLUDES
						.name())) {
			it = context.getIncludeFiles().iterator();
		} else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.SCRIPTITS
						.name()))
			it = context.getScriptIts();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.CODEBLOCKS
						.name()))
			it = (context.getCodeBlocks());
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.CAUSES
						.name()))
			it = context.getCauses();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.ARGUMENTS
						.name()))
			it = context.getArguments();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.PARAMETERS
						.name()))
			it = context.getParameters();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.VARIABLES
						.name()))
			it = context.getVariables();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.IMPLICITS
						.name()))
			it = context.getImplicits();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.EFFECTS
						.name()))
			it = context.getEffects();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.CHILDREN
						.name()))
			it = context.getChildren();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.QUESTNODES
						.name()))
			it = context.getQuestNodes();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.QUESTPOINTNODES
						.name()))
			it = context.getQuestPointNodes();
		else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.PARENTNODES
						.name())) {
			it = context.getParentNodes();
		} else if (dataLabel
				.equalsIgnoreCase(CodeGenerationKeywordConstants.Series.CHILDRENNODES
						.name()))
			it = context.getChildrenNodes();
		else {
			// Default return 'cuz they didn't tell us a real label!
			System.err.println("Series was unable to be resolved for data: "
					+ dataLabel + " >");
			return new ArrayList<String>().iterator();
		}

		return this.seriesFilter.applyFilter(handle(it));
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
