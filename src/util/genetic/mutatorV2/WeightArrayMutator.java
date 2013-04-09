package util.genetic.mutatorV2;

import eval.Weight;

final class WeightArrayMutator implements MutatorPoint2{
	private final Weight[] w;
	private final int[] excludedIndeces;
	/** if true, values treated as symmetric about center*/
	private final boolean symmetric;
	private final Callback c;
	WeightArrayMutator(Weight[] w, int[] excludedIndeces){
		this(w, excludedIndeces, false, null);
	}
	WeightArrayMutator(Weight[] w, int[] excludedIndeces, boolean symmetric){
		this(w, excludedIndeces, symmetric, null);
	}
	WeightArrayMutator(Weight[] w, int[] excludedIndeces, boolean symmetric, Callback c){
		this.w = w;
		this.excludedIndeces = excludedIndeces;
		this.symmetric = symmetric;
		this.c = c;
	}
	
	public void mutate() {
		int index;
		while(contains(index = (int)(Math.random()*w.length), excludedIndeces));
		
		w[index] = WeightMutator.mutateWeight(w[index]);
		if(symmetric){
			w[w.length-index] = w[index];
		}
		if(c != null){
			c.callback();
		}
	}
	
	private static boolean contains(int i, int[] l){
		for(int a = 0; a < l.length; a++){
			if(l[a] == i){
				return true;
			}
		}
		return false;
	}
}