package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ClassFinder {
	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages and which implement the baseType.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<Class<? extends T>> getClassesImplementing(
			Class<T> baseType,
			String basePackageName) {
		List<Class<?>> classes = getClasses(basePackageName);
		List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();

		// iterate over all classes in the package and return those
		// which implement the baseType
		for (Class<?> clazz : classes) {
			if (baseType.isAssignableFrom(clazz)) {
				result.add((Class<? extends T>) clazz);
			}
		}

		return result;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static List<Class<?>> getClasses(String packageName) {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(path);
		} catch (IOException e) {
			throw new Error(e);
		}
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		List<File> dirs = new ArrayList<File>();
		List<JarFile> jars = new ArrayList<JarFile>();

		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			// System.out.println("resource: " + resource.getPath());
			if (resource.getFile().contains("!")) {
				// the resource is a jar
				try {
					URI jarName = new URI(resource.getFile().split("\\!")[0]);

					// System.out.println("jarName: " + jarName);
					jars.add(new JarFile(new File(jarName)));
				} catch (IOException e) {
					throw new Error(e);
				} catch (URISyntaxException e) {
					throw new Error(e);
				}

			}
			else {
				dirs.add(new File(resource.getFile()));
			}
		}

		for (File directory : dirs) {
			// System.out.println("dir: " + directory);
			classes.addAll(findClasses(directory, packageName));
		}
		for (JarFile jar : jars) {
			// System.out.println("jar: " + jar.getName());
			classes.addAll(findClasses(jar, path));
		}
		return classes;
	}

	private static List<Class<?>> findClasses(JarFile jar, String basePath) {
		// System.out.println("basePath: " + basePath);

		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (Enumeration<JarEntry> it = jar.entries(); it.hasMoreElements();) {
			JarEntry entry = it.nextElement();

			if (entry.getName().startsWith(basePath)
					&& entry.getName().endsWith(".class")) {
				// System.out.println("entry: " + entry.getName());
				try {

					Class<?> clazz = Class.forName(
							entry.getName().substring(
									0,
									entry.getName().length()
											- ".class".length())
									.replace("/", "."));
					if (isClassToBeReturned(clazz)) {
						classes.add(clazz);
					}
				} catch (ClassNotFoundException e) {
					// suppress
				}
			}
		}
		return classes;
	}

	private static boolean isClassToBeReturned(Class<?> clazz) {
		return clazz.getEnclosingClass() == null;
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			// System.out.println("file: " + file);
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file,
						packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				try {
					Class<?> clazz = Class.forName(packageName
							+ '.'
							+ file.getName().substring(0,
									file.getName().length() - 6));
					if (isClassToBeReturned(clazz)) {
						classes.add(clazz);
					}
				} catch (ClassNotFoundException e) {
					// suppress
				} catch (NoClassDefFoundError e) {
					// suppress
				}
			}
		}
		return classes;
	}

	/**
	 * Finds all static fields declared in the given package and it's sub
	 * packages and extracts it's value along with the class the field is
	 * defined in
	 * 
	 * @param fieldType
	 *            type of the fields to be extracted
	 * @param packageName
	 *            package of the classes to be searched
	 */
	static public <T> List<Pair<Class<?>, T>> getStaticFieldValues(
			Class<T> fieldType, String packageName) {
		List<Pair<Class<?>, T>> values = new ArrayList<Pair<Class<?>, T>>();

		// get all classes in the package
		List<Class<?>> classes = ClassFinder.getClasses(packageName);

		// loop over all classes
		for (Class<?> clazz : classes) {
			// loop over the fields in the class
			for (Field field : clazz.getDeclaredFields()) {
				// check if the field is static and a ConfigurationKey
				if (Modifier.isStatic(field.getModifiers())
						&& fieldType.isAssignableFrom(field
								.getType()))
				{
					// the field contains a configuration key
					try {
						field.setAccessible(true);

						// retrieve the configuration key object
						@SuppressWarnings("unchecked")
						T key = (T) field
								.get(null);

						// add the configuration key to the result list
						values.add(ImmutablePair.<Class<?>, T> of(
								clazz, key));
					} catch (IllegalArgumentException e) {
						// ignore
					} catch (IllegalAccessException e) {
						// ignore
					}
				}
			}
		}
		return values;
	}
}