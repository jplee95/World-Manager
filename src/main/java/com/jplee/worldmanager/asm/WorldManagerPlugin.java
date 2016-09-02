package com.jplee.worldmanager.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.10.2")
@IFMLLoadingPlugin.TransformerExclusions({ "com.jplee.worldmanager.asm" })
public class WorldManagerPlugin implements IFMLLoadingPlugin {

	public static boolean isObf;
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "com.jplee.worldmanager.asm.WorldManagerClassTransformer" };
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
		isObf = (Boolean) data.get("funtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
