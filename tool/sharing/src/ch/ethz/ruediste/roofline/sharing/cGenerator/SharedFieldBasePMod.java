package ch.ethz.ruediste.roofline.sharing.cGenerator;

import java.util.HashMap;

import ch.ethz.ruediste.roofline.sharing.DOM.FieldType;
import ch.ethz.ruediste.roofline.sharing.DOM.SharedFieldBase;

public class SharedFieldBasePMod {
	private SharedFieldBase sharedFieldBase;

	/** maps type names from XML to Java */
	private static HashMap<String,String> typeMap
		=new HashMap<String, String>();
	
	static {
		typeMap.put("int", "int");
	}

	public SharedFieldBasePMod(SharedFieldBase sharedFieldBase) {
		this.sharedFieldBase=sharedFieldBase;
	}
	
	public String getName(){return sharedFieldBase.getName();}
	
	public String getType(){
		if (typeMap.containsKey(sharedFieldBase.getType())){
			return typeMap.get(sharedFieldBase.getType());
		}
		return sharedFieldBase.getType();
	}
	
	public FieldType getFieldType(){return sharedFieldBase.getFieldType();}
	
	public String getNameUpperCamel(){
		return getName().substring(0,1).toUpperCase()+getName().substring(1);
	}
	
	public String getComment(){return sharedFieldBase.getComment();}
}
