package jplee.worldmanager.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiWorldOptions extends GuiScreen {

	private GuiScreen lastGui;
	private String worldId;
	
	public GuiWorldOptions(GuiScreen lastGui, String worldId) {
		this.lastGui = lastGui;
		this.worldId = worldId;
	}
	
	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel", new Object[0])));
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 0 + 12, I18n.format("wm.selectworld.edit.worldoptions.cleanregistry", new Object[0])));
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == 0) {
			this.mc.displayGuiScreen(this.lastGui);
		} else if(button.id == 1) {
			this.mc.displayGuiScreen(new GuiConfirmClean(this, worldId));
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, I18n.format("wm.selectworld.edit.worldoptions.cleanregistry.warn", new Object[0]), this.width / 2, this.height / 4, 15132160);
		this.drawCenteredString(this.fontRendererObj, I18n.format("wm.selectworld.edit.worldoptions.title", new Object[0]), this.width / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
