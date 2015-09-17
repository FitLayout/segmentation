/**
 * SortByLinesOperator.java
 *
 * Created on 17. 9. 2015, 13:49:32 by burgetr
 */
package org.fit.segm.grouping.op;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.Rectangular;
import org.fit.segm.grouping.AreaImpl;

/**
 * 
 * @author burgetr
 */
public class SortByLinesOperator extends BaseOperator
{
    protected final String[] paramNames = { };
    protected final ValueType[] paramTypes = { };
    
    public SortByLinesOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.SortByLines";
    }
    
    @Override
    public String getName()
    {
        return "Sort by lines";
    }

    @Override
    public String getDescription()
    {
        return "Sorts the visual areas roughly according to the text lines detected in the file";
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
    
    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        recursiveJoinAreas((AreaImpl) atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursiveJoinAreas((AreaImpl) root);
    }
    
    //==============================================================================
    
    /**
     * Goes through all the areas in the tree and tries to join their sub-areas into single
     * areas.
     */
    protected void recursiveJoinAreas(AreaImpl root)
    {
        joinAreas(root);
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveJoinAreas((AreaImpl) root.getChildArea(i));
    }
    
    /**
     * Goes through the grid of areas and joins the adjacent visual areas that are not
     * separated by anything
     */
    protected void joinAreas(AreaImpl a)
    {
        if (a.getGrid() == null) //a gird is necessary for this
            a.createGrid();
        
        boolean change = true;
        while (change)
        {
            change = false;
            for (int i = 0; i < a.getChildCount(); i++)
            {
                AreaImpl node = (AreaImpl) a.getChildArea(i);
                int ny1 = node.getGridPosition().getY1();
                int nx2 = node.getGridPosition().getX2();
                int ny2 = node.getGridPosition().getY2();
                
                //try to expand to the right - find a neighbor
                AreaImpl neigh = null;
                int dist = 1;
                while (neigh == null && nx2 + dist < a.getGrid().getWidth())
                {
                    //try to find some node at the right in the given distance
                    for (int y = ny1; neigh == null && y <= ny2; y++)
                    {
                        neigh = (AreaImpl) a.getGrid().getAreaAt(nx2 + dist, y);
                        if (neigh != null) //something found
                        {
                            if (isOnSameLine(a, node, neigh)) //check if the nodes could be joined
                            {
                                //TODO
                            }
                        }
                    }
                    dist++;
                }
                if (change) break; //something changed, repeat
            }
        }
    }

    /**
     * Joins two boxes horizontally into one area if the node heights are equal or they 
     * can be aligned to a rectangle using free spaces.
     * @param n1 left node to be aligned
     * @param n2 right node to be aligned
     * @return <code>true</code> when succeeded
     */
    private boolean isOnSameLine(AreaImpl parent, AreaImpl n1, AreaImpl n2)
    {
        //System.out.println("HJoin: " + n1.toString() + " + " + n2.toString());
        //align the start
        int sy1 = n1.getGridPosition().getY1();
        int sy2 = n2.getGridPosition().getY1();
        while (sy1 != sy2)
        {
            if (sy1 < sy2) //n1 starts earlier, try to expand n2 up
            {
                if (sy2 > 0 && canExpandY(parent, n2, sy2-1, n1))
                    sy2--;
                else
                    return false; //cannot align - give up
            }
            else if (sy1 > sy2) //n2 starts earlier, try to expand n1 up
            {
                if (sy1 > 0 && canExpandY(parent, n1, sy1-1, n2))
                    sy1--;
                else
                    return false; //cannot align - give up
            }
        }
        //System.out.println("sy1="+sy1);
        //align the end
        int ey1 = n1.getGridPosition().getY2(); //last
        int ey2 = n2.getGridPosition().getY2();
        while (ey1 != ey2)
        {
            if (ey1 < ey2) //n1 ends earlier, try to expand n1 down
            {
                if (ey1 < parent.getGrid().getWidth()-1 && canExpandY(parent, n1, ey1+1, n2))
                    ey1++;
                else
                    return false; //cannot align - give up
            }
            else if (ey1 > ey2) //n2 ends earlier, try to expand n2 down
            {
                if (ey2 < parent.getGrid().getWidth()-1 && canExpandY(parent, n2, ey2+1, n1))
                    ey2++;
                else
                    return false; //cannot align - give up
            }
        }
        //System.out.println("ey1="+ey1);
        //align succeeded
        return true;
    }
    
    
    /**
     * Checks if the area can be vertically expanded to the given 
     * Y coordinate, i.e. there is a free space in the space on this Y coordinate
     * for the whole width of the area.
     * @param node the area node that should be expanded
     * @param y the Y coordinate to that the area should be expanded
     * @param except an area that shouldn't be considered for conflicts (e.g. an overlaping area)
     * @return <code>true</code> if the area can be expanded
     */
    private boolean canExpandY(AreaImpl parent, AreaImpl node, int y, AreaImpl except)
    {
        for (int x = node.getGridX(); x < node.getGridX() + node.getGridWidth(); x++)
        {
            AreaImpl cand = (AreaImpl) parent.getGrid().getAreaAt(x, y);
            if (cand != null && cand != except)
                return false; //something found - cannot expand
        }
        return true;
    }

}
