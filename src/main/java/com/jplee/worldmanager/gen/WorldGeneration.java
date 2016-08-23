package com.jplee.worldmanager.gen;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.primitives.Doubles;
import com.jplee.worldmanager.WorldManager;
import com.jplee.worldmanager.util.Replaceable;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.server.FMLServerHandler;
import scala.xml.dtd.ANY;

public class WorldGeneration {

	public static WorldGeneration instance = new WorldGeneration();
	
	public static final int ANY_DIMENSION = -93578231;
	
	private Multimap<Integer,ChunkPos> unloadedReplacementChunks;
	private Multimap<Integer,ChunkPos> chunksReplacementPending;

	private Map<Integer,Multimap<Block,Replaceable>> modInstalledReplaceables;
	private Map<Integer,Multimap<Block,Replaceable>> sortedReplaceables;

	private WorldGeneration() {
		this.chunksReplacementPending = HashMultimap.create();
		this.unloadedReplacementChunks = HashMultimap.create();
		this.modInstalledReplaceables = Maps.newHashMap();
		this.sortedReplaceables = Maps.newHashMap();
	}

	public void loadReplacables() {
		Map<Integer,Multimap<Block,Replaceable>> replaceables = Maps.newHashMap();
		for(String rep : WorldManager.getReplaceables()) {
			if(!rep.startsWith("#")) {
				Replaceable replaceable = Replaceable.build(rep);
				Block block = replaceable.getBlockFromBlockStateProperty("block");
				int dimension = ANY_DIMENSION;
				if(replaceable.hasProperty("dimension")) {
					dimension = replaceable.getPropertyAsInt("dimension");
				}
				Multimap<Block, Replaceable> blocks = replaceables.get(dimension);
				if(blocks == null) {
					replaceables.put(dimension, HashMultimap.<Block,Replaceable>create());
					blocks = replaceables.get(dimension);
				}
				blocks.put(block, replaceable);
			}
		}
		replaceables.putAll(modInstalledReplaceables);
		
		sortedReplaceables.clear();
		for(int dimension : replaceables.keySet()) {
			for(Block block : replaceables.get(dimension).keys()) {
				List<Replaceable> list = Lists.newArrayList(replaceables.get(dimension).get(block));
				list.sort(replaceableComparable);
				Multimap<Block, Replaceable> blocks = sortedReplaceables.get(dimension);
				if(blocks == null) {
					sortedReplaceables.put(dimension, HashMultimap.<Block,Replaceable>create());
					blocks = sortedReplaceables.get(dimension); 
				}
				blocks.putAll(block, list);
			}
		}
	}

	private static Comparator<Replaceable> replaceableComparable = new Comparator<Replaceable>() {
		@Override public int compare(Replaceable r1, Replaceable r2) {
			return -Doubles.compare(((Double)r1.getProperty("random")), ((Double)r2.getProperty("random")));
		}
	};
	
	public void registerWorldGenerators() {
		GameRegistry.registerWorldGenerator(new GenWorld(), 0);
	}
	
	public void addNewReplaceable(Block block, Replaceable replaceable) {
		int dimension = replaceable.getPropertyAsInt("dimension");
		Multimap<Block, Replaceable> blocks = modInstalledReplaceables.get(dimension);
		if(blocks == null) {
			modInstalledReplaceables.put(dimension, HashMultimap.<Block,Replaceable>create());
			blocks = modInstalledReplaceables.get(dimension); 
		}
		blocks.put(block, replaceable);
	}
	
	public Collection<Replaceable> getReplaceables(int world, Block block) {
		List<Replaceable> rep = Lists.newArrayList();
		rep.addAll(sortedReplaceables.get(ANY_DIMENSION).get(block));
		Multimap<Block, Replaceable> worldReplace = sortedReplaceables.get(world);
		if(worldReplace != null) {
			rep.addAll(worldReplace.get(block));
		}
		return rep;
	}
	
	public boolean hasReplacables() {
		return !sortedReplaceables.isEmpty();
	}

	public void addNewPendingChunk(World world, Chunk chunk) {
		chunksReplacementPending.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public Collection<ChunkPos> getPendingForWorld(World world) {
		return chunksReplacementPending.get(world.provider.getDimension());
	}
	
	public void removePendingForChunk(World world, Chunk chunk) {
		chunksReplacementPending.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public boolean unqueueChunk(World world, Chunk chunk) {
		if(chunksReplacementPending.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair())) {
			removePendingForChunk(world, chunk);
			unloadedReplacementChunks.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			return true;
		}
		return false;
	}
	
	public boolean requeueChunk(World world, Chunk chunk) {
		if(unloadedReplacementChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair())) {
			unloadedReplacementChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			addNewPendingChunk(world, chunk);
			return true;
		}
		return false;
	}
	
	public Collection<ChunkPos> getUnloadedPendingForWorld(World world) {
		return unloadedReplacementChunks.get(world.provider.getDimension());
	}
	
	public boolean isQueuedChunk(World world, Chunk chunk) {
		return isLoadedQueuedChunk(world, chunk) || isUnloadedQueuedChunk(world, chunk);
	}
	
	public boolean isLoadedQueuedChunk(World world, Chunk chunk) {
		return chunksReplacementPending.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public boolean isUnloadedQueuedChunk(World world, Chunk chunk) {
		return unloadedReplacementChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public class GenWorld implements IWorldGenerator {

		@Override
		public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
			chunksReplacementPending.put(world.provider.getDimension(), new ChunkPos(chunkX, chunkZ));
		}
	}
	
	public boolean runProcessChunk(World world, ChunkPos chunkPos) {
		long worldSeed = world.getSeed();
		Random fmlRandom = new Random(worldSeed);
		long xSeed = fmlRandom.nextLong() >> 2 + 1l;
		long zSeed = fmlRandom.nextLong() >> 2 + 1l;
		long chunkSeed = (xSeed * chunkPos.chunkXPos + zSeed * chunkPos.chunkZPos) ^ worldSeed;
		boolean chunkModified = false;

		fmlRandom.setSeed(chunkSeed);
			for(int y = world.getHeight() - 1; y >= 0; y--) {
				for(int x = 0; x < 16; x++) {
					for(int z = 0; z < 16; z++) {
						int posX = chunkPos.chunkXPos * 16 + x;
						int posZ = chunkPos.chunkZPos * 16 + z;
						BlockPos pos = new BlockPos(posX, y, posZ);
						if(this.replacementProcess(world, fmlRandom, pos)) {
							chunkModified = true;
						}
					}
				}
			}
		return chunkModified;
	}
	
	private boolean replacementProcess(World world, Random fmlRandom, BlockPos pos) {
		if(this.hasReplacables()) {
			IBlockState blockState = world.getBlockState(pos);
			int dimension = world.provider.getDimension();
			if(!blockState.getBlock().equals(Blocks.AIR)) {
				Collection<Replaceable> replaceables = getReplaceables(dimension, blockState.getBlock());
				if(replaceables != null) {
					for(Replaceable rep : replaceables) {
						int min = rep.getPropertyAsInt("min");
						int max = rep.getPropertyAsInt("max");
						
						if((min >= pos.getY() || min == -1) && (max <= pos.getY() || max == -1)) {
							double random = (Double) rep.getProperty("random");
							IBlockState replace = rep.getPropertyAsBlockState("replace");
							if(rep.isAdequateState("block", blockState) && (fmlRandom.nextDouble() < random || random == 1.0)) {
	//							WorldManager.info("Replacing %s at (%s %s %s)", blockState.getBlock().getLocalizedName(), pos.getX(), pos.getY(), pos.getZ());
								if(replace != null) {
									world.setBlockState(pos, replace);
								} else {
									world.setBlockToAir(pos);
								}
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}
