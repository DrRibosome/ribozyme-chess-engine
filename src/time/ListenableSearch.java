package time;

import search.Search3;

public interface ListenableSearch extends Search3{
	public void setListener(SearchListener l);
}
