package jplee.worldmanager.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiWorldOptions extends GuiScreen {

	private GuiScreen lastGui;
	private String worldId;
	
	public GuiWorldOptions(GuiScreen lastGui, String worldId) {
		this.lastGui = lastGui;
		this.worldId = worldId;
	}
	
	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
	}
}
