package jplee.worldmanager.asm;

import java.util.Map;

import com.google.common.collect.Maps;

import jplee.worldmanager.util.Pair;

public class CodeDefinition {

	public final String name;
	public final boolean isObf;
	public CodeDefinition parent;
	
	private Map<String,Pair<String,String>> methods;
	private Map<String,Pair<String,String>> fields;
	
	public CodeDefinition(String obfName, String name, boolean isObf) {
		this.name = name;
		this.isObf = isObf;
		this.parent = null;
		
		this.methods = Maps.newHashMap();
		this.fields = Maps.newHashMap();
	}
	
	public Pair<String, String> addMethod(String obfName, String name, String obfDes, String desc) {
		Pair<String,String> func = new Pair<String, String>(this.isObf ? obfName : name, this.isObf ? obfDes : desc);
		return this.methods.put(name, func);
	}

	public Pair<String, String> addMethod(String obfName, String name, String desc) {
		Pair<String,String> func = new Pair<String, String>(this.isObf ? obfName : name, desc);
		return this.methods.put(name, func);
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

	public Pair<String,String> addField(String obfName, String name, String obfDesc, String desc) {
		Pair<String,String> func = new Pair<String, String>(this.isObf ? obfName : name, this.isObf ? obfDesc : desc);
		return this.fields.put(name, func);
	}

	public Pair<String, String> addField(String obfName, String name, String desc) {
		Pair<String,String> func = new Pair<String, String>(this.isObf ? obfName : name, desc);
		return this.fields.put(name, func);
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
		return this.getClass().getName() +":" + this.name + "?" + isObf;
	}
}
