package util.png;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PngLoader {
	private final static Pattern gameSel = Pattern.compile("\\[.*?\\].*?(?:^\\s*$)(.+?)(?:^\\s*$)", Pattern.MULTILINE | Pattern.DOTALL);
	private final static Pattern moveSel = Pattern.compile("\\d+\\.([a-zA-Z].+?)\\s+(?:([a-zA-Z].+?)\\s+)?", Pattern.DOTALL);
	private final static Pattern outcomeSel = Pattern.compile("((?:1/2-1/2)|(?:1-0)|(?:0-1))$");
	
	/**
	 * give a file with png games, process all games in the file online
	 * @param f
	 * @param p processor to offload game processing to
	 * @return
	 * @throws IOException
	 */
	public static void load(File f, PngProcessor p) throws IOException{
		FileInputStream fis = new FileInputStream(f);
		byte[] buff = new byte[16384];
		int last;
		String leftover = "";
		while(fis.read(buff) != -1){
			String s = leftover+new String(buff);
			Matcher m = gameSel.matcher(s);
			last = 0;
			while(m.find()){
				PngGame g = processMoves(m.group(1));
				p.process(g);
				last = m.end();
			}
			leftover = s.substring(last);
			//System.out.print(s);
		}
		fis.close();
	}
	
	/** process a (move,outcome) block*/
	public static PngGame processMoves(String moves){
		System.out.println("moves:"+moves);
		Matcher m = moveSel.matcher(moves);
		List<String> l = new ArrayList<String>();
		while(m.find()){
			l.add(m.group(1));
			if(m.group(2) != null){
				l.add(m.group(2));
			}
		}
		System.out.println(l);
		m = outcomeSel.matcher(moves);
		m.find();
		String outcome = m.group(1);
		System.out.println(outcome);
		
		PngGame g = new PngGame();
		g.moves = l;
		g.outcome = outcome;
		return g;
	}
}
