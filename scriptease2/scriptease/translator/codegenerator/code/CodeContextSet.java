package scriptease.translator.codegenerator.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Stores intermediary Code Generation data. There are three code segments that
 * may or may not be used: Declaration, Definition and Call. These segments are
 * the three different ways that a function or variable can appear in code.
 * Other code concepts, like conditional statements or even the entire file
 * itself, would only used the Definition segment.<br>
 * <br>
 * Furthermore, it also records the set of names used in the immediate scope of
 * the Definition.
 * 
 * @author jtduncan
 * @author remiller
 */
public class CodeContextSet {
	private final List<AbstractFragment> declaration;
	private final List<AbstractFragment> definition;
	private final List<AbstractFragment> call;

	/**
	 * Creates a new instance that has no declaration, definition, or call.
	 */
	public CodeContextSet() {
		this(null, null, null);
	}

	/**
	 * Creates a new instance that starts out with the given declaration,
	 * definition, and call.
	 * 
	 * @param declaration
	 *            The collection of fragments that define the declaration
	 * @param definition
	 *            The collection of fragments that define the definition
	 * @param call
	 *            The collection of fragments that define how the code concept
	 *            gets used
	 */
	public CodeContextSet(Collection<AbstractFragment> declaration,
			Collection<AbstractFragment> definition,
			Collection<AbstractFragment> call) {
		this.declaration = new ArrayList<AbstractFragment>();
		this.definition = new ArrayList<AbstractFragment>();
		this.call = new ArrayList<AbstractFragment>();

		if (declaration != null)
			this.declaration.addAll(declaration);
		if (definition != null)
			this.definition.addAll(definition);
		if (call != null)
			this.call.addAll(call);
	}

	/**
	 * Builds a new instance that copies the given CodeContextSet.
	 * 
	 * @param other
	 *            The CodeContext set to mimic.
	 */
	public CodeContextSet(CodeContextSet other) {
		this(other.getDeclaration(), other.getDefinition(), other.getCall());
	}

	/**
	 * Adds the contents of addedCode to the <i>declaration</i> definition.
	 * 
	 * @param addedCode
	 *            the collection of fragments to add.
	 */
	public void appendToDeclaration(Collection<AbstractFragment> addedCode) {
		this.declaration.addAll(addedCode);
	}

	/**
	 * Adds the contents of addedCode to the <i>definition</i> definition.
	 * 
	 * @param addedCode
	 */
	public void appendToDefinition(Collection<AbstractFragment> addedCode) {
		this.definition.addAll(addedCode);
	}

	/**
	 * Adds the contents of addedCode to the <i>call</i> definition.
	 * 
	 * @param addedCode
	 */
	public void appendToCall(Collection<AbstractFragment> addedCode) {
		this.call.addAll(addedCode);
	}

	/**
	 * Gets a copy of the contents of the declaration token collection.
	 * 
	 * @return the declaration
	 */
	public List<AbstractFragment> getDeclaration() {
		return new ArrayList<AbstractFragment>(this.declaration);
	}

	/**
	 * Gets a copy of the contents of the definition token collection.
	 * 
	 * @return the definition
	 */
	public List<AbstractFragment> getDefinition() {
		return new ArrayList<AbstractFragment>(this.definition);
	}

	/**
	 * Gets a copy of the contents of the call token collection.
	 * 
	 * @return the call
	 */
	public List<AbstractFragment> getCall() {
		return new ArrayList<AbstractFragment>(this.call);
	}
}
