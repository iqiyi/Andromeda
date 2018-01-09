package wang.imallen.blog.moduleexportlib.apple;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Created by wangallen on 2018/1/8.
 */

public abstract class DeliverAppleStub extends Binder implements IDeliverApple, IInterface {

    public static final int TRANSACTION_getApple = 1;
    public static final int TRANSACTION_sendApple = 2;

    public static final String DESCRIPTOR = DeliverAppleStub.class.getCanonicalName();

    public static IDeliverApple asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
        if (iin != null && (iin instanceof IDeliverApple)) {
            return (IDeliverApple) iin;
        }
        return new DeliverAppleProxy(obj);
    }

    @Override
    public IBinder asBinder() {
        return this;
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case INTERFACE_TRANSACTION: {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            case TRANSACTION_getApple: {
                data.enforceInterface(DESCRIPTOR);
                int userId = data.readInt();
                int result = getApple(userId);
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            }
            case TRANSACTION_sendApple: {
                data.enforceInterface(DESCRIPTOR);
                int saleId = data.readInt();
                int appleNum = data.readInt();
                sendApple(saleId, appleNum);
                reply.writeNoException();
                return true;
            }
        }

        return super.onTransact(code, data, reply, flags);
    }
}
