package jplee.worldmanager.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class CommandGetState extends CommandBase {

	@Override
	public String getCommandName() {
		return "getstate";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList(new String[] { "state" });
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.wm.getstate.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			EntityPlayer player = getPlayer(server, sender, sender.getName());
			ItemStack stack = player.getHeldItemMainhand();
			if(stack != null) {
				if(stack.getItem() instanceof ItemBlock) {
					ItemBlock block = (ItemBlock) stack.getItem();
					for(IBlockState state : block.block.getBlockState().getValidStates()) {
						notifyCommandListener(sender, this, state.toString(), new Object[0]);
					}
				} else {
					notifyCommandListener(sender, this, "commands.wm.getstate.notblock", new Object[0]);
				}
			} else {
				notifyCommandListener(sender, this, "commands.wm.getstate.notblock", new Object[0]);
			}
		} else {
			notifyCommandListener(sender, this, "commands.wm.getstate.usage", new Object[0]);
		}
	}

}
