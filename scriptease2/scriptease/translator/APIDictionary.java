package scriptease.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import scriptease.controller.QuestPointNodeGetter;
import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.quests.QuestNode;
import scriptease.model.CodeBlock;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.io.model.GameModule;

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

	public List<CodeBlock> getCodeBlocksByID(String scriptValue) {

		List<StoryComponent> componentList = this.library.getEffectsCategory().getChildren();
		List<CodeBlock> codeBlockList = new ArrayList<CodeBlock>();
		for(StoryComponent component : componentList) {
			if(component instanceof ScriptIt) {
				if(component.getDisplayText().equals(scriptValue)) {
					codeBlockList.addAll(((ScriptIt) component).getCodeBlocks());
					return codeBlockList;
				}
			}
		}
		
		return null;
	}
}
