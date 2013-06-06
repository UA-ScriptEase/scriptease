package io.unityresource;

import io.constants.UnityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.Resource;

public class UnityDialogueLine extends DialogueLine {
	private static Collection<String> audioTypes = new ArrayList<String>();
	private static Collection<String> imageTypes = new ArrayList<String>();

	static {
		// TODO audio types
		imageTypes.add(UnityType.SE_IMAGE.getName());
	}

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

	@Override
	public Collection<String> getAudioTypes() {
		return UnityDialogueLine.audioTypes;
	}

	@Override
	public Collection<String> getImageTypes() {
		return UnityDialogueLine.imageTypes;
	}

}
