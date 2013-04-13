package util.genetic.mutatorV2.getters;

import java.util.List;

import util.genetic.mutatorV2.Getter;
import eval.expEvalV3.EvalParameters;

public final class PawnShelterGetter {
	public static void add(List<Getter> l){
		final String[] names = new String[]{
				"adj king file",
				"in king file",
		};
		
		for(int q = 0; q < 2; q++){
			final int type = q;
			for(int a = 0; a < 7; a++){
				final int index = a;
				l.add(new Getter() {
					@Override
					public int get(EvalParameters p) {
						return p.pawnShelter[type][index];
					}
					@Override
					public void set(EvalParameters p, int i) {
						p.pawnShelter[type][index] = i;
					}
					@Override
					public String toString(){
						return "pawn shelter, "+names[type];
					}
				});
			}
		}
	}
}
