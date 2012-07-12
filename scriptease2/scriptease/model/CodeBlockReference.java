package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * Reference to a code block in the translator. Most of the methods implemented
 * from {@link CodeBlock} are forwarded to the target {@link CodeBlockSource},
 * and therefore changes pushed to an instance of CodeBlockReference will appear
 * to effect all other <code>CodeBlockReference</code> instances that reference
 * the same target. Similarly, in the reverse direction, all same-targeted
 * <code>CodeBlockReference</code> instances will return the same data.<br>
 * <br>
 * A <code>CodeBlockReference</code> stores its own list of parameters, because
 * those are context/instance-specific (sharing them in the target makes no
 * sense).<br>
 * <br>
 * <code>CodeBlockReference</code>s are intended for use in the Story only. If
 * they are to be used within the Translator, then this class must be updated to
 * reflect that new capability.
 * 
 * @author remiller
 */
public class CodeBlockReference extends CodeBlock {
	private CodeBlockSource target;

	/**
	 * Creates a CodeBlockReference that points nowhere.
	 */
	public CodeBlockReference() {
		this(null);
	}

	/**
	 * Creates a new CodeBlockReference that points to the given target.
	 * 
	 * @param target
	 *            The target to point to.
	 */
	public CodeBlockReference(CodeBlockSource target) {
		this.setTarget(target);
		// don't set parameters here because setTarget does that for us.
	}

	@Override
	public CodeBlockReference clone() {
		CodeBlockReference clone = null;
		final Collection<KnowIt> clonedParameters;

		clone = (CodeBlockReference) super.clone();

		clone.setTarget(this.target);

		clone.init();

		// parameters
		clonedParameters = new ArrayList<KnowIt>(this.getParameters().size());
		for (KnowIt parameter : this.getParameters()) {
			clonedParameters.add(parameter.clone());
		}
		clone.setParameters(clonedParameters);

		// Poof! Tadaa!
		return clone;
	}

	private CodeBlockSource getTarget() {
		return this.target;
	}

	/**
	 * Sets the target of this Reference to the given target. It also clones the
	 * parameters from the target using {@link #setParameters(Collection)};
	 * 
	 * @param newTarget
	 *            The new target to point to. May be null.
	 */
	public void setTarget(CodeBlockSource newTarget) {
		final CodeBlockSource oldTarget = this.getTarget();

		if (oldTarget == newTarget)
			return;

		if (oldTarget != null)
			oldTarget.removeReference(this);

		this.target = newTarget;

		if (this.target != null) {
			this.target.addReference(this);

			this.setParameters(this.target.getParameters());
		}
	}

	/**
	 * Override of {@link CodeBlock#setParameters(Collection)} that re-clones
	 * parameters from the target and then tries to match bindings to the given
	 * list of parameters. If the current target is <code>null</code>, then this
	 * will have no effect.
	 * 
	 * @param newBindings
	 *            The list of parameters whose bindings should be used for
	 *            matching parameters in this code block.
	 */
	@Override
	public void setParameters(Collection<KnowIt> newBindings) {
		final List<KnowIt> targetParameters;
		final List<KnowIt> newParameters;
		final CodeBlockSource target = this.getTarget();

		/*
		 * Update our parameters from our target before doing anything. They
		 * need to be cloned so that we don't start mucking with the default
		 * values. Plus, they're shared, so we don't want others to change their
		 * value.
		 */
		targetParameters = target != null ? target.getParameters()
				: new ArrayList<KnowIt>();
		newParameters = new ArrayList<KnowIt>(targetParameters.size());

		for (KnowIt param : targetParameters) {
			newParameters.add(param.clone());
		}

		super.setParameters(newParameters);

		this.setBindings(newBindings);
	}

	@Override
	public void setTypes(Collection<String> types) {
		this.target.setTypes(types);
	}

	@Override
	public void setSubject(String subject) {
		this.target.setSubject(subject);
	}

	@Override
	public void setSlot(String slot) {
		this.target.setSlot(slot);
	}

	@Override
	public void setIncludes(Collection<String> includes) {
		this.target.setIncludes(includes);
	}

	@Override
	public boolean hasSubject() {
		return this.target.hasSubject();
	}

	@Override
	public boolean hasSlot() {
		return this.target.hasSlot();
	}

	@Override
	public String getSlot() {
		return this.target.getSlot();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CodeBlockReference) {
			int thisHashCode = this.hashCode();
			int otherHashCode = other.hashCode();

			return thisHashCode == otherHashCode;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = this.target.hashCode();

		return hashCode;
	}

	@Override
	public Collection<KnowIt> getImplicits(ScriptIt owner) {
		return this.target.getImplicits(owner);
	}

	public Collection<String> getIncludes() {
		return this.target.getIncludes();
	}

	@Override
	public String toString() {
		return "CodeBlockRef [" + this.target.toString() + ","
				+ this.getParameters() + "]";
	}

	@Override
	public Collection<String> getTypes() {
		return this.target.getTypes();
	}

	@Override
	public String getSubjectName() {
		return this.target.getSubjectName();
	}

	@Override
	public void setCode(Collection<FormatFragment> code) {
		this.target.setCode(code);
	}

	@Override
	public Collection<FormatFragment> getCode() {
		return this.target.getCode();
	}

	protected void targetUpdated(CodeBlockSource source) {
		if (this.getTarget() != source) {
			throw new IllegalStateException("Received update event to "
					+ this.toString()
					+ " from a source that is not my target. That's awkward.");
		}

		this.setParameters(source.getParameters());
	}

	@Override
	public int getId() {
		final CodeBlockSource target = this.getTarget();

		if (target == null)
			throw new IllegalStateException("No target to get ID from.");
		else
			return target.getId();
	}

	public void setBindings(Collection<KnowIt> newBindings) {
		/*
		 * Let's go about trying to rebind whatever is possible to match as
		 * closely as we can the given parameter list.
		 */
		for (KnowIt param : this.getParameters()) {
			// find the matching parameter from the given ones
			KnowIt match = null;
			for (KnowIt bindingParam : newBindings) {
				if (param.getDisplayText()
						.equals(bindingParam.getDisplayText())) {
					match = bindingParam;
					break;
				}
			}
			if (match == null)
				continue;

			// use its binding now that we have it
			param.setBinding(match.getBinding());
		}
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processCodeBlockReference(this);
	}
}