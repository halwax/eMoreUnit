package org.emoreunit.save.ui.decorator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.emoreunit.save.ui.EMoreUnitSavePlugin;
import org.moreunit.images.ImageDescriptorCenter;

/**
 * Decorates files with a test on save marker on the bottom right.
 */
@SuppressWarnings("restriction")
public class TestOnSaveDecorator extends LabelProvider implements ILightweightLabelDecorator {
	
	public final static String DECORATOR_ID = "org.emoreunit.testOnSaveDecorator";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void decorate(Object element, IDecoration decoration)
    {
    	if(element instanceof IFile) {
    		boolean saveMarker = EMoreUnitSavePlugin.getDefault().hasTestOnSaveMarker((IFile) element);
    		if(saveMarker) {
    	        ImageDescriptor imageDescriptor = ImageDescriptorCenter.getTestCaseLabelImageDescriptor();
    	        decoration.addOverlay(imageDescriptor, IDecoration.BOTTOM_RIGHT);
    		}    		
    	}
    }
    


}
