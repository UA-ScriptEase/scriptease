package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

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
		TypedComponent, StoryComponentObserver {
	/*
	 * Only instance-specific information may be stored in this parent class.
	 * Anything that is translator-only must be stored in CodeBlockSource,
	 * anything story-only must be stored in CodeBlockReference
	 * 
	 * - remiller
	 */
	// parameters are instance-specific because we can't share their bindings.
	private List<KnowIt> parameters;

	// implicits are instance-specific due to their ownership pointers needing
	// to be set at the instance
	private Collection<KnowIt> implicits;

	protected void init() {
		super.init();
		this.parameters = new ArrayList<KnowIt>();
		// implicits are lazy-load. See getImplicits().
		this.implicits = null;
	}

	@Override
	public String getDisplayText() {
		final ScriptIt owner = this.getOwner();
		return owner != null ? owner.getDisplayText() : "";
	}

	/**
	 * Does nothing since display text is inherited purely from the parent.
	 */
	@Override
	public void setDisplayText(String text) {
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		final List<KnowIt> params = this.getParameters();
		for (KnowIt param : params) {
			KnowItBinding binding = param.getBinding();
			if (binding != null)
				hashCode += binding.hashCode();
		}
		final ScriptIt owner = this.getOwner();
		if (owner instanceof ControlIt) {
			for (KnowIt param : ((ControlIt) owner).getRequiredParameters()) {
				hashCode += param.getBinding().hashCode();
			}

			hashCode += owner.hashCode();
		}

		return hashCode;
	}

	@Override
	public CodeBlock clone() {
		final CodeBlock clone = (CodeBlock) super.clone();
		clone.init();
		for (KnowIt parameter : parameters) {
			clone.addParameter(parameter.clone());
		}
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

	public abstract void addType(String type);

	public abstract void removeType(String type);

	/**
	 * Sets the parameters for this code block to the contents of the given
	 * list.
	 * 
	 * @param parameters
	 *            The new parameters
	 */
	protected void setParameters(Collection<KnowIt> parameters) {
		this.parameters = new ArrayList<KnowIt>(parameters);
	}

	public boolean addParameter(KnowIt parameter) {
		return this.parameters.add(parameter);
	}

	public boolean removeParameter(KnowIt parameter) {
		return this.parameters.remove(parameter);
	}

	/**
	 * Sets the includes to the given include list.
	 * 
	 * @param includes
	 *            the new includes.
	 */
	public abstract void setIncludes(Collection<String> includes);

	public abstract void setCode(Collection<AbstractFragment> code);

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
			parameter.setOwner(this);
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
	 * Resets the implicits to null.
	 */
	protected void resetImplicits() {
		this.implicits = null;
	}

	/**
	 * Returns a collection of this CodeBlock's implicit KnowIts. Implicit
	 * KnowIts are things that are known due to an event. A creature's onDamaged
	 * event, for example, would have a have a damager, a damagee, and a damage
	 * amount that could be known simply because such an event occurred.<br>
	 * <br>
	 * Implicits are lazy-loaded because they actually originate from the Slot
	 * definitions in {@link EventSlotManager}. They are not saved per code
	 * block, since they're common amongst code blocks with the same slot. The
	 * implicits cloned from <code>EventSlotManager</code> have their owner set
	 * to this code block's Cause (as determined by {@link #getCause()}).
	 * 
	 * @return The collection of implicit KnowIts for this code block. It may be
	 *         empty if there are no implicits, or if there is no loaded
	 *         translator to check from. Must not return <code>null</code>.
	 */
	public Collection<KnowIt> getImplicits() {

		if (this.implicits == null) {
			// I guess it's time to load the implicits list. Go git'em boy!
			this.implicits = new CopyOnWriteArraySet<KnowIt>();

			// only fetch implicits for code blocks that actually have them,
			// which is determined by slot.
			if (this.hasSlot()) {
				final Collection<KnowIt> clonedImplicits;

				clonedImplicits = new ArrayList<KnowIt>();

				// clone these because they're CodeBlock instance-specific
				for (KnowIt implicit : this.getLibrary().getSlotImplicits(
						this.getSlot())) {
					final KnowIt clone = implicit.clone();
					clone.setOwner(this);
					clonedImplicits.add(clone);
				}

				this.implicits.addAll(clonedImplicits);
			}
		}

		return new ArrayList<KnowIt>(this.implicits);
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
		final CauseIt cause = this.getCause();
		if (!this.hasSubject()) {
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
	public CauseIt getCause() {
		/*
		 * This is a || because sometimes while editing we can have only one.
		 * It's not a valid cause-codeblock while in that state, but this is as
		 * close as we can get. - remiller
		 */
		StoryComponent owner = this.getOwner();
		if (owner instanceof CauseIt) {
			return (CauseIt) owner;
		} else {
			while (owner != null) {
				if (owner instanceof CauseIt)
					break;
				owner = owner.getOwner();
			}

			if (owner == null) {
				throw new IllegalStateException(
						"Failed to locate enclosing Cause for CodeBlock "
								+ this.toString());
			}

			return (CauseIt) owner;
		}
	}

	/**
	 * Gets the slot that this code block is set to exist on. If this code block
	 * does not have a slot itself, <code>getSlot()</code> will return the slot
	 * of the enclosing cause as determined by {@link #getCause()}.
	 * 
	 * @return The code block's relevant event slot reference.
	 */
	public abstract String getSlot();

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
	public abstract Collection<AbstractFragment> getCode();

	/**
	 * Gets the base ID for this CodeBlock. The Base ID is the ID of the source
	 * code block, which is either directly for this code block, or if it a
	 * reference then is the ID of its source.
	 * 
	 * @return The base id of this code block.
	 */
	public abstract int getId();

	@Override
	public void componentChanged(StoryComponentEvent event) {
		this.notifyObservers(event);
	}

}
