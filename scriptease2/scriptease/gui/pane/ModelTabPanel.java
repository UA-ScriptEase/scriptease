package scriptease.gui.pane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import scriptease.ScriptEase;
import scriptease.controller.FileManager;
import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.UndoManagerObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.ComponentFactory;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.action.file.CloseModelAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.libraryeditor.LibraryEditorPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.LibraryModel;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;
import scriptease.model.complex.StoryPoint;
import scriptease.util.BiHashMap;

@SuppressWarnings("serial")
class ModelTabPanel extends JTabbedPane {
	// A mapping of models to components represented by the models
	private final BiHashMap<SEModel, JComponent> modelToComponent;

	protected ModelTabPanel() {
		this.modelToComponent = new BiHashMap<SEModel, JComponent>();

		// Register a change listener
		this.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				final JComponent tab;

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				// Get the activated frame
				tab = (JComponent) pane.getSelectedComponent();

				if (tab != null) {
					final SEModel model;

					model = ModelTabPanel.this.modelToComponent.getKey(tab);

					SEModelManager.getInstance().activate(model);
				}
			}
		});

		SEModelManager.getInstance().addPatternModelObserver(this,
				new SEModelObserver() {
					@Override
					public void modelChanged(SEModelEvent event) {
						final SEModel model = event.getPatternModel();

						if (event.getEventType() == SEModelEvent.Type.REMOVED) {
							ModelTabPanel.this.removeTabForModel(model);
						} else if (event.getEventType() == SEModelEvent.Type.ADDED) {
							ModelTabPanel.this.createTabForModel(model);
						}

					}
				});
	}

	/**
	 * Removes the tab for the given model.
	 * 
	 * @param model
	 */
	private void removeTabForModel(SEModel model) {
		final BiHashMap<SEModel, JComponent> modelToComponent;
		final JComponent component;

		modelToComponent = ModelTabPanel.this.modelToComponent;
		component = modelToComponent.getValue(model);

		if (FileManager.getInstance().hasUnsavedChanges(model)) {
			// otherwise, close the StoryModel
			modelToComponent.removeKey(model);

			ModelTabPanel.this.remove(component);
		}
	}

	/**
	 * Creates a tab for the given model.
	 * 
	 * @param model
	 */
	private void createTabForModel(SEModel model) {
		model.process(new ModelAdapter() {
			@Override
			public void processLibraryModel(final LibraryModel libraryModel) {
				// Creates a Library Editor panel
				final JPanel scbPanel;
				final JScrollPane scbScrollPane;
				final String savedTitle;

				scbPanel = LibraryEditorPanelFactory.getInstance()
						.buildLibraryEditorPanel(LibraryPanel.getInstance());
				scbScrollPane = new JScrollPane(scbPanel);

				ModelTabPanel.this.modelToComponent.put(libraryModel,
						scbScrollPane);

				savedTitle = libraryModel.getName() + "[Editor]";

				scbScrollPane.getVerticalScrollBar().setUnitIncrement(
						ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

				ModelTabPanel.this.buildTab(savedTitle, libraryModel,
						scbScrollPane);
			}

			@Override
			public void processStoryModel(final StoryModel storyModel) {
				// Creates a story editor panel with a story graph
				final StoryPoint root;
				final JSplitPane newPanel;
				String modelTitle;

				root = storyModel.getRoot();
				newPanel = ModelTabPanel.this.buildStoryPanel(storyModel, root);
				modelTitle = storyModel.getTitle();

				if (modelTitle == null || modelTitle.equals(""))
					modelTitle = "<Untitled>";

				final String savedTitle = modelTitle + "("
						+ storyModel.getModule().getLocation().getName() + ")";

				ModelTabPanel.this.buildTab(savedTitle, storyModel, newPanel);
				/*
				 * Setting the divider needs to occur here because the
				 * JSplitPane needs to actually be drawn before this works.
				 * According to Sun, this is WAD. I would tend to disagree, but
				 * at least this is nicer than subclassing JSplitPane.
				 */
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Need this code for the divider to be set at the right
						// location for different screen resolutions.
						final double top = newPanel.getTopComponent().getSize().height;
						final double bottom = newPanel.getSize().height;
						final double ratio = top / bottom;
						newPanel.setDividerLocation(ratio);
					}
				});
			}
		});
	}

	private void buildTab(final String title, final SEModel model,
			final JComponent tabContents) {
		final CloseableModelTab newTab;
		final Icon icon;

		if (model.getTranslator() != null)
			icon = model.getTranslator().getIcon();
		else
			icon = null;

		newTab = new CloseableModelTab(this, tabContents, model, icon);

		this.addTab(title, icon, tabContents);

		this.setTabComponentAt(this.indexOfComponent(tabContents), newTab);

		this.setSelectedComponent(tabContents);

		this.setFocusable(false);

		UndoManager.getInstance().addUndoManagerObserver(model,
				new UndoManagerObserver() {
					@Override
					public void stackChanged() {
						final ModelTabPanel tabPanel = ModelTabPanel.this;
						final int index = tabPanel
								.indexOfComponent(tabContents);

						if (index < 0)
							return;

						if (UndoManager.getInstance().isSaved(model)) {
							tabPanel.setTitleAt(index, title);
						} else {
							tabPanel.setTitleAt(index, "*" + title);
						}
					}
				});
	}

	/**
	 * Builds a panel for a StoryModel. This panel includes an {@link SEGraph}
	 * and a {@link StoryComponentPanelTree}.
	 * 
	 * @param model
	 * @param start
	 * @return
	 */
	private JSplitPane buildStoryPanel(StoryModel model, final StoryPoint start) {
		final JSplitPane storyPanel;
		final JToolBar graphToolBar;

		final SEGraph<StoryPoint> storyGraph;
		final StoryComponentPanelTree storyComponentTree;
		final StoryComponentObserver graphRedrawer;
		final JPanel storyGraphPanel;

		final JScrollPane storyGraphScrollPane;

		storyPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		graphToolBar = ComponentFactory.buildGraphEditorToolBar();

		storyGraph = SEGraphFactory.buildStoryGraph(start);

		storyComponentTree = new StoryComponentPanelTree(start);
		graphRedrawer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;

				type = event.getType();

				if (type == StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_ADDED) {
					event.getSource().addStoryComponentObserver(this);
					storyGraph.repaint();
					storyGraph.revalidate();
				} else if (type == StoryComponentChangeEnum.CHANGE_FAN_IN
						|| type == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					storyGraph.repaint();
					storyGraph.revalidate();
				} else if (type == StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_REMOVED) {
					storyGraph.repaint();
					storyGraph.revalidate();

					// Set root to start node if we remove the selected node.
					if (event.getSource() == storyComponentTree.getRoot()) {
						final Collection<StoryPoint> nodes;

						nodes = new ArrayList<StoryPoint>();

						nodes.add(storyGraph.getStartNode());

						storyGraph.setSelectedNodes(nodes);
						storyComponentTree.setRoot(storyGraph.getStartNode());
					}
				}

			}
		};
		storyGraphPanel = new JPanel();

		storyGraphScrollPane = new JScrollPane(storyGraph);

		for (StoryPoint point : start.getDescendants()) {
			point.addStoryComponentObserver(graphRedrawer);
		}

		// Put the new pane to the map

		ModelTabPanel.this.modelToComponent.put(model, storyPanel);

		// Set up the Story Graph
		storyGraph.addSEGraphObserver(new SEGraphAdapter<StoryPoint>() {

			@Override
			public void nodesSelected(final Collection<StoryPoint> nodes) {
				SEModelManager.getInstance().getActiveModel()
						.process(new ModelAdapter() {
							@Override
							public void processStoryModel(StoryModel storyModel) {
								storyComponentTree.setRoot(nodes.iterator()
										.next());
							}
						});
			}

			@Override
			public void nodeOverwritten(StoryPoint node) {
				node.revalidateKnowItBindings();
			}

			@Override
			public void nodeRemoved(StoryPoint removedNode) {
				start.revalidateKnowItBindings();
			}

		});

		start.addStoryComponentObserver(graphRedrawer);

		storyGraphPanel.setLayout(new BorderLayout());

		// Reset the ToolBar to select and add the Story Graph to it.
		GraphToolBarModeAction.setMode(GraphToolBarModeAction.getMode());

		final String orientation = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_ORIENTATION_KEY);

		if (orientation != null
				&& orientation.equalsIgnoreCase(ScriptEase.HORIZONTAL_TOOLBAR)) {
			storyGraphScrollPane.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));

			storyGraphPanel.add(graphToolBar, BorderLayout.PAGE_START);
		} else {// if toolbar is vertical
			storyGraphScrollPane.setBorder(BorderFactory.createEmptyBorder());
			storyGraphPanel.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));

			graphToolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
					Color.LIGHT_GRAY));

			storyGraphPanel.add(graphToolBar, BorderLayout.WEST);
		}

		storyGraphPanel.add(storyGraphScrollPane, BorderLayout.CENTER);

		storyComponentTree.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		// Set up the split pane
		storyPanel.setBorder(null);
		storyPanel.setOpaque(true);
		storyPanel.setTopComponent(storyGraphPanel);
		storyPanel.setBottomComponent(storyComponentTree);

		// Set up the divider
		for (Component component : storyPanel.getComponents()) {
			if (component instanceof BasicSplitPaneDivider) {
				final BasicSplitPaneDivider divider;

				divider = (BasicSplitPaneDivider) component;
				divider.setBackground(Color.WHITE);
				divider.setBorder(null);

				break;
			}
		}

		return storyPanel;
	}

	/**
	 * Panel intended to be used as a closeable tab for a JTabbedPane.
	 * 
	 * This code originally came from an Oracle tutorial, although there have
	 * been modifications. The following copyright notice is still required.
	 * 
	 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights
	 * reserved.
	 * 
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are
	 * met:
	 * 
	 * - Redistributions of source code must retain the above copyright notice,
	 * this list of conditions and the following disclaimer.
	 * 
	 * - Redistributions in binary form must reproduce the above copyright
	 * notice, this list of conditions and the following disclaimer in the
	 * documentation and/or other materials provided with the distribution.
	 * 
	 * - Neither the name of Oracle or the names of its contributors may be used
	 * to endorse or promote products derived from this software without
	 * specific prior written permission.
	 * 
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
	 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
	 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
	 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
	 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
	 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
	 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 * 
	 * @author remiller
	 * @author mfchurch
	 */
	private class CloseableModelTab extends JPanel {
		/**
		 * Builds a new Closeable Tab that will draw title information from the
		 * given parent and will display the given icon.
		 * 
		 * @param parent
		 *            the JTabbedPane to be added to.
		 * @param icon
		 *            The icon to display in the tab. Passing <code>null</code>
		 *            will show no icon.
		 */
		private CloseableModelTab(final JTabbedPane parent,
				final JComponent component, final SEModel model, Icon icon) {
			// unset the annoying gaps that come with default FlowLayout
			super(new FlowLayout(FlowLayout.LEFT, 0, 0));

			if (parent == null)
				throw new NullPointerException("TabbedPane is null");

			final JLabel iconLabel;
			final TabButton closeButton;
			final JLabel label;

			this.setOpaque(false);

			// make JLabel read titles from JTabbedPane
			label = new JLabel() {
				public String getText() {
					int i = parent.indexOfTabComponent(CloseableModelTab.this);

					if (i == -1)
						return null;

					return parent.getTitleAt(i);
				}
			};

			if (icon != null) {
				iconLabel = new JLabel(" ");
				iconLabel.setIcon(icon);
				this.add(iconLabel);
			}

			this.add(label);

			// add more space between the label and the button
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

			closeButton = new TabButton(new CloseModelAction(model));
			closeButton.setHideActionText(true);
			this.add(closeButton);

			// add more space to the top of the component
			this.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		}

		/**
		 * Simple tab-closing button.
		 * 
		 * Constructed because JTabbedPanes don't come with closing buttons at
		 * all by default. Figures.
		 */
		private class TabButton extends JButton {

			private TabButton(Action action) {
				super(action);

				int size = 17;
				this.setPreferredSize(new Dimension(size, size));
				this.setToolTipText("close this tab");
				// Make the button looks the same for all Laf's
				this.setUI(new BasicButtonUI());
				// Make it transparent
				this.setContentAreaFilled(false);
				// No need to be focusable
				this.setFocusable(false);
				this.setBorder(BorderFactory.createEtchedBorder());
				this.setBorderPainted(false);
				// Making nice rollover effect
				// we use the same listener for all buttons
				this.addMouseListener(tabButtonMouseListener);
				this.setRolloverEnabled(true);
			}

			// we don't want to update UI for this button
			@Override
			public void updateUI() {
			}

			// paint the cross
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				// shift the image for pressed buttons
				if (getModel().isPressed()) {
					g2.translate(1, 1);
				}
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLACK);
				if (getModel().isRollover()) {
					g2.setColor(Color.LIGHT_GRAY);
				}
				int delta = 6;
				g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
						- delta - 1);
				g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
						- delta - 1);
				g2.dispose();
			}
		}
	}

	/**
	 * Mouse listener that just enables and disables the border on its target
	 * button on mouse enter/exit.
	 */
	private static final MouseListener tabButtonMouseListener = new MouseAdapter() {

		@Override
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};
}
