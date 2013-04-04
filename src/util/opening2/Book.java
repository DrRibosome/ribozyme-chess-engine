package util.opening2;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import state4.BitUtil;
import state4.State4;

public final class Book {
	private long seed;
	private final Map<Long, BookRecord> book = new HashMap<Long, BookRecord>();
	
	public Book(File f){
		try{
			final DataInputStream dis = new DataInputStream(new FileInputStream(f));
			seed = dis.readLong();
			while(dis.available() > 0){
				BookRecord r = BookRecord.read(dis);
				book.put(r.key, r);
			}
			dis.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/** 
	 * gets the hash seed that must be used in the state
	 * in order to recover book moves
	 */
	public long getSeed(){
		return seed;
	}
	
	public List<int[]> getMoves(int player, State4 s){
		return getMoves(player, s, 0);
	}

	/** gets moves that appear in at least minCount games*/
	public List<int[]> getMoves(int player, State4 s, int minCount){
		assert s.getZkeySeed() == seed;
		
		List<int[]> moves = new ArrayList<int[]>();
		genMoves(player, s, moves);
		
		List<int[]> available = new ArrayList<int[]>();
		for(int[] m: moves){
			s.executeMove(player, 1L<<m[0], 1L<<m[1]);
			
			final BookRecord r;
			if((r = book.get(s.zkey())) != null && r.count >= minCount){
				available.add(m);
			}
			s.undoMove();
		}
		
		return available;
	}
	
	public int[] getRandomMove(int player, State4 s){
		return getRandomMove(player, s, 0);
	}
	
	/** gets a random move that appears in at least minCount games*/
	public int[] getRandomMove(int player, State4 s, int minCount){
		final List<int[]> l = getMoves(player, s, minCount);
		return l.size() != 0? l.get((int)(Math.random()*l.size())): null;
	}

	private final static int[] pawnOffset = new int[]{9,7,8,16};
	private static void genMoves(final int player, final State4 s, List<int[]> l){
		
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			recordMoves(queens, State4.getQueenMoves(player, s.pieces, queens), l);
		}

		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			recordMoves(rooks, State4.getRookMoves(player, s.pieces, rooks), l);
		}
		
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			recordMoves(bishops, State4.getBishopMoves(player, s.pieces, bishops), l);
		}
		
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			recordMoves(knights, State4.getKnightMoves(player, s.pieces, knights), l);
		}
		
		for(long kings = s.kings[player]; kings != 0; kings &= kings-1){
			recordMoves(kings, State4.getKingMoves(player, s.pieces, kings)|
					State4.getCastleMoves(player, s), l);
		}

		
		//handle pawn moves specially
		final long[] pawnMoves = new long[4];
		final long pawns = s.pawns[player];
		pawnMoves[0] = State4.getRightPawnAttacks(player, s.pieces, s.enPassante, pawns);
		pawnMoves[1] = State4.getLeftPawnAttacks(player, s.pieces, s.enPassante, pawns);
		pawnMoves[2] = State4.getPawnMoves(player, s.pieces, pawns);
		pawnMoves[3] = State4.getPawnMoves2(player, s.pieces, pawns);

		for(int a = 0; a < 4; a++){
			for(long tempPawnMoves = pawnMoves[a]; tempPawnMoves != 0; tempPawnMoves &= tempPawnMoves-1){
				long moveMask = tempPawnMoves&-tempPawnMoves;
				long pawnMask = player == 0? moveMask>>>pawnOffset[a]: moveMask<<pawnOffset[a];
				recordMoves(pawnMask, moveMask, l);
			}
		}
	}
	
	private static void recordMoves(long piece, long moves, List<int[]> l){
		for(; moves != 0; moves &= moves-1){
			l.add(new int[]{BitUtil.lsbIndex(piece), BitUtil.lsbIndex(moves)});
		}
	}
}
