package org.bouncycastle.i18n;

public class MissingEntryException extends RuntimeException 
{

    protected final String resource;
    protected final String key;

    public MissingEntryException(String message, String resource, String key) 
    {
        super(message);
        this.resource = resource;
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public String getResource()
    {
        return resource;
    }

}
