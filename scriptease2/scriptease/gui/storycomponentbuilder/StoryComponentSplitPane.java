package scriptease.gui.storycomponentbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import scriptease.gui.storycomponentbuilder.StoryComponentDescriptorTemplate.ComponentContext;
import scriptease.gui.storycomponentbuilder.propertypanel.ExpansionButtonSCB;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;

@SuppressWarnings("serial")
public class StoryComponentSplitPane extends JPanel implements ActionListener,
		TreeSelectionListener {
	private final String LEFT_ARROW = "<";
	private final String RIGHT_ARROW = ">";
	private final String PAGE = "Page";

	private ArrayList<JComponent> panelStack = new ArrayList<JComponent>();
	private JSplitPane panelStackPane;
	private JLabel pageLabel;
	boolean currentBaseEditable = false;

	public StoryComponentSplitPane() {
		initSplitPane();

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		add(createHorizShiftButtonPane());

		add(panelStackPane);

	}

	private void initSplitPane() {
		JLabel instruc = new JLabel(
				"Use the file to select new story component");
		JPanel blah = new JPanel();
		blah.add(instruc);
		panelStackPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, blah, blah);
		panelStackPane.setVisible(true);
	}

	private JPanel createHorizShiftButtonPane() {
		JPanel s = new JPanel();
		s.setLayout(new BoxLayout(s, BoxLayout.X_AXIS));

		JButton left = new JButton(LEFT_ARROW);
		JButton right = new JButton(RIGHT_ARROW);
		left.addActionListener(this);
		right.addActionListener(this);

		pageLabel = new JLabel("");

		s.add(left);
		s.add(Box.createHorizontalGlue());
		s.add(pageLabel);
		s.add(Box.createHorizontalGlue());
		s.add(right);

		return s;
	}

	private void setPageLabel() {
		int page = panelStack.indexOf(panelStackPane.getLeftComponent()) / 2 + 1;
		pageLabel.setText(PAGE + ": " + page);
		pageLabel.repaint();
	}

	public void addPanelToStack(JComponent addee) {
		panelStack.add(addee);
	}

	private boolean wipeCheck() {
		if (panelStack.size() > 0) {
			int n = JOptionPane
					.showConfirmDialog(
							this,
							"Warning, loading new pane will wipe the changes made to this current component, continue?",
							"In darkest day, in blackest night...",
							JOptionPane.YES_NO_OPTION);
			if (!(n == 0))
				return false;

			else {
				panelStack.removeAll(panelStack);
				panelStackPane.setLeftComponent(null);
				panelStackPane.setRightComponent(null);
				return true;
			}

		} else
			return true;
	}

	public void setInitialPane(StoryComponentDescriptorTemplate addMe) {
		if (!wipeCheck())
			return;
		addPanelToStack(addMe);
		panelStackPane.setLeftComponent(addMe);
		panelStackPane.revalidate();
		panelStackPane.repaint();

		pageLabel.setText("Page: 1");
		pageLabel.repaint();

	}

	private void updateSplitPane(JComponent left, JComponent right) {
		panelStackPane.setLeftComponent(left);
		panelStackPane.setRightComponent(right);
		panelStackPane.setDividerLocation(panelStackPane.getWidth() / 2);
		panelStackPane.revalidate();
		panelStackPane.repaint();
	}

	private void shiftLeft() {
		if (getCurrentIndexPane() > 1 && !(getCurrentIndexPane() == -1)) {
			updateSplitPane(panelStack.get(getCurrentIndexPane() - 2),
					panelStack.get(getCurrentIndexPane() - 1));
			setPageLabel();
		}
	}

	private void shiftRight() {
		if (getCurrentIndexPane() < panelStack.size() - 2
				&& !(getCurrentIndexPane() == -1)) {
			if (getCurrentIndexPane() + 3 < panelStack.size())
				updateSplitPane(panelStack.get(getCurrentIndexPane() + 2),
						panelStack.get(getCurrentIndexPane() + 3));
			else
				updateSplitPane(panelStack.get(getCurrentIndexPane() + 2), null);

			setPageLabel();
		}

	}

	private int getCurrentIndexPane() {
		// return panelStack.indexOf(panelStackPane.getLeftComponent());
		return panelStack.size() - 1;
	}

	public void removePanelFromStack(JComponent removee) {
		if (panelStack.size() == 1)
			updateSplitPane(null, null);
		else if (panelStack.size() % 2 == 0)
			updateSplitPane(panelStack.get(getCurrentIndexPane() - 1), null);
		else
			shiftLeft();

		panelStack.remove(removee);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(LEFT_ARROW))
			shiftLeft();
		else if (e.getActionCommand().equals(RIGHT_ARROW))
			shiftRight();
		else if (e.getSource() instanceof ExpansionButtonSCB)
			getPanelFromExpansion((ExpansionButtonSCB) e.getSource());
		else if (e.getActionCommand().equals("Ok"))
			okAction((JButton) e.getSource());
		else if (e.getActionCommand().equals("Save"))
			saveAction((JButton) e.getSource());
		else if (e.getActionCommand().equals("Cancel"))
			cancelAction((JButton) e.getSource());
		else if (e.getActionCommand().equals("Delete")) {
			deleteAction((JButton) e.getSource());
		}
	}

	private void deleteAction(JButton button) {

	}

	private void okAction(JButton button) {
		if (checkIfOkToActOnPanel(getPanelFromSaveOKCancel(button))) {
			for (int i = 0; i < ((ScriptIt)((StoryComponentDescriptorTemplate) panelStack
					.get(getCurrentIndexPane() - 1)).getStoryComponent())
					.getParameters().size(); i++) {
				if (   ((ArrayList<KnowIt>)(((ScriptIt)((StoryComponentDescriptorTemplate) panelStack
						.get(getCurrentIndexPane() - 1)).getStoryComponent())
						.getParameters())).get(i).getDisplayText() 
						== 
						((StoryComponentDescriptorTemplate) panelStack
						.get(getCurrentIndexPane())).getStoryComponent()
						.getDisplayText()
						) {

					((ScriptIt)((StoryComponentDescriptorTemplate) panelStack
							.get(getCurrentIndexPane() - 1))
							.getStoryComponent()).getParameters().remove(i);

					((ArrayList<KnowIt>)(((ScriptIt) ((StoryComponentDescriptorTemplate) panelStack
							.get(getCurrentIndexPane() - 1))
							.getStoryComponent())
							.getParameters()))
							.add(i,
									(KnowIt) ((StoryComponentDescriptorTemplate) panelStack
											.get(getCurrentIndexPane()))
											.getStoryComponent());

					break;
				}
			}
			removePanelFromStack(panelStack.get(getCurrentIndexPane()));
			return;
		}
		generateWarningMessage(
				"Finish editing all the parameters and binding first",
				"In Darkest Day, In Blackest Night...");
	}

	private void saveAction(JButton button) {
		if (checkIfOkToActOnPanel(getPanelFromSaveOKCancel(button))) {
			if (saveToTranslator()) {
				removePanelFromStack(panelStack.get(getCurrentIndexPane()));
			} else
				generateWarningMessage("Unable to save",
						"In Darkest Day, In Blackest Night...");
			return;
		}
		generateWarningMessage(
				"Finish editing all the parameters and binding first",
				"In Darkest Day, In Blackest Night...");
		return;
	}

	private void cancelAction(JButton button) {
		if (checkIfOkToActOnPanel(getPanelFromSaveOKCancel(button))) {
			removePanelFromStack(getPanelFromSaveOKCancel(button));
			return;
		}
		generateWarningMessage(
				"Finish editing all the parameters and binding first",
				"In Darkest Day, In Blackest Night...");
		return;

	}

	private boolean checkIfOkToActOnPanel(JComponent panelToAct) {
		if (panelToAct == panelStack.get(getCurrentIndexPane()))
			return true;
		return false;
	}

	private JComponent getPanelFromSaveOKCancel(JButton eventSource) {
		for (int n = 0; n < panelStack.size(); n++) {
			if (eventSource == ((StoryComponentDescriptorTemplate) panelStack
					.get(n)).getCancel()
					|| eventSource == ((StoryComponentDescriptorTemplate) panelStack
							.get(n)).getSaveOK()

			)
				return panelStack.get(n);
		}
		return null;
	}

	private void getPanelFromExpansion(ExpansionButtonSCB eventSource) {
		for (int n = 0; n < panelStack.size(); n++) {
			for (int m = 0; m < getSizeOfParams(panelStack.get(n)); m++) {
				ExpansionButtonSCB a = ((StoryComponentDescriptorTemplate) panelStack
						.get(n)).getParamList().getBindings().get(m)
						.getExpansionBut();
				if (eventSource == a) {
					dealWithIt(
							(StoryComponentDescriptorTemplate) panelStack
									.get(n),
							m);
					return;
				}
			}
		}
	}

	private void generateWarningMessage(String a, String b) {
		JOptionPane.showMessageDialog(this, a, b, JOptionPane.ERROR_MESSAGE);
		return;
	}

	private void dealWithIt(StoryComponentDescriptorTemplate panelToDealWith,
			int index) {
	
		if (!(panelStack.indexOf(panelToDealWith) == getCurrentIndexPane())) {
			generateWarningMessage("This component is not in focus",
					"In darkest day, in blackest night...");
			return;
		}

		/*
		 * if(panelToDealWith.isIsEditingBinding()){ generateWarningMessage(
		 * "This component is not able to be expanded at this moment, another binding is already being edited"
		 * , "In darkest day, in blackest night..."); return; }
		 */

		if (panelToDealWith instanceof StoryComponentScriptItEditor) {
			ArrayList<KnowIt> listOfKnowIts = (ArrayList<KnowIt>) ((ScriptIt)panelToDealWith.getStoryComponent()).getParameters();/*.get(index);*/
			KnowIt addKnowForExtension = listOfKnowIts.get(index);
			StoryComponentKnowItEditor a = new StoryComponentKnowItEditor(
					addKnowForExtension, ComponentContext.EXTENSION);
			((StoryComponentDescriptorTemplate) a)
					.addSaveCancelOperationListener(this);
			((StoryComponentDescriptorTemplate) a)
					.setActionButtonForParameters(this);
			((StoryComponentDescriptorTemplate) a).checkIfUpdatable();

			addPanelToStack(a);
			panelToDealWith.setIsEditingBinding(true);
			updateSplitPane(panelStack.get(getCurrentIndexPane() - 1),
					panelStack.get(getCurrentIndexPane()));
		}

		else if (panelToDealWith instanceof StoryComponentKnowItEditor) {

		}
	}

	private int getSizeOfParams(JComponent a) {
		if (a instanceof StoryComponentDescriptorTemplate)
			return ((ScriptIt)((StoryComponentDescriptorTemplate) a).getStoryComponent()).getParameters().size();
		return 0;
	}

	private boolean saveToTranslator() {
		if (getCurrentIndexPane() == 0)
			return ((StoryComponentDescriptorTemplate) panelStack
					.get(getCurrentIndexPane())).saveComponent();
		return false;
	}

	// ALL TREE ALL THE TIME
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// TODO Auto-generated method stub
		if (!wipeCheck())
			return;

		if (((StoryComponentPanel) e.getSource()).getStoryComponent() instanceof ScriptIt) {
			StoryComponentScriptItEditor scriptItPanel = new StoryComponentScriptItEditor(
					(ScriptIt) ((StoryComponentPanel) e.getSource())
							.getStoryComponent(),
					ComponentContext.BASE);
			scriptItPanel.addSaveCancelOperationListener(this); 
			scriptItPanel.setActionButtonForParameters(this);
			scriptItPanel.checkIfUpdatable();
			setInitialPane(scriptItPanel);
		}

		if (((StoryComponentPanel) e.getSource()).getStoryComponent() instanceof KnowIt) {
			StoryComponentKnowItEditor knowItPanel = new StoryComponentKnowItEditor(
					(KnowIt) ((StoryComponentPanel) e.getSource())
							.getStoryComponent(),
					ComponentContext.BASE);
			knowItPanel.addSaveCancelOperationListener(this);
			setInitialPane(knowItPanel);
		}
	}
}
