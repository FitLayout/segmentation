/**
 * VisualAreaNode.java
 *
 * Created on 28.6.2006, 15:13:48 by burgetr
 */
package org.fit.segm.grouping;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.layout.impl.Area;
import org.fit.layout.impl.AreaNode;
import org.fit.layout.impl.BoxNode;
import org.fit.layout.impl.Tag;
import org.fit.layout.model.Rectangular;

/**
 * A node in the area tree. The nested areas are laid out in a grid.
 * TODO rename to GroupingAreaNode or so?
 * 
 * @author burgetr
 */
public class GroupingAreaNode extends AreaNode
{
    private static final long serialVersionUID = 3931378515695196845L;

    /** Estimated layout type */
    private LayoutType layoutType;
    
    /** Position in the grid */
    private Rectangular gp;
    
    /** Set of separators */
    private SeparatorSet seps;
    
    /** Analyzer for finding the super areas */
    private GroupAnalyzer groups;
    
    /** Explicitely separated area */
    private boolean separated;
    
    /** Node importance computed from headings */
    private double importance = 0;
    
    /** Area markedness computed from multiple features */
    private double markedness = 0;
    
    /** True if the markedness should not be recomputed anymore (it's fixed) */
    private boolean fixedMarkedness = false;
    
    /** True if the node should not be reordered inside */
    private boolean atomic = false;
    
    /** Previous box on the same line */
    private GroupingAreaNode previousOnLine = null;
    
    /** Next box on the same line */
    private GroupingAreaNode nextOnLine = null;
    
    /** The level of the most probable assigned tag (-1 means not computed yet) */
    private int taglevel = -1;
    

    //====================================================================================

    /**
     * Creates an area node from an area.
     * @param area The area to be contained in this node
     */
    public GroupingAreaNode(Area area)
    {
        super(area);
        layoutType = LayoutType.NORMAL;
    }

    public String toString()
    {
    	if (getArea() != null)
    	{
    		String ret = gp.toString();
    		/*if (grid != null)
    			ret += " {" + grid.getWidth() + "x" + grid.getHeight() + "}";
            if (backgroundSeparated())
                ret += "B";*/
    		//ret += "{" + getAverageImportance() + "/" + getTotalImportance() + "}";
    		//ret += "{" + getArea().getExpressiveness() + "} ";
    		return ret + getArea().toString();
    	}
    	else
    		return "(area)";
    }

    //====================================================================================
    
    public LayoutType getLayoutType()
    {
        return layoutType;
    }

    public void setLayoutType(LayoutType layoutType)
    {
        this.layoutType = layoutType;
    }

    /**
     * Creates a set of the horizontal and vertical separators
     */
    public void createSeparators()
    {
    	seps = Config.createSeparators(this);
    }
    
    /**
     * @return the set of separators in this area
     */
    public SeparatorSet getSeparators()
    {
    	return seps;
    }
    
    /**
     * When set to true, the area is considered to be separated from other
     * areas explicitely, i.e. independently on its real borders or background.
     * This is usually used for some new superareas.
     * @return <code>true</code>, if the area is explicitely separated
     */
    public boolean explicitelySeparated()
    {
        return separated;
    }

    /**
     * When set to true, the area is considered to be separated from other
     * areas explicitely, i.e. independently on its real borders or background.
     * This is usually used for some new superareas.
     * @param separated <code>true</code>, if the area should be explicitely separated
     */
    public void setSeparated(boolean separated)
    {
        this.separated = separated;
    }

    /**
     * Obtains the overall style of the area.
     * @return the area style
     */
    public AreaStyle getStyle()
    {
        return new AreaStyle(this);
    }
    
    /**
     * @return the importance
     */
    public double getImportance()
    {
        return importance;
    }

    /**
     * @param importance the importance to set
     */
    public void setImportance(double importance)
    {
        this.importance = importance;
    }

    /**
     * @return the markedness
     */
    public double getMarkedness()
    {
        return markedness;
    }

    /**
     * @param markedness the markedness to set
     */
    public void setMarkedness(double markedness)
    {
        this.markedness = markedness;
    }

    public void setFixedMarkedness(double markedness)
    {
        this.markedness = markedness;
        this.fixedMarkedness = true;
    }
    
    public boolean hasFixedMarkedness()
    {
        return fixedMarkedness;
    }
    
    /**
     * Computes the sum of the importance of all the descendant areas
     */
    public double getTotalImportance()
    {
        double ret = importance;
        for (int i = 0; i < getChildCount(); i++)
            ret += ((GroupingAreaNode) getChildArea(i)).getTotalImportance();
        return ret;
    }
    
    /**
     * Computes the average importance of all the descendant areas
     */
    public double getAverageImportance()
    {
        double ret = 0;
        int cnt = 0;
        for (int i = 0; i < getChildCount(); i++)
        {
            double imp = ((GroupingAreaNode) getChildArea(i)).getAverageImportance();
            if (imp > 0)
            {
                ret += imp;
                cnt++;
            }
        }
        if (cnt > 0) ret = ret / cnt;
        return importance + ret;
    }
    
    /**
     * @return true, if the node is atomic and it should not be reordered
     */
    public boolean isAtomic()
    {
        return atomic;
    }

    /**
     * @param atomic when set to true, the node is marked as atomic and it won't be reordered
     */
    public void setAtomic(boolean atomic)
    {
        this.atomic = atomic;
    }

    /** Obtains all the text from the area and its child areas */
    public String getText()
    {
        String ret = "";
        if (isLeaf())
            ret = getArea().getBoxText();
        else
            for (int i = 0; i < getChildCount(); i++)
                ret += ((GroupingAreaNode) getChildArea(i)).getText();
        return ret;
    }

    /** 
     * Obtains all the boxes from this area and all the child areas.
     * @return The list of boxes
     */
    public List<BoxNode> getAllBoxes()
    {
        Vector<BoxNode> ret = new Vector<BoxNode>();
        recursiveFindBoxes(this, ret);
        return ret;
    }
    
    private void recursiveFindBoxes(GroupingAreaNode root, Vector<BoxNode> result)
    {
        result.addAll(root.getArea().getBoxes());
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveFindBoxes((GroupingAreaNode) root.getChildArea(i), result);
    }
    
    /**
     * Obtains the first box nested in this area or any subarea
     * @return the box node or <code>null</code> if there are no nested boxes
     */
    public BoxNode getFirstNestedBox()
    {
        if (isLeaf())
        {
            Vector<BoxNode> boxes = getArea().getBoxes();
            if (!boxes.isEmpty())
                return boxes.firstElement();
            else
                return null;
        }
        else
            return ((GroupingAreaNode) getChildArea(0)).getFirstNestedBox();
    }
    
    /** Computes the efficient background color by considering the parents if necessary */
    public Color getEffectiveBackgroundColor()
    {
        if (getArea().getBackgroundColor() != null)
            return getArea().getBackgroundColor();
        else
        {
            if (getParentArea() != null)
                return ((GroupingAreaNode) getParentArea()).getEffectiveBackgroundColor();
            else
                return Color.WHITE; //use white as the default root color
        }
    }

    public GroupingAreaNode getPreviousOnLine()
	{
		return previousOnLine;
	}

	public void setPreviousOnLine(GroupingAreaNode previousOnLine)
	{
		this.previousOnLine = previousOnLine;
	}

	public GroupingAreaNode getNextOnLine()
	{
		return nextOnLine;
	}

	public void setNextOnLine(GroupingAreaNode nextOnLine)
	{
		this.nextOnLine = nextOnLine;
	}
    
    /**
     * Obtains the level of the most probable tag assigned to the area. This value is computed from outsied,
     * usually by the AreaTree from the tag predictor and search tree.
     * @return
     */
    public int getTagLevel()
    {
        return taglevel;
    }

    /**
     * Sets the level of the most probable tag assigned to the area.
     * @param taglevel
     */
    public void setTagLevel(int taglevel)
    {
        this.taglevel = taglevel;
    }

    /**
     * Looks for the most important leaf child (with the greatest markedness) of this area which is tagged with the given tag.
     * @param tag The required tag.
     * @return The most important leaf child area with that tag or <code>null</code> if there are no children with this tag.
     */
    public GroupingAreaNode getMostImportantLeaf(Tag tag)
    {
        Enumeration<?> e = depthFirstEnumeration();
        GroupingAreaNode best = null;
        double bestMarkedness = -1;
        while (e.hasMoreElements())
        {
            GroupingAreaNode node = (GroupingAreaNode) e.nextElement();
            if (node.isLeaf() && node.hasTag(tag) && node.getMarkedness() > bestMarkedness)
            {
                bestMarkedness = node.getMarkedness();
                best = node;
            }
        }
        return best;
    }
    
    //====================================================================================
    
    /**
     * Join this area with another area and update the layout in the grid to the given values.
     * @param other The area to be joined to this area
     * @param pos The position of the result in the grid
     * @param horizontal Horizontal or vertical join?
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public void joinArea(GroupingAreaNode other, Rectangular pos, boolean horizontal)
    {
    	gp = pos;
    	if (other.children != null)
    	{
	    	Vector adopt = new Vector(other.children);
	    	for (Iterator it = adopt.iterator(); it.hasNext();)
	    		add((GroupingAreaNode) it.next());
    	}
    	getArea().join(other.getArea(), horizontal);
    	tags.addAll(other.getTags());
    }
    
    //====================================================================================
    
    /**
     * Creates a new subarea from a specified region of the area and moves the selected child
     * nodes to the new area.
     * @param gp the subarea bounds
     * @param selected nodes to be moved to the new area
     * @param name the name (identification) of the new area
     * @return the new AreaNode created in the tree or null, if nothing was created
     */ 
    public GroupingAreaNode createSuperArea(Rectangular gp, Vector<GroupingAreaNode> selected, String name)
    {
        if (getChildCount() > 1 && selected.size() > 1 && selected.size() != getChildCount())
        {
            //create the new area
	        Area area = new Area(getArea().getX1() + grid.getColOfs(gp.getX1()),
	                             getArea().getY1() + grid.getRowOfs(gp.getY1()),
	                             getArea().getX1() + grid.getColOfs(gp.getX2()+1) - 1,
	                             getArea().getY1() + grid.getRowOfs(gp.getY2()+1) - 1);
	        area.setName(name);
        	GroupingAreaNode grp = new GroupingAreaNode(area);
        	int index = getIndex(selected.firstElement());
            insert(grp, index);
        	grp.addAll(selected);
            grp.createGrid();
            createGrid();
            return grp;
        }
        else
            return null;
    }
    
    @SuppressWarnings("unused")
    public void debugAreas(GroupingAreaNode sub)
    {
        if (Config.DEBUG_AREAS && sub.getChildCount() == 0)
        {
            if (groups == null)
                groups = Config.createGroupAnalyzer(this);
            Vector<GroupingAreaNode> inside = new Vector<GroupingAreaNode>();
            createSeparators();
            groups.findSuperArea(sub, inside);
        }
        //joinAreas();
    }
    
    //====================================================================================
    
    public void collapseSubtree()
    {
        //System.out.println("Collapsing: " + toString());
        recursiveCollapseSubtree(this);
        removeAllChildren();
        //System.out.println("Result: " + getText());
    }
    
    private void recursiveCollapseSubtree(GroupingAreaNode dest)
    {
        for (int i = 0; i < getChildCount(); i++)
        {
            GroupingAreaNode child = getChildArea(i);
            child.recursiveCollapseSubtree(dest);
            dest.getArea().joinChild(child.getArea());
        }
    }
    
    //====================================================================================
    
    /**
     * Removes simple separators from current separator set. A simple separator
     * has only one or zero visual areas at each side
     */
    public void removeSimpleSeparators()
    {
    	/*Vector<Separator> hs = seps.getHorizontal();
    	if (hs.size() > 0)
    	{
    		Separator sep = hs.firstElement();
    		System.out.println("filter: " + sep);
    		System.out.println("GX=" + grid.findCellX(sep.getX1()+1));
    		System.out.println("GY=" + grid.findCellY(sep.getY1()+1));
    		System.out.println("A=" + countAreasAbove(sep));
    	}*/
    	removeSimpleSeparators(seps.getHorizontal());
    	removeSimpleSeparators(seps.getVertical());
    	removeSimpleSeparators(seps.getBoxsep());
    }
    
    /**
     * Removes simple separators from a vector of separators. A simple separator
     * has only one or zero visual areas at each side
     */
    private void removeSimpleSeparators(Vector<Separator> v)
    {
        //System.out.println("Rem: this="+this);
    	for (Iterator<Separator> it = v.iterator(); it.hasNext();)
		{
			Separator sep = it.next();
			if (sep.getType() == Separator.HORIZONTAL || sep.getType() == Separator.BOXH)
			{
				int a = countAreasAbove(sep);
				int b = countAreasBelow(sep);
				if (a <= 1 && b <= 1)
					it.remove();
			}
			else
			{
				int a = countAreasLeft(sep);
				int b = countAreasRight(sep);
				if (a <= 1 && b <= 1)
					it.remove();
			}
		}
    }
    
    /**
     * @return the number of the areas directly above the separator
     */
    private int countAreasAbove(Separator sep)
    {
    	int gx1 = grid.findCellX(sep.getX1());
    	int gx2 = grid.findCellX(sep.getX2());
    	int gy = grid.findCellY(sep.getY1() - 1);
    	int ret = 0;
    	if (gx1 >= 0 && gx2 >= 0 && gy >= 0)
    	{
    		int i = gx1;
    		while (i <= gx2)
    		{
    			GroupingAreaNode node = grid.getNodeAt(i, gy);
    			//System.out.println("Search: " + i + ":" + gy + " = " + node);
    			if (node != null)
    			{
    				ret++;
    				i += node.getGridWidth();
    			}
    			else
    				i++;
    		}
    	}
    	return ret;
    }
    
    /**
     * @return the number of the areas directly below the separator
     */
    private int countAreasBelow(Separator sep)
    {
    	int gx1 = grid.findCellX(sep.getX1());
    	int gx2 = grid.findCellX(sep.getX2());
    	int gy = grid.findCellY(sep.getY2() + 1);
    	int ret = 0;
    	if (gx1 >= 0 && gx2 >= 0 && gy >= 0)
    	{
    		int i = gx1;
    		while (i <= gx2)
    		{
    			GroupingAreaNode node = grid.getNodeAt(i, gy);
    			//System.out.println("Search: " + i + ":" + gy + " = " + node);
    			if (node != null)
    			{
    				ret++;
    				i += node.getGridWidth();
    			}
    			else
    				i++;
    		}
    	}
    	return ret;
    }
    
    /**
     * @return the number of the areas directly on the left of the separator
     */
    private int countAreasLeft(Separator sep)
    {
    	int gy1 = grid.findCellY(sep.getY1());
    	int gy2 = grid.findCellY(sep.getY2());
    	int gx = grid.findCellX(sep.getX1() - 1);
    	int ret = 0;
    	if (gy1 >= 0 && gy2 >= 0 && gx >= 0)
    	{
    		int i = gy1;
    		while (i <= gy2)
    		{
    			GroupingAreaNode node = grid.getNodeAt(gx, i);
    			if (node != null)
    			{
    				ret++;
    				i += node.getGridWidth();
    			}
    			else
    				i++;
    		}
    	}
    	return ret;
    }
    
    /**
     * @return the number of the areas directly on the left of the separator
     */
    private int countAreasRight(Separator sep)
    {
    	int gy1 = grid.findCellY(sep.getY1());
    	int gy2 = grid.findCellY(sep.getY2());
    	int gx = grid.findCellX(sep.getX2() + 1);
    	int ret = 0;
    	if (gy1 >= 0 && gy2 >= 0 && gx >= 0)
    	{
    		int i = gy1;
    		while (i <= gy2)
    		{
    			GroupingAreaNode node = grid.getNodeAt(gx, i);
    			if (node != null)
    			{
    				ret++;
    				i += node.getGridWidth();
    			}
    			else
    				i++;
    		}
    	}
    	return ret;
    }
    
    /**
     * Looks for the nearest text box area placed above the separator. If there are more
     * such areas in the same distance, the leftmost one is returned.
     * @param sep the separator 
     * @return the leaf area containing the box or <code>null</code> if there is nothing above the separator
     */
    public GroupingAreaNode findContentAbove(Separator sep)
    {
        return recursiveFindAreaAbove(sep.getX1(), sep.getX2(), 0, sep.getY1());
    }
    
    private GroupingAreaNode recursiveFindAreaAbove(int x1, int x2, int y1, int y2)
    {
        GroupingAreaNode ret = null;
        int maxx = x2;
        int miny = y1;
        Vector <BoxNode> boxes = getArea().getBoxes();
        for (BoxNode box : boxes)
        {
            int bx = box.getBounds().getX1();
            int by = box.getBounds().getX2();
            if ((bx >= x1 && bx <= x2 && by < y2) &&  //is placed above
                    (by > miny ||
                     (by == miny && bx < maxx)))
            {
                ret = this; //found in our boxes
                if (bx < maxx) maxx = bx;
                if (by > miny) miny = by;
            }
        }

        for (int i = 0; i < getChildCount(); i++)
        {
            GroupingAreaNode child = getChildArea(i);
            GroupingAreaNode area = child.recursiveFindAreaAbove(x1, x2, miny, y2);
            if (area != null)
            {   
                int bx = area.getX(); 
                int by = area.getY2();
                int len = area.getText().length();
                if ((len > 0) && //we require some text in the area
                        (by > miny ||
                         (by == miny && bx < maxx)))
                {
                    ret = area;
                    if (bx < maxx) maxx = bx;
                    if (by > miny) miny = by;
                }
            }
        }
        
        return ret;
    }

    //====================================================================================
    
    /**
     * Computes the total square area occupied by the area contents.
     * @return the total square area of the contents in pixels
     */
    public int getContentSquareArea()
    {
        int ret = 0;
        for (int i = 0; i < getChildCount(); i++)
            ret += getChildArea(i).getArea().getSquareArea();
        return ret;
    }
    
    /**
     * Computes the percentage of the contents of the parent area occupied by this area.
     * @return the percentage of this area in the parent area's contents (0..1)
     */
    public double getParentPercentage()
    {
        GroupingAreaNode parent = getParentArea();
        if (parent != null)
            return (double) getArea().getSquareArea() / parent.getContentSquareArea();
        else
            return 0;
    }
    
    /**
     * Checks whether all the child nodes have a coherent style.
     * @return <code>true</code> if none of the children has much different style from the average.
     */
    public boolean isCoherent()
    {
        double afs = getArea().getAverageFontSize();
        double aw = getArea().getAverageFontWeight();
        double as = getArea().getAverageFontStyle();
        for (int i = 0; i < getChildCount(); i++)
        {
            Area child = getChildArea(i).getArea();
            double fsdif = Math.abs(child.getAverageFontSize() - afs) * 2; //allow 1/2 difference only (counting with averages)
            double wdif = Math.abs(child.getAverageFontWeight() - aw) * 2;
            double sdif = Math.abs(child.getAverageFontStyle() - as) * 2;
            if (fsdif > Config.FONT_SIZE_THRESHOLD 
                || wdif > Config.FONT_WEIGHT_THRESHOLD
                || sdif > Config.FONT_STYLE_THRESHOLD)
                return false;
            //TODO: doplnit barvy, viz hasSameStyle
        }
        return true;
    }
    
    /**
     * Compares two areas and decides whether they have the same style. The thresholds of the style are taken from the {@link Config}.
     * @param other the other area to be compared
     * @return <code>true</code> if the areas are considered to have the same style
     */
    public boolean hasSameStyle(GroupingAreaNode other)
    {
        return getStyle().isSameStyle(other.getStyle());
    }
    
    /**
     * @return <code>true</code> if the area is visually separated from its parent by
     * a different background color
     */
    public boolean isBackgroundSeparated()
    {
        return getArea().isBackgroundSeparated();
    }

    /**
     * @return <code>true<code> if the area is separated from the areas below it
     */
    public boolean separatedDown()
    {
        return getArea().hasBottomBorder() || isBackgroundSeparated();
    }
    
    /**
     * @return <code>true<code> if the area is separated from the areas above it
     */
    public boolean separatedUp()
    {
        return getArea().hasTopBorder() || isBackgroundSeparated();
    }
    
    /**
     * @return <code>true<code> if the area is separated from the areas on the left
     */
    public boolean separatedLeft()
    {
        return getArea().hasLeftBorder() || isBackgroundSeparated();
    }
    
    /**
     * @return <code>true<code> if the area is separated from the areas on the right
     */
    public boolean separatedRight()
    {
        return getArea().hasRightBorder() || isBackgroundSeparated();
    }

    /**
     * Checks whether the area is horizontally centered within its parent area
     * @return <code>true</code> if the area is centered
     */
    public boolean isCentered()
    {
        return isCentered(true, true) == 1;
    }
    
    /**
     * Tries to guess whether the area is horizontally centered within its parent area 
     * @param askBefore may we compare the alignment with the preceding siblings?
     * @param askAfter may we compare the alignment with the following siblings?
     * @return 0 when certailny not centered, 1 when certainly centered, 2 when not sure (nothing to compare with and no margins around)
     */
    private int isCentered(boolean askBefore, boolean askAfter)
    {
        GroupingAreaNode parent = getParentArea();
        if (parent != null)
        {
            int left = getX() - parent.getX();
            int right = parent.getX2() - getX2();
            int limit = (int) (((left + right) / 2.0) * Config.CENTERING_THRESHOLD);
            if (limit == 0) limit = 1; //we always allow +-1px
            //System.out.println(this + " left=" + left + " right=" + right + " limit=" + limit);
            boolean middle = Math.abs(left - right) <= limit; //first guess - check if it is placed in the middle
            boolean fullwidth = left == 0 && right == 0; //centered because of full width
            
            if (!middle && !fullwidth) //not full width and certainly not in the middle
            {
                return 0; 
            }
            else //may be centered - check the alignment
            {
                //compare the alignent with the previous and/or the next child
                GroupingAreaNode prev = null;
                GroupingAreaNode next = null;
                int pc = 2; //previous centered?
                int nc = 2; //next cenrered?
                if (askBefore || askAfter)
                {
                    if (askBefore)
                    {
                        prev = (GroupingAreaNode) getPreviousSibling();
                        while (prev != null && (pc = prev.isCentered(true, false)) == 2)
                            prev = (GroupingAreaNode) prev.getPreviousSibling();
                    }
                    if (askAfter)
                    {
                        next = (GroupingAreaNode) getNextSibling();
                        while (next != null && (nc = next.isCentered(false, true)) == 2)
                            next = (GroupingAreaNode) next.getNextSibling();
                    }
                }
                
                if (pc != 2 || nc != 2) //we have something for comparison
                {
                    if (fullwidth) //cannot guess, compare with others
                    {
                        if (pc != 0 && nc != 0) //something around is centered - probably centered
                            return 1;
                        else
                            return 0;
                    }
                    else //probably centered, if it is not left- or right-aligned with something around
                    {
                        if (prev != null && lrAligned(this, prev) == 1 ||
                            next != null && lrAligned(this, next) == 1)
                            return 0; //aligned, not centered
                        else
                            return 1; //probably centered
                    }
                }
                else //nothing to compare, just guess
                {
                    if (fullwidth)
                        return 2; //cannot guess from anything
                    else
                        return (middle ? 1 : 0); //nothing to compare with - guess from the position
                }
            }
        }
        else
            return 2; //no parent - we don't know
    }
    
    /**
     * Checks if the areas are left- or right-aligned.
     * @return 0 if not, 1 if yes, 2 if both left and right
     */
    private int lrAligned(GroupingAreaNode a1, GroupingAreaNode a2)
    {
        if (a1.getX() == a2.getX())
            return (a1.getX2() == a2.getX2()) ? 2 : 1;
        else if (a1.getX2() == a2.getX2())
            return 1;
        else
            return 0;
    }
    
    //======================================================================================
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void sortChildren(Comparator<GroupingAreaNode> comparator)
    {
    	for (Enumeration e = children(); e.hasMoreElements(); )
    	{
    		GroupingAreaNode chld = (GroupingAreaNode) e.nextElement();
    		if (!chld.isAtomic())
    		    chld.sortChildren(comparator);
    	}
    	if (children != null)
    		java.util.Collections.sort(children, comparator);
    }
    
    //======================================================================================
    
    public void drawGrid(BrowserCanvas canvas)
    {
        Graphics ig = canvas.getImageGraphics();
        Color c = ig.getColor();
        ig.setColor(Color.BLUE);
        int xo = getArea().getX1();
        for (int i = 1; i <= grid.getWidth(); i++)
        {
            xo += grid.getCols()[i-1];
            /*System.out.println(i + " : " + xo);
            if (i == 42) ig.setColor(Color.GREEN);
            else if (i == 47) ig.setColor(Color.RED);
            else ig.setColor(Color.BLUE);*/
            ig.drawLine(xo, getArea().getY1(), xo, getArea().getY2());
        }
        int yo = getArea().getY1();
        for (int i = 0; i < grid.getHeight(); i++)
        {
            yo += grid.getRows()[i];
            ig.drawLine(getArea().getX1(), yo, getArea().getX2(), yo);
        }
        ig.setColor(c);
    }

    //====================================================================================
    
    public enum LayoutType 
    { 
        /** Normal flow */
        NORMAL("normal"),
        /** Tabular layout */
        TABLE("table"),
        /** A simple list */
        LIST("list");
        
        private String name;
        
        private LayoutType(String name)
        {
            this.name = name;
        }
        
        public String toString()
        {
            return name;
        }
    } 
    
}
