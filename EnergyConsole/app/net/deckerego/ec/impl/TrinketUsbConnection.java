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
    // Identifiers for the Trinket on the USB bus
    private static final short VENDOR_ID = 0x1781;
    private static final short PRODUCT_ID = 0x1111;
    private static final byte ENDPOINT_ID = (byte) 0x81;
    private static final short CONFIGURATION_ID = 0x00;

    // For the current calibration coefficient, I'm assuming
    // a 33 Ω ±1% burden resistor over a current transformer that
    // transforms 100A to 50mA, or: (100A / 0.05A) / 33 Ω = 60.6060...
    private static final double CT_RATIO = 100.0 / 0.05;
    private static final double BURDEN_RESISTANCE = 32.9;
    // I'm also assuming a 5V Trinket with a 10-bit DAC, and so:
    // Ic * (5V / 10 bits) = 60.6060 * (5.0 / 1024) = 0.29592803030...
    private static final double VOLTAGE_5V = 4.81;
    private static final int DAC_RANGE = 1024;
    // Ratio used for conversion from RMS is: Ic * (V / Rdac)
    // Where Ic is the current calibration coefficient,
    // V is the accepted voltage of the input pin, and
    // Rdac is the resolution of the digital/analog converter.
    private static final double AMPERAGE_RATIO = (CT_RATIO / BURDEN_RESISTANCE) * (VOLTAGE_5V / DAC_RANGE);
    // An experimentally defined value,
    // this could be noise from a crap circuit design
    private static final double NOISE_REDUCTION = 0.15;

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
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) throw new LibUsbException("Unable to fetch device list", result);

        try {
            for (Device device: list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Failure in reading device", result);

                if (descriptor.idVendor() == VENDOR_ID && descriptor.idProduct() == PRODUCT_ID) return device;
            }
        } finally {
            LibUsb.freeDeviceList(list, true);
        }

        return null;
    }

    @Override
    public double getAmperage() {
        double reduced_amps = (AMPERAGE_RATIO * getRootMeanSquared()) - NOISE_REDUCTION;
        return reduced_amps < 0 ? 0.0 : reduced_amps;
    }

    @Override
    public double getRootMeanSquared() {
        int result = LibUsb.claimInterface(trinketHandle, CONFIGURATION_ID);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Cannot open Trinket interface", result);

        try {
            return Double.parseDouble(read(this.trinketHandle, 5000));
        } finally {
            result = LibUsb.releaseInterface(trinketHandle, CONFIGURATION_ID);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Failure releasing Trinket interface", result);
        }
    }

    private String read(DeviceHandle handle, long timeout) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean started = false;

        while(true) {
            ByteBuffer buffer = BufferUtils.allocateByteBuffer(2).order(ByteOrder.LITTLE_ENDIAN);
            IntBuffer transferred = BufferUtils.allocateIntBuffer();
            int result = LibUsb.bulkTransfer(handle, ENDPOINT_ID, buffer, transferred, timeout);
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
