package scriptease.gui.storycomponentbuilder;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class StoryComponentMenu extends JMenuBar {
	private String NEW = "New";
	private String STARTIT = "Cause";
	private String DOIT = "Effect";
	private String KNOWIT = "Describer";
	private String SAVE = "Save";
	private String FILE = "File";
	
	public StoryComponentMenu(){
		super();
		addMenuItems();
	}
	
	private void addMenuItems(){
		//Add accelators
		JMenu fileMenu = new JMenu(FILE);
		JMenu newMenu = new JMenu(NEW);
		JMenu saveMenu = new JMenu(SAVE);
		
		JMenuItem newStartIt = new JMenuItem(STARTIT);
		JMenuItem newDoIt = new JMenuItem(DOIT);
		JMenuItem newKnowIt = new JMenuItem(KNOWIT);
		
		newMenu.add(newStartIt);
		newMenu.add(newDoIt);
		newMenu.add(newKnowIt);
		
		fileMenu.add(newMenu);
		fileMenu.add(saveMenu);
		
		add(fileMenu);
	}

}
