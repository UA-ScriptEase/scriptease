package scriptease.gui.storycomponentbuilder;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.Box;

import scriptease.controller.io.FileIO;
import scriptease.gui.storycomponentbuilder.StoryComponentBindingList.BindingContext;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

@SuppressWarnings("serial")
public class StoryComponentKnowItEditor extends StoryComponentDescriptorTemplate {
	private StoryComponentBindingList bindingList;
	
	public StoryComponentKnowItEditor(KnowIt knowIt, ComponentContext contextOfComp) {
		super(knowIt, contextOfComp);
		bindingList = new StoryComponentBindingList(BindingContext.BINDING);
		add(bindingList);
		
		// TODO: UNCOMMENT
//		if(component.getParameters().size() >1)
//			bindingList.updateBindingList(getStoryComponent());
		
		add(Box.createVerticalGlue());
		add(savecancelPanel);
	}
	
	@Override
	public void checkIfUpdatable() {
		if(component.getDisplayText() != "")
			setUpdatableStoryComponent();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof StoryComponentMultiSelector)
			((KnowIt)component).setTypes((ArrayList<String>)arg);		
		
		
		if(o instanceof LabelField){
			if(component.getLabels().size()>0)
				component.removeLabel( ((ArrayList<String>)component.getLabels()).get(0));
			component.addLabel(((LabelField)o).getLabelText());
		}
		
		if(o instanceof StoryComponentTextField)
			component.setDisplayText(((StoryComponentTextField)o).getNameComp()); 
				
	}

	@Override
	protected void setUpdatableStoryComponent() {
		nameField.setText(component.getDisplayText());
		for(String label : component.getLabels())
			setLabelField(label);
		getTypeSelector().setData((ArrayList<String>)((KnowIt)component).getTypes());
	}

	@Override
	protected void setActionButtonForParameters(ActionListener e) {
		bindingList.setActionListener(e);
	}

	@Override
	public boolean saveComponent() {
		// TODO Auto-generated method stub
		Translator activeTranslator = TranslatorManager.getInstance().getActiveTranslator();
		if (component != null && activeTranslator != null) {
			activeTranslator.getApiDictionary().getLibrary().add(component);
			File filePath = activeTranslator.getPathProperty(Translator.DescriptionKeys.API_DICTIONARY_PATH);

			FileIO.getInstance().writeAPIDictionary(
			activeTranslator.getApiDictionary(), filePath);
				
			return true;
		}
		return false;
	}

	
	@Override
	public void updateComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteComponent() {
		// TODO Auto-generated method stub
		
	}

}
