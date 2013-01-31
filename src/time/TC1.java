package time;

import search.Search3;
import util.board4.State4;

public final class TC1{
	TimerThread timer = new TimerThread();
	
	public TC1(){
		
	}
	
	public void search(Search3 search, State4 s, int player, long time, long inc, long[] moveStore){
		timer.search(search, s, player, time, inc, moveStore);
	}
}
