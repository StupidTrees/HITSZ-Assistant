package com.stupidtree.hita.online;

import com.stupidtree.hita.R;

import static com.stupidtree.hita.HITAApplication.HContext;

public class SearchException extends Exception {
    public static SearchException newConnectError(){
        return new SearchException(HContext.getString(R.string.search_connect_error));
    }
    public static SearchException newResolveError(){
       return new SearchException(HContext.getString(R.string.search_resolve_error));
    }
    public SearchException(String message) {
        super(message);
    }
}
