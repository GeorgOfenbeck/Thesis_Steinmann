package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.javaGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.CodeGeneratorBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeFieldBase;

/** Generates Java code from a SharedClass */
public class JavaCodeGenerator extends CodeGeneratorBase {

	@Override
	public void generate(List<MultiLangugeClass> sharedClasses) {
		VelocityEngine ve=new VelocityEngine();

		// generate java code for all classes
		for(MultiLangugeClass multiLangugeClass: sharedClasses){
			try{
				VelocityContext context=new VelocityContext();
				
				// initialize context
				context.put("class", createPMod(multiLangugeClass));
				
				// open output writer
				FileWriter writer=openWriter("generatedJava/ch/ethz/ruediste/roofline/sharedDOM/"+multiLangugeClass.getName()+".java");
				
				// load template
				InputStream input=ClassLoader.getSystemResourceAsStream("javaTemplate.vm");
				
				// generate output
				ve.evaluate(context, writer, "javaClass", new InputStreamReader(input));
				writer.close();
				input.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}
			
		}
		
	}
	
	private SharedClassPMod createPMod(MultiLangugeClass sharedClass){
		SharedClassPMod pMod=new SharedClassPMod(sharedClass);
		for (MultiLangugeFieldBase field : sharedClass.getFields()){
			pMod.getFields().add(createPMod(field));
		}
		return pMod;
	}
	
	private SharedFieldBasePMod createPMod(MultiLangugeFieldBase sharedFieldBase){
		SharedFieldBasePMod pMod=new SharedFieldBasePMod(sharedFieldBase);
		return pMod;
	}
}
