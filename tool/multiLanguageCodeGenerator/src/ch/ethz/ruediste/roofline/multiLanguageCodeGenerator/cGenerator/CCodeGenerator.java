package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.cGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.CodeGeneratorBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeFieldBase;

/** generates c code from a shared class */
public class CCodeGenerator extends CodeGeneratorBase{

	@Override
	public void generate(List<MultiLangugeClass> multiLanguageClasses) {
		VelocityEngine ve=new VelocityEngine();

		// generate java code for all classes
		for(MultiLangugeClass multiLangugeClass: multiLanguageClasses){
			try {
				VelocityContext context=new VelocityContext();
				
				// initialize context
				context.put("class", multiLangugeClass);
				
				// open output writer
				FileWriter writer = openWriter("generatedC/"+multiLangugeClass.getName()+".h");
				
				// load template
				InputStream input=ClassLoader.getSystemResourceAsStream("cTemplate.vm");
				
				// generate output
				ve.evaluate(context, writer, "cClass", new InputStreamReader(input));
				writer.close();
				input.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// generate SerializerService
		try{
			VelocityContext context=new VelocityContext();
			
			// initialize context
			context.put("classes", multiLanguageClasses);
			
			// open output writer
			FileWriter writer=openWriter("generatedC/MultiLanguageSerializationService.cpp");
			
			// load template
			InputStream input=ClassLoader.getSystemResourceAsStream("cSerializationServiceTemplate.vm");
			
			// generate output
			ve.evaluate(context, writer, "cSerializationService", new InputStreamReader(input));
			writer.close();
			input.close();
			
			// open output writer
			writer=openWriter("generatedC/MultiLanguageTypeEnum.h");
			
			// load template
			input=ClassLoader.getSystemResourceAsStream("cMultiLanguageTypeEnumTemplate.vm");
			
			// generate output
			ve.evaluate(context, writer, "cMultiLanguageTypeEnum", new InputStreamReader(input));
			writer.close();
			input.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
}
