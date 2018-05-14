package ru.kazantsev.template.util;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.List;

/**
 * Created by Admin on 11.09.2017.
 */
public class DBFlowUtils {

    public static <C> List<C> dbFlowOneTwoManyUtilMethod(List<C> list, Class<C> clazz, Operator... in) {
        if (list == null || list.isEmpty()) {
            list = dbFlowQueryList(clazz, in);
        }
        return list;
    }

    public static <C> C dbFlowFindFirst(Class<C> clazz, Operator... in) {
        return SQLite.select()
                .from(clazz)
                .where(in)
                .querySingle();
    }

    public static long dbFlowDelete(Class clazz, Operator... in) {
        From from = SQLite.delete()
                .from(clazz);
        long affected = 0;
        if(in != null) {
            from.where(in).execute();
        } else {
            from.execute();
        }
        return affected;
    }

    public static <C> List<C> dbFlowQueryList(Class<C> clazz, int skip, int limit, OrderBy orderBy, Operator... in) {
        From<C> from = SQLite.select().distinct()
                .from(clazz);
        Where<C> where = null;
        if(in != null) {
            where =  from.where(in);
        }
        if(where != null) {
            if(skip >= 0) {
                where = where.offset(skip);
            }
        } else {
            if(skip >= 0) {
                where = from.offset(skip);
            }
        }
        if(where != null) {
            if(limit >= 0) {
                where = where.limit(limit);
            }
        } else {
            if(limit >= 0) {
                where = from.limit(limit);
            }
        }
        if(where != null) {
            if(orderBy == null) {
                return where.queryList();
            } else {
                return where.orderBy(orderBy).queryList();
            }
        } else {
            if(orderBy == null) {
                return from.queryList();
            } else {
                return from.orderBy(orderBy).queryList();
            }
        }
    }

    public static <C> List<C> dbFlowQueryList(Class<C> clazz, Operator... in) {
        return dbFlowQueryList(clazz, -1, -1, null, in);
    }
}
