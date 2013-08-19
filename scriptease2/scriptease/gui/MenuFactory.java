package scriptease.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import scriptease.ScriptEase;
import scriptease.controller.FileManager;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.observer.RecentFileObserver;
import scriptease.gui.action.components.CopyAction;
import scriptease.gui.action.components.CutAction;
import scriptease.gui.action.components.DeleteAction;
import scriptease.gui.action.components.DisableAction;
import scriptease.gui.action.components.DuplicateAction;
import scriptease.gui.action.components.PasteAction;
import scriptease.gui.action.file.CloseActiveModelAction;
import scriptease.gui.action.file.NewStoryModelAction;
import scriptease.gui.action.file.OpenRecentFileAction;
import scriptease.gui.action.file.OpenStoryModelAction;
import scriptease.gui.action.file.SaveModelAction;
import scriptease.gui.action.file.SaveModelExplicitlyAction;
import scriptease.gui.action.file.SaveModelPackageAction;
import scriptease.gui.action.file.SaveModelWithoutCodeAction;
import scriptease.gui.action.file.TestStoryAction;
import scriptease.gui.action.library.AddLibraryToStoryModelAction;
import scriptease.gui.action.libraryeditor.NewCauseAction;
import scriptease.gui.action.libraryeditor.NewDescriptionAction;
import scriptease.gui.action.libraryeditor.NewEffectAction;
import scriptease.gui.action.libraryeditor.OpenLibraryEditorAction;
import scriptease.gui.action.metrics.MetricsAction;
import scriptease.gui.action.system.ExitScriptEaseAction;
import scriptease.gui.action.translator.TranslatorPreferencesAction;
import scriptease.gui.action.undo.RedoAction;
import scriptease.gui.action.undo.UndoAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.CodeGenerator;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.util.FileOp;

/**
 * MenuFactory is responsible for building all of the menus in ScriptEase
 * including the context menus for StoryComponents. Context menus should be
 * identical or near-identical to similar menus found in the menu bar.<br>
 * <br>
 * The menu builder takes advantage of the support in some Swing widgets for
 * using Action instances to separate state and function from the widget. This
 * is done to avoid sub-classing various JComponents in such a way that we'd end
 * up with special classes that only differ in the command that they perform. <br>
 * <br>
 * It also allows us to tie different methods of accomplishing the same task to
 * one instance of that task. For example, there could be a Delete menu item in
 * the Edit menu of the main frame, and also a Delete menu item in a context
 * menu. If we use Actions, then the function of those buttons may remain the
 * same and in one location in code. <br>
 * <br>
 * To further this goal of sharing Action objects between differing menu items,
 * every Action should be implementing the Singleton design pattern, or
 * something similar, unless there is a very, very good reason to have two
 * separate instances of the same command floating about.<br>
 * <br>
 * See <a
 * href=http://java.sun.com/docs/books/tutorial/uiswing/misc/action.html>http
 * ://java.sun.com/docs/books/tutorial/uiswing/misc/action.html</a> for
 * documentation on how to use Action instances with Swing widgets.
 * 
 * @author remiller
 * @author kschenk
 * @author jyuen
 */
public class MenuFactory {
	private static final String FILE = Il8nResources.getString("File");
	private static final String ADD = Il8nResources.getString("Add");
	private static final String LIBRARY = "Library";
	private static final String HELP = Il8nResources.getString("Help");
	private static final String NEW = Il8nResources.getString("New");
	private static final String DEBUG = "Debug";

	// Change this to false to remove the tools menu.
	private static final boolean TOOLS_MENU_ENABLED = true;

	/**
	 * Creates the top level menu bar for a story.
	 * 
	 * @param model
	 *            Builds a slightly different file menu if the main menu is
	 *            getting created for the library editor.
	 * 
	 * @return the top level menu bar.
	 */
	public static JMenuBar createMainMenuBar(SEModel model) {
		final JMenuBar bar = new JMenuBar();

		bar.add(MenuFactory.buildFileMenu(model));
		bar.add(MenuFactory.buildEditMenu());

		if (TOOLS_MENU_ENABLED)
			bar.add(MenuFactory.buildLibraryMenu());

		bar.add(MenuFactory.buildHelpMenu());
		if (ScriptEase.DEBUG_MODE)
			bar.add(MenuFactory.buildDebugMenu());

		return bar;
	}

	/**
	 * Builds the file menu. The File menu contains menu items for high-level
	 * operations like creating a new code library or story, opening and saving
	 * files, or exiting the application.<br>
	 * <br>
	 * Since we allow multiple models to be open at once, there should be an
	 * appropriate Save All command. In addition, a list of previously opened
	 * files should also be present for ease-of-use.
	 * 
	 * @param model
	 *            If the model is a LibraryModel, we add special functionality
	 *            to the New menu.
	 * 
	 * @return The File menu.
	 */
	private static JMenu buildFileMenu(SEModel model) {
		final JMenu menu = new JMenu(MenuFactory.FILE);
		menu.setMnemonic(KeyEvent.VK_F);

		// clear the menu to make sure we *only* have File Menu relevant stuff
		// in the menu. - remiller
		menu.removeAll();

		if (model == null || !(model instanceof LibraryModel))
			menu.add(NewStoryModelAction.getInstance());
		else {
			final JMenu newMenu;
			final JMenuItem newCause;
			final JMenuItem newEffect;
			final JMenuItem newDescription;

			newMenu = new JMenu(MenuFactory.NEW);
			newCause = new JMenuItem(NewCauseAction.getInstance());
			newEffect = new JMenuItem(NewEffectAction.getInstance());
			newDescription = new JMenuItem(NewDescriptionAction.getInstance());

			newMenu.add(NewStoryModelAction.getInstance());
			newMenu.addSeparator();
			newMenu.add(newCause);
			newMenu.add(newEffect);
			newMenu.add(newDescription);

			menu.add(newMenu);
		}

		menu.add(OpenStoryModelAction.getInstance());
		menu.add(CloseActiveModelAction.getInstance());
		menu.addSeparator();

		if (model == null || !(model instanceof LibraryModel)) {
			menu.add(TestStoryAction.getInstance());
			menu.addSeparator();
		}

		menu.add(SaveModelAction.getInstance());
		menu.add(SaveModelExplicitlyAction.getInstance());
		menu.add(SaveModelPackageAction.getInstance());
		menu.add(SaveModelWithoutCodeAction.getInstance());
		menu.addSeparator();

		// add the recent files list
		short i;
		for (i = 0; i < FileManager.getInstance().getRecentFileCount(); i++) {
			menu.add(new OpenRecentFileAction(i));
		}

		if (i > 0)
			menu.addSeparator();

		menu.add(ExitScriptEaseAction.getInstance());

		FileManager.getInstance().addRecentFileObserver(menu,
				new RecentFileObserver() {

					@Override
					public void updateRecentFiles() {
						rebuildRecentFiles(menu);
					}
				});

		return menu;
	}

	/**
	 * Rebuilds the file menu.
	 * 
	 * @param menu
	 */
	private static void rebuildRecentFiles(JMenu menu) {
		final Component[] menuItems = menu.getPopupMenu().getComponents();

		for (int i = 0; i < menuItems.length; i++) {

			// check for the first recent file entry.
			if (menuItems[i] instanceof JMenuItem
					&& ((AbstractButton) menuItems[i]).getAction() instanceof OpenRecentFileAction) {

				// remove the remainder of the components following the first
				// recent file entry.
				while (i < menuItems.length) {
					menu.remove(menuItems[i]);
					i++;
				}

				break;
			}
		}

		// add the new recent files list.
		int recentFileCount = FileManager.getInstance().getRecentFileCount();
		short i;
		for (i = 0; i < recentFileCount; i++) {
			menu.add(new OpenRecentFileAction(i));
		}

		if (i > 0)
			menu.addSeparator();

		menu.add(ExitScriptEaseAction.getInstance());
	}

	/**
	 * Builds the Edit menu.
	 * 
	 * @return the Edit Menu
	 */
	private static JMenu buildEditMenu() {
		// Create the Edit menu to return.
		final JMenu editMenu;
		final JMenuItem preferencesItem;
		final JMenuItem translatorPreferencesItem;

		editMenu = new JMenu(Il8nResources.getString("Edit"));
		preferencesItem = new JMenuItem(Il8nResources.getString("Preferences")
				+ "...");
		translatorPreferencesItem = new JMenuItem(
				TranslatorPreferencesAction.getInstance());

		// Set up the edit menu item
		editMenu.setMnemonic(KeyEvent.VK_E);

		// Add the Undo and Redo actions.
		editMenu.add(UndoAction.getInstance());
		editMenu.add(RedoAction.getInstance());

		editMenu.addSeparator();

		editMenu.add(CutAction.getInstance());
		editMenu.add(CopyAction.getInstance());
		editMenu.add(PasteAction.getInstance());
		editMenu.add(DeleteAction.getInstance());
		editMenu.add(DuplicateAction.getInstance());
		editMenu.add(DisableAction.getInstance());

		editMenu.addSeparator();

		// Create and add the preferences item.
		preferencesItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				WindowFactory.getInstance().showPreferencesDialog();
			}
		});
		preferencesItem.setMnemonic(KeyEvent.VK_R);

		editMenu.add(preferencesItem);
		editMenu.add(translatorPreferencesItem);

		// Return the Edit menu.
		return editMenu;
	}

	private static JMenu buildHelpMenu() {
		final JMenu menu = new JMenu(MenuFactory.HELP);
		menu.setMnemonic(KeyEvent.VK_H);

		final JMenuItem sendFeedbackItem;
		final JMenuItem sendBugReportItem;
		final JMenuItem helpMenuItem;

		sendFeedbackItem = new JMenuItem("Send Feedback");
		sendBugReportItem = new JMenuItem("Send Bug Report");
		helpMenuItem = new JMenuItem(
				Il8nResources.getString("About_ScriptEase"));

		sendFeedbackItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowFactory.getInstance().buildFeedbackDialog()
						.setVisible(true);
			}
		});
		sendBugReportItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowFactory.getInstance().buildBugReportDialog()
						.setVisible(true);
			}
		});
		helpMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowFactory.getInstance().showAboutScreen();
			}
		});

		sendFeedbackItem.setMnemonic(KeyEvent.VK_F);
		sendBugReportItem.setMnemonic(KeyEvent.VK_R);
		helpMenuItem.setMnemonic(KeyEvent.VK_A);

		menu.add(sendFeedbackItem);
		menu.add(sendBugReportItem);
		menu.addSeparator();
		menu.add(helpMenuItem);

		return menu;
	}

	/**
	 * Builds the library menu, with options to add and edit libraries.
	 * 
	 * @return
	 */
	private static JMenu buildLibraryMenu() {
		final JMenu menu = new JMenu(MenuFactory.LIBRARY);

		final SEModel activeModel;

		activeModel = SEModelManager.getInstance().getActiveModel();

		if (activeModel instanceof StoryModel) {
			final Collection<LibraryModel> optionalLibraries;
			final JMenu addLibrary;

			addLibrary = new JMenu("Add Library");

			optionalLibraries = activeModel.getTranslator()
					.getOptionalLibraries();

			addLibrary.setEnabled(!optionalLibraries.isEmpty());

			for (LibraryModel library : optionalLibraries) {
				addLibrary.add(new AddLibraryToStoryModelAction(library));
			}

			menu.add(addLibrary);
		}

		for (Translator translator : TranslatorManager.getInstance()
				.getTranslators()) {
			final OpenLibraryEditorAction action;
			final JMenuItem translatorItem;

			action = new OpenLibraryEditorAction(translator);
			translatorItem = new JMenuItem(action);

			/*
			 * TODO: RE-ENABLE THIS AFTER RELEASING TO 250
			 */
			
			 menu.add(translatorItem);
		}

		menu.setMnemonic(KeyEvent.VK_L);

		return menu;
	}

	/**
	 * Builds the story-editing menu. The Story menu contains menu items for
	 * pattern creation operations like adding a new encounter instance, or
	 * adding atoms. The actions in this menu are generalised to work for adding
	 * Story Components to both Stories and Libraries.<br>
	 * <br>
	 * The actions will be sensitive to the current selection in that they will
	 * add a new Story Component to the current selection and since we allow
	 * multi-select, the actions also need to be sensitive to <i>what</i> is
	 * selected to ensure it is legal.
	 * 
	 * @return The Story menu.
	 */
	private static JMenu buildStoryMenu() {
		final JMenu menu = new JMenu(MenuFactory.ADD);
		menu.setMnemonic(KeyEvent.VK_A);

		return menu;
	}

	private static JMenu buildDebugMenu() {
		final JMenu menu = new JMenu(MenuFactory.DEBUG);
		final JMenuItem throwExceptionItem;
		final JMenuItem throwErrorItem;
		final JMenuItem generateCodeItem;
		final JMenuItem consoleOutputItem;
		final JMenuItem metricsItem;

		menu.add(MenuFactory.buildStoryMenu());

		menu.setMnemonic(KeyEvent.VK_D);

		throwExceptionItem = new JMenuItem("Throw Exception!");
		throwErrorItem = new JMenuItem("Throw Error!");
		generateCodeItem = new JMenuItem("Generate Code");
		consoleOutputItem = new JMenuItem("Do Nothing");
		metricsItem = new JMenuItem(MetricsAction.getInstance());

		throwExceptionItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final File monocleDinosaur;
				monocleDinosaur = new File(
						"scriptease/resources/monocleDinosaur.txt");
				String monocleDinosaurString;

				try {
					monocleDinosaurString = FileOp
							.readFileAsString(monocleDinosaur);
				} catch (IOException e1) {
					monocleDinosaurString = "Exception!";
				}
				throw new RuntimeException(monocleDinosaurString);
			}
		});

		throwErrorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final File monocleDinosaur;
				monocleDinosaur = new File(
						"scriptease/resources/monocleDinosaur.txt");
				String monocleDinosaurString;

				try {
					monocleDinosaurString = FileOp
							.readFileAsString(monocleDinosaur);
				} catch (IOException e1) {
					monocleDinosaurString = "Exception!";
				}
				throw new Error(monocleDinosaurString);
			}
		});

		generateCodeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Get the active model with which to generate code.
						SEModel activeModel = SEModelManager.getInstance()
								.getActiveModel();
						if (activeModel != null
								&& activeModel instanceof StoryModel) {
							final Collection<StoryProblem> problems = new ArrayList<StoryProblem>();
							final Collection<ScriptInfo> scriptInfos = CodeGenerator
									.getInstance().generateCode(
											(StoryModel) activeModel, problems);

							String code = "";
							for (ScriptInfo script : scriptInfos) {
								code = code
										+ "\n\n==== New script file for slot: "
										+ script.getSlot() + " on object: "
										+ script.getSubject() + " ====\n"
										+ script.getCode();
							}

							JTextArea textArea = new JTextArea(code);
							JScrollPane scrollPane = new JScrollPane(textArea);
							JDialog dialog = WindowFactory.getInstance()
									.buildDialog("Code Generation Results");
							dialog.add(scrollPane);

							dialog.pack();
							dialog.setVisible(true);
						}
					}
				});
			}
		});

		consoleOutputItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// This is convenient for setting a breakpoint on to pause
				// our program.

				// Code to save a screenshot of the entire thing. Watch out,
				// this also removes the content pane since it reassigns its
				// parent.

				// GUIOp.saveScreenshot(WindowFactory.getInstance()
				// .getCurrentFrame().getContentPane(),
				// System.getProperty("user.home") + "/Desktop/image.png");

				System.out.println("Did nothing.");
			}
		});

		throwExceptionItem.setMnemonic(KeyEvent.VK_A);
		menu.add(throwExceptionItem);
		menu.add(throwErrorItem);
		menu.addSeparator();
		menu.add(generateCodeItem);
		menu.add(consoleOutputItem);
		menu.addSeparator();
		menu.add(metricsItem);

		return menu;
	}
}
