package scriptease.gui.SETree;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;
import scriptease.translator.io.model.GameObject;

//TODO This class looks awful and needs a heavy refactoring.
@SuppressWarnings("serial")
public class GameObjectPanelTree extends JPanel implements Observer {
	private GameObjectTree treeModel;
	private ArrayList<GameObjectPanel> gameObjectPanel;
	private ArrayList<GameObjectLabel> gameObjectLabels;

	private final int CONSTANT_OFFSET = 10;

	public GameObjectPanelTree() {
		super();
		treeModel = new GameObjectTree();
		gameObjectPanel = new ArrayList<GameObjectPanel>();
		gameObjectLabels = new ArrayList<GameObjectPanelTree.GameObjectLabel>();
		expandTree();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setVisible(true);
	}

	public GameObjectTree getTreeModel() {
		return treeModel;
	}

	private void addChildrenRoots(GameConversationNode parent) {
		if (parent.isTerminal() == true) {
			return;
		}

		Collection<Object> getChildren = treeModel.getTree().getSuccessors(
				parent);

		if (getChildren.size() == 0) {
			return;
		}

		for (Object a : getChildren) {
			GameObjectPanelConversation newObjPanel = new GameObjectPanelConversation(
					(GameConversationNode) a, CONSTANT_OFFSET * 2);
			this.gameObjectPanel.add(newObjPanel);
			this.add(newObjPanel.getGameObjectPane());
		}

		for (Object a : getChildren) {
			addChildrenRoots((GameConversationNode) a);
		}

	}

	private void addGameObjectPanelToTree(GameObjectPanel addObject) {
		this.gameObjectPanel.add(addObject);
		this.add(addObject.getGameObjectPane());
	}

	private void expandTree() {
		ArrayList<Object> firstRowExpansion = (ArrayList<Object>) treeModel
				.getTree().getSuccessors(treeModel.getTree().getHead());
		int size = firstRowExpansion.size();
		for (int i = 0; i < size; i++) {
			if (firstRowExpansion.get(i) instanceof String) {
				GameObjectLabel label = new GameObjectLabel(firstRowExpansion
						.get(i).toString());
				label.addObserver(this);
				this.add(label.getLabelPanel());
				gameObjectLabels.add(label);

				if (!firstRowExpansion.get(i).equals("dialogue")) {
					Collection<Object> categoryExpansion = treeModel.getTree()
							.getSuccessors(firstRowExpansion.get(i));
					for (Object getGameObject : categoryExpansion) {
						GameObjectPanel newObjPanel = new GameObjectPanel(
								(GameObject) getGameObject, CONSTANT_OFFSET);
						this.addGameObjectPanelToTree(newObjPanel);
					}
				}

				// The dialogue adding code goes here
				else if (firstRowExpansion.get(i).equals("dialogue")) {
					Collection<Object> diagExpansion = treeModel.getTree()
							.getSuccessors(firstRowExpansion.get(i));
					for (Object dialogueExpansionNode : diagExpansion) {
						GameObjectPanel newObjPanel = new GameObjectPanel(
								(GameConversation) dialogueExpansionNode,
								CONSTANT_OFFSET);
						this.gameObjectPanel.add(newObjPanel);
						this.add(newObjPanel.getGameObjectPane());

						Collection<Object> convExpansion = treeModel.getTree()
								.getSuccessors(dialogueExpansionNode);
						for (Object gameConversation : convExpansion) {
							GameObjectPanelConversation newObjPanel2 = new GameObjectPanelConversation(
									(GameConversationNode) gameConversation,
									CONSTANT_OFFSET * 2);
							this.gameObjectPanel.add(newObjPanel2);
							this.add(newObjPanel2.getGameObjectPane());
							addChildrenRoots((GameConversationNode) gameConversation);
						}
					}
				}
			}
		}
	}

	// TODO: Refactor, code is very similar to above method :(
	private void reDrawTree() {
		this.removeAll();
		int size = gameObjectLabels.size();
		for (int i = 0; i < size; i++) {
			Collection<Object> srExp = treeModel.getTree().getSuccessors(
					gameObjectLabels.get(i).getLabel());
			if (gameObjectLabels.get(i).getVisbile()) {
				this.add(gameObjectLabels.get(i).getLabelPanel());
				if (!gameObjectLabels.get(i).collapsed) {
					ArrayList<Object> z = (ArrayList<Object>) srExp;
					for (int j = 0; j < srExp.size(); j++) {
						if (z.get(j) instanceof GameObject) {
							GameObjectPanel newObjPanel = new GameObjectPanel(
									(GameObject) z.get(j), 10);
							this.gameObjectPanel.add(newObjPanel);
							this.add(newObjPanel.getGameObjectPane());
						}

						if (z.get(j) instanceof GameConversation) {
							GameObjectPanel newObjPanel = new GameObjectPanel(
									(GameConversation) z.get(j),
									CONSTANT_OFFSET);
							this.gameObjectPanel.add(newObjPanel);
							this.add(newObjPanel.getGameObjectPane());

							Collection<Object> convExpansion = treeModel
									.getTree().getSuccessors(z.get(j));
							ArrayList<Object> bees3 = (ArrayList<Object>) convExpansion;
							
							for (int zl = 0; zl < convExpansion.size(); zl++) {
								GameObjectPanel newObjPanel2 = new GameObjectPanel(
										(GameConversationNode) bees3.get(zl),
										CONSTANT_OFFSET * 2);
								this.gameObjectPanel.add(newObjPanel2);
								this.add(newObjPanel2.getGameObjectPane());
								addChildrenRoots((GameConversationNode) bees3
										.get(zl));
							}
						}
					}
				}
			}
		}
		this.revalidate();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof GameObjectLabel) {
			reDrawTree();
		}
		if (o instanceof GameObjectMultiSelector) {
			@SuppressWarnings("unchecked")
			ArrayList<String> data = (ArrayList<String>) arg;
			for (int i = 0; i < gameObjectLabels.size(); i++) {
				boolean a = false;
				for (int j = 0; j < data.size(); j++) {
					if (gameObjectLabels.get(i).labelName.equals(data.get(j)))
						a = true;
				}
				gameObjectLabels.get(i).setVisible(a);
			}
			reDrawTree();
		}
	}

	public ArrayList<String> getStringTypes() {
		ArrayList<String> returnMe = new ArrayList<String>();
		for (int i = 0; i < gameObjectLabels.size(); i++) {
			returnMe.add(gameObjectLabels.get(i).labelName);
		}
		return returnMe;
	}

	private class GameObjectLabel extends Observable implements MouseListener {
		private JPanel labelPanel;
		private boolean collapsed;
		private boolean visible;
		private Icon collapseIcon = ScriptEaseUI.COLLAPSE_ICON;
		private Icon expandIcon = ScriptEaseUI.EXPAND_ICON;
		private String labelName;

		public GameObjectLabel(String label) {
			visible = true;
			collapsed = false;
			labelPanel = new JPanel();
			labelName = label;

			labelPanel.addMouseListener(this);
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
			labelPanel.setBackground(Color.WHITE);

			reDrawLabel();

		}

		public boolean getVisbile() {
			return visible;
		}

		public void setVisible(boolean set) {
			visible = set;
		}

		public String getLabel() {
			return labelName;
		}

		public JPanel getLabelPanel() {
			return labelPanel;
		}

		private String convertToLabelViewFormat() {
			String labelView = labelName.substring(0, 1).toUpperCase();
			labelView += labelName.substring(1);
			return labelView;
		}

		private void reDrawLabel() {
			labelPanel.removeAll();

			if (collapsed)
				labelPanel.add(new JLabel(convertToLabelViewFormat(),
						expandIcon, JLabel.CENTER));
			else
				labelPanel.add(new JLabel(convertToLabelViewFormat(),
						collapseIcon, JLabel.CENTER));
			labelPanel.add(Box.createHorizontalGlue());
			labelPanel.revalidate();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			collapsed = !collapsed;
			reDrawLabel();
			setChanged();
			notifyObservers();
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
}