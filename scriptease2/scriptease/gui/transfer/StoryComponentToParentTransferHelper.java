package scriptease.gui.transfer;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;

import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Handles StoryComponentPanels that are being dragged over other
 * StoryComponentPanels in cases where they should instead be redirected to the
 * parent panel.
 * 
 * @author jyuen
 */
public class StoryComponentToParentTransferHelper {

	// Singleton instance of the TransferHandler.
	private static final StoryComponentToParentTransferHelper instance = new StoryComponentToParentTransferHelper();;

	public static StoryComponentToParentTransferHelper getInstance() {
		return instance;
	}

	private StoryComponentToParentTransferHelper() {
	}

	/**
	 * Checks whether the data being transfered can be redirected to a parent
	 * StoryComponentContainer - such as effects, descriptions, and controls.
	 * 
	 * For Example, it wouldn't make sense for effect 1 being dragged over
	 * effect 2 to be imported into effect 2, but it would make sense for it to
	 * default into the parent block of effect 2 (the cause block) as a sibling.
	 * 
	 * @param support
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean canImportToParent(
			TransferHandler.TransferSupport support) {

		if (support.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {

			// If the destination panel isn't even in a StoryComponentPanel
			// we shouldn't be dragging stuff there anyway.
			Component destinationPanel = support.getComponent();
			while (!(destinationPanel instanceof StoryComponentPanel) && destinationPanel != null) {
				destinationPanel = destinationPanel.getParent();
			}
			if (destinationPanel == null)
				return false;

			StoryComponent storyComponent = ((StoryComponentPanel) destinationPanel)
					.getStoryComponent();

			// Make sure the panel we're dropping it in is a StoryComponentContainer panel
			// Or else try the parent.
			if (!(storyComponent instanceof StoryComponentContainer)) {
				destinationPanel = destinationPanel.getParent();
				storyComponent = ((StoryComponentPanel) destinationPanel)
						.getStoryComponent();
				if (!(storyComponent instanceof StoryComponentContainer))
					return false;
			}

			// Finally, check whether we have a valid component.
			try {
				final Collection<StoryComponent> components;

				components = (Collection<StoryComponent>) support
						.getTransferable()
						.getTransferData(
								StoryComponentPanelTransferHandler.storyCompFlavour);

				// Check if its one of the acceptable components.
				for (StoryComponent component : components)
					if ((component instanceof ScriptIt && !(component instanceof CauseIt))
							|| (component instanceof KnowIt)
							|| component instanceof AskIt 
							|| component instanceof ControlIt) {
						
						if (component instanceof ScriptIt) {
							final SEModel model = SEModelManager.getInstance().getActiveModel();
							
							for (CodeBlock codeBlock : ((ScriptIt) component).getCodeBlocks()) {
								final LibraryModel library = codeBlock.getLibrary();

								if (!(model instanceof LibraryModel && model == library)
										&& !(model instanceof StoryModel && ((StoryModel) model)
												.getLibraries().contains(library)))
									return false;
							}
						}
						
						return true;
					}

			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * We handle components that are being dragged over other components by
	 * putting them where they actually belong (and where the user intended them
	 * to go) - in this case the StoryComponentContainer.
	 * 
	 * @param support
	 */
	@SuppressWarnings("unchecked")
	public boolean importToParent(TransferSupport support) {
		final Collection<StoryComponent> components;
		final StoryComponent component;
		final ComplexStoryComponent parent;
		final int insertionIndex;

		if (!this.canImportToParent(support))
			return false;
		
		Component panel;
		panel = support.getComponent();

		// Get the first instance of a StoryComponentPanel if there is one.
		while (!(panel instanceof StoryComponentPanel) && panel != null) {
			panel = panel.getParent();
		}

		if (panel == null)
			return false;

		component = ((StoryComponentPanel) panel).getStoryComponent();

		// Check if the component already is a container. If it is then
		// the parent is just this container. Otherwise, we want to
		// get the parent StoryComponentContainer.
		if (component instanceof StoryComponentContainer) {
			parent = (ComplexStoryComponent) ((StoryComponentPanel) panel)
					.getStoryComponent();
			insertionIndex = StoryComponentToParentTransferHelper.getInstance()
					.getInsertionIndex((StoryComponentPanel) panel, support);
		} else {
			final StoryComponentPanel parentPanel = (StoryComponentPanel) panel
					.getParent();

			parent = (ComplexStoryComponent) parentPanel.getStoryComponent();
			insertionIndex = StoryComponentToParentTransferHelper.getInstance()
					.getParentInsertionIndex((StoryComponentPanel) parentPanel,
							(StoryComponentPanel) panel, support);
		}

		// Now we actually add the transfer data
		try {
			components = (Collection<StoryComponent>) support
					.getTransferable()
					.getTransferData(
							StoryComponentPanelTransferHandler.storyCompFlavour);

			for (StoryComponent newChild : components) {
				StoryComponentToParentTransferHelper.getInstance()
						.addTransferData(newChild, parent, insertionIndex);
			}
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Determines where the transfer will be inserting into.
	 * 
	 * @param support
	 * @param panel
	 * @return
	 */
	public int getInsertionIndex(StoryComponentPanel panel,
			TransferSupport support) {
		final StoryComponent parent = panel.getStoryComponent();
		final Point mouseLocation = support.getDropLocation().getDropPoint();

		// if the mouse is within the panel's boundaries
		if (mouseLocation != null && parent instanceof ComplexStoryComponent) {
			double yMouseLocation = mouseLocation.getY();
			if (((ComplexStoryComponent) parent).getChildCount() > 0) {
				final StoryComponentPanel closest;
				closest = this.findClosestChildPanel(yMouseLocation, panel);
				if (closest != null) {
					final StoryComponent child = closest.getStoryComponent();
					return ((ComplexStoryComponent) parent)
							.getChildIndex(child) + 1;
				}
			}
		}
		return 0;
	}

	/**
	 * Determines where the transfer will be inserting to in the parent panel if
	 * the TransferSupport is for a child panel.
	 * 
	 * @param parentPanel
	 * @param componentPanel
	 * @param support
	 * @return
	 */
	public int getParentInsertionIndex(StoryComponentPanel parentPanel,
			StoryComponentPanel componentPanel, TransferSupport support) {
		final StoryComponent parent = parentPanel.getStoryComponent();
		final StoryComponent component = componentPanel.getStoryComponent();

		double yLocation = support.getDropLocation().getDropPoint().getY();

		if (((ComplexStoryComponent) parent).getChildCount() > 0) {
			final Collection<StoryComponentPanel> children = parentPanel
					.getChildrenPanels();
			final StoryComponentPanel closest;

			for (StoryComponentPanel child : children) {
				if (component == child.getStoryComponent())
					yLocation += child.getLocation().getY();
			}

			closest = this.findClosestChildPanel(yLocation, parentPanel);
			if (closest != null) {
				final StoryComponent child = closest.getStoryComponent();
				return ((ComplexStoryComponent) parent).getChildIndex(child) + 1;
			}
		}

		return 0;
	}

	/**
	 * Get the closest child StoryComponentPanel to the given parentPanel based
	 * on the given yLocation. Will return null if it is unable to find a child
	 * panel.
	 * 
	 * @param yLocation
	 * @param parentPanel
	 * @return
	 */
	private StoryComponentPanel findClosestChildPanel(double yLocation,
			StoryComponentPanel parentPanel) {
		// tracking variables used to maintain which panel is closest
		StoryComponentPanel closestPanel = null;

		final Collection<StoryComponentPanel> children = parentPanel
				.getChildrenPanels();

		// for each child, check if it is closer than the current closest
		for (StoryComponentPanel child : children) {
			double yChildLocation = child.getLocation().getY();
			if (yChildLocation < yLocation) {
				closestPanel = child;
			}
		}
		return closestPanel;
	}

	/**
	 * Add the transfer data to the parent panel at the provided insertion
	 * index.
	 * 
	 * @param child
	 * @param parent
	 * @param insertionIndex
	 */
	public void addTransferData(StoryComponent child,
			ComplexStoryComponent parent, int insertionIndex) {

		final StoryComponent clone = child.clone();
		final boolean success;

		StoryComponent sibling = parent.getChildAt(insertionIndex);
		if (sibling != null) {
			// add in the middle
			success = parent.addStoryChildBefore(clone, sibling);
		} else {
			success = parent.addStoryChild(clone);
		}

		clone.revalidateKnowItBindings();

		if (!success)
			throw new IllegalStateException("Was unable to add " + child
					+ " to " + parent
					+ ". This should have been prevented by canImport.");
	}
}
