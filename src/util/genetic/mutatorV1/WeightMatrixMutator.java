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
		
		w[i1][i2] = WeightMutator.mutateWeight(w[i1][i2]);
	}
}
