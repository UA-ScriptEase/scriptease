package scriptease.gui.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * JPanelList is a List for JPanels (selection not supported). Created to be
 * used with ParameterPanels
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class JPanelList extends JScrollPane {
	final List<JPanel> panels;
	final JPanel contentPanel;

	public JPanelList() {
		this.panels = new ArrayList<JPanel>();
		this.contentPanel = new JPanel();
		this.contentPanel.setLayout(new JPanelListLayoutManager());
		this.setViewportView(contentPanel);
		this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
	}

	public void add(Collection<JPanel> panels) {
		for (JPanel panel : panels) {
			this.add(panel);
		}
	}

	public void add(JPanel panel) {
		panel.setBorder(BorderFactory
				.createMatteBorder(0, 0, 1, 0, Color.black)); 
		this.panels.add(panel);
		this.contentPanel.add(panel);
		this.contentPanel.doLayout();
	}

	public void remove(JPanel panel) {
		this.panels.remove(panel);
		this.contentPanel.remove(panel);
		this.contentPanel.doLayout();
	}

	private class JPanelListLayoutManager implements LayoutManager {
		private int Y_BUFFER = 0;

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return this.minimumLayoutSize(parent);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			final Insets insets = parent.getInsets();
			int xSize = 0;
			int ySize = 0;
			for (JPanel panel : panels) {
				final Dimension preferredSize = panel.getPreferredSize();
				xSize = (int) Math.max(xSize, preferredSize.getWidth());
				ySize += Y_BUFFER + preferredSize.getHeight();
			}
			return new Dimension(xSize + insets.left + insets.right, ySize
					+ insets.top + insets.bottom);
		}

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();
			int xLocation = insets.left;
			int yLocation = insets.top;
			for (JPanel panel : panels) {
				final Dimension preferredSize = panel.getPreferredSize();
				panel.setBounds(xLocation, yLocation,
						(int) preferredSize.getWidth(),
						(int) preferredSize.getHeight());
				yLocation += Y_BUFFER + preferredSize.getHeight();
			}
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}
	}
}
