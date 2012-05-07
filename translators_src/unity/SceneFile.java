import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameObject;


public final class SceneFile implements GameModule {
	// key for the game directory path property in the translator definition
	private static final String GAME_DIRECTORY_KEY = "GAME_DIRECTORY";

	// Translator name to be used with the TranslatorManager lookup
	public static final String UNITY_3D = "Unity";
	
	
	@Override
	public void addGameObject(GameObject object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIncludeFiles(Collection<File> scriptList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scripts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Set<CodeBlock>> aggregateScripts(
			Collection<StoryComponent> root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<StoryRule> getCodeGenerationRules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GameConstant getInstanceForObjectIdentifier(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameConstant> getInstancesOfType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTestCommand(ProcessBuilder builder)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void load(boolean readOnly) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(boolean compile) throws IOException {
		// TODO Auto-generated method stub
		// TODO: LOL DUNNO HOW TO DO THIS
		
	}

	@Override
	public void setLocation(File location) {
		// TODO Auto-generated method stub
		
	}
	
	public static final class FileIDKey{
		public final int SCENE = 29;
		public final int GAME_OBJECT = 1;
		public final int TRANSFROM = 4;
		public final int RENDER_SETTINGS = 104;
		public final int GAME_MANAGER = 127;
		public final int LIGHT = 108;
	}

}
