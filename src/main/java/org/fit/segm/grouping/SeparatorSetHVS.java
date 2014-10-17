/**
 * SeparatorSetHVS.java
 *
 * Created on 24.5.2011, 14:44:33 by burgetr
 */
package org.fit.segm.grouping;

import java.util.Iterator;
import java.util.Vector;

import org.fit.layout.impl.Area;

/**
 * A horizntal/vertical separator detection with shrinking. The separator set is created by splitting the horizontal and vertical separators independently.
 * Additionally, we shring the vertical separators to the minimal length that separates the boxes - i.e. there is no separator considered between empty spaces.
 * @author radek
 */
public class SeparatorSetHVS extends SeparatorSet
{
    /**
     * Creates a new separator set with one horizontal and one vertical separator.
     */
    public SeparatorSetHVS(GroupingAreaNode root)
    {
        super(root);
    }

    /**
     * Creates a new separator set with one horizontal and one vertical separator.
     */
    public SeparatorSetHVS(GroupingAreaNode root, Area filter)
    {
        super(root, filter);
    }
    
    //=====================================================================================
    
    /**
     * Finds the horizontal and vertical list of separators
     * @param area the root area
     * @param filter if not null, only the sub areas enclosed in the filter area
     *  are considered
     */
    protected void findSeparators(GroupingAreaNode area, Area filter)
    {
        hsep = new Vector<Separator>();
        vsep = new Vector<Separator>();
        
        Area base = (filter == null) ? area.getArea() : filter;
        Separator hinit = new Separator(Separator.HORIZONTAL,
                                        base.getX1(),
                                        base.getY1(),
                                        base.getX2(),
                                        base.getY2());
        hsep.add(hinit);
        
        Separator vinit = new Separator(Separator.VERTICAL,
                                        base.getX1(),
                                        base.getY1(),
                                        base.getX2(),
                                        base.getY2());
        vsep.add(vinit);
        
        if (considerSubareas(this.root, filter) > 1)
        {
            //System.out.println("Filter: " + filter);
            Vector<Area> areas = createAreas(filter);
            //System.out.println("Start: " + areas.size() + " areas"); wait(5000);
            if (areas.size() > 1)
            {
                for (Iterator<Area> it = areas.iterator(); it.hasNext(); )
                {
                    Area a = it.next();
                    //System.out.println("Area: " + a);
                    //dispRect(a.getBounds(), java.awt.Color.RED); wait(100);
                    SeparatorSet aset = new SeparatorSetHVS(area, a);
                    hsep.addAll(aset.getHorizontal());
                    vsep.addAll(aset.getVertical());
                }
            }
        }
        else
        {
            hsep.removeAllElements();
            vsep.removeAllElements();
        }
        applyRegularFilters();
    }
    
    /**
     * Consider a new area -- updates the separators according to this new area
     * @param area The new area node to be considered
     */
    private void considerArea(GroupingAreaNode area)
    {
        //area coordinates
        int ax1 = area.getX();
        int ay1 = area.getY();
        int ax2 = area.getX2();
        int ay2 = area.getY2();
        
        //go through horizontal separators
        Vector<Separator> newseps = new Vector<Separator>();
        for (Iterator<Separator> it = hsep.iterator(); it.hasNext();)
        {
            Separator sep = it.next();
            int sy1 = sep.getY1();
            int sy2 = sep.getY2();
            //the box covers the separator -- remove the separator 
            if (ay1 <= sy1 && ay2 >= sy2)
            {
                    it.remove();
            }
            //box entirely inside -- split the separator 
            else if (ay1 > sy1 && ay2 < sy2)
            {
                Separator newsep = new Separator(Separator.HORIZONTAL,
                                                 sep.getX1(), ay2 + 1,
                                                 sep.getX2(), sep.getY2());
                newseps.add(newsep);
                sep.setY2(ay1 - 1);
            }
            //box partially covers the separator -- update the separator
            else if ((ay1 > sy1 && ay1 <= sy2) && ay2 >= sy2)
            {
                sep.setY2(ay1 - 1);
            }
            //box partially covers the separator -- update the separator
            else if (ay1 <= sy1 && (ay2 >= sy1 && ay2 < sy2))
            {
                sep.setY1(ay2 + 1);
            }
        }
        hsep.addAll(newseps);
        
        //go through vertical separators
        newseps = new Vector<Separator>();
        for (Iterator<Separator> it = vsep.iterator(); it.hasNext();)
        {
            Separator sep = it.next();
            int sx1 = sep.getX1();
            int sx2 = sep.getX2();
            //the box covers the separator -- remove the separator 
            if (ax1 <= sx1 && ax2 >= sx2)
            {
                it.remove();
            }
            //box entirely inside -- split the separator 
            else if (ax1 > sx1 && ax2 < sx2)
            {
                Separator newsep = new Separator(Separator.VERTICAL,
                                                 ax2 + 1, sep.getY1(),
                                                 sep.getX2(), sep.getY2());
                newseps.add(newsep);
                sep.setX2(ax1 - 1);
            }
            //box partially covers the separator -- update the separator
            else if ((ax1 > sx1 && ax1 <= sx2) && ax2 >= sx2)
            {
                sep.setX2(ax1 - 1);
            }
            //box partially covers the separator -- update the separator
            else if (ax1 <= sx1 && (ax2 >= sx1 && ax2 < sx2))
            {
                sep.setX1(ax2 + 1);
            }
        }
        vsep.addAll(newseps);
    }
    
    /**
     * Recursively considers all the sub areas and updates the list of
     * separators.
     * @param area the root area
     * @param filter if not null, only the sub areas enclosed in the filter area are considered
     * @return the number of processed subareas
     */
    private int considerSubareas(GroupingAreaNode area, Area filter)
    {
        int ret = 0;
        for (int i = 0; i < area.getChildCount(); i++)
        {
            GroupingAreaNode sub = area.getChildArea(i);
            if (filter == null || filter.encloses(sub.getArea()))
            {
                if (sub.getArea().isHorizontalSeparator())
                {
                }
                else if (sub.getArea().isVerticalSeparator())
                {
                }
                else
                {
                    //dispSeparators();
                    //System.out.println("Consider: " + sub);
                    //dispRect(sub.getArea().getBounds(), java.awt.Color.GREEN); wait(200);
                    //if (sub.toString().contains("MediaEval"))
                    //    System.out.println("jo!");
                    considerArea(sub);
                    ret++;
                }
            }
        }
        applyRegularFilters();
        return ret;
    }
    
    
    //=====================================================================================

    /**
     * Add a separator and split or update the areas if necessary.
     */
    private void considerSeparator(Vector<Area> areas, Separator sep, boolean horizontal)
    {
        //dispRect(sep, java.awt.Color.GREEN); wait(1000);

        Vector<Area> newareas = new Vector<Area>();
        if (horizontal) //horizontal separator
        {
            int sy1 = sep.getY1();
            int sy2 = sep.getY2();
            for (Iterator<Area> it = areas.iterator(); it.hasNext();)
            {
                Area area = it.next();
                int ay1 = area.getY1();
                int ay2 = area.getY2();
                //the separator covers the area -- remove the area 
                if (sy1 <= ay1 && sy2 >= ay2)
                {
                    it.remove();
                }
                //separator entirely inside -- split the area 
                else if (sy1 > ay1 && sy2 < ay2)
                {
                    Area newarea = new Area(area.getX1(), sy2 + 1,
                                                        area.getX2(), area.getY2());
                    newareas.add(newarea);
                    area.getBounds().setY2(sy1 - 1);
                }
                //separator partially covers the area -- update the area
                else if ((sy1 > ay1 && sy1 <= ay2) && sy2 >= ay2)
                {
                    area.getBounds().setY2(sy1 - 1);
                }
                //separator partially covers the area -- update the area
                else if (sy1 <= ay1 && (sy2 >= ay1 && sy2 < ay2))
                {
                    area.getBounds().setY1(sy2 + 1);
                }
            }
        }
        else //vertical separator
        {
            int sx1 = sep.getX1();
            int sx2 = sep.getX2();
            for (Iterator<Area> it = areas.iterator(); it.hasNext();)
            {
                Area area = it.next();
                int ax1 = area.getX1();
                int ax2 = area.getX2();
                //the separator covers the area -- remove the area 
                if (sx1 <= ax1 && sx2 >= ax2)
                {
                    it.remove();
                }
                //separator entirely inside -- split the area 
                else if (sx1 > ax1 && sx2 < ax2)
                {
                    Area newarea = new Area(sx2 + 1, area.getY1(),
                                                        area.getX2(), area.getY2());
                    newareas.add(newarea);
                    area.getBounds().setX2(sx1 - 1);
                }
                //separator partially covers the area -- update the area
                else if ((sx1 > ax1 && sx1 <= ax2) && sx2 >= ax2)
                {
                    area.getBounds().setX2(sx1 - 1);
                }
                //separator partially covers the area -- update the area
                else if (sx1 <= ax1 && (sx2 >= ax1 && sx2 < ax2))
                {
                    area.getBounds().setX1(sx2 + 1);
                }
            }
        }
        areas.addAll(newareas);
    }
    
    /**
     * Add a separator but do not consider its width (zero-width separator). Split or update the areas if necessary.
     * Thin separators are considered only when they span for the whole width/height of the processed area.
     */
    private void considerThinSeparator(Vector<Area> areas, Separator sep, boolean horizontal)
    {
        Vector<Area> newareas = new Vector<Area>();
        if (horizontal) //horizontal separator
        {
            int sy1 = sep.getY1();
            int sy2 = sep.getY2();
            for (Iterator<Area> it = areas.iterator(); it.hasNext();)
            {
                Area area = it.next();
                //the separator width must cover the whole area
                if (sep.getX1() <= area.getX1() && sep.getX2() >= area.getX2())
                {
                    int ay1 = area.getY1();
                    int ay2 = area.getY2();
                    //the separator covers the area -- remove the area 
                    if (sy1 <= ay1 && sy2 >= ay2)
                    {
                        it.remove();
                    }
                    //separator entirely inside -- split the area 
                    else if (sy1 > ay1 && sy2 < ay2)
                    {
                        Area newarea = new Area(area.getX1(), sy1,
                                                            area.getX2(), area.getY2());
                        newareas.add(newarea);
                        area.getBounds().setY2(sy1 - 1);
                    }
                    //separator partially covers the area -- update the area
                    else if ((sy1 > ay1 && sy1 < ay2) && sy2 >= ay2)
                    {
                        area.getBounds().setY2(sy1 - 1);
                    }
                    //separator partially covers the area -- update the area
                    else if (sy1 <= ay1 && (sy2 > ay1 && sy2 < ay2))
                    {
                        area.getBounds().setY1(sy1);
                    }
                }
            }
        }
        else //vertical separator
        {
            int sx1 = sep.getX1();
            int sx2 = sep.getX2();
            for (Iterator<Area> it = areas.iterator(); it.hasNext();)
            {
                Area area = it.next();
                //the separator height must cover the whole area
                if (sep.getY1() <= area.getY1() && sep.getY2() >= area.getY2())
                {
                    int ax1 = area.getX1();
                    int ax2 = area.getX2();
                    //the separator covers the area -- remove the area 
                    if (sx1 <= ax1 && sx2 >= ax2)
                    {
                        it.remove();
                    }
                    //separator entirely inside -- split the area 
                    else if (sx1 > ax1 && sx2 < ax2)
                    {
                        Area newarea = new Area(sx1, area.getY1(),
                                                            area.getX2(), area.getY2());
                        newareas.add(newarea);
                        area.getBounds().setX2(sx1 - 1);
                    }
                    //separator partially covers the area -- update the area
                    else if ((sx1 > ax1 && sx1 < ax2) && sx2 >= ax2)
                    {
                        area.getBounds().setX2(sx1 - 1);
                    }
                    //separator partially covers the area -- update the area
                    else if (sx1 <= ax1 && (sx2 > ax1 && sx2 < ax2))
                    {
                        area.getBounds().setX1(sx1);
                    }
                }
            }
        }
        areas.addAll(newareas);
    }
    
    
    /**
     * Creates new "virtual" visual areas based on detected separators. These areas are further used for detecting more separators.
     * @param an optional filtering area; only the new sub-areas within this area are considered
     * @return a vector of created visual areas
     */
    private Vector<Area> createAreas(Area filter)
    {
        Area base = (filter == null) ? root.getArea() : filter;
        
        Vector<Area> areas = new Vector<Area>();
        Area init = new Area(base.getX1(), base.getY1(), base.getX2(), base.getY2());
        areas.add(init);
        for (Separator sep : hsep)
            considerSeparator(areas, sep, true);
        for (Separator sep : vsep)
            considerSeparator(areas, sep, false);
        for (Separator sep : bsep)
            considerThinSeparator(areas, sep, sep.getType() == Separator.BOXH);
        return areas;
    }
        
}
