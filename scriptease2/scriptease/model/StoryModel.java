package scriptease.model;

import java.util.Collection;

import scriptease.controller.ModelVisitor;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;

/**
 * A StoryModel is the binding object that associates a GameModule with a
 * StoryComponent tree so the tree can access its possible bindings.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 */
public final class StoryModel extends SEModel {
	private final GameModule module;
	private final Translator translator;
	private final Collection<LibraryModel> libraries;
	private StoryPoint startPoint;

	/**
	 * Full constructor for {@code ScriptEaseStoryModule}. Builds a
	 * {@code ScriptEaseStoryModule} with the supplied root model node, module
	 * and author.
	 * 
	 * @param module
	 *            The {@link GameModule} associated with the model objects. The
	 *            StoryModel's model objects can access only the game data from
	 *            this <code>module</code>. If <code>module</code> is null, this
	 *            model is considered to be a code library.
	 * @param title
	 *            The name of the Story.
	 * @param author
	 *            The author's name.
	 * @param translator
	 *            The Translator to use to interpret this story.
	 */
	public StoryModel(GameModule module, String title, String author,
			Translator translator, Collection<LibraryModel> libraries) {
		super(title, author);

		// TODO Where this gets called, need to add libraries ^

		this.startPoint = new StoryPoint("Start");
		this.module = module;
		this.translator = translator;
		this.libraries = libraries;

		// Adds all of the automatic causes to the start point.
		for (LibraryModel library : translator.getLibraries()) {
			for (Resource resource : module.getAutomaticHandlers()) {
				for (ScriptIt automatic : library.getAutomatics()) {

					final ScriptIt copy = automatic.clone();

					for (KnowIt parameter : copy.getParameters()) {
						if (!parameter.getTypes().containsAll(
								resource.getTypes()))
							throw new IllegalArgumentException(
									"Found invalid types for automatics");
						parameter.setBinding(resource);

						this.startPoint.addStoryChild(copy);
					}
				}
			}
		}
	}

	public void setStartPoint(StoryPoint startPoint) {
		if (startPoint == null)
			throw new IllegalArgumentException(
					"Cannot give StoryModel a null tree root.");
		this.startPoint = startPoint;
	}

	public StoryPoint getRoot() {
		return this.startPoint;
	}

	/**
	 * Gets the module used for bindings in this Story.
	 * 
	 * @return the module associated with this Story.
	 */
	public GameModule getModule() {
		return this.module;
	}

	@Override
	public Translator getTranslator() {
		return this.translator;
	}

	@Override
	public String toString() {
		return "StoryModel [" + this.getName() + "]";
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processStoryModel(this);
	}
}
