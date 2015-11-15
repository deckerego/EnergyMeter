package net.deckerego.ec.impl;

import net.deckerego.ec.UsbConnection;
import org.usb4java.*;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

@Singleton
public class TrinketUsbConnection implements UsbConnection {
    private static Context context;
    private static DeviceHandle trinketHandle;

    @Inject
    public TrinketUsbConnection(ApplicationLifecycle lifecycle) {
        if(this.context == null) this.context = init();
        if(this.trinketHandle == null) this.trinketHandle = openDevice();

        lifecycle.addStopHook(() -> {
            this.close();
            return F.Promise.pure(null);
        });
    }

    @Override
    public void close() {
        LibUsb.close(this.trinketHandle);
        LibUsb.exit(this.context);
    }

    @Override
    protected void finalize() {
        this.close();
    }

    private Context init() {
        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb", result);
        return context;
    }

    private DeviceHandle openDevice() {
        Device trinketDev = findDevice();
        DeviceHandle trinketHandle = new DeviceHandle();
        int result = LibUsb.open(trinketDev, trinketHandle);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Cannot find Trinket", result);
        return trinketHandle;
    }

    private Device findDevice() {
        short vendorId = 0x1781;
        short productId = 0x1111;

        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) throw new LibUsbException("Unable to fetch device list", result);

        try {
            for (Device device: list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Failure in reading device", result);

                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
            }
        } finally {
            LibUsb.freeDeviceList(list, true);
        }

        return null;
    }

    @Override
    public String readLine() {
        int result = LibUsb.claimInterface(trinketHandle, (short) 0x00);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Cannot open Trinket interface", result);

        try {
            return read(this.trinketHandle, 5000);
        } finally {
            result = LibUsb.releaseInterface(trinketHandle, (short) 0x00);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Failure releasing Trinket interface", result);
        }
    }

    private String read(DeviceHandle handle, long timeout) {
        byte endpointId = (byte) 0x81;
        StringBuilder stringBuilder = new StringBuilder();
        boolean started = false;

        while(true) {
            ByteBuffer buffer = BufferUtils.allocateByteBuffer(2).order(ByteOrder.LITTLE_ENDIAN);
            IntBuffer transferred = BufferUtils.allocateIntBuffer();
            int result = LibUsb.bulkTransfer(handle, endpointId, buffer, transferred, timeout);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Failure reading data", result);

            CharBuffer charBuffer = buffer.asCharBuffer();
            while(charBuffer.hasRemaining()) {
                char nextChar = charBuffer.get();
                if(nextChar == '\n') started = true; //Start Character
                else if(nextChar == '\r' && started) return stringBuilder.toString(); //End Character
                else if(started) stringBuilder.append(nextChar);
            }
        }
    }
}
