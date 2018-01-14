import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Mar 27, 2017
 * Course:  CSE 5693, Fall 2017
 * Project: HW4, Genetic Algorithms
 * 
 * A rule is a propositional rule represented by bit strings like:
 * 10 01 1 11 10 0
 * A rule consists of some attribute values followed by a target.
 * In tennis the target is only one bit.
 * In Iris the target represents with a 3-bit string at the end.
 * 
 */
public class Rule implements Comparable<Rule>{

	private int[] data; // one bit string of size (e.g. 11 (including the target))
	private int length; // length of a valid bit string
	private int[] target; // only one bit for tennis
	private int target_l;
	private double score; // rule's accuracy over train set
	private int[] boundaries; // helps how to interpret the bit string
	private int crossover_point_d1 = Integer.MAX_VALUE;
	private int crossover_point_d2 = Integer.MAX_VALUE;
	
	// overloaded constructor
	Rule(int rule_l, int target_l, int[] boundaries){
		this.setLength(rule_l);
		this.setTargetLength(target_l);
		this.setBoundaries(boundaries);
		this.data = new int[this.getLength()];
		this.target = new int[this.getTargetLength()];
	}
	
	protected void setData(int d_idx, int d_value) {
		this.getData()[d_idx] = d_value;
	}

	protected void setData(int[] data){
		this.data = data.clone();
	}
	protected int[] getData() {
		return this.data;
	}

	protected int getData(int idx) {
		return this.getData()[idx];
	}

	protected void setTarget(int t_idx, int t_value){
		this.getTarget()[t_idx] = t_value;
	}
	
	protected void setTarget (int[] target){
		this.target = target.clone();
	}
	
	protected int[] getTarget(){
		return this.target;
	}
	
	protected int getTarget(int idx){
		return this.getTarget()[idx];
	}
	
	protected void setBoundaries(int[] argin) {
		this.boundaries = new int[argin.length];
		for (int i = 0; i < boundaries.length; i++) {
			this.boundaries[i] = argin[i];
		}
	}

	protected int[] getBoundaries() {
		return this.boundaries;
	}

	protected int getBoundaries(int idx) {
		return this.getBoundaries()[idx];
	}

	protected void setLength(int argin) {
		this.length = argin;
	}
	
	protected int getLength (){
		return this.length;
	}
	
	protected int getTargetLength(){
		return this.target_l;
	}
	
	protected void setTargetLength(int argin){
		this.target_l = argin;
	}
	
	public void print (String name_dataset, String[] attrs, 
			HashMap<String, ArrayList<String>> attr_vals){
		if (name_dataset.equalsIgnoreCase("tennis")){
			int idx0 = -1, idx1 = -1, idx2 = -1, idx3 = -1, idx = 0;
			String triangular1 = "", triangular2 = "";
			// human-readable form
			for (int i = 0 ; i < this.length; i++){
				int attr_idx = 0, t = 0;
				if ((0 <= i) && (i <= 2)){

					attr_idx = 0; // outlook
					idx0++;
				}else if ((3 <= i) && (i <= 5)){
					attr_idx = 1; // temperature
					idx1++;
				}else if ((6 <= i) && (i <= 7)){
					attr_idx = 2; // humidity
					idx2++;
				}else if ((8 <= i) && (i <= 9)){
					attr_idx = 3; // wind
					idx3++;
				}else if (i == 10){
					t = this.getData(i); // target value (0 or 1)
				}
				
				ArrayList<String> tmp = attr_vals.get(attrs[attr_idx]); // [sunny overcast rain]
				switch (attr_idx){
				case 0: {idx = idx0; break;}
				case 1: {idx = idx1; break;}
				case 2: {idx = idx2; break;}
				case 3: {idx = idx3; break;}
				default: {System.out.println("no good idx in Rule print"); break;}
				}
				if((i == 0) || (i == 3) || (i == 6) || (i == 8)){
					triangular1 = "<";
					triangular2 = "";
				}
				if ((i == 2) || (i == 5) || (i == 7) || (i == 9)){
					triangular1 = "";
					triangular2 = ">";
				}

				System.out.printf("%s", triangular1);
				if (this.getData(i) == 1){
					System.out.printf("%s", tmp.get(idx));
				}
				System.out.printf("%s ", triangular2);
				if (i != this.length - 2)
				System.out.printf("%s", triangular2 == ">"?"^":triangular2);
				triangular1 = ""; triangular2 = "";
				
				// print the target value
				if (i == this.length - 1){
					if (this.getData(i) == 1){
						System.out.println("--> Yes");
					}else{
						System.out.println("--> No");
					}
				}
			}
			
		}else{// iris
			int counter = 0;
			for (int b : this.getData()){
				if (counter == this.length - this.target_l){
					System.out.print("-->");
				}
				if (counter != this.length - 1){
					System.out.printf("%s,", b);
				}else{
					System.out.printf("%s%n", b);
				}
				counter++;
			}
		}
		
	}
	
	public void setScore(double argin){
		this.score = argin;
	}
	
	public double getScore(){
		return this.score;
	}
	
	public void setCrossover_point_d1 (int idx){
		this.crossover_point_d1 = idx;
	}
	
	public int getCrossover_point_d1 (){
		return this.crossover_point_d1;
	}
	
	public void setCrossover_point_d2 (int idx){
		this.crossover_point_d2 = idx;
	}
	
	public int getCrossover_point_d2 (){
		return this.crossover_point_d2;
	}
	
	public Rule clone (){
		Rule new_rule = new Rule(this.getLength(), this.getTargetLength(), this.getBoundaries());
		
		new_rule.setBoundaries(this.getBoundaries().clone());
		
		for (int i = 0; i < this.getLength(); i++){
			new_rule.setData(i, this.getData(i));
		}
		
		new_rule.setLength(this.getLength());
		new_rule.setScore(this.getScore());
		
		for (int i = 0; i < this.getTargetLength(); i++){
			new_rule.setTarget(i, this.getTarget(i));
		}
		
		new_rule.setTargetLength(this.getTargetLength());
		new_rule.setCrossover_point_d1(this.getCrossover_point_d1());
		new_rule.setCrossover_point_d2(this.getCrossover_point_d2());
		return new_rule;
	}
	
	public boolean identical(Rule cand_rule) {

		boolean identical = true;

		// check the data
		for (int i = 0; i < this.length; i++) {
			identical = (this.getData(i)) != (cand_rule.getData(i)) ? false : true;
			if (!identical) return identical;
		}
		// check the targets
		for (int i = 0; i < this.target_l; i++){
			identical = (this.getTarget(i) != cand_rule.getTarget(i)) ? false: true;
			if (!identical) return identical;
		}

		return identical;
	}
	
	
	protected Rule generateRandomly(Random rnd, String name_dataset){

		int idx = 0; boolean valid;
		for (int i = 0; i < boundaries.length; i++){
			
			valid = false;
			int min = (i != 0)? (idx):0;
			int max = min + boundaries[i];
			
			if (name_dataset.equalsIgnoreCase("tennis")){
			while (valid == false){
				valid = setRandomData(min, max, valid, rnd);
			}// end of while
			}else{ // for iris we don't have to have at least one 1 (true) for each attr
				setRandomData(min, max, valid, rnd);
			}
			
			idx += boundaries[i];
		}// end of for
		
		// target
		valid = false;int j;
		while (valid == false){
			j = 0;
			for (int i = idx; i < this.length; i++){
				// give me 0 or 1 randomly
				double tmp = rnd.nextDouble();
				valid = (tmp >= .5)?true:valid;

				if ((valid) || (name_dataset.equalsIgnoreCase("tennis"))){
					this.setData(i, (tmp < .5)? 0:1); // target part of the data
					this.setTarget(j, (tmp < .5)? 0:1);
					break;
				}else{
					j++;
				} // end of if	
			} // end of for
			if (name_dataset.equalsIgnoreCase("tennis")){
				break;
			}
		} // end of while
		
		return this;
	}

	private boolean setRandomData(int min, int max, boolean valid, Random rnd) {
		for (int j = min; j < max; j++){
			// give me 0 or 1 randomly
			double tmp = rnd.nextDouble();
			valid = (tmp >= .5)?true:valid;
			this.setData(j, (tmp < .5)? 0:1);//(j, (tmp < .5)? 0:1);
		}// end of for
		return valid;
	}

	public void assign_ruleScore(List<Exp> examples, String[] attrs, HashMap<String, ArrayList<String>> attr_vals) {
		double num_matched_exp = .0; // preconditions match; not necessarily the targets
		boolean match = true;
		int counter = 1;
		double acc = .0;
		
		// for this rule compute its score	
		for (Exp exp: examples){
			
			// get example (bit string form)
			int[] exp_bitstring = exp.getBitstring();
			
			// see if the example matches the rule (exclude the target comparison here)
			match = checkMatch(exp_bitstring, this.getData(), this.getTargetLength());
			
			// count matched examples
			if (match){
				
				//get example's target bit string
				int[] target_exp = exp.getBitstring_target();
				
				// get rule's target bit string
				int[] target_rule = this.getTarget();
				
				if (checkMatch(target_exp, target_rule, 0)){
					acc++;
				}
				num_matched_exp++;
			}
			
		}
			
			assert (num_matched_exp >= 0);
			score = acc/(num_matched_exp);
			score = num_matched_exp == 0? 0 : score;
			score = num_matched_exp / examples.size();
			
			//score = (num_matched_exp == 0) ? 0 : num_matched_exp/examples.size(); 
			
			this.setScore(score);
			//System.out.println(score);

	}

	private boolean checkMatch(int[] argin1, int[] argin2, int l) {
		// verify those indices with value 1 are in both the example and rule
		// l is 0 when we compare the targets themselves
		// otherwise it's the target_l

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
	



	@Override
	public int compareTo(Rule r) {
		// compare scores
		if (this.getScore() > r.getScore()){
			return 1;
		}else if (this.getScore() == r.getScore()){
			return 0;
		}else{
			return -1;
		}
	}

}
