package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

	private final UnityObject attachedObject;
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

		this.code = scriptInfo.getCode();
		this.fileName = "se_" + Integer.toString(scriptCounter++, NAME_RADIX)
				+ "_" + StringOp.makeAlphaNumeric(subject.getName());

		// Create a 32 char random UUID.
		// TODO Check if this UUID is already in use
		this.guid = UUID.randomUUID().toString().replace("-", "");

		this.attachedObject = scene.getObjectByTemplateID(scriptInfo
				.getSubject().getTemplateID());

		int idNumber = 0;
		for (UnityObject object : scene.getObjects()) {
			final int objectID = object.getUniqueID();
			if (objectID >= idNumber) {
				idNumber = objectID + 1;
			}
		}

		this.idNumber = idNumber;

		final UnityObject monoObject;

		monoObject = this.buildMonoBehaviourObject();

		scene.addObject(monoObject);

		// TODO FIXME This is making me sick. Fix it =|
		final Map<String, PropertyValue> attachedObjectMap = this.attachedObject
				.getPropertyMap();

		final PropertyValue gameObjectValue = attachedObjectMap
				.get("GameObject");

		if (gameObjectValue.isMap()) {
			final PropertyValue mapValue = gameObjectValue.getMap()
					.get("m_Component");

			if (mapValue.isList()) {
				final List<PropertyValue> list = mapValue.getList();

				final Map<String, PropertyValue> firstMap = new HashMap<String, PropertyValue>();
				final Map<String, PropertyValue> secondMap = new HashMap<String, PropertyValue>();

				firstMap.put("114", new PropertyValue(secondMap));
				secondMap.put("fileID", new PropertyValue(this.idNumber));

				list.add(new PropertyValue(firstMap));
			}
		}

	}

	private UnityObject buildMonoBehaviourObject() {
		// FIXME: This is HORRIBLY UGLY CODE! REPAIR REPAIR REPAIRRRR!!!!!!
		final UnityObject monoObject;
		final Map<String, PropertyValue> objectMap;
		final Map<String, PropertyValue> propertiesMap;

		monoObject = new UnityObject(this.idNumber, UnityObject.UNITY_TAG
				+ "114");
		objectMap = new HashMap<String, PropertyValue>();
		propertiesMap = new HashMap<String, PropertyValue>();

		objectMap.put("MonoBehaviour", new PropertyValue(propertiesMap));

		propertiesMap.put("m_ObjectHideFlags", new PropertyValue(0));
		final Map<String, PropertyValue> fileID0Map = new HashMap<String, PropertyValue>();
		fileID0Map.put("fileID", new PropertyValue(0));

		propertiesMap.put("m_PrefabParentObject", new PropertyValue(
				fileID0Map));
		propertiesMap.put("m_PrefabInternal", new PropertyValue(fileID0Map));

		final Map<String, PropertyValue> mGameObjectMap = new HashMap<String, PropertyValue>();

		mGameObjectMap.put("fileID",
				new PropertyValue(this.attachedObject.getUniqueID()));

		propertiesMap.put("m_GameObject", new PropertyValue(mGameObjectMap));
		propertiesMap.put("m_Enabled", new PropertyValue(1));
		propertiesMap.put("m_EditorHideFlags", new PropertyValue(0));

		final Map<String, PropertyValue> mScriptMap = new HashMap<String, PropertyValue>();
		mScriptMap.put("fileID", new PropertyValue(11500000));
		mScriptMap.put("guid", new PropertyValue(this.guid));
		mScriptMap.put("type", new PropertyValue(1));

		propertiesMap.put("m_Script", new PropertyValue(mScriptMap));
		propertiesMap.put("m_Name", new PropertyValue(""));

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
