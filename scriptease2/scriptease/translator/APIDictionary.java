package scriptease.translator;

import scriptease.model.LibraryModel;
import scriptease.translator.apimanagers.DescribeItManager;
import scriptease.translator.apimanagers.EventSlotManager;
import scriptease.translator.apimanagers.GameTypeManager;

/**
 * APIDictionary represents the API dictionary used by a Translator to represent
 * vital game information, such as causes, definitions, effects, types and slots
 * available in the game. It uses a LibraryModel to store the StoryComponents,
 * GameTypeManager to manage the types, and EventSlotManager to manage the
 * slots.<br>
 * <br>
 * APIDictionary also acts as a bit of manager for CodeBlockSources. It is what
 * knows the next available unique ID to be distributed.
 * 
 * @author mfchurch
 * @author remiller
 */
public class APIDictionary {
	private final LibraryModel library;
	private final DescribeItManager describeItManager;
	private final GameTypeManager typeManager;
	private final EventSlotManager slotManager;

	/**
	 * Builds a new API Dictionary with no name, author, or translator set.
	 * 
	 */
	public APIDictionary() {
		this(new LibraryModel());
	}

	/**
	 * Builds a new API Dictionary with the given title, author, and translator.
	 * 
	 * @param title
	 * @param author
	 * @param translator
	 */
	public APIDictionary(String title, String author, Translator translator) {
		this(new LibraryModel(title, author, translator));
	}

	/**
	 * Builds a new API Dictionary with the given library.
	 * 
	 * @param library
	 */
	public APIDictionary(LibraryModel library) {
		this.library = library;
		this.typeManager = new GameTypeManager();
		this.slotManager = new EventSlotManager();
		this.describeItManager = new DescribeItManager();
	}

	// //////////////////////
	// Getters and Setters //
	// //////////////////////
	public DescribeItManager getDescribeItManager() {
		return this.describeItManager;
	}

	public GameTypeManager getGameTypeManager() {
		return this.typeManager;
	}

	public EventSlotManager getEventSlotManager() {
		return this.slotManager;
	}

	public LibraryModel getLibrary() {
		return this.library;
	}

	/**
	 * Returns the name of the APIDictionary, which is the name of its library.
	 * 
	 * @return
	 */
	public String getName() {
		return this.library.getTitle();
	}

	/**
	 * Returns the author of the APIDictionary, which is the author of its
	 * library.
	 * 
	 * @return
	 */
	public String getAuthor() {
		return this.library.getAuthor();
	}

	/**
	 * Returns the translator of the APIDictionary, which is the translator of
	 * its library.
	 * 
	 * @return
	 */
	public Translator getTranslator() {
		return this.library.getTranslator();
	}

	/**
	 * Sets the name of the APIDictionary, which is the name of its library.
	 * 
	 * @return
	 */
	public void setName(String name) {
		this.library.setTitle(name);
	}

	/**
	 * Sets the author of the APIDictionary, which is the author of its library.
	 * 
	 * @return
	 */
	public void setAuthor(String author) {
		this.library.setAuthor(author);
	}

	/**
	 * Sets the translator of the APIDictionary, which is the translator of its
	 * library.
	 * 
	 * @return
	 */
	public void setTranslator(Translator translator) {
		this.library.setTranslator(translator);
	}

	@Override
	public String toString() {
		return "APIDictionary [" + this.getName() + "]";
	}
}