package ru.kazantsev.template.database.ormlite.persister;

import android.net.Uri;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

/**
 * Created by 0shad on 17.03.2016.
 */
public class UriPersister extends BaseDataType {

    private static final UriPersister instance = new UriPersister();

    public UriPersister() {
        super(SqlType.STRING, new Class<?>[]{Uri.class});
    }

    public static UriPersister getSingleton() {
        return instance;
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return Uri.parse(defaultStr);
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String uri = (String) sqlArg;
        if (uri == null) {
            return null;
        } else {
            return Uri.parse(uri);
        }
    }


    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject == null) {
            return null;
        } else {
            return javaObject.toString();
        }
    }
}
