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
		
		int start = w[index].start;
		int end = w[index].end;
		final int choice = (int)(Math.random()*2);
		if(choice == 0){
			final double offset = -start*MutatorV1.mDist + start*MutatorV1.mDist*2*Math.random();
			start += max((int)offset, 1);
		} else{
			if(choice == 0){
				final double offset = -end*MutatorV1.mDist + end*MutatorV1.mDist*2*Math.random();
				end += max((int)offset, 1);
			}
		}
		w[index] = new Weight(start, end);
	}
	
	private static int max(final int i1, final int i2){
		return i1 > i2? i1: i2;
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