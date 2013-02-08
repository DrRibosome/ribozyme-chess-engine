package eval.testEval2;

import util.board4.Masks;
import util.board4.State4;

public final class TestFeatureExtractor {
	
	public static int countDoubledPawns(int player, State4 s){
		int count = 0;
		for(int a = 0; a < 8; a++){
			long col = Masks.colMask[a] & s.pawns[player];
			if(col != 0 && (col&(col-1)) != 0){
				count++;
			}
		}
		
		return count;
	}
}
