/**
 * VisualArea.java
 *
 * Created on 3-�en-06, 10:58:23  by radek
 */
package org.fit.segm.grouping;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.fit.layout.impl.DefaultArea;
import org.fit.layout.impl.GenericTreeNode;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.Box.Type;
import org.fit.layout.model.ContentObject;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.segm.grouping.op.Separator;
import org.fit.segm.grouping.op.SeparatorSet;

/**
 * An area containing several visual boxes.
 * 
 * @author radek
 */
public class AreaImpl extends DefaultArea implements Area
{
    /** Set of separators */
    private SeparatorSet seps;
    
    /**
     * Area level. 0 corresponds to the areas formed by boxes, greater numbers represent
     * greater level of grouping
     */
    private int level = 0;
    
    /**
     * Explicitly separated area
     */
    private boolean separated;
    
    /**
     * Sum for computing the average font size
     */
    private float fontSizeSum;
    
    /**
     * Counter for computing the average font size
     */
    private int fontSizeCnt;
    
    private float fontWeightSum;
    private int fontWeightCnt;
    private float fontStyleSum;
    private int fontStyleCnt;
    private float underlineSum;
    private int underlineCnt;
    private float lineThroughSum;
    private int lineThroughCnt;
    
	//================================================================================
	
    /** 
     * Creates an empty area of a given size
     */
    public AreaImpl(int x1, int y1, int x2, int y2)
	{
        this(new Rectangular(x1, y1, x2, y2));
	}
    
    /** 
     * Creates an empty area of a given size
     */
    public AreaImpl(Rectangular r)
    {
        super(r);
    }
    
    /** 
     * Creates an area from a single box. Update the area bounds and name accordingly.
     * @param box The source box that will be contained in this area
     */
    public AreaImpl(Box box)
    {
        super(box);
    }
    
    /** 
     * Creates an area from a a list of boxes. Update the area bounds and name accordingly.
     * @param boxList The source boxes that will be contained in this area
     */
    public AreaImpl(Vector<Box> boxList)
    {
        super(boxList);
    }
    
    /** 
     * Creates a copy of another area.
     * @param src The source area
     */
    public AreaImpl(AreaImpl src)
    {
        super(src);
        level = src.level;
        fontSizeSum = src.fontSizeSum;
        fontSizeCnt = src.fontStyleCnt;
        fontStyleSum = src.fontStyleSum;
        fontStyleCnt = src.fontStyleCnt;
        fontWeightSum = src.fontWeightSum;
        fontWeightCnt = src.fontWeightCnt;
        underlineCnt = src.underlineCnt;
        underlineSum = src.underlineSum;
        lineThroughCnt = src.lineThroughCnt;
        lineThroughSum = src.lineThroughSum;
    }
    
    @Override
    public void appendChild(Area child)
    {
        super.appendChild(child);
        updateAverages((AreaImpl) child);
    }
    
    @Override
    public void appendChildren(List<Area> list)
    {
        for (Area child : list)
            appendChild(child);
    }
    
    @Override
    public void removeAllChildren()
    {
        super.removeAllChildren();
        resetAverages();
    }

    /**
     * Joins this area with another area and updates the layout in the grid to the given values.
     * Moves the children of the other areas to this area.
     * @param other The area to be joined to this area
     * @param pos The position of the result in the grid
     * @param horizontal Horizontal or vertical join?
     */
    //@SuppressWarnings({ "rawtypes", "unchecked" })
    public void joinArea(AreaImpl other, Rectangular pos, boolean horizontal)
    {
        setGridPosition(pos);
        if (other.getChildCount() > 0)
        {
            Vector<GenericTreeNode> adopt = new Vector<GenericTreeNode>(other.getChildren());
            for (Iterator<GenericTreeNode> it = adopt.iterator(); it.hasNext();)
                appendChild((AreaImpl) it.next());
        }
        join(other, horizontal);
        //copy the tag while preserving the higher support //TODO is this corect?
        for (Map.Entry<Tag, Float> entry : other.getTags().entrySet())
        {
            if (!getTags().containsKey(entry.getKey()) || entry.getValue() > getTags().get(entry.getKey()))
                getTags().put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Joins another area to this area. Update the bounds and the name accordingly.
     * @param other The area to be joined to this area.
     * @param horizontal If true, the areas are joined horizontally.
     * This influences the resulting area borders. If false, the areas are joined vertically.
     */
    public void join(AreaImpl other, boolean horizontal)
    {
    	getBounds().expandToEnclose(other.getBounds());
    	setName(getName() + " . " + other.getName());
        //update border information according to the mutual area positions
        if (horizontal)
        {
            if (getX1() <= other.getX1())
            {
                if (other.hasRightBorder())
                    setRightBorder(other.getRightBorder());
            }
            else
            {
                if (other.hasLeftBorder())
                    setLeftBorder(other.getLeftBorder());
            }
        }
        else
        {
            if (getY1() <= other.getY1())
            {
                if (other.hasBottomBorder())
                    setBottomBorder(other.getBottomBorder());
            }
            else
            {
                if (other.hasTopBorder())
                    setTopBorder(other.getTopBorder());
            }
        }
        //add all the contained boxes
        getBoxes().addAll(other.getBoxes());
        updateAverages(other);
        //just a test
        if (!this.hasSameBackground(other))
        	System.err.println("Area: Warning: joining areas " + getName() + " and " + other.getName() + 
        	        " of different background colors " + this.getBackgroundColor() + " x " + other.getBackgroundColor()); 
    }
    
    /**
     * Joins a child area to this area. Updates the bounds and the name accordingly.
     * @param other The child area to be joined to this area.
     */
    public void joinChild(AreaImpl other)
    {
        for (Box box : other.getBoxes())
            addBox(box);
        getBounds().expandToEnclose(other.getBounds());
        setName(getName() + " . " + other.getName());
    }
    
	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public String toString()
    {
        String bs = "";
        //bs += "{" + getAverageFontSize() + "=" + fontSizeSum + "/" + fontSizeCnt + "}"; 
        /*    + ":" + getAverageFontWeight() 
            + ":" + getAverageFontStyle() + "}";*/
        
        if (hasTopBorder()) bs += "^";
        if (hasLeftBorder()) bs += "<";
        if (hasRightBorder()) bs += ">";
        if (hasBottomBorder()) bs += "_";
        if (isBackgroundSeparated()) bs += "*";
        
        /*if (isHorizontalSeparator()) bs += "H";
        if (isVerticalSeparator()) bs += "I";*/
        
        /*if (getBackgroundColor() != null)
            bs += "\"" + String.format("#%02x%02x%02x", getBackgroundColor().getRed(), getBackgroundColor().getGreen(), getBackgroundColor().getBlue()) + "\"";*/
        
        if (getName() != null)
            return bs + " " + getName() + " " + getBounds().toString();
        else
            return bs + " " + "<area> " + getBounds().toString();
          
    }
    
    /**
     * Add the box node to the area if its bounds are inside of the area bounds.
     * @param node The box node to be added
     */
    public void chooseBox(Box node)
    {
    	if (getBounds().encloses(node.getVisualBounds()))
    		addBox(node);
    }
    
	//=================================================================================
	
    /**
     * Checks if this area has the same background color as another area
     * @param other the other area
     * @return true if the areas are both transparent or they have the same
     * background color declared
     */
    public boolean hasSameBackground(AreaImpl other)
    {
        return (getBackgroundColor() == null && other.getBackgroundColor() == null) || 
               (getBackgroundColor() != null && other.getBackgroundColor() != null && getBackgroundColor().equals(other.getBackgroundColor()));
    }
    
    public boolean encloses(AreaImpl other)
    {
    	return getBounds().encloses(other.getBounds());
    }
    
    public boolean contains(int x, int y)
    {
    	return getBounds().contains(x, y);
    }
    
    public boolean hasContent()
    {
        return !getBoxes().isEmpty();
    }
    
    @Override
    public Color getEffectiveBackgroundColor()
    {
        if (getBackgroundColor() != null)
            return getBackgroundColor();
        else
        {
            if (getParentArea() != null)
                return getParentArea().getEffectiveBackgroundColor();
            else
                return Color.WHITE; //use white as the default root color
        }
    }
    
    //======================================================================================
    
    /**
     * @return true if the area contains any text
     */
    public boolean containsText()
    {
        for (Box root : getBoxes())
        {
            if (recursiveContainsText(root))
                return true;
        }
        return false;
    }
    
    private boolean recursiveContainsText(Box root)
    {
        if (root.getChildCount() == 0)
        {
            return root.getText().trim().length() > 0;
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                if (recursiveContainsText(root.getChildBox(i)))
                    return true;
            return false;
        }
    }
    
    /**
     * @return true if the area contains replaced boxes only
     */
    @Override
    public boolean isReplaced()
    {
        boolean empty = true;
        for (Box root : getBoxes())
        {
            empty = false;
            if (root.getType() != Box.Type.REPLACED_CONTENT)
                return false;
        }
        return !empty;
    }
    
    /**
     * Returns the text string represented by a concatenation of all
     * the boxes contained directly in this area (no subareas)
     */
    public String getBoxText()
    {
        StringBuilder ret = new StringBuilder();
        boolean start = true;
        for (Iterator<Box> it = getBoxes().iterator(); it.hasNext(); )
        {
            if (!start) ret.append(' ');
            else start = false;
            ret.append(it.next().getText());
        }
        return ret.toString();
    }
    
    /**
     * Returns the text string represented by a concatenation of all
     * the boxes contained directly in this area.
     */
    public int getTextLength()
    {
        int ret = 0;
        for (Box box : getBoxes())
        {
            ret += box.getText().length();
        }
        return ret;
    }
    
    /**
     * @return true if the area contains any text
     */
	public ContentObject getReplacedContent()
    {
        for (Iterator<Box> it = getBoxes().iterator(); it.hasNext(); )
        {
            ContentObject obj = recursiveGetReplacedContent(it.next());
            if (obj != null)
                return obj;
        }
        return null;
    }
    
    private ContentObject recursiveGetReplacedContent(Box root)
    {
        if (root.getChildCount() == 0)
        {
            return root.getContentObject();
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                ContentObject obj = recursiveGetReplacedContent(root.getChildBox(i));
                if (obj != null)
                    return obj;
            }
            return null;
        }
    }
    
    /**
     * Tries to guess if this area acts as a horizontal separator. The criteria are:
     * <ul>
     * <li>It doesn't contain any text</li>
     * <li>It is visible</li>
     * <li>It is low and wide</li>
     * </ul>
     * @return true if the area can be used as a horizontal separator
     */
    @Override
    public boolean isHorizontalSeparator()
    {
        return getBounds().getHeight() < 10
               && getBounds().getWidth() > 20 * getBounds().getHeight()
               && (separatedUp() || separatedDown())
               && !containsText();
    }
    
    /**
     * Tries to guess if this area acts as a vertical separator. The criteria are the same
     * as for the horizontal one.
     * @return true if the area can be used as a vertical separator
     */
    @Override
    public boolean isVerticalSeparator()
    {
        return getBounds().getWidth() < 10
               && getBounds().getHeight() > 20 * getBounds().getWidth()
               && (separatedLeft() || separatedRight())
               && !containsText();
    }
    
    @Override
    public Area createSuperArea(Rectangular gp, List<Area> selected, String name)
    {
        if (getChildCount() > 1 && selected.size() > 1 && selected.size() != getChildCount())
        {
            //create the new area
            AreaImpl area = new AreaImpl(getX1() + getGrid().getColOfs(gp.getX1()),
                                         getY1() + getGrid().getRowOfs(gp.getY1()),
                                         getX1() + getGrid().getColOfs(gp.getX2()+1) - 1,
                                         getY1() + getGrid().getRowOfs(gp.getY2()+1) - 1);
            area.setName(name);
            int index = getIndex(selected.get(0));
            insertChild(area, index);
            area.appendChildren(selected);
            area.createGrid();
            createGrid();
            return area;
        }
        else
            return null;
    }
    
    /**
     * Returns the font size declared for the first box. If there are multiple boxes,
     * the first one is used. If there are no boxes (an artificial area), 0 is returned.
     * @return the declared font size or 0 if there are no boxes
     */
    public float getDeclaredFontSize()
    {
        if (getBoxes().size() > 0)
            return getBoxes().firstElement().getFontSize();
        else
            return 0;
    }
    
    /**
     * Computes the average font size of the boxes in the area
     * @return the font size
     */
    @Override
    public float getFontSize()
    {
        if (fontSizeCnt == 0)
            return 0;
        else
            return fontSizeSum / fontSizeCnt;
    }
    
    /**
     * Computes the average font weight of the boxes in the area
     * @return the font size
     */
    @Override
    public float getFontWeight()
    {
        if (fontWeightCnt == 0)
            return 0;
        else
            return fontWeightSum / fontWeightCnt;
    }
    
    /**
     * Computes the average font style of the boxes in the area
     * @return the font style
     */
    @Override
    public float getFontStyle()
    {
        if (fontStyleCnt == 0)
            return 0;
        else
            return fontStyleSum / fontStyleCnt;
    }
    
    @Override
    public float getUnderline()
    {
        if (underlineCnt == 0)
            return 0;
        else
            return underlineSum / underlineCnt;
    }
    
    @Override
    public float getLineThrough()
    {
        if (lineThroughCnt == 0)
            return 0;
        else
            return lineThroughSum / lineThroughCnt;
    }
    
    
    /**
     * Computes the average luminosity of the boxes in the area
     * @return the font size
     */
    public float getColorLuminosity()
    {
        if (getBoxes().isEmpty())
            return 0;
        else
        {
            float sum = 0;
            int len = 0;
            for (Box box : getBoxes())
            {
                int l = box.getText().length(); 
                sum += colorLuminosity(box.getColor()) * l;
                len += l;
            }
            return sum / len;
        }
    }
    
    /**
     * Updates the average values when a new area is added or joined
     * @param other the other area
     */
    public void updateAverages(AreaImpl other)
    {
        fontSizeCnt += other.fontSizeCnt;
        fontSizeSum += other.fontSizeSum;
        fontWeightCnt += other.fontWeightCnt;
        fontWeightSum += other.fontWeightSum;
        fontStyleCnt += other.fontStyleCnt;
        fontStyleSum += other.fontStyleSum;
        underlineCnt += other.underlineCnt;
        underlineSum += other.underlineSum;
        lineThroughCnt += other.lineThroughCnt;
        lineThroughSum += other.lineThroughSum;
    }
    
    /**
     * Resets the averages to the values obtained from the text boxes
     * that belong to this area only. No subareas are considered.
     */
    protected void resetAverages()
    {
        fontSizeCnt = 0;
        fontSizeSum = 0;
        fontWeightCnt = 0;
        fontWeightSum = 0;
        fontStyleCnt = 0; 
        fontStyleSum = 0; 
        underlineCnt = 0; 
        underlineSum = 0; 
        lineThroughCnt = 0; 
        lineThroughSum = 0;
        for (Box box : getBoxes())
            updateAveragesForBox(box);
    }
    
    @Override
    public String getText()
    {
        String ret = "";
        if (isLeaf())
            ret = getBoxText();
        else
            for (int i = 0; i < getChildCount(); i++)
                ret += getChildArea(i).getText();
        return ret;
    }
    
    /**
     * Returns the child areas whose absolute coordinates intersect with the specified rectangle.
     */
    public Vector<AreaImpl> getChildNodesInside(Rectangular r)
    {
        Vector<AreaImpl> ret = new Vector<AreaImpl>();
        for (GenericTreeNode child : getChildren())
        {
            AreaImpl childarea = (AreaImpl) child;
            if (childarea.getBounds().intersects(r))
                ret.add(childarea);
        }
        return ret;
    }
    
    /**
     * Check if there are some children in the given subarea of the area.
     */
    public boolean isAreaEmpty(Rectangular r)
    {
        for (GenericTreeNode child : getChildren())
        {
            AreaImpl childarea = (AreaImpl) child;
            if (childarea.getBounds().intersects(r))
                return false;
        }
        return true;
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
     * Removes simple separators from current separator set. A simple separator
     * has only one or zero visual areas at each side
     */
    public void removeSimpleSeparators()
    {
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
        int gx1 = getGrid().findCellX(sep.getX1());
        int gx2 = getGrid().findCellX(sep.getX2());
        int gy = getGrid().findCellY(sep.getY1() - 1);
        int ret = 0;
        if (gx1 >= 0 && gx2 >= 0 && gy >= 0)
        {
            int i = gx1;
            while (i <= gx2)
            {
                AreaImpl node = (AreaImpl) getGrid().getAreaAt(i, gy);
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
        int gx1 = getGrid().findCellX(sep.getX1());
        int gx2 = getGrid().findCellX(sep.getX2());
        int gy = getGrid().findCellY(sep.getY2() + 1);
        int ret = 0;
        if (gx1 >= 0 && gx2 >= 0 && gy >= 0)
        {
            int i = gx1;
            while (i <= gx2)
            {
                AreaImpl node = (AreaImpl) getGrid().getAreaAt(i, gy);
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
        int gy1 = getGrid().findCellY(sep.getY1());
        int gy2 = getGrid().findCellY(sep.getY2());
        int gx = getGrid().findCellX(sep.getX1() - 1);
        int ret = 0;
        if (gy1 >= 0 && gy2 >= 0 && gx >= 0)
        {
            int i = gy1;
            while (i <= gy2)
            {
                AreaImpl node = (AreaImpl) getGrid().getAreaAt(gx, i);
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
        int gy1 = getGrid().findCellY(sep.getY1());
        int gy2 = getGrid().findCellY(sep.getY2());
        int gx = getGrid().findCellX(sep.getX2() + 1);
        int ret = 0;
        if (gy1 >= 0 && gy2 >= 0 && gx >= 0)
        {
            int i = gy1;
            while (i <= gy2)
            {
                AreaImpl node = (AreaImpl) getGrid().getAreaAt(gx, i);
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
    public AreaImpl findContentAbove(Separator sep)
    {
        return recursiveFindAreaAbove(sep.getX1(), sep.getX2(), 0, sep.getY1());
    }
    
    private AreaImpl recursiveFindAreaAbove(int x1, int x2, int y1, int y2)
    {
        AreaImpl ret = null;
        int maxx = x2;
        int miny = y1;
        Vector <Box> boxes = getBoxes();
        for (Box box : boxes)
        {
            int bx = box.getBounds().getX1();
            int by = box.getBounds().getY2();
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
            AreaImpl child = (AreaImpl) getChildArea(i);
            AreaImpl area = child.recursiveFindAreaAbove(x1, x2, miny, y2);
            if (area != null)
            {   
                int bx = area.getX1(); 
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
    
    
	//=================================================================================

	/**
	 * Adds a new box to the area and updates the area bounds.
	 * @param box the new box to add
	 */
	protected void addBox(Box box)
	{
		super.addBox(box);
		updateAveragesForBox(box);
	}
	
	private void updateAveragesForBox(Box box)
	{
        if (box.getType() == Box.Type.TEXT_CONTENT)
        {
            int len = box.getText().trim().length();
            if (len > 0)
            {
                fontSizeSum += getAverageBoxFontSize(box) * len;
                fontSizeCnt += len;
                fontWeightSum += getAverageBoxFontWeight(box) * len;
                fontWeightCnt += len;
                fontStyleSum += getAverageBoxFontStyle(box) * len;
                fontStyleCnt += len;
            }
        }
	}
	
	private float getAverageBoxFontSize(Box box)
	{
		if (box.getType() == Type.TEXT_CONTENT)
			return box.getFontSize();
		else if (box.getType() == Type.REPLACED_CONTENT)
			return 0;
		else
		{
			float sum = 0;
			int cnt = 0;
			for (int i = 0; i < getChildCount(); i++)
			{
				Box child = box.getChildBox(i);
				String text = child.getText().trim();
				cnt += text.length();
				sum += getAverageBoxFontSize(child);
			}
			if (cnt > 0)
				return sum / cnt;
			else
				return 0;
		}
	}
	
	private float getAverageBoxFontWeight(Box box)
	{
        if (box.getType() == Type.TEXT_CONTENT)
            return box.getFontWeight();
        else if (box.getType() == Type.REPLACED_CONTENT)
            return 0;
        else
        {
            float sum = 0;
            int cnt = 0;
            for (int i = 0; i < getChildCount(); i++)
            {
                Box child = box.getChildBox(i);
                String text = child.getText().trim();
                cnt += text.length();
                sum += getAverageBoxFontWeight(child);
            }
            if (cnt > 0)
                return sum / cnt;
            else
                return 0;
        }
	}
	
	private float getAverageBoxFontStyle(Box box)
	{
        if (box.getType() == Type.TEXT_CONTENT)
            return box.getFontStyle();
        else if (box.getType() == Type.REPLACED_CONTENT)
            return 0;
        else
        {
            float sum = 0;
            int cnt = 0;
            for (int i = 0; i < getChildCount(); i++)
            {
                Box child = box.getChildBox(i);
                String text = child.getText().trim();
                cnt += text.length();
                sum += getAverageBoxFontStyle(child);
            }
            if (cnt > 0)
                return sum / cnt;
            else
                return 0;
        }
	}
	
    private float colorLuminosity(Color c)
    {
        float lr, lg, lb;
        if (c == null)
        {
            lr = lg = lb = 255;
        }
        else
        {
            lr = (float) Math.pow(c.getRed() / 255.0f, 2.2f);
            lg = (float) Math.pow(c.getGreen() / 255.0f, 2.2f);
            lb = (float) Math.pow(c.getBlue() / 255.0f, 2.2f);
        }
        return lr * 0.2126f +  lg * 0.7152f + lb * 0.0722f;
    }

    //==========================================================================
    // TESTS
    //==========================================================================
    
    /**
     * @return <code>true<code> if the area is separated from the areas below it
     */
    public boolean separatedDown()
    {
        return hasBottomBorder() || isBackgroundSeparated();
    }
    
    /**
     * @return <code>true<code> if the area is separated from the areas above it
     */
    public boolean separatedUp()
    {
        return hasTopBorder() || isBackgroundSeparated();
    }
    
    /**
     * @return <code>true<code> if the area is separated from the areas on the left
     */
    public boolean separatedLeft()
    {
        return hasLeftBorder() || isBackgroundSeparated();
    }
    
    /**
     * @return <code>true<code> if the area is separated from the areas on the right
     */
    public boolean separatedRight()
    {
        return hasRightBorder() || isBackgroundSeparated();
    }

    /**
     * When set to true, the area is considered to be separated from other
     * areas explicitly, i.e. independently on its real borders or background.
     * This is usually used for some new superareas.
     * @return <code>true</code>, if the area is explicitly separated
     */
    public boolean isExplicitlySeparated()
    {
        return separated;
    }

    /**
     * When set to true, the area is considered to be separated from other
     * areas explicitly, i.e. independently on its real borders or background.
     * This is usually used for some new superareas.
     * @param separated <code>true</code>, if the area should be explicitly separated
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
     * Compares two areas and decides whether they have the same style. The thresholds of the style are taken from the {@link Config}.
     * @param other the other area to be compared
     * @return <code>true</code> if the areas are considered to have the same style
     */
    public boolean hasSameStyle(AreaImpl other)
    {
        return getStyle().isSameStyle(other.getStyle());
    }
    
}
