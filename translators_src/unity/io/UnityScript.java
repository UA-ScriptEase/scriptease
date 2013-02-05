package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameConstant;
import scriptease.util.StringOp;

public class UnityScript {
	private static final int NAME_RADIX = 36;
	private static final String SCRIPT_EXTENSION = ".js";
	private static final String SCRIPT_META_EXTENSION = ".js.meta";

	private final String code;
	private final String fileName;
	private final String guid;

	private final Scene scene;
	private final UnityObject attachedObject;
	private final UnityObject monoBehaviourObject;
	private final List<PropertyValue> mComponentList;

	private final int idNumber;

	private static int scriptCounter = 0;

	/**
	 * Creates a new Unity Script file.
	 * 
	 * @param scriptInfo
	 * @param scene
	 */
	public UnityScript(final ScriptInfo scriptInfo, final Scene scene) {
		final GameConstant subject;

		subject = scriptInfo.getSubject();

		this.scene = scene;
		this.code = scriptInfo.getCode();
		this.fileName = "se_" + Integer.toString(scriptCounter++, NAME_RADIX)
				+ "_" + StringOp.makeAlphaNumeric(subject.getName());

		this.guid = UnityProject.generateGUID();

		this.attachedObject = this.scene.getObjectByTemplateID(subject
				.getTemplateID());

		int idNumber = 0;
		for (UnityObject object : this.scene.getObjects()) {
			final int objectID = object.getUniqueID();
			if (objectID >= idNumber) {
				idNumber = objectID + 1;
			}
		}

		this.idNumber = idNumber;

		this.monoBehaviourObject = this.buildMonoBehaviourObject();

		final Map<String, PropertyValue> attachedObjectMap;
		final PropertyValue gameObjectValue;

		attachedObjectMap = this.attachedObject.getPropertyMap();
		gameObjectValue = attachedObjectMap.get("GameObject");

		if (gameObjectValue.isMap()) {
			final PropertyValue mapValue;

			mapValue = gameObjectValue.getMap().get("m_Component");

			if (mapValue.isList()) {
				this.mComponentList = mapValue.getList();
			} else {
				throw new IllegalArgumentException(
						"MComponentList not found in " + this.attachedObject);
			}
		} else
			throw new IllegalArgumentException("GameObject not a map in "
					+ this.attachedObject
					+ ". How in the world is this even possible?");

		this.addToScene();

	}

	private void addToScene() {
		this.scene.addObject(this.monoBehaviourObject);

		final Map<String, PropertyValue> firstMap;
		final Map<String, PropertyValue> secondMap;

		firstMap = new HashMap<String, PropertyValue>();
		secondMap = new HashMap<String, PropertyValue>();

		firstMap.put("114", new PropertyValue(secondMap));
		secondMap.put("fileID", new PropertyValue(this.idNumber));

		this.mComponentList.add(new PropertyValue(firstMap));

		// TODO First check if this exists already.
	}

	public void removeFromScene() {
		this.scene.removeObject(this.monoBehaviourObject);

		PropertyValue toBeRemoved = null;

		for (PropertyValue value : this.mComponentList) {
			if (value.isMap()) {
				final Map<String, PropertyValue> firstMap = value.getMap();
				final PropertyValue secondMapValue = firstMap.get("114");

				if (secondMapValue != null && secondMapValue.isMap()) {
					final Map<String, PropertyValue> secondMap = secondMapValue
							.getMap();
					final PropertyValue fileID = secondMap.get("fileID");

					if (fileID != null && fileID.equals(this.idNumber)) {
						// We have found the correct component.
						toBeRemoved = value;
						break;
					}
				}
			}
		}

		if (toBeRemoved != null)
			this.mComponentList.remove(toBeRemoved);
	}

	private UnityObject buildMonoBehaviourObject() {
		final String fileID = "fileID";
		final PropertyValue zeroValue = new PropertyValue(0);

		final UnityObject monoObject;

		final Map<String, PropertyValue> fileIDMap;
		final Map<String, PropertyValue> mGameObjectMap;
		final Map<String, PropertyValue> mScriptMap;
		final Map<String, PropertyValue> propertiesMap;
		final Map<String, PropertyValue> objectMap;

		monoObject = new UnityObject(this.idNumber, UnityObject.UNITY_TAG
				+ "114");

		fileIDMap = new HashMap<String, PropertyValue>();
		mGameObjectMap = new HashMap<String, PropertyValue>();
		mScriptMap = new HashMap<String, PropertyValue>();
		propertiesMap = new HashMap<String, PropertyValue>();
		objectMap = new HashMap<String, PropertyValue>();

		fileIDMap.put(fileID, zeroValue);

		mGameObjectMap.put(fileID,
				new PropertyValue(this.attachedObject.getUniqueID()));

		mScriptMap.put(fileID, new PropertyValue(11500000));
		mScriptMap.put("guid", new PropertyValue(this.guid));
		mScriptMap.put("type", new PropertyValue(1));

		propertiesMap.put("m_ObjectHideFlags", zeroValue);
		propertiesMap.put("m_PrefabParentObject", new PropertyValue(fileIDMap));
		propertiesMap.put("m_PrefabInternal", new PropertyValue(fileIDMap));
		propertiesMap.put("m_GameObject", new PropertyValue(mGameObjectMap));
		propertiesMap.put("m_Enabled", new PropertyValue(1));
		propertiesMap.put("m_EditorHideFlags", zeroValue);
		propertiesMap.put("m_Script", new PropertyValue(mScriptMap));
		propertiesMap.put("m_Name", new PropertyValue(""));

		objectMap.put("MonoBehaviour", new PropertyValue(propertiesMap));

		monoObject.setProperties(objectMap);

		return monoObject;
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

	public static void resetScriptCounter() {
		scriptCounter = 0;
	}
}
