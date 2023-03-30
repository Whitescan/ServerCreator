package de.whitescan.servercreator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import de.whitescan.servercreator.logging.Logger;
import de.whitescan.servercreator.setup.Setup;
import lombok.Getter;

/**
 *
 * @author Whitescan
 *
 */
public class ServerCreator {

	@Getter
	private static ServerCreator instance;

	@Getter
	private int setupStage;

	public static void main(String[] args) {
		new ServerCreator();
	}

	public ServerCreator() {
		ServerCreator.instance = this;
		Logger.info("Server started!");
		setup("");
		openConsole();
	}

	private void openConsole() {

		new Thread(() -> {

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			try {

				String input = "";

				while ((input = reader.readLine()) != null)
					setup(input);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}).start();

	}

	public void stop() {
		Logger.info("Server shutting down...");
		Logger.info("Goodbye!");
		System.exit(0);
	}

	public void setup(String input) {

		if ("stop".equalsIgnoreCase(input))
			stop();

		switch (getSetupStage()) {

		case 0: {
			Logger.info("Please enter server name");
			break;
		}

		case 1: {
			Setup.SERVER_NAME = input;
			Logger.info("Please enter the amount of RAM in GB");
			break;
		}

		case 2: {
			Setup.RAM = Integer.valueOf(input) * 1024;
			Logger.info("Please enter which template you want to use");
			break;
		}

		case 3: {
			Setup.TEMPLATE = input;
			Logger.info("All right beginning to export...");
			build();
			Logger.info("Done!");
			this.setupStage = 0;
			setup(input);
			return;
		}

		default:
			this.setupStage = 0;
			setup(input);
		}

		this.setupStage++;

	}

	public void build() {

		// Data folders
		final File defaultFiles = new File("templates/" + Setup.TEMPLATE);
		defaultFiles.mkdirs();

		final File root = new File("custom/" + Setup.SERVER_NAME);
		final File pluginDir = new File(root, "plugins");
		pluginDir.mkdirs();

		// Server files

		final File eulaFile = new File(root, "eula.txt");
		final File jvmFlagsFile = new File(root, "jvm-flags.txt");

		final File serverFile = new File(root, "server.properties");
		final File bukkitFile = new File(root, "bukkit.yml");
		final File spigotFile = new File(root, "spigot.yml");
		final File paperGlobalFile = new File(root, "config/paper-global.yml");
		final File paperWorldDefaultFile = new File(root, "config/paper-world-defaults.yml");
		final File purpurFile = new File(root, "purpur.yml");
		final File pufferfishFile = new File(root, "pufferfish.yml");

		// Plugin files

		final File bStatsFile = new File(pluginDir, "bStats/config.yml");

		// Export all files

		copyFiles(defaultFiles, root);

		// Edit all files

		try {

			FileInputStream in = new FileInputStream(serverFile);
			Properties properties = new Properties();
			properties.load(in);
			in.close();

			properties.setProperty("level-name", "world_" + Setup.SERVER_NAME);
			properties.setProperty("server-name", Setup.SERVER_NAME);

			FileOutputStream out = new FileOutputStream(serverFile);
			properties.store(out, null);
			out.close();

			String jvmFlags = new String(Files.readAllBytes(jvmFlagsFile.toPath()));
			jvmFlags = jvmFlags.replace("-Xms", "-Xms" + Setup.RAM + "M");
			jvmFlags = jvmFlags.replace("-Xmx", "-Xms" + Setup.RAM + "M");
			Files.write(jvmFlagsFile.toPath(), jvmFlags.getBytes(StandardCharsets.UTF_8));

			YamlConfiguration paperGlobalConfig = YamlConfiguration.loadConfiguration(paperGlobalFile);
			paperGlobalConfig.set("timings.server-name", Setup.SERVER_NAME);
			paperGlobalConfig.save(paperGlobalFile);

			YamlConfiguration purpurConfig = YamlConfiguration.loadConfiguration(purpurFile);
			purpurConfig.set("settings.server-mod-name", Setup.SERVER_NAME);
			purpurConfig.save(purpurFile);

			YamlConfiguration bStatsConfig = YamlConfiguration.loadConfiguration(bStatsFile);
			bStatsConfig.set("serverUuid", UUID.randomUUID().toString());
			bStatsConfig.save(bStatsFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void copyFiles(File source, File target) {

		try {

			FileUtils.copyDirectory(source, target);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
