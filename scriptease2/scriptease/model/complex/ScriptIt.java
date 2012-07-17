package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
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
 * 
 */
public class ScriptIt extends ComplexStoryComponent implements TypedComponent {
	protected Collection<CodeBlock> codeBlocks;

	public ScriptIt(String name) {
		super(name);
		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(StoryComponentContainer.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.codeBlocks = new ArrayList<CodeBlock>();
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
	public boolean canAcceptChild(StoryComponent potentialChild) {
		if (this.codeBlocks.size() == 0)
			return true;
		if (!this.isCause()
				|| (potentialChild instanceof ScriptIt && ((ScriptIt) potentialChild)
						.isCause()))
			// Causes should not have other Causes inside of them, nor should
			// Effects allow children. We don't even know what would mean. To
			// think of it is terrifying. - remiller
			return false;
		else
			return super.canAcceptChild(potentialChild);
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other) && other instanceof ScriptIt) {
			return this.codeBlocks.equals(((ScriptIt) other).codeBlocks);
		}
		return false;
	}

	/**
	 * A cause is a ScriptIt with a single CodeBlock which has a subject and
	 * slot
	 * 
	 * @return
	 */
	public boolean isCause() {
		int size = codeBlocks.size();
		if (size == 1) {
			CodeBlock main = this.getMainCodeBlock();
			return main.hasSubject() && main.hasSlot();
		} else
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

	/**
	 * TODO remove these kind of methods, since they are messy and we should be
	 * enforcing CodeBlock scoping.
	 */
	@Override
	public Collection<String> getTypes() {
		return this.getMainCodeBlock().getTypes();
	}

	/**
	 * Get the parameters for all of the codeBlocks
	 * 
	 * TODO remove these kind of methods, since they are messy and we should be
	 * enforcing CodeBlock scoping.
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
	 * TODO remove these kind of methods, since they are messy and we should be
	 * enforcing CodeBlock scoping.
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
	 * TODO remove these kind of methods, since they are messy and we should be
	 * enforcing CodeBlock scoping.
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
	}

	public void addCodeBlock(CodeBlock codeBlock) {
		if (this.codeBlocks.add(codeBlock)) {
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
