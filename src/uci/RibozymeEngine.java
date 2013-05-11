package uci;

import search.Search4;
import search.search33.Search33v4;
import time.TimerThread4;
import time.TimerThread5;
import eval.Evaluator2;
import eval.e7.E7v6;

public final class RibozymeEngine implements UCIEngine{

	private final static String name = "ribozyme .3";
	
	private final Search4 s;
	private Thread t;
	private final int[] moveStore = new int[2];
	private Position p;
	private TimerThread4.Controller c;
	
	public RibozymeEngine(){
		
		Evaluator2 e = 
				//new SuperEvalS4V10v4();
				//new E4(GParams1v2.buildEval());
				//new E5v2(E5Params3.buildEval());
				new E7v6();
		
		s = new Search33v4(e, 22, true);
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void go(final GoParams params, final Position p) {
		final int player = p.sideToMove;
		if(!params.infinite && params.moveTime == -1){ //allocate time
			t = new Thread(){
				public void run(){
					final int inc = params.increment[player];
					TimerThread5.searchBlocking(s, p.s, player, params.time[player], inc, moveStore);
					String promotion = (p.s.pawns[player] & 1L<<moveStore[0]) != 0 && (moveStore[1]/8==7 || moveStore[1]/8==0)? "q": "";
					String move = posString(moveStore[0])+posString(moveStore[1]);
					System.out.println("bestmove "+move+promotion);
				}
			};
			t.setDaemon(true);
			t.start();
		} else if(!params.infinite && params.moveTime != -1){ //fixed time per move
			assert false;
			t = new Thread(){
				public void run(){
					s.search(player, p.s, moveStore);
					
					String promotion = (p.s.pawns[player] & 1L<<moveStore[0]) != 0 && (moveStore[1]/8==7 || moveStore[1]/8==0)? "q": "";
					String move = posString(moveStore[0])+posString(moveStore[1]);
					System.out.println("bestmove "+move+promotion);
				}
			};
			t.setDaemon(true);
			t.start();
			final Thread timer = new Thread(){
				public void run(){
					final long start = System.currentTimeMillis();
					final long targetTime = params.time[player];
					long time;
					while((time = System.currentTimeMillis()-start) < targetTime){
						try{
							Thread.sleep(time/2);
						} catch(InterruptedException e){}
					}
					s.cutoffSearch();
				}
			};
			timer.setDaemon(true);
			timer.start();
		} else if(params.infinite){
			assert false;
			t = new Thread(){
				public void run(){
					s.search(player, p.s, moveStore);
					String promotion = (p.s.pawns[player] & 1L<<moveStore[0]) != 0 && (moveStore[1]/8==7 || moveStore[1]/8==0)? "q": "";
					String move = posString(moveStore[0])+posString(moveStore[1]);
					System.out.println("bestmove "+move+promotion);
				}
			};
			t.setDaemon(true);
			t.start();
		}
	}
	
	private static String posString(int pos){
		return ""+(char)('a'+pos%8)+(char)('1'+pos/8);
	}

	@Override
	public void stop() {
		s.cutoffSearch();
		final Thread t = this.t;
		if(t != null){
			while(t.isAlive()){
				try{
					t.join();
				} catch(InterruptedException e){}
			}
		}
	}

	@Override
	public void resetEngine() {
		stop();
		s.resetSearch();
	}

}
