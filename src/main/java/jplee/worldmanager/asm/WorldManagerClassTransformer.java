package jplee.worldmanager.asm;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import jplee.jlib.util.asm.ClassTransformer;

public class WorldManagerClassTransformer extends ClassTransformer {

	public WorldManagerClassTransformer() {
		super(WorldManagerPlugin.isObf);
	}

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
			if(CodeDefins.GUI_WORLD_EDIT.methodEquals("actionPerformed", method)) {
				transformWorldEdit_actionPerformed(classNode, method);
			}
			if(CodeDefins.GUI_WORLD_EDIT.methodEquals("initGui", method)) {
				transformWorldEdit_initGui(classNode, method);
			}
		}
	}
	
	private void transformWorldEdit_actionPerformed(ClassNode classNode, MethodNode method) {
		AbstractInsnNode targetNode = null;
		
		for(AbstractInsnNode node : method.instructions.toArray()) {
			if(foundOpcode(node, RETURN)) {
				targetNode = node.getPrevious().getPrevious();
				break;
			}
		}
		
		if(targetNode != null) {
			newWorkList();
			addVarNode(ALOAD, 1);
			addFieldNode(GETFIELD, CodeDefins.GUI_BUTTON, "id");
			addVarNode(ALOAD, 0);
			addVarNode(ALOAD, 0);
			addFieldNode(GETFIELD, CodeDefins.GUI_WORLD_EDIT, "worldId");
			addMethodNode(INVOKESTATIC, CodeDefins.CODE_DEFINES, "worldOptionAction", false);
			insertBefore(targetNode, method.instructions);
		}
	}

	private void transformWorldEdit_initGui(ClassNode classNode, MethodNode method) {
		AbstractInsnNode targetNode = null;
		
		for(AbstractInsnNode node : method.instructions.toArray()) {
			if(foundOpcode(node, ALOAD, 0)) {
				if(foundOpcode(node.getNext(), NEW, CodeDefins.GUI_BUTTON.name)) {
					targetNode = node;
					break;
				}
			}
		}
		
		if(targetNode != null) {
			newWorkList();
			addVarNode(ALOAD, 0);
			addVarNode(ALOAD, 0);
			addFieldNode(GETFIELD, CodeDefins.GUI_WORLD_EDIT, "buttonList");
			addMethodNode(INVOKESTATIC, CodeDefins.CODE_DEFINES, "addWorldOptionButton", false);
			insertBefore(targetNode, method.instructions);
		}
	}
}
