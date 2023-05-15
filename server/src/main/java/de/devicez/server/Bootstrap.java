package de.devicez.server;

import de.devicez.common.application.GenericBoostrap;

public class Bootstrap {

    public static void main(final String[] args) {
        GenericBoostrap.run(DeviceZServerApplication::new);
    }

}
