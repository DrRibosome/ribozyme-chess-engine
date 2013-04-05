package util.genetic.mutatorV1;

import eval.expEvalV3.Weight;

abstract class WeightMutator implements MutatorPoint{
	private final Weight w;
	WeightMutator(Weight w){
		this.w = w;
	}
	public void mutate() {
		int start = w.start;
		int end = w.end;
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
		setWeight(new Weight(start, end));
	}
	/** called to set the new weight value*/
	public abstract void setWeight(Weight w);
	
	public static int max(final int i1, final int i2){
		return i1 > i2? i1: i2;
	}
}
