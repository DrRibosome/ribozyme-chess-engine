package util.genetic.mutatorV1;

import state4.State4;
import util.genetic.Mutator;
import eval.expEvalV3.EvalParameters;
import eval.expEvalV3.Weight;

public final class MutatorV1 implements Mutator{
	static double mDist = .05; // max dist a value can move as a percent
	
	private static MutatorPoint[] getMutationPoints(final EvalParameters p){
		final MutatorPoint[] m = new MutatorPoint[6];
		int index = 0;
		
		m[index++] = new WeightMatrixMutator(p.mobilityWeights);
		m[index++] = new IntArrayMutator(p.materialWeights, 
				new int[]{State4.PIECE_TYPE_EMPTY,State4.PIECE_TYPE_PAWN,State4.PIECE_TYPE_KING});
		
		m[index++] = new WeightMutator(p.bishopPair) {
			public void setWeight(Weight w) {
				p.bishopPair = w;
			}
		};
		m[index++] = new WeightMutator(p.tempo) {
			public void setWeight(Weight w) {
				p.tempo = w;
			}
		};
		
		return m;
	}
	
	@Override
	public void mutate(EvalParameters p, int mutations) {
		final MutatorPoint[] m = getMutationPoints(p);
		for(int a = 0; a < mutations; a++){
			m[(int)(Math.random()*m.length)].mutate();
		}
	}
}
