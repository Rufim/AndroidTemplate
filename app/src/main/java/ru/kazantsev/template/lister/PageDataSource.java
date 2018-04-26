package ru.kazantsev.template.lister;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.kazantsev.template.mvp.view.DataSourceView;

/**
 * Created by 0shad on 01.11.2015.
 */
public interface PageDataSource<P>  {
    public abstract P getPage(int index) throws Exception;
}
