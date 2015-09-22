/**
 * SortByLinesOperator.java
 *
 * Created on 17. 9. 2015, 13:49:32 by burgetr
 */
package org.fit.segm.grouping.op;

import java.util.List;
import java.util.Vector;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;
import org.fit.segm.grouping.AreaUtils;

/**
 * 
 * @author burgetr
 */
public class SortByLinesOperator extends SortByPositionOperator
{
    protected final String[] paramNames = { };
    protected final ValueType[] paramTypes = { };
    
    public SortByLinesOperator()
    {
        super(false);
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
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursivelySortChildAreas(root, false);
        recursiveSortLines((AreaImpl) root);
    }
    
    //==============================================================================
    
    /**
     * Goes through all the areas in the tree and sorts their sub-areas.
     */
    protected void recursiveSortLines(AreaImpl root)
    {
        sortChildLines(root);
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveSortLines((AreaImpl) root.getChildArea(i));
    }
    
    /**
     * Goes through the grid of areas and sorts the adjacent visual areas that are not
     * separated by anything
     */
    protected void sortChildLines(AreaImpl root)
    {
        if (root.getGrid() == null) //a gird is necessary for this
            root.createGrid();
        
        List<Area> src = new Vector<Area>(root.getChildAreas());
        List<Area> dest = new Vector<Area>(src.size());
        while (!src.isEmpty())
        {
            final AreaImpl seed = (AreaImpl) src.get(0);
            List<Area> line = findAreasOnLine(root, seed, src);
            System.out.println("seed: " + seed);
            System.out.println("   r: " + line);
            dest.addAll(line);
            src.removeAll(line);
        }
        
        root.removeAllChildren();
        root.appendChildren(dest);
    }

    private List<Area> findAreasOnLine(AreaImpl parent, AreaImpl area, List<Area> candidates)
    {
        if (area.toString().contains("Bystrc"))
            System.out.println("jo!");
        Vector<Area> ret = new Vector<Area>();
        ret.add(area);
        
        final int ny1 = area.getGridPosition().getY1();
        final int nx2 = area.getGridPosition().getX2();
        final int ny2 = area.getGridPosition().getY2();
        
        //try to expand to the right
        int dist = 1;
        while (nx2 + dist < parent.getGrid().getWidth())
        {
            //try to find some node at the right in the given distance
            for (int y = ny1; y <= ny2; y++)
            {
                AreaImpl neigh = (AreaImpl) parent.getGrid().getAreaAt(nx2 + dist, y);
                if (neigh != null && candidates.contains(neigh)) //something found
                {
                    //if (isOnSameLine(parent, area, neigh)) //check if the nodes could be joined
                    if (AreaUtils.isOnSameLine(area, neigh, 2))
                    {
                        ret.add(neigh);
                        break;
                    }
                }
            }
            dist++;
        }
            
        return ret;
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
