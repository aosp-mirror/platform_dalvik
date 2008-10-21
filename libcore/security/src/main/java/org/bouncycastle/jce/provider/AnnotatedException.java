package org.bouncycastle.jce.provider;

public class AnnotatedException 
    extends Exception
{
    private Exception _underlyingException;

    AnnotatedException(
        String string, 
        Exception e)
    {
        super(string);
        
        _underlyingException = e;
    }
    
    AnnotatedException(
        String string)
    {
        this(string, null);
    }

    Exception getUnderlyingException()
    {
        return _underlyingException;
    }
    
    public Throwable getCause()
    {
        return _underlyingException;
    }
}
