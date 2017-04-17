package jplee.worldmanager.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jplee.worldmanager.manager.GenerationManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class CommandProcessChunk extends CommandBase {

	@Override
	public String getCommandName() {
		return "processchunk";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.wm.processchunk.usage";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList(new String[] { "process" });
	}
	
	@Override
	@SuppressWarnings("unused")
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(true)
			throw new CommandException("Command temperaly disabled", new Object[0]); // TODO: Fix chunk process command

		if(args.length == 2) {
			String[] pos = { args[0], "0", args[1] };
			BlockPos blockPos = parseBlockPos(sender, pos, 0, false);
			World world = sender.getEntityWorld();
			if(!world.isBlockLoaded(blockPos))
				throw new CommandException("commands.wm.processchunk.outofworld", new Object[0]);
			else {
				int chunkX = Math.floorDiv(blockPos.getX(), 16);
				int chunkZ = Math.floorDiv(blockPos.getZ(), 16);
				boolean proccessed  = GenerationManager.instance.processChunk(world, new ChunkPos(chunkX, chunkZ));
				if(proccessed)
					world.playerEntities.forEach(player -> {
						if(player instanceof EntityPlayerMP) {
							EntityPlayerMP playerMp = (EntityPlayerMP) player;
							playerMp.connection.sendPacket(new SPacketChunkData(world.getChunkFromBlockCoords(blockPos), 65535));
						
						}
					});
				notifyCommandListener(sender, this, "commands.wm.processchunk.complete", new Object[0]);
			}
		} else
			throw new CommandException("commands.wm.processchunk.usage", new Object[0]);
	}
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
		BlockPos pos) {
		if(args.length <= 2)
			return getTabCompletionCoordinateXZ(args, 0, pos);
		return Collections.<String>emptyList();
	}
}
