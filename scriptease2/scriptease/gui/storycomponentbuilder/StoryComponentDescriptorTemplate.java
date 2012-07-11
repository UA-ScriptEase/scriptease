package scriptease.gui.storycomponentbuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import scriptease.ScriptEase;
import scriptease.controller.io.FileIO;
import scriptease.gui.storycomponentbuilder.StoryComponentMultiSelector.MultiSelectorContext;
import scriptease.model.StoryComponent;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

@SuppressWarnings("serial")
public abstract class StoryComponentDescriptorTemplate extends JPanel implements
		Observer {
	protected final String NAME = "Name";
	protected final String TYPES = "Types";
	protected final String LABELS = "Labels";
	protected StoryComponent component;
	protected final String VISIBLE = "Visible";
	protected final String VIS_TRUE = "True";
	protected final String VIS_FALSE = "False";

	protected ActionListener ac;
	protected StoryComponentBindingList parameterList;
	protected LabelField gob = new LabelField();
	protected StoryComponentTextField nameField = new StoryComponentTextField(
			true);
	private JComboBox visibleTFListSelection;
	protected final Font labelFont = new Font("Helvetica", Font.BOLD,
			Integer.parseInt(ScriptEase.getInstance().getPreference(
					ScriptEase.FONT_SIZE_KEY)));
	private final String STORY_COMPONENTS = "Story Component Descriptors";
	protected boolean isEditingBinding;
	protected boolean isAnUpdate = false;
	protected ComponentContext editingContx;
	protected SaveCancelPanel savecancelPanel;

	protected StoryComponentMultiSelector bees = new StoryComponentMultiSelector(
			MultiSelectorContext.TYPES);

	public enum ComponentContext {
		BASE, EXTENSION
	}

	public StoryComponentDescriptorTemplate(StoryComponent component,
			ComponentContext contextOfComp) {
		this.component = component;
		editingContx = contextOfComp;
		savecancelPanel = new SaveCancelPanel(editingContx);

		isEditingBinding = false;

		JPanel a = new JPanel();

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(new LineBorder(Color.DARK_GRAY));
		setBorder(new TitledBorder(STORY_COMPONENTS));

		a.setLayout(new SpringLayout());

		String[] labelsDescriptors = { NAME + ":", TYPES + ":", LABELS + ":",
				VISIBLE + ":" };
		boolean textEditable;
		for (int i = 0; i < labelsDescriptors.length - 1; i++) {
			if (i == 0)
				textEditable = true;
			else
				textEditable = false;
			JLabel label = new JLabel(labelsDescriptors[i], JLabel.TRAILING);
			label.setFont(labelFont);
			a.add(label);

			if (textEditable) {
				nameField.addObserver(this);
				label.setLabelFor((JTextField) nameField.getTextField());
				a.add(nameField.getTextField());

			} else {
				if (i == 1) {
					bees.addObserver(this);
					label.setLabelFor(bees.getRootButton());
					a.add(bees.getRootButton());
				}
				if (i == 2) {
					gob.addObserver(this);
					label.setLabelFor(gob.getLabelField());
					a.add(gob.getLabelField());
				}
			}
		}

		String[] o = { VIS_TRUE, VIS_FALSE };
		visibleTFListSelection = new JComboBox(o);
		JLabel visLabel = new JLabel(
				labelsDescriptors[labelsDescriptors.length - 1],
				JLabel.TRAILING);
		visLabel.setFont(labelFont);
		a.add(visLabel);
		visLabel.setLabelFor(visibleTFListSelection);
		a.add(visibleTFListSelection);
		visibleTFListSelection.setMaximumSize(new Dimension(250, 100));

		SpringUtilities.makeCompactGrid(a, labelsDescriptors.length, 2, 6, 6,
				6, 6);
		add(a);
		add(Box.createHorizontalGlue());

	}

	protected abstract void setUpdatableStoryComponent();

	protected abstract void setActionButtonForParameters(ActionListener e);

	public abstract void checkIfUpdatable();

	public abstract boolean saveComponent();

	public abstract void updateComponent();

	public abstract void deleteComponent();

	protected void saveComponentToAPI() {
		Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		activeTranslator.getApiDictionary().getLibrary().add(component);
		File filePath = activeTranslator
				.getPathProperty(Translator.DescriptionKeys.API_DICTIONARY_PATH);
		FileIO.getInstance().writeAPIDictionary(
				activeTranslator.getApiDictionary(), filePath);
	}

	protected StoryComponentMultiSelector getTypeSelector() {
		return bees;
	}

	protected void setLabelField(String label) {
		gob.textField.setText(label);
	}

	public void addSaveCancelOperationListener(ActionListener e) {
		savecancelPanel.addSaveCanelCommans(e);
	}

	public StoryComponent getStoryComponent() {
		return this.component;
	}

	public void setIsEditingBinding(boolean b) {
		isEditingBinding = b;
	}

	public boolean isIsEditingBinding() {
		return isEditingBinding;
	}

	public void setBindingListActionList(ActionListener e) {
		ac = e;
	}

	public boolean getTFSelection() {
		if (visibleTFListSelection.getSelectedIndex() == 1)
			return true;
		return false;
	}

	public void setTFSelection(boolean set) {
		if (set)
			visibleTFListSelection.setSelectedIndex(0);
		else
			visibleTFListSelection.setSelectedIndex(1);
	}

	public void setName(String name) {
		nameField.setText(name);
	}

	protected Font getCurentFont() {
		return labelFont;
	}

	public StoryComponentBindingList getParamList() {
		return parameterList;
	}

	public JButton getSaveOK() {
		return savecancelPanel.getSaveOk();
	}

	public JButton getCancel() {
		return savecancelPanel.getCancel();
	}

	protected String parseReplaceTags(String a) {
		return a;
	}

	protected class LabelField extends Observable implements KeyListener {
		private JTextField textField;

		public LabelField() {
			textField = new JTextField();
			textField.addKeyListener(this);
			textField.setMaximumSize(new Dimension(350, 60));
		}

		public JTextField getLabelField() {
			return textField;
		}

		public String getLabelText() {
			return textField.getText();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			setChanged();
			notifyObservers();
			// TODO Auto-generated method stub

		}

		@Override
		public void keyPressed(KeyEvent e) {
			setChanged();
			notifyObservers();
			// TODO Auto-generated method stub

		}

		@Override
		public void keyReleased(KeyEvent e) {
			setChanged();
			notifyObservers();
			// TODO Auto-generated method stub

		}
	}

	protected class SaveCancelPanel extends JPanel {
		private final String DELETE = "Delete";
		private final String CANCEL = "Cancel";
		private final String SAVE = "Save";
		private final String OK = "Ok";

		private ComponentContext context;

		private JButton Cancel;
		private JButton Ok_Save;
		private JButton Delete;

		public SaveCancelPanel(ComponentContext cont) {
			context = cont;
			Cancel = new JButton(CANCEL);
			Ok_Save = new JButton(getLabel());
			Delete = new JButton(DELETE);

			setLayout(new FlowLayout(FlowLayout.RIGHT));
			if (context == ComponentContext.BASE) {
				add(Delete);
				add(Box.createHorizontalGlue());
			}

			add(Cancel);
			add(Ok_Save);
		}

		private String getLabel() {
			if (context == ComponentContext.BASE)
				return SAVE;
			return OK;
		}

		public void addSaveCanelCommans(ActionListener e) {
			Cancel.addActionListener(e);
			Ok_Save.addActionListener(e);
			if (context == ComponentContext.BASE)
				Delete.addActionListener(e);
		}

		public JButton getSaveOk() {
			return Ok_Save;
		}

		public JButton getCancel() {
			return Cancel;
		}

		public JButton getDelete() {
			return Delete;
		}

	}

}
