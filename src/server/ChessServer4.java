package server;

import java.awt.AWTException;
import java.awt.Robot;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import search.SearchS4V22qzit;
import search.SearchS4V23qzit;
import util.AlgebraicNotation2;
import util.SuperBook;
import util.board4.State4;
import ai.modularAI2.Evaluator2;
import ai.modularAI2.Search2;
import customAI.evaluators.board4.SuperEvalS4V7;


public class ChessServer4 extends WebSocketServer
{

	String prevBoard = "", currentMove = "";
	boolean firstMove = true;
	State4 s = new State4();
	int spaceWidth = 75;
	int offsetx = 29 + spaceWidth / 2, offsety = 155 + spaceWidth / 2;
	Robot clicker;
	int botPlayer;
	boolean ourTurn = false, movedWaiting = false, resettable = false;
	ArrayList<String> processedMoves;
	SuperBook book;
	Random r;

	Search2<State4> searcher;

	public ChessServer4(int port)
	{
		super(new InetSocketAddress(port));

		try
		{
			clicker = new Robot();
		}
		catch (AWTException e){
			e.printStackTrace();
		}
		processedMoves = new ArrayList<String>();
		book = new SuperBook();
		book.init("openingBook.txt");
		s.initialize();
		r = new Random();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake){
		System.out.println("connection opened");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote){}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		synchronized (this){ 
			if(message.contains("reset"))
			{
				resettable = true;
				System.out.println("got reset command");
			}
			else
			{
			//System.out.println(message);
			botPlayer = message.charAt(0) == '0' ? 0 : 1;
			
			//int turn = message.charAt(1) == '0' ? 0 : 1;
			String moves = message.length() >= 3? message.substring(3, message.length()): "";
			//System.out.println(moves);
			String[] moveStrings = moves.split("\n");
			//System.out.println("moveStrings size = " + moveStrings.length);
			
			if(moveStrings[0].equals(""))
				moveStrings = new String[0]; 
			
			if(moveStrings.length < processedMoves.size()-2 && !resettable)
			{
				resettable = true;
			}
			
			if(resettable || searcher == null){
				Evaluator2<State4> e = new SuperEvalS4V7();
				s = new State4();
				s.initialize();
				processedMoves = new ArrayList<String>();
				searcher =
						//new SearchS4V21qzit(16, s, e, 20);
						//new SearchS4V22qzit(16, s, e, 20);
						new SearchS4V23qzit(16, s, e, 20);
				movedWaiting = false;
				ourTurn = false;
				resettable = false;
			}

			for (int i = 0; i < moveStrings.length; i++)
			{
				if (processedMoves.size() <= i)
				{
					// process move and add to arraylist
					int[] move = AlgebraicNotation2.getPos(i%2, moveStrings[i], s);
					//System.out.println("moving "+move[0]+" -> "+move[1]);
					int[] converted = new int[]{move[0]%8, move[0]/8, move[1]%8, move[1]/8};
					s.resetHistory();
					s.executeMove(i%2, converted[0], converted[1], converted[2], converted[3]);
					if(book.hasMoves())
						book.update(converted);
					//System.out.println("new board after move:\n"+s);
					processedMoves.add(moveStrings[i]);
				}
			}

			// waiting to read back and confirm our move
			if (movedWaiting && moveStrings.length % 2 != botPlayer)
			{
				// no longer waiting, also not our turn
				movedWaiting = false;
				ourTurn = false;
			}
			else if(movedWaiting && r.nextBoolean())
			{
				conn.send(currentMove);
			}

			// move list count indicates its our turn and we havent asked the ai
			// for a move yet
			if (moveStrings.length % 2 == botPlayer && !ourTurn)
			{
				ourTurn = true;
				int[] move = new int[4];
				/*if(book.hasMoves())
				{
					book.getMove(move);
					System.out.println("tombookery: " + SuperBook.getMoveString(move));
				}
				else
				{
					searcher.getMove(move, botPlayer, 950);
				}*/
				searcher.getMove(move, botPlayer, 800);
				movedWaiting = true;
				currentMove = "" + (char)('a' + move[0]) + (move[1]+1) + (char)('a' + move[2]) + (move[3]+1);
				conn.send(currentMove);
			}
			}
		}
	}

	/*
	 * clicker.mouseMove(offsetx + move[0]*spaceWidth, offsety +
	 * (7-move[1])*spaceWidth); clicker.mousePress(InputEvent.BUTTON1_MASK);
	 * clicker.mouseRelease(InputEvent.BUTTON1_MASK);
	 * 
	 * clicker.mouseMove(offsetx + move[2]*spaceWidth, offsety +
	 * (7-move[3])*spaceWidth); clicker.mousePress(InputEvent.BUTTON1_MASK);
	 * clicker.mouseRelease(InputEvent.BUTTON1_MASK);
	 */

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		ex.printStackTrace();
		System.exit(0);
	}
 
	public static void main(String args[])
	{
		new ChessServer4(8000).start();
	}
}
