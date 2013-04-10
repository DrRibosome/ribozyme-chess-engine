package util.genetic.mutatorV2.getters;

import java.util.List;

import util.genetic.mutatorV2.Getter;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public final class PawnChainGetter{
	public static void add(List<Getter> l){
		for(int a = 0; a < 4; a++){
			final int index = a;
			l.add(new Getter(){
				public int get(EvalParameters p){
					return p.pawnChain[index].start;
				}
				public void set(EvalParameters p, int i){
					final int end = p.pawnChain[index].end;
					p.pawnChain[index] = new Weight(i, end);
					p.pawnChain[7-index] = new Weight(i, end);
				}
				public String toString(){
					return "pawn chain (c="+index+",start) weight";
				}
			});
			l.add(new Getter(){
				public int get(EvalParameters p){
					return p.pawnChain[index].end;
				}
				public void set(EvalParameters p, int i){
					final int start = p.pawnChain[index].start;
					p.pawnChain[index] = new Weight(start, i);
					p.pawnChain[7-index] = new Weight(start, i);
				}
				public String toString(){
					return "pawn chain (c="+index+",end) weight";
				}
			});
		}
	}
}
