package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeDefine;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeField;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeFieldBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageList;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.TypeDescriptor;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.cGenerator.CCodeGenerator;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.javaGenerator.JavaCodeGenerator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class Main {

	public static void main(String args[]) throws FileNotFoundException{
		// inistialize XStream
		XStream xStream=new XStream(new DomDriver());
		xStream.processAnnotations(MultiLangugeClass.class);
		xStream.processAnnotations(MultiLangugeField.class);
		xStream.processAnnotations(MultiLangugeDefine.class);
		xStream.processAnnotations(MultiLanguageList.class);
		xStream.processAnnotations(MultiLangugeFieldBase.class);
		
		// get input filenames
		File inputDir=new File("./definitions");
		File inputFiles[]=inputDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".xml");
			}
		});
		
		// create Type Descriptors
		HashMap<String, TypeDescriptor> typeDescriptors=new HashMap<String, TypeDescriptor>();
		typeDescriptors.put("int", new TypeDescriptor("int","int","int","%d","nextInt", "Integer"));
		typeDescriptors.put("long", new TypeDescriptor("long","long","long","%ld","nextLong", "Long"));
		typeDescriptors.put("bool", new TypeDescriptor("bool","bool","boolean","%d","nextInt","Boolean"));
		
		// load all class definitions
		List<MultiLangugeClass> classes=new LinkedList<MultiLangugeClass>();
		
		for (File inputFile : inputFiles){
			// load the input
			MultiLangugeClass multiLanguageClass= (MultiLangugeClass) xStream.fromXML(inputFile);
			System.out.println(xStream.toXML(multiLanguageClass));

			classes.add(multiLanguageClass);
			typeDescriptors.put(multiLanguageClass.getName(),new TypeDescriptor(multiLanguageClass.getName()));
		}
		
		for (MultiLangugeClass multiLanguageClass: classes){
			multiLanguageClass.setTypeDescriptors(typeDescriptors);
		}
		
		// instantiate generators
		CodeGeneratorBase generators[]={new JavaCodeGenerator(), new CCodeGenerator()};
				
		// generate code
		for (CodeGeneratorBase generator: generators)
		{
			generator.generate(classes);
		}
	}
}
