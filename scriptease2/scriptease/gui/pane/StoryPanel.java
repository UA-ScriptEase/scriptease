package scriptease.gui.pane;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import scriptease.gui.PanelFactory;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelSetting;
import scriptease.model.StoryModel;

/**
 * Main panel for editing Stories, under the toolbar. Provides a tree view of
 * the StoryComponent model and a graph of quest nodes.
 * 
 * @author remiller
 * @author mfchurch
 */
@SuppressWarnings("serial")
public class StoryPanel extends JPanel {


		
	/**
	 * Sets the tree to the QuestPoint passed.
	 * 
	 * @param The
	 *            QuestPoint to set the tree to.
	 */
	/*public void setTree(QuestPoint questPoint) {

		this.storyComponentTree.setRoot(questPoint);

	}*/
/* TODO There has to be another way to do this
	*//**
	 * Gets the current Story tree;
	 * 
	 * @return the current tree .
	 *//*
	public JScrollPane getTree() {
		return this.storyComponentTree;
	}
*/
/*	*/
	/**
	 * Updates the title bar to use the text from the model this frame
	 * represents.
	 * 
	 * @param model
	 *//*
	 *
	 *TODO This could go into SEFrame.. for now
	public String getTitle() {
		final StoryModel model = this.model;
		final String title;
		
		String modelTitle = model.getTitle();
		if (modelTitle == null || modelTitle.equals(""))
			modelTitle = "<Untitled>";

		title = modelTitle + "(" + model.getModule().getLocation().getName()
				+ ")";

		return title;
	}*/

	
/*	public StoryModel getModel() {
		return this.model;
	}*/

	/**
	 * Determines if this StoryFrame represents the given model.
	 * 
	 * @param model
	 *            The model to test against for representation.
	 * @return True if it represents the model, false otherwise.
	 */
/*	public boolean represents(StoryModel model) {
		return this.model == model;
	}
*/
}
