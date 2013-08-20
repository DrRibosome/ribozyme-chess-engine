package state4;

import java.util.Random;

/** zobrist hashs*/
public final class ZHash {
	
	/** indexed [player][type][position]*/
	public final long[] zhash;
	/** player information block size in {@link #zhash}, used to determine index offsets*/
	private final static int playerOffset = 6*64;
	
	/** records castling rights, indexed [player][castle-side] where castle-side==left? 0: 1*/
	public final long[][] canCastle;
	
	/** en passant enabled for target square*/
	public final long[] enPassante;

	public final long[] turn;
	/** apply to change turn from player to 1-player*/
	public final long turnChange;
	
	/** appeared twice*/
	public final long appeared2;
	/** appeared greater than or equal to 3 times*/
	public final long appeared3;
	
	public ZHash(final long seed){
		Random r = new Random(seed);
		
		zhash = new long[2*playerOffset];
		for(int q = 0; q < 2; q++){
			for(int i = 0; i < 6; i++){
				for(int a = 0; a < 64; a++){
					int index = q*playerOffset + i*64 + a;
					zhash[index] = r.nextLong();
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
		
		canCastle = new long[2][2];
		canCastle[0][0] = r.nextLong();
		canCastle[0][1] = r.nextLong();
		canCastle[1][0] = r.nextLong();
		canCastle[1][1] = r.nextLong();
	}
	
	public long getZHash(final int player, final int pieceType, final int position){
		assert pieceType != State4.PIECE_TYPE_EMPTY;
		final int index = player*playerOffset + (pieceType-1)*64 + position;
		return zhash[index];
	}
}
