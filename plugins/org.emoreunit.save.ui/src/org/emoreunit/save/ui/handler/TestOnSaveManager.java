package org.emoreunit.save.ui.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.emoreunit.save.ui.EMoreUnitSavePlugin;
import org.emoreunit.save.ui.decorator.TestOnSaveDecorator;
import org.moreunit.elements.ClassTypeFacade;
import org.moreunit.elements.EditorPartFacade;
import org.moreunit.elements.TypeFacade;
import org.moreunit.handler.RunTestsActionExecutor;
import org.moreunit.util.PluginTools;

/**
 * <p>
 * This class manages the resources that have been marked to
 * run tests on save.
 * </p>
 * <p>
 * It's also responsible to trigger tests of the corresponding
 * resource after a successful save.
 * </p>
 */
@SuppressWarnings("restriction")
public class TestOnSaveManager {

	/**
	 * Registers the manager to listen to save.
	 */
	public void registerSaveListener() {

		ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getService(ICommandService.class);
		Command command = commandService
				.getCommand(IWorkbenchCommandConstants.FILE_SAVE);
		command.addExecutionListener(new IExecutionListener() {

			@Override
			public void preExecute(String commandId, ExecutionEvent event) {

			}

			@Override
			public void postExecuteSuccess(String commandId, Object returnValue) {
				IEditorPart editorPart = PluginTools.getOpenEditorPart();
				if (editorPart != null) {
					EditorPartFacade editorPartFacade = new EditorPartFacade(
							editorPart);
					IFile file = editorPartFacade.getFile();
					if(file==null) {
						return;
					}
					if (hasTestOnSaveMarker(file)) {
						runTest(editorPartFacade);
					}
				}
			}

			@Override
			public void postExecuteFailure(String commandId,
					ExecutionException exception) {

			}

			@Override
			public void notHandled(String commandId,
					NotHandledException exception) {

			}
		});

	}
	
	/**
	 * Executes the first found launch configuration.
	 * 
	 * @param editorPartFacade
	 */
	@NonNullByDefault
	protected void runTest(EditorPartFacade editorPartFacade) {
		
		ICompilationUnit compilationUnit = editorPartFacade.getCompilationUnit();
		ClassTypeFacade classTypeFacade = new ClassTypeFacade(compilationUnit);		
		Collection<IType> testCases = classTypeFacade.getCorrespondingTestCases();
		if(!testCases.isEmpty() && !TypeFacade.isTestCase(editorPartFacade.getCompilationUnit())) {
			List<IResource> testResources = new ArrayList<IResource>();
			for (IType testCase : testCases) {
					IResource correspondingResource = testCase.getResource();
					if(!testResources.contains(correspondingResource)) {
						testResources.add(correspondingResource);
					}
					for (IResource testResource : testResources) {
						if(testResource!=null) {							
							runJUnitTest(testResource);
						}
					}
			}
		} else {
			IFile file = editorPartFacade.getFile();
			if(file!=null) {				
				runJUnitTest(file);
			}
		}
	}

	@NonNullByDefault
	private void runJUnitTest(IResource resource) {
		
		List<ILaunchConfiguration> launchConfigurations = findLaunchConfigurations(resource);
		for (ILaunchConfiguration iLaunchConfiguration : launchConfigurations) {
			try {

				String mode = ILaunchManager.RUN_MODE;
				Set modes = iLaunchConfiguration.getModes();
				if (modes.size() > 0) {
					Object objMode = modes.iterator().next();
					if (objMode instanceof String) {
						mode = (String) objMode;
					}
				}

				iLaunchConfiguration.launch(mode, new NullProgressMonitor(),
						true);

			} catch (CoreException e) {
				EMoreUnitSavePlugin.getDefault().getLog().log(e.getStatus());
			}			
		}
	}
	
	private List<ILaunchConfiguration> findLaunchConfigurations(IResource resource) {
		
		List<ILaunchConfiguration> result = new ArrayList<ILaunchConfiguration>();
		
		if(resource!=null) {			
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();			
			try {				
				ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations();				
				for (ILaunchConfiguration iLaunchConfiguration : launchConfigurations) {
					IResource[] mappedResources = iLaunchConfiguration.getMappedResources();
					if(mappedResources!=null) {
						for (IResource iResource : mappedResources) {
							if(resource.equals(iResource)) {
								ILaunchConfigurationType type = iLaunchConfiguration.getType();
								if("org.eclipse.jdt.junit.core".equals(type.getPluginIdentifier())) {
									result.add(iLaunchConfiguration);									
								}
							}
						}
					}
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
		
	}

	@NonNullByDefault
	protected boolean hasTestOnSaveMarker(IFile file) {
		return EMoreUnitSavePlugin.getDefault().hasTestOnSaveMarker(file);
	}

	/**
	 * Toggles the on save marker for the given file.
	 * 
	 * @param file
	 */
	@NonNullByDefault
	public synchronized void toggleTestOnSave(IFile file) {

		try {
			if (hasTestOnSaveMarker(file)) {
				file.deleteMarkers(EMoreUnitSavePlugin.TEST_ON_SAVE_MARKER_ID,
						false, IResource.DEPTH_ONE);
				refresh(file);				
			} else if(file!=null){
				file.createMarker(EMoreUnitSavePlugin.TEST_ON_SAVE_MARKER_ID);
				refresh(file);
			}
		} catch (CoreException e) {
			EMoreUnitSavePlugin.getDefault().getLog().log(e.getStatus());
		}

	}
	

	private void refresh(IFile file) throws CoreException {
		
		file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());		
		IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
		decoratorManager.update(TestOnSaveDecorator.DECORATOR_ID);
		
	}

}
