package util.genetic;

import state4.State4;
import eval.expEvalV3.EvalParameters;
import eval.expEvalV3.Weight;

final class MutatorV1 implements Mutator{
	private static double mDist = .05; // max dist a value can move as a percent
	
	private static interface MutatorPoint{
		void mutate();
	}
	private final static class WeightMatrixMutator implements MutatorPoint{
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
				final double offset = -start*mDist + start*mDist*2*Math.random();
				start += min((int)offset, 1);
			} else{
				if(choice == 0){
					final double offset = -end*mDist + end*mDist*2*Math.random();
					end += min((int)offset, 1);
				}
			}
			w[i1][i2] = new Weight(start, end);
		}
	}
	private final static class WeightArrayMutator implements MutatorPoint{
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
				final double offset = -start*mDist + start*mDist*2*Math.random();
				start += min((int)offset, 1);
			} else{
				if(choice == 0){
					final double offset = -end*mDist + end*mDist*2*Math.random();
					end += min((int)offset, 1);
				}
			}
			w[index] = new Weight(start, end);
		}
	}
	private final static class IntArrayMutator implements MutatorPoint{
		private final int[] w;
		private final int[] excludedIndeces;
		IntArrayMutator(int[] w, int[] excludedIndeces){
			this.w = w;
			this.excludedIndeces = excludedIndeces;
		}
		public void mutate() {
			int index;
			while(contains(index = (int)(Math.random()*w.length), excludedIndeces));
			
			int value = w[index];
			final double offset = -value*mDist + value*mDist*2*Math.random();
			value += min((int)offset, 1);
			w[index] = value;
		}
	}
	private static boolean contains(int i, int[] l){
		for(int a = 0; a < l.length; a++){
			if(l[a] == i){
				return true;
			}
		}
		return false;
	}
	
	private static MutatorPoint[] getMutationPoints(EvalParameters p){
		final MutatorPoint[] m = new MutatorPoint[2];
		int index = 0;
		
		m[index++] = new WeightMatrixMutator(p.mobilityWeights);
		m[index++] = new IntArrayMutator(p.materialWeights, 
				new int[]{State4.PIECE_TYPE_EMPTY,State4.PIECE_TYPE_PAWN,State4.PIECE_TYPE_KING});
		
		return m;
	}
	
	@Override
	public void mutate(EvalParameters p, int mutations) {
		final MutatorPoint[] m = getMutationPoints(p);
		for(int a = 0; a < mutations; a++){
			m[(int)(Math.random()*m.length)].mutate();
		}
	}
	
	
	private static int min(final int i1, final int i2){
		return i1 < i2? i1: i2;
	}

}
