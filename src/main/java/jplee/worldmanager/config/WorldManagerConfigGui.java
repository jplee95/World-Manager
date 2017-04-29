package jplee.worldmanager.config;

import java.util.List;

import com.google.common.collect.Lists;

import jplee.worldmanager.WorldManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;


public class WorldManagerConfigGui extends GuiConfig {

	public WorldManagerConfigGui(GuiScreen parentScreen) {
		super(parentScreen, getGuiElements(), WorldManager.MODID, false, false,
			GuiConfig.getAbridgedConfigPath(WorldManager.getBaseConfig().toString()));
	}

	private static List<IConfigElement> getGuiElements() {
		List<IConfigElement> elements = Lists.newArrayList();

		for(String category : WorldManager.getBaseConfig().getCategoryNames()) {
			elements.add(new ConfigElement(WorldManager.getBaseConfig().getCategory(category)));
		}
		
		return elements;
	}
}
