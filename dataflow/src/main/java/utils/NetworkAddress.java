package utils;

/**
 * util class
 * domain & port
 */
public class NetworkAddress {
    private String domain;
    private int port;

    public NetworkAddress(String domain, int port) {
        this.domain = domain;
        this.port = port;
    }

    public String toString() {
        return new StringBuilder().append(this.domain).append(":").append(this.port).toString();
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        return domain.hashCode() + Integer.hashCode(port);
    }

    public boolean equals(NetworkAddress a) {
        return (this.getDomain().equals(a.getDomain()) && this.getPort() == a.getPort());
    }
}
