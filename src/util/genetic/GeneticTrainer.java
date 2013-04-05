package util.genetic;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import eval.expEvalV3.DefaultEvalWeights;
import eval.expEvalV3.EvalParameters;

public final class GeneticTrainer {
	public static class Entity{
		EvalParameters p;
		final AtomicInteger wins = new AtomicInteger();
		final AtomicInteger losses = new AtomicInteger();
		final AtomicInteger draws = new AtomicInteger();
		public double score(){
			final double score = wins.get()+draws.get()/2;
			return score/totalGames();
		}
		public int totalGames(){
			return wins.get() + losses.get() + draws.get();
		}
		public String toString(){
			return "(w,l,d)=("+wins.get()+","+losses.get()+","+draws.get()+"), "+super.toString();
		}
	}
	
	private final static ByteBuffer b = ByteBuffer.allocate(1<<15);
	
	public static void main(String[] args) throws Exception{

		final int tests = 2;
		final int threads = 3;
		final int popSize = 3;
		final int cullSize = min((int)(popSize*.05), 1); //number of entries to cull
		final int minGames = 10; //min games before entry can be culled
		final Mutator m = new MutatorV1();
		final int mutations = 4;
		
		@SuppressWarnings("resource")
		final FileChannel f = new FileOutputStream("genetic-results-v1").getChannel();
		
		final Entity[] population = new Entity[popSize];
		final GameQueue q = new GameQueue(threads);
		
		for(int a = 0; a < population.length; a++){
			population[a] = new Entity();
			population[a].p = DefaultEvalWeights.defaultEval();
		}
		
		for(int i = 0; ; i++){
			simulate(population, q, tests);
			List<Integer> culled = cull(population, cullSize, minGames);
			generate(culled, population, m, mutations);

			System.out.println("completed iteration "+i);
			
			//record data
			int max = -1;
			double maxScore = -1;
			for(int a = 0; a < population.length; a++){
				if(population[a].totalGames() >= minGames && population[a].score() > maxScore){
					max = a;
					maxScore = population[a].score();
				}
			}
			if(max != -1){
				System.out.println("recording...");
				b.clear();
				b.putInt(i); //put iteration count
				population[max].p.write(b);
				b.limit(b.position());
				b.rewind();
				f.write(b);
			}
		}
	}
	
	public static int min(final int i1, final int i2){
		return i1 < i2? i1: i2;
	}
	
	/** runs the simulation, accumulating a score for each entity*/
	public static void simulate(final Entity[] population, final GameQueue q, final int tests){
		for(int a = 0; a < population.length; a++){
			
			for(int w = 0; w < tests; w++){
				int index;
				while((index = (int)(Math.random()*population.length)) == a);
				
				final GameQueue.Game g = new GameQueue.Game(population[a], population[index]);
				q.submit(g);
			}
		}
		
		final int total = tests*population.length;
		while(q.getOutstandingJobs() > 0){
			try{
				Thread.sleep(3000);
			} catch(InterruptedException e){}
			System.out.println("completed "+(total-q.getOutstandingJobs())+" / "+total);
		}
		System.out.println("games complete!");
	}
	
	/** culls bad solutions from population, returns list of culled indeces*/
	public static List<Integer> cull(final Entity[] population, final int cullSize, final int minGames){
		final Comparator<Entity> c = new Comparator<GeneticTrainer.Entity>() {
			public int compare(Entity e1, Entity e2) {
				return e1.score() < e2.score()? -1: 1;
			}
		};
		final Queue<Entity> q = new PriorityQueue<Entity>(population.length, c);
		for(int a = 0; a < population.length; a++) q.add(population[a]);
		
		final boolean print = true;
		
		List<Integer> l = new ArrayList<Integer>();
		while(q.size() > 0 && l.size() < cullSize){
			Entity e = q.poll();
			if(print) System.out.print(e);
			if(e.totalGames() >= minGames){
				if(print) System.out.println(" -- culled");
				for(int w = 0; w < population.length; w++){
					if(population[w] == e){
						population[w] = null;
						l.add(w);
					}
				}
			} else{
				if(print) System.out.println();
			}
		}
		
		while(q.size() > 0){
			if(print) System.out.println(q.poll());
		}
		
		return l;
	}
	
	/** generates new solutions from population , replacing culled entries*/
	public static void generate(final List<Integer> culled, final Entity[] population, final Mutator m, final int mutations){
		for(int index: culled){
			int r; //index of entity to clone and mutate
			while(population[r = (int)(Math.random()*population.length)] == null);
			
			population[r].p.write(b);
			b.rewind();
			final EvalParameters p = new EvalParameters();
			p.read(b);
			m.mutate(p, mutations);
			Entity temp = new Entity();
			temp.p = p;
			
			population[index] = temp;
		}
	}
}
