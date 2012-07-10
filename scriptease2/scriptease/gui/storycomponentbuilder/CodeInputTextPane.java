package scriptease.gui.storycomponentbuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;

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

@SuppressWarnings("serial")
public class CodeInputTextPane extends JTextPane implements DocumentListener,
		KeyListener, UndoableEditListener {
	private boolean hotKeyStatus;
	private final int ctrlDown = KeyEvent.CTRL_DOWN_MASK;
	private Style defaultStyle;
	private Style paramStyle;
	private StyledDocument doc;
	private UndoManager undoMang = new UndoManager();

	public CodeInputTextPane() {
		setSize(new Dimension(250, 300));
		setMinimumSize(new Dimension(250, 400));
		setMaximumSize(getMinimumSize());

		hotKeyStatus = false;
		this.setBackground(Color.WHITE);
		doc = this.getStyledDocument();
		doc.addDocumentListener(this);
		doc.addUndoableEditListener(this);
		this.addKeyListener(this);

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

		regTextMode();
	}

	private void changeTextFormat() {
		if (!hotKeyStatus) {
			regTextMode();
			return;
		}
		this.setCharacterAttributes(paramStyle, true);
		this.setCaretColor(Color.RED);
	}

	public void setCodeFragments(Collection<FormatFragment> fFrag) {
		String setMe = "";
		ArrayList<StringIndx> scopeIndicies = new ArrayList<StringIndx>();
		for (FormatFragment a : fFrag) {
			if (a instanceof LineFragment) {
				for (FormatFragment b : ((LineFragment) a).getSubFragments()) {
					if (b instanceof LiteralFragment) {
						setMe += b.getDirectiveText();
					}
					if (b instanceof ScopeFragment) {
						int currentOffset = setMe.length();
						String scopeString = ((ScopeFragment) b).getNameRef();
						int currentEnd = currentOffset + scopeString.length();
						StringIndx scopeIndexInfo = new StringIndx(
								currentOffset, currentEnd);
						scopeIndicies.add(scopeIndexInfo);
						setMe += scopeString;
					}
				}
				setMe += '\n';
			}
		}
		this.setText(setMe);

		for (StringIndx a : scopeIndicies) {
			doc.setCharacterAttributes(a.begin, a.end, paramStyle, true);
			String s = "";
			try {
				s = doc.getText(0, getText().length());
				doc.setCharacterAttributes(a.end, s.length(),
						defaultStyle, true);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	private void regTextMode() {
		this.setCharacterAttributes(defaultStyle, true);
		this.setCaretColor(Color.BLUE);
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
		if (e.getModifiersEx() == ctrlDown && e.getKeyCode() == KeyEvent.VK_X) {
			hotKeyStatus = !hotKeyStatus;
			changeTextFormat();
		}

		if (e.getModifiersEx() == ctrlDown && e.getKeyCode() == KeyEvent.VK_Z) {
			try {
				if (undoMang.canUndo()) {
					undoMang.undo();
				}
			} catch (CannotUndoException ez) {

			}
		}

		if (e.getModifiersEx() == ctrlDown && e.getKeyCode() == KeyEvent.VK_R) {
			try {
				if (undoMang.canRedo()) {
					undoMang.redo();
				}
			} catch (CannotRedoException ez) {

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

	private class StringIndx {
		public int begin;
		public int end;

		public StringIndx(int beg, int fin) {
			begin = beg;
			end = fin;
		}
	}
}
