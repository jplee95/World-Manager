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
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class WorldGeneration {

	public static WorldGeneration instance = new WorldGeneration();
	
	public static final int ANY_DIMENSION = -93578231;
	
	private Multimap<Integer,ChunkPos> unloadedPendingChunks;
	private Multimap<Integer,ChunkPos> loadedPendingChunks;

	private Map<Integer,Multimap<Block,Replaceable>> modInstalledReplaceables;
	private Map<Integer,Multimap<Block,Replaceable>> sortedReplaceables;
	private Map<Integer,Multimap<String,Replaceable>> oreDictionaries;

	private WorldGeneration() {
		this.loadedPendingChunks = HashMultimap.create();
		this.unloadedPendingChunks = HashMultimap.create();
		this.modInstalledReplaceables = Maps.newHashMap();
		this.oreDictionaries = Maps.newHashMap();
		this.sortedReplaceables = Maps.newHashMap();
	}

	public void loadReplacables() {
		Map<Integer,Multimap<Block,Replaceable>> replaceables = Maps.newHashMap();
		Map<Integer,Multimap<String,Replaceable>> oreReps = Maps.newHashMap();
		for(String rep : WorldManager.getReplaceables()) {
 			if(!rep.startsWith("#") && !rep.isEmpty()) {
				Replaceable replaceable = Replaceable.build(rep);
				int dimension = ANY_DIMENSION;
				if(replaceable.hasProperty("dimension")) {
					dimension = replaceable.getPropertyAsInt("dimension");
				}
				if(replaceable.getPropertyAsBoolean("usingore")) {
					String oreDict = replaceable.getPropertyAsString("oredict");
					Multimap<String,Replaceable> reps = oreDictionaries.get(dimension);
					if(reps == null) {
						oreReps.put(dimension, HashMultimap.<String,Replaceable>create());
						reps = oreReps.get(dimension);
					}
					reps.put(oreDict, replaceable);
				} else {
					Block block = replaceable.getBlockFromBlockStateProperty("block");
					Multimap<Block, Replaceable> blocks = replaceables.get(dimension);
					if(blocks == null) {
						replaceables.put(dimension, HashMultimap.<Block,Replaceable>create());
						blocks = replaceables.get(dimension);
					}
					blocks.put(block, replaceable);
				}
			}
		}
		replaceables.putAll(modInstalledReplaceables);
		
		oreDictionaries.clear();
		for(int dimension : oreReps.keySet()) {
			for(String ore : oreReps.get(dimension).keys()) {
				List<Replaceable> list = Lists.newArrayList(oreReps.get(dimension).get(ore));
				list.sort(replaceableComparable);
				Multimap<String, Replaceable> oreDict = oreDictionaries.get(dimension);
				if(oreDict == null) {
					oreDictionaries.put(dimension, HashMultimap.<String,Replaceable>create());
					oreDict = oreDictionaries.get(dimension); 
				}
				oreDict.putAll(ore, list);
			}
		}
		
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
			return -Doubles.compare(r1.getPropertyAsDouble("random"), r2.getPropertyAsDouble("random"));
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

		ItemStack stack = new ItemStack(block);
		if(stack.getItem() != null) {
			for(int id : OreDictionary.getOreIDs(stack)) {
				String i = OreDictionary.getOreName(id);
				if(oreDictionaries.get(ANY_DIMENSION) != null) {
					rep.addAll(oreDictionaries.get(ANY_DIMENSION).get(i));
				}
				Multimap<String, Replaceable> oreReps = oreDictionaries.get(world);
				if(oreReps != null) {
					rep.addAll(oreReps.get(i));
				}
			}
		}
		
		return rep;
	}
	
	public boolean hasReplacables() {
		return !sortedReplaceables.isEmpty() || !oreDictionaries.isEmpty();
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
						if(this.replacementProcess(world, fmlRandom, pos) && !chunkModified) {
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
							if((fmlRandom.nextDouble() < random || random == 1.0)) {
								if(rep.getPropertyAsBoolean("usingore")) {
									setBlock(world, pos, blockState, replace, rep.getPropertyAsString("loot"));
									return true;
								}
								if(!rep.getPropertyAsBoolean("usingore")) {
									if(rep.isAdequateState("block", blockState)) {
										setBlock(world, pos, blockState, replace, rep.getPropertyAsString("loot"));
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private void setBlock(World world, BlockPos pos, IBlockState old, IBlockState state, String loot) {
		WorldManager.debug("Replacing %s at (%s %s %s)", old.getBlock().getLocalizedName(), pos.getX(), pos.getY(), pos.getZ());
		if(state != null) {
			world.setBlockState(pos, state, 2);
			if(state.getBlock().hasTileEntity(state)) {
				TileEntity tile = world.getTileEntity(pos);
				if(tile != null && tile instanceof ILootContainer) {
					NBTTagCompound compound = new NBTTagCompound();
					if(loot != null) {
						tile.writeToNBT(compound);
						if(!compound.hasKey("LootTable")) {
							compound.setString("LootTable", loot);
						}
						tile.readFromNBT(compound);
						tile.markDirty();
					}
				}
			}
		} else {
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		}
	}
}
