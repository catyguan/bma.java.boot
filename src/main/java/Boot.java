import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Boot {

	public static class Test {
		public static void main(String[] args) {
			System.out.println("hello world");
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					System.out.println("\targ[" + i + "] : " + args[i]);
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String libs = System.getProperty("bootlibs",
				new File(System.getProperty("user.dir"), "libs").toString());
		String main = System.getProperty("bootmain");

		if (main == null) {
			System.out.print("HOWTO: ");
			System.out
					.println("java -cp boot.jar -Dbootmain=xxx.yyy.MainClass [-Dbootlibs=filepath1;filepath2] Boot [param1 param2 ...]");
			System.out.println("bootmain - bootstrap main class");
			System.out.println("bootlibs - file path for libs");
			return;
		}

		List<File> files = new ArrayList<File>();
		if (libs != null) {
			String[] libList = libs.split(";");
			for (String lib : libList) {
				lib = lib.trim();
				if (lib.isEmpty())
					continue;
				File bootlibsDir = new File(lib);
				listAll(bootlibsDir, files);
			}
		}

		URL[] urls = new URL[files.size()];
		for (int i = 0; i < urls.length; i++) {
			try {
				urls[i] = files.get(i).toURI().toURL();
			} catch (MalformedURLException e) {
				System.out.println("skip file [" + files.get(i) + "]");
			}
		}
		ClassLoader loader = new URLClassLoader(urls, Thread.currentThread()
				.getContextClassLoader());
		Thread.currentThread().setContextClassLoader(loader);

		try {
			Class<?> cls = loader.loadClass(main);
			Method m = cls.getMethod("main", String[].class);
			m.invoke(null, new Object[] { args });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listAll(File directory, List<File> result) {
		if (!directory.exists()) {
			return;
		}

		if (!directory.isDirectory()) {
			return;
		}

		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String name = file.getName();
			if (name.equals(".") || name.equals("..")) {
				continue;
			}
			if (file.isDirectory()) {
				listAll(file, result);
			} else {
				if (name.toLowerCase().endsWith(".jar")) {
					result.add(file);
				}
			}
		}
	}
}
