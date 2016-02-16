package me.imli.newme.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Em on 2015/12/1.
 */
public class Image implements Parcelable {


    /**
     * height : 1024
     * url : http://l.sinaimg.cn/www/dy/slidenews/4_img/2015_49/704_1792113_579723.jpg/original.jpg
     * width : 683
     */
    public String url;
    public int height;
    public int width;


    public Image() {
        this("", 0, 0);
    }

    public Image(String url) {
        this(url, 0, 0);
    }

    public Image(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        return !(url != null ? !url.equals(image.url) : image.url != null);

    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeInt(this.height);
        dest.writeInt(this.width);
    }

    protected Image(Parcel in) {
        this.url = in.readString();
        this.height = in.readInt();
        this.width = in.readInt();
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

}
