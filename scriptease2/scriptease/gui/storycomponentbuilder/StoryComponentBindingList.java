package scriptease.gui.storycomponentbuilder;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import scriptease.gui.storycomponentbuilder.propertypanel.BindingPanel;
import scriptease.gui.storycomponentbuilder.propertypanel.ParameterPanel;
import scriptease.gui.storycomponentbuilder.propertypanel.StoryComponentPropertyPanel;
import scriptease.model.atomic.KnowIt;

/**
 * This class appears to get the various bindings for a story component. In the
 * SCB, this is the list of "+" buttons that appears when you press "add". They
 * don't actually do anything, which shouldn't be suprising if you've looked at
 * this class.
 * 
 * @author kschenk - Don't blame me for the code right now, though. I'm editing
 *         it.
 * 
 */
@SuppressWarnings("serial")
public class StoryComponentBindingList extends JPanel implements ActionListener {
	/*
	 * TODO Fix this class so it actually does something, and change the
	 * description to be less snarky.
	 */
	private final String ADD_BUTT = "+";
	private BindingContext contextOfParams;
	private JPanel parameterBindingPane;

	private ArrayList<StoryComponentPropertyPanel> bindings;
	// /lets deal with you tommrow....
	private ArrayList<StoryComponentPropertyPanel> knowItBindings;
	private ActionListener splitpaneActionListner;

	// deal with empty bindings, as well as lets give them some unique name for
	// teh time being

	public static enum BindingContext {
		PARAMETER, BINDING
	}

	public ArrayList<StoryComponentPropertyPanel> getBindings() {
		return bindings;
	}

	public StoryComponentBindingList(BindingContext context) {
		knowItBindings = new ArrayList<StoryComponentPropertyPanel>();

		JButton addButton = new JButton(ADD_BUTT);
		addButton.addActionListener(this);
		contextOfParams = context;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		bindings = new ArrayList<StoryComponentPropertyPanel>();

		add(new JLabel(getLabelTag()));
		add(addButton);
		add(createScrollableBar());

	}

	public void setActionListener(ActionListener e) {
		splitpaneActionListner = e;
	}

	private JScrollPane createScrollableBar() {
		parameterBindingPane = new JPanel();
		JScrollPane scroller = new JScrollPane(parameterBindingPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		parameterBindingPane.setLayout(new BoxLayout(parameterBindingPane,
				BoxLayout.Y_AXIS));

		parameterBindingPane.setMinimumSize(new Dimension(400, 500));
		scroller.setMinimumSize(new Dimension(400, 500));

		return scroller;
	}

	private String getLabelTag() {
		if (contextOfParams == BindingContext.PARAMETER)
			return "Parameters";
		return "Bindings";
	}

	public void addBindingParam(StoryComponentPropertyPanel addBinding) {
		// parameterBindingPane.add(addBinding);
		bindings.add(addBinding);
		parameterBindingPane.add(addBinding);
		parameterBindingPane.repaint();
		parameterBindingPane.revalidate();
	}

	public void removeBindingParam(StoryComponentPropertyPanel removeBinding) {
		bindings.remove(removeBinding);
		parameterBindingPane.remove(removeBinding);
		parameterBindingPane.repaint();
		parameterBindingPane.revalidate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ADD_BUTT)) {
			switch (contextOfParams) {
			case PARAMETER:
				ParameterPanel a = new ParameterPanel(new KnowIt(""));
				a.setButtonAction(splitpaneActionListner, null);
				addBindingParam(a);
				break;
			case BINDING:
				Integer i = knowItBindings.size();
				knowItBindings.add(new BindingPanel(new KnowIt(Integer
						.toString(i))));
				knowItBindings.get(knowItBindings.size() - 1).setButtonAction(
						splitpaneActionListner, null);
				addBindingParam(knowItBindings.get(knowItBindings.size() - 1));
				break;
			default:
				break;
			}
		}
	}

	public void removeBindings() {
		bindings.removeAll(bindings);
		parameterBindingPane.removeAll();
	}

	public void updateBindingList(Collection<KnowIt> parameters) {
		switch (contextOfParams) {
		case PARAMETER:
			if (bindings.size() > 0)
				removeBindings();

			for (KnowIt parameter : parameters) {
				ParameterPanel a = new ParameterPanel(parameter);
				// not sure why you need to specify a parent, doesn't appear to
				// be used. -mfchurch
				a.setButtonAction(splitpaneActionListner, null);
				addBindingParam(a);
			}

			break;
		case BINDING:
			// misleading, can actually build both...
			// for(int i=0; i < comp.getParameters().size(); i++){
			// for(int i=0; i < ((KnowIt)comp).getBindings().size(); i++){

			// ParameterPanel a = new ParameterPanel(
			// (StoryComponent)((KnowIt)comp).getBinding() );
			// System.out.println("HUH1?");

			// a.setButtonAction(splitpaneActionListner,comp);
			// System.out.println("HUH2?");
			// addBindingParam(a);
			// }

			break;

		default:
			break;
		}
	}

}
