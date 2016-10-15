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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeModContainer;

public class WorldManagerClassTransformer extends ClassTransformer {

	@Override
	public String[] getClassesToTransform() {
		return new String[] {
			"net.minecraft.client.gui.GuiWorldEdit",
		};
	}

	@Override
	public void transform(int index, ClassNode classNode, boolean isObf) {
		switch(index) {
		case 0: transformWorldEdit(classNode); break;
		
		}
	}
	
	private void transformWorldEdit(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(CodeDefins.GUI_WORLD_EDIT.getMethodName("actionPerformed"))
			&& method.desc.equals(CodeDefins.GUI_WORLD_EDIT.getMethodDesc("actionPerformed"))) {
				transformWorldEdit_actionPerformed(classNode, method);
			}
			if(method.name.equals(CodeDefins.GUI_WORLD_EDIT.getMethodName("initGui"))
			&& method.desc.equals(CodeDefins.GUI_WORLD_EDIT.getMethodDesc("initGui"))) {
				transformWorldEdit_initGui(classNode, method);
			}
		}
	}

	private void transformWorldEdit_actionPerformed(ClassNode classNode, MethodNode method) {
		AbstractInsnNode targetNode = null;
		
		for(AbstractInsnNode node : method.instructions.toArray()) {
			if(this.foundOpcode(node, RETURN)) {
				targetNode = node.getPrevious().getPrevious();
				break;
			}
		}
		
		if(targetNode != null) {
			this.newWorkList();
			this.addVarNode(ALOAD, 1);
			this.addFieldNode(GETFIELD, CodeDefins.GUI_BUTTON, "id");
			this.addVarNode(ALOAD, 0);
			this.addVarNode(ALOAD, 0);
			this.addFieldNode(GETFIELD, CodeDefins.GUI_WORLD_EDIT, "worldId");
			this.addMethodNode(INVOKESTATIC, CodeDefins.CODE_DEFINES, "worldOptionAction", false);
			this.insertBefore(targetNode, method.instructions);
		}
	}

	private void transformWorldEdit_initGui(ClassNode classNode, MethodNode method) {
		AbstractInsnNode targetNode = null;
		
		for(AbstractInsnNode node : method.instructions.toArray()) {
			if(this.foundOpcode(node, ALOAD, 0)) {
				if(this.foundOpcode(node.getNext(), NEW, CodeDefins.GUI_BUTTON.name)) {
					targetNode = node;
					break;
				}
			}
		}
		
		if(targetNode != null) {
			this.newWorkList();
			this.addVarNode(ALOAD, 0);
			this.addVarNode(ALOAD, 0);
			this.addFieldNode(GETFIELD, CodeDefins.GUI_WORLD_EDIT, "buttonList");
			this.addMethodNode(INVOKESTATIC, CodeDefins.CODE_DEFINES, "addWorldOptionButton", false);
			this.insertBefore(targetNode, method.instructions);
		}
	}

}
