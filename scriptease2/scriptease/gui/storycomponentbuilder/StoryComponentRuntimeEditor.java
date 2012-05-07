package scriptease.gui.storycomponentbuilder;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import scriptease.model.StoryComponent;

@SuppressWarnings("serial")
public class StoryComponentRuntimeEditor extends JPanel implements ActionListener{
	private final String OK = "OK";
	private final String CANCEL = "Cancel";
	private final String RUNTIME = "Runtime";
	
	private boolean runtimeStatus;
	
	public StoryComponentRuntimeEditor(StoryComponent storyComp) {
		// TODO Auto-generated constructor stub
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JRadioButton runtimeYes = new JRadioButton(RUNTIME);
			runtimeYes.addActionListener(this);
		JRadioButton runtimeNo = new JRadioButton("Not " + RUNTIME);
			runtimeNo.addActionListener(this);
			
		ButtonGroup radios = new ButtonGroup();
		radios.add(runtimeYes);
		radios.add(runtimeNo);
		runtimeYes.setSelected(true);
		runtimeStatus = true;
		runtimeNo.setSelected(false);
		
		add(new JLabel(RUNTIME + ":"));
		add(runtimeYes);
		add(runtimeNo);
		
		add(Box.createVerticalGlue());
		add(okCanelPanel());
		
	}
	
	public boolean getRuntimeStatus(){
		return runtimeStatus;
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
		
		if(e.getActionCommand().equals(RUNTIME)){
			runtimeStatus = true;
		}
		
		if(e.getActionCommand().equals("Not "+RUNTIME)){
			runtimeStatus = false;
		}
	}
}
