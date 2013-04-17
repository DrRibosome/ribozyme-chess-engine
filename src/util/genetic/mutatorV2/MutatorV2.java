package util.genetic.mutatorV2;

import java.util.ArrayList;
import java.util.List;

import util.genetic.GEntity;
import util.genetic.mutatorV2.getters.BishopPairGetter;
import util.genetic.mutatorV2.getters.DoubledPawnsGetter;
import util.genetic.mutatorV2.getters.IsolatedPawnsGetter;
import util.genetic.mutatorV2.getters.KingDangerAttacksGetter;
import util.genetic.mutatorV2.getters.KingDangerSquareGetter;
import util.genetic.mutatorV2.getters.MaterialWeightGetter;
import util.genetic.mutatorV2.getters.MobilityGetter;
import util.genetic.mutatorV2.getters.PassedPawnGetter;
import util.genetic.mutatorV2.getters.PawnChainGetter;
import util.genetic.mutatorV2.getters.PawnShelterGetter;
import util.genetic.mutatorV2.getters.PawnStormGetter;
import util.genetic.mutatorV2.getters.TempoGetter;
import eval.expEvalV3.EvalParameters;

public final class MutatorV2 implements Mutator2{
	private final static List<Getter> l;
	
	static {
		l = new ArrayList<Getter>();
		
		MaterialWeightGetter.add(l);
		MobilityGetter.add(l);
		
		PassedPawnGetter.add(l);
		PawnChainGetter.add(l);
		DoubledPawnsGetter.add(l);
		IsolatedPawnsGetter.add(l);
		
		//KingDangerSquareGetter.add(l);
		//KingDangerAttacksGetter.add(l);
		PawnShelterGetter.add(l);
		PawnStormGetter.add(l);
		
		BishopPairGetter.add(l);
		TempoGetter.add(l);
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
		assert len != 0;
		return Math.sqrt(sqrdSum/len - Math.pow(sum/len, 2));
	}
	
	private static double avg(Getter g, GEntity[] p){
		double sum = 0;
		for(int a = 0; a < p.length; a++){
			sum += g.get(p[a].p);
		}
		return sum/p.length;
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
	public void initialMutate(EvalParameters p, double variancePercent) {
		for(Getter g: l){
			final int v = g.get(p);
			final int min = 10;
			final int x = Math.abs(v) <= min? min: v;
			//g.set(p, (int)(r.nextGaussian()*stdDev+v));
			g.set(p, (int)Math.floor((Math.random()-.5)*variancePercent*x + v + .5));
		}
	}

	@Override
	public void printStdDev(GEntity[] population) {
		for(Getter g: l){
			double stdDev = stdDev(g, population, -1);
			System.out.println(g+" = "+stdDev+" (avg = "+avg(g, population)+")");
		}
	}
	
	private static int generateSample(final int v, final double stdDev, final double multiplier){
		assert multiplier != 0;
		return (int)Math.floor((Math.random()-.5)*stdDev*multiplier + v + .5);
	}
}
