package pw.konge.mirai.console.wrapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class Wrapper {
    static File JAR_BASE_PATH = new File("./content");

    static URL getJarURL(File base, String pattern) {
        for (File sub_file : Objects.requireNonNull(base.listFiles())) {
            if (Pattern.matches(pattern, sub_file.getName()) && sub_file.isFile()) {
                try {
                    new JarFile(sub_file).close();
                } catch (IOException e) {
                    System.err.println("malformed jar file!");
                    e.printStackTrace();
                }

                try {
                    return sub_file.toPath().toUri().toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        System.err.println("can't find " + pattern);
        System.exit(153);
        return null;
    }


    public static void main(String[] args) {
        // ExitCode:
        //  151 -> mirai-core jar broken
        //  152 -> mirai-console jar broken
        //  153 -> can't open file

        if (!JAR_BASE_PATH.exists()) {
            System.err.println("jar base path \"./content/\" not exists!");
            System.exit(153);
        }

        URL[] urls = new URL[]{
                getJarURL(JAR_BASE_PATH, "^mirai-console-[^p].*\\.jar"),
                getJarURL(JAR_BASE_PATH, "^mirai-console-pure-.*\\.jar"),
                getJarURL(JAR_BASE_PATH, "^mirai-core-qqandroid-.*\\.jar")
        };

        URLClassLoader loader = new URLClassLoader(urls);

        try {
            loader.loadClass("net.mamoe.mirai.BotFactoryJvm");
        } catch (ClassNotFoundException e) {
            System.err.println("mirai-core broken!");
            e.printStackTrace();
            System.exit(151);
        }

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.gc();
        });
        thread.start();

        try {
            Method m = loader
                    .loadClass("net.mamoe.mirai.console.pure.MiraiConsolePureLoader")
                    .getMethod("main", String[].class);
            m.invoke(null, (Object) new String[]{});
        } catch (
                ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e
        ) {
            System.err.println("mirai-console broken!");
            e.printStackTrace();
            System.exit(152);
        }
    }
}
