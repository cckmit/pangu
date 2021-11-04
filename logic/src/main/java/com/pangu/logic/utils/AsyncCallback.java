package  com.pangu.logic.utils;

public interface AsyncCallback<T> {
	
	void onSuccess(T result);

	void onError(Exception ex);
}
