package scriptease.model.semodel.librarymodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.ScriptIt;

/**
 * GameTypeConverter contains a collection of ScriptIts which specify how a
 * GameType can be converted to another.
 * 
 * @author mfchurch
 * 
 */
public class TypeConverter {
	private final Collection<ScriptIt> converterScriptIts;

	public TypeConverter() {
		this.converterScriptIts = new ArrayList<ScriptIt>();
	}

	/**
	 * Adds the given ScriptIt to the list of converter ScriptIts if it is a
	 * valid converter (Has one parameter, and one return type), Otherwise it
	 * prints an error and does nothing.
	 * 
	 * @param scriptIt
	 */
	private void addConverterScriptIt(final ScriptIt scriptIt) {
		final String invalid = "Converter ScriptIt is invalid: ";
		final CodeBlock codeBlock = scriptIt.getMainCodeBlock();
		final int parameters = codeBlock.getParameters().size();
		final int types = codeBlock.getTypes().size();
		if (parameters != 1) {
			System.err
					.println(invalid + parameters + " parameters, expected 1");
			return;
		}
		if (types != 1) {
			System.err.println(invalid + types + " return types, expected 1");
			return;
		}
		this.converterScriptIts.add(scriptIt);
	}

	/**
	 * Adds the given type converter ScriptIts to the list of converters.
	 * Invalid type converter ScriptIts will be skipped.
	 * 
	 * @param converters
	 */
	public void addConverterScriptIts(final Collection<ScriptIt> converters) {
		for (final ScriptIt scriptIt : converters) {
			this.addConverterScriptIt(scriptIt);
		}
	}

	public Collection<ScriptIt> getConverterDoIts() {
		return new ArrayList<ScriptIt>(this.converterScriptIts);
	}

	/**
	 * Returns a collection of type keywords to which that type is able to be
	 * converted
	 * 
	 * @param type
	 * @return
	 */
	public Collection<String> getConvertableTypes(final String type) {
		final Collection<String> convertableTypes = new HashSet<String>();
		// For each Converter ScriptIt
		for (final ScriptIt scriptIt : this.converterScriptIts) {
			final CodeBlock codeBlock = scriptIt.getMainCodeBlock();
			// If it's parameter is of the specific type
			for (final KnowIt parameter : codeBlock.getParameters()) {
				if (codeBlock.getTypes().contains(type))
					// then it is a converter for the given type, so add the
					// return type
					convertableTypes.addAll(parameter.getTypes());
			}
		}
		return convertableTypes;
	}

	/**
	 * Get the converter doIt to convert fromType to toType. Returns null if one
	 * does not exist.
	 * 
	 * @param fromType
	 * @return
	 */
	private ScriptIt getConverterForType(String fromType, String toType) {
		for (final ScriptIt scriptIt : this.converterScriptIts) {
			final CodeBlock codeBlock = scriptIt.getMainCodeBlock();
			// If the doIt converts to toType
			if (codeBlock.getTypes().contains(toType)) {
				// and it's parameter is fromType
				for (final KnowIt parameter : codeBlock.getParameters()) {
					if (parameter.getTypes().contains(fromType))
						// then it is a converter for the given type, so return
						// it
						return scriptIt;
				}
			}
		}
		return null;
	}

	public ScriptIt convert(KnowIt component) {
		final KnowItBinding binding = component.getBinding();
		final String bindingType = binding.getFirstType();
		final Collection<String> types = component.getTypes();

		for (String argumentType : types) {
			ScriptIt converter = this.getConverterForType(bindingType,
					argumentType);
			if (converter != null) {
				return converter;
			}
		}
		return null;
	}
}
