package scriptease.controller;

import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;

/**
 * Default implementation of ModelVisitor that does nothing. Ever. <br>
 * <br>
 * It is <b>stylistically required</b> that all other ModelVisitor
 * implementations extend this class, allowing us to avoid having to update all
 * of the visitors whenever the interface changes. Subclasses also get the perk
 * of only having to override the methods they <i>do</i> support.<br>
 * <br>
 * Subclasses that wish to provide default behaviour for processing can override
 * {@link #defaultProcess(SEModel)}. <br>
 * <br>
 * AbstractNoOpStoryVisitor is an Adapter (of the Adapter design pattern) to
 * ModelVisitor.
 * 
 * @author kschenk
 * @see ModelVisitor
 */
public abstract class ModelAdapter implements ModelVisitor {
	@Override
	public void processStoryModel(StoryModel storyModel) {
		this.defaultProcess(storyModel);
	}

	@Override
	public void processLibraryModel(LibraryModel libraryModel) {
		this.defaultProcess(libraryModel);
	}

	@Override
	public void processTranslator(Translator translatorModel) {
		this.defaultProcess(translatorModel);
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
	 * @param model
	 *            The PatternModel to process with a default behaviour.
	 */
	protected void defaultProcess(SEModel model) {
	}
}
