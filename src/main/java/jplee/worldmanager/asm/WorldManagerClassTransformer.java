package jplee.worldmanager.asm;

import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

import net.minecraft.launchwrapper.IClassTransformer;

public class WorldManagerClassTransformer extends ClassTransformer {

	@Override
	public String[] getClassesToTransform() {
		return new String[] {
			"net.minecraft.client.gui.GuiWorldEdit",
			"net.minecraft.block.BlockCactus"
		};
	}

	@Override
	public void transform(int index, ClassNode classNode, boolean isObf) {
		switch(index) {
		case 0: transformWorldEdit(classNode); break;
//		case 1: transformCactus(classNode); break;
		
		}
	}
	
	private void transformCactus(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(CodeDefins.BLOCK_CACTUS.getMethodName("onEntityCollidedWithBlock"))
			&& method.desc.equals(CodeDefins.BLOCK_CACTUS.getMethodDesc("onEntityCollidedWithBlock"))) {
				AbstractInsnNode targetNode = null;
				
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(this.foundOpcode(node, ALOAD, 4)) {
						if(this.foundOpcode(node.getNext(), GETSTATIC)) {
							targetNode = node;
							break;
						}
					}
				}
				
				AbstractInsnNode popNode = targetNode;
				while(popNode.getOpcode() != POP) {
					popNode = popNode.getNext();
					
					if(popNode == null) return;
				}
				
				InsnList toInsert = new InsnList();
				LabelNode label1 = new LabelNode();
				toInsert.add(new MethodInsnNode(INVOKESTATIC, "jplee/worldmanager/asm/CodeDefins", "cactusDoesDamage", "()Z", false));
				toInsert.add(new JumpInsnNode(IFEQ, label1));
				
				method.instructions.insertBefore(targetNode, toInsert);
				method.instructions.insert(popNode, label1);
				break;
			}
		}
	}
	
	private void transformWorldEdit(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(CodeDefins.GUI_WORLD_EDIT.getMethodName("actionPerformed"))
			&& method.desc.equals(CodeDefins.GUI_WORLD_EDIT.getMethodDesc("actionPerformed"))) {
				
			}
			if(method.name.equals(CodeDefins.GUI_WORLD_EDIT.getMethodName("initGui"))
			&& method.desc.equals(CodeDefins.GUI_WORLD_EDIT.getMethodDesc("initGui"))) {
				transformWorldEdit_initGui(classNode, method);
			}
		}
	}

	private void transformWorldEdit_initGui(ClassNode classNode, MethodNode method) {
		AbstractInsnNode targetNode = null;
		
		for(AbstractInsnNode node : method.instructions.toArray()) {
			if(node.getOpcode() == ALOAD) {
				if(((VarInsnNode) node).var == 0 && node.getNext() != null) {
					if(node.getNext().getOpcode() == NEW) {
						if(((TypeInsnNode) node.getNext()).desc.equals(CodeDefins.GUI_BUTTON.name)) {
							targetNode = node;
							break;
						}
					}
				}
			}
		}
		
		if(targetNode != null) {
			InsnList toInsert = new InsnList();
			toInsert.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
			toInsert.add(new LdcInsnNode("HELLO WORLD FROM SOMEWHERE!"));
			toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));


			toInsert.add(new VarInsnNode(ALOAD, 0));
			toInsert.add(new VarInsnNode(ALOAD, 0));
			toInsert.add(new FieldInsnNode(GETFIELD, CodeDefins.GUI_WORLD_EDIT.name, CodeDefins.GUI_WORLD_EDIT.getFieldName("buttonList"), CodeDefins.GUI_WORLD_EDIT.getFieldDesc("buttonList")));
			toInsert.add(new MethodInsnNode(INVOKESTATIC, "jplee/worldmanager/asm/CodeDefins", "addWorldOptionButton", "(L" + CodeDefins.GUI_WORLD_EDIT.name + ";Ljava/util/List;)V", false));
			
			method.instructions.insertBefore(targetNode, toInsert);
		}
	}

}
