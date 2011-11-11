package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.javaGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.CodeGeneratorBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeFieldBase;

/** Generates Java code from a SharedClass */
public class JavaCodeGenerator extends CodeGeneratorBase {
	private class logger implements LogChute{

		@Override
		public void init(RuntimeServices arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLevelEnabled(int arg0) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void log(int arg0, String arg1) {
			System.out.println(arg1);
		}

		@Override
		public void log(int arg0, String arg1, Throwable arg2) {
			// TODO Auto-generated method stub
			System.out.println(arg1);
		}
		
	}

	@Override
	public void generate(List<MultiLangugeClass> multiLanguageClasses) {
		VelocityEngine ve=new VelocityEngine();
		ve.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new logger());
		ve.init();
		
		// setup type map
		HashMap<String,String> typeMap
			=new HashMap<String, String>();
		typeMap.put("long", "long");
		typeMap.put("bool", "boolean");
		typeMap.put("int", "int");
		for(MultiLangugeClass multiLangugeClass: multiLanguageClasses){
			typeMap.put(multiLangugeClass.getName(), multiLangugeClass.getName());
		}
		
		// generate java code for all classes
		for(MultiLangugeClass multiLangugeClass: multiLanguageClasses){
			try{
				VelocityContext context=new VelocityContext();
				
				// initialize context
				context.put("class", multiLangugeClass);
				
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
		
		// generate SerializerService
		try{
			VelocityContext context=new VelocityContext();
			
			// initialize context
			context.put("classes", multiLanguageClasses);
			
			// open output writer
			FileWriter writer=openWriter("generatedJava/ch/ethz/ruediste/roofline/sharedDOM/MultiLanguageSerializationService.java");
			
			// load template
			InputStream input=ClassLoader.getSystemResourceAsStream("javaSerializationServiceTemplate.vm");
			
			// generate output
			ve.evaluate(context, writer, "javaSerializationService", new InputStreamReader(input));
			writer.close();
			input.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		
		
	}
	
}
