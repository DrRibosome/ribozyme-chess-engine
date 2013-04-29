package util.genetic;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import search.Search4;
import search.search33.SearchS4V33t;
import state4.BitUtil;
import state4.State4;
import util.opening2.Book;
import eval.legacy.expEvalV3.E4;

/** plays queued games*/
public final class GameQueue {
	public final static class Game{
		public final static int draw = 2;
		final GEntity[] e;
		/** result record result of game, either r\in{0,1} if win/loss, r=2 if draw*/
		public int result;
		public Game(GEntity e1, GEntity e2){
			e = new GEntity[]{e1, e2};
		}
		/** gets which score the passed entity started as */
		public int getScore(GEntity ge){
			int index = ge == e[0]? 0: 1;
			return result == 2? 1: result == index? 2: 0;
		}
		/** gets the opponent*/
		public GEntity getOpponent(GEntity ge){
			if(ge == e[0]){
				return e[1];
			}
			return e[0];
		}
	}
	
	private final static class GauntletThread extends Thread{
		private final long time;
		private final int hashSize;
		private final Queue<Game> q;
		private final int[] move = new int[2];
		/** reference to the global count of outstanding jobs*/
		private final AtomicInteger outstanding;
		
		GauntletThread(long time, int hashSize, AtomicInteger outstanding, final Queue<Game> q){
			this.time = time;
			this.hashSize = hashSize;
			this.outstanding = outstanding;
			this.q = q;
			
			setDaemon(true);
		}
		
		public void run(){
			for(;;){
				
				while(q.size() > 0){
					final Game g = q.poll();
					
					if(g != null){
						final Search4[] search = new Search4[]{
								new SearchS4V33t(new E4(g.e[0].p), hashSize, false),
								new SearchS4V33t(new E4(g.e[1].p), hashSize, false),
						};
						
						final State4 state = new State4(b.getSeed(), 40);
						state.initialize();
						int turn = 0;
						boolean draw = false;
						boolean outOfBook = false;
						final long[] time = new long[]{this.time, this.time};
						boolean error = false;

						while(state.pieceCounts[turn][State4.PIECE_TYPE_KING] != 0 &&
								!isMate(turn, state) && !state.isForcedDraw() && time[turn] > 0){

							int[] bookMove = b.getRandomMove(turn, state, 100);
							if(bookMove != null && !outOfBook){
								System.arraycopy(bookMove, 0, move, 0, 2);
								//if(print) System.out.println("book move");
							} else{
								outOfBook = true;
								//search(turn, state, search[turn], move, time);
								search[turn].search(turn, state, move, 5);
								/*final long start = System.currentTimeMillis();
								TimerThread4.searchBlocking(search[turn], state, turn, time[turn], 0, move);
								time[turn] -= System.currentTimeMillis()-start;*/
							}
							
							if(move[0] == move[1]){ //draw, no moves remaining
								if(time[turn] > 0){
									draw = true;
								}
								break;
							} else{
								try{
									state.executeMove(turn, 1L<<move[0], 1L<<move[1]);
									turn = 1-turn;
								} catch(Exception e){
									e.printStackTrace();
									System.out.println("game error, requeing...");
									q.add(g);
									error = true;
									break;
								}
							}
						}

						if(!error){
							if(state.isForcedDraw() || draw){
								//if(print) System.out.println("draw");
								g.e[0].draws.incrementAndGet();
								g.e[1].draws.incrementAndGet();
								g.result = Game.draw;
							} else{
								final int winner = 1-turn;
								g.e[winner].wins.incrementAndGet();
								g.e[1-winner].losses.incrementAndGet();
								g.result = winner;
								//if(print) System.out.println("player "+winner+" wins");
							}
							outstanding.decrementAndGet();
						}
					}
				}
				
				try{
					Thread.sleep(500);
				} catch(InterruptedException e){}
			}
		}
	}
	
	private static void search(final int player, final State4 s, final Search4 search, final int[] moveStore, final long targetTime){
		final long start = System.currentTimeMillis();
		
		final Thread t = new Thread(){
			public void run(){
				search.search(player, s, moveStore, 99);
			}
		};
		t.setDaemon(true);
		t.start();
		
		while(System.currentTimeMillis()-start < targetTime){
			final long diff = targetTime-(System.currentTimeMillis()-start);
			if(diff/2 >= 1){
				try{
					Thread.sleep(diff/2);
				} catch(InterruptedException e){}
			}
		}
		
		while(t.isAlive()){
			search.cutoffSearch();
			try{
				t.join(500);
			} catch(InterruptedException e){}
		}
	}

	private final static int[] pawnOffset = new int[]{9,7,8,16};
	/** checks that the player to move is not mated using their king*/
	private static boolean isMate(int turn, State4 s){
		if(!State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s)){
			return false;
		}
		final long kings = s.kings[turn];
		for(long m = State4.getKingMoves(turn, s.pieces, s.kings[turn]); m != 0; m &= m-1){
			s.executeMove(turn, kings, m&-m);
			boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
			s.undoMove();
			if(!attacked) return false;
		}
		for(long queens = s.queens[turn]; queens != 0; queens &= queens-1){
			final long queen = queens&-queens;
			for(long m = State4.getQueenMoves(turn, s.pieces, queen); m != 0; m &= m-1){
				s.executeMove(turn, queen, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		for(long knights = s.knights[turn]; knights != 0; knights &= knights-1){
			final long knight = knights&-knights;
			for(long m = State4.getKnightMoves(turn, s.pieces, knight); m != 0; m &= m-1){
				s.executeMove(turn, knight, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		for(long bishops = s.knights[turn]; bishops != 0; bishops &= bishops-1){
			final long bishop = bishops&-bishops;
			for(long m = State4.getBishopMoves(turn, s.pieces, bishop); m != 0; m &= m-1){
				s.executeMove(turn, bishop, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		for(long rooks = s.knights[turn]; rooks != 0; rooks &= rooks-1){
			final long rook = rooks&-rooks;
			for(long m = State4.getRookMoves(turn, s.pieces, rook); m != 0; m &= m-1){
				s.executeMove(turn, rook, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		final long[] temp = new long[4];
		temp[0] = State4.getRightPawnAttacks(turn, s.pieces, s.enPassante, s.pawns[turn]);
		temp[1] = State4.getLeftPawnAttacks(turn, s.pieces, s.enPassante, s.pawns[turn]);
		temp[2] = State4.getPawnMoves(turn, s.pieces, s.pawns[turn]);
		temp[3] = State4.getPawnMoves2(turn, s.pieces, s.pawns[turn]);
		for(int a = 0; a < 4; a++){
			for(long pawnsMoves = temp[a]; pawnsMoves != 0; pawnsMoves &= pawnsMoves-1){
				final long move = pawnsMoves&-pawnsMoves;
				final long pawn = turn == 0? move>>>pawnOffset[a]: move<<pawnOffset[a];
				s.executeMove(turn, pawn, move&-move);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		return true;
	}
	

	final static Book b = new Book(new File("megabook.bk"));
	
	private final GauntletThread[] t;
	/** gives a count of current outstanding jobs*/
	private final AtomicInteger outstanding = new AtomicInteger();
	private final Queue<Game> q = new LinkedBlockingQueue<Game>();
	
	public GameQueue(int threads, long time, int hashSize){
		
		t = new GauntletThread[threads];
		for(int a = 0; a < t.length; a++){
			t[a] = new GauntletThread(time, hashSize, outstanding, q);
			t[a].start();
		}
	}
	
	public void submit(Game g){
		outstanding.incrementAndGet();
		q.add(g);
		for(int a = 0; a < t.length; a++) t[a].interrupt();
	}
	
	public int getOutstandingJobs(){
		return outstanding.get();
	}
}
