package scriptease.gui.pane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.ResourceTreeAdapter;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.UndoManagerObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.action.file.CloseModelAction;
import scriptease.gui.libraryeditor.LibraryEditorPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.Resource;
import scriptease.util.BiHashMap;
import scriptease.util.StringOp;

/**
 * The model tab panel creates a new tab for each new model created. Using an
 * observer, the tabs are removed when the model is removed. This makes the
 * model tabs MVC. Theoretically, we could have multiple ModelTabPanels, but
 * this has only been tested with one so far.
 * 
 * ModelTabPanels are created from the {@link PanelFactory}.
 * 
 * @author ScriptEase Team
 * @author kschenk - major refactor to make it MVC and less buggy
 * 
 */
@SuppressWarnings("serial")
class SEModelTabbedPane extends JTabbedPane {
	// A mapping of models to components represented by the models
	private final BiHashMap<SEModel, Component> modelToComponent;

	/**
	 * Creates a new ModelTabPanel and initializes its change listener.
	 */
	protected SEModelTabbedPane() {
		this.modelToComponent = new BiHashMap<SEModel, Component>();

		// Register a change listener for the tabs
		this.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				final Component tab = SEModelTabbedPane.this
						.getSelectedComponent();
				final SEModel model = SEModelTabbedPane.this.modelToComponent
						.getKey(tab);

				if (tab != null && model != null) {
					SEModelManager.getInstance().activate(model);
				}
			}
		});

		SEModelManager.getInstance().addSEModelObserver(this,
				new SEModelObserver() {
					@Override
					public void modelChanged(SEModelEvent event) {
						final SEModelTabbedPane tabs = SEModelTabbedPane.this;
						final SEModel model = event.getPatternModel();

						if (event.getEventType() == SEModelEvent.Type.REMOVED) {
							tabs.removeTabForModel(model);
						} else if (event.getEventType() == SEModelEvent.Type.ACTIVATED) {
							// We need to delay this until the model is loaded
							// because it switches tabs to the new tab, which
							// fires the previously created change listener,
							// which causes a ConcurrentModificationException,
							// which kills the ScriptEase.
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									tabs.createTabForModel(model);
								}
							});
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
		SEModelTabbedPane.this.remove(this.modelToComponent.getValue(model));
		this.modelToComponent.removeKey(model);
	}

	/**
	 * Creates a tab for the given model if one does not already exist
	 * 
	 * @param model
	 */
	private void createTabForModel(SEModel model) {
		final boolean tabExistsForModel = SEModelTabbedPane.this.modelToComponent
				.containsKey(model);

		if (!tabExistsForModel) {
			model.process(new ModelAdapter() {
				@Override
				public void processLibraryModel(final LibraryModel model) {
					// Creates a Library Editor panel
					final JPanel panel;
					final JScrollPane scrollPane;
					final String savedTitle;

					panel = LibraryEditorPanelFactory.getInstance()
							.buildLibraryEditorPanel();
					scrollPane = new JScrollPane(panel);

					SEModelTabbedPane.this.modelToComponent.put(model,
							scrollPane);

					savedTitle = model.getTitle() + "[Editor]";

					scrollPane.getVerticalScrollBar().setUnitIncrement(
							ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

					SEModelTabbedPane.this.buildTab(savedTitle, model,
							scrollPane);
				}

				@Override
				public void processStoryModel(final StoryModel storyModel) {
					// Creates a story editor panel with a story graph
					final StoryPoint root;
					final JComponent panel;
					final String savedTitle;
					String modelTitle;

					root = storyModel.getRoot();
					panel = SEModelTabbedPane.this.buildStoryPanel(storyModel,
							root);

					modelTitle = storyModel.getTitle();
					if (modelTitle == null || modelTitle.equals(""))
						modelTitle = "<Untitled>";

					savedTitle = modelTitle + "("
							+ storyModel.getModule().getLocation().getName()
							+ ")";

					SEModelTabbedPane.this.buildTab(savedTitle, storyModel,
							panel);
				}
			});
		}
	}

	/**
	 * Sets up a new tab with the title, model, and contents. This is not
	 * dependent on type of model. Tabs have a {@link CloseableModelTab} and are
	 * set to the selected tab when this is called. They also update their name
	 * based on if there are any unsaved changes.
	 * 
	 * @param title
	 * @param model
	 * @param tabContents
	 */
	private void buildTab(final String title, final SEModel model,
			final JComponent tabContents) {
		final CloseableModelTab newTab;
		final Icon icon;

		if (model.getTranslator() != null)
			icon = model.getTranslator().getIcon();
		else
			icon = null;

		newTab = new CloseableModelTab(this, model, icon);

		this.addTab(title, icon, tabContents);

		this.setTabComponentAt(this.indexOfComponent(tabContents), newTab);

		this.setFocusable(false);

		UndoManager.getInstance().addUndoManagerObserver(model,
				new UndoManagerObserver() {
					@Override
					public void stackChanged() {
						final SEModelTabbedPane tabPanel = SEModelTabbedPane.this;
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

		this.setSelectedComponent(tabContents);
	}

	/**
	 * Builds a panel for a StoryModel. This panel includes an {@link SEGraph}
	 * and a {@link StoryComponentPanelTree}.
	 * 
	 * @param model
	 * @param start
	 * @return
	 */
	private JComponent buildStoryPanel(final StoryModel model,
			final StoryPoint start) {
		final String STORY_EDITOR = "Story Editor";
		final String DIALOGUE_EDITOR = "Dialogue Editor";
		final CardLayout layout;
		final JPanel topLevelPane;
		final JSplitPane storyPanel;
		final JToolBar graphToolBar;
		final DialogueEditorPanel dialogueEditor;
		final JButton backToStory;

		final SEGraph<StoryPoint> storyGraph;
		final StoryComponentPanelTree storyComponentTree;
		final StoryComponentObserver graphRedrawer;
		final JPanel storyGraphPanel;

		final JScrollPane storyGraphScrollPane;

		layout = new CardLayout();
		topLevelPane = new JPanel(layout);

		storyPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		storyGraph = SEGraphFactory.buildStoryGraph(start);
		graphToolBar = storyGraph.getToolBar();

		backToStory = new JButton(
				"<html><center>Back<br>to<br>Story</center></html>");

		dialogueEditor = new DialogueEditorPanel(model, backToStory);

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
		SEModelTabbedPane.this.modelToComponent.put(model, topLevelPane);

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

		storyGraphScrollPane.setBorder(BorderFactory.createEmptyBorder());
		storyGraphScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		storyGraphScrollPane.getHorizontalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

		storyGraphPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		graphToolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
				Color.LIGHT_GRAY));

		storyGraphPanel.add(graphToolBar, BorderLayout.WEST);

		storyGraphPanel.add(storyGraphScrollPane, BorderLayout.CENTER);

		storyComponentTree.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		// Set up the split pane
		storyPanel.setBorder(null);
		storyPanel.setOpaque(true);
		storyPanel.setTopComponent(storyGraphPanel);
		storyPanel.setBottomComponent(storyComponentTree);

		// Set the divider to a blank one
		for (Component component : storyPanel.getComponents()) {
			if (component instanceof BasicSplitPaneDivider) {
				final BasicSplitPaneDivider divider;

				divider = (BasicSplitPaneDivider) component;
				divider.setBackground(Color.WHITE);
				divider.setBorder(null);

				break;
			}
		}

		topLevelPane.setBorder(null);
		topLevelPane.setOpaque(true);
		topLevelPane.add(storyPanel, STORY_EDITOR);
		topLevelPane.add(dialogueEditor, DIALOGUE_EDITOR);

		backToStory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				layout.show(topLevelPane, STORY_EDITOR);
				dialogueEditor.setDialogueLine(null);
			}
		});

		ResourcePanel.getInstance().addObserver(topLevelPane,
				new ResourceTreeAdapter() {
					@Override
					public void resourceEditButtonClicked(Resource resource) {
						if (resource instanceof DialogueLine) {
							dialogueEditor
									.setDialogueLine((DialogueLine) resource);
							layout.show(topLevelPane, DIALOGUE_EDITOR);
						}
					}

					@Override
					public void resourceAddButtonClicked(String type) {
						final StoryModel story;
						final String dialogueType;

						story = SEModelManager.getInstance()
								.getActiveStoryModel();
						dialogueType = story.getModule().getDialogueType();

						if (StringOp.exists(dialogueType)
								&& type.equals(dialogueType))
							story.createAndAddDialogueRoot();
					}

					@Override
					public void resourceRemoveButtonClicked(Resource resource) {
						if (!(resource instanceof DialogueLine))
							return;

						final StoryModel story;

						story = SEModelManager.getInstance()
								.getActiveStoryModel();

						story.removeDialogueRoot((DialogueLine) resource);

						if (dialogueEditor.getDialogueLine() == resource) {
							layout.show(topLevelPane, STORY_EDITOR);
							dialogueEditor.setDialogueLine(null);
						}
					}
				});

		return topLevelPane;
	}

	/**
	 * Panel intended to be used as a close-able tab for a JTabbedPane.
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
				final SEModel model, Icon icon) {
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
