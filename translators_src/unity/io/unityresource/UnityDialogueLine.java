package io.unityresource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.Resource;

public class UnityDialogueLine extends DialogueLine {

	public UnityDialogueLine() {
		this("", true, null, null, new ArrayList<DialogueLine>());
	}

	public UnityDialogueLine(String dialogue, boolean enabled, Resource image,
			Resource audio, List<DialogueLine> children) {
		super(dialogue, enabled, image, audio, children);
	}

	@Override
	public Collection<String> getTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTemplateID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCodeText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return obj == this;
	}

}
