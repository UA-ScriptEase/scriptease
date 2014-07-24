package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.codegenerator.CodeGenerator;
import scriptease.translator.codegenerator.ScriptInfo;

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

		final JTextArea textArea = new JTextArea(code);
		final JScrollPane scrollPane = new JScrollPane(textArea);
		final JDialog dialog = WindowFactory.getInstance().buildDialog(
				"Code Generation Results");

		dialog.add(scrollPane);
		dialog.pack();
		dialog.setVisible(true);
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal()
				&& SEModelManager.getInstance().getActiveStoryModel() != null;
	}
}
