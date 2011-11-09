package ch.ethz.ruediste.roofline.sharing.javaGenerator;

import java.util.LinkedList;

import ch.ethz.ruediste.roofline.sharing.DOM.SharedClass;

public class SharedClassPMod {
	SharedClass sharedClass;
	private LinkedList<SharedFieldBasePMod> fields
		=new LinkedList<SharedFieldBasePMod>();
	
	SharedClassPMod(SharedClass sharedClass){
		this.sharedClass=sharedClass;
	}
	
	public String getName(){return sharedClass.getName();}
	public String getBaseType(){return sharedClass.getJavaBaseType();}

	public LinkedList<SharedFieldBasePMod> getFields() {
		return fields;
	}
	
	public String getComment(){return sharedClass.getComment();}
}
