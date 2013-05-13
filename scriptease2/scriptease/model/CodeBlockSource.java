package scriptease.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Concrete representation of a specific code block in the API Dictionary.
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
	private static final int DEFAULT_ID = -1;
	private String subjectName;
	private String slot;
	private Collection<String> returnTypes;
	private Collection<String> includes;
	private Collection<AbstractFragment> code;
	private Set<WeakReference<CodeBlockReference>> references;
	private int id;

	/**
	 * Creates a new CodeBlockSource with default properties.
	 */
	public CodeBlockSource() {
		this("", "", new ArrayList<String>(), new ArrayList<KnowIt>(),
				new ArrayList<String>(), new ArrayList<AbstractFragment>(),
				CodeBlockSource.DEFAULT_ID);
	}

	/**
	 * Creates a new CodeBlockSource with the given property.
	 * 
	 * @param id
	 *            The unique ID of this codeblock. If not being read from a
	 *            file, then clients should use
	 *            {@link APIDictionary#getNextCodeBlockID()}
	 */
	public CodeBlockSource(int id) {
		this("", "", new ArrayList<String>(), new ArrayList<KnowIt>(),
				new ArrayList<String>(), new ArrayList<AbstractFragment>(), id);
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
	 *            The unique ID of this codeblock. If not being read from a
	 *            file, then clients should use
	 *            {@link APIDictionary#getNextCodeBlockID()}
	 */
	public CodeBlockSource(String subject, String slot,
			Collection<KnowIt> parameters, int id) {
		this(subject, slot, new ArrayList<String>(), parameters,
				new ArrayList<String>(), new ArrayList<AbstractFragment>(), id);
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
	 *            Strings must exist in a
	 *            {@link scriptease.translator.apimanagers.GameTypeManager
	 *            GameTypeManager}
	 * @param parameters
	 *            The parameter list of this code block.
	 * @param includes
	 *            The list of includes required for this code block to function.
	 * @param code
	 *            The core code to generate for this code block.
	 * @param id
	 *            The unique ID of this codeblock. If not being read from a
	 *            file, then clients should use
	 *            {@link APIDictionary#getNextCodeBlockID()}
	 */
	public CodeBlockSource(String subject, String slot,
			Collection<String> returnTypes, Collection<KnowIt> parameters,
			Collection<String> includes, Collection<AbstractFragment> code,
			int id) {
		this.init(id);
		this.setSubject(subject);
		this.setSlot(slot);
		this.setTypes(returnTypes);
		this.setParameters(parameters);
		this.setIncludes(includes);
		this.setCode(code);
	}

	@Override
	/**
	 * Accomodating AspectJ
	 */
	public void setTypes(Collection<String> types) {
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
		if (this.returnTypes.add(type)) {
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODE_BLOCK_TYPES));
		}
	}

	@Override
	public void removeType(String type) {
		if (this.returnTypes.remove(type)) {
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODE_BLOCK_TYPES));
		}
	}

	protected void init(int id) {
		super.init();
		this.subjectName = "";
		this.slot = "";
		this.returnTypes = new ArrayList<String>();
		this.id = id;
		this.code = new ArrayList<AbstractFragment>();
		this.references = new HashSet<WeakReference<CodeBlockReference>>();
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
	public CodeBlockSource duplicate(int uniqueId) {
		final CodeBlockSource duplicate = new CodeBlockSource(uniqueId);
		duplicate.setSubject(this.subjectName);
		duplicate.setSlot(this.slot);
		duplicate.setTypes(this.returnTypes);

		final Collection<KnowIt> clonedParameters = new ArrayList<KnowIt>();
		final Collection<KnowIt> parameters = this.getParameters();
		for (KnowIt parameter : parameters) {
			clonedParameters.add(parameter.clone());
		}
		duplicate.setParameters(clonedParameters);

		final Collection<AbstractFragment> clonedCode = new ArrayList<AbstractFragment>(
				this.code.size());
		for (AbstractFragment fragment : this.code) {
			clonedCode.add(fragment.clone());
		}
		duplicate.setCode(clonedCode);

		return duplicate;
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
	public int getId() {
		return this.id;
	}

	/**
	 * IDs are unique, so this must only be called on loading from the API
	 * dictionary. They may not be reset or changed or modified. Ever.
	 * 
	 * @param id
	 *            the id for this CodeBlock.
	 */
	public void setId(int id) {
		if (this.getId() == CodeBlockSource.DEFAULT_ID) {
			this.id = id;
		} else {
			// if you get here, Very Bad Things have happened and you need to
			// fix them. These IDs must never change once set; they must be
			// unique and this is an excellent way to enforce that. - remiller
			throw new IllegalStateException(
					"Cannot change a CodeBlockSource's ID.");
		}
	}

	@Override
	public void setCode(Collection<AbstractFragment> newCode) {
		this.code = new ArrayList<AbstractFragment>(newCode);
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_CODEBLOCK_CODE));
	}

	@Override
	public Collection<AbstractFragment> getCode() {
		return new ArrayList<AbstractFragment>(this.code);
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
