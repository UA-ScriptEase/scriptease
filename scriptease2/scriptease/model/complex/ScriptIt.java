package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.codegenerator.LocationInformation;

/**
 * A ScriptIt represents a StoryComponent which is used to generate functions in
 * code. It contains codeBlocks, which are able to have parameters, implicits,
 * subjects and slots and return types.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 * 
 */
public class ScriptIt extends ComplexStoryComponent implements TypedComponent,
		StoryComponentObserver {

	protected List<CodeBlock> codeBlocks;

	public ScriptIt(LibraryModel library, String name) {
		super(library, name);
		this.codeBlocks = new ArrayList<CodeBlock>();

		// Only classes that extend ScriptIts should be able to have children.
		this.registerChildTypes(
				new ArrayList<Class<? extends StoryComponent>>(), 0);
	}

	public List<CodeBlock> getCodeBlocks() {
		return new ArrayList<CodeBlock>(this.codeBlocks);
	}

	/**
	 * Retrieves only the code blocks which apply to the given location
	 * (subject, slot)
	 * 
	 * @param locationInfo
	 * @return
	 */
	public Collection<CodeBlock> getCodeBlocksForLocation(
			LocationInformation locationInfo) {

		final Collection<CodeBlock> matching = new ArrayList<CodeBlock>(1);

		for (CodeBlock codeBlock : this.codeBlocks) {
			if (locationInfo.matchesLocation(codeBlock))
				matching.add(codeBlock);
		}
		return matching;
	}

	/**
	 * Gets the cause that contains this ScriptIt.
	 * 
	 * @return
	 */
	public CauseIt getCause() {
		if (this instanceof CauseIt)
			return (CauseIt) this;
		else
			for (CodeBlock block : this.codeBlocks) {
				final CauseIt cause = block.getCause();
				if (cause != null)
					return (CauseIt) cause;
			}
		return null;
	}

	/**
	 * Returns whether the codeblocks of one ScriptIt are equal to another.
	 * 
	 * @param other
	 * @return
	 */
	public boolean codeBlocksEqual(ScriptIt other) {
		final Collection<CodeBlock> thisBlocks = this.getCodeBlocks();
		final Collection<CodeBlock> otherBlocks = other.getCodeBlocks();

		boolean equal = super.equals(other);

		if (equal) {
			equal &= thisBlocks.size() == otherBlocks.size();

			for (CodeBlock thisBlock : thisBlocks) {
				boolean isInOther = false;

				for (CodeBlock otherBlock : otherBlocks) {
					if (thisBlock.isEquivalent(otherBlock)) {
						isInOther = true;
					}
				}

				equal &= isInOther;
			}
		}

		return equal;
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other) && other instanceof ScriptIt) {
			return this.codeBlocks.equals(((ScriptIt) other).codeBlocks);
		}
		return false;
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processScriptIt(this);
	}

	@Override
	public String toString() {
		return "ScriptIt [" + this.getDisplayText() + "]";
	}

	@Override
	public ScriptIt clone() {
		final ScriptIt component = (ScriptIt) super.clone();

		// clone the code blocks
		component.codeBlocks = new ArrayList<CodeBlock>(this.codeBlocks.size());
		for (CodeBlock codeBlock : this.codeBlocks) {
			component.addCodeBlock(codeBlock.clone());
		}

		for (StoryComponent child : component.getChildren()) {

			child.process(new StoryAdapter() {

				@Override
				public void processTask(Task task) {
					super.processTask(task);

					for (Task successor : task.getSuccessors())
						successor.process(this);
				}

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

						for (KnowIt activityParam : component.getParameters()) {
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

		return component;
	}

	@Override
	public Collection<String> getTypes() {
		return this.getMainCodeBlock().getTypes();
	}

	/**
	 * Get the parameters for all of the codeBlocks
	 * 
	 * @return
	 */
	public Collection<KnowIt> getParameters() {
		final List<KnowIt> parameters = new ArrayList<KnowIt>();
		for (CodeBlock codeBlock : this.codeBlocks) {
			parameters.addAll(codeBlock.getParameters());
		}
		return parameters;
	}

	/**
	 * Get a specific parameter from one of the codeBlocks. Returns null if a
	 * parameter with that displayName is not found.
	 * 
	 * @param displayName
	 * @return
	 */
	public KnowIt getParameter(String displayName) {
		for (KnowIt parameter : this.getParameters()) {
			if (parameter.getDisplayText().equalsIgnoreCase(displayName))
				return parameter;
		}
		return null;
	}

	/**
	 * Get the implicits for all of the codeBlocks
	 * 
	 * @return
	 */
	public Collection<KnowIt> getImplicits() {
		final Collection<KnowIt> implicits = new CopyOnWriteArraySet<KnowIt>();
		for (CodeBlock codeBlock : this.codeBlocks) {
			implicits.addAll(codeBlock.getImplicits());
		}
		return implicits;
	}

	/**
	 * Get the main CodeBlock for the ScriptIt. Defaults to the first CodeBlock.
	 * 
	 * @return
	 */
	public CodeBlock getMainCodeBlock() {
		for (CodeBlock codeBlock : this.codeBlocks)
			return codeBlock;

		throw new NoSuchElementException(
				"Cannot get main CodeBlock because there are none! Did "
						+ "you remember to add a CodeBlock when you "
						+ "created the ScriptIt?");
	}

	public void removeCodeBlock(CodeBlock codeBlock) {
		if (this.codeBlocks.remove(codeBlock)) {
			codeBlock.removeStoryComponentObserver(this);
			codeBlock.setOwner(null);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODEBLOCK_REMOVED));
		}
	}

	public void addCodeBlock(CodeBlock codeBlock) {
		if (this.codeBlocks.add(codeBlock)) {
			codeBlock.addStoryComponentObserver(this);
			codeBlock.setOwner(this);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODEBLOCK_ADDED));
		}
	}

	public void setCodeBlocks(Collection<CodeBlock> codeBlocks) {
		for (CodeBlock codeBlock : this.codeBlocks) {
			this.removeCodeBlock(codeBlock);
		}
		for (CodeBlock codeBlock : codeBlocks) {
			this.addCodeBlock(codeBlock);
		}

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CODE_BLOCKS_SET));
	}

	public void processParameters(StoryVisitor processController) {
		for (StoryComponent parameter : getParameters()) {
			parameter.process(processController);
		}
	}

	/**
	 * Double-dispatch for the subjects of the ScriptIt
	 * 
	 * @param processController
	 */
	public void processSubjects(StoryVisitor processController) {
		Collection<KnowIt> subjects = new ArrayList<KnowIt>();
		for (CodeBlock codeBlock : this.codeBlocks)
			if (codeBlock.hasSubject())
				subjects.add(codeBlock.getSubject());
		for (KnowIt subject : subjects) {
			subject.process(processController);
		}
	}

	/**
	 * Double-dispatch for the implicits of the ScriptIt
	 * 
	 * @param processController
	 */
	public void processImplicits(StoryVisitor processController) {
		for (KnowIt implicit : this.getImplicits())
			implicit.process(processController);
	}

	/**
	 * Returns the bindings on the ScriptIt's parameters
	 * 
	 * @return
	 */
	public Collection<KnowItBinding> getBindings() {
		final Collection<KnowItBinding> bindings;

		bindings = new ArrayList<KnowItBinding>();

		for (KnowIt parameter : this.getParameters()) {
			bindings.add(parameter.getBinding());
		}

		return bindings;
	}

	@Override
	public void revalidateKnowItBindings() {
		final Collection<KnowIt> parameters;

		parameters = this.getParameters();

		for (KnowIt parameter : parameters) {
			parameter.revalidateKnowItBindings();
		}

		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}

	@Override
	public void setEnabled(Boolean enable) {
		super.setEnabled(enable);

		if (enable) {
			// Enable the descriptions that are used as bindings if the effect
			// is enabled
			final Collection<KnowItBinding> bindings = this.getBindings();

			for (KnowItBinding binding : bindings) {
				if (binding instanceof KnowItBindingReference) {
					final KnowItBindingReference reference = (KnowItBindingReference) binding;

					final KnowIt value = reference.getValue();

					if (!value.isEnabled())
						value.setEnabled(true);
				}
			}
		}
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		final StoryComponentChangeEnum type = event.getType();
		final StoryComponent source = event.getSource();
		// The ScriptIt hijacks CodeBlock events and sends it to it's observers
		if (this.codeBlocks.contains(source)) {
			if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE));
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_NAME_SET) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_NAME_SET));
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_TYPE) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_TYPE));
			} else if (type == StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET));
			} else if (type == StoryComponentChangeEnum.CHANGE_CODEBLOCK_CODE) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_CODEBLOCK_CODE));
			} else if (type == StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET));
			}
		}
	}
}
