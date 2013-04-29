package util.genetic.mutatorV2.getters;

import java.util.List;

import util.genetic.mutatorV2.Getter;
import eval.EvalParameters;
import eval.Weight;

public final class IsolatedPawnsGetter{
	public static void add(List<Getter> l){
		
		final String name = "isolated pawns";
		
		for(int q = 0; q < 2; q++){
			final int opp = q; //opposed flag
			for(int a = 0; a < 4; a++){
				final int index = a;
				l.add(new Getter(){
					public int get(EvalParameters p){
						return p.isolatedPawns[opp][index].start;
					}
					public void set(EvalParameters p, int i){
						final int end = p.isolatedPawns[opp][index].end;
						p.isolatedPawns[opp][index] = new Weight(i, end);
						p.isolatedPawns[opp][7-index] = new Weight(i, end);
					}
					public String toString(){
						return name+" (c="+index+",start,opp="+opp+") weight";
					}
				});
				l.add(new Getter(){
					public int get(EvalParameters p){
						return p.isolatedPawns[opp][index].end;
					}
					public void set(EvalParameters p, int i){
						final int start = p.isolatedPawns[opp][index].start;
						p.isolatedPawns[opp][index] = new Weight(start, i);
						p.isolatedPawns[opp][7-index] = new Weight(start, i);
					}
					public String toString(){
						return name+" (c="+index+",end,opp="+opp+") weight";
					}
				});
			}
		}
	}
}
