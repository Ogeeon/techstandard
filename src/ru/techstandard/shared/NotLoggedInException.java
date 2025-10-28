package ru.techstandard.shared;

public class NotLoggedInException extends Exception {
	private static final long serialVersionUID = 1L;

	public NotLoggedInException() {}

    public NotLoggedInException(String message)
    {
       super(message);
    }
}
