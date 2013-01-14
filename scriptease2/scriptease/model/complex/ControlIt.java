package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import sun.awt.util.IdentityArrayList;

public class ControlIt extends ScriptIt {
	private String format;

	public ControlIt(String name, String format) {
		super(name);
		this.allowableChildMap.clear();

		this.format = format;

		final int max = ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE;

		this.registerChildType(ScriptIt.class, max);
		this.registerChildType(KnowIt.class, max);
		this.registerChildType(AskIt.class, max);
		this.registerChildType(StoryComponentContainer.class, max);
		this.registerChildType(Note.class, max);
		this.registerChildType(ControlIt.class, max);
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {

		if (!(newChild instanceof ScriptIt && ((ScriptIt) newChild).isCause()))
			return super.addStoryChildBefore(newChild, sibling);

		return false;
	}

	@Override
	public Collection<KnowIt> getParameters() {
		// TODO Auto-generated method stub
		return super.getParameters();
	}

/*	@Override
	public void processParameters(StoryVisitor processController) {
		for (KnowIt knowIt : this.getRequiredParameters()) {
			knowIt.process(processController);
		}
	}*/

	/**
	 * Returns all required parameters of the ControlIt.
	 */
	public Collection<KnowIt> getRequiredParameters() {
		final Collection<KnowIt> parameters;

		parameters = new ArrayList<KnowIt>();

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

				// children = complex.getChildren();

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

				if (owner != null
						&& owner instanceof ComplexStoryComponent
						&& !(owner instanceof ScriptIt && ((ScriptIt) owner)
								.isCause())) {
					owner.process(this);
				}
			}
		});

		return parameters;
	}

	@Override
	public ControlIt clone() {
		final ControlIt component = (ControlIt) super.clone();

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
