package ch.ethz.ruediste.roofline.sharing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import ch.ethz.ruediste.roofline.sharing.DOM.SharedClass;
import ch.ethz.ruediste.roofline.sharing.DOM.SharedDefine;
import ch.ethz.ruediste.roofline.sharing.DOM.SharedField;
import ch.ethz.ruediste.roofline.sharing.DOM.SharedFieldBase;
import ch.ethz.ruediste.roofline.sharing.DOM.SharedList;
import ch.ethz.ruediste.roofline.sharing.cGenerator.CCodeGenerator;
import ch.ethz.ruediste.roofline.sharing.javaGenerator.JavaCodeGenerator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class Main {

	public static void main(String args[]) throws FileNotFoundException{
		if (args.length!=1){
			System.out.println("usage: shared <classDefinition.xml>");
			System.exit(1);
		}
		
		// inistialize XSTream
		XStream xStream=new XStream(new DomDriver());
		xStream.processAnnotations(SharedClass.class);
		xStream.processAnnotations(SharedField.class);
		xStream.processAnnotations(SharedDefine.class);
		xStream.processAnnotations(SharedList.class);
		xStream.processAnnotations(SharedFieldBase.class);
		
		// get input filename
		File inputFile=new File(args[0]);
		
		// load the input
		SharedClass sharedClass= (SharedClass) xStream.fromXML(inputFile);
		System.out.println(xStream.toXML(sharedClass));
		
		// instantiate generators
		ICodeGenerator generators[]={new JavaCodeGenerator(), new CCodeGenerator()};
				
		// generate code
		for (ICodeGenerator generator: generators){
			FileWriter writer;
			try {
				writer = new FileWriter(inputFile.getAbsoluteFile().getParent()+inputFile.separator+sharedClass.getName()+generator.getExtension());
				generator.generate(sharedClass,writer);
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
