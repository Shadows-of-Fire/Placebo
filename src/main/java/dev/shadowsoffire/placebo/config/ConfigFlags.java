package dev.shadowsoffire.placebo.config;

public class ConfigFlags {

    public static enum Type {
        COMMON("Common"), // A Common property is important on both client and server, but is not (or cannot be) synced from client to server.
        SYNCED("Synced"), // A Synced property is important on both client and server, and is synced on login to the client, so it is server-authorative.
        SERVER("Server"), // A Server property is important only on the server. Its value is not transmitted to the client, as it does not need to be.
        CLIENT("Client"); // A Client property is important only on the client. Changing this value on the server-side would have no effect.

        String name;

        private Type(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    public static enum Loadability {
        LOCKED("Locked"),			// A Locked property cannot be changed at runtime and requires a full game restart.
        RELOADABLE("Reloadable"),	// A Reloadable property will be updated whenever a data reload occurs.
        RESTARTABLE("Restartable"); // A Restartable property will be updated whenever a new save file is loaded. On a server, this is impossible, so it is equivalent to Locked.

        String name;

        private Loadability(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

}
