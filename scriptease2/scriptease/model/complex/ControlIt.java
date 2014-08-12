package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.librarymodel.LibraryModel;
import sun.awt.util.IdentityArrayList;

/**
 * ControlIts are a special type of ScriptIt that allow children other than the
 * story active and inactive block. They require a format, which can only be one
 * of the enumerated {@link ControlItFormat} types.
 * 
 * @author kschenk
 * 
 */
public class ControlIt extends ScriptIt {

	/**
	 * The formats allowed for ControlIts. Note that
	 * {@link ControlItFormat#NONE} may have undefined behaviour and is not
	 * recommended to be used.
	 * 
	 * @author kschenk
	 * 
	 */
	public enum ControlItFormat {
		NONE, DELAY, REPEAT, BLOCK
	}

	private ControlItFormat format;

	/**
	 * Sets up the ControlIt, registering its allowable children and setting its
	 * format to the passed in format.
	 * 
	 * @param name
	 * @param format
	 */
	public ControlIt(LibraryModel library, String name, ControlItFormat format) {
		super(library, name);
		this.allowableChildMap.clear();

		this.format = format;

		final int max = ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE;

		this.registerChildType(ScriptIt.class, max);
		this.registerChildType(KnowIt.class, max);
		this.registerChildType(StoryComponentContainer.class, max);
		this.registerChildType(Note.class, max);
		this.registerChildType(ControlIt.class, max);
		this.registerChildType(Behaviour.class, max);
		this.registerChildType(AskIt.class, max);
		this.registerChildType(PickIt.class, max);
		this.registerChildType(ActivityIt.class, max);
	}

	/**
	 * Returns the format of the ControlIt.
	 * 
	 * @return
	 */
	public ControlItFormat getFormat() {
		return format;
	}

	/**
	 * Set the format of the ControlIt to one of {@link ControlItFormat}.
	 * 
	 * @param format
	 */
	public void setFormat(ControlItFormat format) {
		this.format = format;
	}

	/**
	 * Returns all parameters of the ControlIt that it will require. Since
	 * ControlIts end up at the same level as Causes, they need to have some
	 * variables passed in as parameters.
	 * 
	 */
	public Collection<KnowIt> getRequiredParameters() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

		final StoryComponent owner = this.getOwner();

		if (owner == null)
			return parameters;

		owner.process(new StoryAdapter() {
			private StoryComponent previousComponent = ControlIt.this;

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				final Collection<StoryComponent> children;

				children = new IdentityArrayList<StoryComponent>(complex
						.getChildren());

				if (children.contains(this.previousComponent)) {
					for (StoryComponent child : children) {
						child.process(new StoryAdapter() {
							@Override
							public void processKnowIt(KnowIt knowIt) {
								parameters.add(knowIt);

								final KnowItBinding binding = knowIt
										.getBinding();

								if (binding != null) {
									final Object value = binding.getValue();
									if (value instanceof StoryComponent) {
										((StoryComponent) value).process(this);
									}
								}
							}

							public void processScriptIt(ScriptIt scriptIt) {
								parameters.addAll(scriptIt.getParameters());
							};

							@Override
							public void processAskIt(AskIt questionIt) {
								parameters.add(questionIt.getCondition());
							}
						});
					}
				}

				this.previousComponent = complex;

				final StoryComponent owner = complex.getOwner();

				if (owner != null && owner instanceof ComplexStoryComponent
						&& !(owner instanceof StoryPoint)) {
					owner.process(this);
				}
			}
		});

		return parameters;
	}

	@Override
	public ControlIt clone() {
		final ControlIt component = (ControlIt) super.clone();

		component.setFormat(this.getFormat());

		return component;
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processControlIt(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		super.revalidateKnowItBindings();
	}

	@Override
	public String toString() {
		return "ControlIt [" + this.getDisplayText() + "]";
	}

}
