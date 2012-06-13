package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * A CodeBlock represents a block of code which is generated in script.
 * Essentially maps one-to-one with a function. It needs information such as
 * subject and slot to dictate where it should end up during the CodeGenerator
 * process, but ultimately that is up to the GameModule implementation. It
 * contains returnTypes which represent the return type of the function,
 * parameters which represent the parameters of the function, code which
 * represents the body of the function, and includes that might be needed to
 * make this function call.
 * 
 * @author mfchurch
 * 
 */
public class CodeBlock implements TypedComponent, Cloneable {
	private ScriptIt owner;
	private String subject;
	private String slot;
	private Collection<String> returnTypes;
	private List<KnowIt> parameters;
	private Collection<KnowIt> implicits;
	private Collection<String> includes;
	private Collection<FormatFragment> code;

	public CodeBlock(String subject, String slot, Collection<String> types,
			Collection<String> includes, Collection<KnowIt> parameters,
			Collection<FormatFragment> code) {
		this.init();
		this.setSubject(subject);
		this.setSlot(slot);
		this.setTypes(types);
		this.setParameters(parameters);
		this.setIncludes(includes);
		this.setCode(code);
	}

	public void setTypes(Collection<String> types) {
		this.returnTypes = new ArrayList<String>(types);
	}

	protected void init() {
		this.subject = "";
		this.slot = "";
		this.returnTypes = new ArrayList<String>();
		this.implicits = null;
		this.parameters = new ArrayList<KnowIt>();
	}

	@Override
	public CodeBlock clone() {
		//private ScriptIt owner;
		//private String subject;
		//private String slot;
		//private Collection<String> returnTypes;
		//private List<KnowIt> parameters;
		//private Collection<KnowIt> implicits;
		//private Collection<String> includes;
		//private Collection<FormatFragment> code;
			
		
		CodeBlock clone = null;
		try {
			clone = (CodeBlock) super.clone();
		} catch (CloneNotSupportedException e) {
			// I can't think of a better way to deal with this -- mfchurch
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}
		clone.init();
						
		// subject
		if (!this.subject.isEmpty())
			clone.setSubject(new String(this.subject));
		// slot
		if (!this.slot.isEmpty())
			clone.setSlot(new String(this.slot));
		// types
		Collection<String> clonedTypes = new ArrayList<String>(
				this.returnTypes.size());
		for (String type : this.returnTypes) {
			clonedTypes.add(new String(type));
		}
		clone.setTypes(clonedTypes);
		// parameters
		Collection<KnowIt> clonedParameters = new ArrayList<KnowIt>(
				this.parameters.size());
		for (KnowIt parameter : this.parameters) {
			clonedParameters.add(parameter.clone());
		}
		clone.setParameters(clonedParameters);
		// includes
		Collection<String> clonedIncludes = new ArrayList<String>(this
				.getIncludes().size());
		for (String include : this.getIncludes()) {
			clonedIncludes.add(new String(include));
		}
		clone.setIncludes(clonedIncludes);
		// code
		Collection<FormatFragment> clonedCode = new ArrayList<FormatFragment>(this.getCode().size());
		for (FormatFragment fragment : this.getCode()) {
			clonedCode.add(fragment);
		} 
		clone.setCode(clonedCode);

		// No need to worry about code and includes since that is handled by
		// codemanager, and the clone _should_ resolve to the same code based on
		// an identical hash

		return clone;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public void setParameters(Collection<KnowIt> parameters) {
		this.parameters = new ArrayList<KnowIt>(parameters);
	}

	// TODO: apply flyweight
	public void setIncludes(Collection<String> includes) {
		this.includes = new ArrayList<String>(includes);
	}

	/**
	 * Sets the code associated with the CodeBlock
	 * 
	 * @param code
	 */
	// TODO: apply flyweight SEE THE TICKET IN PIVOTAL TRACKER REGARDING FLYWEIGHT
	public void setCode(Collection<FormatFragment> code) {
		this.code = new ArrayList<FormatFragment>(code);
	}

	public void setOwner(ScriptIt owner) {
		this.owner = owner;
		for (KnowIt parameter : this.parameters) {
			parameter.setOwner(owner);
		}
	}

	public ScriptIt getOwner() {
		return this.owner;
	}

	public List<KnowIt> getParameters() {
		return this.parameters;
	}
	
	public boolean hasSubject() {
		return !this.subject.isEmpty();
	}

	public boolean hasSlot() {
		return !this.slot.isEmpty();
	}

	public KnowIt getSubject() {
		final ScriptIt cause = this.getCause();
		if (!hasSubject()) {
			final CodeBlock parentBlock = cause.getMainCodeBlock();
			return parentBlock.getSubject();
		} else {
			return cause.getParameter(this.subject);
		}
	}

	public ScriptIt getCause() {
		if (!this.subject.isEmpty() && !this.slot.isEmpty())
			return this.owner;
		else {
			StoryComponent parent = this.owner.getOwner();
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

	public String getSlot() {
		final ScriptIt cause = this.getCause();
		if (!hasSlot()) {
			final CodeBlock parentBlock = cause.getMainCodeBlock();
			return parentBlock.getSlot();
		} else {
			return this.slot;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CodeBlock)
			return this.hashCode() == other.hashCode();
		return false;
	}

	/**
	 * Note: CodeBlock's are not guaranteed to be unique just based on these
	 * criteria. Other important factors in determining if CodeBlocks are
	 * "equal" are their owner ScriptIt.
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.hasSubject())
			hashCode += this.getSubjectName().hashCode();
		if (this.hasSlot())
			hashCode += this.getSlot().hashCode();
		for (String returnType : this.returnTypes)
			hashCode += returnType.hashCode();
		for (KnowIt parameter : this.getParameters())
			hashCode += parameter.getDisplayText().hashCode();
		return hashCode;
	}

	public Collection<KnowIt> getImplicits() {
		if (implicits == null) {
			final Translator active = TranslatorManager.getInstance()
					.getActiveTranslator();
			if (active != null) {
				implicits = new CopyOnWriteArraySet<KnowIt>();
				final EventSlotManager eventSlotManager = active
						.getApiDictionary().getEventSlotManager();
				implicits.addAll(eventSlotManager.getImplicits(this.slot));
				for (KnowIt implicit : implicits) {
					implicit.setOwner(owner);
				}
			}
		}
		return implicits;
	}

	public Collection<String> getIncludes() {
		for(String include : this.includes){
	//		FileContext.addIncludeFile(include);
			System.out.println("Codeblock includes: " + include);
		}
		return this.includes;
	}

	/**
	 * Gets the code associated with the CodeBlock
	 * 
	 * @return
	 */
	// TODO: apply flyweight
	public Collection<FormatFragment> getCode() {
		return this.code;
	}

	@Override
	public String toString() {
		return "CodeBlock [" + this.subject + ", " + this.slot + ", "
				+ this.owner + "]";
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
	public String getSubjectName() {
		return this.subject;
	}
}
