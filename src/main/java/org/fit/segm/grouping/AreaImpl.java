/**
 * VisualArea.java
 *
 * Created on 3-�en-06, 10:58:23  by radek
 */
package org.fit.segm.grouping;

import java.awt.Color;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.Box.Type;
import org.fit.layout.model.ContentObject;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;

/**
 * An area containing several visual boxes.
 * 
 * @author radek
 */
public class AreaImpl implements Area
{
    private static int nextid = 1;
    
    private int id;
    
    /** The node that holds the area in the tree of areas */
    protected AreaNode node;

    /** A grid of inserted elements */
    protected AreaGrid grid;
    
    /** Position of this area in the parent grid */
    protected Rectangular gp;
    
    /** Assigned tags */
    protected Set<Tag> tags;
    
	/**
	 * The visual boxes that form this area.
	 */
	private Vector<Box> boxes;
	
	/**
	 * Declared bounds of the area.
	 */
	private Rectangular bounds;
	
	/**
	 * Effective bounds of the area content.
	 */
	private Rectangular contentBounds;

	/**
     * Area description
     */
    private String name;
    
    /**
     * Area level. 0 corresponds to the areas formed by boxes, greater numbers represent
     * greater level of grouping
     */
    private int level = 0;
    
    /**
     * Borders present?
     */
    private boolean btop, bleft, bbottom, bright;
    
    /**
     * Background color of the first box in the area.
     */
    private Color bgcolor;
    
    /**
     * Is the first box in the area separated by background?
     */
    private boolean backgroundSeparated;
    
    /**
     * Sum for computing the average font size
     */
    private int fontSizeSum = 0;
    
    /**
     * Counter for computing the average font size
     */
    private int fontSizeCnt = 0;
    
    private int fontWeightSum = 0;
    private int fontWeightCnt = 0;
    private int fontStyleSum = 0;
    private int fontStyleCnt = 0;
    
    
	//================================================================================
	
    /** 
     * Creates an empty area of a given size
     */
    public AreaImpl(int x1, int y1, int x2, int y2)
	{
        id = nextid++;
		boxes = new Vector<Box>();
		bounds = new Rectangular(x1, y1, x2, y2);
        name = null;
        btop = false;
        bleft = false;
        bright = false;
        bbottom = false;
        bgcolor = null;
        grid = null;
        gp = new Rectangular();
        tags = new HashSet<Tag>();
	}
    
    /** 
     * Creates an empty area of a given size
     */
    public AreaImpl(Rectangular r)
    {
        id = nextid++;
        boxes = new Vector<Box>();
        bounds = new Rectangular(r);
        name = null;
        btop = false;
        bleft = false;
        bright = false;
        bbottom = false;
        bgcolor = null;
        grid = null;
        gp = new Rectangular();
        tags = new HashSet<Tag>();
    }
    
    /** 
     * Creates an area from a single box. Update the area bounds and name accordingly.
     * @param box The source box that will be contained in this area
     */
    public AreaImpl(Box box)
    {
        id = nextid++;
        boxes = new Vector<Box>();
        addBox(box); //expands the content bounds appropriately
        bounds = new Rectangular(contentBounds);
        this.name = box.toString();
        btop = box.hasTopBorder();
        bleft = box.hasLeftBorder();
        bright = box.hasRightBorder();
        bbottom = box.hasBottomBorder();
        bgcolor = box.getBackgroundColor();
        backgroundSeparated = box.isBackgroundSeparated();
        grid = null;
        gp = new Rectangular();
        tags = new HashSet<Tag>();
    }
    
    /** 
     * Creates an area from a a list of boxes. Update the area bounds and name accordingly.
     * @param boxes The source boxes that will be contained in this area
     */
    public AreaImpl(Vector<Box> boxlist)
    {
        id = nextid++;
        boxes = new Vector<Box>(boxlist.size());
        for (Box box : boxlist)
            addBox(box); //expands the content bounds appropriately
        Box box = boxlist.firstElement();
        bounds = new Rectangular(contentBounds);
        this.name = box.toString();
        btop = box.hasTopBorder();
        bleft = box.hasLeftBorder();
        bright = box.hasRightBorder();
        bbottom = box.hasBottomBorder();
        bgcolor = box.getBackgroundColor();
        backgroundSeparated = box.isBackgroundSeparated();
        grid = null;
        gp = new Rectangular();
        tags = new HashSet<Tag>();
    }
    
    /** 
     * Creates a copy of another area.
     * @param area The source area
     */
    public AreaImpl(AreaImpl src)
    {
        id = nextid++;
        boxes = new Vector<Box>(src.getBoxes());
        contentBounds = (src.contentBounds == null) ? null : new Rectangular(src.contentBounds);
        bounds = new Rectangular(src.bounds);
        name = (src.name == null) ? null : new String(src.name);
        btop = src.btop;
        bleft = src.bleft;
        bright = src.bright;
        bbottom = src.bbottom;
        bgcolor = (src.bgcolor == null) ? null : new Color(src.bgcolor.getRed(), src.bgcolor.getGreen(), src.bgcolor.getBlue());
        backgroundSeparated = src.backgroundSeparated;
        level = src.level;
        fontSizeSum = src.fontSizeSum;
        fontSizeCnt = src.fontStyleCnt;
        fontStyleSum = src.fontStyleSum;
        fontStyleCnt = src.fontStyleCnt;
        fontWeightSum = src.fontWeightSum;
        fontWeightCnt = src.fontWeightCnt;
        grid = null;
        gp = new Rectangular();
        tags = new HashSet<Tag>();
    }
    
    /**
     * Obtains a unique ID of the area within the page.
     * @return the area ID
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Obtains the node that holds the area in the area tree.
     * @return the node or {@code null} when the area is not placed in a tree
     */
    public AreaNode getNode()
    {
        return node;
    }

    /**
     * Sets the node that holds the area in the area tree.
     * @param node the tree node.
     */
    public void setNode(AreaNode node)
    {
        this.node = node;
    }

    /**
     * Joins another area to this area. Update the bounds and the name accordingly.
     * @param other The area to be joined to this area.
     * @param horizontal If true, the areas are joined horizontally.
     * This influences the resulting area borders. If false, the areas are joined vertically.
     */
    public void join(AreaImpl other, boolean horizontal)
    {
    	bounds.expandToEnclose(other.bounds);
    	name = name + " . " + other.name;
        //update border information according to the mutual area positions
        if (horizontal)
        {
            if (getX1() <= other.getX1())
            {
                if (other.hasRightBorder()) bright = true;
            }
            else
            {
                if (other.hasLeftBorder()) bleft = true;
            }
        }
        else
        {
            if (getY1() <= other.getY1())
            {
                if (other.hasBottomBorder()) bbottom = true;
            }
            else
            {
                if (other.hasTopBorder()) btop = true;
            }
        }
        //add all the contained boxes
        boxes.addAll(other.boxes);
        updateAverages(other);
        //just a test
        if (!this.hasSameBackground(other))
        	System.err.println("Area: Warning: joining areas " + name + " and " + other.name + 
        	        " of different background colors " + this.bgcolor + " x " + other.bgcolor); 
    }
    
    /**
     * Joins a child area to this area. Updates the bounds and the name accordingly.
     * @param other The child area to be joined to this area.
     */
    public void joinChild(AreaImpl other)
    {
        //TODO obsah se neimportuje?
        bounds.expandToEnclose(other.bounds);
        name = name + " . " + other.name;
    }
    
    /**
     * Sets the name of the area. The name is used when the area information is displayed
     * using <code>toString()</code>
     * @param The new area name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
    	return name;
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
        
        /*if (bgcolor != null)
            bs += "\"" + String.format("#%02x%02x%02x", bgcolor.getRed(), bgcolor.getGreen(), bgcolor.getBlue()) + "\"";*/
        
        if (name != null)
            return bs + " " + name + " " + bounds.toString();
        else
            return bs + " " + "<area> " + bounds.toString();
          
    }
    
    /**
     * Add the box node to the area if its bounds are inside of the area bounds.
     * @param node The box node to be added
     */
    public void chooseBox(Box node)
    {
    	if (bounds.encloses(node.getVisualBounds()))
    		addBox(node);
    }
    
    /**
     * Returns a vector of boxes that are inside of this area
     * @return A vector containing the {@link org.burgetr.segm.BoxNode BoxNode} objects
     */
    public Vector<Box> getBoxes()
    {
    	return boxes;
    }
    
    /**
     * Set the borders around
     */
    public void setBorders(boolean top, boolean left, boolean bottom, boolean right)
    {
    	btop = top;
    	bleft = left;
    	bbottom = bottom;
    	bright = right;
    }

    @Override
    public int getChildCount()
    {
        return getNode().getChildCount();
    }
    
	//=================================================================================
	
    @Override
    public Rectangular getBounds()
    {
    	return bounds;
    }
    
    public void setBounds(Rectangular bounds)
    {
        this.bounds = bounds;
    }
    
    public Rectangular getContentBounds()
    {
        return contentBounds;
    }
	
    @Override
    public int getX1()
    {
    	return bounds.getX1();
    }
    
    @Override
    public int getY1()
    {
    	return bounds.getY1();
    }
    
    @Override
    public int getX2()
    {
    	return bounds.getX2();
    }
    
    @Override
    public int getY2()
    {
    	return bounds.getY2();
    }
    
    @Override
    public int getWidth()
    {
    	return bounds.getWidth();
    }
    
    @Override
    public int getHeight()
    {
    	return bounds.getHeight();
    }
    
    /**
     * Computes the square area occupied by this visual area.
     * @return the square area in pixels
     */
    public int getSquareArea()
    {
        return bounds.getArea();
    }
    
    @Override
    public boolean hasTopBorder()
    {
        return btop;
    }
    
    @Override
    public boolean hasLeftBorder()
    {
        return bleft;
    }
    
    @Override
    public boolean hasRightBorder()
    {
        return bright;
    }
    
    @Override
    public boolean hasBottomBorder()
    {
        return bbottom;
    }
    
    @Override
    public Color getBackgroundColor()
    {
    	return bgcolor;
    }
    
    @Override
    public boolean isBackgroundSeparated()
    {
        return backgroundSeparated;
    }
    
    /**
     * Checks if this area has the same background color as another area
     * @param other the other area
     * @return true if the areas are both transparent or they have the same
     * background color declared
     */
    public boolean hasSameBackground(AreaImpl other)
    {
        return (bgcolor == null && other.bgcolor == null) || 
               (bgcolor != null && other.bgcolor != null && bgcolor.equals(other.bgcolor));
    }
    
    public boolean encloses(AreaImpl other)
    {
    	return bounds.encloses(other.bounds);
    }
    
    public boolean contains(int x, int y)
    {
    	return bounds.contains(x, y);
    }
    
    public boolean hasContent()
    {
        return !boxes.isEmpty();
    }
    
    //======================================================================================
    
    /**
     * @return true if the area contains any text
     */
    public boolean containsText()
    {
        for (Box root : boxes)
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
    public boolean isReplaced()
    {
        boolean empty = true;
        for (Box root : boxes)
        {
            empty = false;
            if (root.getContentObject() == null)
                return false;
        }
        return !empty;
    }
    
    /**
     * Returns the text string represented by a concatenation of all
     * the boxes contained directly in this area.
     */
    public String getBoxText()
    {
        StringBuilder ret = new StringBuilder();
        boolean start = true;
        for (Iterator<Box> it = boxes.iterator(); it.hasNext(); )
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
        for (Box box : boxes)
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
        for (Iterator<Box> it = boxes.iterator(); it.hasNext(); )
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
    public boolean isHorizontalSeparator()
    {
        return !containsText() && 
               bounds.getHeight() < 10 &&
               bounds.getWidth() > 20 * bounds.getHeight();
    }
    
    /**
     * Tries to guess if this area acts as a vertical separator. The criteria are the same
     * as for the horizontal one.
     * @return true if the area can be used as a vertical separator
     */
    public boolean isVerticalSeparator()
    {
        return !containsText() && 
               bounds.getWidth() < 10 &&
               bounds.getHeight() > 20 * bounds.getWidth();
    }
    
    /**
     * Tries to guess if this area acts as any kind of separator.
     * See the {@link #isVerticalSeparator()} and {@link #isHorizontalSeparator()} methods for more explanation.
     * @return true if the area can be used as a separator
     */
    public boolean isSeparator()
    {
        return isHorizontalSeparator() || isVerticalSeparator();
    }
    
    /**
     * Returns the size height declared for the first box. If there are multiple boxes,
     * the first one is used. If there are no boxes (an artificial area), 0 is returned.
     * @return 
     */
    public float getDeclaredFontSize()
    {
        if (boxes.size() > 0)
            return boxes.firstElement().getFontSize();
        else
            return 0;
    }
    
    /**
     * Computes the average font size of the boxes in the area
     * @return the font size
     */
    public float getAverageFontSize()
    {
        if (fontSizeCnt == 0)
            return 0;
        else
            return (float) fontSizeSum / fontSizeCnt;
    }
    
    /**
     * Computes the average font weight of the boxes in the area
     * @return the font size
     */
    public float getAverageFontWeight()
    {
        if (fontWeightCnt == 0)
            return 0;
        else
            return (float) fontWeightSum / fontWeightCnt;
    }
    
    /**
     * Computes the average font style of the boxes in the area
     * @return the font style
     */
    public float getAverageFontStyle()
    {
        if (fontStyleCnt == 0)
            return 0;
        else
            return (float) fontStyleSum / fontStyleCnt;
    }
    
    /**
     * Computes the average luminosity of the boxes in the area
     * @return the font size
     */
    public float getAverageColorLuminosity()
    {
        if (boxes.isEmpty())
            return 0;
        else
        {
            float sum = 0;
            int len = 0;
            for (Box box : boxes)
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
    }
    
    //====================================================================================
    // grid operations
    //====================================================================================
    
    /**
     * Creates the grid of areas from the child areas.
     */
    public void createGrid()
    {
        grid = new AreaGrid(this);
    }
    
    /**
     * Obtains the gird of contained areas.
     * @return the grid
     */
    public AreaGrid getGrid()
    {
        return grid;
    }
    
    /**
     * @return Returns the height of the area in the grid height in rows
     */
    public int getGridHeight()
    {
        return gp.getHeight();
    }

    /**
     * @return Returns the width of the area in the grid in rows
     */
    public int getGridWidth()
    {
        return gp.getWidth();
    }

    /**
     * @return Returns the gridX.
     */
    public int getGridX()
    {
        return gp.getX1();
    }

    /**
     * @param gridX The gridX to set.
     */
    public void setGridX(int gridX)
    {
        gp.setX1(gridX);
    }

    /**
     * @return Returns the gridY.
     */
    public int getGridY()
    {
        return gp.getY1();
    }

    /**
     * @param gridY The gridY to set.
     */
    public void setGridY(int gridY)
    {
        gp.setY1(gridY);
    }
    
    /**
     * @return the position of this area in the grid of its parent area
     */
    public Rectangular getGridPosition()
    {
        return gp;
    }
    
    /**
     * Sets the position in the parent area grid for this area
     * @param pos the position
     */
    public void setGridPosition(Rectangular pos)
    {
        gp = new Rectangular(pos);
    }
    
    /**
     * Returns the child area at the specified grid position or null, if there is no
     * child area at this position.
     */
    public AreaImpl getChildAtGridPos(int x, int y)
    {
        for (int i = 0; i < getNode().getChildCount(); i++)
        {
            AreaImpl child = getNode().getChildArea(i).getArea();
            if (child.getGridPosition().contains(x, y))
                return child;
        }
        return null;
    }
    
    /**
     * Returns the child areas whose absolute coordinates intersect with the specified rectangle.
     */
    public Vector<AreaImpl> getChildNodesInside(Rectangular r)
    {
        Vector<AreaImpl> ret = new Vector<AreaImpl>();
        for (int i = 0; i < getNode().getChildCount(); i++)
        {
            AreaImpl child = getNode().getChildArea(i).getArea();
            if (child.getBounds().intersects(r))
                ret.add(child);
        }
        return ret;
    }
    
    /**
     * Check if there are some children in the given subarea of the area.
     */
    public boolean isAreaEmpty(Rectangular r)
    {
        for (int i = 0; i < getNode().getChildCount(); i++)
        {
            AreaNode child = getNode().getChildArea(i);
            if (child.getArea().getBounds().intersects(r))
                return false;
        }
        return true;
    }

    //====================================================================================
    // tagging
    //====================================================================================
    
    /**
     * Adds a tag to this area.
     * @param tag the tag to be added.
     */
    @Override
    public void addTag(Tag tag)
    {
        tags.add(tag);
    }
    
    /**
     * Tests whether the area has this tag.
     * @param tag the tag to be tested.
     * @return <code>true</code> if the area has this tag
     */
    @Override
    public boolean hasTag(Tag tag)
    {
        return tags.contains(tag);
    }
    
    /**
     * Removes all tags that belong to the given collection.
     * @param c A collection of tags to be removed.
     */
    public void removeAllTags(Collection<Tag> c)
    {
        tags.removeAll(c);
    }
    
    /**
     * Tests whether the area or any of its <b>direct child</b> areas have the given tag.
     * @param tag the tag to be tested.
     * @return <code>true</code> if the area or its direct child areas have the given tag
     */
    public boolean containsTag(Tag tag)
    {
        if (hasTag(tag))
            return true;
        else
        {
            for (int i = 0; i < getNode().getChildCount(); i++)
                if (getNode().getChildArea(i).getArea().hasTag(tag))
                    return true;
            return false;
        }
    }
    
    /**
     * Obtains the set of tags assigned to the area.
     * @return a set of tags
     */
    @Override
    public Set<Tag> getTags()
    {
        return tags;
    }
    
    /**
     * Obtains all the tags assigned to this area and its child areas (not all descendant areas).
     * @return a set of tags
     */
    public Set<Tag> getAllTags()
    {
        Set<Tag> ret = new HashSet<Tag>(tags);
        for (int i = 0; i < getNode().getChildCount(); i++)
            ret.addAll(getNode().getChildArea(i).getArea().getTags());
        return ret;
    }
    
	//=================================================================================

	/**
	 * Adds a new box to the area and updates the area bounds.
	 * @param box the new box to add
	 */
	private void addBox(Box box)
	{
		boxes.add(box);

        Rectangular sb = box.getVisualBounds();
        if (contentBounds == null)
        	contentBounds = new Rectangular(sb);
        else if (sb.getWidth() > 0 && sb.getHeight() > 0)
        	contentBounds.expandToEnclose(sb);
        
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

}