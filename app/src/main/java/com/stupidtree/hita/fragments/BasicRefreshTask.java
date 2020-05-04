package com.stupidtree.hita.fragments;

public class BasicRefreshTask<T> extends WeakAsyncTask<Boolean, Integer, T, BasicRefreshTask.ListRefreshedListener> {


    protected Object[] others;
    private String id = "";

    public BasicRefreshTask(ListRefreshedListener listRefreshedListener) {
        super(listRefreshedListener);
    }

    public BasicRefreshTask(ListRefreshedListener listRefreshedListener, Boolean... params) {
        super(listRefreshedListener);
        if (params != null) this.params = params;
    }


    @Override
    protected void onPreExecute(ListRefreshedListener listRefreshedListener) {
        super.onPreExecute(listRefreshedListener);
        if (listRefreshedListener != null) listRefreshedListener.onRefreshStart(id, params);
    }

    @Override
    protected T doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
        return null;
    }

    @Override
    protected void onPostExecute(ListRefreshedListener listRefreshedListener, T ts) {
        super.onPostExecute(listRefreshedListener, ts);
        if (listRefreshedListener != null) {
            if (listRefreshedListener instanceof ListRefreshedListener2) {
                ((ListRefreshedListener2) listRefreshedListener).onListRefreshed(id, params, ts, others);
            } else {
                listRefreshedListener.onListRefreshed(id, params, ts);
            }

        }
    }

    public interface ListRefreshedListener<T> {
        void onRefreshStart(String id, Boolean[] params);

        void onListRefreshed(String id, Boolean[] params, T result);
    }

    public interface ListRefreshedListener2<T> extends ListRefreshedListener {
        void onListRefreshed(String id, Boolean[] params, T result, Object[] others);
    }
}
