package jplee.worldmanager.gen;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.util.Replaceable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class WorldGeneration {

	public static WorldGeneration instance = new WorldGeneration();
	
	public static final int ANY_DIMENSION = -93578231;
	
	private Multimap<Integer,ChunkPos> unloadedPendingChunks;
	private Multimap<Integer,ChunkPos> loadedPendingChunks;

	private Map<Integer,Multimap<Block,Replaceable>> modInstalledReplaceables;
	private Map<Integer,Multimap<Block,Replaceable>> sortedReplaceables;

	private WorldGeneration() {
		this.loadedPendingChunks = HashMultimap.create();
		this.unloadedPendingChunks = HashMultimap.create();
		this.modInstalledReplaceables = Maps.newHashMap();
		this.sortedReplaceables = Maps.newHashMap();
	}

	public void loadReplacables() {
		Map<Integer,Multimap<Block,Replaceable>> replaceables = Maps.newHashMap();
		for(String rep : WorldManager.getReplaceables()) {
			if(!rep.startsWith("#") && !rep.isEmpty()) {
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
		if(sortedReplaceables.get(ANY_DIMENSION) != null) {
			rep.addAll(sortedReplaceables.get(ANY_DIMENSION).get(block));
		}
		Multimap<Block, Replaceable> worldReplace = sortedReplaceables.get(world);
		if(worldReplace != null) {
			rep.addAll(worldReplace.get(block));
		}
		return rep;
	}
	
	public boolean hasReplacables() {
		return !sortedReplaceables.isEmpty();
	}

	public Collection<ChunkPos> getLoadedPendingForWorld(World world) {
		return loadedPendingChunks.get(world.provider.getDimension());
	}
	
	public Collection<ChunkPos> getUnloadedPendingForWorld(World world) {
		return unloadedPendingChunks.get(world.provider.getDimension());
	}
	
	public void addPendingForWorld(World world, Chunk chunk, boolean loaded) {
		if(loaded) {
			loadedPendingChunks.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		} else {
			unloadedPendingChunks.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		}
	}
	
	public void removePendingForWorld(World world, Chunk chunk, boolean loaded) {
		if(loaded) {
			loadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		} else {
			unloadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		}
		
	}
	
	public boolean unqueueChunk(World world, Chunk chunk) {
		if(loadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair())) {
			loadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			addPendingForWorld(world, chunk, false);
			return true;
		}
		return false;
	}

	public boolean requeueChunk(World world, Chunk chunk) {
		if(unloadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair())) {
			unloadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			addPendingForWorld(world, chunk, true);
			return true;
		}
		return false;
	}
	
	public void clearQueuedChunks() {
		loadedPendingChunks.clear();
		unloadedPendingChunks.clear();
	}
	
	public int getTotalQueuedChunkCount(int dimension) {
		return getLoadedQueuedChunkCount(dimension) + getUnloadedQueuedChunkCount(dimension);
	}
	
	public int getLoadedQueuedChunkCount(int dimension) {
		Collection<ChunkPos> pos = loadedPendingChunks.get(dimension);
		if(pos != null)
			return pos.size();
		return 0;
	}
	
	public int getUnloadedQueuedChunkCount(int dimension) {
		Collection<ChunkPos> pos = unloadedPendingChunks.get(dimension);
		if(pos != null)
			return pos.size();
		return 0;
	}
	
	public boolean isQueuedChunk(World world, Chunk chunk) {
		return isLoadedQueuedChunk(world, chunk) || isUnloadedQueuedChunk(world, chunk);
	}
	
	public boolean isLoadedQueuedChunk(World world, Chunk chunk) {
		return loadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public boolean isUnloadedQueuedChunk(World world, Chunk chunk) {
		return unloadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public class GenWorld implements IWorldGenerator {

		@Override
		public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
			loadedPendingChunks.put(world.provider.getDimension(), new ChunkPos(chunkX, chunkZ));
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
						
						if((min <= pos.getY() || min == -1) && (max >= pos.getY() || max == -1)) {
							double random = (Double) rep.getProperty("random");
							IBlockState replace = rep.getPropertyAsBlockState("replace");
							if(rep.isAdequateState("block", blockState) && (fmlRandom.nextDouble() < random || random == 1.0)) {
	//							WorldManager.info("Replacing %s at (%s %s %s)", blockState.getBlock().getLocalizedName(), pos.getX(), pos.getY(), pos.getZ());
								if(replace != null) {
									world.setBlockState(pos, replace, 2);
								} else {
									world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
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
