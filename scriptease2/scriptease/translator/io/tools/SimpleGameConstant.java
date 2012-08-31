package scriptease.translator.io.tools;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.translator.io.model.GameConstant;

/**
 * Simple class for storing constant data (integers, strings, etc.)
 * 
 * @author mfchurch
 * @author remiller
 */
public class SimpleGameConstant implements GameConstant {
	private final Collection<String> types;
	private final String codeValue;

	public SimpleGameConstant(Collection<String> types, String value) {
		this.types = new ArrayList<String>(types);
		this.codeValue = value;
	}

	@Override
	public Collection<String> getTypes() {
		return types;
	}

	@Override
	public String getCodeText() {
		return this.codeValue;
	}

	@Override
	public String getName() {
		return this.codeValue;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getTag() {
		return this.codeValue;
	}

	@Override
	public String getTemplateID() {
		return this.codeValue;
	}
}