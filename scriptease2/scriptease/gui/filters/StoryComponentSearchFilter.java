package scriptease.gui.filters;

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.FragmentAdapter;
import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentFragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;

/**
 * Accepts StoryComponents if one of their properties contains the text given in
 * the constructor.
 * 
 * @author mfchurch
 * @author remiller
 * @author kschenk
 */
public class StoryComponentSearchFilter extends StoryComponentFilter {
	private static final String WHITESPACE_AND_QUOTES = " \t\r\n\"";
	private static final String QUOTES_ONLY = "\"";

	private String searchText;

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
		Integer count = 1;

		if (key != null && !key.trim().isEmpty()) {
			final SearchDataCompiler searchData = new SearchDataCompiler();

			component.process(searchData);

			count = countKeyMatches(searchData.getData(), key);
		}

		return count;
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
				if (data != null && parsedKey != null) {
					if (data.toLowerCase().contains(parsedKey.toLowerCase()))
						localCount++;
				}
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
	 * @author jyuen
	 */
	private class SearchDataCompiler extends StoryAdapter {
		private final Collection<String> searchData;

		private SearchDataCompiler() {
			this.searchData = new CopyOnWriteArraySet<String>();
		}

		private Collection<String> getData() {
			return this.searchData;
		}

		private void addTypeData(Collection<String> types) {
			final SEModel model;

			model = SEModelManager.getInstance().getActiveModel();

			for (final String type : types) {
				this.searchData.add(type);
				this.searchData.add(model.getTypeDisplayText(type));
				this.searchData.add(model.getTypeCodeSymbol(type));
				this.searchData.add(model.getTypeWidgetName(type));

				final Map<String, String> enums = model
						.getTypeEnumeratedValues(type);

				this.searchData.addAll(enums.values());
				this.searchData.addAll(enums.keySet());
			}
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
		public void processControlIt(ControlIt controlIt) {
			defaultProcess(controlIt);
			for (CodeBlock codeBlock : controlIt.getCodeBlocks())
				codeBlock.process(this);
		}

		@Override
		public void processCodeBlockSource(CodeBlockSource codeBlock) {
			if (codeBlock.hasSlot())
				this.searchData.add(codeBlock.getSlot());

			final Collection<AbstractFragment> fragments = codeBlock.getCode();
			for (AbstractFragment codeFrag : fragments) {
				codeFrag.process(new FragmentAdapter() {

					@Override
					protected void defaultProcess(AbstractFragment fragment) {
						searchData.add(fragment.getDirectiveText());
					}

					@Override
					public void processScopeFragment(ScopeFragment fragment) {
						for (AbstractFragment frag : fragment.getSubFragments())
							frag.process(this);
					}

					@Override
					public void processSeriesFragment(SeriesFragment fragment) {
						for (AbstractFragment frag : fragment.getSubFragments())
							frag.process(this);
					}

					@Override
					public void processIndentFragment(IndentFragment fragment) {
						for (AbstractFragment frag : fragment.getSubFragments())
							frag.process(this);
					}

					@Override
					public void processLineFragment(LineFragment fragment) {
						for (AbstractFragment frag : fragment.getSubFragments())
							frag.process(this);
					}
				});
			}
		}

		@Override
		public void processActivityIt(ActivityIt activityIt) {
			processScriptIt(activityIt);
			for (StoryComponent child : activityIt.getChildren()) {
				child.process(this);
			}
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
				codeBlock.process(this);
			}

			this.addTypeData(scriptIt.getTypes());
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			defaultProcess(knowIt);

			this.addTypeData(knowIt.getTypes());

			knowIt.getBinding().process(new BindingAdapter() {

				@Override
				public void processResource(KnowItBindingResource constant) {
					searchData.add(constant.getName());
					searchData.add(constant.getTag());
					searchData.add(constant.getScriptValue());
					searchData.add(constant.getTemplateID());

					addTypeData(constant.getTypes());
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

			final DescribeIt describeIt;

			describeIt = knowIt.getLibrary().getDescribeIt(knowIt);

			if (describeIt != null) {
				for (ScriptIt scriptIt : describeIt.getScriptIts()) {
					scriptIt.process(this);
				}
			}
		}
	}
}
