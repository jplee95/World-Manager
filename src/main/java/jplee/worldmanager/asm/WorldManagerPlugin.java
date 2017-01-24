package jplee.worldmanager.asm;

import java.io.File;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("World Manager Plugin")
@IFMLLoadingPlugin.TransformerExclusions({ "jplee.worldmanager.asm", "jplee.jlib.util.asm" })
public class WorldManagerPlugin implements IFMLLoadingPlugin {

	public static File mcDir;
	public static boolean isObf;
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "jplee.worldmanager.asm.WorldManagerClassTransformer" };
	}
	
	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		isObf = (Boolean) data.get("runtimeDeobfuscationEnabled");
		mcDir = (File)data.get("mcLocation");
		CodeDefinition.setObfFlag(isObf);
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
