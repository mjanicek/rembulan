package net.sandius.rembulan.core;

public class AsyncTask<T> {

	// TODO

	public interface CompletionHandler<T> {

		void onSuccess(T result);

		void onFailure(Throwable error);

	}

}
