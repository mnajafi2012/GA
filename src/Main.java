import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Mar 21, 2017
 * Course:  CSE 5693, Fall 2017
 * Project: HW4, Genetic Algorithms
 * 
 */
public class Main {

	static int[] classes;
	private static int attrs_size = 4;
	static HashMap<String, ArrayList<String>> attr_vals = new HashMap<String, ArrayList<String>>();
	static String[] attrs; // Tennis has 4 attrs while Iris needs to be preprocessed.
	static String[] attrs_orig = new String[4];
	static Random rnd = new Random(0);

	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		// variable definitions
		String name_dataset = args[0];
		String txt_attrs = "", txt_input_train = "", txt_input_test;
		List<Exp> examples_train = new ArrayList<Exp>();
		List<Exp> examples_val = new ArrayList<Exp>();
		List<Exp> examples_test = new ArrayList<Exp>();
		int rule_l = 0, target_l = 0; // it is different for various data sets.
        // a hypo contains at least one rule.
		int[] boundaries = null;
		// Compute the majority of class targets for the default target value
		String target_default = null;
		
		Random rnd = new Random(); //rnd.setSeed(0);
		
		// READ EXAMPLES
		if (name_dataset.equalsIgnoreCase("tennis")){
			System.out.printf("Tennis Data Set: %n-------------%n");
			txt_attrs = System.getProperty("user.dir").concat("/tennis-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/tennis-train.txt");
			txt_input_test = System.getProperty("user.dir").concat("/tennis-test.txt");
	
			// a) read from input text file for the tennis dataset
			setAttrs_size(4);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);
			examples_test = readExamples(txt_input_test);
			
			rule_l = getRuleSize(name_dataset);
			target_l = getTargetSize(name_dataset);
			boundaries = setBoundaries(name_dataset);
			
			// create bit strings of the example
			cal_bitstrings_examples(examples_train, rule_l, target_l, boundaries, attrs, attr_vals, name_dataset);
			cal_bitstrings_examples(examples_test, rule_l, target_l, boundaries, attrs, attr_vals, name_dataset);

			target_default = "Yes";
			
		}else if (name_dataset.equalsIgnoreCase("iris")){
			System.out.printf("Iris Data Set: %n-------------%n");
			txt_attrs = System.getProperty("user.dir").concat("/iris-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/iris-train.txt");
			txt_input_test = System.getProperty("user.dir").concat("/iris-test.txt");
			
			// b) read from input text file for the iris dataset
			setAttrs_size(4);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);
			
			examples_test = readExamples(txt_input_test);
			attrs_orig = attrs.clone();

			// pre-process iris dataset to discretize the attributes
		    preprocess_Iris(examples_train);
		    
		    target_default = getMajority(examples_train);
		    
		    rule_l = getRuleSize(name_dataset);
			target_l = getTargetSize(name_dataset);
			boundaries = setBoundaries(name_dataset);
			
			// create bit strings of the example
			cal_bitstrings_examples(examples_train, rule_l, target_l, boundaries, attrs, attr_vals, name_dataset);
			cal_bitstrings_examples(examples_test, rule_l, target_l, boundaries, attrs, attr_vals, name_dataset);
			
		}
		
		// 0. INITIALIZATION
		// population size (p)
		int p = Integer.valueOf(args[1]);
		// replacement rate (r)
		double r = Double.valueOf(args[2]);
		// mutation rate (m)
		double m = Double.valueOf(args[3]);
		// iterations
		double iter_max = Double.valueOf(args[4]);
		// fitness threshold
		double fitness_thr = .95;
		double max_fitness = Double.MIN_NORMAL;
		// selection methods
		String selectionMethod = "rank"; // possible values: proportionate, tournament and rank
		List<Hypothesis> Ps = new ArrayList<>();
		double acc;
			
			// 1. INITIALIZE POPULATION
			// the population is generated for each data set separately
			List<Hypothesis> population = generate_population(p, rule_l, target_l, boundaries, name_dataset, examples_train);

			// 2. EVALUATE FITNESS FOR EACH HYPOTHESIS
			population = eval(population, examples_train, name_dataset, target_default, boundaries);
			
			// compute max_fitness here
			//find_best_hypo(population, max_fitness);
			
			// 3. WHILE argMAX Fitness(h) for all hypo's < fitness-thr
			// stopping criterion (e.g. fitness threshold (used in GABIL), num of
			// generations)
			int iter = 0; Hypothesis best_hypo = null; 
			while ((max_fitness < fitness_thr) && (iter <= iter_max)){
				iter++;
				// 4. create a new generation, Ps
				// 4-1. SELECT
				// a) proportionate selection b) tournament selection c) rank
				// selection
				//Pair Ps_Pcrossover = select(population, selectionMethod, r, p);//best

				Ps = select (population, selectionMethod, r, p);
				//Ps = (List<Hypothesis>) Ps_Pcrossover.getfirst();
				//List<Hypothesis> population_crossover = (List<Hypothesis>) Ps_Pcrossover.getsecond();

				// TEMPORARILY ADD UP PS AND PS-PCROSSOVER AGAIN TO INCLUDE ALL POPULATION!!!!!!!!!
				//List<Hypothesis> all_population = addUp(Ps_Pcrossover, r, p);
				
				
				List<Hypothesis> population_crossover = new ArrayList<>();
				for (int i = 0; i < r * p; i++ ){
					population_crossover.add(population.get(i));
				}

				// 4-2. CROSSOVER
				Ps = crossover(population_crossover, r, p, Ps);
				
				/*for (Hypothesis h: population){
					for (Rule r2: h.getHypo()){
						r2.print();
					}
					System.out.println();
				}*/

				// 4-3. MUTATE & UPDATE P <-- Ps
				Ps = mutate(Ps, m, examples_train, name_dataset, boundaries, target_default);

				// 4-4. UPDATE
				//Ps = sort(Ps, "ascending");
				for (int i= 0; i < p; i++){
					population.set(i, Ps.get(i).clone());
				}
				
				// 4-5. EVALUATE
				population = eval(population, examples_train, name_dataset, target_default, boundaries);

				// compute max_fitness here
				best_hypo = find_best_hypo(population, max_fitness);
				
				max_fitness = best_hypo.getFitness();
				
				//System.out.println(max_fitness);
				
				// 5. TEST
				acc = test(best_hypo, examples_test, attrs, attr_vals, target_default);
				
				//acc = test(best_hypo, examples_test, attrs, attr_vals, target_default);
				//System.out.printf("accuracy on test data: %%%.1f%n",acc * 100 );
				
			}

			// print the rules attributes
			//for (int i = 0; i < attrs.length; i++){
				//System.out.printf("%s, ", attrs[i]);
			//}
			for (Rule rule : best_hypo.getHypo()){
				rule.print(name_dataset, attrs, attr_vals);
			}
			System.out.println();
			acc = test(best_hypo, examples_train, attrs, attr_vals, target_default);

			//System.out.printf("best fitness: %.2f%n",best_hypo.getFitness() );
			System.out.printf("accuracy on train data: %%%.1f%n",acc *100 );
			
			acc = test(best_hypo, examples_test, attrs, attr_vals, target_default);
			System.out.printf("accuracy on test data: %%%.1f%n",acc * 100 );

	}
/**
 * 
 * @param best_hypo best trained hypothesis after iteration # iter
 * @param examples_test all training examples
 * @param target_default 
 * @param attr_vals2 
 * @param attrs2 
 * @return the accuracy of the trained model
 */
	private static double test(Hypothesis best_hypo, List<Exp> examples_test, String[] attrs2, HashMap<String, ArrayList<String>> attr_vals2, String target_default) {
		
		// go over examples
		//best_hypo.remove(-1);
		double acc = best_hypo.calFitness(examples_test, attrs, attr_vals, target_default);
		
		return acc;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private static List<Hypothesis> addUp(Pair Ps_Pcrossover, double r, double p) {
		
		// r* p/2 pairs e.g. for r = .6 and p = 100; 30 pairs should return to crossover
		
		int crossover_size = (int)( r * p);
		
		List<Hypothesis> f = (List<Hypothesis>) Ps_Pcrossover.getfirst();
		List<Hypothesis> s = (List<Hypothesis>) Ps_Pcrossover.getsecond();

		for (int i = 0; i < crossover_size; i++){
			f.add(s.get(i));
		}
		
		return f;
	}

	private static List<Hypothesis> eval(List<Hypothesis> Ps, List<Exp> examples_train, String name_dataset, String target_default, int[] boundaries) {
		// compute the fitness of each hypothesis
		// The classification accuracy of the hypothesis could represent the
		// Fitness function
		for (Hypothesis h : Ps) {
			
			h.computeFitness(examples_train, name_dataset, attrs, attrs_orig, attr_vals, classes, boundaries,
					target_default);

			//System.out.println(h.getFitness());

		}
		return Ps;
	}

	private static Hypothesis find_best_hypo (List<Hypothesis> Ps, double max_fitness) {
		
		Hypothesis best_hypo = new Hypothesis();
		for (Hypothesis h: Ps){
			//System.out.printf("fitness: %.2f%n", h.getFitness());
			if (h.getFitness() >= max_fitness){
				max_fitness = h.getFitness();
				
				// save
				best_hypo = h.clone();
			}
		}
		
		return best_hypo;
	}
	
	private static List<Hypothesis> mutate(List<Hypothesis> Ps, double m, 
			List<Exp> examples_train, String name_dataset, int[] boundaries,
			String target_default) {
		// m is a very small value representing the percentage of population Ps to mutate
	
		int num_of_mutation = (int) (m * Ps.size() ); // e.g. for m = .002 and p = 100; num is 2 hypos to mutate
				
		for (int i = 0; i < num_of_mutation; i++){
			// randomly pick a hypo
			int hypo_idx = (int) (rnd.nextDouble() * Ps.size());

			// randomly pick one of "hypo's" rules 
			int rule_idx = (int) (rnd.nextDouble() * Ps.get(hypo_idx).getSize());
			
			one_complement(Ps, hypo_idx, rule_idx);
			
		}
		
		return Ps;
		
	}
	
	private static void one_complement(List<Hypothesis> Ps, int hypo_idx, int rule_idx) {
		// whatever happens here to picked_group directly affects the Ps (shallow copy)

		boolean valid = false;
		List<Hypothesis> Ps_copy;

		// deep copy
		Ps_copy = new ArrayList<>();
		for (Hypothesis h : Ps){
			Ps_copy.add(h.clone());
		}
		
		int one_or_0 = 0; int div_point; int rule_length = Ps.get(0).getRule(0).getLength();
		
		while (!valid){
			
			div_point = (int) (rnd.nextDouble() * rule_length);
			
			one_or_0 = Ps_copy.get(hypo_idx).getRule(rule_idx).getData(div_point);
			
			if (one_or_0 == 1){
				Ps_copy.get(hypo_idx).getRule(rule_idx).setData(div_point, 0);
			}else{
				Ps_copy.get(hypo_idx).getRule(rule_idx).setData(div_point, 1);
			}
			
			valid = Ps_copy.get(hypo_idx).checkvalidity();
			

			// now apply the effect on the original Ps
			if (valid){			
				if (one_or_0 == 1){
					Ps.get(hypo_idx).getRule(rule_idx).setData(div_point, 0);
				}else{
					Ps.get(hypo_idx).getRule(rule_idx).setData(div_point, 1);
				}
			}
		} // end of while
		
	}
/*
	private static void ones_complement(List<Hypothesis> Ps, List<Hypothesis> picked_group, int picked_hypo_idx, int rule_length, int div_point) {
		// whatever happens here to picked_group directly affects the Ps (shallow copy)
		
		boolean valid = false;
		List<Hypothesis> picked_group_copy = new ArrayList<>();
		// deep copy
		for (Hypothesis h: picked_group){
			picked_group_copy.add(h.clone());
		}
		
		
		// mutate over all rules of the picked hypo in the picked group
		for (int i = 0; i < picked_group_copy.get(picked_hypo_idx).getSize(); i++){
			
			div_point = (int) rnd.nextDouble() * picked_group.get(picked_hypo_idx).getSize();
			
			int rule_idx = 0, one_or_0 = 0;
			
			while (!valid){

				one_or_0 = picked_group_copy.get(picked_hypo_idx).getRule(rule_idx).getData(div_point);
				
				if (one_or_0 == 1){
					picked_group_copy.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 0);
				}else{
					picked_group_copy.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 1);
				}
				
				valid = picked_group_copy.get(picked_hypo_idx).checkvalidity();
			} // end of while
			
			if (valid){			
				if (one_or_0 == 1){
					picked_group.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 0);
				}else{
					picked_group.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 1);
				}
			}
		}		
	}
*/

	/*
	private static List<Hypothesis> mutate(List<Hypothesis> Ps, double m, 
			List<Exp> examples_train, String name_dataset, int[] boundaries,
			String target_default) {
		// m is a very small value representing the percentage of population Ps to mutate

		// Evaluate hypotheses
		// find individuals with uniform likelihood
		for (Hypothesis h : Ps) {
			// compute the fitness of each hypothesis
			// The classification accuracy of the hypothesis could represent the
			// Fitness function
			h.computeFitness(examples_train, name_dataset, attrs, attrs_orig, attr_vals, classes, boundaries,
					target_default);

		}
		for (Hypothesis h_i : Ps) {
			h_i.cal_Pr(Ps);
		}
		
		Ps = sort(Ps, "ascending"); // sort w.r.t. Pr(hi)
		
		int num_of_mutation = (int) (m * Ps.size() ); // e.g. for m = .002 and p = 100; num is 2 hypos to mutate

		List<List<Hypothesis>> unified_hypos = partition_unified_Pr(Ps, num_of_mutation);
		
		// pick num_of_mutation unique (and unified) hypotheses from partitioned groups of Ps
		// first choose a group of unified hypotheses
		int picked_group_idx = (int) (rnd.nextDouble() * unified_hypos.size());
		
		List<Hypothesis> picked_group = null;
		try{
		picked_group = unified_hypos.get(picked_group_idx);}
		catch (IndexOutOfBoundsException e){
			System.out.println();
		}
		
		
		// then probabilistically select m*p/100 random bit in its representation to mutate
		ArrayList<Integer> picked_indices = new ArrayList<>(); int picked_hypo_idx;
		for (int i = 0; i < m * Ps.size(); i++){
			picked_hypo_idx = pick_a_member(picked_indices, picked_group);
			
			// now a unique member of this unified group is chosen
			// it's time to mutate
			// pick a division point randomly
			int num_of_rules = picked_group.get(picked_hypo_idx).getSize(); // number of rules in a hypothesis
			int rule_length = picked_group.get(picked_hypo_idx).getHypo(0).getLength(); // length of rule
			int max_limit_for_div_point = num_of_rules * rule_length;
			
			int div_point = (int) (rnd.nextDouble() * max_limit_for_div_point);
			
			ones_complement(Ps, picked_group, picked_hypo_idx, rule_length, div_point); // note picked group is a shallow copy of Ps
			
		}

		return Ps;
	}*/

	private static void ones_complement(List<Hypothesis> Ps, List<Hypothesis> picked_group, int picked_hypo_idx, int rule_length, int div_point) {
		// whatever happens here to picked_group directly affects the Ps (shallow copy)
		
		boolean valid = false;
		List<Hypothesis> picked_group_copy = new ArrayList<>();
		// deep copy
		for (Hypothesis h: picked_group){
			picked_group_copy.add(h.clone());
		}
		
		
		// mutate over all rules of the picked hypo in the picked group
		for (int i = 0; i < picked_group_copy.get(picked_hypo_idx).getSize(); i++){
			
			div_point = (int) rnd.nextDouble() * picked_group.get(picked_hypo_idx).getSize();
			
			int rule_idx = 0, one_or_0 = 0;
			
			while (!valid){

				one_or_0 = picked_group_copy.get(picked_hypo_idx).getRule(rule_idx).getData(div_point);
				
				if (one_or_0 == 1){
					picked_group_copy.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 0);
				}else{
					picked_group_copy.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 1);
				}
				
				valid = picked_group_copy.get(picked_hypo_idx).checkvalidity();
			} // end of while
			
			if (valid){			
				if (one_or_0 == 1){
					picked_group.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 0);
				}else{
					picked_group.get(picked_hypo_idx).getRule(rule_idx).setData(div_point, 1);
				}
			}
		}		
	}

	private static List<List<Hypothesis>> partition_unified_Pr(List<Hypothesis> Ps, int num_of_mutation) {
		// returns a bunch of sets of hypotheses in which the likelihood is unified.
		
		// num of mutation lets you ignore those unified groups whose number of members are less than
		// desired percentage of mutation
		
		
		List<Hypothesis> tmp = new ArrayList<>();
		List<List<Hypothesis>> unified_Hypos = new ArrayList<>();
		
		/*for (Hypothesis h: Ps){
			System.out.println(h.getPr());
		}*/
		
		
		boolean signal = false;
		for (int i = 0; i < Ps.size() - 1; i++){
			if (Ps.get(i).compareTo(Ps.get(i + 1)) == 0){
				tmp.add(Ps.get(i + 1));
				signal = true;
			}else{
				tmp.add(Ps.get(i));
				if (tmp.size() > num_of_mutation){
					unified_Hypos.add(tmp);
				}
				tmp = new ArrayList<>();
				signal = false;
			}
		} // end of for
		
		if (signal) {
			tmp.add(Ps.get(Ps.size() - 1));
			if (tmp.size() > num_of_mutation){
				unified_Hypos.add(tmp);
			}
		}
		
		if (unified_Hypos.isEmpty()){
			System.out.println();
		}
		return unified_Hypos;
	}

	private static List<Hypothesis> crossover(List<Hypothesis> population_crossover, double r,
			double p, List<Hypothesis> Ps) {
		// Given the selection method the crossover differs

		List<Hypothesis> offsprings;
		// single-point crossover (for both tennis and iris)
		
		/*
		int masklength = population_crossover.get(0).getHypo(0).getLength(); // length 11 for tennis
		int[] mask = new int[masklength];
		
		// generate a random number
		int crossover_point = (int) Math.round((rnd.nextDouble() * masklength));
		
		// modify mask with respect to the point
		for (int i = 0 ; i < crossover_point; i++){
			mask[i] = 1;
		}
		
		// reproduce offsprings from a pair of parent <h1, h2> and add all offsprings to Ps
		offsprings = reproduceOffsprings(population_crossover, mask, r, p);
		*/
		
		// reproduve offsprings using two-point crossover over all pairs of parents
		offsprings = reproduceOffsprings(population_crossover);
		
		// add to Ps
		Ps.addAll(offsprings);

		return Ps;
	}

	private static List<Hypothesis> reproduceOffsprings(List<Hypothesis> population_crossover) {
		// reproduce offsprings from the rest of members in the population (r . p / 2)
		// e.g. for r = .6 and p = 100, 30 pairs should be selected here.  
		//TODO
		int size = population_crossover.size();
		int rule_length = population_crossover.get(0).getHypo(0).getLength();
		int[] d;
		ArrayList<Integer> picked_indices = new ArrayList<>();
		List<Hypothesis> offsprings = new ArrayList<>();
		
		for (int i = 0; i < (int) (size/2); i++){
			// select a pair randomly
			int h1_idx = pick_a_member(picked_indices, population_crossover);
			int h2_idx = pick_a_member(picked_indices, population_crossover);
			Hypothesis h1_parent = population_crossover.get(h1_idx);
			Hypothesis h2_parent = population_crossover.get(h2_idx);
			
			Hypothesis h1_offspringtmp;
			Hypothesis h2_offspringtmp; 
			
			// operate TWO-POINT CROSSOVER (some parents have multiple rules)
			int h1_numOfRules = h1_parent.getSize();
			int h2_numOfRules = h2_parent.getSize();
			
			// pick two crossover points randomly for parent h1
			d = pick_2_random_points(population_crossover);
			
			// pick a rule from parent h1
			h1_parent = set_crossover_points(h1_parent, d, h1_numOfRules).clone();
			
			// pick a rule from parent h2
			h2_parent = set_crossover_points(h2_parent, d, h2_numOfRules).clone();
			
			// reproduce offspring 1
			h1_offspringtmp = reproduce(h1_parent, h2_parent, rule_length);
			
			
			// check offsprings validity
			// if not valid re-do the reproducing
			boolean valid = h1_offspringtmp.checkvalidity();
			
			if (!valid){
				i--;
				continue;
			}else{
				// reproduce offspring 2
				h2_offspringtmp = reproduce(h2_parent, h1_parent, rule_length);
				// check offsprings validity
				// if not valid re-do the reproducing
				valid = h2_offspringtmp.checkvalidity();
			}
			if (h2_offspringtmp.getSize() > 2){
				//System.out.println();
			}
			
			if (!valid){
				i--; continue;
			}

			//System.out.printf("offspring1 size: %d %noffspring2 size: %d %n%n",
					//h1_offspringtmp.getSize(), h2_offspringtmp.getSize());
			
			
			
			offsprings.add(h1_offspringtmp);
			offsprings.add(h2_offspringtmp);
						
		}
		
		return offsprings;
	}

/*
	private static boolean checkvalidity(Hypothesis h1_offspringtmp) {
		// 1) we should have only one bit of value 1 in the target, at most.
		// 2) we should not have all values 0 for iris
		
		// check 1 and 2
		boolean valid = false;
		for (int r = 0; r < h1_offspringtmp.getSize(); r++){
			for (int b: h1_offspringtmp.getHypo(r).getTarget()){
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
		if ((!valid) && (h1_offspringtmp.getHypo(0).getTargetLength()!=1)){
			return false;
		}
		// otherwise it's ok to have only 0 (tennis dataset)
		return true;
	}*/

	private static Hypothesis reproduce(Hypothesis h1_parent, Hypothesis h2_parent, int rule_length) {
		
		/*
		ArrayList<Integer> h1_offspringtmp = new ArrayList<>();
		int d1, d2; // crossover points: d1 and d2 according to the book
		int lowerbound = 0, upperbound;
		
		for (Rule rule_h1 : h1_parent.getHypo()) {
			d1 = rule_h1.getCrossover_point_d1();

			upperbound = (d1 != Integer.MAX_VALUE) ? d1 : rule_length;
			
			if (d1 == Integer.MAX_VALUE){
				d2 = rule_h1.getCrossover_point_d2();
				
				lowerbound = (d2 != Integer.MAX_VALUE) ? d2 : rule_length;
				
				//lowerbound = (d2 == Integer.MAX_VALUE)? 0 : lowerbound;
			}
			
			for (int j = lowerbound; j < upperbound; j++) {
				h1_offspringtmp.add(rule_h1.getData(j));
			}

			if (d1 != Integer.MAX_VALUE) {
				// find the first crossover point from the second parent
				lowerbound = 0; int counter = 0;
				for (Rule rule_h2 : h2_parent.getHypo()) {
					
					d1 = rule_h2.getCrossover_point_d1();
					d2 = rule_h2.getCrossover_point_d2();
					
					// CHECK THIS COMMAND LINE!!!
					lowerbound = (d1 != Integer.MAX_VALUE) ? d1 : rule_length;
					upperbound = (d2 != Integer.MAX_VALUE) ? d2 : rule_length;
					
					if (d1 == Integer.MAX_VALUE){
						
						//if (counter >= 1){
							//if ((h2_parent.getHypo(counter -1 ).getCrossover_point_d2()) == Integer.MAX_VALUE){
								lowerbound = 0;
							//}
						//}
						
						lowerbound = (d2 == Integer.MAX_VALUE)? rule_length: 0;

					}
					counter++;
					
					
					for (int j = lowerbound; j < upperbound; j++) {
						h1_offspringtmp.add(rule_h2.getData(j));
					}
				} // end of for h2
				
				// continue adding bits back from the first rule
				d2 = rule_h1.getCrossover_point_d2();
				lowerbound = (d2 != Integer.MAX_VALUE) ? d2 : rule_length;
				
				for (int j = lowerbound; j < rule_length; j++){
					h1_offspringtmp.add(rule_h1.getData(j));
				}
			} // end of if
			
		} // end of for h1
		*/
		
		// parent 1 and parent 2 bit strings
		int[] parent1 = new int[h1_parent.getSize()* rule_length];
		int[] parent2 = new int[h2_parent.getSize()* rule_length];
		
		int idx = 0;
		for (Rule r: h1_parent.getHypo()){
			for (int i = 0; i < rule_length; i++){
				parent1[idx] = r.getData(i);
				idx++;
			}	
		}
		
		idx = 0;
		for (Rule r: h2_parent.getHypo()){
			for (int i = 0; i < rule_length; i++){
				parent2[idx] = r.getData(i);
				idx++;
			}	
		}
		
		// generate 2 unique random numbers
		int[] d = new int[2];
		do {
			d[0] = (int) (rnd.nextDouble() * rule_length); // e.g. 1
			d[1] = (int) (rnd.nextDouble() * rule_length); // e.g. 3
		} while ((d[0] == d[1]) || (d[0]==0) || (d[1]==0));
		int tmp = d[1]; d[1] = d[0]>d[1]?d[0]:d[1]; d[0] = d[0]==d[1]?tmp:d[0]; // sort d

		// create set of crossover points randomly
		ArrayList<Integer[]> tuples1 = new ArrayList<>();
		for (int r = 0; r < h1_parent.getSize(); r++){
			for (int w = 0; w < h1_parent.getSize(); w++){
				
				Integer[] tpl = new Integer[2];

				tpl[0] = d[0] + (r * rule_length);
				tpl[1] = d[1] + (w * rule_length);
				
				if (tpl[0] < tpl[1]){
					tuples1.add(tpl);
				}
			}
		}
		
		ArrayList<Integer[]> tuples2 = new ArrayList<>();
		for (int r = 0; r < h2_parent.getSize(); r++){
			for (int w = 0; w < h2_parent.getSize(); w++){
				
				Integer[] tpl = new Integer[2];

				tpl[0] = d[0] + (r * rule_length);
				tpl[1] = d[1] + (w * rule_length);
				
				if (tpl[0] < tpl[1]){
					tuples2.add(tpl);
				}
			}
		}
		
		// crossover
		
		int choice1 = (int) (rnd.nextDouble() * tuples1.size()) ;
		int choice2 = (int) (rnd.nextDouble() * tuples2.size()) ;
		
		// offspring 1
		ArrayList<Integer> h1_offspringtmp = new ArrayList<>();

		if (tuples1.isEmpty()){
			System.out.println();
		}
		
		for (int i = 0; i <tuples1.get(choice1)[0]; i++){
			h1_offspringtmp.add(parent1[i]);
		}
		for (int i = tuples2.get(choice2)[0]; i <tuples2.get(choice2)[1]; i++){
			h1_offspringtmp.add(parent2[i]);
		}
		for (int i = tuples1.get(choice1)[1]; i < parent1.length; i++){
			h1_offspringtmp.add(parent1[i]);
		}
		
		// offspring 2
		ArrayList<Integer> h2_offspringtmp = new ArrayList<>();

		for (int i = 0; i <tuples2.get(choice2)[0]; i++){
			h2_offspringtmp.add(parent2[i]);
		}
		for (int i = tuples1.get(choice1)[0]; i <tuples1.get(choice1)[1]; i++){
			h2_offspringtmp.add(parent1[i]);
		}
		for (int i = tuples2.get(choice2)[1]; i < parent2.length; i++){
			h2_offspringtmp.add(parent2[i]);
		}
		

		// create the actual offspring
		Hypothesis offspring = new Hypothesis();
		int target_l = h1_parent.getHypo(0).getTargetLength();
		int num_of_rules = h1_offspringtmp.size()/rule_length;
		idx = 0; // num of rules in the offspring
		
		if (num_of_rules == 0){
			System.out.println();
		}
		
		int[] boundaries = h1_parent.getHypo(0).getBoundaries().clone();
		for (int i = 0; i < num_of_rules; i++){
			Rule rule_tmp = new Rule(rule_length, target_l, boundaries);
			// set data
			idx = 0;
			for (int j = i*rule_length; j < (i + 1) * rule_length; j++){
				rule_tmp.setData(idx, h1_offspringtmp.get(j));
				idx++;
			}
			
			idx = 0;
			for (int j = (i+1)*(rule_length) - target_l; j < (i + 1) * rule_length; j++){
				rule_tmp.setTarget(idx, h1_offspringtmp.get(j));
				
				idx++;
			} // end of for
			offspring.add2hypo(rule_tmp);
		}
		
		return offspring;
	}

	private static int[] pick_2_random_points(List<Hypothesis> population_crossover) {
		
		int rule_length = population_crossover.get(0).getHypo(0).getLength(); // length 11 for tennis
		
		// generate 2 random numbers
		int[] d = new int[2];
		do {
			d[0] = (int) (rnd.nextDouble() * rule_length); // e.g. 1
			d[1] = (int) (rnd.nextDouble() * rule_length); // e.g. 3
		} while ((d[0] == d[1]) || (d[0]==0) || (d[1]==0));
		int tmp = d[1]; d[1] = d[0]>d[1]?d[0]:d[1]; d[0] = d[0]==d[1]?tmp:d[0]; // sort d
		
		return d;
	}

	private static Hypothesis set_crossover_points(Hypothesis parent, int[] d, int numOfRules) {
		
		parent.resetCrossover_points();
		
		int r1_idx = (int) (rnd.nextDouble() * numOfRules), r2_idx;
		
		//do {
			r2_idx = (int) (rnd.nextDouble() * numOfRules);
		//}while (r2_idx == r1_idx);
		// set crossover points to rules
		if (r1_idx < r2_idx) {
			parent.getHypo(r1_idx).setCrossover_point_d1(d[0]);
			parent.getHypo(r1_idx).setCrossover_point_d2(Integer.MAX_VALUE);
			parent.getHypo(r2_idx).setCrossover_point_d2(d[1]);
			parent.getHypo(r2_idx).setCrossover_point_d1(Integer.MAX_VALUE);
		} else if (r1_idx > r2_idx){
			parent.getHypo(r2_idx).setCrossover_point_d1(d[0]);
			parent.getHypo(r2_idx).setCrossover_point_d2(Integer.MAX_VALUE);
			parent.getHypo(r1_idx).setCrossover_point_d2(d[1]);
			parent.getHypo(r1_idx).setCrossover_point_d1(Integer.MAX_VALUE);

		}else{
			parent.getHypo(r1_idx).setCrossover_point_d1(d[0]);
			parent.getHypo(r2_idx).setCrossover_point_d2(d[1]);
			
		}
		
		
		return parent;
		
	}

	private static int pick_a_member(ArrayList<Integer> picked_indices, List<Hypothesis> population_crossover) {

		int idx;
		do {
			idx = (int) (rnd.nextDouble() * population_crossover.size());
		} while (Arrays.asList(picked_indices).indexOf(idx) == 1);

		picked_indices.add(idx);
		return idx;
	}

	private static List<Hypothesis> select(List<Hypothesis> population, String selectionMethod, double r, int p) {
		// Three selection approaches are used:

		List<Hypothesis> Ps = null;

		switch (selectionMethod.toLowerCase()) {
		// 1. proportionate selection
		case "proportionate": {
			Ps = rouletteWheel(population, r, p);
			//System.out.println("proportionate");

			break;
		}

		// 2. tournament selection
		case "tournament":
			Ps = tournament(population, r, p);
			//System.out.println("tournament");
			break;

		// 3. rank selection
		case "rank":
			Ps = rank(population, r, rnd);
			//System.out.println("rank");
			break;

		default:{
			Ps = rouletteWheel(population, r, p);
			//System.out.println("proportionate");
			break;
		}

		}
		
		// collect the rest of members in a new array to perform the crossover on
		List<Hypothesis> population_crossover = new ArrayList<>();
		for (int i = (int)((1 - r)*p); i < population.size(); i++ ){
			population_crossover.add(population.get(i));
		}
		
		
		return Ps;
		//return new Pair(Ps, population_crossover);
	}

	private static List<Hypothesis> rank (List<Hypothesis> population, double r, Random rnd){
		// rank selection
		
		int p = population.size();
		List<Hypothesis> Ps = new ArrayList<>();
		
		for (Hypothesis h_i : population) {
			h_i.cal_Pr(population);
		}

		// sort according to Pr(h_i)
		// let's sort the population by their Pr(h_i)
		// ;where Pr(h_i) = Fitness(h_i) / (Sum over all population
		// Fitness(h_j))
		
		population = sort(population, "ascending"); // 1 and -1 mean ascending and descending
									// order
		
		// rank hypotheses
		double[] ranks = new double[p];
		for (int i = 0 ; i < p; i++){			
			ranks[i] = rnd.nextDouble() * (p - i);
			
		}
		
		for (int j = 0; j < (1 - r) * p; j++){
			double max = Double.MIN_NORMAL;
			int best = 0;
			for (int i = 0 ; i < p; i++){
				if (ranks[i] > max){
					best = i;
					max = ranks[i];
				}
			}
			Ps.add(population.get(best));
			
		}

		return Ps;
	}

	private static List<Hypothesis> tournament(List<Hypothesis> population, double r, int p) {
		// tournament selection
		
		double pr = .95; // chance for best hypos
		List<Hypothesis> Ps = new ArrayList<>();
		
		int iter = (int) ((1- r) * p);
		
		for (int i = 0 ; i < iter; i++){
			int d1 = 0, d2 = 0;
			while (d1 == d2){
				d1 = (int) (rnd.nextDouble() * p);
				d2 = (int) (rnd.nextDouble() * p);
			}
			
			
			double d_pr = rnd.nextDouble() * pr;
			double d_pr2 = rnd.nextDouble() * (1 - pr);
			
			if (d_pr > d_pr2){
				if (population.get(d1).getFitness() > population.get(d2).getFitness()){
					Ps.add(population.get(d1).clone());
				}else{
					Ps.add(population.get(d2).clone());
				}
			}else{
				if (population.get(d1).getFitness() > population.get(d2).getFitness()){
					Ps.add(population.get(d2).clone());
				}else{
					Ps.add(population.get(d1).clone());
				}
			}
		}

		return Ps;
	}

	private static List<Hypothesis> rouletteWheel(List<Hypothesis> P, double r, int p) {
		// proportionate selection

		for (Hypothesis h_i : P) {
			h_i.cal_Pr(P);
		}

		// sort according to Pr(h_i)
		// let's sort the population by their Pr(h_i)
		// ;where Pr(h_i) = Fitness(h_i) / (Sum over all population
		// Fitness(h_j))
		
		P = sort(P, "ascending"); // 1 and -1 mean ascending and descending
									// order
		

		// select (1-r)p members of P to add to Ps
		List<Hypothesis> Ps = segment(P, r, p);

		return Ps;

	}
	
	private static List<Hypothesis> sort(List<Hypothesis> population, String s) {
		// Sort hypotheses based on their fitness values
		//s represents the order of sorting
		int order = s.equalsIgnoreCase("descending")? 1:-1;
		//assert population.size() == 100;

		for (int i = 0; i < population.size(); i++){
			if (i < population.size() - 1){
				for (int j = i + 1; j < population.size(); j++){
					// compare two hypotheses(-1/1 for less/greater than, and
					// 0 for equals to)
					
					if (population.get(i).compareTo(population.get(j)) == order){
						population = swap_population(population, i, j);
					}
				}
			}
		}
		
		return population;
	}

	private static List<Hypothesis> swap_population(List<Hypothesis> population, int i, int j) {
		
		Hypothesis tmp = population.get(i).clone();
		
		population.set(i, population.get(j).clone());
		population.set(j, tmp);

		return population;
		
	}

	private static List<Hypothesis> segment(List<Hypothesis> P, double r, double p) {
		List<Hypothesis> Ps = new ArrayList<>();
		
		for (int i = 0; i < (1-r)*p ; i++){
			Ps.add(P.get(i));
			
		}
		
		return Ps;
	}

	private static void cal_bitstrings_examples(List<Exp> examples, int rule_l, int target_l, int[] boundaries, String[] attrs,
			HashMap<String, ArrayList<String>> attr_vals, String name_dataset) {
		
		for (Exp e: examples){
			e.calBitstring(rule_l, target_l, boundaries, attrs, attrs_orig, attr_vals, name_dataset);
		}
	}

	private static String getMajority(List<Exp> examples) {
		
		int[] counter = new int[classes.length];
		
		for (Exp exp: examples){
			switch (exp.getTarget()) {
			case "Iris-setosa":
				counter[0]++;
				break;
			case "Iris-versicolor":
				counter[1]++;
				break;
			case "Iris-virginica":
				counter[2]++;
				break;
			default:
				break;
			}
		}
		
		// find the max
		int max = Integer.MIN_VALUE, idx = 0;
		for (int i = 0; i < counter.length - 1; i++){
			if ( counter[i] >= max){
				max = counter[i];
				idx = i;
			}
		}
		
		String t = replace(idx);
		
		return t;
	}
	
	private static String replace(int i) {
		// 0 is replaced with Yes or Iris-setosa
		// 1 is replaced with No or Iris-versicolor
		// 2 is replaced with Iris-virginica
		
		String output = "noun";

			switch (i) {
			case 0:
				output = "Iris-setosa";
				break;
			case 1:
				output = "Iris-versicolor";
				break;
			case 2:
				output = "Iris-virginica";
				break;
			default:
				output = "noun";
				break;
			}
		
		return output;
	}
	
	private static int[] setBoundaries(String name_dataset) {
		// for tennis data set boundaries is [3, 3, 2, 2]
		// meaning that the first 3 elements in bit string belong to
		// the first attr. the second 3 belong to second attr.
		// the next 2 elements are for 3rd attribute.
		// and next 2 elements are for 4th attribute.
		// the remaining belongs to the class target. (1 bit here)
		
		int[] boundaries = new int[attrs.length];
		if (name_dataset.equalsIgnoreCase("tennis")){
			boundaries = new int[attrs.length];
			for (int i = 0; i < attrs.length; i++){
				boundaries[i] = attr_vals.get(attrs[i]).size();
			}
		}else{ // continuous data sets (e.g. iris)
			boundaries = new int[attrs.length];
			for (int i = 0; i < attrs.length; i++){ // each attr has t/f
				boundaries[i] = attr_vals.get(attrs[i]).size() - 1;
			}
		}
		return boundaries;
	}

	private static int getRuleSize(String name_dataset) {
		// the number of bits in the bit strings (each rule)
		int counter = 0, rulesize;
		
		Iterator<String> itr = attr_vals.keySet().iterator();
		while (itr.hasNext()) {
			counter += attr_vals.get(itr.next()).size();
		}

		counter = (name_dataset.equalsIgnoreCase("iris"))? (counter / 2) : counter;
		
		rulesize = counter + classes.length;

		// -1 for assigning only 1 bit instead of 2 as for the target
		rulesize = (name_dataset.equalsIgnoreCase("tennis")) ? (--rulesize) : rulesize;
		return rulesize;
	}
	
	private static int getTargetSize(String name_dataset) {
		return (name_dataset.equalsIgnoreCase("tennis")) ? (classes.length - 1) : classes.length;
		
	}

	/**
	 * 
	 * @param p the number of individuals in our population. It's a user-defined argument.
	 * @param boundaries 
	 * @param name_dataset 
	 * @param examples_train 
	 */
	private static List<Hypothesis> generate_population(int p, int rule_l, int target_l,
			int[] boundaries, String name_dataset, List<Exp> examples_train) {
		// given the structure of the data set (number of attributes, values,
		// target classes)
		// forms a set of hypotheses where each hypo is a bit string.
		// A bit string is allocated to represent a hypothesis.
		// e.g. outlook (overcast or rain) and Wind (strong) ==> playtennis
		// (yes)
		// will be transformed to:
		// 011 10 10

		List<Hypothesis> population = new ArrayList<Hypothesis>();

		// bit strings are formed randomly. They will be validate later on. (No
		// 000 is acceptable!)
		// the validity of the rule is checked within the Rule itself (where
		// it's being generated)
		int counter = 0;
		int upp = examples_train.size();
		while (counter < upp) {
			// randomly generate bit strings until you reach p number of hypo's
			// generate a rule
			Hypothesis h = new Hypothesis(rule_l, target_l, boundaries); // 000 000 00 00 0
			
			//if (name_dataset.equalsIgnoreCase("tennis")){
				//h.generate_random_hypo(boundaries, rnd, name_dataset); // e.g. 001 101 01 11 1
			//}else{ // use train set to generate the population for iris
				h.generate_init_hypo(boundaries, examples_train.get(counter), rnd, name_dataset);
			//}
			// check if the rule is duplicated
			boolean has_duplicates = check4duplicates(population, h);
			has_duplicates = false; // do not check for duplicates
			
			// if yes decrease p otherwise increase it
			//counter = ((has_duplicates) && name_dataset.equalsIgnoreCase("tennis")) 
					//? counter : ++counter;
			counter = (has_duplicates? counter : ++counter);

			// add to the population if not identical
			if (!has_duplicates) {
				population.add(h);
			}

		}
		
		// Only for Iris we need the following
		// add the remaining hypos to finish up with p = 100 hypos 
		// (we had identical training data points)
		int current_size = population.size(); // population size
		if ( current_size != p){
			counter = 0;
			while ( counter < p - current_size){
				// randomly generate bit strings until you reach p number of hypo's
				// generate a rule
				Hypothesis h = new Hypothesis(rule_l, target_l, boundaries); // 000 000 00 00 0
				
				h.generate_random_hypo(boundaries, rnd, name_dataset); // e.g. 001 101 01 11 1
				
				// check if the rule is duplicated
				boolean has_duplicates = check4duplicates(population, h);
				has_duplicates = false; // do not check for duplicates

				// if yes decrease p otherwise increase it
				//counter = ((has_duplicates) && name_dataset.equalsIgnoreCase("tennis")) 
						//? counter : ++counter;
				counter = (has_duplicates? counter : ++counter);

				// add to the population if not identical
				if (!has_duplicates) {
					population.add(h);
				}
			}
		}
		

		
		return population;
	}

	private static boolean check4duplicates(List<Hypothesis> population, Hypothesis new_hypo) {
		// check the population to find a twin rule for r
		// if you have a duplicate rule don't add it to the population
		// and return false
		
		boolean yes_no = false;
		
		for (Hypothesis h: population){
			yes_no = h.identical(new_hypo);
			if (yes_no == true){
				break;
			}
		}
		return yes_no;
	}

	private static void preprocess_Iris(List<Exp> examples) {
		attr_vals.clear();
		ArrayList<String> attrs_tmp = new ArrayList<String>();
		
		// DISCRETIZE THE CONTINUOUS ATTRIBUTES
		for (int i = 0; i < attrs.length; i++){
			
			// 1. SORT EXAMPLES w.r.t. EACH ATTRIBUTE A (one at a time) (e.g. sepal-length)
			List<Exp> examples_sorted = sort(examples, i);
			
			// 2. FIND c THRESHOLDS FOR THE ATTRIBUTE A
			Set<Double> c = find_c(examples_sorted, i);
			
			// 3. FORM NEW DISCRETE ATTRIBUTES (e.g. sepal-length < 5.0)
			attrs_tmp.addAll(formAttributes(c, i));

			// 4. ADD THE ATTRIBUTE WITH ITS VALUES TO OUR KNOWLEDGE (e.g. attr_vals or attrs)
	        //    The values for each discretized attribute A is now true or false 
			updateAttributes(attrs_tmp);
	
		}
     	// 5. ADD ALL NEWLY FORMED ATTRIBUTES TO OUR KNOWLEDGE
		updateAttributes();
		
		//printExamples(examples);
		
	}
	
	private static void updateAttributes() {
		Iterator<String> itr = attr_vals.keySet().iterator();
		attrs = new String[attr_vals.size()];
		int i = 0;
		while (itr.hasNext()){
			attrs[i] = itr.next();
			i++;
		}
	}
	
	private static void updateAttributes(ArrayList<String> attrs_tmp) {
		
		ArrayList<String> vals;
		
		for (int i = 0; i < attrs_tmp.size(); i++){
			vals = new ArrayList<String>();
			vals.add("true"); vals.add("false");
			attr_vals.put(attrs_tmp.get(i), vals);
	
		}
		
	}
	
	private static ArrayList<String> formAttributes(Set<Double> c, int i) {
		// takes a set of numbers representing thresholds over i-th attribute of the original data
		// [ 4.3, 5.9,...] over sepal-length
		
		String attr_label = attrs[i]; // e.g. sepal-length
		String tmp;
		ArrayList<String> attrs_tmp = new ArrayList<String>();
		
		Iterator<Double> itr = c.iterator();
		while (itr.hasNext()){
			tmp = attr_label;
			
			tmp = tmp.concat("<=" + (String.valueOf(itr.next())));
			attrs_tmp.add(tmp);
		}
		
		return attrs_tmp;
	}
	
	private static Set<Double> find_c(List<Exp> examples, int idx) {
		// pinpoint thresholds on which the target value switches
		
		Set<Double> c = new HashSet<Double>();
		DecimalFormat dec_form = new DecimalFormat("0.##");
		
		String sentinel = examples.get(0).getTarget();
		for (int i = 0; i <examples.size(); i++){
			if (!examples.get(i).getTarget().equals(sentinel)){
				double tmp = ((Double.valueOf(examples.get(i - 1).get(idx)) + Double.valueOf(examples.get(i).get(idx))) / 2);
				
				tmp = Double.valueOf(dec_form.format(tmp));
				
				c.add(tmp);
				sentinel = examples.get(i).getTarget();
			}
		}

		
		return c;
	}
	
	private static List<Exp> sort(List<Exp> examples, int a) {
		// sort all examples with respect to attribute A
		for (int i = 0; i < examples.size(); i++){
			if (i < examples.size() - 1){
				for (int j = i + 1; j < examples.size(); j++){
					Double candidate1 = Double.valueOf(examples.get(i).getData()[a]);
					Double candidate2 = Double.valueOf(examples.get(j).getData()[a]);
					//System.out.printf("%s, ", examples.get(i).getData()[a]);
					//System.out.printf("%s%n", examples.get(j).getData()[a]);

					// compare two examples (-1/1 for less/greater than, and 0 for equals to)
					if (candidate1 > candidate2) {
						
						// swap examples
						examples = swap (examples, i, j);
						
						//System.out.printf("%s, ", examples.get(i).getData()[a]);
						//System.out.printf("%s%n", examples.get(j).getData()[a]);

						
					}
				}
			}
		}
		
		return examples;
	}
	
	private static List<Exp> swap(List<Exp> examples, int i, int j) {
		
		Exp tmp = new Exp();
		tmp = examples.get(i).clone();
		examples.set(i, examples.get(j).clone());
		examples.set(j, tmp);

		return examples;
	}
	
	private static void setAttrs_size(int i) {
		attrs_size = i;

	}
	
	private static void readAttributes(String filepath) throws FileNotFoundException, IOException {
		// reads input txt file line by line
		// There are 4 attributes: Outlook, Temperature, Humidity, Wind, plus target PlayTennis
		
		// Outlook Sunny Overcast Rain
		// Temperature Hot Mild Cool
		// Humidity High Normal
		// Wind Weak Strong

		// PlayTennis Yes No
		
		attrs = new String[attrs_size];
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line = reader.readLine();
			int counter = 0;
			
			// read the first 4 lines
			while (!line.isEmpty()){
				
				String[] tmp = line.split(" ");
				ArrayList<String> tmp_vals = new ArrayList<String>();
				
				for (int i = 1; i < tmp.length; i++){
					tmp_vals.add(tmp[i]);
				}
				
				attr_vals.put(tmp[0], tmp_vals);
				
				attrs[counter] = tmp[0];
				
				counter++;
				
				line = reader.readLine();
			}
			
			// now read PlayTennis Yes No
			line = reader.readLine();
			String[] tmp = line.split(" ");
			classes = new int[tmp.length - 1];
			for (int i = 0; i < classes.length; i++){
				classes[i] = i;
			}
			// classes contains 0 or 1 for tennis dataset meanning yes or no
			// classes contains 0, 1, or 2 for iris dataset meaning setosa versicolor virginica
			
		}
		
	}
	
	private static List<Exp> readExamples(String filepath) throws FileNotFoundException, IOException {
		// reads input txt file line by line
		// every line contains a row of a value for all possible attributes plus a target class at the end.
		// Exp is a class containing only one example. (line)
		// we have an array of Exp comprises with the entire training dataset.

		List<Exp> examples = new ArrayList<Exp>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line = reader.readLine();
			
			while (line != null){
				
				// create an example
				Exp e = new Exp(line, attrs.length);
				
				// add an example to the list
				examples.add(e);
				
				line = reader.readLine();
			}
			
		}

		return examples;
	}

}
