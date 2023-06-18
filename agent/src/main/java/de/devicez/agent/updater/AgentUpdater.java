package de.devicez.agent.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.devicez.agent.DeviceZAgentApplication;
import de.devicez.agent.installer.AgentInstaller;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AgentUpdater {

    private static final String API_URL = "https://api.github.com/repos/DevicezApp/application/releases/latest";
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    public static String checkForUpdate(final double currentVersion) {
        final Request request = new Request.Builder().url(API_URL).build();
        try (final Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            final ResponseBody body = response.body();
            if (body == null) return null;

            final JsonObject parsedResponse = JsonParser.parseString(body.string()).getAsJsonObject();
            final double version = Double.parseDouble(parsedResponse.get("tag_name").getAsString().substring(1));
            if (version > currentVersion) {
                final JsonArray assets = parsedResponse.get("assets").getAsJsonArray();
                for (final JsonElement element : assets) {
                    final JsonObject object = element.getAsJsonObject();

                    if (object.get("name").getAsString().equals("DeviceZAgent.jar")) {
                        return object.get("browser_download_url").getAsString();
                    }
                }
            }
        } catch (final IOException ignored) {
        }
        return null;
    }

    public static void startUpdate(final DeviceZAgentApplication application, final String downloadUrl) throws IOException {
        final Path targetPath = new File(application.getApplicationFolder(), "DeviceZAgent-update.jar").toPath();

        final Request request = new Request.Builder().url(downloadUrl).build();
        try (final Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            final ResponseBody body = response.body();
            if (body == null) return;

            // Copy updated executable into application folder
            Files.copy(body.byteStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException ignored) {
        }

        // Platform specific update
        switch (application.getPlatform()) {
            case WINDOWS ->
                    new ProcessBuilder("cmd", "/c", "DeviceZService.exe", "restart").directory(application.getApplicationFolder()).start();
            case LINUX ->
                    new ProcessBuilder("systemctl", "restart", "devicez").directory(application.getApplicationFolder()).start();
        }
    }

    public static void finishUpdate(final DeviceZAgentApplication application) throws IOException, URISyntaxException {
        final File executable = new File(AgentInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final Path targetPath = new File(application.getApplicationFolder(), "DeviceZAgent.jar").toPath();
        Files.copy(executable.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void clearWorkingDirectory(final DeviceZAgentApplication application) throws IOException {
        final File updateFile = new File(application.getApplicationFolder(), "DeviceZAgent-update.jar");
        final File lockFile = new File(application.getApplicationFolder(), "update-lock");

        if (updateFile.exists()) Files.delete(updateFile.toPath());
        if (lockFile.exists()) Files.delete(lockFile.toPath());
    }

    public static boolean isUpdateExecutable() throws URISyntaxException {
        final File executable = new File(AgentInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return executable.getName().contains("update");
    }
}
