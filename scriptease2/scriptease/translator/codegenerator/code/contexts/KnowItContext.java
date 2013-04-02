package scriptease.translator.codegenerator.code.contexts;

import java.util.Iterator;

import scriptease.controller.get.VariableGetter;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.apimanagers.TypeConverter;
import scriptease.translator.codegenerator.CodeGenerationException;

/**
 * KnowItContext is Context for a KnowIt object.
 * 
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItContext extends StoryComponentContext {
	public KnowItContext(Context other, KnowIt source) {
		super(other, source);
	}

	/**
	 * Get the KnowIt's Binding
	 */
	@Override
	public KnowItBinding getBinding() {
		return this.getComponent().getBinding();
	}

	@Override
	public String getValue() {
		final KnowItBinding binding = this.getBinding();
		final Context bindingContext;

		bindingContext = ContextFactory.getInstance().createContext(this,
				binding);

		return bindingContext.getValue();
	}

	@Override
	public String getTemplateID() {
		final KnowItBinding binding = this.getBinding();

		if (binding instanceof KnowItBindingResource) {
			return ((KnowItBindingResource) binding).getTemplateID();
		}
		return "Error when generating Template ID in " + this;
	}

	@Override
	public String getFormattedValue() {
		final KnowIt knowIt = this.getComponent();
		final KnowItBinding binding = this.getBinding();
		final Context bindingContext;

		bindingContext = ContextFactory.getInstance().createContext(this,
				binding);

		final String formattedValue = bindingContext.getFormattedValue();
		if (formattedValue.isEmpty()) {
			System.err.println("Value of " + knowIt
					+ " resolved to an empty string.");
		}

		if (binding.explicitlyCompatibleWith(knowIt)) {
			return formattedValue;
		} else {
			// the binding type isn't a listed type for this KnowIt, so try to
			// convert it
			final TypeConverter converter;
			final ScriptIt scriptIt;

			converter = TranslatorManager.getInstance()
					.getActiveGameTypeManager().getTypeConverter();
			scriptIt = converter.convert(knowIt);

			if (scriptIt != null) {
				final Context scriptItContext;
				final String scriptItContextName;

				scriptItContext = ContextFactory.getInstance().createContext(
						this, scriptIt);
				scriptItContextName = scriptItContext.getName() + "("
						+ formattedValue + ")";
				return scriptItContextName;
			} else
				throw new CodeGenerationException("<Cannot convert binding("
						+ binding + ") to expected types(" + knowIt.getTypes()
						+ ")>");
		}
	}

	/**
	 * Get the KnowIt's Binding's Type
	 */
	@Override
	public String getType() {
		final GameTypeManager typeManager;
		final String defaultType = this.getComponent().getDefaultType();

		typeManager = this.translator.getGameTypeManager();

		return typeManager.getCodeSymbol(defaultType);
	}

	@Override
	public String getUniqueID() {
		final KnowItBinding binding = this.getComponent().getBinding();

		if (binding instanceof KnowItBindingStoryPoint) {
			return ((KnowItBindingStoryPoint) binding).getValue().getUniqueID()
					.toString();
		} else {
			return "<Binding was not of type \"KnowItBindingStoryPoint\">";
		}
	}

	@Override
	public Iterator<KnowIt> getVariables() {
		/**
		 * Get all of the dependent variables for the KnowIt (since we are
		 * probably defining it)
		 */
		final VariableGetter getter = new VariableGetter();
		this.getComponent().process(getter);
		return getter.getObjects().iterator();
	}

	@Override
	public String toString() {
		return "KnowItContext [" + this.getComponent() + "]";
	}

	@Override
	protected KnowIt getComponent() {
		return (KnowIt) super.getComponent();
	}
}
