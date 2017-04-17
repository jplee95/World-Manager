package jplee.worldmanager.util;

import javax.annotation.concurrent.Immutable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;


@Immutable
public class ScanPos extends BlockPos {

	int depth;

	public ScanPos(int x, int y, int z, int depth) {
		super(x, y, z);
		this.depth = depth;
	}
	
	public ScanPos(double x, double y, double z, double depth) {
		super(x, y, z);
		this.depth = (int)depth;
	}
	
	public ScanPos(BlockPos pos, int depth) {
		this(pos.getX(), pos.getY(), pos.getZ(), depth);
	}
	
	public int getDepth() {
		return depth;
	}

	@Override
	public ScanPos up(int n) {
		return new ScanPos(super.up(n), depth + n);
	}

	@Override
	public ScanPos up() {
		return this.up(1);
	}

	@Override
	public ScanPos down(int n) {
		return new ScanPos(super.down(n), depth + n);
	}

	@Override
	public ScanPos down() {
		return this.down(1);
	}

	@Override
	public ScanPos north(int n) {
		return new ScanPos(super.north(n), depth + n);
	}

	@Override
	public ScanPos north() {
		return this.north(1);
	}

	@Override
	public ScanPos south(int n) {
		return new ScanPos(super.south(n), depth + n);
	}

	@Override
	public ScanPos south() {
		return this.south(1);
	}

	@Override
	public ScanPos east(int n) {
		return new ScanPos(super.east(n), depth + n);
	}

	@Override
	public ScanPos east() {
		return this.east(1);
	}

	@Override
	public ScanPos west(int n) {
		return new ScanPos(super.west(n), depth + n);
	}

	@Override
	public ScanPos west() {
		return this.west(1);
	}

	@Override
	public ScanPos offset(EnumFacing facing) {
		return offset(facing, 1);
	}
	
	@Override
	public ScanPos offset(EnumFacing facing, int n) {
		return new ScanPos(super.offset(facing, n), depth + n);
	}
}
