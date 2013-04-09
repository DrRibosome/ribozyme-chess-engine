package util.genetic.mutatorV1;

import util.genetic.GEntity;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public final class MutatorV1 implements Mutator{
	/** max dist a value can move as a percent*/
	public static double mDist = .1;
	
	private static MutatorPoint[] getMutationPoints(final EvalParameters p){
		final MutatorPoint[] m = new MutatorPoint[1];
		int index = 0;
		
		/*m[index++] = new WeightMatrixMutator(p.mobilityWeights);
		m[index++] = new IntArrayMutator(p.materialWeights, 
				new int[]{State4.PIECE_TYPE_EMPTY,State4.PIECE_TYPE_PAWN,State4.PIECE_TYPE_KING});*/
		
		m[index++] = new WeightMutator(p.bishopPair) {
			public void setWeight(Weight w) {
				p.bishopPair = w;
			}
		};
		/*m[index++] = new WeightMutator(p.tempo) {
			public void setWeight(Weight w) {
				p.tempo = w;
			}
		};
		m[index++] = new WeightArrayMutator(p.doubledPawns[0], new int[0], true);
		m[index++] = new WeightArrayMutator(p.doubledPawns[1], new int[0], true);*/
		/*m[index++] = new WeightArrayMutator(p.passedPawnRowWeight[0], new int[]{0,7}, false, new Callback() {
			public void callback() {
				for(int a = 0; a < 8; a++) p.passedPawnRowWeight[1][a] = p.passedPawnRowWeight[0][7-a];
			}
		});*/
		
		return m;
	}
	
	@Override
	public void mutate(EvalParameters p, int mutations, GEntity parent) {
		final MutatorPoint[] m = getMutationPoints(p);
		for(int a = 0; a < mutations; a++){
			m[(int)(Math.random()*m.length)].mutate(parent);
		}
	}
}
