package scriptease.controller;

import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.FunctionIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryPoint;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;

/**
 * Default implementation of StoryVisitor that does nothing. Ever. <br>
 * <br>
 * It is <b>stylistically required</b> that all other StoryVisitor
 * implementations extend this class, allowing us to avoid having to update all
 * of the visitors whenever the interface changes. Subclasses also get the perk
 * of only having to override the methods they <i>do</i> support.<br>
 * <br>
 * Subclasses that wish to provide default behaviour for processing can override
 * either {@link #defaultProcessAtomic(StoryComponent)} for atoms,
 * {@link #defaultProcessComplex(ComplexStoryComponent)} for
 * ComplexStroyComponents, or {@link #defaultProcess(StoryComponent)} for
 * anything. <br>
 * <br>
 * StoryAdapter is an Adapter (of the Adapter design pattern) to
 * StoryVisitor.
 * 
 * @author jtduncan
 * @author remiller
 * @author jyuen
 */
public abstract class StoryAdapter implements StoryVisitor {
	/*
	 * ============ COMPLEX TYPES ============
	 */
	@Override
	public void processFunctionIt(FunctionIt functionIt) {
		this.defaultProcessComplex(functionIt);
	}
	
	@Override
	public void processBehaviour(Behaviour behaviour) {
		this.processScriptIt(behaviour);
	}
	
	@Override
	public void processIndependentTask(IndependentTask task) {
		this.defaultProcessComplex(task);
	}
	
	@Override
	public void processCollaborativeTask(CollaborativeTask task) {
		this.defaultProcessComplex(task);
	}
	
	@Override
	public void processStoryGroup(StoryGroup storyGroup) {
		this.defaultProcessComplex(storyGroup);
	}
	
	@Override
	public void processStoryPoint(StoryPoint storyPoint) {
		this.defaultProcessComplex(storyPoint);
	}

	@Override
	public void processStoryComponentContainer(StoryComponentContainer container) {
		this.defaultProcessComplex(container);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		this.defaultProcessComplex(scriptIt);
	}

	public void processControlIt(ControlIt controlIt) {
		this.processScriptIt(controlIt);
	};
	
	public void processCauseIt(CauseIt causeIt) {
		this.processScriptIt(causeIt);
	}

	@Override
	public void processAskIt(AskIt questionIt) {
		this.defaultProcessComplex(questionIt);
	}
	
	@Override
	public void processPickIt(PickIt pickIt) {
		this.defaultProcessComplex(pickIt);
	}

	/*
	 * ============ ATOMIC TYPES ============
	 */

	@Override
	public void processKnowIt(KnowIt knowIt) {
		this.defaultProcessAtomic(knowIt);
	}

	@Override
	public void processCodeBlockSource(CodeBlockSource codeBlockSource) {
		this.defaultProcessAtomic(codeBlockSource);
	}

	@Override
	public void processCodeBlockReference(CodeBlockReference codeBlockReference) {
		this.defaultProcessAtomic(codeBlockReference);
	}

	@Override
	public void processNote(Note note) {
		this.defaultProcessAtomic(note);
	}

	/*
	 * ============ Defaults ============
	 */
	/**
	 * The default process method that is called by every non-overridden
	 * <code>process<i>X</i>(<i>X</i> <i>x</i>)</code> method where X is an
	 * atom.<br>
	 * <br>
	 * Override this method if you want to provide a non-null default behaviour
	 * for processing atoms. Unless it is overridden, it does nothing.
	 * 
	 * @param atom
	 *            The ComplexStroyComponent to process with a default behaviour.
	 */
	protected void defaultProcessAtomic(StoryComponent atom) {
		this.defaultProcess(atom);
	}

	/**
	 * The default process method that is called by every
	 * <code>process<i>Y</i>(<i>&lt;Y extends ComplexStoryComponent&gt;</i> <i>y</i>)</code>
	 * method in this class' standard Complex Story Component methods. <br>
	 * <br>
	 * Override this method if you want to provide a non-null default behaviour
	 * (like processing children, for example) for every non-overridden
	 * <code>process<i>Y</i></code> method. Unless it is overridden, it does
	 * nothing.
	 * 
	 * @param complex
	 *            The ComplexStroyComponent to process with a default behaviour.
	 */
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		this.defaultProcess(complex);
	}

	/**
	 * The default process method that is called by every
	 * process<i>Z</i>(<i>Z</i> <i>z</i>) method in this class' standard
	 * methods. <br>
	 * <br>
	 * Override this method if you want to provide a non-null default behaviour
	 * for every non-overridden process<i>Z</i> method. Unless it is overridden,
	 * it does nothing.
	 * 
	 * @param component
	 *            The StoryComponent to process with a default behaviour.
	 */
	protected void defaultProcess(StoryComponent component) {
	}
}
