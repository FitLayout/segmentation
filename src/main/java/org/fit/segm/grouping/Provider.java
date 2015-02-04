/**
 * Provider.java
 *
 * Created on 15. 1. 2015, 15:03:22 by burgetr
 */
package org.fit.segm.grouping;

import org.fit.layout.impl.BaseAreaTreeProvider;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.Page;

/**
 * An AreaTreeProvider for the default FitLayout Segmentation algorithm based on grouping.
 * 
 * @author burgetr
 */
public class Provider extends BaseAreaTreeProvider
{

    @Override
    public String getId()
    {
        return "FitLayout.Grouping";
    }

    @Override
    public String getName()
    {
        return "FitLayout grouping segmentation algorithm";
    }

    @Override
    public String getDescription()
    {
        return "A configurable bottom-up segmentation algorithm";
    }

    @Override
    public AreaTree createAreaTree(Page page)
    {
        SegmentationAreaTree atree = new SegmentationAreaTree(page);
        atree.findBasicAreas();
        return atree; 
    }

    @Override
    public String[] getParamNames()
    {
        return new String[0];
    }

    @Override
    public ValueType[] getParamTypes()
    {
        return new ValueType[0];
    }

}
