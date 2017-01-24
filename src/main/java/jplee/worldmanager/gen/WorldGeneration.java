package jplee.worldmanager.gen;

import java.util.Arrays;
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
import jplee.worldmanager.config.GenConfig;
import jplee.worldmanager.gen.ore.WorldGenVanillaMinable;
import jplee.worldmanager.util.OreGenInfo;
import jplee.worldmanager.util.Replaceable;
import net.minecraft.block.Block;
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
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class WorldGeneration {
	
	public static final WorldGeneration instance = new WorldGeneration();
	
	public static final int ANY_DIMENSION = -93578231;
	
	private Multimap<Integer,ChunkPos> loadedPendingChunks;
	private Multimap<Integer,ChunkPos> unloadedPendingChunks;
	
	private Map<Integer,Multimap<Block,Replaceable>> sortedReplaceables;
	private Map<Integer,Multimap<String,Replaceable>> sortedOreDictionaries;
	
	private Map<Integer,Multimap<Block,Replaceable>> modInstalledReplaceables;
	@SuppressWarnings("unused")
	private Map<Integer,Multimap<String,Replaceable>> modInstalledOreDictionaries;
	
	private Multimap<Integer,OreGenInfo> generatableOres;
	
	private List<Integer> replaceableWorlds;
	private List<Integer> oreGenWorlds;
	
	private boolean replaceablesBlacklist;
	private boolean oreGenBlacklist;
	
	private boolean enableReplaceables;
	private boolean enableOreGen;
	
	private WorldGeneration() {
		this.loadedPendingChunks = HashMultimap.create();
		this.unloadedPendingChunks = HashMultimap.create();
		this.modInstalledReplaceables = Maps.newHashMap();
		this.sortedOreDictionaries = Maps.newHashMap();
		this.sortedReplaceables = Maps.newHashMap();
		this.generatableOres = HashMultimap.create();
		this.replaceableWorlds = Lists.newArrayList();
		this.oreGenWorlds = Lists.newArrayList();
		
		this.replaceablesBlacklist = true;
		this.oreGenBlacklist = true;
		
		this.enableReplaceables = false;
		this.enableOreGen = false;
	}
	
	public void loadWorldGenerationInfo(GenConfig config) {
		Map<Integer,Multimap<Block,Replaceable>> replaceables = Maps.newHashMap();
		Map<Integer,Multimap<String,Replaceable>> oreReps = Maps.newHashMap();

		this.replaceablesBlacklist = config.isReplaceablesBlacklist();
		this.oreGenBlacklist = config.isOreGenBlacklist();

		this.enableReplaceables = config.isReplaceablesEnabled();
		this.enableOreGen = config.isOreGenEnabled();
		
		for(int i = 0; i < config.getReplaceablesDimensionsList().length; i++)
			this.replaceableWorlds.add(config.getReplaceablesDimensionsList()[i]);

		Arrays.asList(config.getReplaceables()).stream()
		.filter(line -> !line.startsWith("#") && !line.isEmpty())
		.forEach(line -> {
			Replaceable rep = Replaceable.build(line);
			try {
				int dimension = rep.hasData("dimension") ? rep.getInt("dimension") : ANY_DIMENSION;
				if(this.replaceableWorlds.contains(dimension) && !this.replaceablesBlacklist ||
					!this.replaceableWorlds.contains(dimension) && this.replaceablesBlacklist ||
					dimension == ANY_DIMENSION) {
					if(rep.getBoolean("usingore")) {
						String oreDict = rep.getString("oredict");
						add(oreReps, dimension, oreDict, rep);
					} else {
						Block block = rep.getBlockFromBlockState("block");
						add(replaceables, dimension, block, rep);
					}
				}
			} catch(Exception e) {
				WorldManager.logger.error("Unable to read %s", rep == null ? "" :rep.toString());
				e.printStackTrace();
			}
		});

		this.generatableOres.clear();
		for(int i = 0; i < config.getReplaceablesDimensionsList().length; i++)
			this.oreGenWorlds.add(config.getReplaceablesDimensionsList()[i]);

		Arrays.asList(config.getOreGeneration()).stream()
		.filter(line -> !line.startsWith("#") && !line.isEmpty())
		.forEach(line -> {
			OreGenInfo info = OreGenInfo.build(line);
			int dimension = info.hasData("dimension") ? info.getInt("dimension") : ANY_DIMENSION;
			if(this.oreGenWorlds.contains(dimension) && !this.oreGenBlacklist ||
				!this.oreGenWorlds.contains(dimension) && this.oreGenBlacklist ||
				dimension == ANY_DIMENSION) {
				if(info.getBoolean("override")) {
					String rep = info.getBlockState("ore").toString();
					Replaceable replaceable = Replaceable.build(rep, false);
					add(replaceables, dimension, info.getBlockFromBlockState("ore"), replaceable);
				}
			}
			this.generatableOres.put(dimension, info);
		});
		
		this.sortedOreDictionaries.clear();
		for(int dimension : oreReps.keySet()) {
			for(String ore : oreReps.get(dimension).keys()) {
				List<Replaceable> list = Lists.newArrayList(oreReps.get(dimension).get(ore));
				list.sort(replaceableComparable);
				Multimap<String, Replaceable> oreDict = this.sortedOreDictionaries.get(dimension);
				if(oreDict == null) {
					this.sortedOreDictionaries.put(dimension, HashMultimap.<String,Replaceable>create());
					oreDict = this.sortedOreDictionaries.get(dimension); 
				}
				oreDict.putAll(ore, list);
			}
		}
		
		this.sortedReplaceables.clear();
		for(int dimension : replaceables.keySet()) {
			for(Block block : replaceables.get(dimension).keys()) {
				List<Replaceable> list = Lists.newArrayList(replaceables.get(dimension).get(block));
				list.sort(replaceableComparable);
				Multimap<Block, Replaceable> blocks = this.sortedReplaceables.get(dimension);
				if(blocks == null) {
					this.sortedReplaceables.put(dimension, HashMultimap.<Block,Replaceable>create());
					blocks = this.sortedReplaceables.get(dimension); 
				}
				blocks.putAll(block, list);
			}
		}
	}
	
	private static <M extends Object, K extends Object, J extends Object> boolean add(Map<M,Multimap<K,J>> map, M key1, K key2, J value) {
		Multimap<K,J> innerMap = null;
		if((innerMap = map.get(key1)) == null) {
			map.put(key1, HashMultimap.<K,J>create());
			innerMap = map.get(key1);
		}
		return innerMap.put(key2, value);
	}
	
	private static Comparator<Replaceable> replaceableComparable = new Comparator<Replaceable>() {
		@Override public int compare(Replaceable r1, Replaceable r2) {
			return -Doubles.compare(r1.getDouble("random"), r2.getDouble("random"));
		}
	};
	
	public void registerWorldGenerators() {
		GameRegistry.registerWorldGenerator(new GenWorld(), 0);
	}
	
	public void addNewReplaceable(Block block, Replaceable replaceable) {
		int dimension = replaceable.getInt("dimension");
		Multimap<Block, Replaceable> blocks = this.modInstalledReplaceables.get(dimension);
		if(blocks == null) {
			this.modInstalledReplaceables.put(dimension, HashMultimap.<Block,Replaceable>create());
			blocks = this.modInstalledReplaceables.get(dimension); 
		}
		blocks.put(block, replaceable);
	}
	
	public Collection<Replaceable> getReplaceables(int world, Block block) {
		List<Replaceable> rep = Lists.newArrayList();

		if(this.sortedReplaceables.get(ANY_DIMENSION) != null) {
			rep.addAll(this.sortedReplaceables.get(ANY_DIMENSION).get(block));
		}
		Multimap<Block, Replaceable> worldReplace = this.sortedReplaceables.get(world);
		if(worldReplace != null) {
			rep.addAll(worldReplace.get(block));
		}

		ItemStack stack = new ItemStack(block);
		if(stack.getItem() != null) {
			for(int id : OreDictionary.getOreIDs(stack)) {
				String i = OreDictionary.getOreName(id);
				if(this.sortedOreDictionaries.get(ANY_DIMENSION) != null) {
					rep.addAll(this.sortedOreDictionaries.get(ANY_DIMENSION).get(i));
				}
				Multimap<String, Replaceable> oreReps = this.sortedOreDictionaries.get(world);
				if(oreReps != null) {
					rep.addAll(oreReps.get(i));
				}
			}
		}
		
		return rep;
	}
	
	public Collection<ChunkPos> getQueuedForWorld(World world) {
		return loadedPendingChunks.get(world.provider.getDimension());
	}
	
	public boolean unqueueChunk(World world, Chunk chunk) {
		if(loadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair())) {
			loadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			unloadedPendingChunks.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			return true;
		}
		return false;
	}

	public boolean queueChunk(World world, Chunk chunk) {
		if(unloadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair())) {
			unloadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			loadedPendingChunks.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
			return true;
		}
		return false;
	}
	
	public void addToQueue(World world, Chunk chunk, boolean loaded) {
		if(loaded)
			loadedPendingChunks.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		else
			unloadedPendingChunks.put(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public void removeFromQueue(World world, Chunk chunk, int state) {
		if((state & 1) == 1)
			loadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		if((state & 2) == 2)
			unloadedPendingChunks.remove(world.provider.getDimension(), chunk.getChunkCoordIntPair());
	}
	
	public void clearQueuedChunks() {
		loadedPendingChunks.clear();
		unloadedPendingChunks.clear();
	}
	
	public Collection<Integer> getWorldsWithQueues() {
		return loadedPendingChunks.keySet();
	}
	
	public Collection<Integer> getWorldsWithUnqueses() {
		return unloadedPendingChunks.keySet();
	}
	
	public boolean isQueuedChunk(World world, Chunk chunk) {
		return isQueuedChunk(world, chunk, 3);
	}
	
	public boolean isQueuedChunk(World world, Chunk chunk, int flag) {
		boolean loaded = loadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		boolean unloaded = unloadedPendingChunks.containsEntry(world.provider.getDimension(), chunk.getChunkCoordIntPair());
		return loaded && ((flag & 1) == 1)
			|| unloaded && ((flag & 2) == 2);
	}
	
	public int getQueuedChunkCount(int dimension) {
		int size = 0;
		Collection<ChunkPos> pos = loadedPendingChunks.get(dimension);
		if(pos != null)
			size += pos.size();

		pos = unloadedPendingChunks.get(dimension);
		if(pos != null)
			size += pos.size();
		
		return size;
	}

	public boolean isWorldProcessable(World world) {
		return worldHasReplaceables(world) || worldOreGenable(world);
	}
	
	public boolean isWorldProcessable(int dimension) {
		return worldHasReplaceables(dimension) || worldOreGenable(dimension);
	}
	
	public boolean worldOreGenable(World world) {
		return worldOreGenable(world.provider.getDimension());
	}
	
	private boolean worldOreGenable(int worldId) {
		boolean hasOreGen = false;
		if(this.enableOreGen) {
			hasOreGen = generatableOres.containsKey(worldId) && !generatableOres.get(worldId).isEmpty()
				|| generatableOres.containsKey(ANY_DIMENSION) && !generatableOres.get(ANY_DIMENSION).isEmpty();
		}
		return hasOreGen
			&& ((oreGenWorlds.contains(worldId) && !this.oreGenBlacklist)
			|| (!oreGenWorlds.contains(worldId) && this.oreGenBlacklist));
	}
	
	public boolean worldHasReplaceables(World world) {
		return worldHasReplaceables(world.provider.getDimension());
	}
	
	private boolean worldHasReplaceables(int worldId) {
		boolean hasReplaceables = false;
		if(this.enableReplaceables) {
			hasReplaceables = sortedReplaceables.containsKey(worldId) && !sortedReplaceables.get(worldId).isEmpty()
				|| sortedReplaceables.containsKey(ANY_DIMENSION) && !sortedReplaceables.get(ANY_DIMENSION).isEmpty();
			hasReplaceables = hasReplaceables
				|| sortedOreDictionaries.containsKey(worldId) && !sortedOreDictionaries.get(worldId).isEmpty()
				|| sortedOreDictionaries.containsKey(ANY_DIMENSION) && !sortedReplaceables.get(ANY_DIMENSION).isEmpty();
		}
		boolean contains = replaceableWorlds.contains(worldId);
		
		return hasReplaceables
			&& ((contains && !this.replaceablesBlacklist)
			|| (!contains && this.replaceablesBlacklist));
	}
	
	public boolean processChunk(World world, ChunkPos chunkPos) {
		boolean chunkModified = false;
		
		long worldSeed = world.getSeed();
		Random fmlRandom = new Random(worldSeed);
		long xSeed = fmlRandom.nextLong() >> 2 + 1l;
		long zSeed = fmlRandom.nextLong() >> 2 + 1l;
		long chunkSeed = (xSeed * chunkPos.chunkXPos + zSeed * chunkPos.chunkZPos) ^ worldSeed;
		fmlRandom.setSeed(chunkSeed);
		
		if(this.worldHasReplaceables(world)) {
			chunkModified = processChunk(world, fmlRandom, chunkPos);
		}
		if(this.worldOreGenable(world)) {
			chunkModified = generateOres(world, fmlRandom, chunkPos);
		}
		return chunkModified;
	}
	
	private boolean processChunk(World world, Random random, ChunkPos chunkPos) {
		boolean chunkModified = false;
		for(int y = world.getHeight() - 1; y >= 0; y--) {
			for(int x = 0; x < 16; x++) {
				for(int z = 0; z < 16; z++) {
					int posX = chunkPos.chunkXPos * 16 + x;
					int posZ = chunkPos.chunkZPos * 16 + z;
					BlockPos pos = new BlockPos(posX, y, posZ);
					if(this.replacementProcess(world, random, pos) && !chunkModified) {
						chunkModified = true;
					}
				}
			}
		}
		
		return chunkModified;
	}
	
	private boolean replacementProcess(World world, Random fmlRandom, BlockPos pos) {
			IBlockState blockState = world.getBlockState(pos);
			int dimension = world.provider.getDimension();
			if(!blockState.getBlock().equals(Blocks.AIR)) {
				Collection<Replaceable> replaceables = getReplaceables(dimension, blockState.getBlock());
				if(replaceables != null) {
					for(Replaceable rep : replaceables) {
						int min = rep.getInt("min");
						int max = rep.getInt("max");
						
						if((min <= pos.getY() || min == -1) && (max >= pos.getY() || max == -1)) {
							double random = rep.getDouble("random");
							IBlockState replace = rep.getBlockState("replace");
							if((random == 1.0 || fmlRandom.nextDouble() < random)) {
								WorldManager.logger.debug("Replacing %s at (%s %s %s)", blockState.getBlock().getLocalizedName(), pos.getX(), pos.getY(), pos.getZ());
								if(rep.getBoolean("usingore")) {
									setBlock(world, pos, replace, rep.getString("loot"), 4);
									return true;
								}
								if(!rep.getBoolean("usingore")) {
									if(rep.isAdequateState("block", blockState)) {
										setBlock(world, pos, replace, rep.getString("loot"), 4);
										return true;
									}
								}
							}
						}
					}
				}
			}
		return false;
	}
	
	private void setBlock(World world, BlockPos pos, IBlockState state, String loot, int flags) {
		IBlockState replace = (state == null ? Blocks.AIR.getDefaultState() : state);
		world.setBlockState(pos, replace, flags);
		if(state != null) {
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
		}
	}

	private boolean generateOres(World w, Random rand, ChunkPos pos) {
		boolean chunkModified = false;
		List<OreGenInfo> genable = Lists.newArrayList(generatableOres.get(w.provider.getDimension()));
		genable.addAll(generatableOres.get(ANY_DIMENSION));
		for(OreGenInfo info : genable) {
			IBlockState state = info.getBlockState("ore");
			int min = info.getInt("min");
			int max = info.getInt("max");
			int minSize = info.getInt("minSize");
			int maxSize = info.getInt("maxSize");
			int chance = info.getInt("chance");

			WorldGenVanillaMinable minable = new WorldGenVanillaMinable(state, minSize, maxSize, 4, replaceable -> info.isAdequateState("replace", replaceable));
			
			for(int i = 0; i < chance; i++) {
				double xRand = pos.chunkXPos * 16 + rand.nextInt(16);
				double yRand = rand.nextInt(max - min) + min;
				double zRand = pos.chunkZPos * 16 + rand.nextInt(16);
				if(minable.generate(w, rand, new BlockPos(xRand, yRand, zRand))) {
					chunkModified = true;
				}
			}
		}
		return chunkModified;
	}

	public class GenWorld implements IWorldGenerator {

		@Override
		public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
			if(isWorldProcessable(world))
				loadedPendingChunks.put(world.provider.getDimension(), new ChunkPos(chunkX, chunkZ));
		}
	}
	
}
