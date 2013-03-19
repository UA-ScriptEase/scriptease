package io;

import io.UnityConstants.UnityField;
import io.UnityConstants.UnityType;
import io.unityobject.PropertyValue;
import io.unityobject.UnityResource;
import io.unityobject.UnityResourceFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.Resource;
import scriptease.util.StringOp;

/**
 * This class represents a Unity Script. When a Script is created, it is
 * attached to a scene automatically and must then be removed via
 * {@link #removeFromScene()}. The Scene does not need to store references to
 * these UnityScripts.
 * 
 * TODO It may be better design to have the Scene store the UnityScripts as a
 * collection of objects. We could have the scene deal with itself, rather than
 * the UnityScripts altering the scene. The Scene would likely have to refresh
 * the GUID each time.
 * 
 * @author kschenk
 * 
 */
public class UnityScript {
	private static final int NAME_RADIX = 36;
	private static final String SCRIPT_EXTENSION = ".js";
	private static final String SCRIPT_META_EXTENSION = ".js.meta";

	private final String code;
	private final String fileName;
	private final String guid;

	private final Scene scene;
	private final UnityResource attachedObject;
	private final UnityResource monoBehaviourObject;
	private final List<PropertyValue> mComponentList;

	private final int idNumber;

	// Added in front of the script name to prevent duplicate identical names
	// from occurring.
	private static int scriptCounter = 0;

	/**
	 * Creates a new Unity Script file from the script info and attaches it to
	 * the passed in scene.
	 * 
	 * @param scriptInfo
	 * @param scene
	 */
	public UnityScript(final ScriptInfo scriptInfo, final Scene scene) {
		final Resource subject;

		subject = scriptInfo.getSubject();

		this.scene = scene;
		this.code = scriptInfo.getCode();
		this.fileName = UnityProject.SCRIPTEASE_FILE_PREFIX
				+ Integer.toString(scriptCounter++, NAME_RADIX) + "_"
				+ StringOp.makeAlphaNumeric(subject.getName());

		this.guid = UnityProject.getActiveProject().generateGUIDForFile(
				new File(this.fileName + SCRIPT_META_EXTENSION));

		this.attachedObject = this.scene.getObjectByTemplateID(subject
				.getTemplateID());

		this.idNumber = this.scene.getNextEmptyID();

		this.monoBehaviourObject = UnityResourceFactory.getInstance()
				.buildMonoBehaviourObject(this.attachedObject.getUniqueID(),
						this.guid, this.idNumber, this.scene);

		final PropertyValue mComponentMapValue;

		mComponentMapValue = this.attachedObject.getPropertyMap().get(
				UnityField.M_COMPONENT.getName());

		if (mComponentMapValue.isList()) {
			this.mComponentList = mComponentMapValue.getList();
		} else {
			throw new IllegalArgumentException("MComponentList not found in "
					+ this.attachedObject);
		}

		this.addToScene();
	}

	/**
	 * Attaches the Script to the scene. This is private and should only be
	 * called in the constructor. I've moved the code into a method since we may
	 * move this type of code to the Scene file itself later...
	 */
	private void addToScene() {
		this.scene.addObject(this.monoBehaviourObject);
		final String fileID = UnityField.FILEID.getName();
		final int fileIDNum = UnityType.MONOBEHAVIOUR.getID();

		final Map<String, PropertyValue> firstMap;
		final Map<String, PropertyValue> secondMap;
		firstMap = new HashMap<String, PropertyValue>();
		secondMap = new HashMap<String, PropertyValue>();

		firstMap.put(String.valueOf(fileIDNum), new PropertyValue(secondMap));
		secondMap.put(fileID, new PropertyValue(this.idNumber));

		this.mComponentList.add(new PropertyValue(firstMap));
	}

	/**
	 * Removes the script from the scene it is attached to. Once removed,
	 * nothing can be done with it.
	 */
	public void removeFromScene() {
		this.scene.removeObject(this.monoBehaviourObject);

		PropertyValue toBeRemoved = null;

		for (PropertyValue value : this.mComponentList) {
			if (value.isMap()) {
				final Map<String, PropertyValue> firstMap = value.getMap();
				final PropertyValue secondMapValue = firstMap.get(String
						.valueOf(UnityType.MONOBEHAVIOUR.getID()));

				if (secondMapValue != null && secondMapValue.isMap()) {
					final Map<String, PropertyValue> secondMap = secondMapValue
							.getMap();
					final PropertyValue fileID;

					fileID = secondMap.get(UnityField.FILEID.getName());

					if (fileID != null && fileID.equals(this.idNumber)) {
						// We have found the correct component.
						toBeRemoved = value;
						break;
					}
				}
			}
		}

		if (toBeRemoved != null) {
			this.mComponentList.remove(toBeRemoved);
		}
	}

	/**
	 * Writes the script file and meta file to the passed in directory.
	 * 
	 * @param directory
	 */
	public void write(File directory) throws IOException {
		final File scriptFile;
		final File metaFile;
		final BufferedWriter scriptWriter;
		final BufferedWriter metaWriter;
		final String metaContents;

		scriptFile = new File(directory, this.fileName + SCRIPT_EXTENSION);
		metaFile = new File(directory, this.fileName + SCRIPT_META_EXTENSION);
		scriptWriter = new BufferedWriter(new FileWriter(scriptFile));
		metaWriter = new BufferedWriter(new FileWriter(metaFile));
		metaContents = "fileFormatVersion: 2\n" + "guid: " + this.guid + "\n"
				+ "MonoImporter:\n" + "  serializedVersion: 2\n"
				+ "  defaultReferences: []\n" + "  executionOrder: 0\n"
				+ "  icon: {instanceID: 0}";

		scriptWriter.write(this.code);
		metaWriter.write(metaContents);

		scriptWriter.close();
		metaWriter.close();
	}

	/**
	 * Reset the script counter.
	 */
	public static void resetScriptCounter() {
		scriptCounter = 0;
	}
}
