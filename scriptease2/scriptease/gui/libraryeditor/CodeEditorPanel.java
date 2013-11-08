package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.AbstractFragmentAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.action.libraryeditor.codeeditor.DeleteFragmentAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertIndentAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertLineAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertLiteralAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertReferenceAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertScopeAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertSeriesAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertSimpleAction;
import scriptease.gui.action.libraryeditor.codeeditor.MoveFragmentDownAction;
import scriptease.gui.action.libraryeditor.codeeditor.MoveFragmentUpAction;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.CodeGenerationConstants;
import scriptease.translator.codegenerator.CodeGenerationConstants.ScopeType;
import scriptease.translator.codegenerator.CodeGenerationConstants.SeriesFilterType;
import scriptease.translator.codegenerator.CodeGenerationConstants.SeriesType;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentFragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;
import scriptease.util.GUIOp;

/**
 * Panel used to edit code inside of code blocks graphically.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class CodeEditorPanel extends JPanel implements StoryComponentObserver {

	private final Map<JPanel, AbstractFragment> panelToFragmentMap;
	private final CodeBlock codeBlock;
	/**
	 * The top level JPanel.
	 */
	private final JPanel codeEditorPanel;
	private final JScrollPane codeEditorScrollPane;
	private String simplePanelName;

	/**
	 * Creates a new Code Editor Panel using the passed in Code Block.
	 * 
	 * @param codeBlock
	 */
	protected CodeEditorPanel(CodeBlock codeBlock) {
		super();
		this.codeBlock = codeBlock;
		this.panelToFragmentMap = new HashMap<JPanel, AbstractFragment>();
		this.codeBlock.addStoryComponentObserver(this);
		this.simplePanelName = "Simple Data";

		final String CODE_EDITOR_PANEL_NAME = "Code";

		final JToolBar toolbar;
		final JButton lineButton;
		final JButton indentButton;
		final JButton scopeButton;
		final JButton seriesButton;
		final JButton simpleButton;
		final JButton literalButton;
		final JButton referenceButton;
		final JButton deleteButton;
		final JButton moveUpButton;
		final JButton moveDownButton;
		final JButton listerineButton;

		final Border lineBorder;
		final Border titledBorder;

		toolbar = new JToolBar("Code Editor ToolBar");
		lineButton = new JButton(InsertLineAction.getInstance());
		indentButton = new JButton(InsertIndentAction.getInstance());
		scopeButton = new JButton(InsertScopeAction.getInstance());
		seriesButton = new JButton(InsertSeriesAction.getInstance());
		simpleButton = new JButton(InsertSimpleAction.getInstance());
		literalButton = new JButton(InsertLiteralAction.getInstance());
		referenceButton = new JButton(InsertReferenceAction.getInstance());
		deleteButton = new JButton(DeleteFragmentAction.getInstance());
		moveUpButton = new JButton(MoveFragmentUpAction.getInstance());
		moveDownButton = new JButton(MoveFragmentDownAction.getInstance());
		listerineButton = new JButton(" ");

		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.CODE_EDITOR_COLOR);
		titledBorder = BorderFactory.createTitledBorder(lineBorder,
				CODE_EDITOR_PANEL_NAME, TitledBorder.LEADING, TitledBorder.TOP,
				new Font("SansSerif", Font.PLAIN, 12),
				ScriptEaseUI.CODE_EDITOR_COLOR);

		this.codeEditorPanel = buildObjectContainerPanel(CODE_EDITOR_PANEL_NAME);
		this.codeEditorScrollPane = new JScrollPane(this.codeEditorPanel);

		listerineButton.setOpaque(false);
		listerineButton.setContentAreaFilled(false);
		listerineButton.setBorderPainted(false);
		listerineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CodeEditorPanel.this.simplePanelName = "Listerine";
			}
		});

		toolbar.setFloatable(false);

		toolbar.add(lineButton);
		toolbar.add(indentButton);
		toolbar.add(scopeButton);
		toolbar.add(seriesButton);
		toolbar.add(simpleButton);
		toolbar.add(literalButton);
		toolbar.add(referenceButton);
		toolbar.add(deleteButton);
		toolbar.add(moveUpButton);
		toolbar.add(moveDownButton);
		toolbar.add(listerineButton);

		this.codeEditorScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		this.codeEditorScrollPane.setPreferredSize(new Dimension(400, 400));

		this.setLayout(new BorderLayout());
		this.codeEditorPanel.setLayout(new BoxLayout(this.codeEditorPanel,
				BoxLayout.PAGE_AXIS));
		this.codeEditorPanel.setBorder(titledBorder);

		this.add(toolbar, BorderLayout.PAGE_START);
		this.add(this.codeEditorScrollPane, BorderLayout.CENTER);

		this.fillCodeEditorPanel();

		FormatFragmentSelectionManager.getInstance().setFormatFragment(null,
				codeBlock);
	}

	/**
	 * Fills the code editor panel with FormatFragments present in the
	 * CodeBlock.
	 */
	private void fillCodeEditorPanel() {
		final Collection<AbstractFragment> codeFragments;
		final Rectangle visibleRectangle;

		codeFragments = this.codeBlock.getCode();
		visibleRectangle = this.codeEditorScrollPane.getVisibleRect();

		this.codeEditorPanel.removeAll();
		this.panelToFragmentMap.clear();
		final AbstractFragment selectedFragment = FormatFragmentSelectionManager
				.getInstance().getFormatFragment();

		if (selectedFragment == null) {
			this.codeEditorPanel.setBackground(GUIOp.scaleWhite(
					ScriptEaseUI.CODE_EDITOR_COLOR, 1.7));
		} else {
			this.codeEditorPanel
					.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);
		}
		buildDefaultPanes(this.codeEditorPanel, codeFragments);
		updatePanelSelectionHighlight(selectedFragment);
		this.codeEditorPanel.repaint();
		this.codeEditorPanel.revalidate();

		this.codeEditorScrollPane.scrollRectToVisible(visibleRectangle);
	}

	/**
	 * This creates a panel with the specified title, and using the passed in
	 * colour. It is used by the various Fragment Panels to create a common
	 * appearance between them.
	 * 
	 * @param title
	 * @param color
	 * @return
	 */
	private JPanel buildObjectContainerPanel(final String title) {
		final JPanel objectContainerPanel;

		objectContainerPanel = new JPanel();
		objectContainerPanel.setName(title);

		objectContainerPanel.setOpaque(true);
		objectContainerPanel.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);

		objectContainerPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				final AbstractFragment selectedFragment;

				selectedFragment = CodeEditorPanel.this.panelToFragmentMap
						.get(objectContainerPanel);

				FormatFragmentSelectionManager.getInstance().setFormatFragment(
						selectedFragment, CodeEditorPanel.this.codeBlock);
				updatePanelSelectionHighlight(selectedFragment);
			}
		});
		return objectContainerPanel;
	}

	/**
	 * Creates a panel representing a LineFragment. This is a container
	 * fragment, meaning it can contain other fragments. LineFragments place
	 * whatever code is within them on its own line. Code does not automatically
	 * wrap, so using LineFragments can help with formatting.
	 * 
	 * @return
	 */
	private JPanel buildLinePanel() {
		final String TITLE = "Line";
		final JPanel linePanel;
		final Border lineBorder;

		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.LINE_FRAGMENT_COLOR);

		linePanel = buildObjectContainerPanel(TITLE);

		linePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		linePanel.setBorder(lineBorder);

		return linePanel;
	}

	/**
	 * Creates a panel representing an IndentedFragment. This is a container
	 * fragment, meaning it can contain other fragments. IndentedFragments
	 * indent whatever code is within them using the indent string. Using
	 * IndentedFragments can help with formatting.<br>
	 * <br>
	 * The indent string is defined in the LanguageDictionary within the
	 * &lt;IndentString /&gt; tag.
	 * 
	 * @return
	 */
	private JPanel buildIndentPanel(IndentFragment indentFragment) {
		final String TITLE = "Indent";

		final JPanel indentPanel;
		final JPanel subFragmentsPanel;
		final JLabel indentLabel;
		final Border lineBorder;

		indentPanel = buildObjectContainerPanel(TITLE);
		subFragmentsPanel = new JPanel();
		indentLabel = new JLabel(String.valueOf('\u21e5'));
		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.INDENT_FRAGMENT_COLOR);

		indentLabel.setForeground(ScriptEaseUI.INDENT_FRAGMENT_COLOR);
		indentLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));

		indentPanel.setBorder(lineBorder);

		indentPanel.add(indentLabel);

		subFragmentsPanel.setLayout(new BoxLayout(subFragmentsPanel,
				BoxLayout.PAGE_AXIS));

		buildDefaultPanes(subFragmentsPanel, indentFragment.getSubFragments());

		indentPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		indentPanel.add(subFragmentsPanel);

		return indentPanel;
	}

	/**
	 * Creates a panel representing a Scope Fragment.<br>
	 * <br>
	 * <b>How A User Will Insert a Parameter:</b> <br>
	 * <br>
	 * <ol>
	 * <li>Insert a ScopeFragment into the Code Editor.</li>
	 * <li>Set the Data box to "Argument".</li>
	 * <li>Set the NameRef field to the desired parameter name.</li>
	 * <li>Insert a SimpleFragment into the ScopeFragment</li>
	 * <li>Set the Data field to "Name"</li>
	 * <li>Set the LegalValues field to a relevant regular expression.<br>
	 * The RegEx commonly used in the Neverwinter Nights translator is
	 * <code>"^[a-zA-Z]+[0-9a-zA-Z_]*"</code>.
	 * </ol>
	 * The resulting code will look something like this:<br>
	 * <br>
	 * <code>
	 * &lt;Scope data="argument" ref="Plot"&gt;
	 * <br>
	 * &nbsp;&nbsp;&lt;Fragment data="name" legalValues="^[a-zA-Z]+[0-9a-zA-Z_]*"/&gt;
	 * <br>
	 * &lt;/Scope&gt;
	 * </code>
	 * 
	 * @param scopeFragment
	 *            The ScopeFragment to create a panel for. This can be a
	 *            completely new ScopeFragment.
	 * @return
	 */
	private JPanel buildScopePanel(final ScopeFragment scopeFragment) {
		final String TITLE = "Scope";

		final JPanel scopePanel;
		final JPanel scopeComponentPanel;
		final JComboBox directiveBox;
		final JTextField nameRefField;
		final JLabel directiveLabel;
		final JLabel nameRefLabel;
		final Border lineBorder;
		final Border titledBorder;

		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.SCOPE_FRAGMENT_COLOR);
		titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
				TitledBorder.LEADING, TitledBorder.TOP, new Font("SansSerif",
						Font.BOLD, 12), ScriptEaseUI.SCOPE_FRAGMENT_COLOR);

		scopePanel = buildObjectContainerPanel(TITLE);
		scopeComponentPanel = new JPanel();
		directiveBox = new JComboBox();
		nameRefField = new JTextField();
		directiveLabel = new JLabel("Data");
		nameRefLabel = new JLabel("NameRef");

		directiveLabel.setLabelFor(directiveBox);
		nameRefLabel.setLabelFor(nameRefField);

		for (ScopeType directiveType : ScopeType.values())
			directiveBox.addItem(directiveType.name());

		directiveBox.setSelectedItem(scopeFragment.getDirectiveText()
				.toUpperCase());

		directiveBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scopeFragment.setDirectiveText((String) directiveBox
						.getSelectedItem());
			}
		});

		nameRefField.setText(scopeFragment.getNameRef());

		nameRefField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				scopeFragment.setNameRef(nameRefField.getText());

				scopePanel.revalidate();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.PAGE_AXIS));
		scopePanel.setBorder(titledBorder);

		scopeComponentPanel.setOpaque(false);

		scopeComponentPanel.add(directiveLabel);
		scopeComponentPanel.add(directiveBox);
		scopeComponentPanel.add(nameRefLabel);
		scopeComponentPanel.add(nameRefField);

		scopePanel.add(scopeComponentPanel);

		return scopePanel;
	}

	/**
	 * Series panel for editing series fragments.
	 * 
	 * @param seriesFragment
	 * @return
	 */
	private JPanel buildSeriesPanel(final SeriesFragment seriesFragment) {
		final String TITLE = "Series";

		final JPanel seriesPanel;
		final JPanel seriesComponentPanel;
		final JPanel filterComponentPanel;

		final JComboBox directiveBox;
		final JTextField separatorField;
		final JCheckBox uniqueCheckBox;
		final JTextField filterField;
		final JComboBox filterTypeBox;

		final JLabel directiveLabel;
		final JLabel separatorLabel;
		final JLabel uniqueLabel;
		final JLabel filterLabel;
		final JLabel filterTypeLabel;

		final Border lineBorder;
		final Border titledBorder;

		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.SERIES_FRAGMENT_COLOR);
		titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
				TitledBorder.LEADING, TitledBorder.TOP, new Font("SansSerif",
						Font.BOLD, 12), ScriptEaseUI.SERIES_FRAGMENT_COLOR);

		seriesPanel = buildObjectContainerPanel(TITLE);
		seriesComponentPanel = new JPanel();
		filterComponentPanel = new JPanel();

		directiveBox = new JComboBox();
		separatorField = new JTextField();
		uniqueCheckBox = new JCheckBox();
		filterField = new JTextField();
		filterTypeBox = new JComboBox();

		directiveLabel = new JLabel("Data");
		separatorLabel = new JLabel("Separator");
		uniqueLabel = new JLabel("Unique");
		filterLabel = new JLabel("Filter");
		filterTypeLabel = new JLabel("Filter Type");

		directiveLabel.setLabelFor(directiveBox);
		separatorLabel.setLabelFor(separatorField);
		uniqueLabel.setLabelFor(uniqueCheckBox);
		filterLabel.setLabelFor(filterField);
		filterTypeLabel.setLabelFor(filterTypeBox);

		for (SeriesType directiveType : SeriesType.values())
			directiveBox.addItem(directiveType.name());

		directiveBox.setSelectedItem(seriesFragment.getDirectiveText()
				.toUpperCase());

		directiveBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				seriesFragment.setDirectiveText((String) directiveBox
						.getSelectedItem());
			}
		});

		separatorField.setText(seriesFragment.getSeparator());

		separatorField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void insertUpdate(DocumentEvent e) {
						seriesFragment.setSeparator(separatorField.getText());

						seriesPanel.revalidate();
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						insertUpdate(e);
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
					}
				});

		uniqueCheckBox.setSelected(seriesFragment.isUnique());

		uniqueCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				seriesFragment.setUnique(uniqueCheckBox.isSelected());
			}
		});

		filterField.setText(seriesFragment.getFilter());

		filterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				seriesFragment.setFilter(filterField.getText());

				seriesPanel.revalidate();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		for (SeriesFilterType filterType : SeriesFilterType.values()) {
			filterTypeBox.addItem(filterType);
		}

		filterTypeBox.setSelectedItem(seriesFragment.getFilterType());

		filterTypeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				seriesFragment.setFilterType((SeriesFilterType) filterTypeBox
						.getSelectedItem());
			}
		});

		seriesPanel.setLayout(new BoxLayout(seriesPanel, BoxLayout.PAGE_AXIS));
		seriesPanel.setBorder(titledBorder);

		seriesComponentPanel.setOpaque(false);
		filterComponentPanel.setOpaque(false);

		seriesComponentPanel.add(directiveLabel);
		seriesComponentPanel.add(directiveBox);
		seriesComponentPanel.add(separatorLabel);
		seriesComponentPanel.add(separatorField);
		seriesComponentPanel.add(uniqueLabel);
		seriesComponentPanel.add(uniqueCheckBox);

		filterComponentPanel.add(filterLabel);
		filterComponentPanel.add(filterField);
		filterComponentPanel.add(filterTypeLabel);
		filterComponentPanel.add(filterTypeBox);

		seriesPanel.add(seriesComponentPanel);
		seriesPanel.add(filterComponentPanel);

		return seriesPanel;
	}

	/**
	 * Creates a panel representing a Simple Fragment.
	 * 
	 * @param simpleFragment
	 * @return
	 */
	private JPanel buildSimplePanel(final SimpleDataFragment simpleFragment) {
		final String TITLE = "Simple Data";

		final JPanel simplePanel;
		final JComboBox directiveBox;
		final JTextField legalRangeField;
		final JLabel directiveLabel;
		final JLabel legalRangeLabel;
		final Border lineBorder;
		final Border titledBorder;

		simplePanel = buildObjectContainerPanel(TITLE);

		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.SIMPLE_FRAGMENT_COLOR);
		titledBorder = BorderFactory.createTitledBorder(lineBorder,
				this.simplePanelName, TitledBorder.LEADING, TitledBorder.TOP,
				new Font("SansSerif", Font.BOLD, 12),
				ScriptEaseUI.SIMPLE_FRAGMENT_COLOR);

		directiveBox = new JComboBox();
		legalRangeField = new JTextField();
		directiveLabel = new JLabel("Data");
		legalRangeLabel = new JLabel("LegalRange");

		directiveLabel.setLabelFor(directiveBox);
		legalRangeLabel.setLabelFor(legalRangeField);

		for (CodeGenerationConstants.DataType directiveType : CodeGenerationConstants.DataType
				.values())
			directiveBox.addItem(directiveType.name());

		directiveBox.setSelectedItem(simpleFragment.getDirectiveText()
				.toUpperCase());

		directiveBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simpleFragment.setDirectiveText((String) directiveBox
						.getSelectedItem());
			}
		});

		legalRangeField.setText(simpleFragment.getLegalRange().toString());

		legalRangeField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void insertUpdate(DocumentEvent e) {
						simpleFragment.setLegalRange(legalRangeField.getText());

						simplePanel.revalidate();
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						insertUpdate(e);
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
					}
				});

		simplePanel.setBorder(titledBorder);

		simplePanel.add(directiveLabel);
		simplePanel.add(directiveBox);
		simplePanel.add(legalRangeLabel);
		simplePanel.add(legalRangeField);

		return simplePanel;
	}

	/**
	 * Creates a panel representing a Literal Fragment.
	 * 
	 * @param literalFragment
	 * @return
	 */
	private JPanel buildLiteralPanel(final LiteralFragment literalFragment) {
		final String TITLE = "Literal";

		final JPanel literalPanel;
		final JTextField literalField;
		final Border lineBorder;
		final Border titledBorder;

		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.LITERAL_FRAGMENT_COLOR);
		titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
				TitledBorder.LEADING, TitledBorder.TOP, new Font("SansSerif",
						Font.BOLD, 12), ScriptEaseUI.LITERAL_FRAGMENT_COLOR);

		literalPanel = buildObjectContainerPanel(TITLE);
		literalField = new JTextField(literalFragment.getDirectiveText());

		literalField.setMinimumSize(new Dimension(15, literalField
				.getMinimumSize().height));

		literalField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				literalFragment.setDirectiveText(literalField.getText());

				literalPanel.revalidate();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				literalFragment.setDirectiveText(literalField.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

		});

		literalPanel.setBorder(titledBorder);
		literalPanel.add(literalField);

		return literalPanel;
	}

	/**
	 * Creates a panel representing a Reference Fragment.
	 * 
	 * @param referenceFragment
	 * @return
	 */
	private JPanel buildReferencePanel(
			final FormatReferenceFragment referenceFragment) {
		final String TITLE = "Format Reference";

		final JPanel referencePanel;
		final JTextField referenceField;
		final Border lineBorder;
		final Border titledBorder;

		lineBorder = BorderFactory
				.createLineBorder(ScriptEaseUI.REFERENCE_FRAGMENT_COLOR);
		titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
				TitledBorder.LEADING, TitledBorder.TOP, new Font("SansSerif",
						Font.BOLD, 12), ScriptEaseUI.REFERENCE_FRAGMENT_COLOR);

		referencePanel = buildObjectContainerPanel(TITLE);
		referenceField = new JTextField(referenceFragment.getDirectiveText());

		referenceField.setMinimumSize(new Dimension(15, referenceField
				.getMinimumSize().height));

		referenceField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void insertUpdate(DocumentEvent e) {
						referenceFragment.setDirectiveText(referenceField
								.getText());

						referencePanel.revalidate();
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						referenceFragment.setDirectiveText(referenceField
								.getText());
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
					}

				});

		referencePanel.setBorder(titledBorder);
		referencePanel.add(referenceField);

		return referencePanel;
	}

	private void updatePanelSelectionHighlight(final AbstractFragment fragment) {
		for (Entry<JPanel, AbstractFragment> entry : this.panelToFragmentMap
				.entrySet()) {
			final JPanel panel = entry.getKey();
			final AbstractFragment value = entry.getValue();

			if ((fragment != null) && (value.equals(fragment))) {
				highlightPanel(panel, value);
			} else {
				panel.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);
			}

			panel.revalidate();
			panel.repaint();
		}
	}

	private void highlightPanel(final JPanel panel,
			final AbstractFragment fragment) {
		final AbstractFragmentAdapter adapter = new AbstractFragmentAdapter() {
			@Override
			public void processLineFragment(LineFragment fragment) {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.LINE_FRAGMENT_COLOR, 1.2));
			}

			@Override
			public void processIndentFragment(IndentFragment fragment) {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.INDENT_FRAGMENT_COLOR, 1.2));
			}

			@Override
			public void processScopeFragment(ScopeFragment fragment) {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.SCOPE_FRAGMENT_COLOR, 5.0));
			}

			@Override
			public void processLiteralFragment(LiteralFragment fragment) {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.LITERAL_FRAGMENT_COLOR, 1.7));
			}

			@Override
			public void processSeriesFragment(SeriesFragment fragment) {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.SERIES_FRAGMENT_COLOR, 3.0));
			}

			@Override
			public void processFormatReferenceFragment(
					FormatReferenceFragment fragment) {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.REFERENCE_FRAGMENT_COLOR, 3.0));
			}

			@Override
			public void processSimpleDataFragment(SimpleDataFragment fragment) {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.SIMPLE_FRAGMENT_COLOR, 3.5));
			}
		};
		fragment.process(adapter);
	}

	/**
	 * Recursively builds the default panel according to the passed code
	 * fragments.
	 * 
	 * @param panel
	 * @param codeFragments
	 */
	private void buildDefaultPanes(JPanel panel,
			Collection<AbstractFragment> codeFragments) {

		for (AbstractFragment codeFragment : codeFragments) {
			final JPanel fragmentPanel;

			if (codeFragment instanceof LineFragment) {
				final JLabel lineLabel;

				lineLabel = new JLabel("\\n");
				fragmentPanel = buildLinePanel();

				lineLabel.setForeground(ScriptEaseUI.LINE_FRAGMENT_COLOR);
				lineLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));

				buildDefaultPanes(fragmentPanel,
						((LineFragment) codeFragment).getSubFragments());

				fragmentPanel.add(lineLabel);

				panel.add(fragmentPanel);
				this.panelToFragmentMap.put(fragmentPanel, codeFragment);
			} else if (codeFragment instanceof IndentFragment) {
				fragmentPanel = buildIndentPanel((IndentFragment) codeFragment);

				panel.add(fragmentPanel);
				this.panelToFragmentMap.put(fragmentPanel, codeFragment);
			} else if (codeFragment instanceof LiteralFragment) {
				fragmentPanel = buildLiteralPanel((LiteralFragment) codeFragment);

				panel.add(fragmentPanel);
				this.panelToFragmentMap.put(fragmentPanel, codeFragment);
			} else if (codeFragment instanceof ScopeFragment) {
				fragmentPanel = buildScopePanel((ScopeFragment) codeFragment);

				buildDefaultPanes(fragmentPanel,
						((ScopeFragment) codeFragment).getSubFragments());
				panel.add(fragmentPanel);
				this.panelToFragmentMap.put(fragmentPanel, codeFragment);
			} else if (codeFragment instanceof SeriesFragment) {
				fragmentPanel = buildSeriesPanel((SeriesFragment) codeFragment);

				buildDefaultPanes(fragmentPanel,
						((SeriesFragment) codeFragment).getSubFragments());
				panel.add(fragmentPanel);
				this.panelToFragmentMap.put(fragmentPanel, codeFragment);
			} else if (codeFragment instanceof FormatReferenceFragment) {
				fragmentPanel = buildReferencePanel((FormatReferenceFragment) codeFragment);

				panel.add(fragmentPanel);
				this.panelToFragmentMap.put(fragmentPanel, codeFragment);
			} else if (codeFragment instanceof SimpleDataFragment) {
				fragmentPanel = buildSimplePanel((SimpleDataFragment) codeFragment);

				panel.add(fragmentPanel);
				this.panelToFragmentMap.put(fragmentPanel, codeFragment);
			}
		}
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		fillCodeEditorPanel();
	}
}