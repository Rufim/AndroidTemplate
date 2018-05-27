package ru.kazantsev.template.lister;

/**
 * Created by 0shad on 01.11.2015.
 */
public interface PageDataSource<P>  {
    public abstract P getPage(int index) throws Exception;
}
