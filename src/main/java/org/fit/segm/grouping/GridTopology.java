/**
 * GridTopology.java
 *
 * Created on 12. 11. 2014, 10:33:00 by burgetr
 */
package org.fit.segm.grouping;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class GridTopology implements AreaTopology
{
    private AreaImpl area;
    
    public GridTopology(AreaImpl area)
    {
        this.area = area;
    }

    @Override
    public Rectangular getPosition()
    {
        return area.getGridPosition();
    }

    @Override
    public Area getPreviousOnLine()
    {
        return area.getPreviousOnLine();
    }

    @Override
    public Area getNextOnLine()
    {
        return area.getNextOnLine();
    }

    @Override
    public int getMinIndent()
    {
        return area.getGrid().getMinIndent();
    }

    @Override
    public int getMaxIndent()
    {
        return area.getGrid().getMaxIndent();
    }

}
