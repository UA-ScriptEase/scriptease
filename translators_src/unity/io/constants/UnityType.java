package io.constants;

/**
 * A list of types. Note that not all numbers have a valid type, so they may
 * return null. A full list of types can be found at <a href=
 * "http://docs.unity3d.com/Documentation/Manual/ClassIDReference.html">the
 * Unity Manual page.</a> <br>
 * <br>
 * Some types are ScriptEase types. The proper way of creating one is to append
 * SE_ in front. They should not have a type number in their constructor since
 * that is specific to Unity generated types.
 */
public enum UnityType {
	GAMEOBJECT(1, "Game Object"),

	COMPONENT(2, "Component"),

	LEVELGAMEMANAGER(3, "LevelGameManager"),

	TRANSFORM(4, "Transform"),

	TIMEMANAGER(5, "TimeManager"),

	GLOBALGAMEMANAGER(6, "GlobalGameManager"),

	BEHAVIOUR(8, "Behaviour"),

	GAMEMANAGER(9, "GameManager"),

	AUDIOMANAGER(11, "AudioManager"),

	PARTICLEANIMATOR(12, "ParticleAnimator"),

	INPUTMANAGER(13, "InputManager"),

	ELLIPSOIDPARTICLEEMITTER(15, "EllipsoidParticleEmitter"),

	PIPELINE(17, "Pipeline"),

	EDITOREXTENSION(18, "EditorExtension"),

	CAMERA(20, "Camera"),

	MATERIAL(21, "Material"),

	MESHRENDERER(23, "MeshRenderer"),

	RENDERER(25, "Renderer"),

	PARTICLERENDERER(26, "ParticleRenderer"),

	TEXTURE(27, "Texture"),

	TEXTURE2D(28, "Texture2D"),

	SCENE(29, "Scene"),

	RENDERMANAGER(30, "RenderManager"),

	MESHFILTER(33, "MeshFilter"),

	OCCLUSIONPORTAL(41, "OcclusionPortal"),

	MESH(43, "Mesh"),

	SKYBOX(45, "Skybox"),

	QUALITYSETTINGS(47, "QualitySettings"),

	SHADER(48, "Shader"),

	TEXTASSET(49, "TextAsset"),

	NOTIFICATIONMANAGER(52, "NotificationManager"),

	RIGIDBODY(54, "Rigidbody"),

	PHYSICSMANAGER(55, "PhysicsManager"),

	COLLIDER(56, "Collider"),

	JOINT(57, "Joint"),

	HINGEJOINT(59, "HingeJoint"),

	MESHCOLLIDER(64, "MeshCollider"),

	BOXCOLLIDER(65, "BoxCollider"),

	ANIMATIONMANAGER(71, "AnimationManager"),

	ANIMATIONCLIP(74, "AnimationClip"),

	CONSTANTFORCE(75, "ConstantForce"),

	WORLDPARTICLECOLLIDER(76, "WorldParticleCollider"),

	TAGMANAGER(78, "TagManager"),

	AUDIOLISTENER(81, "AudioListener"),

	AUDIOSOURCE(82, "AudioSource"),

	AUDIOCLIP(83, "AudioClip"),

	RENDERTEXTURE(84, "RenderTexture"),

	MESHPARTICLEEMITTER(87, "MeshParticleEmitter"),

	PARTICLEEMITTER(88, "ParticleEmitter"),

	CUBEMAP(89, "Cubemap"),

	GUILAYER(92, "GUILayer"),

	SCRIPTMAPPER(94, "ScriptMapper"),

	TRAILRENDERER(96, "TrailRenderer"),

	DELAYEDCALLMANAGER(98, "DelayedCallManager"),

	TEXTMESH(102, "TextMesh"),

	RENDERSETTINGS(104, "RenderSettings"),

	LIGHT(108, "Light"),

	CGPROGRAM(109, "CGProgram"),

	ANIMATION(111, "Animation"),

	MONOBEHAVIOUR(114, "MonoBehaviour"),

	MONOSCRIPT(115, "MonoScript"),

	MONOMANAGER(116, "MonoManager"),

	TEXTURE3D(117, "Texture3D"),

	PROJECTOR(119, "Projector"),

	LINERENDERER(120, "LineRenderer"),

	FLARE(121, "Flare"),

	HALO(122, "Halo"),

	LENSFLARE(123, "LensFlare"),

	FLARELAYER(124, "FlareLayer"),

	HALOLAYER(125, "HaloLayer"),

	NAVMESHLAYERS(126, "NavMeshLayers"),

	HALOMANAGER(127, "HaloManager"),

	FONT(128, "Font"),

	PLAYERSETTINGS(129, "PlayerSettings"),

	NAMEDOBJECT(130, "NamedObject"),

	GUITEXTURE(131, "GUITexture"),

	GUITEXT(132, "GUIText"),

	GUIELEMENT(133, "GUIElement"),

	PHYSICMATERIAL(134, "PhysicMaterial"),

	SPHERECOLLIDER(135, "SphereCollider"),

	CAPSULECOLLIDER(136, "CapsuleCollider"),

	SKINNEDMESHRENDERER(137, "SkinnedMeshRenderer"),

	FIXEDJOINT(138, "FixedJoint"),

	RAYCASTCOLLIDER(140, "RaycastCollider"),

	BUILDSETTINGS(141, "BuildSettings"),

	ASSETBUNDLE(142, "AssetBundle"),

	CHARACTERCONTROLLER(143, "CharacterController"),

	CHARACTERJOINT(144, "CharacterJoint"),

	SPRINGJOINT(145, "SpringJoint"),

	WHEELCOLLIDER(146, "WheelCollider"),

	RESOURCEMANAGER(147, "ResourceManager"),

	NETWORKVIEW(148, "NetworkView"),

	NETWORKMANAGER(149, "NetworkManager"),

	PRELOADEDDATA(150, "PreloadedData"),

	MOVIETEXTURE(152, "MovieTexture"),

	CONFIGURABLEJOINT(153, "ConfigurableJoint"),

	TERRAINCOLLIDER(154, "TerrainCollider"),

	MASTERSERVERINTERFACE(155, "MasterServerInterface"),

	TERRAINDATA(156, "TerrainData"),

	LIGHTMAPSETTINGS(157, "LightmapSettings"),

	WEBCAMTEXTURE(158, "WebCamTexture"),

	EDITORSETTINGS(159, "EditorSettings"),

	INTERACTIVECLOTH(160, "InteractiveCloth"),

	CLOTHRENDERER(161, "ClothRenderer"),

	SKINNEDCLOTH(163, "SkinnedCloth"),

	AUDIOREVERBFILTER(164, "AudioReverbFilter"),

	AUDIOHIGHPASSFILTER(165, "AudioHighPassFilter"),

	AUDIOCHORUSFILTER(166, "AudioChorusFilter"),

	AUDIOREVERBZONE(167, "AudioReverbZone"),

	AUDIOECHOFILTER(168, "AudioEchoFilter"),

	AUDIOLOWPASSFILTER(169, "AudioLowPassFilter"),

	AUDIODISTORTIONFILTER(170, "AudioDistortionFilter"),

	AUDIOBEHAVIOUR(180, "AudioBehaviour"),

	AUDIOFILTER(181, "AudioFilter"),

	WINDZONE(182, "WindZone"),

	CLOTH(183, "Cloth"),

	SUBSTANCEARCHIVE(184, "SubstanceArchive"),

	PROCEDURALMATERIAL(185, "ProceduralMaterial"),

	PROCEDURALTEXTURE(186, "ProceduralTexture"),

	OFFMESHLINK(191, "OffMeshLink"),

	OCCLUSIONAREA(192, "OcclusionArea"),

	TREE(193, "Tree"),

	NAVMESH(194, "NavMesh"),

	NAVMESHAGENT(195, "NavMeshAgent"),

	NAVMESHSETTINGS(196, "NavMeshSettings"),

	LIGHTPROBECLOUD(197, "LightProbeCloud"),

	PARTICLESYSTEM(198, "ParticleSystem"),

	PARTICLESYSTEMRENDERER(199, "ParticleSystemRenderer"),

	LODGROUP(205, "LODGroup"),

	LIGHTPROBEGROUP(220, "LightProbeGroup"),

	PREFAB(1001, "Prefab"),

	EDITOREXTENSIONIMPL(1002, "EditorExtensionImpl"),

	ASSETIMPORTER(1003, "AssetImporter"),

	ASSETDATABASE(1004, "AssetDatabase"),

	MESH3DSIMPORTER(1005, "Mesh3DSImporter"),

	TEXTUREIMPORTER(1006, "TextureImporter"),

	SHADERIMPORTER(1007, "ShaderImporter"),

	AUDIOIMPORTER(1020, "AudioImporter"),

	HIERARCHYSTATE(1026, "HierarchyState"),

	GUIDSERIALIZER(1027, "GUIDSerializer"),

	ASSETMETADATA(1028, "AssetMetaData"),

	DEFAULTASSET(1029, "DefaultAsset"),

	DEFAULTIMPORTER(1030, "DefaultImporter"),

	TEXTSCRIPTIMPORTER(1031, "TextScriptImporter"),

	NATIVEFORMATIMPORTER(1034, "NativeFormatImporter"),

	MONOIMPORTER(1035, "MonoImporter"),

	ASSETSERVERCACHE(1037, "AssetServerCache"),

	LIBRARYASSETIMPORTER(1038, "LibraryAssetImporter"),

	MODELIMPORTER(1040, "ModelImporter"),

	FBXIMPORTER(1041, "FBXImporter"),

	TRUETYPEFONTIMPORTER(1042, "TrueTypeFontImporter"),

	MOVIEIMPORTER(1044, "MovieImporter"),

	EDITORBUILDSETTINGS(1045, "EditorBuildSettings"),

	DDSIMPORTER(1046, "DDSImporter"),

	INSPECTOREXPANDEDSTATE(1048, "InspectorExpandedState"),

	ANNOTATIONMANAGER(1049, "AnnotationManager"),

	MONOASSEMBLYIMPORTER(1050, "MonoAssemblyImporter"),

	EDITORUSERBUILDSETTINGS(1051, "EditorUserBuildSettings"),

	PVRIMPORTER(1052, "PVRImporter"),

	SUBSTANCEIMPORTER(1112, "SubstanceImporter"),

	SE_ANIMATION("Animation"),

	SE_AUDIO("Audio"),

	SE_IMAGE("Image"),

	SE_GUISKIN("GUISkin"),

	SE_DIALOGUE("Dialogue"),

	SE_DIALOGUELINE("Dialogue Line");
	
	public static final int SCRIPTEASE_TYPE = 0;

	private final String name;
	private final int id;

	/**
	 * Creates a new ScriptEase UnityType with the name. ScriptEase types have
	 * an invalid ID number that should not be accessed.
	 * 
	 * @param name
	 */
	private UnityType(String name) {
		this.name = name;
		this.id = SCRIPTEASE_TYPE;
	}

	/**
	 * Creates a new UnityType with the passed in id and name.
	 * 
	 * @param id
	 * @param name
	 */
	private UnityType(int id, String name) {
		this.name = name;
		this.id = id;
	}

	/**
	 * Returns the type that corresponds to the id. Throws an
	 * {@link IllegalArgumentException} if the type is not valid in Unity, since
	 * type numbers are not unique for ScriptEase types. Returns null if no type
	 * found.
	 * 
	 * @param id
	 * @return
	 */
	public static UnityType getTypeForID(int id) {
		if (id == SCRIPTEASE_TYPE)
			throw new IllegalArgumentException(
					"Cannot get a ScriptEase type for its id as they are not unique.");

		for (UnityType type : UnityType.values()) {
			if (type.getID() == id)
				return type;
		}

		return null;
	}

	/**
	 * Returns the type with the name. Returns null if no type found.
	 * 
	 * @param name
	 * @return
	 */
	public static UnityType getTypeForName(String name) {
		for (UnityType type : UnityType.values()) {
			if (type.getName().equals(name))
				return type;
		}

		return null;
	}

	/**
	 * Returns the properly formatted name of the type.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the id number of the type.
	 * 
	 * @return
	 */
	public int getID() {
		return this.id;
	}
}
