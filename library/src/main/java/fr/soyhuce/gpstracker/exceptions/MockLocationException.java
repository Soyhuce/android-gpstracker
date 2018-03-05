package fr.soyhuce.gpstracker.exceptions;

/**
 * Created by mathieuedet on 05/03/2018.
 */


public class MockLocationException extends Exception{
    @Override
    public String getMessage() {
        return "Mock location not permits for the application.";
    }
}