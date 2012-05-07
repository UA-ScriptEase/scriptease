package scriptease.gui.storycomponentbuilder;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class StoryComponentTextField extends Observable implements KeyListener, DocumentListener{
	private final char LESS_THAN = '<';
	private final char GREATER_THAN = '>';
	
	private JTextField textField;
	private ArrayList<String> bindNames = new ArrayList<String>();
	
	public StoryComponentTextField(boolean type){
		textField = new JTextField();
		textField.addKeyListener(this);
		textField.getDocument().addDocumentListener(this);
		textField.setMaximumSize(new Dimension(350,60));
		
	}
	
	//Copy, and deletion is problematic....FUCK ME, wtf is up wid this noise....
	//Y u so annyoing to fix, i should be getting you for free!
	
	public void setText(String text){
		textField.setText(text);
	}
	
	public JComponent getTextField(){
		return textField;
	}
	
	public String getNameComp(){
		return textField.getText();
	}
	
	private boolean editingNCake(int offset, int len) {
		if (offset == 0 && len == 0 || offset == len )
			return false;
		return beforeCheck(offset) && afterCheck(offset, len);
	}

	private boolean beforeCheck(int offset) {
		for (int i = offset; i >= 0; i--) {
			if (textField.getText().charAt(i) == LESS_THAN)
				return true;
			
			if(!(i==offset)){
				if (textField.getText().charAt(i) == GREATER_THAN)
					return false;
			}
		}
		return false;
	}

	private boolean afterCheck(int offset, int len) {
		for (int i = offset; i < len; i++) {
			if (textField.getText().charAt(i) == GREATER_THAN)
				return true;
			
			if (textField.getText().charAt(i) == LESS_THAN) 
				return false;
		}
		return false;
	}
	
	private void updateBindingNamesInTF(DocumentEvent e){
		if (editingNCake(e.getOffset(), textField.getText().length())) 
			genParamList(getIndexofBeginners());
		
		else{
			setChanged();
			notifyObservers(null);
		}
	}
	
	private Vector<Integer> getIndexofBeginners() {
		bindNames.removeAll(bindNames);
		Vector<Integer> indexOfBeginners = new Vector<Integer>();
		indexOfBeginners.removeAllElements();
		
		for (int gob = 0; gob < textField.getText().length(); gob++) {
			if (textField.getText().charAt(gob) == '<') 
				indexOfBeginners.add((Integer) gob);
		}
		return indexOfBeginners;
	}
	
	private void genParamList(Vector<Integer> indi) {
		for (int n = 0; n < indi.size(); n++) {
			String nName = "";
			for (int i = indi.elementAt(n) + 1; i < textField.getText().length(); i++) {
				if (textField.getText().charAt(i) == '>') {
					bindNames.add(nName);
					break;
				}
				nName += textField.getText().charAt(i);
			}
		}
		setChanged();
		notifyObservers(bindNames);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateBindingNamesInTF(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateBindingNamesInTF(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateBindingNamesInTF(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyChar() == LESS_THAN) {
			Vector<Integer> indexOfBeginners = getIndexofBeginners();
			String bob = textField.getText();
			Integer name = indexOfBeginners.size();
			textField.setText(bob + name.toString() + GREATER_THAN);
			textField.setCaretPosition(bob.length());
			genParamList(indexOfBeginners);
		}
	}
	
}
	
	


