package scriptease.gui.control.editor;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.undo.UndoManager;
import scriptease.model.PatternModelManager;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.tools.GameConstantFactory;

/**
 * ValueEditor takes a KnowIt and edits the KnowItBinding's Value's scriptValue.
 * Currently only supports KnowItBindingConstants
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class ValueEditor extends NameEditor {
	public ValueEditor(KnowIt component) {
		super(component);
		if (!(component.getBinding() instanceof KnowItBindingConstant))
			System.err
					.println("Warning: ValueEditor currently only supports KnowItBindingConstant");
	}

	@Override
	protected KnowIt getComponent() {
		return (KnowIt) this.storyComponent;
	}

	@Override
	protected void setupTextField() {
		final KnowItBinding binding = this.getComponent().getBinding();
		binding.process(new AbstractNoOpBindingVisitor() {
			@Override
			public void processConstant(KnowItBindingConstant constant) {
				ValueEditor.this.setText(constant.getScriptValue());
			}
		});

	}

	@Override
	protected void updateText() {
		final String newValue = this.getText();
		if (PatternModelManager.getInstance().hasActiveModel()) {
			final KnowIt knowIt = this.getComponent();
			final KnowItBinding binding = knowIt.getBinding();
			binding.process(new AbstractNoOpBindingVisitor() {
				@Override
				public void processConstant(KnowItBindingConstant constant) {
					final String oldValue = constant.getScriptValue();
					if (!oldValue.equals(newValue)) {
						if (!UndoManager.getInstance().hasOpenUndoableAction()) {
							UndoManager.getInstance().startUndoableAction(
									"Change " + oldValue + " to " + newValue);
							GameConstant newConstant = GameConstantFactory
									.getInstance().getConstant(
											constant.getTypes(), newValue);
							knowIt.setBinding(newConstant);
							UndoManager.getInstance().endUndoableAction();
						}
					}
				}
			});
		}
	}
}
