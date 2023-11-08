package me.squeeglii.plugin.dislink.exception;

/**
 * For use when a required property is missing from a config
 * @author CG360
 */
public class MissingPropertyException extends RuntimeException {

    public MissingPropertyException() { super(); }
    public MissingPropertyException(String str) { super(str); }

}