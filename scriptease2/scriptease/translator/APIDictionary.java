package scriptease.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.controller.observer.LibraryEvent;
import scriptease.controller.observer.LibraryObserver;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

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

		this.library.addLibraryChangeListener(this);
	}

	@Override
	public void modelChanged(LibraryModel model, LibraryEvent event) {
		final StoryComponent source = event.getEvent().getSource();
		CodeBlockSource block;

		if (event.getEventType() == LibraryEvent.STORYCOMPONENT_ADDED) {
			if (source instanceof CodeBlockSource) {
				block = (CodeBlockSource) source;
				this.nextID = Math.max(block.getId() + 1, this.nextID);
			}
		}
	};

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

	public List<CodeBlock> getCodeBlocksByValRef(String scriptValue) {
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
	 * Retrieves the next code block unique ID for this translator.
	 * 
	 * @return The next available unique id for a code block.
	 */
	public int getNextCodeBlockID() {
		return this.nextID++;
	}

	/**
	 * Finds the CodeBlockSource that matches the given data fields.
	 * 
	 * @param subject
	 *            The subject of the desired code block.
	 * @param slot
	 *            The slot of the desired code block.
	 * @param returnTypes
	 *            The return types of the desired code block.
	 * @param parameters
	 *            The parameters of the desired code block.
	 * @param includes
	 *            The includes of the desired code block.
	 * @param code
	 *            The code of the desired code block. This is the only optional
	 *            segment. If missing, it will be ignored.
	 * 
	 * @return The matching code block.
	 */
	public CodeBlockSource getCodeBlockByData(String subject, String slot,
			Collection<String> returnTypes, Collection<KnowIt> parameters,
			Collection<String> includes, Collection<FormatFragment> code) {
		final CodeBlockFinder finder = new CodeBlockFinder();

		return finder.findByData(subject, slot, returnTypes, parameters,
				includes, code);
	}

	/**
	 * Finds a CodeBlockSource by its ID number.
	 * 
	 * @param targetId
	 *            The ID number of the CodeBlockSource to locate.
	 * 
	 * @return The CodeBlockSource that has the given id.
	 */
	public CodeBlockSource getCodeBlockByID(int targetId) {
		final CodeBlockFinder finder = new CodeBlockFinder();

		return finder.findByID(targetId, this.library);
	}

	private class CodeBlockFinder extends AbstractNoOpStoryVisitor {
		private CodeBlockSource found = null;
		private int targetId;

		/**
		 * Finds a CodeBlockSource by ID.
		 * 
		 * @param targetId
		 *            The ID to search by.
		 * @param library
		 *            The library to search in.
		 * 
		 * @return The source with the given id.
		 */
		public CodeBlockSource findByID(int targetId, LibraryModel library) {
			this.targetId = targetId;

			// let's start snooping about. Quick, someone play Pink Panther or
			// Mission Impossible! - remiller
			library.getRoot().process(this);

			return this.found;
		}

		/**
		 * Finds a CodeBlockSource by data matching.
		 * 
		 * @param targetId
		 *            The ID to search by.
		 * @param library
		 *            The library to search in.
		 * 
		 * @return The source with the given id.
		 */
		public CodeBlockSource findByData(String subject, String slot,
				Collection<String> returnTypes, Collection<KnowIt> parameters,
				Collection<String> includes, Collection<FormatFragment> code) {
			return this.found;
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			super.processScriptIt(scriptIt);

			for (CodeBlock block : scriptIt.getCodeBlocks()) {
				if (block.getId() == targetId
						&& block instanceof CodeBlockSource) {
					this.found = (CodeBlockSource) block;
					return;
				}
			}
		}
		
		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			super.defaultProcessComplex(complex);
			
			for(StoryComponent child : complex.getChildren()){
				child.process(this);
			}
		}
	}
}