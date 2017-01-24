package jplee.jlib.util.asm;

import java.util.Map;

import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Maps;

import jplee.jlib.util.Pair;

public class CodeDefinition {

	private static boolean isObf = false;
	
	public final String name;
	public CodeDefinition parent;
	
	private Map<String,Pair<String,String>> methods;
	private Map<String,Pair<String,String>> fields;
	
	public CodeDefinition(String obfName, String name) {
		this.name = isObf ? obfName : name;
		this.parent = null;
		
		this.methods = Maps.newHashMap();
		this.fields = Maps.newHashMap();
	}
	
	public CodeDefinition(String name) {
		this.name = name;
		this.parent = null;

		this.methods = Maps.newHashMap();
		this.fields = Maps.newHashMap();
	}
	
	public String asClass() {
		return "L" + name + ";";
	}
	
	public CodeDefinition addMethod(String obfName, String name, String obfDes, String desc) {
		Pair<String,String> func = new Pair<String, String>
		(CodeDefinition.isObf ? obfName : name, CodeDefinition.isObf ? obfDes : desc);
		this.methods.put(name, func);
		return this;
	}

	public CodeDefinition addMethod(String obfName, String name, String desc) {
		Pair<String,String> func = new Pair<String, String>(isObf ? obfName : name, desc);
		this.methods.put(name, func);
		return this;
	}

	public CodeDefinition addMethod(String name, String desc) {
		Pair<String,String> func = new Pair<String, String>(name, desc);
		this.methods.put(name, func);
		return this;
	}
	
	public String getMethodName(String name) {
		return this.getMethod(name).getKey();
	}
	
	public String getMethodDesc(String name) {
		return this.getMethod(name).getItem();
	}
	
	public Pair<String,String> getMethod(String name) {
		Pair<String,String> pair = this.methods.get(name);
		if(pair != null) return pair;
		return this.getParent().getMethod(name);
	}

	public CodeDefinition addField(String obfName, String name, String obfDesc, String desc) {
		Pair<String,String> func = new Pair<String, String>
		(CodeDefinition.isObf ? obfName : name, CodeDefinition.isObf ? obfDesc : desc);
		this.fields.put(name, func);
		return this;
	}

	public CodeDefinition addField(String obfName, String name, String desc) {
		Pair<String,String> func = new Pair<String, String>(CodeDefinition.isObf ? obfName : name, desc);
		this.fields.put(name, func);
		return this;
	}

	public CodeDefinition addField(String name, String desc) {
		Pair<String,String> func = new Pair<String,String>(name,desc);
		this.fields.put(name, func);
		return this;
	}
	
	public String getFieldName(String name) {
		return this.getField(name).getKey();
	}

	public String getFieldDesc(String name) {
		return this.getField(name).getItem();
	}
	
	public Pair<String,String> getField(String name) {
		Pair<String,String> pair = this.fields.get(name);
		if(pair != null) return pair;
		return this.getParent().getField(name);
	}
	
	public CodeDefinition setParent(CodeDefinition code) {
		CodeDefinition def = this.parent;
		this.parent = code;
		return def;
	}
	
	public CodeDefinition getParent() {
		return this.parent;
	}
	
	public boolean methodEquals(String method, Object obj) {
		 if(obj instanceof MethodNode) {
			MethodNode node = (MethodNode) obj;
			String methodName = this.getMethodName(method);
			String methodDesc = this.getMethodDesc(method);
			return node.name.equals(methodName) && node.desc.equals(methodDesc);
		}
		return false;
	}
	
	public boolean fieldEquals(String field, Object obj) {
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CodeDefinition) {
			CodeDefinition def = (CodeDefinition) obj;
			return def.name == this.name;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() +"@" + this.name + "?" + isObf;
	}
	
	public static void setObfFlag(boolean isObj) {
		if(!CodeDefinition.isObf)
			CodeDefinition.isObf = isObj;
	}
}
