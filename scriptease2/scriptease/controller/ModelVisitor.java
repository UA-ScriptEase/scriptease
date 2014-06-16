package scriptease.controller;

import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;

/**
 * Generic controller object that is a collection of double dispatch methods to
 * correspond with a call to PatternModel.process(). Pass an implementation of
 * <code>ModelVisitor</code> to a {@link SEModel}'s
 * {@link SEModel#process()} method to get type-specific behaviour.<br>
 * <br>
 * Classes should not implement this interface directly since it is strongly
 * recommended (and stylistically required) that they subclass
 * {@link ModelAdapter}. <br>
 * <br>
 * <code>ModelVisitor</code> is an implementation of the Visitor design pattern.
 * 
 * @author kschenk
 * 
 * @see StoryVisitor
 * @see ModelAdapter
 */
public interface ModelVisitor {
	public void processStoryModel(StoryModel storyModel);
	
	public void processLibraryModel(LibraryModel libraryModel);
	
	public void processTranslator(Translator translator);
}
