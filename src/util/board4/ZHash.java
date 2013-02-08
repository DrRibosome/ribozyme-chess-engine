package util.board4;

import java.util.Random;

/** zobrist hashs*/
public final class ZHash {
	
	/** indexed [player][type][position]*/
	public final static long[][][] zhash;
	public final static long[] kingMoved;
	public final static long[][] rookMoved;
	
	/** en passant enabled for target square*/
	public final static long[] enPassante;

	public final static long[] turn;
	/** apply to change turn from player to 1-player*/
	public final static long turnChange;
	
	/** appeared twice*/
	public final static long appeared2;
	/** appeared greater than or equal to 3 times*/
	public final static long appeared3;
	
	static{
		Random r = new Random();
		zhash = new long[2][7][64];
		for(int q = 0; q < 2; q++){
			for(int i = 0; i < zhash[0].length; i++){
				for(int a = 0; a < 64; a++){
					zhash[q][i][a] = r.nextLong();
					//System.out.println(zhash[q][i][a]);
				}
			}
		}
		
		enPassante = new long[64];
		for(int i = 0; i < 64; i++){
			enPassante[i] = r.nextLong();
		}
		
		turn = new long[2];
		turn[0] = r.nextLong();
		turn[1] = r.nextLong();
		turnChange = turn[0]^turn[1];
		
		appeared2 = r.nextLong();
		appeared3 = r.nextLong();
		
		kingMoved = new long[2];
		kingMoved[0] = r.nextLong();
		kingMoved[1] = r.nextLong();
		
		rookMoved = new long[2][2];
		rookMoved[0][0] = r.nextLong();
		rookMoved[0][1] = r.nextLong();
		rookMoved[1][0] = r.nextLong();
		rookMoved[1][1] = r.nextLong();
	}
}
