package wang.imallen.blog.moduleexportlib.apple;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Created by wangallen on 2018/1/8.
 */

public class DeliverAppleProxy extends Binder implements IDeliverApple, IInterface {

    private android.os.IBinder mRemote;

    public DeliverAppleProxy(IBinder remote) {
        this.mRemote = remote;
    }

    @Override
    public IBinder asBinder() {
        return mRemote;
    }

    @Override
    public int getApple(int userId) {
        android.os.Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        try {
            data.writeInterfaceToken(DeliverAppleStub.DESCRIPTOR);
            data.writeInt(userId);
            mRemote.transact(DeliverAppleStub.TRANSACTION_getApple, data, reply, 0);
            reply.readException();
            result = reply.readInt();

        } catch (RemoteException ex) {
            ex.printStackTrace();
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    @Override
    public void sendApple(int saleId, int appleNum) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DeliverAppleStub.DESCRIPTOR);
            data.writeInt(saleId);
            data.writeInt(appleNum);
            mRemote.transact(DeliverAppleStub.TRANSACTION_sendApple, data, reply, 0);
            reply.readException();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
