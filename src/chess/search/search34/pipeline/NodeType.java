package chess.search.search34.pipeline;

/** pvs framework node types*/
public enum NodeType{
	pv(){
		public NodeType next(){
			return cut;
		}
	},
	cut(){
		public NodeType next(){
			return all;
		}
	},
	all(){
		public NodeType next(){
			return cut;
		}
	};
	public abstract NodeType next();
};
