package scriptease.controller;

import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;
import scriptease.model.complex.StoryPoint;

/**
 * Generic controller object that is a collection of double dispatch methods to
 * correspond with a call to StoryComponent.process(). Pass an implementation of
 * <code>StoryVisitor</code> to a {@link StoryComponent}'s
 * {@link StoryComponent#process()} method to get type-specific behaviour.<br>
 * <br>
 * Classes should not implement this interface directly since it is strongly
 * recommended (and stylistically required) that they subclass
 * {@link StoryAdapter}. <br>
 * <br>
 * <code>StoryVisitor</code> is an implementation of the Visitor design pattern.
 * 
 * @author jtduncan
 * @author friesen
 * @author remiller
 * 
 * @see StoryAdapter
 */
public interface StoryVisitor {
	/** COMPLEX TYPES **/
	public void processStoryPoint(StoryPoint storyPoint);
	
	public void processScriptIt(ScriptIt scriptIt);

	public void processStoryComponentContainer(
			StoryComponentContainer storyComponentContainer);

	public void processStoryItemSequence(StoryItemSequence sequence);

	public void processAskIt(AskIt askIt);
	
	/** ATOMIC TYPES **/
	public void processKnowIt(KnowIt knowIt);

	public void processCodeBlockSource(CodeBlockSource codeBlockSource);
	
	public void processCodeBlockReference(CodeBlockReference codeBlockReference);

	public void processNote(Note note);
}
