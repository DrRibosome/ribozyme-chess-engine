package util.pgnLoader;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ParallelPgnProcessor implements PgnProcessor{
	private final ThreadPoolExecutor e;
	private final PgnProcessor processor;
	
	private final class GameProc implements Runnable{
		private final PgnGame g;
		GameProc(PgnGame g){
			this.g = g;
		}
		@Override
		public void run() {
			//System.out.println("thread = "+Thread.currentThread());
			processor.process(g);
		}
		
	}
	
	public ParallelPgnProcessor(PgnProcessor processor){
		this.processor = processor;
		final int processors = Runtime.getRuntime().availableProcessors();
		this.e = new ThreadPoolExecutor(processors, processors, 0,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	@Override
	public void process(PgnGame g) {
		e.execute(new GameProc(g));
	}
	
	public void shutdown(){
		e.shutdown();
		boolean complete = false;
		while(!complete){
			try {
				complete = e.awaitTermination(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
