package uci;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import state4.MoveEncoder;
import state4.State4;
import uci.UCIMove.MoveType;
import util.FenParser;

public final class UCI {
	private UCIEngine engine;
	private Position pos;
	
	private final Thread t = new Thread(){
		@Override
		public void run(){
			Scanner scanner = new Scanner(System.in);
			
			while(scanner.hasNextLine()){
				String interfaceCommand = scanner.nextLine();
				
				interfaceCommand = interfaceCommand.replace("\r", "");
				
				String[] s = interfaceCommand.split("\\s+");
				
				
				if(s[0].equalsIgnoreCase("uci")){
					send("id name "+engine.getName());
					send("id author Jack Crawford");
					send("uciok");
				} else if(s[0].equalsIgnoreCase("ucinewgame")){
					engine.resetEngine();
					pos = Position.startPos();
				} else if(s[0].equalsIgnoreCase("position")){
					if(s[1].equalsIgnoreCase("fen")){
						Pattern fenSel = Pattern.compile("fen ((.*?\\s+){5}.*?)(\\s+|$)");
						Matcher temp = fenSel.matcher(interfaceCommand);
						temp.find();
						pos = FenParser.parse(temp.group(1));
					} else if(s[1].equalsIgnoreCase("startpos")){
						pos = Position.startPos();
					}
					
					Pattern moveSel = Pattern.compile("moves\\s+(.*)");
					Matcher temp = moveSel.matcher(interfaceCommand);
					if(temp.find()){
						int turn = pos.sideToMove;
						String moves = temp.group(1);
						String[] ml = moves.split("\\s+");
						for(int a = 0; a < ml.length; a++){
							UCIMove m = parseMove(ml[a]);
							long encoding = 0;
							if(m.type == UCIMove.MoveType.Normal){
								encoding = pos.s.executeMove(turn, 1L<<m.move[0], 1L<<m.move[1]);
							} else if(m.type == UCIMove.MoveType.Null){
								pos.s.nullMove();
							}
							pos.s.resetHistory();
							turn = 1-turn;
							pos.fullMoves++;
							pos.halfMoves = MoveEncoder.getTakenType(encoding) == State4.PIECE_TYPE_EMPTY ||
									m.type == MoveType.Null? 0: pos.halfMoves+1;
						}
						pos.sideToMove = turn;
					}
				} else if(s[0].equalsIgnoreCase("isready")){
					send("readyok");
				} else if(s[0].equalsIgnoreCase("stop")){
					engine.stop();
				} else if(s[0].equalsIgnoreCase("go")){
					GoParams params = new GoParams(interfaceCommand);
					engine.go(params, pos);
				} else if(s[0].equalsIgnoreCase("quit")){
					break;
				}
			}
			scanner.close();
		}
	};
	
	private void send(String s){
		System.out.println(s);
		System.out.flush();
	}
	
	private static UCIMove parseMove(String move){
		UCIMove m = new UCIMove();
		if(move.equals("0000")){
			m.type = UCIMove.MoveType.Null;
			return m;
		}
		
		m.type = UCIMove.MoveType.Normal;
		move = move.toLowerCase();
		m.move[0] = move.charAt(0)-'a'+(move.charAt(1)-'1')*8;
		m.move[1] = move.charAt(2)-'a'+(move.charAt(3)-'1')*8;
		if(move.length() == 5){
			char promotion = move.charAt(4);
			if(promotion == 'q'){
				m.ptype = UCIMove.PromotionType.Queen;
			} else if(promotion == 'b'){
				m.ptype = UCIMove.PromotionType.Bishop;
			} else if(promotion == 'r'){
				m.ptype = UCIMove.PromotionType.Rook;
			} else if(promotion == 'n'){
				m.ptype = UCIMove.PromotionType.Knight;
			}
		}
		
		return m;
	}
	
	public UCI(final int size){
		engine = new RibozymeEngine(size);
		t.start();
	}
	
	public static void main(String[] args){
		int size = 20; //hash size, as a power of 2
		if(args.length == 2){
			boolean complete = false;
			if(args[0].equals("--hash")){
				try{
					size = Integer.parseInt(args[1]);
					complete = true;
				} catch(NumberFormatException e){}
			}
			if(!complete){
				System.out.println("incorrect args, needs: '--hash <size>'");
			}
		}
		new UCI(size);
	}
}
