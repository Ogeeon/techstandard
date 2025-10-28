package ru.techstandard.shared;

public class LoginErrorException extends Exception {
	private static final long serialVersionUID = 1L;

	public LoginErrorException() {}

    public LoginErrorException(String message)
    {
       super(message);
    }
}
