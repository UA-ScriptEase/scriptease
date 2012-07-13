package scriptease.gui.storycomponentbuilder;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.LineFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.ScopeFragment;

public class CodeInputTextPane {
	private boolean hotKeyStatus;
	private final int ctrlDown = KeyEvent.CTRL_DOWN_MASK;
	private Style defaultStyle;
	private Style paramStyle;
	private StyledDocument doc;

	private JTextPane codeInputTextPane;

	public CodeInputTextPane() {
		codeInputTextPane = new JTextPane();

		hotKeyStatus = false;
		codeInputTextPane.setBackground(Color.WHITE);

		doc = codeInputTextPane.getStyledDocument();

		defaultStyle = codeInputTextPane.addStyle("BASE", null);
		paramStyle = codeInputTextPane.addStyle("BASEPARAM", null);

		codeInputTextPane.setLogicalStyle(defaultStyle);

		codeInputTextPane.setCharacterAttributes(defaultStyle, true);
		codeInputTextPane.setCaretColor(Color.BLUE);
	}

	public void setCodeFragments(Collection<FormatFragment> fFrag) {
		String textToSet = "";
		ArrayList<StringIndx> scopeIndicies = new ArrayList<StringIndx>();
		for (FormatFragment a : fFrag) {
			if (a instanceof LineFragment) {
				for (FormatFragment b : ((LineFragment) a).getSubFragments()) {
					if (b instanceof LiteralFragment) {
						textToSet += b.getDirectiveText();
					}
					if (b instanceof ScopeFragment) {
						int currentOffset = textToSet.length();
						String scopeString = ((ScopeFragment) b).getNameRef();
						int currentEnd = currentOffset + scopeString.length();
						StringIndx scopeIndexInfo = new StringIndx(
								currentOffset, currentEnd);
						scopeIndicies.add(scopeIndexInfo);
						textToSet += scopeString;
					}
				}
				textToSet += '\n';
			}
		}
		codeInputTextPane.setText(textToSet);

		for (StringIndx stringIndex : scopeIndicies) {
			doc.setCharacterAttributes(stringIndex.begin, stringIndex.end, paramStyle, true);
			String parameterString = "";
			try {
				parameterString = doc.getText(0, codeInputTextPane.getText().length());
				doc.setCharacterAttributes(stringIndex.end, parameterString.length(), defaultStyle,
						true);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getModifiersEx() == ctrlDown && e.getKeyCode() == KeyEvent.VK_X) {
			hotKeyStatus = !hotKeyStatus;
			if (!hotKeyStatus) {
				codeInputTextPane.setCharacterAttributes(defaultStyle, true);
				codeInputTextPane.setCaretColor(Color.BLUE);
				return;
			}
			codeInputTextPane.setCharacterAttributes(paramStyle, true);
			codeInputTextPane.setCaretColor(Color.RED);
		}
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
