package scriptease.gui;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.gui.action.graphs.ConnectModeAction;
import scriptease.gui.action.graphs.DeleteModeAction;
import scriptease.gui.action.graphs.DisconnectModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.InsertModeAction;
import scriptease.gui.action.graphs.SelectModeAction;

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
		final JToolBar graphEditorToolBar = new JToolBar();

		final ButtonGroup graphEditorButtonGroup = new ButtonGroup();

		final ArrayList<JToggleButton> buttonList = new ArrayList<JToggleButton>();

		final JToggleButton selectNodeButton = new JToggleButton(
				SelectModeAction.getInstance());

		final JToggleButton insertNodeButton = new JToggleButton(
				InsertModeAction.getInstance());

		final JToggleButton deleteNodeButton = new JToggleButton(
				DeleteModeAction.getInstance());

		final JToggleButton connectNodeButton = new JToggleButton(
				ConnectModeAction.getInstance());

		final JToggleButton disconnectNodeButton = new JToggleButton(
				DisconnectModeAction.getInstance());

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.LINE_AXIS));
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

		// TODO We may be able to switch this with a model change listener,
		// removing the need to know about the tabbed pane.
		final ChangeListener graphEditorListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
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

		PanelFactory.getInstance().getModelTabPane()
				.addChangeListener(graphEditorListener);

		return graphEditorToolBar;
	}
}
