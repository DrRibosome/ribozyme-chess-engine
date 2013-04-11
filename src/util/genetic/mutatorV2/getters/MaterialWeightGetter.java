package util.genetic.mutatorV2.getters;

import java.util.List;

import state4.State4;
import util.genetic.mutatorV2.Getter;
import eval.expEvalV3.EvalParameters;

public final class MaterialWeightGetter{
	public static void add(List<Getter> l){
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
			final int ptype = pieces[a];
			final String pname = pieceNames[a];
			l.add(new Getter(){
				public int get(EvalParameters p){
					return p.materialWeights[ptype];
				}
				public void set(EvalParameters p, int i){
					assert i != 0;
					p.materialWeights[ptype] = i; 
				}
				public String toString(){
					return pname+" weight";
				}
			});
		}
	}
}
