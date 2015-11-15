package net.deckerego.ec;

import com.google.inject.ImplementedBy;
import net.deckerego.ec.impl.TrinketUsbConnection;

@ImplementedBy(TrinketUsbConnection.class)
public interface UsbConnection {
    void close();
    String readLine();
}
