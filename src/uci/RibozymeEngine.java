package uci;

import search.Search3;
import search.SearchS4V30;
import util.board4.State4;
import eval.Evaluator2;
import eval.SuperEvalS4V8;

public class RibozymeEngine implements UCIEngine{

	private Search3 s;
	private Thread t;
	private final int[] moveStore = new int[2];
	private Position p;
	
	@Override
	public void go(final GoParams params) {
		final int player = p.sideToMove;
		t = new Thread(){
			public void run(){
				s.search(player, moveStore, params.depth);
				String move = posString(moveStore[0])+posString(moveStore[1]);
				System.out.println("bestmove "+move);
			}
		};
		t.setDaemon(true);
		t.start();
		
		Thread stopThread = new Thread(){
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
		stopThread.start();
		
	}
	
	private static String posString(int pos){
		return ""+(char)('a'+pos%8)+(char)('1'+pos/8);
	}

	@Override
	public void stop() {
		if(t != null){
			s.cutoffSearch();
			while(t.isAlive()){
				try{
					t.join();
				} catch(InterruptedException e){}
			}
		}
	}

	@Override
	public void setPos(Position p) {
		this.p = p;
		
		Evaluator2<State4> e = new SuperEvalS4V8();
		s = new SearchS4V30(p.s, e, 20, false);
		
	}

}
