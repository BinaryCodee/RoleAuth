package xyz.blacked.bungee.api;

import net.md_5.bungee.api.plugin.Plugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteBridge {

    private final Plugin plugin;
    private final Logger logger;
    private boolean driverLoaded = false;

    public SQLiteBridge(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Initializes and loads the SQLite JDBC driver
     *
     * @return True if successfully loaded the driver
     */
    public boolean initialize() {
        logger.info("Initializing SQLite Bridge...");
        try {
            Class.forName("org.sqlite.JDBC");
            logger.info("SQLite JDBC driver already available in classpath!");
            driverLoaded = true;
            return true;
        } catch (ClassNotFoundException e) {
            logger.info("SQLite JDBC driver not found in classpath, attempting to load it...");
        }

        try {
            File libDir = new File(plugin.getDataFolder(), "lib");
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
            }
            if (!libDir.exists()) {
                libDir.mkdir();
            }

            File sqliteJar = new File(libDir, "sqlite-jdbc-3.40.1.0.jar");

            if (!sqliteJar.exists()) {
                logger.info("Extracting SQLite JDBC driver JAR...");
                extractResource("sqlite-jdbc-3.40.1.0.jar", sqliteJar);
            }

            if (sqliteJar.exists()) {
                loadJarIntoClasspath(sqliteJar);
                try {
                    Class.forName("org.sqlite.JDBC");
                    logger.info("SQLite JDBC driver loaded successfully!");
                    driverLoaded = true;
                    return true;
                } catch (ClassNotFoundException e) {
                    logger.log(Level.SEVERE, "Failed to load SQLite driver class after extraction", e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing SQLiteBridge", e);
        }

        return false;
    }

    /**
     * Extracts a resource from the JAR file to the given destination
     *
     * @param resourceName Name of the resource in JAR
     * @param destination Destination file
     * @throws IOException If extraction fails
     */
    private void extractResource(String resourceName, File destination) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (in == null) {
            throw new IOException("Resource not found: " + resourceName);
        }

        try (FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            in.close();
        }
    }

    /**
     * Loads a JAR file into the current classpath
     *
     * @param jarFile JAR file to load
     */
    private void loadJarIntoClasspath(File jarFile) {
        try {
            URL url = jarFile.toURI().toURL();
            URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();

            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);

            logger.info("Added SQLite JAR to classpath: " + jarFile.getAbsolutePath());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to add SQLite JAR to classpath", e);
        }
    }

    /**
     * Check if the driver was successfully loaded
     *
     * @return True if driver is loaded
     */
    public boolean isDriverLoaded() {
        return driverLoaded;
    }
}