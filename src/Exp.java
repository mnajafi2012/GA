import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Mar 21, 2017
 * Course:  CSE 5693, Fall 2017
 * Project: HW4, Genetic Algorithms
 * 
 * Exp is a class containing only one example; a line from the input txt file.
 * Each line contains a row of a value for all possible attributes plus a target class at the end.

 */
public class Exp{
	
	private String[] data = new String[4];
	private int[] bitstring_data;
	private String target;
	private int[] bitstring_target;
	
	// argin could be like [Sunny Hot High Weak No]
	Exp(String argin){	 
		this.add(argin.split(" "));
	}
	
	Exp (){
		data = new String[4];
		target = "";
	}
	
	Exp (String argin, int datasize){
		this.data = new String[datasize];
		this.add(argin.split(" "));
	}
	
	// called functions	
	void add(String[] strings){
		
		for (int i = 0; i < strings.length - 1; i++){
			this.data[i] = strings[i];
		}
		
		this.settarget(strings[strings.length - 1]);
	}
	
	// getters and setters
	public String get(int idx){
		// takes the index of data and returns the particular element of the string array
		return this.getData()[idx];
	}
	public void set(){
		
	}
	public String[] getData(){
		return this.data;
	}
	public void setData(String[] d){
		for (int i = 0; i < d.length; i++){
			this.data[i] = d[i];
		}
	}
	public String getTarget(){
		return this.target;
	}
	public void settarget(String t){
		this.target = t;
	}
	
	
	public int compareTo(Exp exp, int a) {
		int res = Double.compare(Double.valueOf(this.getData()[a]), Double.valueOf(exp.getData()[a]));

		return res;
	}
	
	public Exp clone (){
		Exp e = new Exp();
		e.settarget(this.getTarget());
		e.setData(this.getData());
		
		return e;
	}

	protected void calBitstring(int rule_l, int target_l, int[] boundaries,
			String[] attrs, String[] attrs_orig, HashMap<String, ArrayList<String>> attr_vals, String name_dataset) {
		// return a bit string of the example (data + target)

		// 1. zero-pad a bit string and substitute 1 to appropriate indices
		int[] bitstring = new int[rule_l];
		//this.bitstring_data = new int[rule_l];
		//this.bitstring_target = new int[target_l];
		
		// 2. simply retrieve the bit string of this particular example
		// go over attributes
		for (int i = 0; i < attrs.length; i++){
			int idx = (name_dataset.equalsIgnoreCase("tennis"))? 
					this.getAttributeValue_idx(i, attrs[i], attr_vals): 0;
			int idx_tmp = 0;
			for (int j = 0; j < i; j++){ idx_tmp += boundaries[j];}
			idx_tmp = (i != 0)? idx_tmp + idx: idx; 
			
			int value = 1;
			// determine whether the bit should be 0 or 1 (for tennis it is always 1 since idx_tmp is changed)
			// for iris it depends on the precondition. The condition should met. sepal<=7.5 for 
			// this particular Exp should met.
			if (!name_dataset.equalsIgnoreCase("tennis")){ // for iris
				double thr = Double.parseDouble(attrs[i].split("<=")[1]); // 7.05
				String attr = attrs[i]; // sepal-length
				// find the index of attr in global Attributes.
				int attr_idx = Arrays.asList(attrs_orig).indexOf(attr.split("<=")[0]); // 0

				// find the value of attribute in example
				double attr_val_exp = Double.parseDouble(this.get(attr_idx)); // e.g. 4.3

				// check the condition is met (for sepal-length of this example it's value < thr?)
				value = (attr_val_exp <= thr)?1:0;
				
			} // end of if
			
			// for both datasets do assign value
			bitstring[idx_tmp] = value;
			
		}
		
		// 3. go over the target value
		if (target_l != 1){ // iris
			int idx = this.getClassValue_idx(target_l);
			
			int subtrahend = (name_dataset.equalsIgnoreCase("tennis"))? 1:0;
			bitstring[rule_l - target_l - subtrahend + idx] = 1;
		}else{ // tennis
			String target = this.getTarget();
			bitstring[rule_l - 1] = (target.equalsIgnoreCase("Yes"))?1:0;
		}
		
		// 4. set bit strings
		this.setBitstring(bitstring);
		this.setBitstringTarget(Arrays.copyOfRange(bitstring, rule_l - target_l, rule_l));
		
	}
	
	public void setBitstringTarget(int[] bits) {
		// this bit string contains only target (of size 1 for tennis)
		this.bitstring_target = new int[bits.length];
		this.bitstring_target = bits.clone();
		
	}

	public void setBitstring(int[] bits) {
		// this bit string contains data + target (size 11 for tennis)
		this.bitstring_data = new int[bits.length];
		this.bitstring_data = bits.clone();

	}
	
	public int[] getBitstring() {
		return this.bitstring_data;
	}
	
	public int[] getBitstring_target(){
		return this.bitstring_target;
	}

	private int getClassValue_idx(int target_l) {
		
		assert (target_l == 3): "no Iris! no Tennis! in Exp";
		
		String target = this.getTarget();
		
		//if (target_l == 3){ // iris
		switch (target.toLowerCase()){ // return index 0, 1, or 2
			case "iris-setosa": return 0;
			case "iris-versicolor": return 1;
			case "iris-virginica": return 2;
			default: System.out.println("no good target!"); break;
			}
		//}
		return 0;
	}

	private int getAttributeValue_idx(int attr_index, String candidate_attr, HashMap<String, ArrayList<String>> attr_vals) {
		// returns the index of the particular attribute-value in the example
		// according to the candidate attribute
		
		String attr_val = this.get(attr_index); // e.g. sunny
		
		ArrayList<String> dummy = attr_vals.get(candidate_attr); // [sunny, overcast, rain]
		String[] tmp = new String[dummy.size()];
		for (int i = 0; i < tmp.length; i++){
			tmp[i] = dummy.get(i);
		}
		int attr_val_idx = Arrays.asList(tmp).indexOf(attr_val); // 0
		
		return attr_val_idx;
	}
	
}
