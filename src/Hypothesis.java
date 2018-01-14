import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Mar 24, 2017
 * Course:  CSE 5693, Fall 2017
 * Project: HW4, Genetic Algorithms
 * 
 * Hypothesis is a bit string, containing *at least* one rule (several attribute-values and one target).
 * For Tennis data set, a rule could look like:
 * 001 011 01 01 1
 * And it is interpreted as 
 * Outlook(rain) ^ Temperature(mild & cool) ^ Humidity(normal) ^ Wind(strong) ==> Play(yes)
 * 
 * A Hypothesis could look like:
 * h1: 10 01 1 11 10 0 where the first 5 bits belong to the first rule.
 * 
 */
public class Hypothesis implements Comparable<Hypothesis>{
	
	private List<Rule> hypo = new ArrayList<>(); // a set of rules (first it comprises of 1 rule)
	private int size; // number of rules in a hypothesis
	private double fitness; // classification accuracy of the hypothesis
	private double Pr; // the proportional probability Pr(h) = Fitness(h)/Fitness(all)
	
	Hypothesis(int rule_l,int target_l, int[] boundaries){
		Rule rule = new Rule(rule_l, target_l, boundaries);
		this.add2hypo(rule);
		
	}
	
	Hypothesis (){
		;
	}
	
	protected void add2hypo(Rule r) {
		this.setSize(++size);
		this.hypo.add(r);
	}
	
	protected List<Rule> getHypo(){
		return this.hypo;
	}
	
	protected Rule getHypo(int r_idx){
		return this.getHypo().get(r_idx);
	}
	
	protected void setHypo (int r_idx, Rule r){
		for (int i = 0; i < r.getLength(); i++){
			this.setHypo(r_idx, i, r.getData(i));
		}
	}
	
	protected void setHypo (int r_idx, int d_idx, int d_value){
		this.getHypo().get(r_idx).setData(d_idx, d_value);
	}

	public Rule getRule(int idx){
		return this.getHypo().get(idx);
	}
	
	public int getRule(int r_idx, int idx){
		return this.getHypo().get(r_idx).getData(idx);
	}
	
	public void setSize(int argin){
		this.size = argin;
	}
	
	public int getSize(){
		return this.size;
	}
	
	private void setFitness(double argin) {
		this.fitness = argin;
		
	}
	
	public double getFitness(){
		return this.fitness;
	}
	
	public void setPr(double argin){
		this.Pr = argin;
	}
	
	public double getPr(){
		return this.Pr;
	}

	
	public Hypothesis generate_random_hypo (int[] boundaries, Random rnd, String name_dataset){
		// bit strings are formed randomly. They will be validated later on. (No 000 is acceptable!)
		// The initial population comprises of only one random rule.
		
		// 1st RULE
		Rule r = this.getRule(0); // we have only one rule at the moment.
		r.generateRandomly(rnd, name_dataset); // reference copy

		// 2nd RULE
		// add another rule to hypo since each individual has 2 rules
		r = new Rule(r.getLength(), r.getTargetLength(), r.getBoundaries());
		
		boolean valid = false;
		while (!valid){
			r.generateRandomly(rnd, name_dataset);
			
			this.add2hypo(r);
			valid = this.checkvalidity();
			if (!valid){
				this.remove(-1);
			}
		}

		return this;
	}

	protected boolean identical (Hypothesis cand_hypo){
		// assume we have only one rule in a hypo at the step.
		boolean identical = false;
		for (Rule r: this.getHypo()){
			identical = r.identical(cand_hypo.getHypo(0));
		}
		
		return identical;
	}
	public Hypothesis generate_init_hypo(int[] boundaries, Exp exp, Random rnd, String name_dataset) {
		// use this function for iris data set
		
		// 1st RULE
		Rule r = this.getRule(0); // we have only one rule at the moment.
		
		r.setData(exp.getBitstring());
		for (int i = 0; i < r.getTargetLength(); i++){
			r.setTarget(i, exp.getBitstring_target()[i]);
		}
		
		// 2nd RULE
		// add another rule to hypo since each individual has 2 rules
		r = new Rule(r.getLength(), r.getTargetLength(), r.getBoundaries());
		
		boolean valid = false;
		while (!valid){
			r.generateRandomly(rnd, name_dataset);
			
			this.add2hypo(r);
			valid = this.checkvalidity();
			if (!valid){
				this.remove(-1);
			}
		}
		//System.out.println();
		return this;
		
	}
	
	protected boolean checkvalidity() {
		// 1) we should have only one bit of value 1 in the target, at most.
		// 2) we should not have all values 0 for iris

		boolean valid;
		//if (this.getSize() == 0){
		//	System.out.println();
		//	return false;
		//}
		if (this.getHypo(0).getTargetLength() == 1) {// tennis
			boolean Onebefore = false;
			// check data validity
			for (int r = 0; r < this.getSize(); r++) {
				valid = false;
				int idx = 0;
				for (int i = 0; i < this.getHypo(r).getBoundaries().length; i++) {

					valid = false;
					Onebefore = false;
					int min = (i != 0) ? (idx) : 0;
					int max = min + this.getHypo(r).getBoundaries(i);

					for (int b : Arrays.copyOfRange(this.getHypo(r).getData(), min, max)) {
						if (b == 1) {
							valid = true;
							Onebefore = true;

						} else {
							if (Onebefore) {
								valid = true;
							}
						}

					} // end of for
					if (!valid) {
						return false;
					}

					idx += this.getHypo(r).getBoundaries(i);

				} // end of for
			} // end of outer for

		}
		
		
		// for both data sets we don't want all data 1
		/*for (int r = 0; r < this.getSize(); r++){
			int sum = 0, sum2 = 0;
			int rule_length = this.getHypo(r).getLength();
			int target_length = this.getHypo(r).getTargetLength();
			for (int i = 0; i < rule_length - target_length; i++){
				sum += this.getHypo(r).getData(i);
			}
			
			if ((rule_length - target_length - sum ) == 0){
				return false;
			}else{
				valid = true;
			}
		}*/
		
		
		
		// check 1 and 2
		valid = false;
		for (int r = 0; r < this.getSize(); r++){
			valid = false;
			for (int b: this.getHypo(r).getTarget()){
				if (b == 1){
					if (valid){
						return false;
					}else{
						valid = true;
					}
				}
			}
		}
		
		// if there is no 1 at all for iris return false
		if (this.getSize() != 0) {
			if ((!valid) && (this.getHypo(0).getTargetLength() != 1)) {
				return false;
			}
		} else {

			// System.out.println();
			return false;
		}
		// otherwise it's ok to have only 0 (tennis dataset)
		return true;
	}
	
	protected Hypothesis remove (int i) {
		if (i == -1){
			this.hypo.remove(this.size - 1 );
			this.setSize(this.getSize() - 1);
		}else{
			this.hypo.remove(i);
			this.setSize(this.getSize() - 1);
		}
		return this;
	}
		
	/**
	 * 
	 * @param examples training examples
	 * @param name_dataset data set's name
	 * @param attrs data set features
	 * @param attrs_orig 
	 * @param attr_vals attributes and their values
	 * @param classes possible target values
	 * @param boundaries indices showing each attribute's bit length in the bit string
	 * @param target_default a target value in case voting is not unanimous.
	 * @return Evaluated fitness values for each hypothesis
	 */
	protected double computeFitness (List<Exp> examples, String name_dataset, String[] attrs,
			String[] attrs_orig, HashMap<String, ArrayList<String>> attr_vals, int[] classes, int[] boundaries, String target_default){
		// The classification accuracy of the hypothesis could represent the
		// Fitness function
		
		// covered examples
		/*List<Exp> covered_exps = new ArrayList<>();
		for (Exp e: examples){
			covered_exps.add(e.clone());
		}*/
		
		// first assign coverage scores for each rule of the hypothesis
		for (int i = 0; i < this.size; i++){
			Rule r_tmp = this.getRule(i);
			r_tmp.assign_ruleScore(examples, attrs, attr_vals);
		}
		
		// calculate the fitness (accuracy of the hypothesis over examples)
		double fitness = calFitness(examples, attrs, attr_vals, target_default);
		
		// set the fitness to this object
		this.setFitness(fitness);

		
		return .0;
	}
	/*
	public int[] getTarget(int r_idx){
		
		int[] tmp = this.getHypo(r_idx);
		
		tmp.toString().substring(this.getBoundaries(this.getBoundaries().length -1 ), )
		
		return null;
	}
	
	*/


	/**
	 * 
	 * @param examples
	 *            all examples
	 * @param attrs
	 *            attributes
	 * @param attr_vals
	 *            attribute_values
	 * @param target_default 
	 * @return fitness value of the hypothesis
	 */
	protected double calFitness(List<Exp> examples, String[] attrs, HashMap<String, ArrayList<String>> attr_vals, String target_default) {

		double acc = .0; double matched_examples_count = examples.size();
		
		// go through a loop over examples
		for (Exp exp : examples) {
			
			if (exp.getTarget().equals("Iris-setosa")){
				//System.out.println();
			}else{
				//System.out.println();
			}
			double num_matched_rules = .0;
			ArrayList<Rule> matched_rules = new ArrayList<>();
			
			// check all matched rules are in the hypo for one example
			for (int i = 0; i < this.size; i++) {
				boolean match = true;
				
				Rule rule = this.getRule(i);	
				
				// see if the example matches the rule (exclude the target comparison here)
				match = checkMatch(exp.getBitstring(), rule.getData(), rule.getTargetLength());
				
				// count matched rules
				if (match){
					matched_rules.add(rule);
					num_matched_rules++;
				}
			} // end of inner loop
			
			assert num_matched_rules >= 0;
			
			
			if (num_matched_rules == 0){ // if no rules was found
				acc += cal_accuracy (exp, matched_rules, target_default);
				//matched_examples_count--;
				//acc += .0;
			}else if (num_matched_rules == 1){ // if one rule is found
				acc += cal_accuracy (exp, matched_rules, target_default);
				
			}else { // if multiple rules are found (use rules' scores for voting)
				acc += cal_accuracy (exp, matched_rules, target_default);
			}

		} // end of outer loop

		
		if (acc == .0) return .0;
		
		double fitness = acc/examples.size();
		
		return fitness;

	}
	

	private double cal_accuracy(Exp exp, ArrayList<Rule> matched_rules, String target_default) {
		
		int[] target_exp, target_rule ; double acc = 0.0; 
		
		if (matched_rules.isEmpty()){
			// compare the example's target with target default
			String t_exp = exp.getTarget();
			acc = (t_exp.equalsIgnoreCase(target_default))? 1:0;
			return acc;
			
		}
		
		if (matched_rules.size() == 1){
			Rule rule = matched_rules.get(0);
			
			// get targets from example
			target_exp = exp.getBitstring_target();
			
			// get target from the rule
			target_rule = rule.getTarget();
			
			// check whether the targets match
			if (checkMatch(target_exp, target_rule, 0)){
				acc++;
			}
			
			return acc;
			
		}else{
			// among matched rules select the one with the highest score
			//Rule rule = getHighestScoredRule(matched_rules);
			
			// get targets from example
			target_exp = exp.getBitstring_target();
			
			// get majority target from the rule
			target_rule = cal_majority_target(matched_rules);
			
			//target_rule[0] = 0; target_rule[1] = 0; target_rule[2] = 1;
			
			// get target from the rule (instead of majority calculate highest score)
			//target_rule = rule.getTarget();
			
			// check whether the targets match
			if (checkMatch(target_exp, target_rule, 0)){
				acc++;
			}
			
			return acc;
			
		}

	}



	private int[] cal_majority_target(ArrayList<Rule> matched_rules) {
		
		int size = matched_rules.get(0).getTargetLength();

		int[] sentinel = new int[size]; // [0, 0, 0]
		
		int[] output = new int[size];
		
		if (size == 1){ // tennis
			for (Rule r : matched_rules) {
				for (int i = 0; i < size; i++) {
					sentinel[i] += r.getTarget(i);
				}
			}
			
			output[0] = ((Double.valueOf(sentinel[0])/matched_rules.size()) >= .5)? 1: 0; 
			
		}else{ // iris

			for (Rule r : matched_rules) {
				for (int i = 0; i < size; i++) {
					sentinel[i] += r.getTarget(i);
				}
			}

			// get max
			int max = Integer.MIN_VALUE, idx = 0;
			for (int i = 0; i < size; i++) {
				max = (sentinel[i] >= max) ? sentinel[i] : max;
				idx = (sentinel[i] >= max) ? i : idx;
			}

			output[idx] = 1;

		}
		
		
		return output;
	}

	private Rule getHighestScoredRule(ArrayList<Rule> matched_rules) {
		Rule cand;
		if (matched_rules.size() == 1){
			return matched_rules.get(0);
		}else{
			cand = matched_rules.get(0);
			for (int i = 1; i < matched_rules.size(); i++){
				if (matched_rules.get(i).compareTo(cand) == 1){
					cand = matched_rules.get(i);
				}
			}
		}
		return cand;
	}



	private boolean checkMatch(int[] argin1, int[] argin2, int l) {
		// verify those indices with value 1 are in both the example and rule
		// l is 0 when we compare the targets themselves
		// otherwise it's the target_l

		if (argin1.length == 1){ // tennis target
			if (argin1[0] == argin2[0]){
				return true;
			}else{
				return false;
			}
		}
		for (int i = 0; i < argin1.length - l; i++){
			if (argin1[i] == 1){
				// now check it in the rule
				if (argin2[i] != 1){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public double cal_Pr(List<Hypothesis> P) {
		double sum = .0;
		
		for (Hypothesis h : P){
			sum += h.getFitness();
		}
		//assert sum != 0;
		this.setPr(this.getFitness()/sum);
		return this.getPr();
		
	}
	
	@Override
	public int compareTo(Hypothesis h) {
		// compare scores
		
		if (this.getPr() > h.getPr()){
			return 1;
		}else if (this.getPr() == h.getPr()){
			return 0;
		}else{
			return -1;
		}
		/*
		if (this.getFitness() > h.getFitness()){
			return 1;
		}else if (this.getFitness() == h.getFitness()){
			return 0;
		}else{
			return -1;
		}
		*/
	}

	public Hypothesis clone() {
		Hypothesis new_hypo = new Hypothesis();

		for (int i = 0; i < this.getSize(); i++) {
			Rule r_tmp = this.getHypo(i).clone();

			new_hypo.add2hypo(r_tmp);
		}
		
		new_hypo.setFitness(this.getFitness());
		new_hypo.setPr(this.getPr());
		new_hypo.setSize(this.getSize());
		

		return new_hypo;

	}

	public void resetCrossover_points() {
		// assing inf to all crossover points of rules of hypo
		
		for (Rule r: this.getHypo()){
			r.setCrossover_point_d1(Integer.MAX_VALUE);
			r.setCrossover_point_d2(Integer.MAX_VALUE);
		}
		
	}

	
	
/*
	private List<Exp> derive_matched_examples(List<Exp> covered_exps, String[] attrs, int[] boundaries, HashMap<String, ArrayList<String>> attr_vals) {
		
		// look over examples and narrow down until your hypothesis covers them maximally.
		// eliminate those examples that are not matched to the hypothesis
		for (Exp e: covered_exps){
			// check whether this example matches against 
			// the this hypo for the candidate attr (e.g. Outlook)
			
			// check for each attribute
			for (int i = 0; i < attrs.length; i++){
				
				int idx = 0;
				int min = (i != 0)? (idx):0;
				int max = min + boundaries[i];
				
				String candidate_attr = attrs[i]; // Outlook
				
				// get the index of the example's attr-val 
				int pin = getAttributeValue_idx(e, i, candidate_attr, attr_vals);
				
				// check if we consider such attr-val in the hypo (is it 0 or 1?)
				boolean inclusion = keepExamples (this, pin, i, boundaries);
				
				
				for (int j = min; j < max; j++){
					// search what values are considered in this rule for the candid. attr.
					
					
					
				} // end of inner for
				
				idx += boundaries[i];
			} // end of middle for
			
			
		} // end of outer for
		
		
		
		return covered_exps;
		
	}

	private boolean keepExamples(Hypothesis hypothesis, int pin, int i, int[] boundaries) {
		// returns false or true for 0/1

		int hypo_IDX = (i != 0)? (pin + boundaries[i - 1]):boundaries[i];
		
		;
		
		
		return false;
	}

	private int getAttributeValue_idx(Exp e, int attr_index, String candidate_attr, HashMap<String, ArrayList<String>> attr_vals) {
		// returns the index of the particular attribute-value in the example
		// according to the candidate attribute
		
		String attr_val = e.get(attr_index); // e.g. sunny
		
		ArrayList<String> tmp = attr_vals.get(candidate_attr); // [sunny, overcast, rain]
		int attr_val_idx = Arrays.asList(tmp).indexOf(attr_val); // 0
		
		return attr_val_idx;
	}*/

}
