package scriptease.controller.io.converter.storycomponent;

import java.util.List;

import scriptease.controller.StoryAdapter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Converts {@link ActivityIt}s to/from XML.
 * 
 * @author jyuen
 */
public class ActivityItConverter extends ScriptItConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ActivityIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library) {
		return new ActivityIt(library, "");
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ActivityIt activity;

		activity = (ActivityIt) super.unmarshal(reader, context);

		// we must go through all of its descendants that have a
		// KnowItBindingUninitialized and change its KnowIt reference to the
		// activity parameter KnowIt otherwise KnowItBindingUnitialized's don't
		// reference their correct slot.
		final List<StoryComponent> children = activity.getChildren();

		for (StoryComponent child : children) {

			child.process(new StoryAdapter() {

				@Override
				public void processScriptIt(ScriptIt scriptIt) {
					this.defaultProcessComplex(scriptIt);
					scriptIt.processParameters(this);
				}

				@Override
				public void processKnowIt(KnowIt knowIt) {
					final KnowItBinding binding = knowIt.getBinding();

					if (binding instanceof KnowItBindingFunction) {
						final KnowItBindingFunction function = (KnowItBindingFunction) binding;

						function.getValue().process(this);

					} else if (binding instanceof KnowItBindingUninitialized) {
						KnowItBindingUninitialized uninitialized = (KnowItBindingUninitialized) binding;

						for (KnowIt activityParam : activity.getParameters()) {
							if (uninitialized.getValue().getDisplayText()
									.equals(activityParam.getDisplayText())) {
								uninitialized = new KnowItBindingUninitialized(
										new KnowItBindingReference(
												activityParam));

								knowIt.setBinding(uninitialized);
								break;
							}
						}
					}
				}

				@Override
				protected void defaultProcessComplex(
						ComplexStoryComponent complex) {
					for (StoryComponent child : complex.getChildren()) {
						child.process(this);
					}
				}
			});
		}

		return activity;
	}
}
