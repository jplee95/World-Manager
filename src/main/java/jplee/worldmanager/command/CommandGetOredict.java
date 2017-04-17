package jplee.worldmanager.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.oredict.OreDictionary;


public class CommandGetOredict extends CommandBase {

	@Override
	public String getCommandName() {
		return "getoredict";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList(new String[] { "ore", "oredict" });
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.wm.getoredict.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer entityplayer = getPlayer(server, sender, sender.getName());
		ItemStack stack = entityplayer.getHeldItemMainhand();
		if(stack != null) {
			for(int id : OreDictionary.getOreIDs(stack)) {
				notifyCommandListener(sender, this, OreDictionary.getOreName(id), new Object[0]);
			}
		} else {
			notifyCommandListener(sender, this, "commands.wm.getoredict.noitem", new Object[0]);
		}
	}

}
