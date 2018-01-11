package org.qiyi.video.svg.bean;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangallen on 2018/1/10.
 */

public class BinderBean implements Parcelable {

    private IBinder binder;
    private String stubImplCanonicalName;

    public BinderBean() {

    }

    public BinderBean(Binder stubImpl) {
        this.binder = stubImpl;
        this.stubImplCanonicalName = stubImpl.getClass().getCanonicalName();
        //this.stubImplCanonicalName=stubImpl.getInterfaceDescriptor();
    }

    public BinderBean(Parcel in) {
        this.binder = in.readStrongBinder();
        this.stubImplCanonicalName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(binder);
        dest.writeString(stubImplCanonicalName);
    }

    public static final Parcelable.Creator<BinderBean> CREATOR = new Creator<BinderBean>() {
        @Override
        public BinderBean createFromParcel(Parcel source) {
            return new BinderBean(source);
        }

        @Override
        public BinderBean[] newArray(int size) {
            return new BinderBean[size];
        }
    };

}
