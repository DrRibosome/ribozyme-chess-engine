package util.genetic.mutatorV2.getters;

import java.util.List;

import state4.State4;
import util.genetic.mutatorV2.Getter;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public final class MobilityGetter{
	public static void add(List<Getter> l){
		
		final int[] movement = new int[7];
		movement[State4.PIECE_TYPE_BISHOP] = 16;
		movement[State4.PIECE_TYPE_KNIGHT] = 9;
		movement[State4.PIECE_TYPE_ROOK] = 16;
		movement[State4.PIECE_TYPE_QUEEN] = 32;
		
		final String[] names = new String[7];
		names[State4.PIECE_TYPE_BISHOP] = "bishop";
		names[State4.PIECE_TYPE_KNIGHT] = "knight";
		names[State4.PIECE_TYPE_ROOK] = "rook";
		names[State4.PIECE_TYPE_QUEEN] = "queen";
		
		final String name = "mobility";
		
		for(int a = 0; a < 7; a++){
			final int ptype = a;
			if(movement[ptype] > 0){
				for(int q = 0; q < movement[a]; q++){
					final int mov = q;
					l.add(new Getter(){
						public int get(EvalParameters p){
							return p.mobilityWeights[ptype][mov].start;
						}
						public void set(EvalParameters p, int i){
							p.mobilityWeights[ptype][mov] = new Weight(i, 
									p.mobilityWeights[ptype][mov].end);
						}
						public String toString(){
							return name+" (m="+mov+",start,"+names[ptype]+") weight";
						}
					});
					l.add(new Getter(){
						public int get(EvalParameters p){
							return p.mobilityWeights[ptype][mov].end;
						}
						public void set(EvalParameters p, int i){
							p.mobilityWeights[ptype][mov] = new Weight( 
									p.mobilityWeights[ptype][mov].start, i);
						}
						public String toString(){
							return name+" (m="+mov+",end,"+names[ptype]+") weight";
						}
					});
				}
			}
		}
	}
}
