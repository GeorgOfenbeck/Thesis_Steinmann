package ch.ethz.ruediste.roofline.sharedEntityGenerator;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.*;

import ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.MultiLanguageClassBase;

/** Generates Java for a list of multi language classes */
public class JavaCodeGenerator extends CodeGeneratorBase {
	public JavaCodeGenerator() {

	}

	@Override
	public void generate(List<MultiLanguageClassBase> multiLanguageClasses) {
		// clear output directory
		{
			File outputDirectory = new File("generatedJava");
			System.out.println("output directory is "
					+ outputDirectory.getAbsolutePath());
			if (outputDirectory.exists()) {
				Utility.deleteDirectory(outputDirectory);
			}
		}

		String basePath = "generatedJava/"
				+ MultiLanguageClassBase.javaBasePackage.replace(".", "/")
				+ "/";

		Template javaTemplate = getTemplate("javaTemplate.vm");

		// generate java code for all classes
		for (MultiLanguageClassBase multiLanguageClass : multiLanguageClasses) {

			VelocityContext context = new VelocityContext();

			// initialize context
			context.put("class", multiLanguageClass);

			context.put("StringUtils", StringUtils.class);

			String outputFileName = basePath
					+ StringUtils.join(multiLanguageClass.getPath(), "/") + "/"
					+ multiLanguageClass.getJavaName() + ".java";

			applyTemplate(javaTemplate, context, outputFileName);
		}

		// generate SerializerService

		// initialize context
		VelocityContext context = new VelocityContext();
		context.put("javaBasePackage", MultiLanguageClassBase.javaBasePackage);
		context.put("classes", multiLanguageClasses);

		String outputFileName = basePath
				+ "MultiLanguageSerializationService.java";
		applyTemplate(getTemplate("javaSerializationServiceTemplate.vm"),
				context, outputFileName);

	}

}
