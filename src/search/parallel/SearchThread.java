package search.parallel;

import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import search.SearchListener2;
import search.SearchStat;
import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.Evaluator2;

public final class SearchThread extends Thread{
	public final static class SearchStat32k extends SearchStat{
		/** scores returned from quiet search without bottoming out*/
		public long forcedQuietCutoffs;
		public long nullMoveVerifications;
		public long nullMoveCutoffs;
		public long nullMoveFailLow;
		/** scores seen pes ply*/
		public int[] scores;
		public int maxPlySearched;
		
		public String toString(){
			return "n="+nodesSearched+", t="+searchTime+", hh="+hashHits+", qc="+forcedQuietCutoffs;
		}
	}
	
	private final MoveList[] stack;
	
	private final static class MoveList{
		private final static int defSize = 128;
		public final long[] pieceMasks = new long[defSize];
		public final long[] moves = new long[defSize];
		public final int[] ranks = new int[defSize];
		public final long[] pawnMoves = new long[4];
		public int length;
		public final boolean[] kingAttacked = new boolean[2];
		public final long[] upTakes = new long[7];
		public boolean skipNullMove = false;
		/** holds killer moves as first 12 bits (ie, masked 0xFFF) of move encoding*/
		public final long[] killer = new long[2];
	}
	
	private final static int[] pawnOffset = new int[]{9,7,8,16};
	/** rough material gain by piece type*/
	private final static int[] materialGain = new int[7];
	/** stores lmr reductions by [isPv][depth][move-count]*/
	private final static int[][][] reductions;
	/** stores futility margins by [depth][move-count]*/
	private final static int[][] futilityMargins;
	/** stores futility margins for move count pruning by [depth]*/
	private final static int[] futilityMoveCounts;
	
	static{
		materialGain[State4.PIECE_TYPE_BISHOP] = 300;
		materialGain[State4.PIECE_TYPE_KNIGHT] = 300;
		materialGain[State4.PIECE_TYPE_PAWN] = 100;
		materialGain[State4.PIECE_TYPE_QUEEN] = 900;
		materialGain[State4.PIECE_TYPE_ROOK] = 500;
		
		reductions = new int[2][64][64];
		for (int d = 1; d < 64; d++){ //depth
			//System.out.print("d="+d+":\t");
			for (int mc = 1; mc < 64; mc++) //move count
			{
				final double pvRed = Math.log(d) * Math.log(mc) / 3.0;
				final double nonPVRed = 0.33 + Math.log(d) * Math.log(mc) / 2.25;
				reductions[1][d][mc] = (int)(pvRed > 1? pvRed: 0);
				reductions[0][d][mc] = (int)(nonPVRed > 1? nonPVRed: 0);
				//System.out.print(reductions[1][d][mc]+", ");
			}
			//System.out.println();
		}
		
		futilityMargins = new int[16][64];
		for (int d = 1; d < 16; d++){
			//System.out.print("d="+d+":\t");
			for (int mc = 0; mc < 64; mc++){
				futilityMargins[d][mc] = 112 * (int)(Math.log(d*d/2.) / Math.log(2) + 1.001) - 8 * mc + 45;
				//System.out.print(futilityMargins[d][mc]+",\t");
			}
			//System.out.println();
		}
		
		futilityMoveCounts = new int[32];
		for (int d = 0; d < 32; d++){
			futilityMoveCounts[d] = (int)(3.001 + 0.25 * Math.pow(d, 2));
			//System.out.print(futilityMoveCounts[d]+", ");
		}
		//System.out.println();
	}
	
	private final SearchStat32k stats = new SearchStat32k();
	private final Evaluator2 e;
	private final int qply = 12;
	private final Hash m;
	private FileWriter f;
	private SearchListener2 l;
	private final static int stackSize = 256;
	/** sequence number for hash entries*/
	private int seq;
	private final AtomicBoolean cutoffSearch = new AtomicBoolean(false);
	private final TTEntry fillEntry = new TTEntry();
	private final static int tteMoveRank = -1;
	/** rank set to the first of the non takes*/
	private final static int killerMoveRank = 5;
	private final SplitQueue q;
	private final AtomicBoolean sleep = new AtomicBoolean(false);
	private final SplitQueue.SplitState splitState = new SplitQueue.SplitState(new State4());

	/** stores alpha beta information, indexed [stack-index][0:alpha, 1:beta]*/
	private final int[][] ab;
	/** stores move count information*/
	private final int[] moveCount;

	private static class TreeStack{
		final int[] ab = new int[2];
		int score;
		boolean pv;
		boolean cutoff;
		int moveCount;
		
		boolean splitHit;
		/** index in the stack of the split hit*/
		int splitIndex;
	}
	private final TreeStack[] t;
	
	public SearchThread(SplitQueue q, Evaluator2 e, int hashSize){
		this.e = e;
		
		m = new ZMap4(hashSize);
		
		stack = new MoveList[stackSize];
		for(int i = 0; i < stack.length; i++){
			stack[i] = new MoveList();
		}
		stats.scores = new int[stackSize];
		
		ab = new int[stackSize][2];
		moveCount = new int[stackSize];
		
		this.q = q;
		setDaemon(true);
		
		t = new TreeStack[stackSize];
		for(int a = 0; a < stackSize; a++) t[a] = new TreeStack();
	}
	
	@Override
	public void run(){
		for(;;){
			while(!sleep.get()){
				final boolean loaded = q.get(splitState);
				if(loaded){
					cutoffSearch.set(false);
					final int player = splitState.sideToMove;
					final State4 s = splitState.s;
					final int alpha = splitState.alpha;
					final int beta = splitState.beta;
					final boolean pv = splitState.pv;
					final int depth = splitState.depth;
					
					final TreeStack t = descend(player, s, depth, alpha, beta, pv, 0);
					final SplitPoint sp = splitState.sp;
					final int score = t.score;
					if(splitState.sp.score.get() < score){
						int exp;
						while((exp = splitState.sp.score.get()) < score){
							splitState.sp.score.compareAndSet(exp, score);
						}
					}
					final int completionIndex = sp.completed.incrementAndGet();
					if(completionIndex == sp.totalMoves){
						//completed a split point, time to load and continue parent
					}
				}
			}
			
			synchronized(this){
				try{
					wait();
				} catch(InterruptedException e){}
			}
		}
	}
	
	public synchronized void setSleep(final boolean sleep){
		this.sleep.set(sleep);
		cutoffSearch.set(true);
		interrupt();
	}
	
	/** tests to see if the passed player is in check*/
	private static boolean isChecked(final int player, final State4 s){
		return State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
	}
	
	private TreeStack descend(final int player, final State4 s, final int depth,
			int alpha, final int beta, final boolean pv, final int stackIndex){
		
		final TreeStack t = this.t[stackIndex];
		stats.nodesSearched++;

		//record node
		t.ab[0] = alpha;
		t.ab[1] = beta;
		t.pv = pv;
		t.splitHit = false;
		t.cutoff = false;
		t.moveCount = 0;
		
		assert alpha < beta;
		
		if(s.isForcedDraw()){
			t.score = 0;
			return t;
		} else if(depth <= 0){
			t.score = qsearch(player, 0, stackIndex, pv, s);
			t.cutoff = cutoffSearch.get(); //only need this because qsearch doesnt return tree stack
			return t;
		} else if(cutoffSearch.get()){
			t.cutoff = true;
			return t;
		}

		final MoveList ml = stack[stackIndex];
		final long[] pieceMasks = ml.pieceMasks; //piece moving
		final long[] moves = ml.moves; //moves available to piece (can be multiple)
		final int[] ranks = ml.ranks; //move ranking
		ml.killer[0] = 0;
		ml.killer[1] = 0;

		final long zkey = s.zkey();
		final TTEntry e = m.get(zkey);
		
		if(e != null){
			stats.hashHits++;
			if(e.depth >= depth){
				if(pv ? e.cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
						e.cutoffType == TTEntry.CUTOFF_TYPE_LOWER: e.cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
					
					if(stackIndex-1 >= 0 && e.score >= beta){
						attemptKillerStore(e.move, ml.skipNullMove, stack[stackIndex-1]);
					}
					
					//return e.score;
					t.score = e.score;
					return t;
				}
			}
		}
		
		ml.kingAttacked[player] = isChecked(player, s);
		ml.kingAttacked[1-player] = isChecked(1-player, s);

		//move generation
		ml.length = 0;
		genMoves(player, s, ml, m, false);
		final int length = ml.length;
		if(length == 0){ //no moves, draw
			t.score = 0;
			return t;
		}
		isort(pieceMasks, moves, ranks, length);
		
		
		int g = alpha;
		long bestMove = 0;
		final int initialBestScore = -99999;
		int bestScore = initialBestScore;
		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
		int moveCount = 0;
		
		final int drawCount = s.drawCount; //stored for error checking purposes
		
		boolean hasMove = ml.kingAttacked[player];
		final boolean inCheck = ml.kingAttacked[player];

		//final TreeStack next = this.t[stackIndex+1];
		for(int i = 0; i < length && !t.cutoff; i++){
			for(long movesTemp = moves[i]; movesTemp != 0 ; movesTemp &= movesTemp-1){
				if(moveCount++ == t.moveCount){
					t.moveCount++;
					long encoding = s.executeMove(player, pieceMasks[i], movesTemp&-movesTemp);
					this.e.processMove(encoding);
					boolean isDrawable = s.isDrawable(); //player can take a draw

					if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
						//king in check after move
						g = -88888+stackIndex+1;
					} else{
						hasMove = true;
						final TreeStack temp = descend(1-player, s, depth-1, -beta, -alpha, false, stackIndex+1);
						t.cutoff |= temp.cutoff;
						g = -temp.score;
					}
					s.undoMove();
					this.e.undoMove(encoding);
					assert zkey == s.zkey(); //keys should be unchanged after undo
					assert drawCount == s.drawCount;
					
					if(isDrawable && 0 > g){ //can take a draw instead of making the move
						g = 0;
						encoding = 0;
					} 
					
					if(g > bestScore){
						bestScore = g;
						t.score = bestScore;
						bestMove = encoding;
						if(g > alpha){
							alpha = g;
							cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
							if(alpha >= beta){
								if(!t.cutoff){
									fillEntry.fill(zkey, encoding, alpha, depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
									m.put(zkey, fillEntry);
								}
								t.score = g;
								return t;
							}
						}
					}
				}
			}
			
			if(!t.cutoff){
				//will have to subtract off hash and killer moves, etc, here
				t.moveCount = i;
			}
		}
		
		if(!hasMove){
			//no moves except king into death - draw
			bestMove = 0;
			bestScore = 0;
			cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
		}
		
		if(!t.cutoff){
			fillEntry.fill(zkey, bestMove, bestScore, depth, pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
			m.put(zkey, fillEntry);
		}
		t.score = bestScore;
		return t;
	}
	
	private int qsearch(final int player, final int depth,
			final int stackIndex, final boolean pv, final State4 s){
		stats.nodesSearched++;

		int alpha = ab[stackIndex][0];
		final int beta = ab[stackIndex][1];
		
		if(depth < -qply){
			stats.forcedQuietCutoffs++;
			return beta; //qply bottomed out, return bad value
		} else if(cutoffSearch.get()){
			return 0;
		}

		
		int w = 0;
		final long[] pieceMasks = stack[stackIndex].pieceMasks; //piece moving
		final long[] moves = stack[stackIndex].moves; //moves available to piece (can be multiple)
		final int[] ranks = stack[stackIndex].ranks; //move ranking

		final long zkey = s.zkey();
		final TTEntry e = m.get(zkey);
		if(e != null){
			stats.hashHits++;
			if(e.depth >= depth){
				if(pv ? e.cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
						e.cutoffType == TTEntry.CUTOFF_TYPE_LOWER: e.cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
					return e.score;
				}
			}
			if(e.move != 0){
				long encoding = e.move;
				pieceMasks[w] = 1L<<MoveEncoder.getPos1(encoding);
				moves[w] = 1L<<MoveEncoder.getPos2(encoding);
				ranks[w++] = tteMoveRank;
			}
		}
		
		MoveList ml = stack[stackIndex];

		int bestScore;
		ml.kingAttacked[player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
		if(ml.kingAttacked[player]){
			bestScore = -77777;
		} else{
			bestScore = this.e.eval(s, player);
			if(bestScore >= beta){ //standing pat
				return bestScore;
			} else if(bestScore > alpha && pv){
				alpha = bestScore;
			}
		}
		ml.kingAttacked[1-player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[1-player]), player, s);
		
		ml.length = w;
		genMoves(player, s, ml, m, true);
		final int length = ml.length;
		isort(pieceMasks, moves, ranks, length);

		
		int g = alpha;
		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
		long bestMove = 0;
		final int drawCount = s.drawCount; //stored for error checking purposes
		for(int i = 0; i < length; i++){
			for(long movesTemp = moves[i]; movesTemp != 0 ; movesTemp &= movesTemp-1){
				long encoding = s.executeMove(player, pieceMasks[i], movesTemp&-movesTemp);
				this.e.processMove(encoding);
				final boolean isDrawable = s.isDrawable();
				
				if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
					//king in check after move
					g = -77777;
				} else{
					ab[stackIndex+1][0] = -beta;
					ab[stackIndex+1][1] = -alpha;
					g = -qsearch(1-player, depth-1, stackIndex+1, pv, s);
				}
				s.undoMove();
				this.e.undoMove(encoding);
				
				if(isDrawable && 0 > g){// && -10*depth > g){ //can draw instead of making the move
					g = 0;
					encoding = 0;
				}
				
				assert zkey == s.zkey();
				assert drawCount == s.drawCount;
				
				if(g > bestScore){
					bestScore = g;
					bestMove = encoding;
					if(g > alpha){
						alpha = g;
						cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
						if(g >= beta){
							if(!cutoffSearch.get()){
								fillEntry.fill(zkey, encoding, g, depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
								m.put(zkey, fillEntry);
							}
							return g;
						}
					}
				}
			}
		}

		if(!cutoffSearch.get()){
			fillEntry.fill(zkey, bestMove, bestScore, depth, pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
			m.put(zkey, fillEntry);
		}
		return bestScore;
	}

	/**
	 * checks too see if a move is legal, assumming we do not start in check,
	 * moving does not yield self check, we are not castling, and if moving a pawn
	 * we have chosen a non take move that could be legal if no piece is
	 * blocking the target square
	 * 
	 * <p> used to check that killer moves are legal
	 * @param player
	 * @param piece
	 * @param move
	 * @param s
	 * @return
	 */
	private static boolean isPseudoLegal(final int player, final long encoding, State4 s){
		final long p = 1L<<MoveEncoder.getPos1(encoding);
		final long m = 1L<<MoveEncoder.getPos2(encoding);
		final long[] pieces = s.pieces;
		if((pieces[player] & p) != 0 && (pieces[player] & pieces[1-player] & m) == 0){
			final int type = s.mailbox[BitUtil.lsbIndex(p)];
			switch(type){
			case State4.PIECE_TYPE_BISHOP:
				final long tempBishopMoves = State4.getBishopMoves(player, pieces, p);
				return (m & tempBishopMoves) != 0;
			case State4.PIECE_TYPE_KNIGHT:
				final long tempKnightMoves = State4.getKnightMoves(player, pieces, p);
				return (m & tempKnightMoves) != 0;
			case State4.PIECE_TYPE_QUEEN:
				final long tempQueenMoves = State4.getQueenMoves(player, pieces, p);
				return (m & tempQueenMoves) != 0;
			case State4.PIECE_TYPE_ROOK:
				final long tempRookMoves = State4.getRookMoves(player, pieces, p);
				return (m & tempRookMoves) != 0;
			case State4.PIECE_TYPE_KING:
				final long tempKingMoves = State4.getKingMoves(player, pieces, p);
				return (m & tempKingMoves) != 0;
			case State4.PIECE_TYPE_PAWN:
				final long tempPawnMoves = State4.getPawnMoves2(player, pieces, s.pawns[player]);
				return (m & tempPawnMoves) != 0;
			}
		}
		return false;
	}
	
	/**
	 * attempts to store a move as a killer move
	 * @param move move to be stored
	 * @param skipNullMove current skip null move status
	 * @param prev previous move list
	 */
	private static void attemptKillerStore(final long move, final boolean skipNullMove, final MoveList prev){
		assert prev != null;
		if(move != 0 && !skipNullMove &&
				(move&0xFFF) != prev.killer[0] &&
				(move&0xFFF) != prev.killer[1] &&
				MoveEncoder.getTakenType(move) == State4.PIECE_TYPE_EMPTY &&
				MoveEncoder.isCastle(move) == 0 &&
				MoveEncoder.isEnPassanteTake(move) == 0 &&
				!MoveEncoder.isPawnPromoted(move)){
			if(prev.killer[0] != move){
				prev.killer[1] = prev.killer[0];
				prev.killer[0] = move & 0xFFF;
			} else{
				prev.killer[0] = prev.killer[1];
				prev.killer[1] = move & 0xFFF;
			}
		}
	}
	
	/** record moves as blocks*/
	private static void recordMoves(int player, int pieceMovingType, long pieceMask,
			long moves, MoveList ml, State4 s, Hash m, boolean quiesce, long retakeMask){
		final long piece = pieceMask&-pieceMask;
		if(piece != 0 && moves != 0){
			final long enemy = s.pieces[1-player];
			int w = ml.length;
			if((moves & enemy) != 0){
				
				//retakes provides very small gains
				final long retakes = moves & retakeMask;
				if(retakes != 0){
					ml.pieceMasks[w] = piece;
					ml.moves[w] = retakes;
					ml.ranks[w] = 2;
					w++;
				}
				
				final long upTakes = moves & enemy & ml.upTakes[pieceMovingType] & ~retakes;
				if(upTakes != 0){
					ml.pieceMasks[w] = piece;
					ml.moves[w] = upTakes;
					ml.ranks[w] = 3;
					w++;
				}
				
				final long takes = moves & enemy & ~ml.upTakes[pieceMovingType] & ~retakes;
				if(takes != 0){
					ml.pieceMasks[w] = piece;
					ml.moves[w] = takes;
					ml.ranks[w] = 4;
					w++;
				}
			}
			final long nonTake = moves & ~enemy;
			if(nonTake != 0 && !quiesce){
				ml.pieceMasks[w] = piece;
				ml.moves[w] = nonTake;
				ml.ranks[w] = 5;
				w++;
			}
			ml.length = w;
		}
	}
	
	private static void genMoves(final int player, final State4 s, final MoveList ml, final Hash m, final boolean quiece){
		ml.upTakes[State4.PIECE_TYPE_KING] = s.pieces[1-player];
		ml.upTakes[State4.PIECE_TYPE_QUEEN] = s.queens[1-player]|s.kings[1-player];
		ml.upTakes[State4.PIECE_TYPE_ROOK] = ml.upTakes[State4.PIECE_TYPE_QUEEN]|s.rooks[1-player];
		ml.upTakes[State4.PIECE_TYPE_KNIGHT] = ml.upTakes[State4.PIECE_TYPE_ROOK]|s.knights[1-player]|s.bishops[1-player];
		ml.upTakes[State4.PIECE_TYPE_BISHOP] = ml.upTakes[State4.PIECE_TYPE_KNIGHT];
		ml.upTakes[State4.PIECE_TYPE_PAWN] = s.pieces[1-player];
		
		long retakeMask = 0;
		/*if(MoveEncoder.getTakenType(ml.prevMove) != State4.PIECE_TYPE_EMPTY){
			retakeMask = 1L<<MoveEncoder.getPos2(ml.prevMove);
		}*/
		
		if(ml.kingAttacked[player]){
			long kingMoves = State4.getKingMoves(player, s.pieces, s.kings[player]);
			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves, ml, s, m, false, retakeMask);
		}
		
		long queens = s.queens[player];
		recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
				State4.getQueenMoves(player, s.pieces, queens), ml, s, m, quiece, retakeMask);
		queens &= queens-1;
		recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
				State4.getQueenMoves(player, s.pieces, queens), ml, s, m, quiece, retakeMask);
		queens &= queens-1;
		if(queens != 0){
			while(queens != 0){
				recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
						State4.getQueenMoves(player, s.pieces, queens), ml, s, m, quiece, retakeMask);
				queens &= queens-1;
			}
		}

		long rooks = s.rooks[player];
		recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
				State4.getRookMoves(player, s.pieces, rooks), ml, s, m, quiece, retakeMask);
		rooks &= rooks-1;
		recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
				State4.getRookMoves(player, s.pieces, rooks), ml, s, m, quiece, retakeMask);
		
		long knights = s.knights[player];
		recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
				State4.getKnightMoves(player, s.pieces, knights), ml, s, m, quiece, retakeMask);
		knights &= knights-1;
		recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
				State4.getKnightMoves(player, s.pieces, knights), ml, s, m, quiece, retakeMask);
		
		long bishops = s.bishops[player];
		recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
				State4.getBishopMoves(player, s.pieces, bishops), ml, s, m, quiece, retakeMask);
		bishops &= bishops-1;
		recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
				State4.getBishopMoves(player, s.pieces, bishops), ml, s, m, quiece, retakeMask);

		
		//handle pawn moves specially
		final long[] pawnMoves = ml.pawnMoves;
		final long pawns = s.pawns[player];
		pawnMoves[0] = State4.getRightPawnAttacks(player, s.pieces, s.enPassante, pawns);
		pawnMoves[1] = State4.getLeftPawnAttacks(player, s.pieces, s.enPassante, pawns);
		pawnMoves[2] = State4.getPawnMoves(player, s.pieces, pawns);
		pawnMoves[3] = State4.getPawnMoves2(player, s.pieces, pawns);
		
		int w = ml.length;
		final long[] pieces = ml.pieceMasks;
		final long[] moves = ml.moves;
		final int[] ranks = ml.ranks;

		//pawn movement promotions
		for(long tempPawnMoves = pawnMoves[2]&Masks.pawnPromotionMask[player]; tempPawnMoves != 0; tempPawnMoves&=tempPawnMoves-1){
			long moveMask = tempPawnMoves&-tempPawnMoves;
			long pawnMask = player == 0? moveMask>>>pawnOffset[2]: moveMask<<pawnOffset[2];
			pieces[w] = pawnMask;
			moves[w] = moveMask;
			ranks[w] = 2;
			w++;
		}
		//pawn takes
		for(int i = 0; i < 2; i++){
			for(long tempPawnMoves = pawnMoves[i]; tempPawnMoves != 0; tempPawnMoves&=tempPawnMoves-1){
				long moveMask = tempPawnMoves&-tempPawnMoves;
				long pawnMask = player == 0? moveMask>>>pawnOffset[i]: moveMask<<pawnOffset[i];
				pieces[w] = pawnMask;
				moves[w] = moveMask;
				ranks[w] = (moveMask & Masks.pawnPromotionMask[player]) == 0? 3: 1;
				w++;
			}
		}
		if(!quiece){
			//non-promoting pawn movement
			for(int i = 2; i < 4; i++){
				for(long tempPawnMoves = pawnMoves[i]&~Masks.pawnPromotionMask[player];
						tempPawnMoves != 0; tempPawnMoves&=tempPawnMoves-1){
					long moveMask = tempPawnMoves&-tempPawnMoves;
					long pawnMask = player == 0? moveMask>>>pawnOffset[i]: moveMask<<pawnOffset[i];
					pieces[w] = pawnMask;
					moves[w] = moveMask;
					ranks[w] = 5;
					w++;
				}
			}
		}
		ml.length = w;

		if(!ml.kingAttacked[player]){
			long kingMoves = State4.getKingMoves(player, s.pieces, s.kings[player])|State4.getCastleMoves(player, s);
			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves, ml, s, m, quiece, retakeMask);
		}
	}
	
	/**
	 * insertion sort (lowest rank first)
	 * @param pieceMasks entries to sort
	 * @param moves moves to sort
	 * @param rank rank of entries
	 * @param length number of entries to sort
	 */
	private static void isort(final long[] pieceMasks, final long[] moves, final int[] rank, final int length){
		for(int i = 1; i < length; i++){
			//for(int a = i; moves[i] != 0 && a > 0 && (rank[a-1]>rank[a] || moves[a-1] == 0); a--){
			for(int a = i; a > 0 && rank[a-1]>rank[a]; a--){
				long templ1 = pieceMasks[a];
				pieceMasks[a] = pieceMasks[a-1];
				pieceMasks[a-1] = templ1;
				long templ2 = moves[a];
				moves[a] = moves[a-1];
				moves[a-1] = templ2;
				
				int tempr = rank[a];
				rank[a] = rank[a-1];
				rank[a-1] = tempr;
			}
		}
	}
}
