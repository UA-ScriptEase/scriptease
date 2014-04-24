package scriptease.controller.io.converter.storycomponent;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.io.FileIO;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Converts CauseIts to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 * 
 * @see ScriptItConverter
 */
public class CauseItConverter extends ScriptItConverter {

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final CauseIt causeIt = (CauseIt) super.unmarshal(reader, context);

		// Search for KnowItBindingReferences.
		if (FileIO.getInstance().getMode() == FileIO.IoMode.STORY) {
			// Go down through and rebind references to what they should really
			// be bound to: implicits
			this.rebindKnowItBindingReferences(causeIt);
		}

		return causeIt;
	}

	/*
	 * TODO Abstract this somehow. We only call this method on causes while in
	 * story mode. However, we also go over the entire model in the NWN
	 * translator to find journal effects. We should just make one pass through
	 * the entire module and run a bunch of runnables at appropriate times.
	 * 
	 * Ticket: 40870537
	 */
	private void rebindKnowItBindingReferences(final ScriptIt scriptIt) {
		final StoryAdapter adapter;

		adapter = new StoryAdapter() {
			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				for (StoryComponent child : complex.getChildren()) {
					child.process(this);
				}
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer storyComponentContainer) {
				this.defaultProcessComplex(storyComponentContainer);
			}

			@Override
			public void processScriptIt(ScriptIt s) {
				s.processSubjects(this);
				s.processParameters(this);
				this.defaultProcessComplex(s);
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				KnowItBinding binding = knowIt.getBinding();
				final StoryAdapter outerAnonInnerClass = this;
				binding.process(new BindingAdapter() {
					@Override
					public void processReference(
							KnowItBindingReference reference) {

						for (KnowIt implicit : scriptIt.getImplicits()) {
							if (implicit.equals(reference.getValue())) {
								knowIt.setBinding(new KnowItBindingReference(
										implicit));
							}
						}
					}

					@Override
					public void processFunction(KnowItBindingFunction function) {
						ScriptIt referenced = function.getValue();
						referenced.process(outerAnonInnerClass);
					}
				});
			}

			@Override
			public void processAskIt(AskIt askIt) {
				askIt.getCondition().process(this);
				this.defaultProcessComplex(askIt);
			}
		};

		scriptIt.process(adapter);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CauseIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library) {
		return new CauseIt(library, "");
	}
}
