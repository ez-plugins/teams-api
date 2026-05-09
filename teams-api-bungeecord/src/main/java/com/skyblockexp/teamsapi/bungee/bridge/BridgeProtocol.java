package com.skyblockexp.teamsapi.bungee.bridge;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines the {@code teamsapi:bridge} plugin-messaging protocol.
 *
 * <p>Messages in both directions are UTF-8 encoded JSON byte arrays.</p>
 *
 * <p><b>Request format</b> (BungeeCord proxy to backend):</p>
 * <pre>{@code {"reqId":"<uuid>","op":"<operation>","<argKey>":"<argVal>",...}}</pre>
 *
 * <p><b>Response format</b> (backend to proxy):</p>
 * <pre>{@code {"reqId":"<uuid>","ok":true,...result fields...}}</pre>
 * <pre>{@code {"reqId":"<uuid>","ok":false,"error":"<message>"}}</pre>
 *
 * <p>All string values containing backslashes or double-quotes are escaped.</p>
 */
public final class BridgeProtocol {

    /** The plugin messaging channel name used for all bridge traffic. */
    public static final String CHANNEL_NAME = "teamsapi:bridge";

    /** Utility class -- not instantiable. */
    private BridgeProtocol() {
    }

    /**
     * Builds a UTF-8 encoded JSON request payload.
     *
     * @param reqId the unique request identifier
     * @param op    the operation name (e.g. {@code "getPlayerTeam"})
     * @param args  additional string arguments to include in the JSON object
     * @return the serialized request as a byte array
     */
    public static byte[] buildRequest(final String reqId, final String op,
            final Map<String, String> args) {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"reqId\":").append(quoted(reqId));
        sb.append(",\"op\":").append(quoted(op));
        for (final Map.Entry<String, String> e : args.entrySet()) {
            sb.append(",").append(quoted(e.getKey())).append(":").append(quoted(e.getValue()));
        }
        sb.append("}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Parses a JSON response received from the backend.
     * Returns a simple string-keyed map of the top-level fields.
     *
     * @param data the raw response bytes
     * @return the response object, or an empty map on parse failure
     */
    public static Map<String, String> parseResponse(final byte[] data) {
        final String json = new String(data, StandardCharsets.UTF_8);
        final Map<String, String> result = new LinkedHashMap<>();
        final int start = json.indexOf('{');
        final int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return result;
        }
        String body = json.substring(start + 1, end);
        while (!body.isEmpty()) {
            body = body.stripLeading();
            if (body.isEmpty() || body.charAt(0) != '"') {
                break;
            }
            final int keyEnd = body.indexOf('"', 1);
            if (keyEnd < 0) {
                break;
            }
            final String key = body.substring(1, keyEnd);
            final int colon = body.indexOf(':', keyEnd);
            if (colon < 0) {
                break;
            }
            body = body.substring(colon + 1).stripLeading();
            final String value;
            if (!body.isEmpty() && body.charAt(0) == '"') {
                final int vEnd = body.indexOf('"', 1);
                if (vEnd < 0) {
                    break;
                }
                value = body.substring(1, vEnd).replace("\\\"", "\"").replace("\\\\", "\\");
                body = body.substring(vEnd + 1);
            }
            else {
                int vEnd = 0;
                int depth = 0;
                while (vEnd < body.length()) {
                    final char ch = body.charAt(vEnd);
                    if (ch == '{' || ch == '[') {
                        depth++;
                    }
                    else if (ch == '}' || ch == ']') {
                        if (depth == 0) {
                            break;
                        }
                        depth--;
                    }
                    else if (ch == ',' && depth == 0) {
                        break;
                    }
                    vEnd++;
                }
                value = body.substring(0, vEnd).strip();
                body = body.substring(vEnd);
            }
            result.put(key, value);
            body = body.stripLeading();
            if (!body.isEmpty() && body.charAt(0) == ',') {
                body = body.substring(1);
            }
        }
        return result;
    }

    /**
     * Convenience getter for a string field from a parsed response map.
     * Returns {@code null} if the key is absent or the value is the JSON literal {@code null}.
     *
     * @param map the parsed response map
     * @param key the field name
     * @return the field value, or {@code null}
     */
    public static String getString(final Map<String, String> map, final String key) {
        final String val = map.get(key);
        return "null".equals(val) ? null : val;
    }

    /**
     * Convenience getter for a boolean field from a parsed response map.
     *
     * @param map          the parsed response map
     * @param key          the field name
     * @param defaultValue value returned when the key is absent
     * @return the boolean value, or {@code defaultValue}
     */
    public static boolean getBool(final Map<String, String> map,
            final String key, final boolean defaultValue) {
        final String val = map.get(key);
        if (val == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(val);
    }

    /**
     * Convenience getter for an integer field from a parsed response map.
     *
     * @param map          the parsed response map
     * @param key          the field name
     * @param defaultValue value returned when the key is absent or unparseable
     * @return the integer value, or {@code defaultValue}
     */
    public static int getInt(final Map<String, String> map,
            final String key, final int defaultValue) {
        final String val = map.get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns a JSON-safe double-quoted string.
     *
     * @param value the raw string value
     * @return the quoted and escaped JSON string token
     */
    private static String quoted(final String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
