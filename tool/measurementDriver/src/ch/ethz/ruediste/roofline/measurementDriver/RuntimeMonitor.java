package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.*;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;

/**
 * The runtime monitor moitors the time spent for various tasks. The time is
 * categorized into one of various categories. Each time quantum can only be
 * categorized into a single category.
 * 
 * To accomplish this, the runtime monitor manages a stack, containing the
 * categories which have been entered. At all times, only the timer of the top
 * of the stack is running.
 */
@Singleton
public class RuntimeMonitor {
	private static Logger log = Logger.getLogger(RuntimeMonitor.class);

	/**
	 * a time category. use enter() and leave() to start/stop categorizing time
	 */
	public class Category {
		public Category(String name) {
			this.name = name;
			this.parent = null;
		}

		public Category(String name, Category parent) {
			this.name = name;
			this.parent = parent;
			parent.children.add(this);
		}

		private final List<Category> children = new LinkedList<Category>();
		private final Category parent;
		private final String name;

		private long start;
		private long categoryTime;
		private int startCount;

		private void startTimer() {
			start = System.currentTimeMillis();
		}

		private void stopTimer() {
			categoryTime += System.currentTimeMillis() - start;
		}

		/**
		 * start accounting time into this category.
		 */
		public void enter() {
			startCount++;
			RuntimeMonitor.this.enter(this);
		}

		/**
		 * stop accounting time into this category
		 */
		public void leave() {
			RuntimeMonitor.this.leave(this);
		}

		/**
		 * get the time spent in this category, not including the time spent in
		 * any children
		 */
		public long getCategoryTime() {
			return categoryTime;
		}

		/**
		 * get the time spent in this category and any of the children
		 */
		public long getTotalTime() {
			long result = categoryTime;
			for (Category c : getChildren()) {
				result += c.getTotalTime();
			}
			return result;
		}

		/**
		 */
		public List<Category> getChildren() {
			return Collections.unmodifiableList(children);
		}

		public Category getParent() {
			return parent;
		}

		public String getName() {
			return name;
		}
	}

	private final Stack<Category> stack = new Stack<RuntimeMonitor.Category>();

	private void enter(Category category) {
		// stop the timer of the current stack head
		if (!stack.isEmpty()) {
			stack.peek().stopTimer();
		}

		stack.push(category);
		category.startTimer();
	}

	private void leave(Category category) {
		// check if the category is present at all
		if (!stack.contains(category)) {
			log.warn("category " + category.getName() + " was not on stack");
			// abort
			return;
		}

		// pop the head
		Category head = stack.pop();

		// stop the timer of the head
		head.stopTimer();

		// warn and pop further elements if necessary
		if (head != category) {
			log.warn("non matching leave of runtime category "
					+ category.getName() + ". The stack head was "
					+ head.getName());

			// pop till we find the category
			while (!stack.isEmpty() && head != category)
				head = stack.pop();

		}

		if (!stack.isEmpty()) {
			// start the timer of the new head
			stack.peek().startTimer();
		}
	}

	public Category rootCategory = new Category("root");
	public Category hashingCategory = new Category("hashing", rootCategory);
	public Category buildCategory = new Category("build", rootCategory);
	public Category buildPreparationCategory = new Category("buildPreparation",
			buildCategory);
	public Category compilationCategory = new Category("compilation",
			buildCategory);
	public Category runMeasurementCategory = new Category("run measurement",
			rootCategory);
	public Category loadMeasurementResultsCategory = new Category(
			"load measurements", rootCategory);
	public Category startupCategory = new Category("startup", rootCategory);

	public void print() {
		System.out.println("Execution Time Spent");
		print(rootCategory, 0);
		System.out.println();
	}

	private void print(Category category, int indent) {
		for (int i = 0; i < indent; i++)
			System.out.print("  ");
		System.out.printf("%s: #%d %.1f", category.getName(),
				category.startCount, category.getCategoryTime() / 1000.0);

		if (category.getChildren().size() > 0) {

			System.out.printf(" %.1f\n", category.getTotalTime() / 1000.0);

			for (Category c : category.getChildren()) {
				print(c, indent + 1);
			}
		}
		else {
			System.out.println();
		}
	}
}
