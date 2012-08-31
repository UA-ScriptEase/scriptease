package scriptease.controller;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collection;

public class ContainerCollector {
	public Collection<Component> getAllComponents(Container container) {
		Collection<Component> allComponents = new ArrayList<Component>();
		Component[] components = container.getComponents();
		for (Component component : components) {
			allComponents.add(component);
			if (component instanceof Container)
				allComponents.addAll(this.getAllComponents((Container) component));
		}
		return allComponents;
	}

}
