package server;

import java.awt.AWTException;
import java.awt.Robot;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import search.Search3;
import search.SearchS4V26;
import search.SearchStat;
import util.AlgebraicNotation2;
import util.SuperBook;
import util.board4.State4;
import ai.modularAI2.Evaluator2;
import customAI.evaluators.board4.SuperEvalS4V8;


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

	Search3<State4> searcher;

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
		book.init("millionBook.txt");
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
			else{
				/*botPlayer = message.charAt(0) == '0' ? 0 : 1;
				//int turn = message.charAt(1) == '0' ? 0 : 1;
				String moves = message.length() >= 3? message.substring(3, message.length()): "";
				String[] moveStrings = moves.split("\n");*/
				
				//System.out.println("message = "+message);
				botPlayer = message.charAt(0) == '0' ? 0 : 1;
				Pattern timeSelector = Pattern.compile("<times=(.*?)>");
				Matcher m1 = timeSelector.matcher(message);
				m1.find();
				String[] times = m1.group(1).split(","); //times
				//System.out.println("time = "+times[0]+", "+times[1]);
				int[] time = new int[]{getMilliSec(times[0]), getMilliSec(times[1])};

				Pattern moveSelector = Pattern.compile("<moves=(.*?)>", Pattern.DOTALL);
				Matcher m2 = moveSelector.matcher(message);
				m2.find();
				String moves = m2.group(1);
				String[] moveStrings = moves.length() > 1? moves.split("\n"): new String[0];
				//System.out.println("moves = "+moves);
				
				
				
				
				/*if(moveStrings[0].equals(""))
					moveStrings = new String[0]; */
				
				if(moveStrings.length < processedMoves.size()-2 && !resettable)
				{
					resettable = true;
				}
				
				if(resettable || searcher == null){
					Evaluator2<State4> e = new SuperEvalS4V8();
					s = new State4();
					s.initialize();
					book = new SuperBook();
					book.init("millionBook.txt");
					processedMoves = new ArrayList<String>();
					searcher =
							//new SearchS4V25qzit(16, s, e, 20);
							new SearchS4V26(16, s, e, 20, true);
					movedWaiting = false;
					ourTurn = false;
					resettable = false;
				}

				for (int i = 0; i < moveStrings.length; i++)
				{
					if (processedMoves.size() <= i)
					{
						System.out.println(i);
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
					boolean hasMoves = book.hasMoves();
					boolean failed = false;
					boolean skipped = true;
					if(1==2){
						skipped = false;
						try{
							if(book.hasMoves()){
								book.getMove(move);
								System.out.println("tombookery: " + SuperBook.getMoveString(move));
								try
								{
									Thread.sleep(50);
								}
								catch (InterruptedException e) {e.printStackTrace();}
							}
						} catch(Exception a){
							a.printStackTrace();
							failed = true;
						}
					}
					if(!hasMoves || failed || skipped){
						//int offset = (int)(Math.random()*300-150);
						int searchTime = 4270 < time[botPlayer]/53? 4270: time[botPlayer]/53;
						
						
						if(time[botPlayer] < 5000){ //5 sec left
							searchTime = 300;
						} else if(time[botPlayer] < 10000){
							searchTime = 400;
						} else if(time[botPlayer] < 14000){
							searchTime = 670;
						} else if(time[botPlayer] < 47000){
							searchTime = 850;
						} else if(time[botPlayer] < 51000){ //51 sec left
							searchTime = 1040;
						} else if(time[botPlayer] < 60000){
							searchTime = 1200;
						}
						//searcher.getMove(move, botPlayer, searchTime);
						//searcher.search(botPlayer, move, searchTime);
						search(searcher, botPlayer, 16, searchTime, move);
						SearchStat stats = searcher.getStats();
						System.out.println("search time = "+stats.searchTime);
					}
					//searcher.getMove(move, botPlayer, 650);
					movedWaiting = true;
					currentMove = "" + (char)('a'+move[0]%8)+(move[0]/8+1)+(char)('a'+move[1]%8)+(move[1]/8+1);
					conn.send(currentMove);
				}
			}
		}
	}
	
	private static void search(final Search3<?> s, final int player,
			final int maxPly, final int searchTime, final int[] move){
		Thread t = new Thread(){
			public void run(){
				s.search(player, move, maxPly);
			}
		};
		t.setDaemon(true);
		t.start();
		long start = System.currentTimeMillis();
		while(t.isAlive() && System.currentTimeMillis()-start < searchTime){
			try{
				Thread.sleep(30);
			} catch(InterruptedException e){}
		}
		
		s.cutoffSearch();
		while(t.isAlive()){
			try{
				t.join();
			} catch(InterruptedException e){}
		}
	}
	
	private static int getMilliSec(String time){
		String[] s = time.split(":");
		double min = Double.parseDouble(s[0]);
		double sec = Double.parseDouble(s[1]);
		return (int)((min*60+sec)*1000);
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
