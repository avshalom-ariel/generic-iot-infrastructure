package plugAndPlay;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.*;

public class DynamicJarLoader {
    private final List<Class<?>> classes = new ArrayList<>();
    private final String interfaceName;

    public DynamicJarLoader(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public List<Class<?>> load(String jarFilePath) {
        URL[] urls = null;
        try {
            urls = new URL[]{ new URL("jar:file:" + jarFilePath + "!/") };
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try (JarFile jarFile = new JarFile(jarFilePath);
            URLClassLoader classLoader = URLClassLoader.newInstance(urls)) {
            Enumeration<JarEntry> enumeration = jarFile.entries();

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement();

                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
                    className = className.replace('/', '.');
                    System.out.println(className);
                    Class<?> c = classLoader.loadClass(className);
                    Class<?>[] interfaces = c.getInterfaces();
                    for (Class<?> i : interfaces) {
                        if (i.getName().equals(interfaceName)) {
                            classes.add(c);
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return classes;
    }
}
