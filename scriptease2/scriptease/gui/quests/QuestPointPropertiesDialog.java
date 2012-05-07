package scriptease.gui.quests;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.gui.internationalization.Il8nResources;

/**
 * A dialog for setting the properties of a QuestPoint.
 * @author graves
 */
@SuppressWarnings("serial")
public class QuestPointPropertiesDialog extends JDialog {

	public QuestPointPropertiesDialog(final Frame owner, final QuestPointNode questPointNode){
		super(owner, Il8nResources.getString("Properties"), true);
		final QuestPoint questPoint = questPointNode.getQuestPoint();
		
		this.getContentPane().setLayout(new GridLayout(0,1));
		
		// Create the general properties panel.
		JPanel propertiesPanel = new JPanel();
		propertiesPanel.setLayout(new GridLayout(0,1));
		this.add(propertiesPanel);
		
		// Create the name textfield.
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		namePanel.add(new JLabel(Il8nResources.getString("Name")));
		final JTextField nameTextField = new JTextField(questPoint.getDisplayText(), 10);
		namePanel.add(nameTextField);
		propertiesPanel.add(namePanel);
		
		// Create the Committing checkbox.
		JPanel committingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		committingPanel.add(new JLabel(Il8nResources.getString("Committing")));
		final JCheckBox committingCheckBox = new JCheckBox();
		committingCheckBox.setSelected(questPoint.getCommitting());
		committingPanel.add(committingCheckBox);
		propertiesPanel.add(committingPanel);
		
		// Create the FanIn spinner.
		JPanel fanInPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		fanInPanel.add(new JLabel(Il8nResources.getString("Fan_In")));
		int maxFanIn = questPointNode.getParents().size();
		maxFanIn = maxFanIn>1 ? maxFanIn : 1;
		final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(questPoint.getFanIn(), new Integer(1), new Integer(maxFanIn), new Integer(1));
		fanInPanel.add(new JSpinner(fanInSpinnerModel));
		propertiesPanel.add(fanInPanel);
		
		// Build the Ok and Cancel buttons.
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton(Il8nResources.getString("Okay"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Set the QuestPoint properties.
				questPoint.setDisplayText(nameTextField.getText());
				questPoint.setCommiting(committingCheckBox.isSelected());
				questPoint.setFanIn((Integer) fanInSpinnerModel.getValue());
				
				questPoint.notifyObservers(new StoryComponentEvent(questPoint, StoryComponentChangeEnum.CHANGE_TEXT_NAME));
				
				// Close the dialog.
				QuestPointPropertiesDialog.this.setVisible(false);
				QuestPointPropertiesDialog.this.dispose();
			}
		});
		JButton cancelButton = new JButton(Il8nResources.getString("Cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Close the dialog.
				QuestPointPropertiesDialog.this.setVisible(false);
				QuestPointPropertiesDialog.this.dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		propertiesPanel.add(buttonPanel);
		this.add(propertiesPanel);
	}
	
}
