package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.FieldTypeDescriptor;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageList;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeDefine;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeField;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeFieldBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.cGenerator.CCodeGenerator;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.javaGenerator.JavaCodeGenerator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/** loads all class definitions and generates the code*/
public class Main {

	public static void main(String args[]) throws FileNotFoundException{
		
		// get input filenames
		File[] inputFiles = scanClassDefinitionDirectory(new File("./definitions"));
		
		// create Type Descriptors
		HashMap<String, FieldTypeDescriptor> typeDescriptors = createTypeDescriptorsForPrimitiveTypes();
		
		// load the class definitions
		List<MultiLangugeClass> classes = loadClassDefinitions(inputFiles);
		
		// create type descriptor for each loaded class
		for (MultiLangugeClass multiLanguageClass: classes){
			typeDescriptors.put(multiLanguageClass.getName(),new FieldTypeDescriptor(multiLanguageClass.getName()));
		}
		
		// set type descriptors of all fields in all classes
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

	private static List<MultiLangugeClass> loadClassDefinitions(File[] inputFiles) {
		// inistialize XStream
		XStream xStream = createXStream();
		
		// load all classes
		List<MultiLangugeClass> classes=new LinkedList<MultiLangugeClass>();
		for (File inputFile : inputFiles){
			// load the input
			MultiLangugeClass multiLanguageClass= (MultiLangugeClass) xStream.fromXML(inputFile);
			System.out.println(xStream.toXML(multiLanguageClass));

			// add class to the list of classes
			classes.add(multiLanguageClass);
		}
		return classes;
	}

	/** Scan the given directory for all files containing class definitions (all .xml files) */
	private static File[] scanClassDefinitionDirectory(File classDefinitionDirectory) {
		File inputDir=classDefinitionDirectory;
		File inputFiles[]=inputDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".xml");
			}
		});
		return inputFiles;
	}

	/** initialize XStream and process annotaions*/
	private static XStream createXStream() {
		XStream xStream=new XStream(new DomDriver());
		xStream.processAnnotations(MultiLangugeClass.class);
		xStream.processAnnotations(MultiLangugeField.class);
		xStream.processAnnotations(MultiLangugeDefine.class);
		xStream.processAnnotations(MultiLanguageList.class);
		xStream.processAnnotations(MultiLangugeFieldBase.class);
		return xStream;
	}

	/** create type descirptors for supported primitive types*/ 
	private static HashMap<String, FieldTypeDescriptor> createTypeDescriptorsForPrimitiveTypes() {
		HashMap<String, FieldTypeDescriptor> typeDescriptors=new HashMap<String, FieldTypeDescriptor>();
		typeDescriptors.put("int", new FieldTypeDescriptor("int","int","int","%d","nextInt", "Integer"));
		typeDescriptors.put("long", new FieldTypeDescriptor("long","long","long","%ld","nextLong", "Long"));
		typeDescriptors.put("bool", new FieldTypeDescriptor("bool","bool","boolean","%d","nextInt","Boolean"));
		typeDescriptors.put("double", new FieldTypeDescriptor("double","double","double","%lf","nextDouble","Double"));
		return typeDescriptors;
	}
}
