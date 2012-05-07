package scriptease.gui.storycomponentbuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

public class StoryComponentMultiSelector extends Observable implements ActionListener{
	private String TYPES_LABELS;
	private MultiSelectorContext selectorContext;
	private JButton rootTypeLabelMenu;
	private ArrayList<String> data;
	
	public enum MultiSelectorContext{
		TYPES, LABELS, GAME_OBJECT
	}
		
	public StoryComponentMultiSelector(MultiSelectorContext context){
		selectorContext = context;
		rootTypeLabelMenu = new JButton(setLabel());
		rootTypeLabelMenu.addActionListener(this);
		data = new ArrayList<String>();
	}
	
	public void setData(ArrayList<String> typesOrLabels){
		data = typesOrLabels;
	}
	
	public MultiSelectorContext getSelectorContext(){
		return selectorContext;
	}
	
	public JButton getRootButton(){
		return rootTypeLabelMenu;
	}
	
	public ArrayList<String> getAllSelected(){
		return null;
	}
	
	private String setLabel(){
		if(selectorContext == MultiSelectorContext.TYPES)
			return "Types";
		return "Labels";
	}
	
	private void populateMenu(final JPopupMenu a){
		Translator activeTranslator = TranslatorManager.getInstance().getActiveTranslator();
	
		final List<JCheckBoxMenuItem> buttons;
		JCheckBoxMenuItem item;
		final MenuVisibilityHandler menuVisHandler;
		buttons = new ArrayList<JCheckBoxMenuItem>();
		menuVisHandler = new MenuVisibilityHandler(a);
		
		if (activeTranslator != null){
			GameTypeManager typeManager = activeTranslator.getGameTypeManager();
			for (String type : typeManager.getKeywords()) {
					item = new JCheckBoxMenuItem(type);
					if(data.contains(type))
						item.setSelected(true);
					item.setIcon(null);
					item.addActionListener(menuVisHandler);
					item.addActionListener(this);
					buttons.add(item);
			}
		}
		a.addSeparator();
		for (JCheckBoxMenuItem newItem : buttons){
			a.add(newItem);
		}
	}
	
	private JPopupMenu popMeUp(){
		final JPopupMenu a = new JPopupMenu(TYPES_LABELS);
		populateMenu(a);
		return a;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton)
			popMeUp().show((JComponent)e.getSource(), ((JComponent)e.getSource()).getWidth(), 0);
		
		if(e.getSource() instanceof JCheckBoxMenuItem){
			if(((JCheckBoxMenuItem)e.getSource()).isSelected())
				data.add(((JCheckBoxMenuItem)e.getSource()).getText());
			else
				data.remove(((JCheckBoxMenuItem)e.getSource()).getText());
			setChanged();
			notifyObservers(data);
		}
	}

	private final class MenuVisibilityHandler implements ActionListener {
		private final JPopupMenu menu;
		
		private MenuVisibilityHandler(JPopupMenu menu) {
			this.menu = menu;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.menu.setVisible(true);
		}
	}
	
}
