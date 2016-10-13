package jplee.worldmanager.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiEventManager {
	
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onGuiOpen(GuiScreenEvent.InitGuiEvent.Pre event) {
		if(event.getGui() instanceof GuiWorldEdit) {
			GuiWorldEdit worldEdit = (GuiWorldEdit) event.getGui();
//			event.getButtonList().add(new GuiButton(5, worldEdit.width / 2 - 100, worldEdit.height / 4 + 12 + 112, "HELLO"));
		}
	}

//	@SubscribeEvent @SideOnly(Side.CLIENT)
//	public void onButtonActionPre(GuiScreenEvent.ActionPerformedEvent.Pre event) {
//		if(event.getGui() instanceof GuiWorldSelection) {
//			GuiWorldSelection worldSelect = (GuiWorldSelection) event.getGui();
//			
//			if(event.getButton().id == 4 && event.isCancelable()) {
//				event.setCanceled(true);
//				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
//				Minecraft.getMinecraft().getSaveLoader().
//				Minecraft.getMinecraft().displayGuiScreen(new GuiWorldEditExt(worldSelect, ""));
//			}
//				
//		}
//	}
}
