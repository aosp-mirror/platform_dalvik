package org.bouncycastle.i18n;

import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.bouncycastle.i18n.filter.Filter;
import org.bouncycastle.i18n.filter.UntrustedInput;

public class LocalizedMessage 
{

    protected final String id;
    protected final String resource;
    
    protected Object[] arguments;
    protected Object[] filteredArguments;
    
    protected Filter filter = null;
    
    /**
     * Constructs a new LocalizedMessage using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public LocalizedMessage(String resource,String id) throws NullPointerException
    {
        if (resource == null || id == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        this.arguments = new Object[0];
        this.filteredArguments = arguments;
    }
    
    /**
     * Constructs a new LocalizedMessage using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param arguments an array containing the arguments for the message
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public LocalizedMessage(String resource, String id, Object[] arguments) throws NullPointerException
    {
        if (resource == null || id == null || arguments == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        this.arguments = arguments;
        this.filteredArguments = arguments;
    }
    
    /**
     * Reads the entry <code>id + "." + key</code> from the resource file and returns a 
     * formated message for the given Locale and TimeZone.
     * @param key second part of the entry id
     * @param loc the used {@link Locale}
     * @param timezone the used {@link TimeZone}
     * @return a Strng containing the localized message
     * @throws MissingEntryException if the resource file is not available or the entry does not exist.
     */
    public String getEntry(String key,Locale loc, TimeZone timezone) throws MissingEntryException
    {
        String entry = id + "." + key;
        
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(resource,loc);
            String template = bundle.getString(entry);
            if (arguments == null || arguments.length == 0)
            {
                return template;
            }
            else
            {
                return formatWithTimeZone(template,filteredArguments,loc,timezone);
            }
        }
        catch (MissingResourceException mre)
        {
            throw new MissingEntryException("Can't find entry " + entry + " in resource file " + resource + ".",
                    resource,
                    entry); 
        }
    }
    
    protected String formatWithTimeZone(
            String template,
            Object[] arguments, 
            Locale locale,
            TimeZone timezone) 
    {
        MessageFormat mf = new MessageFormat(" ");
        mf.setLocale(locale);
        mf.applyPattern(template);
        if (!timezone.equals(TimeZone.getDefault())) 
        {
            Format[] formats = mf.getFormats();
            for (int i = 0; i < formats.length; i++) 
            {
                if (formats[i] instanceof DateFormat) 
                {
                    DateFormat temp = (DateFormat) formats[i];
                    temp.setTimeZone(timezone);
                    mf.setFormat(i,temp);
                }
            }
        }
        return mf.format(arguments);
    }
    
    /**
     * Sets the {@link Filter} that is used to filter the arguments of this message
     * @param filter the {@link Filter} to use. <code>null</code> to disable filtering.
     */
    public void setFilter(Filter filter)
    {
        if (filter == null)
        {
            filteredArguments = arguments;
        }
        else if (!filter.equals(this.filter))
        {
            filteredArguments = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++)
            {
                if (arguments[i] instanceof UntrustedInput) 
                {
                    filteredArguments[i] = filter.doFilter(((UntrustedInput) arguments[i]).getString());
                }
                else
                {
                    filteredArguments[i] = arguments[i];
                }
            }
        }
        this.filter = filter;
    }
    
    /**
     * Returns the current filter.
     * @return the current filter
     */
    public Filter getFilter()
    {
        return filter;
    }
    
    /**
     * Returns the id of the message in the resource bundle.
     * @return the id of the message
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * Returns the name of the resource bundle for this message
     * @return name of the resource file
     */
    public String getResource()
    {
        return resource;
    }
    
    /**
     * Returns an <code>Object[]</code> containing the message arguments.
     * @return the message arguments
     */
    public Object[] getArguments()
    {
        return arguments;
    }
    
}
