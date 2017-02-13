package nepgear;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * This class loads and manages a list of images, stored as URLs, within the
 * bot.
 */
public class ImageManager {

	/**
	 * The map of all images loaded, mapping file names to the list of URLs
	 */
	private static Map<String, List<String>> imageMap;

	private static boolean initialized = false;

	/**
	 * Initializes the ImageManager. Required before using any of the other
	 * methods within this class.
	 */
	public static void init() {
		imageMap = new HashMap<>();
		try (Stream<Path> paths = Files.walk(Paths.get("res/img-list"))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					add(new File(filePath.toUri()));
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		initialized = true;
	}

	/**
	 * Fetches a random picture from the list of URLs associated with a file.
	 * Has a slightly fuzzy input.
	 */
	public static String fetchRandomPic(String file) {
		checkInit();
		if (!imageMap.containsKey(file))
			return null;
		else {
			Random r = new Random();
			List<String> images = imageMap.get(file);
			return images.get(r.nextInt(images.size()));
		}
	}

	/**
	 * Refreshes the contents of a file and reloads all the existing URLs from
	 * it. Can be used to add or remove files...
	 */
	public static boolean refreshFile(String file) {
		checkInit();
		String path = "res/img-list/";
		path += file;

		System.out.println("[Attemping to use " + path + "]");
		File f = new File(path);
		if (!f.exists()) {
			if (imageMap.containsKey(file))
				imageMap.remove(file);
			return false;
		}
		add(f);
		return true;
	}

	/**
	 * Adds the URLs of a file to the image map. Note that they must all be line
	 * separated and all be valid...
	 */
	public static void add(File f) {
		checkInit();
		Scanner s = null;
		try {
			s = new Scanner(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> links = new LinkedList<>();
		while (s.hasNextLine())
			links.add(s.nextLine());
		s.close();
		imageMap.put(f.getName(), links);
		System.out.println("[SYSTEM] Added " + f.getName() + " to image list...");
	}

	/**
	 * This method checks that the image manager was properly initialized.
	 */
	private static void checkInit() {
		if (!initialized)
			throw new RuntimeException("ImageManager not initialized!");
	}

}