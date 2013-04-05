package util.genetic.mutatorV1;

import eval.expEvalV3.Weight;

final class WeightArrayMutator implements MutatorPoint{
	private final Weight[] w;
	private final int[] excludedIndeces;
	WeightArrayMutator(Weight[] w, int[] excludedIndeces){
		this.w = w;
		this.excludedIndeces = excludedIndeces;
	}
	public void mutate() {
		int index;
		while(contains(index = (int)(Math.random()*w.length), excludedIndeces));
		
		w[index] = WeightMutator.mutateWeight(w[index]);
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