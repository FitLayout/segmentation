/**
 * SortByPositionOperator.java
 *
 * Created on 17. 9. 2015, 10:21:22 by burgetr
 */
package org.fit.segm.grouping.op;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;

/**
 * 
 * @author burgetr
 */
public class SortByPositionOperator extends BaseOperator
{
    protected boolean columnFirst;
    
    protected final String[] paramNames = { "columnFirst" };
    protected final ValueType[] paramTypes = { ValueType.BOOLEAN };
    
    public SortByPositionOperator()
    {
        columnFirst = false;
    }
    
    public SortByPositionOperator(boolean columnFirst)
    {
        this.columnFirst = columnFirst;
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.SortByPosition";
    }
    
    @Override
    public String getName()
    {
        return "Sort by position";
    }

    @Override
    public String getDescription()
    {
        return "Sorts the visual areas by their position (x,y coordinates)";
    }

    @Override
    public String[] getParamNames()
    {
        return paramNames;
    }

    @Override
    public ValueType[] getParamTypes()
    {
        return paramTypes;
    }
    
    public boolean getColumnFirst()
    {
        return columnFirst;
    }

    public void setColumnFirst(boolean columnFirst)
    {
        this.columnFirst = columnFirst;
    }

    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursivelySortChildAreas(root, columnFirst);
    }

    //==============================================================================
    
    protected void recursivelySortChildAreas(Area root, final boolean columnFirst)
    {
        if (root.getChildCount() > 1)
        {
            Vector<Area> list = new Vector<Area>(root.getChildAreas());
            Collections.sort(list, new Comparator<Area>() {
                public int compare(Area a1, Area a2)
                {
                    if (!columnFirst)
                        return a1.getY1() == a2.getY1() ? a1.getX1() - a2.getX1() : a1.getY1() - a2.getY1();
                    else
                        return a1.getX1() == a2.getX1() ? a1.getY1() - a2.getY1() : a1.getX1() - a2.getX1();
                }
            });
            
            root.removeAllChildren();
            root.appendChildren(list);
        }
        for (int i = 0; i < root.getChildCount(); i++)
            recursivelySortChildAreas(root.getChildArea(i), columnFirst);
        
    }

}
