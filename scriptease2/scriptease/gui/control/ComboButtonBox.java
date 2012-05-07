package scriptease.gui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * ComboButton is an AbstractButton which acts like a comboBox of JButtons. Ya
 * dig?
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class ComboButtonBox extends JButton {
	private Collection<AbstractButton> buttons;
	private JPopupMenu popupMenu;
	private AbstractButton selectedButton;
	private Icon icon = UIManager.getIcon("Tree.collapsedIcon");

	public ComboButtonBox(Collection<AbstractButton> buttons) {
		super();
		this.setHorizontalTextPosition(SwingConstants.LEFT);
		this.setButtons(buttons);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				popupMenu = buildPopUpMenu();
				popupMenu.setLocation(ComboButtonBox.this.getLocationOnScreen());
				popupMenu.setVisible(true);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				popupMenu.setVisible(false);
				if (selectedButton != null)
					chooseButton(selectedButton);
			}
		});
	}

	private JPopupMenu buildPopUpMenu() {
		JPopupMenu popUp = new JPopupMenu();

		// add the selected button first
		this.addButtonToPopUpMenu(popUp, selectedButton);

		// add the rest
		for (final AbstractButton button : buttons) {
			if (button != selectedButton)
				this.addButtonToPopUpMenu(popUp, button);
		}
		return popUp;
	}

	private void addButtonToPopUpMenu(JPopupMenu popUp,
			final AbstractButton button) {
		// safety
		if (button == null)
			return;

		popUp.add(button);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				selectedButton = button;
			}
		});
	}

	public void setButtons(Collection<AbstractButton> buttons) {
		this.buttons = new ArrayList<AbstractButton>(buttons);

		if (!this.buttons.isEmpty())
			chooseButton(this.buttons.iterator().next());
	}

	private void chooseButton(AbstractButton button) {
		final String text = button.getText();
		final Icon icon = button.getIcon();
		final Action action = button.getAction();

		if (action != null)
			this.setAction(action);
		if (icon != null)
			this.setIcon(icon);
		else
			this.setIcon(this.icon);
		if (text != null)
			this.setText(text);
		button.doClick();
	}

	public Collection<AbstractButton> getButtons() {
		return this.buttons;
	}
}
