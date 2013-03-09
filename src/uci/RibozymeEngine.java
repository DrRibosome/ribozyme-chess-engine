package uci;

import search.Search3;
import search.search32k.SearchS4V32k;
import state4.State4;
import time.TimerThread3;
import eval.Evaluator2;
import eval.evalV8.SuperEvalS4V8;

public class RibozymeEngine implements UCIEngine{

	private Search3 s;
	private Thread t;
	private final int[] moveStore = new int[2];
	private Position p;
	private TimerThread3.Controller c;
	
	@Override
	public void go(final GoParams params) {
		final int player = p.sideToMove;
		if(1==1||!params.infinite && params.moveTime != -1){
			t = new Thread(){
				public void run(){
					final int inc = 0;
					TimerThread3.searchBlocking(s, p.s, player, params.time[player], inc, moveStore);
					//s.search(player, moveStore, params.depth);
					String move = posString(moveStore[0])+posString(moveStore[1]);
					System.out.println("bestmove "+move);
				}
			};
			t.setDaemon(true);
			t.start();
		}
		
		/*Thread stopThread = new Thread(){
			public void run(){
				if(!params.infinite && params.moveTime != -1){ //time stop
					long start = System.currentTimeMillis();
					while(t.isAlive() && System.currentTimeMillis()-start < params.moveTime){
						try{
							Thread.sleep(30);
						} catch(InterruptedException e){}
					}
					RibozymeEngine.this.stop();
				}
			}
		};
		stopThread.setDaemon(true);
		stopThread.start();*/
		
		/*Thread runner = new Thread(){
			public void run(){
				final int inc = 0;
				this.c = TimerThread3.searchNonBlocking(s, p.s, player, params.time[player], inc, moveStore);
				String move = posString(moveStore[0])+posString(moveStore[1]);
			}
		};
		runner.setDaemon(true);
		runner.start();*/

		/*String move = posString(moveStore[0])+posString(moveStore[1]);
		System.out.println("bestmove "+move);*/
	}
	
	private static String posString(int pos){
		return ""+(char)('a'+pos%8)+(char)('1'+pos/8);
	}

	@Override
	public void stop() {
		s.cutoffSearch();
		/*if(c != null){
			c.stopSearch();
		}*/
	}

	@Override
	public void setPos(Position p) {
		this.p = p;
		
		Evaluator2<State4> e = new SuperEvalS4V8();
		s = new SearchS4V32k(p.s, e, 21, false);
		
	}

}
