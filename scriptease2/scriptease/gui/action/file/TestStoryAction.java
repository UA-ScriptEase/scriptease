package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.SEFrame;
import scriptease.gui.WindowManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.util.StringOp;

/**
 * Represents and performs the Test Story command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Test Story runs the Game Module in the game specified for that story's
 * translator, if there is much a method available. Otherwise, it will be
 * disabled.
 * 
 * TODO: Supress actions and disable scriptease while game is playing.
 * 
 * @author lari
 * @author remiller
 * 
 */
@SuppressWarnings("serial")
public final class TestStoryAction extends ActiveModelSensitiveAction {
	private static final String TEST_STORY_TOOLTIP = "Run the current story in game";

	private static final String TEST_STORY = Il8nResources
			.getString("Test_Story");

	private static final Action instance = new TestStoryAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return TestStoryAction.instance;
	}

	private TestStoryAction() {
		super(TestStoryAction.TEST_STORY);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
		this.putValue(Action.SHORT_DESCRIPTION,
				TestStoryAction.TEST_STORY_TOOLTIP);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final WindowManager winMan = WindowManager.getInstance();
		final StoryModel activeModel = StoryModelPool.getInstance()
				.getActiveModel();
		final Thread testThread;
		final Runnable testTask;

		if (!winMan.showConfirmTestStory())
			return;

		testTask = new Runnable() {
			@Override
			public void run() {
				final ProcessBuilder procBuilder;
				final Process tester;
				final SEFrame frame = SEFrame.getInstance();

				frame.setStatus("Testing " + activeModel.getTitle());
				try {
					procBuilder = new ProcessBuilder();
					activeModel.getModule().configureTester(procBuilder);

					// I think merging error and regular output is probably a
					// good thing
					// since we don't do anything special with them. - remiller
					procBuilder.redirectErrorStream(true);
					tester = procBuilder.start();

					BufferedReader input = new BufferedReader(
							new InputStreamReader(tester.getInputStream()));

					/*
					 * Ordinarily, we might call tester.waitFor() here, but
					 * since we don't care what the game does, we won't hold up
					 * ourselves for it. - remiller
					 */

					// dump the process output, just in case it's important.
					String line;
					while ((line = input.readLine()) != null) {
						System.err.println(line);
					}
				} catch (FileNotFoundException fnfEx) {
					winMan.showProblemDialog("Missing Game Files",
							"I can't run the game, because it seems to be missing a file. \n"
									+ fnfEx.getMessage());
				} catch (IOException ioEx) {
					winMan.showProblemDialog("Game Died",
							"There was a problem starting the game engine. Sorry about that.");
				} catch (UnsupportedOperationException nopeEx) {
					winMan.showProblemDialog(
							"I can't do that, Dave.",
							"This translator doesn't actually support testing, sorry.\n\nThe translator author was supposed to tell me that in the translator.ini file.");
				}
				frame.setStatus("Finished testing " + activeModel.getTitle());
			}
		};

		// run this on a separate thread to be able to at least still draw
		// correctly, if not receive input events.
		testThread = new Thread(testTask, "TestStory-"
				+ StringOp.removeWhiteSpace(activeModel.getName()));
		testThread.start();
	}

	@Override
	protected boolean isLegal() {
		boolean isLegal = super.isLegal();
		final Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		isLegal &= activeTranslator != null
				&& activeTranslator.getSupportsTesting();

		return isLegal;
	}
}
