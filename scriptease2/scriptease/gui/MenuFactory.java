package scriptease.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.FocusManager;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import scriptease.ScriptEase;
import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.FileManager;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.observer.FileManagerObserver;
import scriptease.gui.action.DeleteAction;
import scriptease.gui.action.file.ClosePatternModelAction;
import scriptease.gui.action.file.NewStoryModelAction;
import scriptease.gui.action.file.OpenRecentFileAction;
import scriptease.gui.action.file.OpenStoryModelAction;
import scriptease.gui.action.file.SaveModelAction;
import scriptease.gui.action.file.SaveModelExplicitlyAction;
import scriptease.gui.action.file.TestStoryAction;
import scriptease.gui.action.libraryeditor.NewCauseAction;
import scriptease.gui.action.libraryeditor.NewDescriptionAction;
import scriptease.gui.action.libraryeditor.NewEffectAction;
import scriptease.gui.action.system.ExitScriptEaseAction;
import scriptease.gui.action.undo.RedoAction;
import scriptease.gui.action.undo.UndoAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
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
 */
public class MenuFactory {
	private static final String FILE = Il8nResources.getString("File");
	private static final String ADD = Il8nResources.getString("Add");
	private static final String TOOLS = Il8nResources.getString("Tools");
	private static final String HELP = Il8nResources.getString("Help");
	private static final String NEW = Il8nResources.getString("New");
	private static final String DEBUG = "Debug";

	/**
	 * Creates the top level menu bar for a story.
	 * 
	 * @param model
	 *            Builds a slightly different file menu if the main menu is
	 *            getting created for the library editor.
	 * 
	 * @return the top level menu bar.
	 */
	public static JMenuBar createMainMenuBar(PatternModel model) {
		final JMenuBar bar = new JMenuBar();

		bar.add(MenuFactory.buildFileMenu(model));
		bar.add(MenuFactory.buildEditMenu());
		bar.add(MenuFactory.buildToolsMenu());

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
	private static JMenu buildFileMenu(PatternModel model) {
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
		menu.add(ClosePatternModelAction.getInstance());
		menu.addSeparator();

		if (model == null || !(model instanceof LibraryModel)) {
			menu.add(TestStoryAction.getInstance());
			menu.addSeparator();
		}

		menu.add(SaveModelAction.getInstance());
		menu.add(SaveModelExplicitlyAction.getInstance());
		menu.addSeparator();

		// add the recent files list
		short i;
		for (i = 0; i < FileManager.getInstance().getRecentFileCount(); i++) {
			menu.add(new OpenRecentFileAction(i));
		}

		if (i > 0)
			menu.addSeparator();

		menu.add(ExitScriptEaseAction.getInstance());
		// Set up a listener to update the file menu's recent file list.
		// I'm doing this via an anonymous inner class because I don't think its
		// worth creating a new subclass over. - remiller
		FileManager.getInstance().addObserver(new FileManagerObserver() {
			@Override
			public void fileReferenced(StoryModel model, File location) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						MenuFactory.rebuildRecentFiles(menu);
					}
				});
			}
		});

		return menu;
	}

	private static void rebuildRecentFiles(JMenu menu) {
		final Component[] menuItems = menu.getPopupMenu().getComponents();
		int startIndex = -1;
		for (int i = 0; i < menuItems.length; i++) {
			if (menuItems[i] instanceof JMenuItem
					&& ((AbstractButton) menuItems[i]).getAction() instanceof OpenRecentFileAction) {
				if (startIndex == -1) {
					startIndex = i;
				}
				// remove current entry from menu
				menu.remove(menuItems[i]);

				// add new entry to menu
				menu.add(new JMenuItem(new OpenRecentFileAction(
						(short) (i - startIndex))), i);
			}
		}
	}

	/**
	 * Builds the Edit menu.
	 * 
	 * @return the Edit Menu
	 */
	private static JMenu buildEditMenu() {
		// Create the Edit menu to return.
		final JMenu editMenu = new JMenu(Il8nResources.getString("Edit"));
		editMenu.setMnemonic(KeyEvent.VK_E);

		// Add the Undo and Redo actions.
		editMenu.add(UndoAction.getInstance());
		editMenu.add(RedoAction.getInstance());

		editMenu.addSeparator();

		// The Cut/Copy/Paste items are different because Transfer Handlers
		// use special actions whose display names are hardcoded into a check in
		// TransferHandler. Since we don't want those display names (they're
		// lower case and the check is case-sensitive) I'm overriding the
		// getText method to no longer return the display text from the action,
		// but the display text set here. It's ugly but it's the least horrible
		// solution I've come up with and it works. -remiller

		// Create and add the Cut item.
		@SuppressWarnings("serial")
		final JMenuItem cutItem = new JMenuItem(TransferHandler.getCutAction()) {
			@Override
			public String getText() {
				return "Cut";
			}
		};
		cutItem.setMnemonic(KeyEvent.VK_U);
		cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		editMenu.add(cutItem);

		// Create and add the Copy item.
		@SuppressWarnings("serial")
		final JMenuItem copyItem = new JMenuItem(
				TransferHandler.getCopyAction()) {
			@Override
			public String getText() {
				return "Copy";
			}
		};
		copyItem.setMnemonic(KeyEvent.VK_T);
		copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		editMenu.add(copyItem);

		// Create and add the Paste item.
		@SuppressWarnings("serial")
		final JMenuItem pasteItem = new JMenuItem(
				TransferHandler.getPasteAction()) {
			@Override
			public String getText() {
				return "Paste";
			}
		};
		pasteItem.setMnemonic(KeyEvent.VK_P);
		pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		editMenu.add(pasteItem);

		// delete item
		editMenu.add(DeleteAction.getInstance());

		editMenu.addSeparator();

		// Create and add the preferences item.
		final JMenuItem preferencesItem = new JMenuItem(
				Il8nResources.getString("Preferences") + "...");
		preferencesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				WindowFactory.getInstance().showPreferencesDialog();
			}
		});
		preferencesItem.setMnemonic(KeyEvent.VK_R);
		editMenu.add(preferencesItem);

		// Return the Edit menu.
		return editMenu;
	}

	private static JMenu buildHelpMenu() {
		final JMenu menu = new JMenu(MenuFactory.HELP);
		menu.setMnemonic(KeyEvent.VK_H);

		final JMenuItem helpMenuItem = new JMenuItem(
				Il8nResources.getString("About_ScriptEase"));

		helpMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowFactory.getInstance().showAboutScreen();
			}
		});
		helpMenuItem.setMnemonic(KeyEvent.VK_A);
		menu.add(helpMenuItem);

		return menu;
	}

	private static JMenu buildToolsMenu() {
		final JMenu menu = new JMenu(MenuFactory.TOOLS);
		menu.setMnemonic(KeyEvent.VK_T);

		final JMenu libraryEditorMenu;

		libraryEditorMenu = new JMenu("Library Editor");

		for (final Translator translator : TranslatorManager.getInstance()
				.getTranslators()) {
			final JMenuItem translatorItem;

			translatorItem = new JMenuItem(translator.getName());

			translatorItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TranslatorManager.getInstance().setActiveTranslator(
							translator);

					PanelFactory.getInstance().createTabForModel(
							translator.getLibrary());
				}
			});

			libraryEditorMenu.add(translatorItem);
		}
		menu.add(libraryEditorMenu);

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
		final JMenuItem viewGraphEditorItem;
		final JMenuItem consoleOutputItem;

		menu.add(MenuFactory.buildStoryMenu());

		menu.setMnemonic(KeyEvent.VK_D);

		throwExceptionItem = new JMenuItem("Throw Exception!");
		throwErrorItem = new JMenuItem("Throw Error!");
		generateCodeItem = new JMenuItem("Generate Code");
		viewGraphEditorItem = new JMenuItem("View Graph Editor (NWN Only)");
		consoleOutputItem = new JMenuItem("Print Things!");

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
						PatternModel activeModel = PatternModelManager
								.getInstance().getActiveModel();
						if (activeModel != null
								&& activeModel instanceof StoryModel) {
							final Collection<StoryProblem> problems = new ArrayList<StoryProblem>();
							final Collection<ScriptInfo> scriptInfos = CodeGenerator
									.generateCode((StoryModel) activeModel,
											problems);

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

		viewGraphEditorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final TranslatorManager manager = TranslatorManager
						.getInstance();
				Translator nwn = manager.getTranslator("Neverwinter Nights");

				LibraryModel model = nwn.getApiDictionary().getLibrary();
				for (StoryComponent component : model.getAllStoryComponents()) {
					component.process(new StoryAdapter() {
						public void processKnowIt(final KnowIt knowIt) {
							knowIt.getBinding().process(new BindingAdapter() {
								public void processDescribeIt(
										KnowItBindingDescribeIt described) {
									JFrame frame = new JFrame("Graph Editor");

									frame.add(PanelFactory.getInstance()
											.buildDescribeItPanel(
													described.getValue()
															.getStartNode(),
													described.getValue()));
									frame.setMinimumSize(new Dimension(800, 300));
									frame.setVisible(true);
								};
							});
						}
					});
				}
			}
		});

		consoleOutputItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Current Focus: "
						+ FocusManager.getCurrentManager().getFocusOwner());
			}
		});

		throwExceptionItem.setMnemonic(KeyEvent.VK_A);
		menu.add(throwExceptionItem);
		menu.add(throwErrorItem);
		menu.addSeparator();
		menu.add(generateCodeItem);
		menu.addSeparator();
		menu.add(viewGraphEditorItem);
		menu.add(consoleOutputItem);

		return menu;
	}
}
