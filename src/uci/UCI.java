package uci;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.FenParser;

public final class UCI {
	UCIEngine engine = new RibozymeEngine();
	FileWriter out;
	
	private final Thread t = new Thread(){
		@Override
		public void run(){
			Scanner scanner = new Scanner(System.in);
			
			while(scanner.hasNextLine()){
				String interfaceCommand = scanner.nextLine();
				try{
					out.write(interfaceCommand + "\n");
				} catch (IOException e){
					e.printStackTrace();
				}
				
				interfaceCommand = interfaceCommand.replace("\r", "");
				
				debug("parsed command: ");
				String[] s = interfaceCommand.split("\\s+");
				for(int i = 0; i < s.length; i++){
					debug("'"+s[i]+"' ");
				}
				debug("\n");
				
				
				if(s[0].equalsIgnoreCase("uci")){
					debug("processing 'uci' command\n");
					System.out.println("id name ribozyme 1.0");
					System.out.println("id author ribozyme team");
					System.out.println("uciok");
				} else if(s[0].equalsIgnoreCase("ucinewgame")){
					
				} else if(s[0].equalsIgnoreCase("position")){
					Position p = null;
					if(s[1].equalsIgnoreCase("fen")){
						//Pattern fenSel = Pattern.compile("<(.*?)>");
						Pattern fenSel = Pattern.compile("fen ((.*?\\s+){5}.*?)(\\s+|$)");
						Matcher temp = fenSel.matcher(interfaceCommand);
						temp.find();
						p = FenParser.parse(temp.group(1));
					} else if(s[1].equalsIgnoreCase("startpos")){
						p = Position.startPos();
					}
					
					Pattern moveSel = Pattern.compile("moves\\s+(.*)");
					Matcher temp = moveSel.matcher(interfaceCommand);
					if(temp.find()){
						int turn = p.sideToMove;
						String moves = temp.group(1);
						String[] ml = moves.split("\\s+");
						for(int a = 0; a < ml.length; a++){
							Move m = parseMove(ml[a]);
							if(m.type == Move.MoveType.Normal){
								p.s.executeMove(turn, 1L<<m.move[0], 1L<<m.move[1]);
							} else if(m.type == Move.MoveType.Null){
								p.s.nullMove();
							}
							turn = 1-turn;
							p.fullMoves++;
							p.halfMoves++; //not correct, should only update on non-takes
						}
						p.sideToMove = turn;
					}
					engine.setPos(p);
				} else if(s[0].equalsIgnoreCase("isready")){
					System.out.println("readyok");
				} else if(s[0].equalsIgnoreCase("stop")){
					engine.stop();
				} else if(s[0].equalsIgnoreCase("go")){
					GoParams p = new GoParams();
					p.ponder = interfaceCommand.contains("ponder");
					p.infinite = interfaceCommand.contains("infinite");
					
					//hell borked
					/*Pattern whiteTimeSel = Pattern.compile("wtime\\s+(\\d+)");
					Matcher temp = whiteTimeSel.matcher(interfaceCommand);
					p.whiteTime = temp.find()? Integer.parseInt(temp.group(1)): -1;
					
					Pattern blackTimeSel = Pattern.compile("btime\\s+\\d+");
					temp = blackTimeSel.matcher(interfaceCommand); temp.
					p.blackTime = temp.find()? Integer.parseInt(temp.group(1)): -1;
					
					Pattern whiteTimeIncSel = Pattern.compile("winc\\s+(\\d+)");
					temp = whiteTimeIncSel.matcher(interfaceCommand);
					p.whiteTimeInc = temp.find()? Integer.parseInt(temp.group(1)): -1;
					
					Pattern blackTimeIncSel = Pattern.compile("binc\\s+(\\d+)");
					temp = blackTimeIncSel.matcher(interfaceCommand);
					p.blackTimeInc = temp.find()? Integer.parseInt(temp.group(1)): -1;
					
					Pattern depthSel = Pattern.compile("depth\\s+(\\d+)");
					temp = depthSel.matcher(interfaceCommand);
					p.depth = temp.find()? Integer.parseInt(temp.group(1)): -1;
					*/
					Pattern moveTimeSel = Pattern.compile("movetime\\s+(\\d+)");
					Matcher temp = moveTimeSel.matcher(interfaceCommand);
					p.moveTime = temp.find()? Integer.parseInt(temp.group(1)): -1;
					
					engine.go(p);
				}
			}
			scanner.close();
			try{
				out.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	};
	
	private void debug(String s){
		if(out != null){
			try{
				out.write(s);
			} catch(IOException e){}
		}
	}
	
	private static Move parseMove(String move){
		Move m = new Move();
		if(move.equals("0000")){
			m.type = Move.MoveType.Null;
			return m;
		}
		
		m.type = Move.MoveType.Normal;
		move = move.toLowerCase();
		m.move[0] = move.charAt(0)-'a'+(move.charAt(1)-'1')*8;
		m.move[1] = move.charAt(2)-'a'+(move.charAt(3)-'1')*8;
		if(move.length() == 5){
			char promotion = move.charAt(4);
			if(promotion == 'q'){
				m.ptype = Move.PromotionType.Queen;
			} else if(promotion == 'b'){
				m.ptype = Move.PromotionType.Bishop;
			} else if(promotion == 'r'){
				m.ptype = Move.PromotionType.Rook;
			} else if(promotion == 'n'){
				m.ptype = Move.PromotionType.Knight;
			}
		}
		
		return m;
	}
	
	public UCI(){
		try{
			out = new FileWriter("uci-log.txt", true);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			out.write("========= "+dateFormat.format(cal.getTime())+" =========\n");
		} catch (IOException e){
			e.printStackTrace();
		}
		//System.setProperty("line.separator", "\r\n");
		t.start();
	}
	
	public static void main(String[] args){
		new UCI();
	}
}
