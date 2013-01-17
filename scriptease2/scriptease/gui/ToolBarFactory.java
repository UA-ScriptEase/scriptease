package scriptease.gui;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.gui.action.graphs.ConnectModeAction;
import scriptease.gui.action.graphs.DeleteModeAction;
import scriptease.gui.action.graphs.DisconnectModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.InsertModeAction;
import scriptease.gui.action.graphs.SelectModeAction;
import scriptease.model.PatternModelManager;

/**
 * ToolBarFactory is responsible for creating all JToolBars.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {
	private static ToolBarFactory instance = new ToolBarFactory();

	/**
	 * Returns the sole instance of ToolBarFactory.
	 * 
	 * @return
	 */
	public static ToolBarFactory getInstance() {
		return ToolBarFactory.instance;
	}

	/**
	 * Builds a ToolBar to edit graphs with. Includes buttons for selecting
	 * nodes, adding and deleting nodes, and adding and deleting paths. The
	 * ToolBar buttons only set the mode; the graph itself contains the specific
	 * actions that should happen.
	 * 
	 * @return
	 */
	public JToolBar buildGraphEditorToolBar() {
		final JToolBar graphEditorToolBar;
		final ButtonGroup graphEditorButtonGroup;
		final ArrayList<JToggleButton> buttonList;

		final JToggleButton selectNodeButton;
		final JToggleButton insertNodeButton;
		final JToggleButton deleteNodeButton;
		final JToggleButton connectNodeButton;
		final JToggleButton disconnectNodeButton;

		final Runnable selectButtonRunnable;

		graphEditorToolBar = new JToolBar();
		graphEditorButtonGroup = new ButtonGroup();
		buttonList = new ArrayList<JToggleButton>();

		selectNodeButton = new JToggleButton(SelectModeAction.getInstance());
		insertNodeButton = new JToggleButton(InsertModeAction.getInstance());
		deleteNodeButton = new JToggleButton(DeleteModeAction.getInstance());
		connectNodeButton = new JToggleButton(ConnectModeAction.getInstance());
		disconnectNodeButton = new JToggleButton(
				DisconnectModeAction.getInstance());

		selectButtonRunnable = new Runnable() {
			@Override
			public void run() {

				switch (GraphToolBarModeAction.getMode()) {

				case SELECT:
					graphEditorButtonGroup.setSelected(
							selectNodeButton.getModel(), true);
					break;
				case DELETE:
					graphEditorButtonGroup.setSelected(
							deleteNodeButton.getModel(), true);
					break;
				case INSERT:
					graphEditorButtonGroup.setSelected(
							insertNodeButton.getModel(), true);
					break;
				case CONNECT:
					graphEditorButtonGroup.setSelected(
							connectNodeButton.getModel(), true);
					break;
				case DISCONNECT:
					graphEditorButtonGroup.setSelected(
							disconnectNodeButton.getModel(), true);
					break;
				}
			}
		};

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.PAGE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);
		graphEditorToolBar.setBackground(Color.WHITE);

		buttonList.add(selectNodeButton);
		buttonList.add(insertNodeButton);
		buttonList.add(deleteNodeButton);
		buttonList.add(connectNodeButton);
		buttonList.add(disconnectNodeButton);

		for (JToggleButton toolBarButton : buttonList) {
			toolBarButton.setHideActionText(true);
			toolBarButton.setFocusable(false);
			graphEditorButtonGroup.add(toolBarButton);
			graphEditorToolBar.add(toolBarButton);
		}

		PatternModelManager.getInstance().addPatternModelObserver(
				graphEditorToolBar, new PatternModelObserver() {
					@Override
					public void modelChanged(PatternModelEvent event) {
						if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED)
							selectButtonRunnable.run();
					}
				});

		selectButtonRunnable.run();

		return graphEditorToolBar;
	}
}
