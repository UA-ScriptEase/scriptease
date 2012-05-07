package scriptease.gui.storycomponentbuilder;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scriptease.model.StoryComponent;


@SuppressWarnings("serial")
public class StoryComponentConstantEditor extends JPanel implements ActionListener {
	private final String CONSTANT = "Constant";
	private final String OK = "OK";
	private final String CANCEL = "Cancel";
	private JTextField constant;
	//need ref for componant
	
	public StoryComponentConstantEditor(StoryComponent storyComp){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JLabel(CONSTANT + ": "));
		constant = new JTextField();
		add(constant);
		add(Box.createVerticalGlue());
		add(okCanelPanel());
	}
	
	private JPanel okCanelPanel(){
		JPanel a = new JPanel();
		a.setLayout(new FlowLayout(FlowLayout.RIGHT));
		a.add(Box.createHorizontalGlue());
		JButton ok = new JButton(OK);
		ok.addActionListener(this);
		JButton cancel = new JButton(CANCEL);
		cancel.addActionListener(this);
		a.add(cancel);
		a.add(ok);
		return a;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(CANCEL)){
			
		}
		
		if(e.getActionCommand().equals(OK)){
			
		}
	}

}
