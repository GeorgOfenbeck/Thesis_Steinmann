package ch.ethz.ruediste.roofline.sharedEntities;


public class Rule extends RuleData {
	public Rule() {
	}

	public Rule(EventPredicateBase predicate, ActionBase action) {
		this();
		setPredicate(predicate);
		setAction(action);
	}

}
