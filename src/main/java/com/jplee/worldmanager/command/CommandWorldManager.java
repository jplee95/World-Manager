package com.jplee.worldmanager.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jplee.worldmanager.WorldManager;
import com.jplee.worldmanager.gen.WorldGeneration;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class CommandWorldManager extends CommandBase {

	@Override
	public String getCommandName() {
		return "worldmanager";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
	@Override
	public List<String> getCommandAliases() {
		
		return Arrays.<String>asList(new String[] { "wm" });
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.wm.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length > 0) {
			if(args[0].equals("processchunk") && args.length == 3) {
				String[] pos = { args[1], "0", args[2] };
				BlockPos blockPos = parseBlockPos(sender, pos, 0, false);
				World world = sender.getEntityWorld();
				if(!world.isBlockLoaded(blockPos)) {
					throw new CommandException("commands.wm.processchunk.outofworld", new Object[0]);
				} else {
					int chunkX = Math.floorDiv(blockPos.getX(), 16);
					int chunkZ = Math.floorDiv(blockPos.getZ(), 16);
					WorldGeneration.instance.runProcessChunk(world, new ChunkPos(chunkX, chunkZ));
					notifyCommandListener(sender, this, "commands.wm.processchunk.complete", new Object[0]);
				}
			} else if(args[0].equals("reloadconfig") && args.length == 1) {
				WorldManager.reloadConfig();
				notifyCommandListener(sender, this, "commands.wm.reloadconfig.complete", new Object[0]);
			} else {
				notifyCommandListener(sender, this, "commands.wm.usage", new Object[0]);
			}
		} else {
			notifyCommandListener(sender, this, "commands.wm.usage", new Object[0]);
		}

	}
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, "processchunk", "reloadconfig") : Collections.<String>emptyList();
	}
}
