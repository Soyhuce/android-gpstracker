package fr.soyhuce.gpstracker.exceptions;

/**
 * Created by mathieuedet on 05/03/2018.
 */

public class NoLocationException extends Exception{
    @Override
    public String getMessage() {
        return "No location detected";
    }
}