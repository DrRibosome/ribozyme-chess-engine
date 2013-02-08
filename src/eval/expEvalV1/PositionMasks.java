package eval.expEvalV1;


public final class PositionMasks {
	/** masks to check whether pawn is supported from either
	 * the left or right by another pawn*/
	public final static long[][] supportedPawn;
	
	static{
		supportedPawn = new long[2][64];
		for(int i = 0; i < 2; i++){
			for(int a = 0; a < 64; a++){
				//supportedPawn[i][a] = 1L<<a;
				if(a%8 != 0){
					supportedPawn[i][a] |= i==0? 1L<<a-9: 1L<<a+7;
				}
				if(a%8 != 7){
					supportedPawn[i][a] |= i==0? 1L<<a-7: 1L<<a+9;
				}
				//System.out.println(Masks.getString(supportedPawn[i][a]));
			}
		}
	}
}
