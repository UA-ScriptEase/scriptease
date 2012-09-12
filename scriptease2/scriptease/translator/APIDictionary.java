package scriptease.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.BindingVisitor;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryObserver;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.apimanagers.EventSlotManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.Slot;

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

		if (event.getEventType() == LibraryEvent.STORYCOMPONENT_ADDED) {
			if (source instanceof ScriptIt) {
				final List<CodeBlock> codeBlocks;

				codeBlocks = new ArrayList<CodeBlock>(
						((ScriptIt) source).getCodeBlocks());
				
				for(CodeBlock codeBlock : codeBlocks) {
					this.nextID = Math.max(codeBlock.getId() +1, this.nextID);
				}
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

	/**
	 * Increases the NextCodeBlockID to the passed int. If the current id is
	 * larger than the passed int, nothing is changed.
	 * 
	 * @param ID
	 */
	public void increaseNextCodeBlockIDTo(int ID) {
		this.nextID = Math.max(this.nextID, ID);
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
		CodeBlockSource found;

		found = finder.findByID(targetId, this);

		return found;
	}

	private class CodeBlockFinder extends StoryAdapter {
		private CodeBlockSource found = null;
		private int targetId;

		/**
		 * Finds a CodeBlockSource by ID.
		 * 
		 * @param targetId
		 *            The ID to search by.
		 * @param dictionary
		 *            The dictionary to search in.
		 * 
		 * @return The source with the given id.
		 */
		public CodeBlockSource findByID(int targetId, APIDictionary dictionary) {
			this.targetId = targetId;

			// let's start snooping about. Quick, someone play Pink Panther or
			// Mission Impossible! - remiller
			dictionary.getLibrary().getRoot().process(this);

			// not in the library. Try the slots next?
			if (this.found == null) {
				final Collection<KnowIt> knowIts = new ArrayList<KnowIt>();

				for (Slot slot : dictionary.getEventSlotManager()
						.getEventSlots()) {
					// gotta collect 'em together first.
					knowIts.addAll(slot.getImplicits());
					knowIts.addAll(slot.getParameters());

					for (KnowIt knowIt : knowIts) {
						knowIt.process(this);

						if (this.found != null) {
							return this.found;
						}
					}
					// keep looking
					knowIts.clear();
				}
			}

			return this.found;
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			if (this.found != null)
				return;

			super.processScriptIt(scriptIt);

			for (CodeBlock block : scriptIt.getCodeBlocks()) {
				if (block.getId() == this.targetId
						&& block instanceof CodeBlockSource) {
					this.found = (CodeBlockSource) block;
					return;
				}
			}
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			if (this.found != null)
				return;

			super.processKnowIt(knowIt);

			final CodeBlockFinder searcher = this;
			final BindingVisitor bindingSearcher;

			bindingSearcher = new BindingAdapter() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					function.getValue().process(searcher);
				}

				@Override
				public void processReference(KnowItBindingReference reference) {
					reference.getValue().process(searcher);
				}

				@Override
				public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
					storyPoint.getValue().process(searcher);
				}
			};

			knowIt.getBinding().process(bindingSearcher);
		}

		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			super.defaultProcessComplex(complex);

			for (StoryComponent child : complex.getChildren()) {
				child.process(this);

				// Found it. All craft, pull up!
				if (this.found != null)
					return;
			}
		}
	}
}