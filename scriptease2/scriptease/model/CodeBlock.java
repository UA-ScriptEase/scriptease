package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * A CodeBlock represents a block of code which is generated in script,
 * effectively mapping one-to-one with a function (or similar) in the generated
 * code. <br>
 * <br>
 * <code>CodeBlock</code>s know the following in addition to all of the subclass
 * attributes:
 * 
 * <ul>
 * <li><b>Parameters</b> the parameters of the function (or similar)</li>
 * </ul>
 * 
 * @author mfchurch
 * @author remiller
 */
public abstract class CodeBlock extends StoryComponent implements
		TypedComponent {
	/*
	 * Only instance-specific information may be stored in this parent class.
	 * Anything that is translator-only must be stored in CodeBlockSource,
	 * anything story-only must be stored in CodeBlockReference
	 * 
	 * - remiller
	 */
	private List<KnowIt> parameters;

	protected void init() {
		super.init();
		this.parameters = new ArrayList<KnowIt>();
	}

	@Override
	public CodeBlock clone() {
		final CodeBlock clone = (CodeBlock) super.clone();

		clone.init();

		return clone;
	}

	/**
	 * Sets the subject reference to the given subject name.
	 * 
	 * @param subject
	 *            The name of the KnowIt to use as the subject for this code
	 *            block.
	 */
	public abstract void setSubject(String subject);

	/**
	 * Sets the event slot reference to the given string.
	 * 
	 * @param slot
	 *            The name of the event slot to be used by
	 *            {@link scriptease.translator.io.model.GameModule GameModule}
	 *            in installing the code block's script to the correct location.
	 */
	public abstract void setSlot(String slot);

	/**
	 * Sets the return types for this code block to the contents of the given
	 * list.
	 * 
	 * @param types
	 *            the new return types
	 */
	public abstract void setTypes(Collection<String> types);

	/**
	 * Sets the parameters for this code block to the contents of the given
	 * list.
	 * 
	 * @param parameters
	 *            The new parameters
	 */
	public void setParameters(Collection<KnowIt> parameters) {
		this.parameters = new ArrayList<KnowIt>(parameters);
	}

	/**
	 * Sets the includes to the given include list.
	 * 
	 * @param includes
	 *            the new includes.
	 */
	public abstract void setIncludes(Collection<String> includes);

	/**
	 * Sets the code to the given code.
	 * 
	 * @param code
	 *            The new code.
	 */
	public abstract void setCode(Collection<FormatFragment> code);

	/**
	 * CodeBlocks can only be owned by ScriptIts
	 */
	@Override
	public ScriptIt getOwner() {
		return (ScriptIt) super.getOwner();
	}

	/**
	 * Sets the owner of this code block to the given owner.
	 * 
	 * @param owner
	 *            The codeblock's new owner.
	 */
	public void setOwner(ScriptIt newOwner) {
		super.setOwner(newOwner);
		for (KnowIt parameter : this.getParameters()) {
			parameter.setOwner(newOwner);
		}
	}

	/**
	 * Gets a list of the code block's parameters. This is a list because order
	 * is potentially important.
	 * 
	 * @return The parameters of the code block.
	 */
	public List<KnowIt> getParameters() {
		return new ArrayList<KnowIt>(this.parameters);
	}

	/**
	 * Determines whether this code block has a subject or not.
	 * 
	 * @return <code>true<code> if this code block has a subject set for it.
	 */
	public abstract boolean hasSubject();

	/**
	 * Determines whether this code block has an event slot set for it or not.
	 * 
	 * @return <code>true</code> if this code block has an event slot set for
	 *         it.
	 */
	public abstract boolean hasSlot();

	/**
	 * Gets the code block's subject as a KnowIt.
	 * 
	 * @return The concrete subject KnowIt.
	 */
	public KnowIt getSubject() {
		final ScriptIt cause = this.getCause();
		if (!hasSubject()) {
			final CodeBlock parentBlock = cause.getMainCodeBlock();
			return parentBlock.getSubject();
		} else {
			return cause.getParameter(this.getSubjectName());
		}
	}

	/**
	 * Gets the string representing the name of the subject
	 * 
	 * @return The by-name reference of the subject KnowIt for this code block.
	 */
	public abstract String getSubjectName();

	/**
	 * Gets the Cause ScriptIt that this code block is owned by, even if it is
	 * not its direct parent.
	 * 
	 * @return The encapsulating Cause.
	 */
	public ScriptIt getCause() {
		if (!this.getSubjectName().isEmpty() && !this.getSlot().isEmpty())
			return (ScriptIt) this.getOwner();
		else {
			StoryComponent parent = this.getOwner().getOwner();
			while (parent != null) {
				if (parent instanceof ScriptIt && ((ScriptIt) parent).isCause())
					break;
				parent = parent.getOwner();
			}
			if (parent == null)
				throw new IllegalStateException(
						"CodeBlock does not have a cause.");
			return (ScriptIt) parent;
		}
	}

	/**
	 * Gets the slot that this code block is set to exist on.
	 * 
	 * @return The code block's relevant event slot reference.
	 */
	public abstract String getSlot();

	/**
	 * Returns a collection of this CodeBlock's implicit KnowIts. Implicit
	 * KnowIts are things that are known due to an event. A creature's onDamaged
	 * event, for example, would have a have a damager, a damagee, and a damage
	 * amount that could be known simply because such an event occurred.<br>
	 * <br>
	 * Because the implicits are lazy-loaded, they need to be given the owner.
	 * 
	 * @param owner
	 *            The ScriptIt that will own the implicits.
	 * 
	 * @return The collection of implicit KnowIts for this code block. It may be
	 *         empty if there are no implicits, or if there is no loaded
	 *         translator to check from. Must not return <code>null</code>.
	 */
	public abstract Collection<KnowIt> getImplicits(ScriptIt owner);

	/**
	 * The list of include names that the script will need to run.
	 * 
	 * @return A list of include file names.
	 */
	public abstract Collection<String> getIncludes();

	/**
	 * Gets a copy of the code for this code block.
	 * 
	 * @return the code block's code.
	 */
	public abstract Collection<FormatFragment> getCode();

	/**
	 * Gets the base ID for this CodeBlock. The Base ID is the ID of the source
	 * code block, which is either directly for this code block, or if it a
	 * reference then is the ID of its source.
	 * 
	 * @return The base id of this code block.
	 */
	public abstract int getId();
}
