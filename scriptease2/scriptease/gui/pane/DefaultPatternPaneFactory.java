package scriptease.gui.pane;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.gui.action.story.parameter.BindParameterAction;
import scriptease.gui.control.KeywordEditor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;

/**
 * A Default Pattern Pane contains:
 * <ol>
 * <li>a general description section,</li>
 * <li>one or more speciality panes,</li>
 * <li>A pane for editing the parameters. This takes the form of a list of
 * parameters and some controls for setting any property that isn't a binding.</li>
 * </ol>
 * Speciality panes are context-specific panes for editing the properties of
 * special <code>StoryComponent</code>s, like Quests. <br>
 * <br>
 * The current implementation shows each of the above points in a tab.<br>
 * <br>
 * 
 * @author remiller
 */
public class DefaultPatternPaneFactory {
	private StoryComponent represented;

	public JPanel buildPane(StoryComponent represented) {
		// I'm using a GridLayout to force the pane to stretch to fit its
		// parent. No other reason. - remiller
		JPanel propPane = new JPanel(new GridLayout(1, 1));

		this.represented = represented;

		this.populate(propPane);

		return propPane;
	}

	private void populate(JPanel pane) {
		final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP,
				JTabbedPane.SCROLL_TAB_LAYOUT);

		// add the special panes.
		for (SpecialPaneInfo specialPane : this.buildSpecialtyPanes()) {
			tabs.addTab(specialPane.tabName, specialPane.panel);
		}

		tabs.addTab("Description", this.buildDescriptionPane());

		// parameters pane
		// tabs.addTab("Parameters", this.buildParameterPane());

		pane.add(tabs);
	}

	/**
	 * Provides a default Description Pane for any <code>StoryComponent</code>.
	 * The default Description Pane contains widgets for viewing and editing the
	 * name, description, notes, and other simple properties of
	 * <code>StoryComponents</code>.<br>
	 * <br>
	 * Subclasses may override this method to customise the look of the
	 * Description Pane for their own specific purpose.
	 * 
	 * @return The Description Pane for the represented
	 *         <code>StoryComponent</code>.
	 */
	private JScrollPane buildDescriptionPane() {
		final JPanel descriptionPane = new JPanel();
		descriptionPane.setLayout(new BoxLayout(descriptionPane,
				BoxLayout.Y_AXIS));
		final KeywordEditor nameEditor = new KeywordEditor();

		nameEditor.setEditedComponent(this.represented);
		nameEditor.setMaximumSize(nameEditor.getPreferredSize());

		descriptionPane.add(new JLabel("Name: "));
		descriptionPane.add(nameEditor);

		if (this.represented instanceof ScriptIt) {

		}

		return new JScrollPane(descriptionPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	/**
	 * Builds the collection of JPanels that are set up to view/edit the special
	 * properties of the represented object.<br>
	 * <br>
	 * 
	 * @return A collection of speciality panes.
	 */
	private Collection<SpecialPaneInfo> buildSpecialtyPanes() {
		final SpecialPaneBuilder builder = new SpecialPaneBuilder();

		this.represented.process(builder);

		return builder.getSpecialPanes();
	}

	/**
	 * Builds a pane that contains widgets for viewing and editing the current
	 * binding of a parameter.
	 * 
	 * @return A parameter binder
	 */
	private JPanel buildParameterBinder(KnowIt parameter) {
		JPanel parameterPane = new JPanel();

		// TODO: refactor this JLabel text and text updater when the binder pane
		// is properly rebuilt. - remiller
		final JLabel currentBinding = new JLabel("Binding: "
				+ parameter.getBinding().toString());
		JButton bind = new JButton(
				BindParameterAction.deriveActionForParameter(parameter));

		// observer to update the label when the parameter binding changes
		StoryComponentObserver observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				StoryComponentChangeEnum type = event.getType();

				if (type == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
					currentBinding.setText("Binding: "
							+ ((KnowIt) event.getSource()).getBinding()
									.toString());
				}
			}
		};

		parameter.addStoryComponentObserver(observer);

		parameterPane.add(currentBinding);
		parameterPane.add(bind);

		return parameterPane;
	}

	private JComponent buildDoItPane() {
		final JPanel pane = new JPanel();
		return pane;
	}

	/**
	 * Extremely simple container class that just pairs together data for the
	 * pane to be added as a tab, and the tab name for that pane.
	 * 
	 * @author remiller
	 */
	private class SpecialPaneInfo {
		public String tabName;
		public JComponent panel;

		public SpecialPaneInfo(String name, JComponent pane) {
			this.tabName = name;
			this.panel = pane;
		}
	}

	/**
	 * Builds special panes as needed by various Story Component classes.
	 * 
	 * @author remiller
	 */
	private class SpecialPaneBuilder extends AbstractNoOpStoryVisitor {
		private final List<SpecialPaneInfo> panels = new ArrayList<SpecialPaneInfo>();

		@Override
		public void processKnowIt(KnowIt knowIt) {
			// final JComponent knowItPane;
			// knowItPane = DefaultPatternPaneFactory.this.buildKnowItPane();
			//
			// this.panels.add(new SpecialPaneInfo("KnowIt", knowItPane));
		}

		@Override
		public void processScriptIt(ScriptIt doIt) {
			final JComponent doItPane;
			doItPane = DefaultPatternPaneFactory.this.buildDoItPane();

			this.panels.add(new SpecialPaneInfo("DoIt", doItPane));
		}

		public Collection<SpecialPaneInfo> getSpecialPanes() {
			return this.panels;
		}
	}
}
