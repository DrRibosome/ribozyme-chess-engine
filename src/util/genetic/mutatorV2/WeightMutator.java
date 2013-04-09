package util.genetic.mutatorV2;

import java.util.Random;

import util.genetic.GEntity;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

abstract class WeightMutator implements MutatorPoint2{
	private final Weight w;
	
	WeightMutator(Weight w){
		this.w = w;
	}
	
	abstract Weight getWeight(EvalParameters p);
	
	public void mutate(double stdDev) {
		setWeight(mutateWeight(w, stdDev));
	}
	
	static Weight mutateWeight(Weight w, double[] stdDev){
		int start = w.start;
		int end = w.end;
		final Random r = new Random();
		start += stdDev[0]*r.nextGaussian();
		end += stdDev[1]*r.nextGaussian();
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
