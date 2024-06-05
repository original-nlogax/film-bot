

public static class MediaStream {
    private final String type;
    private final Map<String, String> entries;

    public MediaStream (Map<String, String> entries) {
        this.entries = entries;
        this.type = entries.get("codec_type");	// "audio" or "video"
    }

    public String getType () {
        return type;
    }

    public boolean has (String entry) {
        return entry != null
            && entries.get(entry) != null
            && !entries.get(entry).equals("N/A");
    }

    public float getFloat (String entry) {
        return Float.parseFloat(entries.get(entry));
    }

    public int getInt (String entry) {
        return Integer.parseInt(entries.get(entry));
    }

    public byte getByte (String entry) {
        return Byte.parseByte(entries.get(entry));
    }

    public String get (String entry) {
        return entries.get(entry);
    }

    public byte getFramerate () {
        int videoTotalFrames =
            Integer.parseInt(entries.get("avg_frame_rate").split("/")[0]);
        int videoTotalSeconds =
            Integer.parseInt(entries.get("avg_frame_rate").split("/")[1]);
        return (byte)(Math.round(videoTotalFrames / videoTotalSeconds));
    }

    @Override
    public String toString () {
        return "MediaStream{"
            + "type=" + type + ";"
            + "entries=" + entries + "}";
    }
}
