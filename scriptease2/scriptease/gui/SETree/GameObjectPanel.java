package scriptease.gui.SETree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.io.model.GameConstant;
import scriptease.util.StringOp;

//View class for the specific game objects, and visual representation of the Game Object Tree

public class GameObjectPanel extends Observable implements MouseMotionListener, MouseListener {
	public static final Color UNSELECTED_COLOUR = Color.WHITE;
	public static final Color SELECTED_COLOUR = new Color(220, 220, 220);
	public static final MatteBorder SELECTED_BORDER = BorderFactory
			.createMatteBorder(1, 1, 1, 1, Color.black);
	public static final CompoundBorder UNSELECTED_BORDER = BorderFactory
			.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 1, 0, 0, Color.gray),
					BorderFactory.createEmptyBorder(1, 0, 1, 1));
	
	protected int HORIZONTAL_STRUT;
	protected BindingWidget gameObjectBindingWidget;
	protected JPanel backgroundPanel;
	protected JPanel gameObjectPanel2;
	protected JPanel gameObjectPanel3;
	protected GameConstant gameObject;
	

	protected String shortViewText;
	protected String regularText;

	//but what do you really do?
	private boolean selected;
	
	public GameObjectPanel(GameConstant gameObject, int horStrut){
		regularText = gameObject.getName();
		
		if(StringOp.wordCount(regularText) > 5)
			shortViewText = createShortHandViewofText();
		else
			shortViewText = regularText;
		
		
		HORIZONTAL_STRUT = horStrut;
		
		this.gameObject = gameObject;
		
		backgroundPanel = new JPanel();
		backgroundPanel.setOpaque(true);
		
		selected = false;
		
		this.backgroundPanel.addMouseListener(this);
		this.backgroundPanel.addMouseMotionListener(this);
		
		this.backgroundPanel.setBorder(UNSELECTED_BORDER);
		this.backgroundPanel.setBackground(UNSELECTED_COLOUR);
		
		this.backgroundPanel.setLayout(new BoxLayout(this.backgroundPanel, BoxLayout.X_AXIS));
		this.backgroundPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		
		buildGameObjectPane();
		
	}
	
	public int getHorStrut(){
		return HORIZONTAL_STRUT;
	}
	
	public GameConstant getGameObject(){
		return gameObject;
	}
	
	//private void buildGameObjectPane(){
	protected void buildGameObjectPane(){	
		gameObjectBindingWidget = new BindingWidget(
				new KnowItBindingConstant(gameObject));
		//String name = gameObject.getName();
		gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(shortViewText,
				Color.WHITE));
		
		gameObjectBindingWidget.setBorder(BorderFactory.createEmptyBorder(
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE));
		
		gameObjectBindingWidget.addMouseMotionListener(this);
		gameObjectBindingWidget.addMouseListener(this);
		
		this.backgroundPanel.add(gameObjectBindingWidget);
		this.backgroundPanel.add(Box.createHorizontalGlue());
		
		gameObjectPanel2 = new JPanel();
		gameObjectPanel2.setBackground(Color.WHITE);
		BoxLayout layout = new BoxLayout(gameObjectPanel2, BoxLayout.X_AXIS);
		gameObjectPanel2.setLayout(layout);
		gameObjectPanel2.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
				
		gameObjectPanel2.add(backgroundPanel);
		
		gameObjectPanel3 = new JPanel();
		gameObjectPanel3.setBackground(Color.WHITE);
		
		BoxLayout layout2 = new BoxLayout(gameObjectPanel3, BoxLayout.Y_AXIS);
		gameObjectPanel3.setLayout(layout2);
		gameObjectPanel3.add(Box.createVerticalStrut(5));
		gameObjectPanel3.add(gameObjectPanel2);
	}
	
	public void toggleSelectionStatus(boolean selected){
		this.selected = selected;
		if(selected){
			this.backgroundPanel.setBorder(SELECTED_BORDER);
			this.backgroundPanel.setBackground(SELECTED_COLOUR);
		}
		else{
			this.backgroundPanel.setBorder(UNSELECTED_BORDER);
			this.backgroundPanel.setBackground(UNSELECTED_COLOUR);
		}
	}
	
	private String createShortHandViewofText(){
		String truncatedVersion = "";
		int firstSpace = 0;
		int secondSpace =0;
		
		firstSpace = this.regularText.indexOf(" ");
		secondSpace = this.regularText.indexOf(" ", firstSpace+1);
			
		int lastSpace = this.regularText.lastIndexOf(" ");
		int penultimateSpace = this.regularText.substring(0, lastSpace-1).lastIndexOf(" ");
		
		truncatedVersion = regularText.substring(0, secondSpace + 1) + " ... " + regularText.substring(penultimateSpace, regularText.length());
		
		return truncatedVersion;
	}
	
	public JComponent getGameObjectPane(){
		return this.gameObjectPanel3;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		makeObjectSelected();
		e.consume();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		setChanged();
		notifyObservers();
		//add this to the list
		e.consume();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//should be overridden
		e.consume();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		makeObjectUnselected();
		e.consume();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		makeObjectSelected();
		e.consume();
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		makeObjectUnselected();
		e.consume();
	}
	
	protected void makeObjectSelected(){
		this.backgroundPanel.setBackground(SELECTED_COLOUR);
		this.backgroundPanel.revalidate();
	}
	
	protected void makeObjectUnselected(){
		this.backgroundPanel.setBackground(UNSELECTED_COLOUR);
		this.backgroundPanel.revalidate();
	}
	
	
	
}
