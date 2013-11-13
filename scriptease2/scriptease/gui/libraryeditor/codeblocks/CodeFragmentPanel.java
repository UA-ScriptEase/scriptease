package scriptease.gui.libraryeditor.codeblocks;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import scriptease.controller.FragmentAdapter;
import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.WidgetDecorator;
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
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;
import scriptease.translator.codegenerator.code.fragments.container.FormatDefinitionFragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentFragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;
import scriptease.util.GUIOp;

/**
 * Creates a panel for a code fragment. These accept SEFocus. They will build
 * their children automatically.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class CodeFragmentPanel extends JPanel {
	private final CodeBlock codeBlock;
	private final AbstractFragment fragment;

	public CodeFragmentPanel(final CodeBlock codeBlock,
			AbstractFragment fragment) {
		this.codeBlock = codeBlock;
		this.fragment = fragment;

		this.setOpaque(true);
		this.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				SEFocusManager.getInstance().setFocus(CodeFragmentPanel.this);
				CodeFragmentPanel.this.requestFocusInWindow();
			}
		});

		SEFocusManager.getInstance().addSEFocusObserver(this,
				new SEFocusObserver() {
					@Override
					public void gainFocus(Component oldFocus) {
						CodeFragmentPanel.this.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.CODE_EDITOR_COLOR, 1.7));
					}

					@Override
					public void loseFocus(Component oldFocus) {
						CodeFragmentPanel.this
								.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);
					}
				});

		this.redraw();
	}

	public AbstractFragment getFragment() {
		return fragment;
	}

	public CodeBlock getCodeBlock() {
		return codeBlock;
	}

	public void redraw() {
		this.removeAll();

		if (this.fragment == null) {
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			this.setupPanel("Code", ScriptEaseUI.CODE_EDITOR_COLOR);
			for (AbstractFragment subFragment : this.codeBlock.getCode()) {
				this.add(new CodeFragmentPanel(codeBlock, subFragment));
			}
			return;
		}

		this.fragment.process(new FragmentAdapter() {
			private final CodeFragmentPanel panel = CodeFragmentPanel.this;

			/**
			 * Builds and adds the children of the fragment.
			 * 
			 * @param fragment
			 */
			private void buildSubPanes(AbstractContainerFragment fragment) {
				this.buildSubPanes(panel, fragment);
			}

			/**
			 * Builds and adds the children of the fragment to a panel.
			 * 
			 * @param fragment
			 */
			private void buildSubPanes(JPanel panel,
					AbstractContainerFragment fragment) {
				for (AbstractFragment subFragment : fragment.getSubFragments()) {
					panel.add(new CodeFragmentPanel(codeBlock, subFragment));
				}
			}

			@Override
			public void processLineFragment(LineFragment fragment) {
				final JLabel lineLabel = new JLabel("\\n");

				lineLabel.setForeground(ScriptEaseUI.LINE_FRAGMENT_COLOR);
				lineLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));

				panel.setupPanel("Line", ScriptEaseUI.LINE_FRAGMENT_COLOR);
				panel.setLayout(new FlowLayout(FlowLayout.LEADING));

				this.buildSubPanes(fragment);
				panel.add(lineLabel);

			}

			@Override
			public void processIndentFragment(IndentFragment fragment) {
				panel.setupPanel("Indent", ScriptEaseUI.INDENT_FRAGMENT_COLOR);

				final JLabel indentLabel = new JLabel(String.valueOf('\u21e5'));
				final JPanel subFragmentsPanel = new JPanel();

				indentLabel.setForeground(ScriptEaseUI.INDENT_FRAGMENT_COLOR);
				indentLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));

				panel.add(indentLabel);

				subFragmentsPanel.setLayout(new BoxLayout(subFragmentsPanel,
						BoxLayout.PAGE_AXIS));

				buildSubPanes(subFragmentsPanel, fragment);

				panel.setLayout(new FlowLayout(FlowLayout.LEADING));
				panel.add(subFragmentsPanel);
			}

			@Override
			public void processScopeFragment(ScopeFragment fragment) {
				panel.setupPanel("Scope", ScriptEaseUI.SCOPE_FRAGMENT_COLOR);
				panel.buildScopePanel(fragment);
				this.buildSubPanes(fragment);
			}

			@Override
			public void processSeriesFragment(SeriesFragment fragment) {
				panel.setupPanel("Series", ScriptEaseUI.SERIES_FRAGMENT_COLOR);
				panel.buildSeriesPanel(fragment);
				this.buildSubPanes(fragment);
			}

			@Override
			public void processFormatReferenceFragment(
					FormatReferenceFragment fragment) {
				panel.setupPanel("Format Reference",
						ScriptEaseUI.REFERENCE_FRAGMENT_COLOR);
				panel.buildTextEditorPanel(fragment);
			}

			@Override
			public void processLiteralFragment(LiteralFragment fragment) {
				panel.setupPanel("Literal", ScriptEaseUI.LITERAL_FRAGMENT_COLOR);
				panel.buildTextEditorPanel(fragment);
			}

			@Override
			public void processSimpleDataFragment(SimpleDataFragment fragment) {
				panel.setupPanel("Simple Data",
						ScriptEaseUI.SIMPLE_FRAGMENT_COLOR);
				panel.buildSimplePanel(fragment);
			}

			public void processFormatDefinitionFragment(
					FormatDefinitionFragment fragment) {
				// Does nothing yet. It will have to if we use this for the
				// language dictionary.
			};
		});

		this.revalidate();
		this.repaint();
	}

	private void setupPanel(String title, Color color) {
		final Border lineBorder;
		final Border titledBorder;

		lineBorder = BorderFactory.createLineBorder(color);
		titledBorder = BorderFactory.createTitledBorder(lineBorder, title,
				TitledBorder.LEADING, TitledBorder.TOP, new Font("SansSerif",
						Font.BOLD, 12), color);

		this.setName(title);
		this.setBorder(titledBorder);
	}

	/**
	 * Creates a panel representing a Scope Fragment.<br>
	 * 
	 * @param scopeFragment
	 *            The ScopeFragment to create a panel for. This can be a
	 *            completely new ScopeFragment.
	 * @return
	 */
	private void buildScopePanel(final ScopeFragment scopeFragment) {
		final JLabel directiveLabel = new JLabel("Data");
		final JLabel nameRefLabel = new JLabel("NameRef");

		final JPanel scopeComponentPanel;
		final JComboBox directiveBox;
		final JTextField nameRefField;

		scopeComponentPanel = new JPanel();
		directiveBox = new JComboBox();
		nameRefField = new JTextField(scopeFragment.getNameRef());

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

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameRefField,
				new Runnable() {
					@Override
					public void run() {
						scopeFragment.setNameRef(nameRefField.getText());
					}
				});

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		scopeComponentPanel.setOpaque(false);

		scopeComponentPanel.add(directiveLabel);
		scopeComponentPanel.add(directiveBox);
		scopeComponentPanel.add(nameRefLabel);
		scopeComponentPanel.add(nameRefField);

		this.add(scopeComponentPanel);
	}

	/**
	 * Series panel for editing series fragments.
	 * 
	 * @param seriesFragment
	 * @return
	 */
	private void buildSeriesPanel(final SeriesFragment seriesFragment) {
		final JLabel directiveLabel = new JLabel("Data");
		final JLabel separatorLabel = new JLabel("Separator");
		final JLabel uniqueLabel = new JLabel("Unique");
		final JLabel filterLabel = new JLabel("Filter");
		final JLabel filterTypeLabel = new JLabel("Filter Type");

		final JPanel seriesComponentPanel;
		final JPanel filterComponentPanel;

		final JComboBox directiveBox;
		final JTextField separatorField;
		final JTextField filterField;
		final JCheckBox uniqueCheckBox;
		final JComboBox filterTypeBox;

		seriesComponentPanel = new JPanel();
		filterComponentPanel = new JPanel();

		directiveBox = new JComboBox();
		uniqueCheckBox = new JCheckBox();
		separatorField = new JTextField(seriesFragment.getSeparator());
		filterField = new JTextField(seriesFragment.getFilter());
		filterTypeBox = new JComboBox();

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

		uniqueCheckBox.setSelected(seriesFragment.isUnique());

		uniqueCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				seriesFragment.setUnique(uniqueCheckBox.isSelected());
			}
		});
		WidgetDecorator.decorateJTextFieldForFocusEvents(separatorField,
				new Runnable() {
					@Override
					public void run() {
						seriesFragment.setSeparator(separatorField.getText());
					}
				});
		WidgetDecorator.decorateJTextFieldForFocusEvents(filterField,
				new Runnable() {
					@Override
					public void run() {
						seriesFragment.setFilter(filterField.getText());
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

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

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

		this.add(seriesComponentPanel);
		this.add(filterComponentPanel);
	}

	/**
	 * Creates a panel that lets one edit the directive text.
	 * 
	 * @param fragment
	 * @return
	 */
	private void buildTextEditorPanel(final AbstractFragment fragment) {
		final JTextField field;

		field = new JTextField(fragment.getDirectiveText());

		field.setMinimumSize(new Dimension(15, field.getMinimumSize().height));

		WidgetDecorator.decorateJTextFieldForFocusEvents(field, new Runnable() {
			@Override
			public void run() {
				fragment.setDirectiveText(field.getText());
			}
		});

		this.add(field);
	}

	/**
	 * Creates a panel representing a Simple Fragment.
	 * 
	 * @param simpleFragment
	 * @return
	 */
	private void buildSimplePanel(final SimpleDataFragment simpleFragment) {
		final JLabel directiveLabel = new JLabel("Data");
		final JLabel legalRangeLabel = new JLabel("LegalRange");

		final JComboBox directiveBox;
		final JTextField legalRangeField;

		directiveBox = new JComboBox();
		legalRangeField = new JTextField(simpleFragment.getLegalRange());

		for (CodeGenerationConstants.DataType value : CodeGenerationConstants.DataType
				.values())
			directiveBox.addItem(value.name());

		directiveBox.setSelectedItem(simpleFragment.getDirectiveText()
				.toUpperCase());

		directiveBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simpleFragment.setDirectiveText((String) directiveBox
						.getSelectedItem());
			}
		});
		WidgetDecorator.decorateJTextFieldForFocusEvents(legalRangeField,
				new Runnable() {
					@Override
					public void run() {
						simpleFragment.setLegalRange(legalRangeField.getText());
					}
				});

		this.add(directiveLabel);
		this.add(directiveBox);
		this.add(legalRangeLabel);
		this.add(legalRangeField);
	}
}
