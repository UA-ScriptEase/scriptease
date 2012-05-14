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
 * 
 * @author graves
 * @author kschenk
 */
@SuppressWarnings("serial")
public class QuestPointPropertiesManager {
	
/*	private static final QuestPointPropertiesManager instance = new QuestPointPropertiesManager();
	
	private QuestPointPropertiesManager() {
		QuestPoint questPoint = questPointNode.getQuestPoint();

//		final JTextField nameTextField = new JTextField(
//				questPoint.getDisplayText(), 10);
//		namePanel.add(nameTextField);

		// Create the FanIn spinner.
		
		int maxFanIn = questPointNode.getParents().size();
		

		// Build the Ok and Cancel buttons.
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton(Il8nResources.getString("Okay"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			
				questPoint.setDisplayText(nameTextField.getText());

				questPoint.notifyObservers(new StoryComponentEvent(questPoint,
						StoryComponentChangeEnum.CHANGE_TEXT_NAME));
			}
		});
	}*/
}
