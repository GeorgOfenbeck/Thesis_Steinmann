package ch.ethz.ruediste.roofline.sharedEntityGenerator;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/** loads all class definitions and generates the code */
public class Main {

	public static void main(String args[]) throws FileNotFoundException {

		// get input filenames
		File classDefinitionDirectory = new File("./definitions");

		File[] inputFiles = scanClassDefinitionDirectory(classDefinitionDirectory);

		// create Type Descriptors
		HashMap<String, FieldTypeDescriptor> typeDescriptors = createTypeDescriptorsForPrimitiveTypes();

		// load the class definitions
		List<MultiLanguageClassBase> classes = loadClassDefinitions(inputFiles,
				classDefinitionDirectory);

		// create type descriptor for each loaded class
		for (MultiLanguageClassBase multiLanguageClass : classes) {
			typeDescriptors.put(multiLanguageClass.getName(),
					new FieldTypeDescriptor(multiLanguageClass));
		}

		// set type descriptors of all fields in all classes
		for (MultiLanguageClassBase multiLanguageClass : classes) {
			multiLanguageClass.setTypeDescriptors(typeDescriptors);
			if (multiLanguageClass instanceof MultiLanguageDerivedClass) {
				MultiLanguageDerivedClass derivedClass = (MultiLanguageDerivedClass) multiLanguageClass;
				derivedClass.setBaseClass(findBaseClass(derivedClass, classes));
			}
		}

		// instantiate generators
		CodeGeneratorBase generators[] = { new JavaCodeGenerator(),
				new CCodeGenerator() };

		// generate code
		for (CodeGeneratorBase generator : generators) {
			generator.generate(classes);
		}
	}

	/**
	 * Finds the base class of a bulti language class
	 * 
	 * @param multiLanguageClass
	 *            class to find the base type for
	 * @param classes
	 *            list of all available classes
	 * @return
	 */
	private static MultiLanguageClassBase findBaseClass(
			MultiLanguageDerivedClass multiLanguageClass,
			List<MultiLanguageClassBase> classes) {
		// if there is no base type, there is no base class
		if (multiLanguageClass.getBaseType() == null) {
			return null;
		}

		// iterate over all available classes and return the first matching one
		for (MultiLanguageClassBase baseClass : classes) {
			if (baseClass.getName().equals(multiLanguageClass.getBaseType())) {
				return baseClass;
			}
		}

		throw new Error(String.format("base class %s of classs %s not found",
				multiLanguageClass.getBaseType(), multiLanguageClass.getName()));
	}

	/**
	 * load all class definitions defined in the given input files
	 * 
	 * @param classDefinitionDirectory
	 *            base directory of the class definitions
	 */
	private static List<MultiLanguageClassBase> loadClassDefinitions(
			File[] inputFiles, File classDefinitionDirectory) {
		// inistialize XStream
		XStream xStream = createXStream();

		// load all classes
		List<MultiLanguageClassBase> classes = new LinkedList<MultiLanguageClassBase>();
		for (File inputFile : inputFiles) {
			System.out.println("loading " + inputFile.getAbsolutePath());
			// load the input
			MultiLanguageClassBase multiLanguageClass = (MultiLanguageClassBase) xStream
					.fromXML(inputFile);

			// set the name and the path of the loaded class
			multiLanguageClass.setName(StringUtils.removeEnd(
					inputFile.getName(), ".xml"));
			multiLanguageClass.setPath(getRelativePath(
					classDefinitionDirectory, inputFile.getParentFile()));

			System.out.printf("name: %s path: %s\n",
					multiLanguageClass.getName(),
					StringUtils.join(multiLanguageClass.getPath(), "/"));
			//System.out.println(xStream.toXML(multiLanguageClass));

			// add class to the list of classes
			classes.add(multiLanguageClass);
		}
		return classes;
	}

	/**
	 * get's the relative path from parent to child
	 * 
	 * @param classDefinitionDirectory
	 * @param parentFile
	 * @return
	 */
	private static List<String> getRelativePath(File parent, File child) {
		File current = child;
		ArrayList<String> result = new ArrayList<String>();

		while (!current.equals(parent)) {
			// add the current directory to the path
			result.add(current.getName());

			// move one level up in the hierarchy
			current = current.getParentFile();
		}

		// since we added the directories in reverse order, 
		// reverse the result
		Collections.reverse(result);

		return result;
	}

	/**
	 * Scan the given directory for all files containing class definitions (all
	 * .xml files), including sub directories (recursively)
	 */
	private static File[] scanClassDefinitionDirectory(
			File classDefinitionDirectory) {

		// check if the definition directory exists
		if (!classDefinitionDirectory.exists()) {
			throw new Error(
					"Directory containing the class definitions does not exist: "
							+ classDefinitionDirectory.getAbsolutePath());
		}

		// get all sub directories
		File inputDirectoriesFiles[] = classDefinitionDirectory
				.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						// get all directories
						return pathname.isDirectory();
					}
				});

		// get all files in all subdirectories
		ArrayList<File> inputFiles = new ArrayList<File>();

		// scan subdirectories recursively
		for (File dir : inputDirectoriesFiles) {
			Collections.addAll(inputFiles, scanClassDefinitionDirectory(dir));
		}

		// add files in the current directory
		Collections.addAll(inputFiles,
				classDefinitionDirectory.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						// get all .xml files
						return !pathname.isDirectory()
								&& pathname.getName().endsWith(".xml");
					}
				}));

		return inputFiles.toArray(new File[] {});
	}

	/** initialize XStream and process annotaions */
	private static XStream createXStream() {
		XStream xStream = new XStream(new DomDriver());
		xStream.processAnnotations(MultiLanguageClass.class);
		xStream.processAnnotations(MultiLanguageField.class);
		xStream.processAnnotations(MultiLanguageList.class);
		xStream.processAnnotations(MultiLanguageFieldBase.class);
		xStream.processAnnotations(MultiLanguageDerivedClass.class);
		return xStream;
	}

	/** create type descriptors for supported primitive types */
	private static HashMap<String, FieldTypeDescriptor> createTypeDescriptorsForPrimitiveTypes() {
		HashMap<String, FieldTypeDescriptor> typeDescriptors = new HashMap<String, FieldTypeDescriptor>();
		typeDescriptors.put("int", new FieldTypeDescriptor("int", "int32_t",
				"int", "%d", "nextInt", "Integer", "0", null));
		typeDescriptors.put("long", new FieldTypeDescriptor("long", "int64_t",
				"long", "%\"PRId64\"", "nextLong", "Long", "0", null));
		typeDescriptors.put("bool", new FieldTypeDescriptor("bool", "bool",
				"boolean", "%d", "nextInt", "Boolean", "0", null));
		typeDescriptors
				.put("double", new FieldTypeDescriptor("double", "double",
						"double", "%lf", "nextDouble", "Double", "0.0", null));

		typeDescriptors.put("string", new FieldTypeDescriptor("string",
				"std::string", "String", "%c", "nextLine", "String", "\"\"",
				"\"\""));

		typeDescriptors.put("ulong", new FieldTypeDescriptor("ulong",
				"uint64_t", "BigInteger", "%\"PRIu64\"", "nextBigInteger",
				"BigInteger", "0", "BigInteger.ZERO"));

		return typeDescriptors;
	}
}
