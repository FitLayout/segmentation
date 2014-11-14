/**
 * AreaTreeOperator.java
 *
 * Created on 24. 10. 2013, 9:46:04 by burgetr
 */
package org.fit.segm.grouping.op;

import org.fit.segm.grouping.AreaImpl;
import org.fit.segm.grouping.AreaTree;

/**
 * A generic procedure that processes the area tree. The procedures may be applied in any order.
 * @author burgetr
 */
public interface AreaTreeOperator
{
    
    /**
     * Applies the operation to the given tree.
     * @param atree the area tree to be modified.
     */
    public void apply(AreaTree atree);
    
    /**
     * Applies the operation to the given subtree of the tree.
     * @param atree the area tree to be modified.
     * @param root the root node of the affected subtree.
     */
    public void apply(AreaTree atree, AreaImpl root);
    
    
}
