package scriptease.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * Imported from SE1. is this worth keeping, or should we do this sort of thing with Aspects?  
 */
@SuppressWarnings("serial")
public abstract class AutofiringMenuItem extends JMenuItem
{
  public AutofiringMenuItem(String label)
  {
    super(label);
    this.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent event)
      {
        AutofiringMenuItem.this.action();
      }
    });
  }
 
  protected abstract void action();
}
