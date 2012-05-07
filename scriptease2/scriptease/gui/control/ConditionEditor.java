package scriptease.gui.control;

import java.util.Collection;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.tools.GameConstantFactory;

/**
 * FlushableTextField for editing the name of a parameter.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public class ConditionEditor extends FlushableTextField implements
		StoryComponentObserver, DocumentListener {
	private KnowIt edited;

	/**
	 * Creates a new Name Editor
	 */
	public ConditionEditor(KnowIt condition) {
		super(condition.getBinding().getScriptValue(), 25);
		setEditedComponent(condition);
	}

	private void setEditedComponent(KnowIt edited) {
		StoryComponent oldComponent = this.edited;
		if (oldComponent != null)
			oldComponent.removeStoryComponentObserver(this);
		this.edited = edited;
		this.edited.addStoryComponentObserver(this);
		this.getDocument().addDocumentListener(this);
	}

	@Override
	protected void updateKnowItBinding(final String newValue) {
		GameConstant constant = GameConstantFactory.getInstance().getConstant(
				edited.getTypes(), newValue);
		this.edited.setBinding(constant);
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_TYPE) {
			this.setText("");
			this.setInputVerifier(new TypeVerifier(edited.getAcceptableTypes()));
			edited.clearBinding();
		}
	}

	@Override
	protected String getModelText() {
		return edited.getBinding().getScriptValue();
	}

	/**
	 * TypeVerifier is a standard InputVerifier which checks if the input
	 * matches the regex of any of the provided types.
	 * 
	 * @author mfchurch
	 * 
	 */
	private class TypeVerifier extends InputVerifier {
		private Collection<String> types;

		public TypeVerifier(Collection<String> types) {
			super();
			this.types = types;
		}

		// Checks if the input matches the selected type's regex
		@Override
		public boolean verify(JComponent input) {
			final GameTypeManager typeManager;

			typeManager = TranslatorManager.getInstance().getActiveTranslator()
					.getGameTypeManager();

			if (!types.isEmpty()) {
				for (String type : types) {
					if (typeManager.hasReg(type)) {
						JTextField tf = (JTextField) input;
						if (tf.getText().matches(typeManager.getReg(type)))
							return true;
					}
				}
				return false;
			}
			return true;
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateKnowItBinding(this.getText());
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateKnowItBinding(this.getText());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateKnowItBinding(this.getText());
	}
}