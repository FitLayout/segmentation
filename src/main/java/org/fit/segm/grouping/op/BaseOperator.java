/**
 * BaseOperator.java
 *
 * Created on 21. 1. 2015, 10:12:31 by burgetr
 */
package org.fit.segm.grouping.op;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fit.layout.api.AreaTreeOperator;

/**
 * A common base for our area operators.
 * 
 * @author burgetr
 */
public abstract class BaseOperator implements AreaTreeOperator
{

    /**
     * Sets the parameter using the appropriate setter method (if present).
     */
    @Override
    public boolean setParam(String name, Object value)
    {
        String sname = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Method m;
        try {
            if (value instanceof Integer)
            {
                m = getClass().getMethod(sname, int.class);
                m.invoke(this, value);
            }
            else if (value instanceof Double)
            {
                m = getClass().getMethod(sname, float.class);
                m.invoke(this, ((Double) value).floatValue());
            }
            else if (value instanceof Float)
            {
                m = getClass().getMethod(sname, float.class);
                m.invoke(this, value);
            }
            else if (value instanceof Boolean)
            {
                m = getClass().getMethod(sname, boolean.class);
                m.invoke(this, value);
            }
            else
            {
                m = getClass().getMethod(sname, String.class);
                m.invoke(this, value.toString());
            }
            return true;
            
        } catch (NoSuchMethodException e) {
            System.err.println("Setting unknown parameter: " + e.getMessage());
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

    }

}
