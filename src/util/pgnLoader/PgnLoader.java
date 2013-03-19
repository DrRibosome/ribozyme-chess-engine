package util.pgnLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PgnLoader {
	private final static Pattern gameSel = Pattern.compile("\\[.*?\\].*?(?:^\\s*$)(.+?)(?:^\\s*$)", Pattern.MULTILINE | Pattern.DOTALL);
	private final static Pattern moveSel = Pattern.compile("\\d+\\.[ ]?([\\w+=]+)\\s+([\\w+=]+)?\\s+", Pattern.DOTALL);
	private final static Pattern outcomeSel = Pattern.compile("((?:1/2-1/2)|(?:1-0)|(?:0-1))$");
	
	/**
	 * give a file with png games, process all games in the file online
	 * @param f
	 * @param p processor to offload game processing to
	 * @return returns the number of bad entries that cound not be loaded
	 * @throws IOException
	 */
	public static int load(File f, PgnProcessor p) throws IOException{
		int badEntries = 0;
		FileInputStream fis = new FileInputStream(f);
		byte[] buff = new byte[1<<10];
		int last;
		String leftover = "";
		while(fis.read(buff) != -1){
			//System.out.println("reading chunk");
			final String s = leftover+new String(buff);
			final Matcher m = gameSel.matcher(s);
			last = 0;
			while(m.find()){
				//System.out.println(m.group());
				PgnGame g = processMoves(m.group(1));
				if(g != null){
					p.process(g);
				} else{
					badEntries++;
				}
				//System.out.println("complete");
				last = m.end();
			}
			leftover = s.substring(last);
			//System.out.println("leftover = "+leftover);
		}
		fis.close();
		return badEntries;
	}
	
	/** process a (move,outcome) block*/
	public static PgnGame processMoves(String moves){
		//System.out.println("processing moves...");
		Matcher moveMatcher = moveSel.matcher(moves);
		List<String> l = new ArrayList<String>();
		while(moveMatcher.find()){
			l.add(moveMatcher.group(1));
			if(moveMatcher.group(2) != null){
				l.add(moveMatcher.group(2));
			}
		}
		//System.out.println(l);
		
		Matcher outcomeMatcher = outcomeSel.matcher(moves);
		if(outcomeMatcher.find()){
			String outcome = outcomeMatcher.group(1);
			return new PgnGame(l, outcome);
		} else{
			//System.out.println("failing");
			return null;
		}
	}
}
