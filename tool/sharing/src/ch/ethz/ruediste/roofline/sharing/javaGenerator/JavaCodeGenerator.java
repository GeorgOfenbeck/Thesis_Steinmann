package ch.ethz.ruediste.roofline.sharing.javaGenerator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import ch.ethz.ruediste.roofline.sharing.ICodeGenerator;
import ch.ethz.ruediste.roofline.sharing.DOM.SharedClass;
import ch.ethz.ruediste.roofline.sharing.DOM.SharedFieldBase;

/** Generates Java code from a SharedClass */
public class JavaCodeGenerator implements ICodeGenerator {

	@Override
	public void generate(SharedClass sharedClass, Writer writer) {
		VelocityEngine ve=new VelocityEngine();
		
		VelocityContext context=new VelocityContext();
		context.put("class", createPMod(sharedClass));
		
		InputStream input=ClassLoader.getSystemResourceAsStream("javaTemplate.vm");
		
		ve.evaluate(context, writer, "javaClass", new InputStreamReader(input));
		
	}
	
	private SharedClassPMod createPMod(SharedClass sharedClass){
		SharedClassPMod pMod=new SharedClassPMod(sharedClass);
		for (SharedFieldBase field : sharedClass.getFields()){
			pMod.getFields().add(createPMod(field));
		}
		return pMod;
	}
	
	private SharedFieldBasePMod createPMod(SharedFieldBase sharedFieldBase){
		SharedFieldBasePMod pMod=new SharedFieldBasePMod(sharedFieldBase);
		return pMod;
	}

	@Override
	public String getFileName(SharedClass sharedClass) {
		return "generatedJava/"+sharedClass.getName()+".java";
	}
}
