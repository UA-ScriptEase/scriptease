package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.ResourceTreeObserver;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Displays a tree of game objects and contains various methods of filtering
 * them, including a search field and a {@link TypeAction}.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class ResourcePanel extends JPanel {
	private static ResourcePanel instance = new ResourcePanel();

	private final ResourceTree resources;

	public static ResourcePanel getInstance() {
		return ResourcePanel.instance;
	}

	public ResourcePanel() {
		this.resources = new ResourceTree();

		final JPanel filterPane;
		final JPanel searchFilterPane;

		final JScrollPane treeScrollPane;

		final JTextField searchField;

		final TypeAction typeFilter;

		filterPane = new JPanel();
		searchFilterPane = new JPanel();

		searchField = ComponentFactory.buildJTextFieldWithTextBackground(20,
				"Game Objects", "");

		typeFilter = new TypeAction();

		treeScrollPane = new JScrollPane(this.resources,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.resources.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.resources.setBackground(Color.WHITE);

		treeScrollPane.setBackground(Color.WHITE);
		treeScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				ResourcePanel.this.resources.filterByTypes(typeFilter
						.getSelectedTypes());
			}
		});

		// Sets up the type filter.
		searchFilterPane.add(searchField);
		searchFilterPane.add(new JButton(typeFilter));
		searchFilterPane.setLayout(new BoxLayout(searchFilterPane,
				BoxLayout.LINE_AXIS));

		// FilterPane Layout
		filterPane.setLayout(new BoxLayout(filterPane, BoxLayout.Y_AXIS));
		filterPane.add(searchFilterPane);
		filterPane.setMaximumSize(new Dimension(ScriptEaseUI.MAX_SCREEN_WIDTH,
				50));

		this.setPreferredSize(new Dimension(
				this.resources.getPreferredSize().width, 0));

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.add(filterPane);
		this.add(Box.createVerticalStrut(5));
		this.add(treeScrollPane);

		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				ResourcePanel.this.resources.filterByText(searchField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
		});

		SEModelManager.getInstance().addSEModelObserver(this,
				new SEModelObserver() {

					public void modelChanged(SEModelEvent event) {
						final SEModelEvent.Type eventType = event
								.getEventType();

						if (eventType == SEModelEvent.Type.ACTIVATED) {
							ResourcePanel.this.resources.fillTree();

							final SEModel model = event.getPatternModel();

							if (model != null) {
								ResourcePanel.this.resources
										.filterByTypes(model.getTypeKeywords());

								model.process(new ModelAdapter() {
									@Override
									public void processLibraryModel(
											LibraryModel libraryModel) {
										ResourcePanel.this.setVisible(false);
									}

									@Override
									public void processStoryModel(
											StoryModel storyModel) {
										ResourcePanel.this.setVisible(true);
									}
								});

							}
						} else if (eventType == SEModelEvent.Type.REMOVED
								&& SEModelManager.getInstance()
										.getActiveModel() == null) {
							ResourcePanel.this.resources.fillTree();
							ResourcePanel.this.setVisible(false);
						}
					}
				});

		this.setVisible(SEModelManager.getInstance().getActiveModel() instanceof StoryModel);
	}

	/**
	 * Adds a {@link ResourceTreeObserver} directly to the {@link ResourcePanel}
	 * 's {@link ResourceTree}.
	 * 
	 * @param object
	 * @param observer
	 */
	public void addObserver(Object object, ResourceTreeObserver observer) {
		this.resources.addObserver(object, observer);
	}
}
