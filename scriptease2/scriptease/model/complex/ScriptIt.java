package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
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

	protected Collection<CodeBlock> codeBlocks;

	public ScriptIt(String name) {
		super(name);

		final int max = ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE;
		
		this.codeBlocks = new ArrayList<CodeBlock>();

		this.registerChildType(ScriptIt.class, max);
		this.registerChildType(KnowIt.class, max);
		this.registerChildType(StoryComponentContainer.class, max);
	}

	public Collection<CodeBlock> getCodeBlocks() {
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

		Collection<CodeBlock> matching = new ArrayList<CodeBlock>(1);
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
		for (KnowIt parameter : this.getParameters()) {
			final KnowItBinding binding;

			binding = parameter.getBinding();

			if (!binding.compatibleWith(parameter))
				parameter.setBinding(new KnowItBindingNull());
		}
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		final StoryComponentChangeEnum type = event.getType();
		final StoryComponent source = event.getSource();
		// The ScriptIt hijacks the event and sends it to it's observers
		if (this.codeBlocks.contains(source)) {
			if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE));
			} else if (type == StoryComponentChangeEnum.CHANGE_CODE_BLOCK_TYPES) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_CODE_BLOCK_TYPES));
			}
		}
	}
}
