package com.jplee.worldmanager.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.jplee.worldmanager.WorldManager;

import static org.objectweb.asm.Opcodes.*;
import net.minecraft.launchwrapper.IClassTransformer;
import scala.actors.threadpool.Arrays;

public class WorldManagerClassTransformer implements IClassTransformer {

	private static final String[] classesBeingTransformed = {
		"net.minecraft.world.chunk.ChunkPrimer"
	};
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
		return index != -1 ? transform(index, basicClass, WorldManagerPlugin.isObf) : basicClass;
	}
	
	private static byte[] transform(int index, byte[] basicClass, boolean isObf) {
		WorldManager.info("Transforming class: ", classesBeingTransformed[index]);
		
		try {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			
			classReader.accept(classNode, 0);
			
			switch(index) {
			case 0: transformChunkPrimer(classNode, isObf); break;
			}
			
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(classWriter);
			return classWriter.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void transformChunkPrimer(ClassNode classNode, boolean isObf) {
		final String SET_BLOCK_STATE = isObf ? "a" : "setblockState";
		final String SET_BLOCK_STATE_DESC = isObf ? "(IIILars;)V" : "(IIILnet/minecraft/block/state/IBlockState;)V";
		
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(SET_BLOCK_STATE) && method.desc.equals(SET_BLOCK_STATE_DESC)) {
				AbstractInsnNode targetNode = null;
				for(AbstractInsnNode instruction : method.instructions.toArray()) {
					if(instruction.getOpcode() == ALOAD) {
						if(((VarInsnNode) instruction).var == 5 && instruction.getNext().getOpcode() == GETSTATIC) {
							targetNode = instruction;
							break;
						}
					}
				}
				if(targetNode != null) {
					
				} else {
					// TODO Cause error
				}
			}
		}
	}
}
