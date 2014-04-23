package scriptease.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;

/**
 * Concrete representation of a specific code block in the LibraryModel.
 * {@link CodeBlockReference}s will point to a shared instance of
 * <code>CodeBlockSource</code>, eliminating redundant data and keeping
 * game-specific data translator-side.<br>
 * <br>
 * These <b>must only ever exist translator-side</b>. Putting a source code
 * block into a story is defeating the purpose of storing game-specific things
 * like code and includes in the translator in the first place. <br>
 * <br>
 * <code>CodeBlockSources</code>s know:
 * 
 * <ul>
 * <li><b>Subject</b> and <b>Slot</b> to hint to GameModule where it should end
 * up after generation</li>
 * <li><b/>Return types</b> the return type of the function (or similar)</li>
 * <li><b/>Implicits</b> the implicitly known KnowIts from the event (for
 * causes)</li>
 * <li><b>Parameters</b> (from the parent class
 * {@link scriptease.model.CodeBlock CodeBlock})</li>
 * <li><b>Code</b> the body of the function</li>
 * <li><b>Includes</b> include file names necessary for the code above to run</li>
 * <li><b>ID</b> unique ID given to each CodeBlockSource on creation.</li>
 * </ul>
 * 
 * <code>CodeBlockSources</code>s are not really clonable in the strictest sense
 * of the term, and instead will return a {@link CodeBlockReference reference}
 * when {@link #clone()} is called.
 * 
 * @author mfchurch
 * @author remiller
 */
public class CodeBlockSource extends CodeBlock {
	private String subjectName;
	private String slot;
	private Collection<String> returnTypes;
	private Collection<String> includes;
	private List<AbstractFragment> code;
	private Set<WeakReference<CodeBlockReference>> references;

	/**
	 * Creates a new CodeBlockSource with default properties.
	 */
	public CodeBlockSource(LibraryModel library) {
		this(library, library.getNextID());
	}

	/**
	 * Creates a new CodeBlockSource with the given property.
	 * 
	 * @param id
	 *            The unique ID of this codeblock.
	 */
	public CodeBlockSource(LibraryModel library, int id) {
		this("", "", new ArrayList<KnowIt>(), library, id);
	}

	/**
	 * Creates a new CodeBlockSource with the given properties.
	 * 
	 * @param subject
	 *            The name of the subject KnowIt that will be the event slot
	 *            owner.
	 * @param slot
	 *            The event slot name to be used by
	 *            {@link scriptease.translator.io.model.GameModule GameModule}
	 *            to determine the script hook to attach to.
	 * @param parameters
	 *            The parameter list of this code block.
	 * @param id
	 *            The unique ID of this codeblock.
	 */
	public CodeBlockSource(String subject, String slot,
			Collection<KnowIt> parameters, LibraryModel library, int id) {
		this(subject, slot, new ArrayList<String>(), parameters,
				new ArrayList<String>(), new ArrayList<AbstractFragment>(),
				library, id);
	}

	/**
	 * Creates a new CodeBlockSource with the given properties.
	 * 
	 * @param subject
	 *            The name of the subject KnowIt that will be the event slot
	 *            owner.
	 * @param slot
	 *            The event slot name to be used by
	 *            {@link scriptease.translator.io.model.GameModule GameModule}
	 *            to determine the script hook to attach to.
	 * @param returnTypes
	 *            The possible return types of this CodeBlock. All of these
	 *            Strings must exist in the model's types.
	 * @param parameters
	 *            The parameter list of this code block.
	 * @param includes
	 *            The list of includes required for this code block to function.
	 * @param code
	 *            The core code to generate for this code block.
	 * @param id
	 *            The unique ID of this codeblock.
	 */
	public CodeBlockSource(String subject, String slot,
			Collection<String> returnTypes, Collection<KnowIt> parameters,
			Collection<String> includes, List<AbstractFragment> code,
			LibraryModel library, int id) {
		super(library, id);
		super.init();

		this.subjectName = "";
		this.slot = "";
		this.returnTypes = new ArrayList<String>();

		this.code = new ArrayList<AbstractFragment>();
		this.references = new HashSet<WeakReference<CodeBlockReference>>();

		this.setSubject(subject);
		this.setSlot(slot);
		this.setTypesByName(returnTypes);
		this.setParameters(parameters);
		this.setIncludes(includes);
		this.setCode(code);
	}

	@Override
	/**
	 * Accomodating AspectJ
	 */
	public void setTypesByName(Collection<String> types) {
		Collection<String> oldTypes = new ArrayList<String>(this.returnTypes);
		for (String type : oldTypes) {
			removeType(type);
		}

		for (String type : types) {
			addType(type);
		}
	}

	@Override
	public void addType(String type) {
		this.returnTypes.add(type);
	}

	@Override
	public void removeType(String type) {
		this.returnTypes.remove(type);
	}

	@Override
	public CodeBlock clone() {
		// Sources can't be cloned, since they're supposed to be unique.
		return new CodeBlockReference(this);
	}

	/**
	 * Creates a duplicate of the CodeBlock with the given ID assigned. This is
	 * similar to cloning however should only be used with regards to making
	 * CodeBlocks modifications that we do not wish to be reflected in the
	 * original (LibraryEditor).
	 * 
	 * @author mfchurch
	 * @return
	 */
	public CodeBlockSource duplicate(LibraryModel library) {
		final CodeBlockSource duplicate = new CodeBlockSource(library,
				library.getNextID());

		duplicate.setSubject(this.subjectName);
		duplicate.setSlot(this.slot);
		duplicate.setTypesByName(this.returnTypes);

		final Collection<KnowIt> clonedParameters = new ArrayList<KnowIt>();
		final Collection<KnowIt> parameters = this.getParameters();
		for (KnowIt parameter : parameters) {
			clonedParameters.add(parameter.clone());
		}
		duplicate.setParameters(clonedParameters);

		final List<AbstractFragment> clonedCode = new ArrayList<AbstractFragment>(
				this.code.size());
		for (AbstractFragment fragment : this.code) {
			clonedCode.add(fragment.clone());
		}
		duplicate.setCode(clonedCode);

		return duplicate;
	}

	/**
	 * Move the currently selected format fragment an amount determined by the
	 * delta.
	 * 
	 * @param topLevelFormatFragments
	 * @param subFragment
	 * @param parentFragment
	 * @return
	 */
	public boolean moveCodeFragment(final AbstractFragment subFragment,
			int delta) {
		final boolean found = AbstractContainerFragment.moveFragmentInList(
				subFragment, this.code, delta);

		if (found)
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODEBLOCK_CODE));

		return found;
	}

	/**
	 * Creates a duplicate of the CodeBlock with the given ID assigned. This is
	 * similar to cloning however should only be used with regards to making
	 * CodeBlocks modifications that we do not wish to be reflected in the
	 * original (LibraryEditor).
	 * 
	 * @author mfchurch
	 * @return
	 */
	public CodeBlockSource duplicate() {
		return this.duplicate(this.getLibrary());
	}

	@Override
	public void setSubject(String subject) {
		if (subject == null)
			subject = "";
		this.subjectName = subject;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET));
	}

	@Override
	public void setSlot(String slot) {
		if (slot == null)
			slot = "";

		this.slot = slot;
		this.updateReferences();
		this.resetImplicits();

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET));
	}

	@Override
	public void setIncludes(Collection<String> includes) {
		this.includes = new ArrayList<String>(includes);
	}

	@Override
	public boolean hasSubject() {
		return !this.subjectName.isEmpty();
	}

	@Override
	public boolean hasSlot() {
		return !this.slot.isEmpty();
	}

	@Override
	public String getSlot() {
		String slot = null;
		if (this.hasSlot()) {
			slot = this.slot;
		} else {
			final ScriptIt cause = this.getCause();
			if (cause != null) {
				final CodeBlock parentBlock = cause.getMainCodeBlock();
				slot = parentBlock.getSlot();
			}
		}
		return slot;
	}

	@Override
	public boolean addParameter(KnowIt parameter) {
		final boolean success = super.addParameter(parameter);
		if (success) {
			this.updateReferences();
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
		}
		return success;
	}

	@Override
	public boolean removeParameter(KnowIt parameter) {
		final boolean success = super.removeParameter(parameter);
		if (success) {
			this.updateReferences();
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE));
		}
		return success;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CodeBlock) {
			int thisHashCode = this.hashCode();
			int otherHashCode = other.hashCode();
			return thisHashCode == otherHashCode;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.hasSubject())
			hashCode += this.getSubjectName().hashCode();
		if (this.hasSlot())
			hashCode += this.getSlot().hashCode();
		for (String returnType : this.returnTypes)
			hashCode += returnType.hashCode();
		for (KnowIt parameter : this.getParameters()) {
			hashCode += parameter.getDisplayText().hashCode();
		}
		for (String include : this.getIncludes()) {
			hashCode += include.hashCode();
		}

		return hashCode;
	}

	@Override
	public Collection<String> getIncludes() {
		return this.includes;
	}

	@Override
	public String toString() {
		return "CodeBlockSource [" + this.subjectName + ", " + this.slot + ", "
				+ this.getOwner() + "]";
	}

	@Override
	public Collection<String> getTypes() {
		return this.returnTypes;
	}

	/**
	 * Gets the string representing the name of the subject
	 * 
	 * @return
	 */
	@Override
	public String getSubjectName() {
		return this.subjectName;
	}

	@Override
	public void setCode(List<AbstractFragment> newCode) {
		this.code = newCode;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_CODEBLOCK_CODE));
	}

	@Override
	public List<AbstractFragment> getCode() {
		return this.code;
	}

	/**
	 * Registers a reference
	 * 
	 * @param added
	 */
	protected void addReference(CodeBlockReference added) {
		this.references.add(new WeakReference<CodeBlockReference>(added));
	}

	protected void removeReference(CodeBlockReference removed) {
		WeakReference<CodeBlockReference> found = null;

		for (WeakReference<CodeBlockReference> ref : this.references) {
			if (ref.get() == removed) {
				found = ref;
				break;
			}
		}

		if (found == null)
			System.err
					.println("Tried to remove a code block reference that was never known about.");
		else
			this.references.remove(found);
	}

	private void updateReferences() {
		for (WeakReference<CodeBlockReference> ref : this.references) {
			ref.get().targetUpdated(this);
		}
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processCodeBlockSource(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		System.err
				.println("Attempted to revalidate KnowItBindings on CodeBlockSource: "
						+ this);
	}
}
