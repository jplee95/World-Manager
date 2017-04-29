package jplee.worldmanager.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import jplee.jlib.util.Pair;
import jplee.worldmanager.WorldManager;
import jplee.worldmanager.config.WorldManagerConfig;
import jplee.worldmanager.util.BlockProperty;
import jplee.worldmanager.util.ScanPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.oredict.OreDictionary;

public class BlockManager {

	public static final BlockManager instance = new BlockManager();

	private Map<Integer,Map<Block,BlockProperty>> blockProperties;
	private Map<Integer,Map<String,BlockProperty>> oreBlockProperties;
	private Map<Integer,List<Pair<BlockPos,Integer>>> fallEvents;

	private boolean propertiesEnabled;

	private boolean worldBlacklist;
	private List<Integer> propertyWorlds;

	private BlockManager() {
		blockProperties = Maps.newHashMap();
		oreBlockProperties = Maps.newHashMap();
		propertyWorlds = Lists.newArrayList();
		fallEvents = Maps.newHashMap();
	}

	public void loadFromConfig(WorldManagerConfig config) {
		if(!blockProperties.isEmpty()) blockProperties.clear();
		if(!oreBlockProperties.isEmpty()) oreBlockProperties.clear();
		if(!propertyWorlds.isEmpty()) propertyWorlds.clear();

		this.propertiesEnabled = config.isBlockPropertiesEnabled();

		this.worldBlacklist = config.isBlockPropertyBlacklist();
		if(propertiesEnabled) {
			Map<Integer,Map<Block,BlockProperty>> blockProp = Maps.newHashMap();
			Map<Integer,Map<String,BlockProperty>> oreBlockProp = Maps.newHashMap();

			for(int i = 0; i < config.getPropertyDimensionsList().length; i++)
				this.propertyWorlds.add(config.getOreGenDimensionsList()[i]);

			for(String line : config.getBlockProperties()) {
				if(!line.startsWith("#") && !line.isEmpty()) {
					BlockProperty prop = BlockProperty.build(line);
					try {
						int dimension = prop.hasData("dimension") ? prop.getInt("dimension")
							: ManagerUtil.ANY_DIMENSION;
						if(this.propertyWorlds.contains(dimension) && !this.worldBlacklist
							|| !this.propertyWorlds.contains(dimension) && this.worldBlacklist
							|| dimension == ManagerUtil.ANY_DIMENSION) {
							if(prop.getBoolean("usingore")) {
								String oreDict = prop.getString("oredict");
								ManagerUtil.addToMap(oreBlockProp, dimension, oreDict, prop);
							} else {
								Block block = prop.getBlockFromBlockState("block");
								ManagerUtil.addToMap(blockProp, dimension, block, prop);
							}
						}
					} catch(Exception e) {
						WorldManager.logger.error("Unable to read %s", prop == null ? "" : prop.toString());
						e.printStackTrace();
					}
				}
			}
			blockProperties.putAll(blockProp);
			oreBlockProperties.putAll(oreBlockProp);
		}
	}

	public boolean worldHasProperties(World world) {
		return worldHasProperties(world.provider.getDimension());
	}

	private boolean worldHasProperties(int world) {
		boolean hasProp = false;
		if(this.propertiesEnabled) {
			hasProp = blockProperties.containsKey(world) && !blockProperties.get(world).isEmpty()
				|| blockProperties.containsKey(ManagerUtil.ANY_DIMENSION)
					&& !blockProperties.get(ManagerUtil.ANY_DIMENSION).isEmpty();
			hasProp = hasProp || oreBlockProperties.containsKey(world) && !oreBlockProperties.get(world).isEmpty()
				|| oreBlockProperties.containsKey(ManagerUtil.ANY_DIMENSION)
					&& !oreBlockProperties.get(ManagerUtil.ANY_DIMENSION).isEmpty();
		}
		boolean contains = propertyWorlds.contains(world);
		return hasProp && ((contains && !this.worldBlacklist) || (!contains && this.worldBlacklist));
	}

	// TODO: Configure proper property getting function
	public Collection<BlockProperty> getProperties(World world, Block block) {
		List<BlockProperty> props = Lists.newArrayList();

		Map<Block,BlockProperty> blockProps = blockProperties.get(world.provider.getDimension());
		if(blockProps != null) {
			props.add(blockProps.get(block));
		}
		blockProps = blockProperties.get(ManagerUtil.ANY_DIMENSION);
		if(blockProps != null && blockProps.containsKey(block)) {
			props.add(blockProps.get(block));
		}

		ItemStack stack = new ItemStack(block);
		if(stack.getItem() != null) {
			for(int id : OreDictionary.getOreIDs(stack)) {
				String i = OreDictionary.getOreName(id);
				if(this.oreBlockProperties.get(ManagerUtil.ANY_DIMENSION) != null) {
					props.add(this.oreBlockProperties.get(ManagerUtil.ANY_DIMENSION).get(i));
				}
				Map<String,BlockProperty> oreProps = this.oreBlockProperties.get(world);
				if(oreProps != null && oreProps.containsKey(i)) {
					props.add(oreProps.get(i));
				}
			}
		}
		return props;
	}

	public boolean canFall(World world, IBlockState state, BlockPos pos) {
		boolean fall = false;
		if(!cantFall(state)) {
			Collection<BlockProperty> props = getProperties(world, state.getBlock());
			Boolean gravity = null;
			Double hold = null;
			Boolean canFall = null;
			for(BlockProperty prop : props) {
				if(canFall == null) canFall = prop.isAdequateState("block", state);
				if(canFall) {
					if(prop.hasData("gravity") && gravity == null) gravity = prop.getBoolean("gravity");
					if(prop.hasData("hold") && hold == null) hold = prop.getDouble("hold");
				}
			}
			if(gravity != null && gravity == true) {
				fall = true;
				for(EnumFacing face : EnumFacing.HORIZONTALS) {
					if(!BlockFalling.canFallThrough(world.getBlockState(pos.offset(face)))) {
						fall = false;
						break;
					}
				}

				// TODO: Finish strength property
				// int str = getBlockStrength(world, pos);
				int str = -1;

				if(str >= 0) {
					fall = false;
				} else if(!fall) {
					Random rand = new Random();
					if(hold != 1.0 && (hold == 0.0 || rand.nextDouble() > hold)) {
						fall = true;
					} else
						fall = false;
				}
			}
		}
		return fall;
	}

	private boolean cantFall(IBlockState state) {
		Material material = state.getMaterial();
		return state == null || state.getBlock() == Blocks.FIRE || material == Material.AIR
			|| material == Material.WATER || material == Material.LAVA || state.getBlock().hasTileEntity(state);
	}

	public int getFallEventCount(World world) {
		if(fallEvents.containsKey(world.provider.getDimension()))
			return fallEvents.get(world.provider.getDimension()).size();
		return 0;
	}

	public void fallBlock(World world, BlockPos pos, int delay) {
		List<Pair<BlockPos,Integer>> events = fallEvents.get(world.provider.getDimension());
		if(events == null) {
			events = Lists.newArrayList();
			fallEvents.put(world.provider.getDimension(), events);
		}
		events.add(new Pair<BlockPos,Integer>(pos, delay));
	}

	public void clearFallEvents() {
		for(List<Pair<BlockPos,Integer>> events : fallEvents.values())
			events.clear();
		fallEvents.clear();
	}

	public void processEvent(TickEvent.WorldTickEvent event) {
		World world = event.world;
		if(fallEvents.containsKey(world.provider.getDimension())) {
			List<Pair<BlockPos,Integer>> events = Lists.newArrayList(fallEvents.get(world.provider.getDimension()));
			for(Pair<BlockPos,Integer> e : events) {
				if(e.getItem() <= 0) {
					if(canFall(world, world.getBlockState(e.getKey()), e.getKey())
						&& BlockFalling.canFallThrough(world.getBlockState(e.getKey().down()))) {
						IBlockState aboveState = world.getBlockState(e.getKey());
						world.setBlockToAir(e.getKey());
						EntityFallingBlock entity = new EntityFallingBlock(world, e.getKey().getX() + .5,
							e.getKey().getY(), e.getKey().getZ() + .5, aboveState);
						entity.fallTime = 1;
						world.spawnEntityInWorld(entity);
						if(canFall(world, world.getBlockState(e.getKey().up()), e.getKey().up())) {
							fallBlock(world, e.getKey().up(), Blocks.SAND.tickRate(world));
						}
					}
					fallEvents.get(world.provider.getDimension()).remove(e);
				}
				if(e.getItem() > 0) {
					e.setItem(e.getItem() - 1);
				}
			}
		}
	}

	// TODO: Finish strength property
	public int getBlockStrength(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Collection<BlockProperty> props = getProperties(world, state.getBlock());
		Boolean hasProperty = null;
		Integer strength = null;
		for(BlockProperty prop : props) {
			if(hasProperty == null) hasProperty = prop.isAdequateState("block", state);
			if(hasProperty) {
				if(prop.hasData("strength") && strength == null) strength = prop.getInt("strength");
			}
		}

		if(!hasProperty || strength == 0) return 0;

		if(!BlockFalling.canFallThrough(world.getBlockState(pos.down()))) return strength;

		ScanPos start = new ScanPos(pos, 0);
		int depth = 0;
		List<Pair<ScanPos,Integer>> depthPos = new ArrayList<>();
		List<ScanPos> points = new ArrayList<>();
		List<ScanPos> finished = new ArrayList<>();
		depthPos.add(new Pair<>(start, strength));
		boolean down = false;
		boolean ground = false;
		while(!depthPos.isEmpty()) {
			Pair<ScanPos,Integer> dep = depthPos.remove(0);
			start = dep.getKey();
			strength = dep.getItem();
			points.add(start);
			while(strength > 0 && !points.isEmpty()) {
				ScanPos p = points.remove(0);
				finished.add(p);
				if(!BlockFalling.canFallThrough(world.getBlockState(p.down()))) {
					int groundCount = 0;
					for(EnumFacing face : EnumFacing.HORIZONTALS) {
						groundCount += (!BlockFalling.canFallThrough(world.getBlockState(p.down().offset(face))) ? 1
							: 0);
					}
					if(groundCount >= 2 && !BlockFalling.canFallThrough(world.getBlockState(p.down().down()))) {
						ground = true;
						break;
					}

					depthPos.add(new Pair<>(new ScanPos(p.down(), 0), strength));
					down = true;
				} else {
					if(!start.equals(p) && down) {
						strength /= 2;
						down = false;
					}
					if(p.getDepth() > depth) depth = p.getDepth();
					for(EnumFacing face : EnumFacing.HORIZONTALS) {
						ScanPos sp = p.offset(face);
						if(sp.getDepth() <= strength && !points.contains(sp) && !finished.contains(sp)
							&& !BlockFalling.canFallThrough(world.getBlockState(sp))) points.add(sp);
					}
				}
			}
		}
		if(!ground) return -1;
		return strength - depth;
	}
}
