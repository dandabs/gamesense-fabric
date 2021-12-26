package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

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

	JsonObject makeEventArray(String name, Integer data) {

		JsonObject params = new JsonObject();
		JsonObject event = new JsonObject();

		event = new JsonObject();
		event.addProperty("value", data);
		params = new JsonObject();
		params.addProperty("game", "MINECRAFT");
		params.addProperty("event", name);
		params.add("data", event);
		postNoResponse("game_event", params);

		return params;

	}

	private void postNoResponse(String path, JsonObject json) {
		URL url;
		try {
			url = new URL("http://" + ADDRESS + "/" + path);

			URLConnection con;
			try {
				con = url.openConnection();
	
				HttpURLConnection http = (HttpURLConnection)con;
				try {
					http.setRequestMethod("POST");
				} catch (ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // PUT is another valid option
				http.setDoOutput(true);
				
				byte[] out = json.toString().getBytes(StandardCharsets.UTF_8);
				int length = out.length;

				//OutputStream os = con.getOutputStream();
				//os.write(json.toString().getBytes("UTF-8"));
				//os.close();
				
				//http.setFixedLengthStreamingMode(length);
				http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				DataOutputStream out2 = new DataOutputStream(con.getOutputStream());
        		out2.write(out);
        		out2.close();

				try {

					http.connect();
					LOGGER.info(http.getResponseCode());

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

		// initialize the thingy
		JsonObject params = new JsonObject();
		params.addProperty("game", "MINECRAFT");
		params.addProperty("game_display_name", "Minecraft Fabric");
		params.addProperty("developer", "dandabs");
		postNoResponse("game_metadata", params);

		// register health stat
		params = new JsonObject();
		params.addProperty("game", "MINECRAFT");
		params.addProperty("event", "HEALTH");
		params.addProperty("min_value", 0);
		params.addProperty("max_value", 100);
		params.addProperty("icon_id", 38);
		params.addProperty("value_optional", false);
		postNoResponse("register_game_event", params);

		// register hunger stat
		params = new JsonObject();
		params.addProperty("game", "MINECRAFT");
		params.addProperty("event", "HUNGER");
		params.addProperty("min_value", 0);
		params.addProperty("max_value", 100);
		params.addProperty("icon_id", 10);
		params.addProperty("value_optional", false);
		postNoResponse("register_game_event", params);

		// register air stat
		params = new JsonObject();
		params.addProperty("game", "MINECRAFT");
		params.addProperty("event", "AIR");
		params.addProperty("min_value", 0);
		params.addProperty("max_value", 100);
		params.addProperty("icon_id", 11);
		params.addProperty("value_optional", false);
		postNoResponse("register_game_event", params);

		// register tool damage stat
		params = new JsonObject();
		params.addProperty("game", "MINECRAFT");
		params.addProperty("event", "TOOL_DAMAGE");
		params.addProperty("min_value", 0);
		params.addProperty("max_value", 100);
		params.addProperty("icon_id", 13);
		params.addProperty("value_optional", false);
		postNoResponse("register_game_event", params);

		// start heartbeat 15s to server
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				JsonObject params = new JsonObject();
				params.addProperty("game", "MINECRAFT");
				postNoResponse("game_heartbeat", params);
			}
		  }, 0, 14000); // 1000ms = 1s // we'll run it as 14 seconds just to be safe and hope it doesnt time out on us

		// start relaying info to server
		Timer timer2 = new Timer();
		timer2.schedule(new TimerTask() {
			@Override
			public void run() {
				if (MinecraftClient.getInstance().player != null) {
					postNoResponse("game_event", makeEventArray("HEALTH", 5* Math.round(MinecraftClient.getInstance().player.getHealth())));
					postNoResponse("game_event", makeEventArray("HUNGER", 5* Math.round(MinecraftClient.getInstance().player.getHungerManager().getFoodLevel())));
					postNoResponse("game_event", makeEventArray("AIR", Math.round(MinecraftClient.getInstance().player.getAir() / 3)));
					postNoResponse("game_event", makeEventArray("TOOL_DAMAGE", Math.round(MinecraftClient.getInstance().player.getInventory().getMainHandStack().getDamage() / MinecraftClient.getInstance().player.getInventory().getMainHandStack().getMaxDamage()) * 100));
					LOGGER.info(Math.round(MinecraftClient.getInstance().player.getHealth()));
				}
			}
		  }, 0, 1000); // again, 1000ms = 1s

	}
}
