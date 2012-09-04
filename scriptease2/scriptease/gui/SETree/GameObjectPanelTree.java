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
import scriptease.model.StoryModel;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;
import scriptease.translator.io.model.GameObject;

//TODO This class looks awful and needs a heavy refactoring. -kschenk
@SuppressWarnings("serial")
public class GameObjectPanelTree extends JPanel implements Observer {
	private GameObjectTree treeModel;
	private ArrayList<GameObjectPanel> gameObjectPanel;
	private ArrayList<GameObjectLabel> gameObjectLabels;

	private final int CONSTANT_OFFSET = 10;

	public GameObjectPanelTree(StoryModel storyModel) {
		super();
		this.treeModel = new GameObjectTree(storyModel);
		this.gameObjectPanel = new ArrayList<GameObjectPanel>();
		this.gameObjectLabels = new ArrayList<GameObjectPanelTree.GameObjectLabel>();
		expandTree();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setVisible(true);
	}

	public GameObjectTree getTreeModel() {
		return this.treeModel;
	}

	private void addChildrenRoots(GameConversationNode parent) {
		if (parent.isTerminal() == true) {
			return;
		}

		Collection<Object> getChildren = this.treeModel.getTree().getSuccessors(
				parent);

		if (getChildren.size() == 0) {
			return;
		}

		for (Object a : getChildren) {
			GameObjectPanelConversation newObjPanel = new GameObjectPanelConversation(
					(GameConversationNode) a, this.CONSTANT_OFFSET * 2);
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
		ArrayList<Object> firstRowExpansion = (ArrayList<Object>) this.treeModel
				.getTree().getSuccessors(this.treeModel.getTree().getHead());
		int size = firstRowExpansion.size();
		for (int i = 0; i < size; i++) {
			if (firstRowExpansion.get(i) instanceof String) {
				GameObjectLabel label = new GameObjectLabel(firstRowExpansion
						.get(i).toString());
				label.addObserver(this);
				this.add(label.getLabelPanel());
				this.gameObjectLabels.add(label);

				if (!firstRowExpansion.get(i).equals("dialogue")) {
					Collection<Object> categoryExpansion = this.treeModel.getTree()
							.getSuccessors(firstRowExpansion.get(i));
					for (Object getGameObject : categoryExpansion) {
						GameObjectPanel newObjPanel = new GameObjectPanel(
								(GameObject) getGameObject, this.CONSTANT_OFFSET);
						this.addGameObjectPanelToTree(newObjPanel);
					}
				}

				// The dialogue adding code goes here
				else if (firstRowExpansion.get(i).equals("dialogue")) {
					Collection<Object> diagExpansion = this.treeModel.getTree()
							.getSuccessors(firstRowExpansion.get(i));
					for (Object dialogueExpansionNode : diagExpansion) {
						GameObjectPanel newObjPanel = new GameObjectPanel(
								(GameConversation) dialogueExpansionNode,
								this.CONSTANT_OFFSET);
						this.gameObjectPanel.add(newObjPanel);
						this.add(newObjPanel.getGameObjectPane());

						Collection<Object> convExpansion = this.treeModel.getTree()
								.getSuccessors(dialogueExpansionNode);
						for (Object gameConversation : convExpansion) {
							GameObjectPanelConversation newObjPanel2 = new GameObjectPanelConversation(
									(GameConversationNode) gameConversation,
									this.CONSTANT_OFFSET * 2);
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
		int size = this.gameObjectLabels.size();
		for (int i = 0; i < size; i++) {
			Collection<Object> srExp = this.treeModel.getTree().getSuccessors(
					this.gameObjectLabels.get(i).getLabel());
			if (this.gameObjectLabels.get(i).getVisbile()) {
				this.add(this.gameObjectLabels.get(i).getLabelPanel());
				if (!this.gameObjectLabels.get(i).collapsed) {
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
									this.CONSTANT_OFFSET);
							this.gameObjectPanel.add(newObjPanel);
							this.add(newObjPanel.getGameObjectPane());

							Collection<Object> convExpansion = this.treeModel
									.getTree().getSuccessors(z.get(j));
							ArrayList<Object> bees3 = (ArrayList<Object>) convExpansion;
							
							for (int zl = 0; zl < convExpansion.size(); zl++) {
								GameObjectPanel newObjPanel2 = new GameObjectPanel(
										(GameConversationNode) bees3.get(zl),
										this.CONSTANT_OFFSET * 2);
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
	}

	public ArrayList<String> getStringTypes() {
		ArrayList<String> returnMe = new ArrayList<String>();
		for (int i = 0; i < this.gameObjectLabels.size(); i++) {
			returnMe.add(this.gameObjectLabels.get(i).labelName);
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
			this.visible = true;
			this.collapsed = false;
			this.labelPanel = new JPanel();
			this.labelName = label;

			this.labelPanel.addMouseListener(this);
			this.labelPanel.setLayout(new BoxLayout(this.labelPanel, BoxLayout.X_AXIS));
			this.labelPanel.setBackground(Color.WHITE);

			reDrawLabel();

		}

		// YOUR SPELLING IS BAD AND YOU SHOULD FEEL BAD
		public boolean getVisbile() {
			return this.visible;
		}

		public String getLabel() {
			return this.labelName;
		}

		public JPanel getLabelPanel() {
			return this.labelPanel;
		}

		private String convertToLabelViewFormat() {
			String labelView = this.labelName.substring(0, 1).toUpperCase();
			labelView += this.labelName.substring(1);
			return labelView;
		}

		private void reDrawLabel() {
			this.labelPanel.removeAll();

			if (this.collapsed)
				this.labelPanel.add(new JLabel(convertToLabelViewFormat(),
						this.expandIcon, JLabel.CENTER));
			else
				this.labelPanel.add(new JLabel(convertToLabelViewFormat(),
						this.collapseIcon, JLabel.CENTER));
			this.labelPanel.add(Box.createHorizontalGlue());
			this.labelPanel.revalidate();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			this.collapsed = !this.collapsed;
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