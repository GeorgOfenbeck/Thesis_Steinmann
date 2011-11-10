package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.javaGenerator;

import java.util.LinkedList;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;

public class SharedClassPMod {
	MultiLangugeClass sharedClass;
	private LinkedList<SharedFieldBasePMod> fields
		=new LinkedList<SharedFieldBasePMod>();
	
	SharedClassPMod(MultiLangugeClass sharedClass){
		this.sharedClass=sharedClass;
	}
	
	public String getName(){return sharedClass.getName();}
	public String getBaseType(){return sharedClass.getJavaBaseType();}

	public LinkedList<SharedFieldBasePMod> getFields() {
		return fields;
	}
	
	public String getComment(){return sharedClass.getComment();}
}
