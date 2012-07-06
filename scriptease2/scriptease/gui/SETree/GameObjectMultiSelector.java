package scriptease.gui.SETree;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import scriptease.gui.SEMultiSelector;

public class GameObjectMultiSelector extends SEMultiSelector {

	public GameObjectMultiSelector(ArrayList<String> rootDataTypes) {
		super(rootDataTypes);
		this.data = new ArrayList<String>();
		for (String type : rootData) {
			data.add(type);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton)
			popUpMenu().show((JComponent) e.getSource(),
					((JComponent) e.getSource()).getWidth(), 0);

		if (e.getSource() instanceof JCheckBoxMenuItem) {
			if (((JCheckBoxMenuItem) e.getSource()).isSelected())
				data.add(((JCheckBoxMenuItem) e.getSource()).getText());
			else
				data.remove(((JCheckBoxMenuItem) e.getSource()).getText());
			setChanged();
			notifyObservers(data);
		}
	}

	@Override
	protected String setLabel() {
		return "Types";
	}

	@Override
	protected void populateMenu(JPopupMenu a) {
		final List<JCheckBoxMenuItem> buttons;
		JCheckBoxMenuItem item;
		final MenuVisibilityHandler menuVisHandler;
		buttons = new ArrayList<JCheckBoxMenuItem>();
		menuVisHandler = new MenuVisibilityHandler(a);

		for (String type : this.rootData) {
			item = new JCheckBoxMenuItem(type);

			if (data.contains(type))
				item.setSelected(true);
			else
				item.setSelected(false);

			item.setIcon(null);
			item.addActionListener(menuVisHandler);
			item.addActionListener(this);
			buttons.add(item);
		}
		a.addSeparator();
		for (JCheckBoxMenuItem newItem : buttons) {
			a.add(newItem);
		}
	}
}
