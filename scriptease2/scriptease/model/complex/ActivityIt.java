package scriptease.model.complex;

import java.util.List;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Represents a container of effects, descriptions, questions, and controls.
 * 
 * @author jyuen
 */
public class ActivityIt extends ScriptIt {

	/**
	 * Constructor. Creates a new ActivityIt with the given name
	 * 
	 * @param name
	 */
	public ActivityIt(LibraryModel library, String name) {
		super(library, name);

		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(ControlIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(PickIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processActivityIt(this);
	}

	/**
	 * When cloning Activities, we must go through all of its descendents that
	 * have a KnowItBindingUninitialized and change its KnowIt reference to the
	 * cloned parameter KnowIt otherwise bad things will happen.
	 */
	@Override
	public ActivityIt clone() {
		final ActivityIt activityIt = (ActivityIt) super.clone();

		final List<StoryComponent> children = activityIt.getChildren();

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

						for (KnowIt activityParam : activityIt.getParameters()) {
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

		return activityIt;
	}
}
