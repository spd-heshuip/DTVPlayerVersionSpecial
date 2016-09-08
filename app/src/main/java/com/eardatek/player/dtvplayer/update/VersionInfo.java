package com.eardatek.player.dtvplayer.update;

/**
 * Created by tomato on 2016/8/17.
 */
public class VersionInfo {
    private Double version;
    private Long   size;
    private String url;
    private String filename;
    private String sha1;

    VersionInfo(Double version, String filename,Long size,String sha1, String url){
        this.version = version;
        this.filename = filename;
        this.size = size;
        this.sha1 = sha1;
        this.url = url;

    }
    public void setSize(Long size) {
        this.size = size;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public Double getVersion() {
        return version;
    }

    public Long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    public String getSha1() {
        return sha1;
    }

    @Override
    public String toString() {
        return "Version:" + version +
                "\n" + "Filename:" + filename +
                "\n" + "FileSize:" + size +
                "\n" + "Shal" + sha1 +
                "\n" + "DownLoadUrl" + url;
    }
}
