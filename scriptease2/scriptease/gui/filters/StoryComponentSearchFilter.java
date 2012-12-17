package scriptease.gui.filters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.DescribeItManager;

/**
 * Accepts StoryComponents if one of their properties contains the text given in
 * the constructor.
 * 
 * @author mfchurch
 * @author remiller
 */
public class StoryComponentSearchFilter extends StoryComponentFilter {
	private static final String WHITESPACE_AND_QUOTES = " \t\r\n\"";
	private static final String QUOTES_ONLY = "\"";

	private String searchText;

	// TODO Store by string. Sort the map for more efficient search.
	// TODO Add a translator listener to update this map
	private static Map<String, Collection<StoryComponent>> searchMap = new HashMap<String, Collection<StoryComponent>>();

	private static TranslatorObserver translatorObserver = new TranslatorObserver() {
		@Override
		public void translatorLoaded(Translator newTranslator) {
			// TODO Reload the search map.
		}
	};

	static {
		// TODO will have to uncomment once we have reverse index search

		// TranslatorManager.getInstance().addTranslatorObserver(
		// translatorObserver);
	}

	public StoryComponentSearchFilter(String searchText) {
		this.searchText = searchText;
	}

	@Override
	public int getMatchCount(StoryComponent component) {
		return this.search(component, this.searchText);
	}

	@Override
	public void addRule(Filter newFilter) {
		if (newFilter instanceof StoryComponentSearchFilter) {
			this.searchText = ((StoryComponentSearchFilter) newFilter).searchText;
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
	private int search(StoryComponent component, String key) {
		final Collection<String> searchableData;
		Integer count = 1;

		// TODO This should instead search the map.

		if (key != null && !key.trim().isEmpty()) {
			searchableData = getSearchDataForComponent(component);
			count = countKeyMatches(searchableData, key);
		}

		return count;
	}

	/**
	 * Creates a Collection of String tokens which can be compared with the key
	 * 
	 * @param component
	 * @return
	 */
	private Collection<String> getSearchDataForComponent(
			StoryComponent component) {
		SearchDataCompiler searchData = new SearchDataCompiler();
		component.process(searchData);
		return searchData.getData();
	}

	/**
	 * Counts the number of key matches in the given collection of tokens
	 * 
	 * @param tokens
	 * @param key
	 * @return
	 */
	private Integer countKeyMatches(Collection<String> searchableData,
			String key) {
		int count = 0;
		Collection<String> parsedKeys = parseKey(key);
		for (String parsedKey : parsedKeys) {
			int localCount = 0;
			for (String data : searchableData) {
				if (data.toLowerCase().contains(parsedKey.toLowerCase()))
					localCount++;
			}
			if (localCount == 0)
				return 0;
			count += localCount;
		}
		return count;
	}

	/**
	 * Returns a Collection of String tokens representing either words, or
	 * phrases in quotations from the given text based on
	 * http://www.javapractices.com/topic/TopicAction.do?Id=87
	 * 
	 * @param text
	 * @return
	 */
	private Collection<String> parseKey(String text) {
		Collection<String> result = new CopyOnWriteArraySet<String>();

		String currentDelims = WHITESPACE_AND_QUOTES;
		boolean returnTokens = true;

		StringTokenizer parser = new StringTokenizer(text, currentDelims,
				returnTokens);

		while (parser.hasMoreTokens()) {
			String token = parser.nextToken(currentDelims);
			if (!token.equals(QUOTES_ONLY)) {
				token = token.trim();
				if (!token.equals("")) {
					result.add(token);
				}
			} else {
				currentDelims = flipDelimiters(currentDelims);
			}
		}
		return result;
	}

	private String flipDelimiters(String currentDelims) {
		if (currentDelims.equals(WHITESPACE_AND_QUOTES))
			return QUOTES_ONLY;
		else
			return WHITESPACE_AND_QUOTES;
	}

	/**
	 * Handles Aggregation of SearchableData for StoryComponents
	 * 
	 * @author mfchurch
	 * 
	 */
	private class SearchDataCompiler extends StoryAdapter {
		private final Collection<String> searchData;

		private SearchDataCompiler() {
			this.searchData = new CopyOnWriteArraySet<String>();
		}

		private Collection<String> getData() {
			return this.searchData;
		}

		@Override
		protected void defaultProcess(StoryComponent component) {
			this.searchData.add(component.getDisplayText());
			this.searchData.addAll(component.getLabels());
		}

		@Override
		public void processAskIt(AskIt askIt) {
			defaultProcess(askIt);
			askIt.getCondition().process(this);
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			defaultProcess(scriptIt);
			for (KnowIt parameter : scriptIt.getParameters()) {
				KnowItBinding binding = parameter.getBinding();
				if (binding.isBound())
					// allow searching of bound values
					this.searchData.add(binding.getScriptValue());
				parameter.process(this);
			}
			for (KnowIt implicit : scriptIt.getImplicits()) {
				implicit.process(this);
			}

			// searchable by slot
			for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
				if (codeBlock.hasSlot())
					this.searchData.add(codeBlock.getSlot());
			}
			this.searchData.addAll(scriptIt.getTypes());
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			defaultProcess(knowIt);

			this.searchData.addAll(knowIt.getTypes());

			knowIt.getBinding().process(new BindingAdapter() {

				@Override
				public void processConstant(KnowItBindingConstant constant) {
					searchData.add(constant.getName());
					searchData.add(constant.getTag());
					searchData.add(constant.getScriptValue());
					searchData.add(constant.getTemplateID());

					searchData.addAll(constant.getTypes());
				}

				@Override
				public void processFunction(KnowItBindingFunction function) {
					function.getValue().process(SearchDataCompiler.this);
				}

				@Override
				public void processReference(KnowItBindingReference reference) {
					reference.getValue().process(SearchDataCompiler.this);
				}

				@Override
				public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
					storyPoint.getValue().process(SearchDataCompiler.this);
				}
			});

			final DescribeItManager describeItManager;
			final DescribeIt describeIt;

			describeItManager = TranslatorManager.getInstance()
					.getActiveAPIDictionary().getDescribeItManager();
			describeIt = describeItManager.getDescribeIt(knowIt);

			if (describeIt != null) {
				for (ScriptIt scriptIt : describeIt.getScriptIts()) {
					scriptIt.process(this);
				}
			}
		}
	}
}
