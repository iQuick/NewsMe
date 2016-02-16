package me.imli.newme.model;

/**
 * Created by Em on 2015/12/22.
 */
public class Version extends BaseModel {

    public String name;
    public String version;
    public String changelog;
    public int updated_at;
    public String versionShort;
    public String build;
    public String installUrl;
    public String install_url;
    public String direct_install_url;
    public String update_url;
    public BinaryEntity binary;

    public static class BinaryEntity {
        /**
         * fsize : 2640412
         */

        public int fsize;

    }
}
