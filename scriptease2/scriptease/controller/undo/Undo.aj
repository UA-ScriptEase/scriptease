package scriptease.controller.undo;

import java.util.Collection;

import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.EditableResource;
import scriptease.translator.io.model.Resource;

/**
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
 * @author kschenk
 * @author jyuen
 */
public aspect Undo {
	/*
	 * =========================== Undo.aj 101 =================================
	 * 
	 * First off, everything in this class is model side. So if you have a
	 * button that adds a child to a resource, we worry about the addition, not
	 * the button. We don't even care if the button changes colour; that's not
	 * Undo's problem. That should be handled by a listener on the button.
	 * 
	 * After you find exactly which piece of the model is getting changed, you
	 * need to remember the opposite action of this. So if we're adding
	 * children, the opposite would be removing them. Remember this.
	 * 
	 * We can now add code to this class. First, create a pointcut just like the
	 * ones that already exist.
	 * 
	 * Let's use one of the most simple ones to understand how to do this.
	 * 
	 * ============================ POINTCUTS ==================================
	 * 
	 * public pointcut addingResourceChild(): within(EditableResource+) &&
	 * execution(* addChild(Resource+));
	 * 
	 * Dissection:
	 * 
	 * "public pointcut addingResourceChild():" - the definition.
	 * 
	 * "within(EditableResource+)" -The class that contains the method we want
	 * to undo. The + indicates that we also want to undo the method for every
	 * class that extends this one.
	 * 
	 * "&& execution(* addChild(Resource+));" - The method that we want to undo.
	 * Note that the parameter is a type, and we also add the plus here to
	 * indicate that subclasses of Resource should also be undone.
	 * 
	 * ============================= ADVICE ====================================
	 * 
	 * The point cut tells this Aspect that we should be looking at a certain
	 * method inside a certain class. Any time that method is called, the Aspect
	 * knows. Now we actually need to do something. This is what Advice is for.
	 * We can choose to do things before, after, or while a method executes.
	 * 
	 * What follows is an examination of a "before" advice. They all work
	 * similarly, though, and you only need to worry about "before" for Undo.
	 * 
	 * "before(final EditableResource resource, final Resource child):" - The
	 * parameters for the advice. This will include any parameters in the method
	 * we are cutting into, and also the object for which we are undoing stuff.
	 * In this case, we have the resource that a child is getting added to
	 * (this) and the child itself (.addChild(child)).
	 * 
	 * "addingResourceChild()" - This is the name of the pointcut.
	 * 
	 * "&& args(child)" - All arguments are passed in like this. The
	 * "addChild(Resource)" method has one parameter. We give it the same name
	 * as one of the parameters in the advice declaration so that "child" in the
	 * called method is assigned to "child" in the advice. To add more
	 * parameters, you would just use another "&& args(param)".
	 * 
	 * "&& this(resource) {" - This is a special type of parameter. It returns
	 * what would get returned when we call "this" in the undone method, then
	 * assigns it to resource.
	 * 
	 * Everything in the curly brackets is what gets executed. In this case, it
	 * adds a new modification to the UndoManager's modifications list. I'm not
	 * going to copy this part of the code, as it's pretty straight forward.
	 * Just remember that you can use the two variables, resource and child,
	 * that we defined earlier.
	 * 
	 * ========================= IMPLEMENTATION ================================
	 * 
	 * We are now able to track undoable changes. However, we still need to tell
	 * the UndoManager that we should start undoing something. Since there could
	 * be places where we want to add or remove a child where it shouldn't be
	 * undoable, such as when loading a file, we don't do it in the model. We go
	 * back to the button for this and call the relevant methods to open an
	 * undoable action. This also lets us undo more than one modification at a
	 * time. Always remember to close your undoable action after all the
	 * undoable things are done with.
	 * 
	 * 
	 * Remember to test, test, test this so that nothing breaks. You may have to
	 * start the undoable action at a higher or lower level in different
	 * situations.
	 */

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
	 * This is duplicated because just implementing for CodeBlock will end up
	 * firing twice as the subclasses call super causing the pointcut twice
	 */
	public pointcut addingParameter():
		(within(CodeBlockSource) && execution(* addParameter(KnowIt))) || (within(CodeBlockReference) && execution(* addParameter(KnowIt)));

	/**
	 * This is duplicated because just implementing for CodeBlock will end up
	 * firing twice as the subclasses call super causing the pointcut twice
	 */
	public pointcut removingParameter():
		(within(CodeBlockSource) && execution(* removeParameter(KnowIt))) || (within(CodeBlockReference) && execution(* removeParameter(KnowIt)));

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

	public pointcut settingEnabled():
		within(StoryComponent+) && execution(* setEnabled(Boolean));

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

	public pointcut addingCodeBlockType():
		within (CodeBlock+) && execution(* addType(String));

	public pointcut removingCodeBlockType():
		within (CodeBlock+) && execution(* removeType(String));

	public pointcut settingCodeBlockCode():
		within(CodeBlock+) && execution(* setCode(Collection<AbstractFragment>));

	public pointcut settingCodeBlockSubject():
		within (CodeBlock+) && execution(* setSubject(String));

	public pointcut settingCodeBlockSlot():
		within (CodeBlock+) && execution(* setSlot(String));

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
		within(SEModel+) && execution(* setTitle(String));

	/**
	 * Defines the Set Author operation in PatternModels.
	 */
	public pointcut settingAuthor():
		within(SEModel+) && execution(* setAuthor(String));

	/**
	 * Defines the Set Root operation in LibraryModels.
	 */
	public pointcut settingLibraryModelRoot():
		within(LibraryModel+) && execution(* setRoot(StoryComponent+));

	public pointcut deletingStoryComponent():
		within(LibraryModel+) && execution(* remove(StoryComponent+));

	public pointcut addingStoryComponent():
		within(LibraryModel+) && execution(* add(StoryComponent+));

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
	 * Defines the Add Successor operation in StoryNodes
	 */
	public pointcut addingSuccessor():
		within(StoryNode+) && execution(* addSuccessor(StoryNode+));

	/**
	 * Defines the Remove Successor operation in StoryNodes.
	 */
	public pointcut removingSuccessor():
		within(StoryNode+) && execution(* removeSuccessor(StoryNode+));

	/**
	 * Defines the Add Child operation in EditableResource.
	 */
	public pointcut addingResourceChild():
		within(EditableResource+) && execution(* addChild(Resource+));

	/**
	 * Defines the Remove Child operation in EditableResource.
	 */
	public pointcut removingResourceChild():
		within(EditableResource+) && execution(* removeChild(Resource+));

	/**
	 * Defines the Add Dialogue operation in EditableResource.
	 */
	public pointcut addingDialogueRoot():
		within(StoryModel+) && execution(* addDialogueRoot(DialogueLine+));

	/**
	 * Defines the Remove Dialogue operation in EditableResource.
	 */
	public pointcut removingDialogueRoot():
		within(StoryModel+) && execution(* removeDialogueRoot(DialogueLine+));

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

	before(final StoryComponent owner, final Boolean enable): settingEnabled() && args(enable) && this(owner) {
		Modification mod = new FieldModification<Boolean>(enable,
				owner.isEnabled()) {
			@Override
			public void setOp(Boolean value) {
				owner.setEnabled(value);
			}

			@Override
			public String toString() {
				return "setting " + owner + "'s enable factor to " + enable;
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

	before(final SEModel owner, final String newTitle): settingTitle() && args(newTitle) && this(owner){
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

	before(final SEModel owner, final String newAuthor): settingAuthor() && args(newAuthor) && this(owner){
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

	before(final LibraryModel model, final StoryComponent toRemove): deletingStoryComponent() && args(toRemove) && this(model) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				model.remove(toRemove);
			}

			@Override
			public void undo() {
				model.add(toRemove);
			}

			@Override
			public String toString() {
				return "deleting " + toRemove + " from " + model;
			}
		};
		this.addModification(mod);
	}

	before(final LibraryModel model, final StoryComponent toAdd): addingStoryComponent() && args(toAdd) && this(model) {
		Modification mod = new Modification() {
			@Override
			public void redo() {
				model.add(toAdd);
			}

			@Override
			public void undo() {
				model.remove(toAdd);
			}

			@Override
			public String toString() {
				return "adding " + toAdd + " to " + model;
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

	before(final CodeBlock codeBlock, final String type): addingCodeBlockType() && args(type) && this(codeBlock) {
		Modification mod = new Modification() {
			@Override
			public void redo() {
				codeBlock.addType(type);
			}

			@Override
			public void undo() {
				codeBlock.removeType(type);
			}

			@Override
			public String toString() {
				return "adding type " + type + " to " + codeBlock;
			}
		};
		this.addModification(mod);
	}

	before(final CodeBlock codeBlock, final String type): removingCodeBlockType() && args(type) && this(codeBlock) {
		Modification mod = new Modification() {
			@Override
			public void redo() {
				codeBlock.removeType(type);
			}

			@Override
			public void undo() {
				codeBlock.addType(type);
			}

			@Override
			public String toString() {
				return "removing type " + type + " to " + codeBlock;
			}
		};
		this.addModification(mod);
	}

	before(final CodeBlock codeBlock, final Collection<AbstractFragment> code): settingCodeBlockCode() && args(code) && this(codeBlock) {
		Modification mod = new FieldModification<Collection<AbstractFragment>>(
				code, codeBlock.getCode()) {
			public void setOp(Collection<AbstractFragment> newCode) {
				codeBlock.setCode(newCode);
			};

			@Override
			public String toString() {
				return "setting " + codeBlock + "'s code to " + code;
			}
		};

		this.addModification(mod);
	}

	before(final CodeBlock codeBlock, final KnowIt parameter): addingParameter() && args(parameter) && this(codeBlock) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				codeBlock.addParameter(parameter);
			}

			@Override
			public void undo() {
				codeBlock.removeParameter(parameter);
			}

			@Override
			public String toString() {
				return "adding " + parameter + " parameter to " + codeBlock;
			}
		};
		this.addModification(mod);
	}

	before(final CodeBlock codeBlock, final KnowIt parameter): removingParameter() && args(parameter) && this(codeBlock) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				codeBlock.removeParameter(parameter);
			}

			@Override
			public void undo() {
				codeBlock.addParameter(parameter);
			}

			@Override
			public String toString() {
				return "removing " + parameter + " parameter from " + codeBlock;
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

	before(final StoryNode storyNode, final StoryNode successor): addingSuccessor() && args(successor) && this(storyNode) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				storyNode.addSuccessor(successor);
			}

			@Override
			public void undo() {
				storyNode.removeSuccessor(successor);
			}

			@Override
			public String toString() {
				return "adding " + successor + " to " + storyNode;
			}
		};
		this.addModification(mod);
	}

	before(final StoryNode storyNode, final StoryNode successor): removingSuccessor() && args(successor) && this(storyNode) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				storyNode.removeSuccessor(successor);
			}

			@Override
			public void undo() {
				storyNode.addSuccessor(successor);
			}

			@Override
			public String toString() {
				return "removing " + successor + " to " + storyNode;
			}
		};
		this.addModification(mod);
	}

	before(final EditableResource resource, final Resource child): addingResourceChild() && args(child) && this(resource) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				resource.addChild(child);
			}

			@Override
			public void undo() {
				resource.removeChild(child);
			}

			@Override
			public String toString() {
				return "adding" + child + " to " + resource;
			}
		};
		this.addModification(mod);
	}

	before(final EditableResource resource, final Resource child): removingResourceChild() && args(child) && this(resource) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				resource.removeChild(child);
			}

			@Override
			public void undo() {
				resource.addChild(child);
			}

			@Override
			public String toString() {
				return "removing" + child + " from " + resource;
			}
		};
		this.addModification(mod);
	}

	before(final StoryModel model, final DialogueLine line): addingDialogueRoot() && args(line) && this(model) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				model.addDialogueRoot(line);
			}

			@Override
			public void undo() {
				model.removeDialogueRoot(line);
			}

			@Override
			public String toString() {
				return "adding" + line + " to " + model;
			}
		};
		this.addModification(mod);
	}

	before(final StoryModel model, final DialogueLine line): removingDialogueRoot() && args(line) && this(model) {
		Modification mod = new Modification() {

			@Override
			public void redo() {
				model.removeDialogueRoot(line);
			}

			@Override
			public void undo() {
				model.addDialogueRoot(line);
			}

			@Override
			public String toString() {
				return "removing" + line + " from " + model;
			}
		};
		this.addModification(mod);
	}
}
