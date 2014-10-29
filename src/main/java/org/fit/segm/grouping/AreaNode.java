/**
 * VisualAreaNode.java
 *
 * Created on 28.6.2006, 15:13:48 by burgetr
 */
package org.fit.segm.grouping;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A node that holds an area in the tree.
 * 
 * @author burgetr
 */
public class AreaNode extends DefaultMutableTreeNode
{
    private static final long serialVersionUID = 3342686210987580546L;
    
    //====================================================================================
    
    public AreaNode(AreaImpl area)
    {
        super(area);
        area.setNode(this);
    }

    //====================================================================================

    /**
     * Obtains the contained area.
     * @return the contained area
     */
    public AreaImpl getArea()
    {
        return (AreaImpl) getUserObject();
    }
    
    /**
     * Adds a new area as a subarea. Updates the area bounds if necessary.
     * @param area the area node to be added
     */
    public void addArea(AreaNode sub)
    {
        add(sub);
        getArea().getBounds().expandToEnclose(sub.getArea().getBounds());
        getArea().updateAverages(sub.getArea());
    }
    
    /**
     * Adds all the areas in the vector as the subareas. It the areas
     * have already had another parent, they are removed from the old
     * parent.
     * @param areas the collection of subareas
     */
    public void addAll(Collection<? extends AreaNode> areas)
    {
        for (Iterator<? extends AreaNode> it = areas.iterator(); it.hasNext(); )
            addArea(it.next());
    }
    
    /**
     * Obtains the parent are of this node in the tree.
     * @return the parent area node or {@code null} for root areas.
     */
    public AreaNode getParentArea()
    {
        return (AreaNode) getParent();
    }

    /**
     * Returns a specified child area node.
     * @param index the child index
     * @return the specified child box
     */
    public AreaNode getChildArea(int index)
    {
        return (AreaNode) getChildAt(index);
    }

    /**
     * Obtains all the child areas.
     * @return a vector of the child areas 
     */
    @SuppressWarnings("unchecked")
    public Vector<? extends AreaNode> getChildAreas()
    {
        return this.children;
    }
    

}
