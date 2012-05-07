package scriptease.gui.quests;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;

/**
 * QuestNodePanel is a JPanel used to display a QuestNode in a Graph
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class QuestNodePanel extends JPanel {
	private QuestNode questNode; 

	public QuestNodePanel(final QuestNode questNode) {
		if (questNode == null)
			throw new IllegalArgumentException("quest cannot be null");
		this.questNode = questNode;

		this.setLayout(new QuestNodePanelLayoutManager());
	}

	private class QuestNodePanelLayoutManager implements LayoutManager {
		private static final int INFOPANEL_Y_INDENT = 5;
		private static final int FANINPANEL_X_INDENT = 2;

		private ExpansionButton expansionButton;
		private JButton shrinkButton;
		private JButton growButton;
		private GraphPanel expandedPanel;
		private JPanel collapsedPanel;
		private JPanel fanInPanel;

		public QuestNodePanelLayoutManager() {
			// initialize the panels
			this.collapsedPanel = new JPanel();
			this.collapsedPanel.setOpaque(false);
			GraphNode startPoint = questNode.getStartPoint();
			this.expandedPanel = new GraphPanel(startPoint);

			// expansion button
			expansionButton = ScriptWidgetFactory
					.buildExpansionButton(questNode.isCollapsed());

			expansionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean collapsed = questNode.isCollapsed();
					questNode.setCollapsed(!collapsed);
					expansionButton.setCollapsed(!collapsed);
					QuestNodePanel.this.revalidate();
				}
			});

			// Grow button
			growButton = new JButton();
			growButton.setText(">");
			growButton.setToolTipText("grow the Quest to the next valid end");
			growButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					questNode.grow();
					QuestNodePanel.this.revalidate();
				}
			});

			// Shrink button
			shrinkButton = new JButton();
			shrinkButton.setText("<");
			shrinkButton
					.setToolTipText("shrink the Quest to the previous valid end");
			shrinkButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					questNode.shrink();
					QuestNodePanel.this.revalidate();
				} 
			}); 

			// Name field
			JLabel nameField = new JLabel(questNode.getName());
			// Fan in
			startPoint.process(new AbstractNoOpGraphNodeVisitor() {
				@Override
				public void processQuestNode(QuestNode questNode) {
					GraphNode start = questNode.getStartPoint();
					start.process(this);
				}

				@Override
				public void processQuestPointNode(QuestPointNode questPointNode) {
					QuestPoint questPoint = questPointNode.getQuestPoint();
					fanInPanel = ScriptWidgetFactory.buildFanInPanel(questPoint
							.getFanIn());
				}

				@Override
				protected void defaultProcess(GraphNode node) {
					final String msg = "Processed Illegal Node type";
					System.err.println(msg);
					throw new IllegalStateException(msg);
				}
			});

			// add the components so they display
			this.collapsedPanel.add(nameField);
			this.collapsedPanel.add(shrinkButton);
			this.collapsedPanel.add(growButton);
			this.collapsedPanel.add(expansionButton);
			QuestNodePanel.this.add(collapsedPanel);
			QuestNodePanel.this.add(fanInPanel);
			QuestNodePanel.this.add(expandedPanel);
		}

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();
			final boolean collapsed = questNode.isCollapsed();

			// disable shrink or grow if you can't
			this.shrinkButton.setEnabled(questNode.canShrink());
			this.growButton.setEnabled(questNode.canGrow());

			// update the visibility
			expandedPanel.setVisible(!collapsed);

			if (collapsed) {
				int fanInPanelY = (QuestNodePanel.this.getPreferredSize().height / 2)
						- (this.fanInPanel.getPreferredSize().height / 2);
				layoutFanIn(insets.left, fanInPanelY);
				int collapsedPanelX = (insets.left
						+ this.fanInPanel.getPreferredSize().width + FANINPANEL_X_INDENT);
				layoutCollapsed(collapsedPanelX, insets.top);
			} else {
				layoutExpanded(insets.left, insets.top);
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			final Insets insets = parent.getInsets();
			final boolean collapsed = questNode.isCollapsed();

			if (collapsed)
				return minimumCollapsedLayoutSize(insets.left + insets.right,
						insets.top + insets.bottom);
			else
				return minimumExpandedLayoutSize(insets.left + insets.right,
						insets.top + insets.bottom);
		}

		/**
		 * Calculates the minimum layout size when the panel is collapsed
		 * 
		 * @param parent
		 * @return
		 */
		private Dimension minimumCollapsedLayoutSize(int xSize, int ySize) {
			return new Dimension(xSize
					+ this.fanInPanel.getPreferredSize().width
					+ FANINPANEL_X_INDENT
					+ this.collapsedPanel.getPreferredSize().width, ySize
					+ this.collapsedPanel.getPreferredSize().height);
		}

		/**
		 * Layout the graph panel in it's collapsed form (text)
		 * 
		 * @param parent
		 */
		private void layoutCollapsed(int xLocation, int yLocation) {
			this.collapsedPanel.setBounds(xLocation, yLocation,
					this.collapsedPanel.getPreferredSize().width,
					this.collapsedPanel.getPreferredSize().height);
		}

		/**
		 * Calculates the minimum layout size when the panel is expanded (graph)
		 * 
		 * @param parent
		 * @return
		 */
		protected Dimension minimumExpandedLayoutSize(int xSize, int ySize) {
			// which is bigger: collapsed panel centered, or the expandedPanel?
			xSize += Math.max(
					expandedPanel.getPreferredSize().width / 2
							+ this.collapsedPanel.getPreferredSize().width / 2,
					this.fanInPanel.getPreferredSize().width
							+ FANINPANEL_X_INDENT
							+ this.expandedPanel.getPreferredSize().width);
			ySize += this.collapsedPanel.getPreferredSize().height
					+ INFOPANEL_Y_INDENT
					+ this.expandedPanel.getPreferredSize().height;

			return new Dimension(xSize, ySize);
		}

		private void layoutFanIn(int xLocation, int yLocation) {
			this.fanInPanel.setBounds(xLocation, yLocation,
					this.fanInPanel.getPreferredSize().width,
					this.fanInPanel.getPreferredSize().height);
		}

		/**
		 * Layout the graph panel in it's expanded form (graph)
		 * 
		 * @param xLocation
		 * @param yLocation
		 */
		protected void layoutExpanded(int xLocation, int yLocation) {
			int collapsedPanelX = xLocation
					+ (expandedPanel.getPreferredSize().width / 2 - collapsedPanel
							.getPreferredSize().width / 2);
			this.layoutCollapsed(collapsedPanelX, yLocation);

			int fanInPanelY = yLocation
					+ (QuestNodePanel.this.getPreferredSize().height / 2)
					- (fanInPanel.getPreferredSize().height / 2);
			this.layoutFanIn(xLocation, fanInPanelY);

			xLocation += this.fanInPanel.getPreferredSize().width
					+ FANINPANEL_X_INDENT;

			yLocation += this.collapsedPanel.getPreferredSize().height
					+ INFOPANEL_Y_INDENT;

			// graphPanel does the rest
			expandedPanel.setBounds(xLocation, yLocation,
					expandedPanel.getPreferredSize().width,
					expandedPanel.getPreferredSize().height);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}
	}
}
