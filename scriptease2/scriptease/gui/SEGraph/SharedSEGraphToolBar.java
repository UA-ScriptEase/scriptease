package scriptease.gui.SEGraph;

/**
 * SharedSEGraphToolBar is a extension of {@link SEGraphToolBar}. This is useful
 * in causes such as behaviour graphs so that all graphs may share the same tool
 * instead of having a toolbar for each graph.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class SharedSEGraphToolBar extends SEGraphToolBar {

	/**
	 * The sole instance of this class as per the singleton pattern.
	 */
	private static final SharedSEGraphToolBar instance = new SharedSEGraphToolBar();

	/**
	 * @return the sole instance of WindowManager
	 */
	public static SharedSEGraphToolBar getInstance() {
		return SharedSEGraphToolBar.instance;
	}
	
	private SharedSEGraphToolBar() {
		super(true);
	}
}
