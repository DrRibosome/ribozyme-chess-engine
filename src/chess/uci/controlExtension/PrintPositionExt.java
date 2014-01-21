package chess.uci.controlExtension;

import chess.uci.Position;
import chess.uci.UCIEngine;

/** extension for printing a position*/
public class PrintPositionExt implements ControlExtension {

	@Override
	public void execute(String command, Position pos, UCIEngine engine) {
		if(pos == null){
			System.out.println("no state information");
		} else{
			System.out.println("side to move: "+pos.sideToMove);
			System.out.println(pos.s);
		}
	}
}
