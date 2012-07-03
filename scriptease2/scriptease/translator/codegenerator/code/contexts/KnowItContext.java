package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;
import java.util.Iterator;

import scriptease.controller.VariableGetter;
import scriptease.controller.apimanagers.TypeConverter;
import scriptease.gui.quests.QuestNode;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * KnowItContext is Context for a KnowIt object.
 * 
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItContext extends StoryComponentContext {

	public KnowItContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInformation) {
		super(model, indent, existingNames, translator, locationInformation);
	}

	public KnowItContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public KnowItContext(Context other, KnowIt source) {
		this(other);
		component = source;
	}

	/**
	 * Get the KnowIt's Binding
	 */
	@Override
	public KnowItBinding getBinding() {
		return ((KnowIt) component).getBinding();
	}

	@Override
	public String getValue() {
		Context binding = ContextFactory.getInstance().createContext(this,
				((KnowIt) component).getBinding());
		return binding.getValue();
	}

	@Override
	public String getFormattedValue() {
		final KnowIt knowIt = (KnowIt) component;
		final KnowItBinding binding = knowIt.getBinding();
		final Collection<String> types = knowIt.getTypes();
		final Context bindingContext = ContextFactory.getInstance()
				.createContext(this, knowIt.getBinding());

		final String formattedValue = bindingContext.getFormattedValue();

		if (binding.explicitlyCompatibleWith((KnowIt) component)) {
			return formattedValue;
		} else {
			// the binding type isn't a listed type for this KnowIt, so try to
			// convert it
			final TypeConverter converter = TranslatorManager.getInstance()
					.getActiveTranslator().getGameTypeManager()
					.getTypeConverter();
			final ScriptIt doIt = converter.convert(knowIt);
			if (doIt != null) {
				final Context converterContext = ContextFactory.getInstance()
						.createContext(this, doIt);
				String converterContextName = converterContext.getName() + "("
						+ formattedValue + ")";
				return converterContextName;
			} else
				throw new CodeGenerationException("<Cannot convert binding("
						+ binding + ") to expected types(" + types + ")>");
		}
	}

	/**
	 * Get the KnowIt's Binding's Type
	 */
	@Override
	public String getType() {
		final APIDictionary apiDictionary = this.translator.getApiDictionary();
		return apiDictionary.getGameTypeManager().getCodeSymbol(
				((KnowIt) component).getDefaultType());
	}

	@Override
	public Iterator<KnowIt> getVariables() {
		/**
		 * Get all of the dependent variables for the KnowIt (since we are
		 * probably defining it)
		 */
		VariableGetter getter = new VariableGetter();
		this.component.process(getter);
		return getter.getObjects().iterator();
	}

	@Override
	public String toString() {
		return "KnowItContext [" + component + "]";
	}
}
