package chess.uci;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chess.state4.MoveEncoder;
import chess.state4.State4;
import chess.uci.UCIMove.MoveType;
import chess.uci.controlExtension.ControlExtension;
import chess.uci.controlExtension.EvalPositionExt;
import chess.uci.controlExtension.PrintPositionExt;
import chess.util.FenParser;

public final class UCI {
	private final UCIEngine engine;
	private Position pos;
	private final static Pattern fenSel = Pattern.compile("fen ((.*?\\s+){5}.*?)(\\s+|$)");
	private final static Pattern moveSel = Pattern.compile("moves\\s+(.*)");
	private final static Map<String, ControlExtension> controllerExtMap = new HashMap<>();

	/** if true ignores uci quit command*/
	private final boolean ignoreQuit;
	/** controls whether controlled extensionmodules should be allowed
	 * execute. Should be turned off for better uci controller performance*/
	private final boolean controllerExtras;

	/** convenience class for easily expanding uci param list while
	 * providing defaults values*/
	private final static class UCIParams{
		int hashSize = 20; //hash size, as a power of 2
		int pawnHashSize = 16;
		boolean printInfo = true;
		boolean ignoreQuit = false;
		boolean warmUp = false;
		boolean controllerExtras = false;

		/** controls whether fen used for speed profiling*/
		boolean profile = false;
		/** fen to use for profiling*/
		String profileFen;
	}

	static{
		controllerExtMap.put("print", new PrintPositionExt());
		controllerExtMap.put("eval", new EvalPositionExt());
	}

	public UCI(UCIParams p){

		this.ignoreQuit = p.ignoreQuit;
		this.controllerExtras = p.controllerExtras;

		if(!p.profile){
			//prepare and start engine for normal operation
			engine = new RibozymeEngine(p.hashSize, p.pawnHashSize, p.printInfo, p.warmUp);
			t.start();
		} else{
			//profile engine on passed fen file
			engine = new RibozymeEngine(p.hashSize, p.pawnHashSize, false, p.warmUp);
			engine.profile(new File(p.profileFen));
		}
	}
	
	private final Thread t = new Thread(){
		@Override
		public void run(){
			Scanner scanner = new Scanner(System.in);

			while(scanner.hasNextLine()){
				String interfaceCommand = scanner.nextLine();
				interfaceCommand = interfaceCommand.replace("\r", "");
				String[] s = interfaceCommand.split("\\s+");

				if(s[0].equalsIgnoreCase("uci")){
					System.out.println("id name " + engine.getName());
					System.out.println("id author Jack Crawford");
					System.out.println("uciok");
					System.out.flush();
				} else if(s[0].equalsIgnoreCase("ucinewgame")){
					engine.resetEngine();
					pos = Position.startPos();
				} else if(s[0].equalsIgnoreCase("position")){
					if(s[1].equalsIgnoreCase("fen")){
						Matcher temp = fenSel.matcher(interfaceCommand);
						temp.find();
						pos = FenParser.parse(temp.group(1));
					} else if(s[1].equalsIgnoreCase("startpos")){
						pos = Position.startPos();
					}

					Matcher temp = moveSel.matcher(interfaceCommand);
					if(temp.find()){
						int turn = pos.sideToMove;
						String moves = temp.group(1);
						String[] ml = moves.split("\\s+");
						for(int a = 0; a < ml.length; a++){
							UCIMove m = parseMove(ml[a]);
							long encoding = 0;
							if(m.type == UCIMove.MoveType.Normal){
								encoding = pos.s.executeMove(turn, 1L<<m.move[0], 1L<<m.move[1], m.ptype.getCode());
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
					System.out.println("readyok");
					System.out.flush();
				} else if(s[0].equalsIgnoreCase("stop")){
					engine.stop();
				} else if(s[0].equalsIgnoreCase("go")){
					GoParams params = new GoParams(interfaceCommand);
					engine.go(params, pos);
				} else if(!ignoreQuit && s[0].equalsIgnoreCase("quit")){
					break;
				} else if(controllerExtras){
					ControlExtension ext = controllerExtMap.get(s[0]);
					if(ext != null){
						ext.execute(interfaceCommand, pos, engine);
					}
				}
			}
			scanner.close();
		}
	};
	
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
		
		m.ptype = UCIMove.PromotionType.Queen;
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
	
	public static void main(String[] args){
		UCIParams p = new UCIParams();
		
		for(int a = 0; a < args.length; a++){
			try{
				if(args[a].equals("--hash")){ //sets main hash size (must be power of 2)
					p.hashSize = Integer.parseInt(args[++a]);
				} else if(args[a].equals("--pawnHash")){ //sets pawn hash size (must be power of 2)
					p.pawnHashSize = Integer.parseInt(args[++a]);
				} else if(args[a].equals("--no-info")){ //turns off chess.uci info printing (ie, pv, score, time, etc)
					p.printInfo = false;
				} else if(args[a].equals("--ignore-quit")){ //turns off handling of uci quit command (need C-c to shutdown)
					p.ignoreQuit = true;
				} else if(args[a].equals("--warm-up")){ //warm up the jvm
					p.warmUp = true;
				} else if(args[a].equals("--extras")){ //turn on console controller extensions
					p.controllerExtras = true;
				} else if(args[a].equals("--profile")){ //profile engine on passed fen file then exit
					p.profile = true;
					p.profileFen = args[++a];
				}
			} catch(Exception e){
				System.out.println("error, incorrect args");
				System.exit(1);
			}
		}
		
		new UCI(p);
	}
}
