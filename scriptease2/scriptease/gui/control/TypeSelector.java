package scriptease.gui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JComboBox;

import scriptease.model.atomic.KnowIt;
import scriptease.translator.TranslatorManager;

/**
 * TypeSelector represents a combo box used to select the type of a knowIt
 * 
 * @author mfchurch
 */
@SuppressWarnings("serial")
public class TypeSelector extends JComboBox implements ActionListener {
	private KnowIt edited;

	/**
	 * Creates a new Event Slot Editor
	 */
	public TypeSelector() {
		super(TranslatorManager.getInstance().getActiveTranslator().getGameTypeManager()
				.getKeywords().toArray());
	}

	public void setEditedComponent(KnowIt edited) {
		this.edited = edited;
		this.setSelectedIndexToKnowItType();
		this.addActionListener(this);
	}

	/**
	 * Moves the selected index in the comboBox to the current type of the
	 * knowIt
	 */
	private void setSelectedIndexToKnowItType() {
		Collection<String> types = TranslatorManager.getInstance().getActiveTranslator().getGameTypeManager().getKeywords();
		int location = 0;
		for (String type : types) {
			if (edited.getAcceptableTypes().contains(type)) {
				this.setSelectedIndex(location);
				return;
			}
			location++;
		}
		this.setSelectedIndex(-1);
	}

	private void updateKnowItType(String type) {
		this.edited.clearTypes();
		this.edited.addType(type);
	}

	/** Listens to the combo box. */
	@Override
	public void actionPerformed(ActionEvent e) {
		TypeSelector selector = (TypeSelector) e.getSource();
		String type = (String) selector.getSelectedItem();
		selector.updateKnowItType(type);
	}
}