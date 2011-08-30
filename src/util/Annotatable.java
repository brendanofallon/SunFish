package util;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: brendan
 * Date: Nov 10, 2010
 * Time: 2:18:10 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Annotatable {

    public  Set<String> getAnnotationKeys();

    public boolean hasAnnotations();

    public String getAnnotationValue(String key);

    public boolean addAnnotation(String key, String value);

}


