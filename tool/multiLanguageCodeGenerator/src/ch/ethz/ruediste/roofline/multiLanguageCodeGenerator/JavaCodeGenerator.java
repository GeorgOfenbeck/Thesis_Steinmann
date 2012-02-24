package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.*;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageClassBase;

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

		// generate java code for all classes
		for (MultiLanguageClassBase multiLanguageClass : multiLanguageClasses) {

			VelocityContext context = new VelocityContext();

			// initialize context
			context.put("class", multiLanguageClass);

			context.put("StringUtils", StringUtils.class);

			String templateName = "javaTemplate.vm";

			String outputFileName = basePath
					+ StringUtils.join(multiLanguageClass.getPath(), "/") + "/"
					+ multiLanguageClass.getJavaName() + ".java";

			applyTemplate(outputFileName, templateName, context, "javaClass");
		}

		// generate SerializerService

		// initialize context
		VelocityContext context = new VelocityContext();
		context.put("javaBasePackage", MultiLanguageClassBase.javaBasePackage);
		context.put("classes", multiLanguageClasses);

		String outputFileName = basePath
				+ "MultiLanguageSerializationService.java";
		String templateName = "javaSerializationServiceTemplate.vm";
		applyTemplate(outputFileName, templateName, context,
				"javaSerializationService");

	}

}
