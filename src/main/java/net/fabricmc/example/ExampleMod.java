package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("modid");
	private static final JsonParser JSON_PARSER = new JsonParser();  

	public String ADDRESS = "";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		String path = "/Library/Application Support/SteelSeries Engine 3/coreProps.json";
		File file = new File(path);
		String fileData;
		try {

			fileData = Files.toString(file, Charsets.UTF_8);
			JsonObject json = (JsonObject) JSON_PARSER.parse(fileData);

			ADDRESS = json.get("address").getAsString();

			LOGGER.info(ADDRESS);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
