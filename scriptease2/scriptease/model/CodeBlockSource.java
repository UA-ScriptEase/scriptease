package scriptease.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
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
	 *            {@link scriptease.controller.apimanagers.GameTypeManager
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
			Collection<String> includes, Collection<AbstractFragment> code, int id) {
		this.init(id);
		this.setSubject(subject);
		this.setSlot(slot);
		this.setTypes(returnTypes);
		this.setParameters(parameters);
		this.setIncludes(includes);
		this.setCode(code);
	}

	@Override
	public void setTypes(Collection<String> types) {
		this.returnTypes = new ArrayList<String>(types);
	}

	protected void init(int id) {
		super.init();
		this.subjectName = "";
		this.slot = "";
		this.returnTypes = new ArrayList<String>();
		this.id = id;

		this.references = new HashSet<WeakReference<CodeBlockReference>>();
	}

	@Override
	public CodeBlock clone() {
		// Sources can't be cloned, since they're supposed to be unique.
		return new CodeBlockReference(this);

		// final Translator activeTranslator;
		// CodeBlockSource clone = null;
		//
		// clone = (CodeBlockSource) super.clone();
		//
		// activeTranslator = TranslatorManager.getInstance()
		// .getActiveTranslator();
		//
		// clone.init(activeTranslator.getApiDictionary().getNextCodeBlockID());
		//
		// // subject
		// if (!this.subjectName.isEmpty())
		// clone.setSubject(new String(this.subjectName));
		// // slot
		// if (!this.slot.isEmpty())
		// clone.setSlot(new String(this.slot));
		// // types
		// Collection<String> clonedTypes = new ArrayList<String>(
		// this.returnTypes.size());
		// for (String type : this.returnTypes) {
		// clonedTypes.add(new String(type));
		// }
		// clone.setTypes(clonedTypes);
		// // parameters
		// Collection<KnowIt> clonedParameters = new ArrayList<KnowIt>(this
		// .getParameters().size());
		// for (KnowIt parameter : this.getParameters()) {
		// clonedParameters.add(parameter.clone());
		// }
		// clone.setParameters(clonedParameters);
		// // includes
		// Collection<String> clonedIncludes = new ArrayList<String>(this
		// .getIncludes().size());
		// for (String include : this.getIncludes()) {
		// clonedIncludes.add(new String(include));
		// }
		// clone.setIncludes(clonedIncludes);
		// // code
		// Collection<FormatFragment> clonedCode = new
		// ArrayList<FormatFragment>(
		// this.getCode().size());
		// for (FormatFragment fragment : this.getCode()) {
		// clonedCode.add(fragment);
		// }
		// clone.setCode(clonedCode);
		//
		// return clone;
	}

	@Override
	public void setSubject(String subject) {
		if (subject == null)
			subject = "";

		this.subjectName = subject;
	}

	@Override
	public void setSlot(String slot) {
		if (slot == null)
			slot = "";

		this.slot = slot;
		this.updateReferences();
		this.resetImplicits();
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
		final ScriptIt cause;
		if (hasSlot()) {
			return this.slot;
		} else {
			cause = this.getCause();
			final CodeBlock parentBlock = cause.getMainCodeBlock();
			return parentBlock.getSlot();
		}
	}

	@Override
	public void setParameters(Collection<KnowIt> parameters) {
		super.setParameters(parameters);

		this.updateReferences();
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
		// for (KnowIt implicit : this.getImplicits(this.getOwner())) {
		// hashCode += implicit.getDisplayText().hashCode();
		// }

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
			// unique and this si an excellent way to enforce that. - remiller
			throw new IllegalStateException(
					"Cannot change a CodeBlockSource's ID.");
		}
	}

	@Override
	public void setCode(Collection<AbstractFragment> code) {
		this.code = new ArrayList<AbstractFragment>(code);
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_CODEBLOCK_CODE));
	}

	@Override
	public Collection<AbstractFragment> getCode() {
		return new ArrayList<AbstractFragment>(this.code);
	}

	/**
	 * Registers a reference with the
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
}
