package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLangugeClass;


public abstract class CodeGeneratorBase {
	/** Logger for Velocity which logs everything to System.out */
	private class ConsoleLogger implements LogChute{

		@Override
		public void init(RuntimeServices arg0) throws Exception {
		}

		@Override
		public boolean isLevelEnabled(int arg0) {
			return true;
		}

		@Override
		public void log(int arg0, String arg1) {
			System.out.println(arg1);
		}

		@Override
		public void log(int arg0, String arg1, Throwable arg2) {
			System.out.println(arg1);
		}
	}
		
	/** Velocity engine user for code generation */
	protected final VelocityEngine velocityEngine;

	public CodeGeneratorBase(){
		// initialize the velocity engine
		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new ConsoleLogger());
		velocityEngine.init();
	}
	public abstract void generate(List<MultiLangugeClass> classes);
	
	protected FileWriter openWriter(String fileName) throws IOException{
		File file=new File(fileName);
		file.getParentFile().mkdirs();
		return new FileWriter(file);
	}

	/** applies a template and puts the output in the specified file */
	protected void applyTemplate(String outputFileName, String templateName,
			VelocityContext context, String logTag) {
		try {
			// open output writer
			FileWriter writer = openWriter(outputFileName);

			// load template
			InputStream input = ClassLoader
					.getSystemResourceAsStream(templateName);

			// generate output
			velocityEngine.evaluate(context, writer, logTag,
					new InputStreamReader(input));
			
			// close files
			writer.close();
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

