package util.genetic.mutatorV1;

import java.util.Random;

import util.genetic.GEntity;
import eval.Weight;

abstract class WeightMutator implements MutatorPoint{
	private final Weight w;
	
	WeightMutator(Weight w){
		this.w = w;
	}
	
	public void mutate(GEntity parent) {
		setWeight(mutateWeight(w, parent.variance));
	}
	
	static Weight mutateWeight(Weight w, double variance){
		int start = w.start;
		int end = w.end;
		final Random r = new Random();
		start += Math.sqrt(variance)*r.nextGaussian();
		end += Math.sqrt(variance)*r.nextGaussian();
		return new Weight(start, end);
	}
	
	/** called to set the new weight value*/
	public abstract void setWeight(Weight w);
	
	public static int max(final int i1, final int i2){
		return i1 > i2? i1: i2;
	}
	
	public static int sign(double d){
		return d == 0? 0: d < 0? -1: 1;
	}
}
