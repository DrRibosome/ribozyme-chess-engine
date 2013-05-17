
var connection;
var activeBoard = 0;
var moveString;
try {
connection = new WebSocket('ws://localhost:8000');
}
catch(exception) {
console.log(exception);
}

connection.onopen = function(){  
    console.log("Socket has been opened!");  
}  

connection.onmessage = function(evt)
{
	moveString = evt.data;
	//console.log("move received: " + evt.data);

	var space1 = document.querySelector("#img_chessboard_" + activeBoard + "_" + evt.data.charAt(0) + evt.data.charAt(1));
	//console.log("space1 = "+space1);

	if(space1 == null)
	{
		//console.log("creating dummy space");
		var t1 = document.getElementById("chessboard_" + activeBoard + "_boardarea");
		space1 = document.createElement("img");
		space1.id = "img_chessboard_" + activeBoard + "_" + evt.data.charAt(2) + evt.data.charAt(3);
		space1.class = space1.class;
		space1.style = space1.style;
		space1.src = space1.src;
		space1.style.display = "none";
		
		t1.appendChild(space1);
	}

	var evt1 = document.createEvent("MouseEvents");
	evt1.initMouseEvent("click", true, true, window,
				0, 0, 0, 0, 0, false, false, false, false, 0, null);
	space1.dispatchEvent(evt1);

	setTimeout(function(){
	//console.log("" + evt.data.charAt(2) + evt.data.charAt(3));
	var space2 = document.querySelector("#img_chessboard_" + activeBoard + "_" + evt.data.charAt(2) + evt.data.charAt(3));
	//console.log("space2 = "+space2);
	//console.log("selector2 = "+"#img_chessboard_" + activeBoard + "_" + evt.data.charAt(2) + evt.data.charAt(3));

	if(space2 == null)
	{
		//console.log("creating dummy space");
		var t1 = document.getElementById("chessboard_" + activeBoard + "_boardarea");
		space2 = document.createElement("img");
		space2.id = "img_chessboard_" + activeBoard + "_" + evt.data.charAt(2) + evt.data.charAt(3);
		space2.class = space1.class;
		space2.style = space1.style;
		space2.src = space1.src;
		space2.style.display = "none";
		
		t1.appendChild(space2);
	}
		
	var evt2 = document.createEvent("MouseEvents");
	evt2.initMouseEvent("click", true, true, window,
				0, 0, 0, 0, 0, false, false, false, false, 0, null);
	space2.dispatchEvent(evt2);
	}, 50);
	//setTimeout(clickAgain, 2000);
}

function clickAgain()
{
	var space1 = document.querySelector("#img_chessboard_" + activeBoard + "_" + moveString.charAt(0) + moveString.charAt(1));
	//console.log("space1 = "+space1);
	var evt1 = document.createEvent("MouseEvents");
	evt1.initMouseEvent("click", true, true, window,
				0, 0, 0, 0, 0, false, false, false, false, 0, null);
	space1.dispatchEvent(evt1);

	setTimeout(function(){
	//console.log("" + moveString.charAt(2) + moveString.charAt(3));
	var space2 = document.querySelector("#img_chessboard_" + activeBoard + "_" + moveString.charAt(2) + moveString.charAt(3));
	//console.log("space2 = "+space2);
	//console.log("selector2 = "+"#img_chessboard_" + activeBoard + "_" + moveString.charAt(2) + moveString.charAt(3));

	//if(space2 == null)
	//{
		//console.log("creating dummy space");
		var t1 = document.getElementById("chessboard_" + activeBoard + "_boardarea");
		space2 = document.createElement("img");
		space2.id = "img_chessboard_" + activeBoard + "_" + moveString.charAt(2) + moveString.charAt(3);
		space2.class = space1.class;
		space2.style = space1.style;
		space2.src = space1.src;
		space2.style.display = "none";
		
		t1.appendChild(space2);
	//}
		
	var evt2 = document.createEvent("MouseEvents");
	evt2.initMouseEvent("click", true, true, window,
				0, 0, 0, 0, 0, false, false, false, false, 0, null);
	space2.dispatchEvent(evt2);
	}, 50);
	updateBoard();
}


function updateBoard()
{
	boardList = document.querySelectorAll("#chessboard_1, #chessboard_2, #chessboard_3, #chessboard_4, #chessboard_5, #chessboard_6, #chessboard_7, #chessboard_8, #chessboard_9");
	
	var i = 1;
	var j = 1;
	var playerTurn;
	activeBoard = boardList[0].id.charAt(11);
	boardNum = activeBoard;
	
	var moveCount = 1;
	while(document.getElementById("notation_" + activeBoard + "_gotomoveid_0_" + moveCount) != null && document.getElementById("notation_" + activeBoard + "_gotomoveid_0_" + moveCount).innerHTML != "")
	{
		moveCount++;
	}
	moveCount -= 1;
	
	var board = [[" "," "," "," "," "," "," "," "],
	             [" "," "," "," "," "," "," "," "],
	             [" "," "," "," "," "," "," "," "],
	             [" "," "," "," "," "," "," "," "],
	             [" "," "," "," "," "," "," "," "],
	             [" "," "," "," "," "," "," "," "],
	             [" "," "," "," "," "," "," "," "],
	             [" "," "," "," "," "," "," "," "]];
	
	
	var letters = ["a", "b", "c", "d", "e", "f", "g", "h"];
	for(i = 0; i < 8; i++)
	{
		for(j = 8; j > 0; j--)
		{
			var space = document.getElementById("img_chessboard_" + boardNum + "_" + letters[i] + j);
			if(space != null && space.style.display == "block")
			{
				if(space.src.charAt(72) == 'b')
				{
					board[8-j][i] = String.fromCharCode(space.src.charAt(73).charCodeAt() - 32);
				}
				else
				{
					board[8-j][i] = space.src.charAt(73);
				}
			}
			else
			{
				board[8-j][i] = "#";
			}
		}
	}
	
	var display = new String("");
	
	
	for(i = 0; i < 8; i++)
	{
		for(j = 0; j < 8; j++)
		{
			//display += board[i][j];
		}
	}
	var name = document.getElementById("white_player_name_" + activeBoard).innerHTML;
	if(name == "MrEnzyme" || name == "thesupergame" || name == "drribosome" || name == "Ribozyme" || name == "RibozymeRidesAgain")
	//if(name == "drribosome" || name == "Ribozyme")
	{
		display += "0";
	}
	else
	{
		display += "1";
	}
	
	if(moveCount % 2 == 0)
	{
		display += "0";
	}
	else
	{
		display += "1";
	}
	
	display += "<moves="
	var i = 1;
	for(i = 1; i <= moveCount; i++)
	{
		display += document.getElementById("notation_" + activeBoard + "_gotomoveid_0_" + i).innerHTML;
		if(i != moveCount){
			display += "\n";
		}
	}
	display += ">";
	display += "<times="+document.getElementById("white_timer_" + activeBoard).value+",";
	display += document.getElementById("black_timer_" + activeBoard).value+">";
	
	if(connection.readyState == 1)
	{
		connection.send(display);
	}
	
	tryPromotion();

}

function tryPromotion()
{
	var area = document.getElementById("chessboard_"+activeBoard+"_promotionarea");
	var button = document.getElementById("chessboard_"+activeBoard+"_promotionq");
	if(button != null && area.style.display == "block")
	{
		console.log("clicking");
		var evt1 = document.createEvent("MouseEvents");
		evt1.initMouseEvent("click", true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
		button.dispatchEvent(evt1);
	}
}

window.setInterval(updateBoard, 100);
