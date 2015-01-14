/**
 * SuperAreaOperator.java
 *
 * Created on 24. 10. 2013, 14:40:09 by burgetr
 */
package org.fit.segm.grouping.op;

import java.util.Vector;

import org.fit.layout.api.AreaTreeOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;
import org.fit.segm.grouping.Config;

/**
 * Detects the larger visual areas and creates the artificial area nodes.
 * 
 * @author burgetr
 */
public class SuperAreaOperator implements AreaTreeOperator
{
    /** Recursion depth limit while detecting the sub-areas */
    protected int depthLimit;

    /**
     * Creates the deparator with default parameter values.
     */
    public SuperAreaOperator()
    {
        depthLimit = 2;
    }
    
    /**
     * Creates the operator.
     * @param depthLimit Recursion depth limit while detecting the sub-areas
     */
    public SuperAreaOperator(int depthLimit)
    {
        this.depthLimit = depthLimit;
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.SuperAreas";
    }
    
    @Override
    public String getName()
    {
        return "Super areas";
    }

    @Override
    public String getDescription()
    {
        return "..."; //TODO
    }

    @Override
    public void apply(AreaTree atree)
    {
        recursiveFindSuperAreas((AreaImpl) atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursiveFindSuperAreas((AreaImpl) root);
    }

    //==============================================================================

    protected GroupAnalyzer createGroupAnalyzer(AreaImpl root)
    {
        return Config.createGroupAnalyzer(root);
    }
    
    //==============================================================================
    
    /**
     * Goes through all the areas in the tree and tries to join their sub-areas into single
     * areas.
     */
    private void recursiveFindSuperAreas(AreaImpl root)
    {
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveFindSuperAreas((AreaImpl) root.getChildArea(i));
        findSuperAreas(root, depthLimit);
    }
    
    /**
     * Creates syntetic super areas by grouping the subareas of the given area.
     * @param the root area to be processed
     * @param passlimit the maximal number of passes while some changes occur 
     */ 
    public void findSuperAreas(AreaImpl root, int passlimit)
    {
        if (root.getChildCount() > 0)
        {
            boolean changed = true;
            int pass = 0;
            root.createSeparators();
            while (changed && pass < passlimit)
            {
                changed = false;
                
                GroupAnalyzer groups = createGroupAnalyzer(root);
                
                Vector<Area> chld = new Vector<Area>();
                chld.addAll(root.getChildAreas());
                while (chld.size() > 1) //we're not going to group a single element
                {
                    //get the super area
                    Vector<AreaImpl> selected = new Vector<AreaImpl>();
                    int index = root.getIndex(chld.firstElement());
                    AreaImpl grp = null;
                    if (chld.firstElement().isLeaf())
                        grp = groups.findSuperArea((AreaImpl) chld.firstElement(), selected);
                    if (selected.size() == root.getChildCount())
                    {
                        //everything grouped into one group - it makes no sense to create a new one
                        //System.out.println("(contains all)");
                        break;
                    }
                    else
                    {
                        //add a new area
                        if (selected.size() > 1)
                        {
                            root.insertChild(grp, index);
                            //add(grp); //add the new group to the end of children (so that it is processed again later)
                            for (AreaImpl a : selected)
                                grp.appendChild(a);
                            chld.removeAll(selected);
                            grp.createGrid();
                            findSuperAreas(grp, passlimit - 1); //in the next level, we use smaller pass limit to stop the recursion
                            changed = true;
                        }
                        else
                        {
                             //couldn't group the first element -- remove it and go on
                            chld.removeElementAt(0);
                        }
                    }
                }
                root.createGrid();
                root.removeSimpleSeparators();
                //System.out.println("Pass: " + pass + " changed: " + changed);
                pass++;
            }
        }
    }

}
