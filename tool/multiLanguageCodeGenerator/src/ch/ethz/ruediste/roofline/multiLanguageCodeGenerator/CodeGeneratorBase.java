package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;


public abstract class CodeGeneratorBase {
	public abstract void generate(List<MultiLangugeClass> classes);
	
	protected FileWriter openWriter(String fileName) throws IOException{
		File file=new File(fileName);
		file.getParentFile().mkdirs();
		return new FileWriter(file);
	}
}
