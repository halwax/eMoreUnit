package org.emoreunit.save.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.emoreunit.save.ui.handler.TestOnSaveManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EMoreUnitSavePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.emoreunit.save.ui"; //$NON-NLS-1$

	public static final String TEST_ON_SAVE_MARKER_ID = PLUGIN_ID + ".testOnSaveMarker";

	// The shared instance
	private static EMoreUnitSavePlugin plugin;

	private TestOnSaveManager testOnSaveManager;
	
	/**
	 * The constructor
	 */
	public EMoreUnitSavePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		testOnSaveManager = initTestOnSaveManager();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EMoreUnitSavePlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns an singleton instance of {@link TestOnSaveManager}
	 * that manages the resources that are marked to run tests on
	 * save.
	 * 
	 * @return a singleton instance of {@link TestOnSaveManager}
	 */
	public synchronized @NonNull TestOnSaveManager getTestOnSaveManager() {
		TestOnSaveManager testOnSaveManager = this.testOnSaveManager;
		if(testOnSaveManager==null) {
			testOnSaveManager = initTestOnSaveManager();
		}
		return testOnSaveManager;
	}

	@NonNullByDefault
	private TestOnSaveManager initTestOnSaveManager() {
		TestOnSaveManager testOnSaveManager;
		testOnSaveManager = new TestOnSaveManager();
		testOnSaveManager.registerSaveListener();
		return testOnSaveManager;
	}
	
	/**
	 * @param file
	 * @return true if the file has a test on save marker
	 */
	@NonNullByDefault
	public boolean hasTestOnSaveMarker(IFile file) {

		boolean result = false;
		try {
			IMarker[] markers = file.findMarkers(
					TEST_ON_SAVE_MARKER_ID, false,
					IResource.DEPTH_ONE);
			if (markers.length > 0) {
				result = true;
			}

		} catch (CoreException e) {
		}

		return result;
	}

}
