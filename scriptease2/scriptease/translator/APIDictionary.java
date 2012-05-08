package scriptease.translator;

import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.model.LibraryModel;

/**
 * APIDictionary represents the apidictionary used by a Translator to represent
 * vital game information, such as causes, definitions, effects, types and slots
 * avaliable in the game. It uses a LibraryModel to manage the StoryComponents,
 * GameTypeManager to manage the types, and EventSlotManager to manage the
 * slots.
 * 
 * @author mfchurch
 * 
 */
public class APIDictionary {
	private LibraryModel library;
	private GameTypeManager typeManager;
	private EventSlotManager slotManager;

	public APIDictionary() {
		this.library = new LibraryModel();
		this.typeManager = new GameTypeManager();
		this.slotManager = new EventSlotManager();
	}

	// //////////////////////
	// Getters and Setters //
	// //////////////////////

	public GameTypeManager getGameTypeManager() {
		return this.typeManager;
	}

	public EventSlotManager getEventSlotManager() {
		return this.slotManager;
	}

	public LibraryModel getLibrary() {
		return this.library;
	}

	public String getName() {
		return this.library.getTitle();
	}

	public String getAuthor() {
		return this.library.getAuthor();
	}

	public void setName(String name) {
		this.library.setTitle(name);
	}

	public void setAuthor(String author) {
		this.library.setAuthor(author);
	}

	@Override
	public String toString() {
		return "APIDictionary [" + this.getName() + "]";
	}
}
