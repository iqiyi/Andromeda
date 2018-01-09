package wang.imallen.blog.moduleexportlib.cherry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangallen on 2018/1/8.
 */

public class Cherry implements Parcelable {

    private String color;
    private float weight;

    public Cherry() {

    }

    public Cherry(String color, float weight) {
        this.color = color;
        this.weight = weight;
    }

    public Cherry(Parcel in) {
        this.color = in.readString();
        this.weight = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(color);
        out.writeFloat(weight);
    }


    public static final Parcelable.Creator<Cherry> CREATOR = new Creator<Cherry>() {
        @Override
        public Cherry createFromParcel(Parcel source) {
            return new Cherry(source);
        }

        @Override
        public Cherry[] newArray(int size) {
            return new Cherry[size];
        }
    };

}
