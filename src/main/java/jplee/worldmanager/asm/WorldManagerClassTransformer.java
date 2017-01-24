package jplee.worldmanager.asm;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

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
