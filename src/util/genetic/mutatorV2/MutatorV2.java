package util.genetic.mutatorV2;

import java.util.ArrayList;
import java.util.List;

import util.genetic.GEntity;
import util.genetic.mutatorV2.getters.DoubledPawnsGetter;
import util.genetic.mutatorV2.getters.MaterialWeightGetter;
import util.genetic.mutatorV2.getters.MobilityGetter;
import util.genetic.mutatorV2.getters.PassedPawnGetter;
import util.genetic.mutatorV2.getters.PawnChainGetter;
import eval.expEvalV3.EvalParameters;

public final class MutatorV2 implements Mutator2{
	private final static List<Getter> l;
	
	static {
		l = new ArrayList<Getter>();
		MaterialWeightGetter.add(l);
		PassedPawnGetter.add(l);
		PawnChainGetter.add(l);
		MobilityGetter.add(l);
		DoubledPawnsGetter.add(l);
	}
	
	private static double stdDev(Getter g, GEntity[] population, int exclude){
		double sum = 0;
		double sqrdSum = 0;
		int len = 0;
		for(int a = 0; a < population.length; a++){
			if(a != exclude){
				final double d = g.get(population[a].p);
				sum += d;
				sqrdSum += d*d;
				len++;
			}
		}
		len--; //to compute unbiased variance estimator
		assert len != 0;
		return Math.sqrt(sqrdSum/len - Math.pow(sum/len, 2));
	}
	
	@Override
	public void mutate(EvalParameters p, GEntity[] population, int excludeIndex, double multiplier) {
		for(Getter g: l){
			double stdDev = stdDev(g, population, excludeIndex);
			int v = g.get(p);
			g.set(p, generateSample(v, stdDev, multiplier));
		}
	}

	@Override
	public void initialMutate(EvalParameters p, double stdDev) {
		for(Getter g: l){
			int v = g.get(p);
			//g.set(p, (int)(r.nextGaussian()*stdDev+v));
			g.set(p, generateSample(v, stdDev, 1));
		}
	}

	@Override
	public void printStdDev(GEntity[] population) {
		for(Getter g: l){
			double stdDev = stdDev(g, population, -1);
			System.out.println(g+" = "+stdDev);
		}
	}
	
	private static int generateSample(final int v, final double stdDev, final double multiplier){
		return (int)((Math.random()-.5)*stdDev*multiplier+v+.5);
	}
}
