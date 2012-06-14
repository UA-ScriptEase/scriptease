package scriptease.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractButton;
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
import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.FileManager;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.observer.FileManagerObserver;
import scriptease.gui.action.file.CloseModelAction;
import scriptease.gui.action.file.NewModelAction;
import scriptease.gui.action.file.OpenModelAction;
import scriptease.gui.action.file.OpenRecentFileAction;
import scriptease.gui.action.file.SaveModelAction;
import scriptease.gui.action.file.SaveModelExplicitlyAction;
import scriptease.gui.action.file.TestStoryAction;
import scriptease.gui.action.story.DeleteStoryComponentAction;
import scriptease.gui.action.system.ExitScriptEaseAction;
import scriptease.gui.action.undo.RedoAction;
import scriptease.gui.action.undo.UndoAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentbuilder.StoryComponentDescriptorTemplate.ComponentContext;
import scriptease.gui.storycomponentbuilder.StoryComponentFrame;
import scriptease.gui.storycomponentbuilder.StoryComponentKnowItEditor;
import scriptease.gui.storycomponentbuilder.StoryComponentScriptItEditor;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.CodeGenerator;
import scriptease.translator.codegenerator.ScriptInfo;

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
 */
public class MenuFactory {
	private static final String FILE = Il8nResources.getString("File");
	private static final String ADD = Il8nResources.getString("Add");
	private static final String TOOLS = Il8nResources.getString("Tools");
	private static final String HELP = Il8nResources.getString("Help");
	private static final String CREATE = Il8nResources.getString("Create");
	private static final String NEW_DOIT = Il8nResources.getString("DoIt");
	private static final String NEW_KNOWIT = Il8nResources.getString("KnowIt");
	private static final String DEBUG = "Debug";

	/**
	 * Creates the top level menu bar.
	 * 
	 * @return the top level menu bar.
	 */
	public static JMenuBar createStoryMenuBar() {
		final JMenuBar bar = new JMenuBar();

		bar.add(MenuFactory.buildFileMenu());
		bar.add(MenuFactory.buildEditMenu());
		bar.add(MenuFactory.buildToolsMenu());

		bar.add(MenuFactory.buildHelpMenu());
		if (ScriptEase.DEBUG_MODE)
			bar.add(MenuFactory.buildDebugMenu());

		return bar;
	}

	/**
	 * Used in the StoryComponentBuilder.
	 * 
	 * @return
	 */
	public static JMenuBar buildBuilderMenuBar() {
		final JMenuBar builderMenuBar;
		final JMenu createMenu;
		final JMenuItem newScriptIt;
		final JMenuItem newKnowIt;

		builderMenuBar = new JMenuBar();
		createMenu = new JMenu(MenuFactory.CREATE);
		newScriptIt = new JMenuItem(MenuFactory.NEW_DOIT);
		newKnowIt = new JMenuItem(MenuFactory.NEW_KNOWIT);

		newScriptIt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Need to add listners same for describers
				StoryComponentFrame
						.getInstance()
						.getStackedBuilder()
						.setInitialPane(
								new StoryComponentScriptItEditor(new ScriptIt(
										""), ComponentContext.BASE));
			}
		});
		newKnowIt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StoryComponentFrame
						.getInstance()
						.getStackedBuilder()
						.setInitialPane(
								new StoryComponentKnowItEditor(new KnowIt(""),
										ComponentContext.BASE));
			}
		});

		createMenu.add(newScriptIt);
		createMenu.add(newKnowIt);

		builderMenuBar.add(createMenu);
		builderMenuBar.add(MenuFactory.buildEditMenu());

		return builderMenuBar;
	}

	/**
	 * Builds the specific context menu that is filled with all of the
	 * operations applicable for the given <code>StoryComponent</code>.
	 * 
	 * @param component
	 *            The <code>StoryComponent</code> to be used as a reference in
	 *            building the menu.
	 * @return The context menu for <code>component</code>.
	 */
	public static JMenu buildContextMenu(StoryComponent component) {
		JMenu contextMenu = new JMenu();

		// Action action = MenuFactory.usedActions
		// .get(DeleteStoryComponentAction.class);
		//
		// if (action == null) {
		// action = new DeleteStoryComponentAction();
		// usedActions.put(DeleteStoryComponentAction.class, action);
		// }
		//
		// contextMenu.add(action);

		return contextMenu;
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
	 * @return The File menu.
	 */
	public static JMenu buildFileMenu() {
		final JMenu menu = new JMenu(MenuFactory.FILE);
		menu.setMnemonic(KeyEvent.VK_F);

		fillFileMenu(menu);

		// Set up a listener to update the file menu's recent file list.
		// I'm doing this via an anonymous inner class because I don't think its
		// worth creating a new subclass over. - remiller
		FileManager.getInstance().addObserver(new FileManagerObserver() {
			@Override
			public void fileReferenced(StoryModel model, File location) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						rebuildRecentFiles(menu);
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
				// TODO: remove current entry from menu
				menu.remove(menuItems[i]);

				// TODO: add new entry to menu
				menu.add(new JMenuItem(new OpenRecentFileAction(
						(short) (i - startIndex))), i);
			}
		}
	}

	/**
	 * Configures the given menu as the File Menu
	 * 
	 * @param menu
	 *            The menu to be configured.
	 */
	private static void fillFileMenu(JMenu menu) {
		// clear the menu to make sure we *only* have File Menu relevant stuff
		// in the menu. - remiller
		menu.removeAll();

		// final JMenu newMenu = new JMenu("New");
		// newMenu.setMnemonic(KeyEvent.VK_N);

		menu.add(NewModelAction.getInstance());
		menu.add(OpenModelAction.getInstance());
		menu.addSeparator();

		menu.add(TestStoryAction.getInstance());
		menu.addSeparator();

		menu.add(SaveModelAction.getInstance());
		menu.add(SaveModelExplicitlyAction.getInstance());
		menu.addSeparator();

		menu.add(CloseModelAction.getInstance());
		menu.addSeparator();

		// add the recent files list
		short i;
		for (i = 0; i < FileManager.getInstance().getRecentFileCount(); i++) {
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
	public static JMenu buildEditMenu() {
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
		editMenu.add(DeleteStoryComponentAction.getInstance());

		editMenu.addSeparator();

		// Create and add the preferences item.
		@SuppressWarnings("serial")
		final JMenuItem preferencesItem = new AutofiringMenuItem(
				Il8nResources.getString("Preferences") + "...") {
			@Override
			protected void action() {
				WindowManager.getInstance().showPreferencesDialog();
			}
		};
		preferencesItem.setMnemonic(KeyEvent.VK_R);
		editMenu.add(preferencesItem);

		// Return the Edit menu.
		return editMenu;
	}

	@SuppressWarnings("serial")
	public static JMenu buildHelpMenu() {
		// TODO: Encapsulate this into an action object.
		final JMenu menu = new JMenu(MenuFactory.HELP);
		menu.setMnemonic(KeyEvent.VK_H);

		JMenuItem item = new AutofiringMenuItem(
				Il8nResources.getString("About_ScriptEase")) {
			@Override
			protected void action() {
				WindowManager.getInstance().showAboutScreen();
			}
		};
		item.setMnemonic(KeyEvent.VK_A);
		menu.add(item);

		return menu;
	}

	@SuppressWarnings("serial")
	public static JMenu buildToolsMenu() {
		final JMenu menu = new JMenu(MenuFactory.TOOLS);
		menu.setMnemonic(KeyEvent.VK_T);

		// TODO: Encapsulate this into an action object.
		JMenuItem item = new AutofiringMenuItem(
				Il8nResources.getString("Story_Component_Builder")) {
			@Override
			protected void action() {
				WindowManager.getInstance().showStoryComponentBuilder();
			}
		};
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.CTRL_MASK));

		menu.add(item);

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
	public static JMenu buildStoryMenu() {
		final JMenu menu = new JMenu(MenuFactory.ADD);
		menu.setMnemonic(KeyEvent.VK_A);

		return menu;
	}

	@SuppressWarnings("serial")
	public static JMenu buildDebugMenu() {
		final JMenu menu = new JMenu(MenuFactory.DEBUG);
		JMenuItem item;

		menu.add(MenuFactory.buildStoryMenu());

		menu.setMnemonic(KeyEvent.VK_D);

		item = new AutofiringMenuItem("Throw Exception") {
			@Override
			protected void action() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						throw new RuntimeException("This is a fake exception");
					}
				});
			}
		};
		item.setMnemonic(KeyEvent.VK_A);
		menu.add(item);

		item = new AutofiringMenuItem("Throw Error") {
			@Override
			protected void action() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						throw new Error("This is a fake error");
					}
				});
			}
		};
		menu.add(item);

		menu.addSeparator();

		item = new AutofiringMenuItem("Generate Code") {
			@Override
			protected void action() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Get the active model with which to generate code.
						StoryModel activeModel = StoryModelPool.getInstance()
								.getActiveModel();
						if (activeModel != null) {
							final Collection<StoryProblem> problems = new ArrayList<StoryProblem>();
							final Collection<ScriptInfo> scriptInfos = CodeGenerator
									.generateCode(activeModel, problems);

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
							JDialog dialog = new JDialog(SEFrame.getInstance(),
									"Code Generation Results");
							dialog.add(scrollPane);

							dialog.pack();
							dialog.setVisible(true);
						}
					}
				});
			}
		};
		menu.add(item);

		menu.addSeparator();
		item = new AutofiringMenuItem("View Graph Editor (NWN Only)") {

			@Override
			protected void action() {

				Translator nwn = TranslatorManager.getInstance().getTranslator(
						"Neverwinter Nights");
				LibraryModel model = nwn.getApiDictionary().getLibrary();
				for (StoryComponent component : model.getAllStoryComponents()) {
					component.process(new AbstractNoOpStoryVisitor() {
						public void processKnowIt(final KnowIt knowIt) {
							knowIt.getBinding().process(
									new AbstractNoOpBindingVisitor() {
										public void processDescribeIt(
												KnowItBindingDescribeIt described) {
											JFrame frame = new JFrame(
													"Graph Editor");

											frame.add(PanelFactory
													.buildDescribeItPanel(
															described
																	.getValue()
																	.getHeadNode(),
															described
																	.getValue()));
											frame.setMinimumSize(new Dimension(
													800, 300));
											frame.setVisible(true);
										};
									});
						}
					});
				}
			}
		};
		menu.add(item);

		return menu;
	}
}
