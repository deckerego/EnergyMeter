package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import javax.usb.*;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Application extends Controller {
    short vendorId = 0x1781;
    short productId = 0x1111;

    public Result index() {
        try {
            final UsbServices services = UsbHostManager.getUsbServices();
            UsbDevice trinket = findDevice(services.getRootUsbHub(), vendorId, productId);
            String serialNumber = trinket == null ? "Not Found" : trinket.getSerialNumberString();

            return ok(index.render(serialNumber));
        } catch(UsbException | UnsupportedEncodingException e) {
            return internalServerError();
        }
    }

    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }
}
