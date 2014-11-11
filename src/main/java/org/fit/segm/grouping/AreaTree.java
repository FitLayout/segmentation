/**
 * VisualAreaTree.java
 *
 * Created on 28.6.2006, 15:10:11 by burgetr
 */
package org.fit.segm.grouping;

import java.util.HashSet;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.Box.DisplayType;
import org.fit.layout.model.Box.Type;
import org.fit.layout.model.Page;
import org.fit.layout.model.SearchableAreaContainer;
import org.fit.layout.model.Tag;


/**
 * A tree of visual areas created from a box tree.
 * 
 * @author burgetr
 */
public class AreaTree implements SearchableAreaContainer
{
    /** The source tree */
    protected Page boxtree;
    
    /** The root node area */
    protected AreaImpl rootarea;
    
    //=================================================================================
    
    /**
     * Create a new tree of areas by the analysis of a box tree
     * @param srctree the source box tree
     */
    public AreaTree(Page srctree)
    {
        boxtree = srctree;
        rootarea = new AreaImpl(0, 0, 0, 0);
    }
    
    /**
     * @return the root node of the tree of areas
     */
    public Area getRoot()
    {
        return rootarea;
    }
    
    /**
     * Creates the area tree skeleton - selects the visible boxes and converts
     * them to areas 
     */
    public Area findBasicAreas()
    {
        rootarea = new AreaImpl(0, 0, 0, 0);
        new AreaNode(rootarea);
        for (int i = 0; i < boxtree.getRoot().getChildCount(); i++)
        {
            Area sub;
            sub = new AreaImpl(boxtree.getRoot().getChildBox(i));
            if (sub.getWidth() > 1 || sub.getHeight() > 1)
            {
                findStandaloneAreas(boxtree.getRoot().getChildBox(i), sub);
                rootarea.appendChild(sub);
            }
        }
        createGrids(rootarea);
        return rootarea;
    }
    
    //=================================================================================
    
    /**
     * Goes through a box tree and tries to identify the boxes that form standalone
     * visual areas. From these boxes, new areas are created, which are added to the
     * area tree. Other boxes are ignored.
     * @param boxroot the root of the box tree
     * @param arearoot the root node of the new area tree 
     */ 
    private void findStandaloneAreas(Box boxroot, Area arearoot)
    {
        if (boxroot.isVisible())
        {
            for (int i = 0; i < boxroot.getChildCount(); i++)
            {
                Box child = boxroot.getChildBox(i);
		        if (child.isVisible())
		        {
	                if (isVisuallySeparated(child))
	                {
	                    Area newnode = new AreaImpl(child);
	                    if (newnode.getWidth() > 1 || newnode.getHeight() > 1)
	                    {
                            findStandaloneAreas(child, newnode);
	                    	arearoot.appendChild(newnode);
	                    }
	                }
	                else
	                    findStandaloneAreas(child, arearoot);
		        }
            }
        }
    }
    
    /**
     * Goes through all the areas in the tree and creates the grids in these areas
     * @param root the root node of the tree of areas
     */
    protected void createGrids(AreaImpl root)
    {
        root.createGrid();
        for (int i = 0; i < root.getChildCount(); i++)
            createGrids((AreaImpl) root.getChildArea(i));
    }

    public static boolean isVisuallySeparated(Box box)
    {
        //invisible boxes are not separated
        if (!box.isVisible()) 
            return false;
        //root box is visually separated
        else if (box.getParentBox() == null)
            return true;
        //non-empty text boxes are visually separated
        else if (box.getType() == Type.TEXT_CONTENT) 
        {
            if (box.getText().trim().isEmpty())
                return false;
            else
                return true;
        }
        //replaced boxes are visually separated
        else if (box.getType() == Type.REPLACED_CONTENT)
        {
            return true;
        }
        //list item boxes with a bullet
        else if (box.getDisplayType() == DisplayType.LIST_ITEM)
        {
            return true;
        }
        //other element boxes
        else 
        {
            //check if separated by border -- at least one border needed
            if (box.getBorderCount() >= 1)
                return true;
            //check the background
            else if (box.isBackgroundSeparated())
                return true;
            return false;
        }

    }
    
    
    //=================================================================================
    // node search
    //=================================================================================
    
    public Area getAreaAt(int x, int y)
    {
        return recursiveGetAreaAt(rootarea, x, y);
    }
    
    private Area recursiveGetAreaAt(Area root, int x, int y)
    {
        if (root.getBounds().contains(x, y))
        {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                Area ret = recursiveGetAreaAt(root.getChildArea(i), x, y);
                if (ret != null)
                    return ret;
            }
            return root;
        }
        else
            return null;
    }
    
    public Area getAreaByName(String name)
    {
        return recursiveGetAreaByName(rootarea, name);
    }
    
    private Area recursiveGetAreaByName(Area root, String name)
    {
        if (root.toString().indexOf(name) != -1) //TODO ???
            return root;
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                Area ret = recursiveGetAreaByName(root.getChildArea(i), name);
                if (ret != null)
                    return ret;
            }
            return null;
        }
    }

    //=================================================================================
    // tagging
    //=================================================================================
    
    /**
     * Obtains all the tags that are really used in the tree.
     * @return A set of used tags.
     */
    public Set<Tag> getUsedTags()
    {
        Set<Tag> ret = new HashSet<Tag>();
        recursiveGetTags(getRoot(), ret);
        return ret;
    }
    
    private void recursiveGetTags(Area root, Set<Tag> dest)
    {
        dest.addAll(root.getTags().keySet());
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveGetTags(root.getChildArea(i), dest);
    }
    
}
