package util.genetic.mutatorV1;

import eval.expEvalV3.Weight;

final class WeightMatrixMutator implements MutatorPoint{
	private final Weight[][] w;
	WeightMatrixMutator(Weight[][] w){
		this.w = w;
	}
	public void mutate() {
		int i1;
		while(w[i1 = (int)(Math.random()*w.length)].length == 0);
		final int i2 = (int)(Math.random()*w[i1].length);
		final int choice = (int)(Math.random()*2);
		int start = w[i1][i2].start;
		int end = w[i1][i2].end;
		if(choice == 0){
			final double offset = -start*MutatorV1.mDist + start*MutatorV1.mDist*2*Math.random();
			start += max((int)offset, 1);
		} else{
			if(choice == 0){
				final double offset = -end*MutatorV1.mDist + end*MutatorV1.mDist*2*Math.random();
				end += max((int)offset, 1);
			}
		}
		w[i1][i2] = new Weight(start, end);
	}
	
	private static int max(final int i1, final int i2){
		return i1 > i2? i1: i2;
	}
}
