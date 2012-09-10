package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.codegenerator.LocationInformation;

/**
 * A ScriptIt represents a StoryComponent which is used to generate functions in
 * code. It contains codeBlocks, which are able to have parameters, implicits,
 * subjects and slots and return types.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class ScriptIt extends ComplexStoryComponent implements TypedComponent {
	/**
	 * The group of children that are in the Story part of the Cause.
	 */
	private StoryItemSequence storyBlock;

	/**
	 * The group of children that are in the Always part of the Cause.
	 */
	private StoryItemSequence alwaysBlock;

	protected Collection<CodeBlock> codeBlocks;

	public ScriptIt(String name) {
		super(name);

		this.codeBlocks = new ArrayList<CodeBlock>();

		final List<Class<? extends StoryComponent>> validTypes;

		validTypes = new ArrayList<Class<? extends StoryComponent>>();

		this.registerChildType(StoryItemSequence.class, 2);

		validTypes.add(ScriptIt.class);
		validTypes.add(KnowIt.class);
		validTypes.add(AskIt.class);
		validTypes.add(StoryComponentContainer.class);

		this.storyBlock = new StoryItemSequence(validTypes);
		this.storyBlock.setDisplayText("Story:");
		this.alwaysBlock = new StoryItemSequence(validTypes);
		this.alwaysBlock.setDisplayText("Always:");
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

	@Override
	public boolean equals(Object other) {
		if (super.equals(other) && other instanceof ScriptIt) {
			return this.codeBlocks.equals(((ScriptIt) other).codeBlocks);
		}
		return false;
	}

	/**
	 * A cause is a ScriptIt where all CodeBlocks have a subject and a slot.
	 * 
	 * @return true if all CodeBlocks have both subjects and slots.
	 * 
	 */
	public boolean isCause() {
		if (this.codeBlocks.size() == 0)
			return false;

		for (CodeBlock codeBlock : this.codeBlocks) {
			if (!codeBlock.hasSubject() || !codeBlock.hasSlot())
				return false;
		}
		return true;
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

		if (component.isCause()) {
			component
					.setStoryBlock((StoryItemSequence) component.childComponents
							.get(0));
			component
					.setAlwaysBlock((StoryItemSequence) component.childComponents
							.get(1));
		}

		return component;
	}

	/**
	 * Gets the container for children that are in the Story block of a cause.
	 * 
	 * @return
	 */
	public StoryItemSequence getStoryBlock() {
		return this.storyBlock;
	}

	/**
	 * Gets the container for children that are in the Always block of a cause.
	 * 
	 * @return
	 */
	public StoryItemSequence getAlwaysBlock() {
		return this.alwaysBlock;
	}

	public void setStoryBlock(StoryItemSequence storyBlock) {
		this.storyBlock = storyBlock;
		storyBlock.setOwner(this);
	}

	public void setAlwaysBlock(StoryItemSequence alwaysBlock) {
		this.alwaysBlock = alwaysBlock;
		alwaysBlock.setOwner(this);
	}

	/**
	 * TODO Handles these stupid If/ElseBlock pointers.. we need to fix this NOT
	 * THIS
	 */
	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		boolean success = super.addStoryChildBefore(newChild, sibling);
		if (success) {
			if (this.getChildren().iterator().next() == newChild)
				this.setStoryBlock((StoryItemSequence) newChild);
			else
				this.setAlwaysBlock((StoryItemSequence) newChild);
		}
		return success;
	}

	@Override
	public Collection<String> getTypes() {
		return this.getMainCodeBlock().getTypes();
	}

	public void setTypes(Collection<String> types) {
		this.getMainCodeBlock().setTypes(types);

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_SCRIPT_IT_TYPES));
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
	 * TODO: is this what we want?
	 * 
	 * @return
	 */
	public CodeBlock getMainCodeBlock() {
		return this.codeBlocks.iterator().next();
	}

	public void removeCodeBlock(CodeBlock codeBlock) {
		if (this.codeBlocks.remove(codeBlock)) {
			codeBlock.setOwner(null);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODEBLOCK_REMOVED));
		}

		this.updateStoryChildren();
	}

	public void addCodeBlock(CodeBlock codeBlock) {
		if (this.codeBlocks.add(codeBlock)) {
			codeBlock.setOwner(this);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODEBLOCK_ADDED));
		}

		this.updateStoryChildren();
	}

	/**
	 * Updates the ScriptIt to display the Story and Always blocks, depending on
	 * if it is a cause or not.
	 */
	public void updateStoryChildren() {
		if (this.isCause()) {
			if (!this.childComponents.contains(this.storyBlock))
				this.addStoryChild(this.storyBlock);
			if (!this.childComponents.contains(this.alwaysBlock))
				this.addStoryChild(this.alwaysBlock);
		} else {
			this.removeStoryChild(this.storyBlock);
			this.removeStoryChild(this.alwaysBlock);
		}
	}

	public void setCodeBlocks(Collection<CodeBlock> codeBlocks) {
		for (CodeBlock codeBlock : this.codeBlocks) {
			this.removeCodeBlock(codeBlock);
		}
		for (CodeBlock codeBlock : codeBlocks) {
			this.addCodeBlock(codeBlock);
		}

		this.updateStoryChildren();

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CODE_BLOCKS_SET));
	}

	public final void processParameters(StoryVisitor processController) {
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

}
