package util.genetic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** cli tool for viewing and perusing genetic logs*/
public final class GLogCLI {
	private static abstract class Command{
		private final Pattern p;
		Command(String regex){
			p = Pattern.compile(regex);
		}
		/** pattern for matching command, run against a read line*/
		public Pattern getPattern(){return p;}
		/** executes the command, only called if {@link #getPattern()}
		 * has matched on input string*/
		public abstract void execute(Matcher m);
	}
	
	private final static List<Command> c = new ArrayList<Command>();
	private static GeneticLogger.GLog log;
	
	static{
		c.add(new Command("\\s*iterations") { //load a new log file
			public void execute(Matcher m) {
				System.out.println("num iterations = "+log.iterationResults.size());
			}
		});
		c.add(new Command("\\s*ids") { //load a new log file
			public void execute(Matcher m) {
				System.out.println("num ids = "+log.params.size());
			}
		});
		c.add(new Command("\\s*view\\s+(\\d+)") { //load a new log file
			public void execute(Matcher m) {
				final int iteration = Integer.parseInt(m.group(1));
				if(log.iterationResults.containsKey(iteration)){
					final List<GEntity> results = log.iterationResults.get(iteration).results;
					final Comparator<GEntity> sortWorstFirst = new Comparator<GEntity>() {
						public int compare(GEntity e1, GEntity e2) {
							if(e1.score() < e2.score()){
								return -1;
							} else if(e1.score() > e2.score()){
								return 1;
							} else{
								return e1.totalGames() < e2.totalGames()? -1: 1;
							}
						}
					};
					Collections.sort(results, sortWorstFirst);
					System.out.println("rank\t(w,l,d)");
					System.out.println("------------------------------------------------");
					for(int a = 0; a < results.size(); a++){
						GEntity r = results.get(a);
						System.out.println((results.size()-a)+"\t"+r);
					}
				} else{
					System.out.println("no iteration '"+iteration+"', max iteration = "+(log.iterationResults.size()-1));
				}
			}
		});
		c.add(new Command("\\s*inspect\\s+(\\d+)") { //load a new log file
			public void execute(Matcher m) {
				final int id = Integer.parseInt(m.group(1));
				if(log.params.containsKey(id)){
					System.out.println(log.params.get(id));
				} else{
					System.out.println("no id '"+id+"', max id = "+(log.params.size()-1));
				}
			}
		});
	}
	
	public static void main(String[] args) throws IOException{
		final Scanner scanner = new Scanner(System.in);
		/*if(args.length != 1){
			while(log == null){
				File logF = new File(scanner.nextLine().trim());
				log = GeneticLogger.loadLog(logF);
			}
		}*/
		log = GeneticLogger.loadLog(new File("genetic-results/genetic-results-mac-5"));
		
		while(scanner.hasNext()){
			String line = scanner.nextLine();
			for(int a = 0; a < c.size(); a++){
				final Command temp = c.get(a);
				final Matcher m = temp.getPattern().matcher(line);
				if(m.find()){
					temp.execute(m);
					break;
				}
			}
		}
		scanner.close();
	}
}
