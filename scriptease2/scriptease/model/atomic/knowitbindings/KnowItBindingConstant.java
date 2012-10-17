package scriptease.model.atomic.knowitbindings;

import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.tools.SimpleGameConstant;

/**
 * This class represents a <b>Constant</b> binding for a <code>KnowIt</code>.
 * 
 * @author graves
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingConstant extends KnowItBinding {
	private final GameConstant constantValue;

	public KnowItBindingConstant(GameConstant value) {
		if (value == null)
			throw new IllegalStateException(
					"GameConstant's cannot be set to null in a KnowItBindingConstant.");
		this.constantValue = value;
	}

	@Override
	public String getScriptValue() {
		return this.constantValue.getCodeText();
	}

	public String getTag() {
		return this.constantValue.getTag();
	}

	public String getTemplateID() {
		return this.constantValue.getTemplateID();
	}

	@Override
	public GameConstant getValue() {
		return this.constantValue;
	}

	@Override
	public Collection<String> getTypes() {
		return this.constantValue.getTypes();
	}

	@Override
	public String toString() {
		if (this.isIdentifiableGameConstant())
			return this.getValue().getTemplateID();
		return this.getValue().getTypes().iterator().next();
	}

	@Override
	public boolean compatibleWith(KnowIt knowIt) {
		if (typeMatches(knowIt.getAcceptableTypes())) {
			if (knowIt.getOwner() != null
					&& !(this.getValue() instanceof SimpleGameConstant)) {
				final PatternModel model;

				model = PatternModelManager.getInstance().getActiveModel();
				if (model instanceof StoryModel) {
					return ((StoryModel) model).getModule()
							.getInstanceForObjectIdentifier(
									this.getValue().getTemplateID()) != null;
				}
			} else
				return true;
		}
		return false;
	}

	/**
	 * Determines if this binding is wrapping a Game Object (<code>true</code>)
	 * or a Simple Game Constant (<code>false</code>).
	 * 
	 * @return <code>true</code> if the binding represents a Game Object,
	 *         <code>false</code> otherwise.
	 */
	public boolean isIdentifiableGameConstant() {
		return !(this.constantValue instanceof SimpleGameConstant);
	}

	@Override
	public boolean equals(Object other) {
		if (this.constantValue.getCodeText() != null)
			return (other instanceof KnowItBindingConstant)
					&& ((KnowItBindingConstant) other).constantValue
							.equals(this.constantValue);
		return false;
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	/**
	 * No need to clone constants, that is why they are constant
	 */
	@Override
	public KnowItBinding clone() {
		return this;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processConstant(this);
	}
}
