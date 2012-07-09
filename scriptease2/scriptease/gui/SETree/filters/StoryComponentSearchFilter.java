package scriptease.gui.SETree.filters;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;

/**
 * Accepts StoryComponents if one of their properties contains the text given in
 * the constructor.
 * 
 * @author mfchurch
 * @author remiller
 */
public class StoryComponentSearchFilter extends StoryComponentFilter {
	private String searchText;

	public StoryComponentSearchFilter(String searchText) {
		this.searchText = searchText;
	}

	@Override
	public int getMatchCount(StoryComponent component) {
		return this.search(component, searchText);
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
		
		if (key != null && !key.trim().isEmpty()) {
			searchableData = getSearchDataForComponent(component);
			count = SearchFilterHelper.countKeyMatches(searchableData,
					key);
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
		//Freezes on this line...
		searchData.compile(component);
		return searchData.getData();
	}

	/**
	 * Handles Aggregation of SearchableData for StoryComponents
	 * 
	 * @author mfchurch
	 * 
	 */
	private class SearchDataCompiler extends AbstractNoOpStoryVisitor {
		private Collection<String> searchData;

		public SearchDataCompiler() {
			searchData = new CopyOnWriteArraySet<String>();
		}

		public void compile(StoryComponent component) {
			searchData.add(component.getDisplayText());
			component.process(this);
		}

		public Collection<String> getData() {
			return searchData;
		}

		@Override
		protected void defaultProcess(StoryComponent component) {
			searchData.addAll(component.getLabels());
		}

		@Override
		public void processAskIt(AskIt askIt) {
			defaultProcess(askIt);
			this.compile(askIt.getCondition());
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			defaultProcess(scriptIt);
			for (KnowIt parameter : scriptIt.getParameters()) {
				KnowItBinding binding = parameter.getBinding();
				if (binding.isBound())
					// allow searching of bound values
					searchData.add(binding.getScriptValue());
				this.compile(parameter);
			}
			// searchable by implicit
			//int i = 0;
			//System.out.println("Size of Implicits" + scriptIt.getImplicits().size());
			for (KnowIt implicit : scriptIt.getImplicits()) {
				//i++;
				//System.out.println("Index of Implicit: " + i);
				//More problems here it seems
				this.compile(implicit);
			}
		
			// searchable by slot
			for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
				if (codeBlock.hasSlot())
					this.searchData.add(codeBlock.getSlot());
			}
			searchData.addAll(scriptIt.getTypes());
		}
		
		@Override
		public void processKnowIt(KnowIt knowIt) {
			defaultProcess(knowIt);
			searchData.addAll(knowIt.getTypes());
	//		final KnowItBinding binding = knowIt.getBinding();
		/*	binding.process(new AbstractNoOpBindingVisitor() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					final ScriptIt doIt = function.getValue();
					SearchDataCompiler.this.compile(doIt);
				}

				@Override
				public void processDescribeIt(KnowItBindingDescribeIt described) {
					final DescribeIt describeIt = described.getValue();
					final Collection<ScriptIt> doIts = describeIt
							.getScriptIts();
					for (final ScriptIt doIt : doIts) {
						SearchDataCompiler.this.compile(doIt);
					}
				}
			});*/
//			binding.process(new AbstractNoOpBindingVisitor() {
				//Inner methods need alot ofwork...
	//		});
		}
	}
}
