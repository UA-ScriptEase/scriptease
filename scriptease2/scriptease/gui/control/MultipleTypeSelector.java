package scriptease.gui.control;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import scriptease.model.atomic.KnowIt;
import scriptease.translator.TranslatorManager;

/**
 * TypeSelector represents a combo box used to select the type of a knowIt
 * 
 * @author mfchurch
 */
@SuppressWarnings("serial")
public class MultipleTypeSelector extends JPanel implements ItemListener {
	private KnowIt edited;
	List<JCheckBox> checkBoxTypes;

	/**
	 * Creates a new Event Slot Editor
	 */
	public MultipleTypeSelector() {
		super();
		Collection<String> types = TranslatorManager.getInstance()
				.getActiveTranslator().getGameTypeManager().getKeywords();
		checkBoxTypes = new ArrayList<JCheckBox>();
		for (String type : types) {
			JCheckBox checkBox = new JCheckBox(type);
			checkBox.setName(type);
			checkBoxTypes.add(checkBox);
			checkBox.setSelected(false);
			checkBox.addItemListener(this);
			this.add(checkBox);
		}
	}

	public void setEditedComponent(KnowIt edited) {
		this.edited = edited;
		setSelectedIndexToKnowItTypes();
	}

	/**
	 * Moves the selected index in the comboBox to the current type of the
	 * knowIt
	 */
	private void setSelectedIndexToKnowItTypes() {
		Collection<String> types = edited.getAcceptableTypes();
		for (JCheckBox checkBox : checkBoxTypes) {
			if (types.contains(checkBox.getName()))
				checkBox.setSelected(true);
			else
				checkBox.setSelected(false);
		}
	}

	public void itemStateChanged(ItemEvent e) {
        JCheckBox source = (JCheckBox) e.getItemSelectable();

		if (edited != null) {

			if (source.isSelected()) {
				if (!edited.getTypes().contains(source.getName()))
					this.edited.addType(source.getName());
			} else {
				if (edited.getTypes().contains(source.getName()))
					this.edited.removeType(source.getName());
    }
		}
	}
}