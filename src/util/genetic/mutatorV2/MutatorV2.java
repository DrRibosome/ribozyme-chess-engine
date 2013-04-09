package util.genetic.mutatorV2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import state4.State4;
import util.genetic.GEntity;
import eval.expEvalV3.EvalParameters;

public final class MutatorV2 implements Mutator2{
	/** max dist a value can move as a percent*/
	public static double mDist = .1;
	
	interface Getter{
		int get(EvalParameters p);
		void set(EvalParameters p, int i);
	}
	
	private final static List<Getter> l;
	
	static {
		l = new ArrayList<>();
		final int[] pieces = new int[]{
				State4.PIECE_TYPE_BISHOP,
				State4.PIECE_TYPE_KNIGHT,
				State4.PIECE_TYPE_QUEEN,
				State4.PIECE_TYPE_ROOK
		};
		final String[] pieceNames = new String[]{
				"bishop",
				"knight",
				"queen",
				"rook"
		};
		
		for(int a = 0; a < pieces.length; a++){
			final int index = a;
			l.add(new Getter(){
				public int get(EvalParameters p){
					return p.materialWeights[pieces[index]];
				}
				public void set(EvalParameters p, int i){
					p.materialWeights[pieces[index]] = i; 
				}
				public String toString(){
					return pieceNames[index]+" weight";
				}
			});
		}
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
	
	@Override
	public void mutate(EvalParameters p, GEntity[] population, int excludeIndex, double multiplier) {
		final Random r = new Random();
		for(Getter g: l){
			double stdDev = stdDev(g, population, excludeIndex);
			int v = g.get(p);
			g.set(p, (int)(r.nextGaussian()*stdDev+v));
		}
	}

	@Override
	public void initialMutate(EvalParameters p, double stdDev) {
		final Random r = new Random();
		for(Getter g: l){
			int v = g.get(p);
			g.set(p, (int)(r.nextGaussian()*stdDev+v));
		}
	}

	@Override
	public void printStdDev(GEntity[] population) {
		for(Getter g: l){
			double stdDev = stdDev(g, population, -1);
			System.out.println(g+" = "+stdDev);
		}
	}
}
