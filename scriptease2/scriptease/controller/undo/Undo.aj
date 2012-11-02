package scriptease.controller.undo;

import java.util.Collection;

import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.CodeBlock;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;

/*
 * This aspect inserts Undo-specific code into any model object that must
 * support undoable operations (called Undo-Active Objects). This includes
 * <code>StoryComponent</code>s, <code>PatternModel</code>s, and their
 * subclasses. The undo code that is inserted notifies the UndoManager of model
 * modifications, which the UndoManager records.<br>
 * <br>
 * This aspect assumes and requires that the Undo-Active Objects adhere to the
 * following conventions:
 * <ol>
 * <li>any state change made internally is done using its external interface.
 * For example, if a StoryComponent has instance variable <code>int A</code>,
 * the only place that <code>A = <i>value</i></code> is allowed is its public
 * <code>setA(int newA)</code> method.</li>
 * <li>all methods that change the StoryComponent's state in the class's public
 * interface match one of the following forms: <code>set*(...)</code>,
 * <code>add*(...)</code>, or <code>remove*(...)</code>.</li>
 * </ol>
 * 
 * The first convention ensures that there are never any model changes that go
 * un-recorded by the Undo System. The second convention exists to allow the
 * UndoErrors aspect to help protect against errors.<br>
 * <br>
 * The modification is created in the before advice, and is stored in this
 * aspect until the the operation has successfully completed. This functionality
 * may be do-able with around() advice instead, but I don't have any more time
 * to work on this - remiller.
 * 
 * 
 * @author remiller
 */
public aspect Undo {
	/*
	 * ====================== UNDO POINT CUTS ======================
	 */
	/**
	 * Defines cloning a StoryComponent
	 */

	public pointcut cloneKnowIt():
		within(KnowIt) && execution(* clone());

	public pointcut cloneAskIt():
		within(AskIt) && execution(* clone());

	public pointcut cloneScriptIt():
		within(ScriptIt) && execution(* clone());

	/**
	 * Defines set name operations in StoryComponents.
	 */
	public pointcut settingName():
		within(StoryComponent+) && execution(* setDisplayText(String));

	/**
	 * Defines the Add Label operation in StoryComponents.
	 */
	public pointcut addingLabel():
		within(StoryComponent+) && execution(* addLabel(String));

	/**
	 * Defines the Remove Label operation in StoryComponents.
	 */
	public pointcut removingLabel():
		within(StoryComponent+) && execution(* removeLabel(String));

	/**
	 * Defines the Set Label operation in StoryComponents.
	 */
	public pointcut settingLabels():
		within(StoryComponent+) && execution(* setLabels(Collection<String>));

	public pointcut settingVisible():
		within(StoryComponent+) && execution(* setVisible(Boolean));

	/**
	 * Defines the Add Observer operation in StoryComponents.
	 */
	public pointcut addingObserver():
		within(StoryComponent+) && execution(* addStoryComponentObserver(StoryComponentObserver+));

	/**
	 * Defines the Remove Observer operation in StoryComponents.
	 */
	public pointcut removingObserver():
		within(StoryComponent+) && execution(* removeStoryComponentObserver(StoryComponentObserver+));

	/**
	 * Defines the Set Keyword operation in DoIts.
	 */
	public pointcut settingKeyword():
		within(ScriptIt+) && execution(* setKeyword(String));

	/**
	 * Defines the Set Binding operation in KnowIts.
	 */
	public pointcut settingBinding():  
		within(KnowIt+) && execution(* setBinding(..));

	/**
	 * Defines the Add Type operation in KnowIts.
	 */
	public pointcut addingType():
		within (KnowIt+) && execution(* addType(String));

	/**
	 * Defines the Remove Type operation in KnowIts.
	 */
	public pointcut removingType():
		within (KnowIt+) && execution(* removeType(String));

	/**
	 * Defines the Clear Types operation in KnowIts.
	 */
	public pointcut clearingTypes():
		within (KnowIt+) && execution(* clearTypes());

	/**
	 * Defines the Add Story Child operation in ComplexStoryComponents.
	 */
	public pointcut addingChild():
		within(ComplexStoryComponent+) && execution(* addStoryChildBefore(StoryComponent+, StoryComponent+));

	/**
	 * Defines the Remove Story Child operation in ComplexStoryComponents.
	 */
	public pointcut removingChild():
		within(ComplexStoryComponent+) && execution(* removeStoryChild(StoryComponent+));

	/**
	 * Defines the Set Condition operation in AskIts.
	 */
	public pointcut settingCondition():
		within(AskIt+) && execution(* setCondition(KnowIt+));

	/**
	 * Defines the Set Title operation in PatternModels.
	 */
	public pointcut settingTitle():
		within(PatternModel+) && execution(* setTitle(String));

	/**
	 * Defines the Set Author operation in PatternModels.
	 */
	public pointcut settingAuthor():
		within(PatternModel+) && execution(* setAuthor(String));

	/**
	 * Defines the Set Root operation in LibraryModels.
	 */
	public pointcut settingLibraryModelRoot():
		within(LibraryModel+) && execution(* setRoot(StoryComponent+));

	/**
	 * Defines the Set Fan In operation in StoryPoints.
	 */
	public pointcut settingFanIn():
		within(StoryPoint+) && execution(* setFanIn(Integer+));

	/**
	 * Defines the AddCodeBlock operation in ScriptIts.
	 */
	public pointcut addingCodeBlock():
		within(ScriptIt+) && execution(* addCodeBlock(CodeBlock+));

	/**
	 * Defines the RemoveCodeBlock operation in ScriptIts.
	 */
	public pointcut removingCodeBlock():
		within(ScriptIt+) && execution(* removeCodeBlock(CodeBlock+));

	/**
	 * Defines the Add Successor operation in StoryPoints.
	 */
	public pointcut addingSuccessor():
		within(StoryPoint+) && execution(* addSuccessor(StoryPoint+));

	/**
	 * Defines the Remove Successor operation in StoryPoints.
	 */
	public pointcut removingSuccessor():
		within(StoryPoint+) && execution(* removeSuccessor(StoryPoint+));

	/*
	 * ====================== ADVICE ======================
	 */
	private boolean recording = true;

	private void addModification(Modification mod) {
		final UndoManager undo = UndoManager.getInstance();
		if (undo.hasOpenUndoableAction() && this.recording)
			undo.appendModification(mod);
	}

	KnowIt around(): cloneKnowIt() {
		KnowIt component;
		// Only stop recording once, if it current is recording
		if (this.recording) {
			this.recording = false;
			component = (KnowIt) proceed();
			this.recording = true;
		} else
			component = (KnowIt) proceed();
		return component;
	}

	AskIt around(): cloneAskIt() {
		AskIt component;
		// Only stop recording once, if it current is recording
		if (this.recording) {
			this.recording = false;
			component = (AskIt) proceed();
			this.recording = true;
		} else
			component = (AskIt) proceed();
		return component;
	}

	ScriptIt around(): cloneScriptIt() {
		ScriptIt component;
		// Only stop recording once, if it current is recording
		if (this.recording) {
			this.recording = false;
			component = (ScriptIt) proceed();
			this.recording = true;
		} else
			component = (ScriptIt) proceed();
		return component;
	}

	before(final StoryComponent owner, final String newName): settingName() && args(newName) && this(owner){
		Modification mod = new FieldModification<String>(newName,
				owner.getDisplayText()) {
			@Override
			public void setOp(String value) {
				owner.setDisplayText(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s name to " + newName;
			}
		};
		this.addModification(mod);
	}

	before(final StoryComponent owner, final String addedLabel): addingLabel() && args(addedLabel) && this(owner) {
		Modification mod = new Modification() {
			@Override
			public void redo() {
				owner.addLabel(addedLabel);
			}

			@Override
			public void undo() {
				owner.removeLabel(addedLabel);
			}

			@Override
			public String toString() {
				return "adding label " + addedLabel + " to " + owner;
			}

		};
		this.addModification(mod);
	}

	before(final StoryComponent owner, final String removedLabel): removingLabel() && args(removedLabel) && this(owner){
		Modification mod = new Modification() {
			@Override
			public void redo() {
				owner.removeLabel(removedLabel);
			}

			@Override
			public void undo() {
				owner.addLabel(removedLabel);
			}

			@Override
			public String toString() {
				return "removing parameter " + removedLabel + " from " + owner;
			}
		};
		this.addModification(mod);
	}

	before(final StoryComponent owner, final Collection<String> labels): settingLabels() && args(labels) && this(owner) {
		Modification mod = new FieldModification<Collection<String>>(labels,
				owner.getLabels()) {
			@Override
			public void setOp(Collection<String> value) {
				owner.setLabels(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s labels to " + labels;
			}
		};
		this.addModification(mod);
	}

	before(final StoryComponent owner, final Boolean visible): settingVisible() && args(visible) && this(owner) {
		Modification mod = new FieldModification<Boolean>(visible,
				owner.isVisible()) {
			@Override
			public void setOp(Boolean value) {
				owner.setVisible(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s visiblity to " + visible;
			}
		};
		this.addModification(mod);
	}

	before(final StoryComponent owner,
			final StoryComponentObserver addedObserver): addingObserver() && args(addedObserver) && this(owner){
		Modification mod = new Modification() {
			@Override
			public void redo() {
				owner.addStoryComponentObserver(addedObserver);
			}

			@Override
			public void undo() {
				owner.removeStoryComponentObserver(addedObserver);
			}

			@Override
			public String toString() {
				return "adding observer " + addedObserver + " to " + owner;
			}
		};
		this.addModification(mod);
	}

	before(final StoryComponent owner,
			final StoryComponentObserver removedObserver): removingObserver() && args(removedObserver) && this(owner){
		Modification mod = new Modification() {
			@Override
			public void redo() {
				owner.removeStoryComponentObserver(removedObserver);
			}

			@Override
			public void undo() {
				owner.addStoryComponentObserver(removedObserver);
			}

			@Override
			public String toString() {
				return "removing observer " + removedObserver + " from "
						+ owner;
			}
		};
		this.addModification(mod);
	}

	before(final KnowIt owner, final KnowItBinding newBinding): settingBinding() && args(newBinding) && this(owner) {
		Modification mod = new FieldModification<KnowItBinding>(newBinding,
				owner.getBinding()) {
			@Override
			public void setOp(KnowItBinding value) {
				owner.setBinding(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s binding to " + newBinding;
			}
		};
		this.addModification(mod);
	}

	before(final KnowIt owner, final String newType): addingType() && args(newType) && this(owner) {
		Modification mod = new Modification() {
			@Override
			public void redo() {
				owner.addType(newType);
			}

			@Override
			public void undo() {
				owner.removeType(newType);
			}

			@Override
			public String toString() {
				return "adding type " + newType + " to " + owner;
			}
		};
		this.addModification(mod);
	}

	before(final KnowIt owner, final String newType): removingType() && args(newType) && this(owner) {
		Modification mod = new Modification() {
			KnowItBinding binding = owner.getBinding();

			@Override
			public void redo() {
				owner.removeType(newType);
			}

			@Override
			public void undo() {
				owner.addType(newType);
				owner.setBinding(this.binding);
			}

			@Override
			public String toString() {
				return "removing type " + newType + " from " + owner;
			}
		};
		this.addModification(mod);
	}

	before(final ComplexStoryComponent owner, final StoryComponent addedChild,
			final StoryComponent sibling): 
								addingChild() && args(addedChild, sibling) && this(owner){
		Modification mod = new Modification() {
			@Override
			public void redo() {
				owner.addStoryChildBefore(addedChild, sibling);
			}

			@Override
			public void undo() {
				owner.removeStoryChild(addedChild);
			}

			@Override
			public String toString() {
				return "adding " + addedChild + " to " + owner;
			}
		};
		this.addModification(mod);
	}

	before(final ComplexStoryComponent owner, final StoryComponent removedChild): removingChild() && args(removedChild) && this(owner){
		Modification mod = new Modification() {
			private StoryComponent sibling = owner.getChildAfter(removedChild);

			@Override
			public void redo() {
				owner.removeStoryChild(removedChild);
			}

			@Override
			public void undo() {
				owner.addStoryChildBefore(removedChild, this.sibling);
			}

			@Override
			public String toString() {
				return "removing " + removedChild + " from " + owner;
			}
		};
		this.addModification(mod);
	}

	before(final AskIt owner, final KnowIt newCondition): settingCondition() && args(newCondition) && this(owner) {
		Modification mod = new FieldModification<KnowIt>(newCondition,
				owner.getCondition()) {
			@Override
			public void setOp(KnowIt value) {
				owner.setCondition(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s condition to" + newCondition;
			}
		};
		this.addModification(mod);
	}

	before(final PatternModel owner, final String newTitle): settingTitle() && args(newTitle) && this(owner){
		Modification mod = new FieldModification<String>(newTitle,
				owner.getTitle()) {
			@Override
			public void setOp(String value) {
				owner.setTitle(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s title to" + newTitle;
			}
		};
		this.addModification(mod);
	}

	before(final PatternModel owner, final String newAuthor): settingAuthor() && args(newAuthor) && this(owner){
		Modification mod = new FieldModification<String>(newAuthor,
				owner.getAuthor()) {
			@Override
			public void setOp(String value) {
				owner.setAuthor(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s author to" + newAuthor;
			}
		};
		this.addModification(mod);
	}

	before(final LibraryModel owner, final StoryComponentContainer newRoot): settingLibraryModelRoot() && args(newRoot) && this(owner) {
		Modification mod = new FieldModification<StoryComponentContainer>(
				newRoot, owner.getRoot()) {
			@Override
			public void setOp(StoryComponentContainer value) {
				owner.setRoot(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s root to" + newRoot;
			}
		};
		this.addModification(mod);
	}

	before(final StoryPoint storyPoint, final Integer fanIn): settingFanIn() && args(fanIn) && this(storyPoint) {
		Modification mod = new FieldModification<Integer>(fanIn,
				storyPoint.getFanIn()) {
			public void setOp(Integer value) {
				storyPoint.setFanIn(value);
			};

			@Override
			public String toString() {
				return "setting " + storyPoint + "'s fanin to " + fanIn;
			}
		};

		this.addModification(mod);
	}

	before(final ScriptIt scriptIt, final CodeBlock codeBlock): addingCodeBlock() && args(codeBlock) && this(scriptIt) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				scriptIt.addCodeBlock(codeBlock);
			}

			@Override
			public void undo() {
				scriptIt.removeCodeBlock(codeBlock);
			}

			@Override
			public String toString() {
				return "adding " + codeBlock + " to " + scriptIt;
			}
		};
		this.addModification(mod);
	}

	before(final ScriptIt scriptIt, final CodeBlock codeBlock): removingCodeBlock() && args(codeBlock) && this(scriptIt) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				scriptIt.removeCodeBlock(codeBlock);
			}

			@Override
			public void undo() {
				scriptIt.addCodeBlock(codeBlock);
			}

			@Override
			public String toString() {
				return "removing " + codeBlock + " to " + scriptIt;
			}
		};
		this.addModification(mod);
	}

	before(final StoryPoint storyPoint, final StoryPoint successor): addingSuccessor() && args(successor) && this(storyPoint) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				storyPoint.addSuccessor(successor);
			}

			@Override
			public void undo() {
				storyPoint.removeSuccessor(successor);
			}

			@Override
			public String toString() {
				return "adding " + successor + " to " + storyPoint;
			}
		};
		this.addModification(mod);
	}

	before(final StoryPoint storyPoint, final StoryPoint successor): removingSuccessor() && args(successor) && this(storyPoint) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				storyPoint.removeSuccessor(successor);
			}

			@Override
			public void undo() {
				storyPoint.addSuccessor(successor);
			}

			@Override
			public String toString() {
				return "removing " + successor + " to " + storyPoint;
			}
		};
		this.addModification(mod);
	}
}
