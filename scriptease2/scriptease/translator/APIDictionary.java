package scriptease.translator;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryObserver;
import scriptease.model.CodeBlock;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
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
public class APIDictionary implements LibraryObserver {
	private final LibraryModel library;
	private final DescribeItManager describeItManager;
	private final GameTypeManager typeManager;
	private final EventSlotManager slotManager;
	private int nextID;

	/**
	 * Builds a new API Dictionary with the given next ID.
	 * 
	 * @param nextId
	 *            the next id available to be assigned to a CodeBlockSource.
	 */
	public APIDictionary() {
		this.library = new LibraryModel();
		this.typeManager = new GameTypeManager();
		this.slotManager = new EventSlotManager();
		this.describeItManager = new DescribeItManager();

		this.library.addLibraryChangeListener(this);
	}

	@Override
	public void modelChanged(LibraryModel model, LibraryEvent event) {
		final StoryComponent source = event.getSource();

		if (event.getEventType() == LibraryEvent.Type.ADDITION) {
			if (source instanceof ScriptIt) {
				final List<CodeBlock> codeBlocks;

				codeBlocks = new ArrayList<CodeBlock>(
						((ScriptIt) source).getCodeBlocks());

				for (CodeBlock codeBlock : codeBlocks) {
					this.nextID = Math.max(codeBlock.getId() + 1, this.nextID);
				}
			}
		}
	};

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

	public String getName() {
		return this.library.getTitle();
	}

	public String getAuthor() {
		return this.library.getAuthor();
	}

	public Translator getTranslator() {
		return this.library.getTranslator();
	}

	public void setName(String name) {
		this.library.setTitle(name);
	}

	public void setAuthor(String author) {
		this.library.setAuthor(author);
	}

	public void setTranslator(Translator translator) {
		this.library.setTranslator(translator);
	}

	@Override
	public String toString() {
		return "APIDictionary [" + this.getName() + "]";
	}

	public List<CodeBlock> getCodeBlocksByName(String scriptValue) {
		final List<StoryComponent> componentList;

		componentList = this.library.getEffectsCategory().getChildren();

		for (StoryComponent component : componentList) {
			if (component instanceof ScriptIt
					&& component.getDisplayText().equals(scriptValue)) {
				return new ArrayList<CodeBlock>(
						((ScriptIt) component).getCodeBlocks());
			}
		}

		return null;
	}

	/**
	 * Retrieves the next code block unique ID for this translator.
	 * 
	 * @return The next available unique id for a code block.
	 */
	public int getNextCodeBlockID() {
		return this.nextID++;
	}
}