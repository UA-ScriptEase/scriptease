package scriptease.controller;

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;

/**
 * A factory that builds observers. Usually we can just create observers
 * directly in the class they are needed, but sometimes we have to store them
 * somewhere so that they do not get garbage collected. In order to avoid a
 * situation like <b>The Great 2012 ScriptEase Memory Leak</b>, we register
 * observers here and provide some way of deregistering them.
 * 
 * @author kschenk
 * 
 */
public class ObserverFactory {

	private static ObserverFactory instance = new ObserverFactory();

	/**
	 * Returns the sole instance of ObserverFactory.
	 * 
	 * @return
	 */
	public static ObserverFactory getInstance() {
		return instance;
	}

	private ObserverFactory() {
		//Empty private constructor
	}

	/**
	 * Builds an observer for a Name Label. This observer will update the text
	 * on the label based on what the text on the Story Component was updated
	 * to.
	 * 
	 * @param label
	 * @return
	 */
	public StoryComponentObserver buildNameLabelObserver(final JLabel label) {

		StoryComponentObserver observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				// only update the name for now, but if anything else is
				// needed later, it should be added here. - remiller
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					label.setText(event.getSource().getDisplayText());
				}
			}
		};

		return observer;
	}
}
