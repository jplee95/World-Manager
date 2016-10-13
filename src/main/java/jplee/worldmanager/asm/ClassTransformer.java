package jplee.worldmanager.asm;

import java.util.Arrays;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class ClassTransformer implements IClassTransformer {

	public ClassTransformer() {
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		int index = Arrays.asList(getClassesToTransform()).indexOf(transformedName);
		if(index != -1) System.out.println("Transforming class: " + getClassesToTransform()[index]);
		return index != -1 ? transform(index, basicClass, WorldManagerPlugin.isObf) : basicClass;
	}

	private byte[] transform(int index, byte[] basicClass, boolean isObf) {
		try {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);
			
			transform(index, classNode, isObf);
			
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(classWriter);
			return classWriter.toByteArray();
		} catch(Exception e) {
			System.out.println("Unable to transform class: " + getClassesToTransform()[index]);
			e.printStackTrace();
		}
		return basicClass;
	}
	
	public abstract String[] getClassesToTransform();
	public abstract void transform(int index, ClassNode classNode, boolean isObf);
	
	public boolean foundOpcode(AbstractInsnNode node, int opcode, Object...values) {
		if(node.getOpcode() != opcode) return false;
		switch(node.getType()) {
		case InsnNode.INSN: 				return true;
		case InsnNode.INT_INSN:				return ((IntInsnNode) node).operand == (Integer) values[0];
		case InsnNode.VAR_INSN:				return ((VarInsnNode) node).var == (Integer) values[0];
		case InsnNode.TYPE_INSN:			return ((TypeInsnNode) node).desc.equals((String) values[0]);
		case InsnNode.FIELD_INSN:			return ((FieldInsnNode) node).name.equals((String) values[0]) && ((FieldInsnNode) node).desc.equals((String) values[1]);
		case InsnNode.INVOKE_DYNAMIC_INSN:	return ((MethodInsnNode) node).name.equals((String) values[0]) && ((MethodInsnNode) node).desc.equals((String) values[1]) && ((MethodInsnNode) node).itf == (Boolean) values[2];
		case InsnNode.JUMP_INSN:			return true;
		case InsnNode.LABEL:				return true;
		case InsnNode.LDC_INSN:				return ((LdcInsnNode) node).cst.equals(values[0]);
		case InsnNode.IINC_INSN:			return ((IincInsnNode) node).var == (Integer) values[0] && ((IincInsnNode) node).incr == (Integer) values[1];
		case InsnNode.TABLESWITCH_INSN:		return true;
		case InsnNode.LOOKUPSWITCH_INSN:	return true;
		case InsnNode.MULTIANEWARRAY_INSN:	return ((MultiANewArrayInsnNode) node).desc.equals((String) values[0]);
		case InsnNode.FRAME:				return ((FrameNode) node).type == (Integer) values[0];
		case InsnNode.LINE:					return ((LineNumberNode) node).line == (Integer) values[0];
		}
		return false;
	}
	
	protected boolean foundOpcode(AbstractInsnNode node, int opcode) {
		return node.getOpcode() == opcode;
	}
	
}
