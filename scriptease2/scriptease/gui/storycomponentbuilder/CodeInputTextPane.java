package scriptease.gui.storycomponentbuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.LineFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleFragment;

@SuppressWarnings("serial")
public class CodeInputTextPane extends JTextPane implements DocumentListener, KeyListener, UndoableEditListener{
	private boolean hotKeyStatus;
	private final int ctrlDown = KeyEvent.CTRL_DOWN_MASK;
	private Style defaultStyle;
	private Style paramStyle;
	private StyledDocument doc;
	private UndoManager undoMang = new UndoManager();
	
	public CodeInputTextPane(){
		setSizeOfCodeComp();
		initCodePane();
	}
	
	private void initStyles(){
		defaultStyle = this.addStyle("BASE", null);
		StyleConstants.setFontSize(defaultStyle, 16);
		StyleConstants.setFontFamily(defaultStyle, "Courier");
		StyleConstants.setForeground(defaultStyle, Color.BLUE);
		
		paramStyle = this.addStyle("BASEPARAM", null);
		StyleConstants.setFontSize(paramStyle, 16);
		StyleConstants.setFontFamily(paramStyle, "Courier");
		StyleConstants.setForeground(paramStyle, Color.RED);
		StyleConstants.setBold(paramStyle, true);

		this.setLogicalStyle(defaultStyle);
	}
	
	//this does not work...
	private void setSizeOfCodeComp(){
		setSize(new Dimension(250,300));
		setMinimumSize(new Dimension(250,400));
		setMaximumSize(getMinimumSize());
	}
		
	private void initCodePane(){
		hotKeyStatus = false;
		this.setBackground(Color.WHITE);
		doc = this.getStyledDocument();
		doc.addDocumentListener(this);
		doc.addUndoableEditListener(this);
		this.addKeyListener(this);
		initStyles();
		regTextMode();	
	}
	
	public StyledDocument getCodePaneStyledDocument(){
		return doc;
	}
		
	public boolean hotKeyStatus(){
		return hotKeyStatus;
	}
	
	private void adjustFontSettings(){
		hotKeyStatus = !hotKeyStatus;
		changeTextFormat();
	}
	
	private void changeTextFormat(){
		if(!hotKeyStatus){
			regTextMode();
			return;
		}
		paramTextMode();
	}
	
	public void setCodeSymbol(String codeSymbol){
		String realSet = codeSymbol;
		this.setText(realSet);
	}
	
	private void setTextInRangeToScopeFormat(ArrayList<StringIndx> scopeIndicies){
		for(StringIndx a : scopeIndicies){
			doc.setCharacterAttributes(a.begin, a.end, paramStyle, true);
			doc.setCharacterAttributes(a.end, getDocTextAll().length(), defaultStyle, true);
		}
	}
	 
	public void setCodeFragments(Collection<FormatFragment> fFrag){
		String setMe = "";
		ArrayList<StringIndx> scopeIndicies = new ArrayList<StringIndx>();
		for(FormatFragment a : fFrag){
			if(a instanceof LineFragment){
				for(FormatFragment b : ((LineFragment)a).getSubFragments()){
					if(b instanceof LiteralFragment){
						setMe += b.getDirectiveText();
					}
					if(b instanceof ScopeFragment){
						int currentOffset = setMe.length();
						String scopeString = ((ScopeFragment)b).getNameRef();
						int currentEnd = currentOffset + scopeString.length();
						StringIndx scopeIndexInfo = new StringIndx(currentOffset, currentEnd);
						scopeIndicies.add(scopeIndexInfo);
						setMe += scopeString;
					}
				}
				setMe += '\n';
			}
		}
		this.setText(setMe);
		this.setTextInRangeToScopeFormat(scopeIndicies);
	}
	
	public ArrayList<FormatFragment> getCodeFragments(){
		ArrayList<StringIndx> stringsToParse = stringContextParse();
		ArrayList<FormatFragment> lineCollention = new ArrayList<FormatFragment>();
		ArrayList<FormatFragment> subFragments = new ArrayList<FormatFragment>();
		for(StringIndx textFragmentsFromInput : stringsToParse){
			FormatFragment newFragment;
			if(textFragmentsFromInput.newLine){
				LineFragment lineFragment = new LineFragment("/n", subFragments);
				lineCollention.add(lineFragment);
				subFragments.removeAll(subFragments);
			}
			if(textFragmentsFromInput.context && !textFragmentsFromInput.newLine){
				String addString = getDocTextInRange(textFragmentsFromInput.begin, textFragmentsFromInput.end);
				newFragment = new LiteralFragment(addString);
				subFragments.add(newFragment);
			}
			if(!textFragmentsFromInput.context && !textFragmentsFromInput.newLine){
				String scopeString = getDocTextInRange(textFragmentsFromInput.begin, textFragmentsFromInput.end);
				ArrayList<FormatFragment> simpleFragment = new ArrayList<FormatFragment>();
				simpleFragment.add(new SimpleFragment("name", Pattern.compile("^[a-zA-Z]+[0-9a-zA-Z_]*")));
				newFragment = new ScopeFragment("argument", scopeString,simpleFragment);
				subFragments.add(newFragment);
			}
			if(stringsToParse.indexOf(stringsToParse) == stringsToParse.size()-1){
				LineFragment lineFragment = new LineFragment("/n", subFragments);
				lineCollention.add(lineFragment);
			}
		}
		return lineCollention;
	}
	
	private void regTextMode(){
		this.setCharacterAttributes(defaultStyle, true);
		this.setCaretColor(Color.BLUE);
	}
	
	private void paramTextMode(){
		this.setCharacterAttributes(paramStyle, true);
		this.setCaretColor(Color.RED);
	}
	
	private String getDocTextInRange(int begin, int end){
		String s = "";
		try {
			s = doc.getText(begin, end - begin);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	private String getDocTextAll(){
		String s = "";
		try {
			s = doc.getText(0, getText().length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	//true = reg/blue, false = param/red
	private boolean regOrParam(int i){
		return doc.getCharacterElement(i).getAttributes().containsAttribute(StyleConstants.Foreground, Color.BLUE);
	}
	
	private ArrayList<StringIndx> stringContextParse(){
		String osNewLine = System.getProperty("line.separator");
		String[] lineSeperatedStrings = getText().split(osNewLine);
				
		ArrayList<StringIndx> a = new ArrayList<StringIndx>();
		int masterCount = 0;
		for(int m =0; m < lineSeperatedStrings.length; m++){
			int n =0;
			while(n < lineSeperatedStrings[m].length()){
				StringIndx stringFragIndxs;
				stringFragIndxs  = new StringIndx(masterCount, regOrParam(masterCount));
				while((regOrParam(masterCount) == stringFragIndxs.context) && n < lineSeperatedStrings[m].length()){
					n++;
					masterCount++;
				}
				stringFragIndxs.end = masterCount;
				a.add(stringFragIndxs);
				
				
			}
				masterCount ++;
				StringIndx aNL = new StringIndx(true);
				a.add(aNL);
		}
		
		return a;
	}
	
	private void changeHighlightedText(boolean stat){
		Style s;
		
		if(stat){
			s = defaultStyle;
		}
		else{
			s = paramStyle;
		}
		
		int startIndex = this.getSelectionStart();
		int offset = this.getSelectedText().length();
		
			doc.setCharacterAttributes(startIndex, offset, s, true);
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getModifiersEx() == ctrlDown && e.getKeyCode() == KeyEvent.VK_X){
				adjustFontSettings();
		}
		
		if(e.getModifiersEx() == ctrlDown && e.getKeyCode() == KeyEvent.VK_Z){
			try{
				if (undoMang.canUndo()){
					undoMang.undo();
				}
			} catch(CannotUndoException ez){
				
			}
		}
		
		if(e.getModifiersEx() == ctrlDown && e.getKeyCode() == KeyEvent.VK_R){
			try{
				if(undoMang.canRedo()){
					undoMang.redo();
				}
			} catch(CannotRedoException ez){
				
			}
			
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
				
	}
	

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		undoMang.addEdit(e.getEdit());
	}
	
	
	public void highlightToProperExpression(boolean swicthConext){
		try{
			if(!(this.getSelectedText() == null)){
				changeHighlightedText(swicthConext);
			}
			else{
				hotKeyStatus = !swicthConext;
				changeTextFormat();		
			}
		} catch(NullPointerException e){}
	}
	
	private class StringIndx {
		public boolean context;
		public int begin;
		public int end;
		public boolean newLine;
		
		public StringIndx(int beg, boolean cont) {
			context = cont;
			begin = beg;
			newLine = false;
		}
		
		public StringIndx(int beg, int fin){
			begin = beg;
			end = fin;
		}
		
		public StringIndx(boolean newLi){
			newLine = newLi;
		}
	}
	

}
