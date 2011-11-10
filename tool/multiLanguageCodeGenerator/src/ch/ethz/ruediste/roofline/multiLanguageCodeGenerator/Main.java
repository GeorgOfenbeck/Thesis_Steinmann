package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeDefine;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeField;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeFieldBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeList;
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
		xStream.processAnnotations(MultiLangugeList.class);
		xStream.processAnnotations(MultiLangugeFieldBase.class);
		
		// get input filenames
		File inputDir=new File("./definitions");
		File inputFiles[]=inputDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".xml");
			}
		});
		
		// load all class definitions
		List<MultiLangugeClass> classes=new LinkedList<MultiLangugeClass>();
		
		for (File inputFile : inputFiles){
			// load the input
			MultiLangugeClass multiLanguageClass= (MultiLangugeClass) xStream.fromXML(inputFile);
			System.out.println(xStream.toXML(multiLanguageClass));
			classes.add(multiLanguageClass);
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
