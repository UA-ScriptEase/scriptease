package scriptease.gui.action.file;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.codegenerator.CodeGenerator;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.util.StringOp;

/**
 * Represents and performs the Test Story command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Test Story runs the Game Module in the game specified for that story's
 * translator, if there is much a method available. Otherwise, it will be
 * disabled.
 * 
 * @author lari
 * @author remiller
 * 
 */
@SuppressWarnings("serial")
public final class TestCodeAction extends ActiveModelSensitiveAction {
	private static final String TEST_STORY_TOOLTIP = "Test if the code will work and display it";
	private static final String TEST_CODE = "Test and Display Code";

	private static final Action instance = new TestCodeAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return TestCodeAction.instance;
	}

	private TestCodeAction() {
		super(TestCodeAction.TEST_CODE);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		this.putValue(Action.SHORT_DESCRIPTION,
				TestCodeAction.TEST_STORY_TOOLTIP);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final StoryModel story;
		final Collection<StoryProblem> problems;
		final Collection<ScriptInfo> scriptInfos;

		story = SEModelManager.getInstance().getActiveStoryModel();
		problems = new ArrayList<StoryProblem>();
		scriptInfos = CodeGenerator.getInstance().generateCode(story, problems);

		String code = "";
		for (ScriptInfo script : scriptInfos) {
			code = code + "\n\n==== New script file for slot: "
					+ script.getSlot() + " on object: " + script.getSubject()
					+ " ====\n" + script.getCode();
		}

		final JDialog dialog = new CodeEditorDialog(code);

		dialog.setVisible(true);
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal()
				&& SEModelManager.getInstance().getActiveStoryModel() != null;
	}

	private class CodeEditorDialog extends JDialog {
		private int searchIndex = 0;

		public CodeEditorDialog(final String code) {
			super(WindowFactory.getInstance().getCurrentFrame(),
					"Code Generation Results",
					Dialog.ModalityType.DOCUMENT_MODAL);

			final JMenuBar menu = new JMenuBar();
			final JTextArea textArea = new JTextArea(code);
			final JScrollPane scrollPane = new JScrollPane(textArea);
			final Highlighter highlight = textArea.getHighlighter();
			final JTextField searchField = new JTextField();
			final JButton next = new JButton("Next");
			final ActionListener nextListener = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					highlight.removeAllHighlights();

					final String text = searchField.getText();

					searchIndex = code.indexOf(text,
							searchIndex + text.length());

					updateHighlight(code, highlight, searchField, textArea);
				}
			};

			searchField.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void changedUpdate(DocumentEvent e) {
							highlight.removeAllHighlights();

							final String text = searchField.getText();

							searchIndex = code.indexOf(text, searchIndex);

							updateHighlight(code, highlight, searchField,
									textArea);
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							this.changedUpdate(e);
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							this.changedUpdate(e);
						}

					});

			next.addActionListener(nextListener);
			searchField.addActionListener(nextListener);

			menu.add(new JLabel("Find"));
			menu.add(searchField);
			menu.add(next);

			textArea.setEditable(false);

			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setLayout(new BorderLayout());
			this.add(menu, BorderLayout.NORTH);
			this.add(scrollPane, BorderLayout.CENTER);
			final Dimension screenSize = Toolkit.getDefaultToolkit()
					.getScreenSize();

			this.setPreferredSize(new Dimension(screenSize.width / 2,
					screenSize.height / 2));
			this.pack();

		}

		private void updateHighlight(String code, Highlighter highlight,
				JTextField searchField, JTextArea area) {

			final String text = searchField.getText();
			if (searchIndex == -1) {
				searchIndex = code.indexOf(text);
			}
			if (searchIndex > -1 && StringOp.exists(text)) {
				area.setCaretPosition(searchIndex);
				area.moveCaretPosition(searchIndex + text.length());
				try {
					highlight.addHighlight(searchIndex,
							searchIndex + text.length(),
							DefaultHighlighter.DefaultPainter);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
