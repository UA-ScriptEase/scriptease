package scriptease.gui.filters;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;

public class TranslatorFilter extends StoryComponentFilter {
	private Translator translator;

	public TranslatorFilter(Translator translator) {
		this.translator = translator;
	}

	@Override
	public int getMatchCount(StoryComponent component) {
		final TranslatorFilterVisitor visitor = new TranslatorFilterVisitor();

		if (this.translator != null)
			component.process(visitor);

		return visitor.acceptable ? 1 : 0;
	}

	@Override
	public void addRule(Filter newFilter) {
		// new translator filters override old ones outright. -- remiller
		if (newFilter instanceof TranslatorFilter) {
			this.translator = ((TranslatorFilter) newFilter).translator;
		} else
			super.addRule(newFilter);
	}

	private class TranslatorFilterVisitor extends StoryAdapter {
		public boolean acceptable = false;

		/**
		 * If there exists a DoIt with the same name in the library, they match
		 * the same desired pattern
		 */
		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			this.acceptable = (TranslatorFilter.this.translator.getApiDictionary().getLibrary()
					.retrieveScriptIt(scriptIt.getDisplayText()) != null);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			final KnowItBinding binding = knowIt.getBinding();
			// If the describeIt is bound to a DoIt, we can rely on the DoIt to
			// check if the describeIt is acceptable for this Translator.
			binding.process(new BindingAdapter() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					final ScriptIt doIt = function.getValue();
					doIt.process(TranslatorFilterVisitor.this);
				}

				@Override
				public void processDescribeIt(KnowItBindingDescribeIt described) {
					boolean isValid = true;
					final DescribeIt describeIt = described.getValue();
					for (final ScriptIt doIt : describeIt.getScriptIts()) {
						// process the DoIt to determine if it is valid
						doIt.process(TranslatorFilterVisitor.this);
						isValid &= TranslatorFilterVisitor.this.acceptable;

						// no need to continue if there exists an un supported
						// DoIt
						if (!isValid)
							break;
						TranslatorFilterVisitor.this.acceptable = isValid;
					}
				}
			});
		}

		@Override
		protected void defaultProcess(StoryComponent component) {
			this.acceptable = true;
		}
	}
	
	@Override
	public String toString(){ 
		if(this.translator == null)
			return "TranslatorFilter for null Translator";
		else
			return "TranslatorFilter ["+this.translator.getName()+"]";
	}
}
