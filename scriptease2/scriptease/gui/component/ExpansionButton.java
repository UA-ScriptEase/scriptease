package scriptease.gui.component;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;

import scriptease.gui.ui.ScriptEaseUI;

/**
 * JButton used for toggling between expanded and collapsed states.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class ExpansionButton extends JButton {
	private final Icon collapse;
	private final Icon expand;

	public ExpansionButton(boolean isCollapsed) {
		this.expand = ScriptEaseUI.COLLAPSE_ICON;
		this.collapse = ScriptEaseUI.EXPAND_ICON;

		final Dimension MAX_SIZE;

		MAX_SIZE = new Dimension(Math.max(this.collapse.getIconWidth(),
				this.expand.getIconWidth()) + 1, Math.max(
				this.collapse.getIconHeight(), this.expand.getIconHeight()) + 1);

		this.setPreferredSize(MAX_SIZE);
		this.setFocusable(false);
		this.setOpaque(true);
		this.setCollapsed(isCollapsed);
	}

	public void setCollapsed(boolean isCollapsed) {
		if (isCollapsed)
			this.setIcon(this.collapse);
		else
			this.setIcon(this.expand);
	}
}
