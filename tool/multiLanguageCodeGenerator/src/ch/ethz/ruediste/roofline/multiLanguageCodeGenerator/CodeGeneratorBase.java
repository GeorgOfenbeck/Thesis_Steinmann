package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;

import java.io.*;
import java.util.List;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageClassBase;

/**
 * base class for code generators. Initializes the velocity engine and provides
 * a wrapper.
 * 
 */
public abstract class CodeGeneratorBase {
	/** Logger for Velocity which logs everything to System.out */
	private class ConsoleLogger implements LogChute {

		public void init(RuntimeServices arg0) throws Exception {
		}

		public boolean isLevelEnabled(int arg0) {
			return true;
		}

		public void log(int arg0, String arg1) {
			System.out.println(arg1);
		}

		public void log(int arg0, String arg1, Throwable arg2) {
			System.out.println(arg1);
		}
	}

	/** Velocity engine user for code generation */
	protected final VelocityEngine velocityEngine;

	public CodeGeneratorBase() {
		// initialize the velocity engine
		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM,
				new ConsoleLogger());
		velocityEngine.setProperty(Velocity.RESOURCE_LOADER, "classPath");
		velocityEngine
				.setProperty("classPath.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		velocityEngine.init();
	}

	public abstract void generate(List<MultiLanguageClassBase> classes);

	protected FileWriter openWriter(String fileName) throws IOException {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		return new FileWriter(file);
	}

	protected Template getTemplate(String templateName) {
		return velocityEngine.getTemplate(templateName);
	}

	/** applies a template and puts the output in the specified file */
	protected void applyTemplate(Template template, VelocityContext context,
			String outputFileName) {
		try {
			// open output writer
			FileWriter writer = openWriter(outputFileName);

			// generate output
			template.merge(context, writer);

			// close files
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
