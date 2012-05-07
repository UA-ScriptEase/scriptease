package scriptease.gui.storycomponentbuilder;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;

@SuppressWarnings("serial")
public class StoryComponentFrame extends JFrame {
	private StoryComponentSplitPane stackedBuilder;
	private StoryComponentLibraryPanel compLibraryPane;

	private static StoryComponentFrame instance = new StoryComponentFrame();

	public static StoryComponentFrame getInstance() {
		return StoryComponentFrame.instance;
	}

	private StoryComponentFrame() {
		super("Story Component Builder");
		
		stackedBuilder = new StoryComponentSplitPane();
		compLibraryPane = new StoryComponentLibraryPanel();
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, compLibraryPane, stackedBuilder));
		compLibraryPane.getLibPane().getSCPTree().addTreeSelectionListener(stackedBuilder);
		setSize(new Dimension(1200, 600));
	}

	public StoryComponentSplitPane getStackedBuilder(){
		return stackedBuilder;
	}

	public StoryComponentPanelTree getLibraryTree() {
		return this.compLibraryPane.getLibPane().getSCPTree();
	}
}
