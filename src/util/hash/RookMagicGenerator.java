package util.hash;

import java.util.Map;
import java.util.Set;

import state4.Masks;
import state4.State4;

/** run to generate rook magics*/
public class RookMagicGenerator {
	public static void main(String[] args){
		final int maxTests = 9999999;
		final int bits = 13;
		
		final long[] magics = new long[64];
		for(int i = 0; i < 64; i++){
			Map<Long, Set<Long>> m = HashGenerator.genEquivalenceClasses(Masks.rookMoves[i], i, State4.PIECE_TYPE_ROOK);
			magics[i] = HashGenerator.genMagics(m, maxTests, bits);
			System.out.print(magics[i]+"L, ");
			if((i+1) % 4 == 0){
				System.out.println();
			}
		}
	}
}
