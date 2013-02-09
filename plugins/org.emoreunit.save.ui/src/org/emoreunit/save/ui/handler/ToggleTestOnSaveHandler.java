package org.emoreunit.save.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.emoreunit.save.ui.EMoreUnitSavePlugin;
import org.moreunit.elements.EditorPartFacade;
import org.moreunit.util.PluginTools;

/**
 * Toggles the test on save marker on the selected file.
 */
@SuppressWarnings("restriction")
public class ToggleTestOnSaveHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editorPart = PluginTools.getOpenEditorPart();
		if(editorPart==null) {
			return null;
		}
		
        EditorPartFacade editorPartFacade = new EditorPartFacade(editorPart);
        IFile file = editorPartFacade.getFile();
        if(file==null) {
        	return null;
        }
        
        TestOnSaveManager runTestOnSaveManager = EMoreUnitSavePlugin.getDefault().getTestOnSaveManager();
        runTestOnSaveManager.toggleTestOnSave(file);
        
		return null;
	}

}