package scriptease.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.model.CodeBlock;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * APIDictionary represents the API dictionary used by a Translator to represent
 * vital game information, such as causes, definitions, effects, types and slots
 * available in the game. It uses a LibraryModel to manage the StoryComponents,
 * GameTypeManager to manage the types, and EventSlotManager to manage the
 * slots.
 * 
 * @author mfchurch
 */
public class APIDictionary {
	private final LibraryModel library;
	private final GameTypeManager typeManager;
	private final EventSlotManager slotManager;
	private final Map<CodeBlock, Collection<FormatFragment>> codeBlocksToCodes;

	public APIDictionary() {
		this.library = new LibraryModel();
		this.typeManager = new GameTypeManager();
		this.slotManager = new EventSlotManager();
		this.codeBlocksToCodes = new HashMap<CodeBlock, Collection<FormatFragment>>();
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

	public List<CodeBlock> getCodeBlocksByID(String scriptValue) {
		List<StoryComponent> componentList = this.library.getEffectsCategory()
				.getChildren();
		List<CodeBlock> codeBlockList = new ArrayList<CodeBlock>();
		for (StoryComponent component : componentList) {
			if (component instanceof ScriptIt) {
				if (component.getDisplayText().equals(scriptValue)) {
					codeBlockList
							.addAll(((ScriptIt) component).getCodeBlocks());
					return codeBlockList;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the code for the given codeblock.
	 * 
	 * @param codeBlock
	 *            The code block whose code is to be set.
	 * @param code
	 *            The code to be used for the given code block.
	 */
	protected void setCode(CodeBlock codeBlock, Collection<FormatFragment> code) {
		this.codeBlocksToCodes.put(codeBlock, code);
	}

	/**
	 * Gets the game-specific code for the given codeblock.
	 * 
	 * @param codeBlock
	 *            The codeblock whose code is to be retrieved.
	 * @return The code for the given codeblock.
	 */
	protected Collection<FormatFragment> getCode(CodeBlock codeBlock) {
		return this.codeBlocksToCodes.get(codeBlock);
	}
}
