package scriptease.gui.control;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;

import scriptease.gui.SETree.ui.ScriptEaseUI;

/**
 * JButton used for toggling between expanded and collapsed states.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class ExpansionButton extends JButton {
	private Icon collapse;
	private Icon expand;

	public ExpansionButton(boolean isCollapsed) {
		expand = ScriptEaseUI.COLLAPSE_ICON;
		collapse = ScriptEaseUI.EXPAND_ICON;
		Dimension maxSize = new Dimension(Math.max(collapse.getIconWidth(),
				expand.getIconWidth()) + 1, Math.max(collapse.getIconHeight(),
				expand.getIconHeight()) + 1);
		this.setPreferredSize(maxSize);
		this.setFocusable(false);
		this.setOpaque(true);
		this.setCollapsed(isCollapsed); 
	}

	public void setCollapsed(boolean isCollapsed) {
		if (isCollapsed)
			this.setIcon(collapse);
		else
			this.setIcon(expand);
	}
}
