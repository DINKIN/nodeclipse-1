package org.nodeclipse.ui.preferences;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.nodeclipse.ui.Activator;
import org.nodeclipse.ui.util.Constants;
import org.nodeclipse.ui.util.LogUtil;
import org.nodeclipse.ui.util.NodeclipseConsole;
import org.nodeclipse.ui.util.OSUtils;
import org.nodeclipse.ui.util.ProcessUtils;

/**
 * 
 * @author oncereply, Paul Verest
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.NODECLIPSE_CONSOLE_ENABLED, true);
		
		//store.setDefault(PreferenceConstants.NODE_OPTIONS, "--harmony");
		//store.setDefault(PreferenceConstants.NODE_APPLICATION_ARGUMENTS, "--tea-pot-mode");
		
		store.setDefault(PreferenceConstants.NODE_ALLOW_MANY, true);
		
		store.setDefault(PreferenceConstants.ADD_JSDT_NATURE, false);
		store.setDefault(PreferenceConstants.ADD_TERN_NATURE, true);
		
		store.setDefault(PreferenceConstants.USE_NODEJS_BASE_MODULE_DEFINITIONS, true);
		store.setDefault(PreferenceConstants.USE_ORION_INDEX_FILES, true);
		store.setDefault(PreferenceConstants.USE_COMPLETIONS_JSON, true);
		
		//store.setDefault(PreferenceConstants.NODE_DEBUG_NO_BREAK, ""); //default is empty,null,no
		store.setDefault(PreferenceConstants.NODE_DEBUG_PORT, "5858");
		
		String node_path = "/usr/local/bin/node";
		String node_monitor_path = "/usr/local/lib/node_modules/node-dev/bin/node-dev";
		String express_path = "/usr/local/lib/node_modules/express/bin/express";
		String express_generator_path = "/usr/local/lib/node_modules/express-generator/bin/express";
		String coffee_path = "/usr/local/bin/coffee";
		String typescript_compiler_path = "/usr/local/lib/node_modules/typescript/bin/tsc";
		
		File file;
		if (OSUtils.isWindows()) {
			store.setDefault(PreferenceConstants.NODE_JUST_NODE, true); 
			node_path = "C:/Program Files/nodejs/node.exe".replace('/', File.separatorChar);
			file = new File(node_path);
			if (!file.exists()) {
				node_path = "C:/Program Files (x86)/nodejs/node.exe".replace('/', File.separatorChar);
			}
			String windowsNodeModulesPath = System.getProperty("user.home") 
					+ "/AppData/Roaming/npm/node_modules/";
			node_monitor_path = (windowsNodeModulesPath+"node-dev/bin/node-dev").replace('/', File.separatorChar);
			express_path = (windowsNodeModulesPath+"express/bin/express").replace('/', File.separatorChar);
			express_generator_path = (windowsNodeModulesPath+"express-generator/bin/express").replace('/', File.separatorChar);
			coffee_path = (windowsNodeModulesPath+"coffee-script/bin/coffee").replace('/', File.separatorChar);
			typescript_compiler_path = (windowsNodeModulesPath+"typescript/bin/tsc").replace('/', File.separatorChar);
		} else if (OSUtils.isMacOS()) {
			file = new File(node_path);
			if (!file.exists()) {
				node_path = "/opt/local/bin/node";
			}
			file = new File(node_monitor_path);
			if (!file.exists()) {
				node_monitor_path = "/opt/local/lib/node_modules/node-dev/bin/node-dev";
			}
			file = new File(express_path);
			if (!file.exists()) {
				express_path = "/opt/local/lib/node_modules/express/bin/express";
			}
			file = new File(express_generator_path);
			if (!file.exists()) {
				express_generator_path = "/opt/local/lib/node_modules/express-generator/bin/express";
			}
			file = new File(coffee_path);
			if (!file.exists()) {
				coffee_path = "/opt/local/lib/node_modules/coffee-script/bin/coffee";
			}
			file = new File(typescript_compiler_path);
			if (!file.exists()) {
				typescript_compiler_path = "/opt/local/lib/node_modules/typescript/bin/tsc";
			}
		}
		
		// Check & set Preferences
		
		file = new File(node_path);
		if (file.exists()) {
			store.setDefault(PreferenceConstants.NODE_PATH, node_path);
		} else {
			file = findNode();
			if (file != null && file.exists()) {
				store.setDefault(PreferenceConstants.NODE_PATH, file.getAbsolutePath());
			}			
		}
		file = new File(node_monitor_path);
		if (file.exists()) {
			store.setDefault(PreferenceConstants.NODE_MONITOR_PATH, node_monitor_path);
		}
		
		// using bundles Node.js modules for Express & CoffeeScript {
		
		// Express: try to use express-generator (for Express 4.x)		
		file = new File(express_generator_path);
		if (file.exists()) {
			store.setDefault(PreferenceConstants.EXPRESS_PATH, express_generator_path);
			store.setDefault(PreferenceConstants.EXPRESS_VERSION,
					ProcessUtils.getCurrentVersionOf(express_generator_path));
		} else {
			file = new File(express_path);
			if (file.exists()) {
				store.setDefault(PreferenceConstants.EXPRESS_PATH, express_path);
				store.setDefault(PreferenceConstants.EXPRESS_VERSION,
						ProcessUtils.getCurrentVersionOf(express_path));
			} else {
				express_path = ProcessUtils.getBundledExpressPath();
				file = new File(express_path);
				if (file.exists()) {
					store.setDefault(PreferenceConstants.EXPRESS_PATH, express_path);
					store.setDefault(PreferenceConstants.EXPRESS_VERSION,
							ProcessUtils.getCurrentVersionOf(express_path));
				}
			}
		}
		//coffee
		file = new File(coffee_path);
		if (file.exists()) {
			store.setDefault(PreferenceConstants.COFFEE_PATH, coffee_path);
		} else {
			coffee_path = ProcessUtils.getBundledCoffeePath();
			file = new File(coffee_path);
			if (file.exists()) {
				store.setDefault(PreferenceConstants.COFFEE_PATH, coffee_path);
			}
		}
		//}
		store.setDefault(PreferenceConstants.COFFEE_COMPILE_OPTIONS, "--watch");
		file = new File(typescript_compiler_path);
		if (file.exists()) {
			store.setDefault(PreferenceConstants.TYPESCRIPT_COMPILER_PATH, typescript_compiler_path);
		}
		
		store.setDefault(PreferenceConstants.MONGODB_SHELL_OPTIONS, "--shell");
	}
	
    private static String getNodeFileName() {
        if (OSUtils.isWindows()) {
            return "node.exe";
        }
        return "node";
    }

    private static File findNode() {
        String nodeFileName = getNodeFileName();
        String path = System.getenv("PATH");
        String[] paths = path.split("" + File.pathSeparatorChar, 0);
        List<String> directories = new ArrayList<String>();
        for(String p : paths) {
        	directories.add(p);
        }

        // ensure /usr/local/bin is included for OS X
        if (OSUtils.isMacOS()) {
            directories.add("/usr/local/bin");
        }

        // search for Node.js in the PATH directories
        for (String directory : directories) {
            File nodeFile = new File(directory, nodeFileName);

            if (nodeFile.exists()) {
                return nodeFile;
            }
        }

        // #158 do not throw Exception for not standard Node path or name, let Node path be empty in Preferences
        //throw new IllegalStateException("Could not find Node.js.");
        LogUtil.error("Node.js executable can't be found!");
        return null;
    }
}