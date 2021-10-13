package org.wildfly.blog.reactive.messaging.common;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class PageVisit {
    private final String userName;
    private final String address;
    private final String page;

    public PageVisit(String userName, String address, String page) {
        this.userName = userName;
        this.address = address;
        this.page = page;
    }

    public String getUserName() {
        return userName;
    }

    public String getAddress() {
        return address;
    }

    public String getPage() {
        return page;
    }

    @Override
    public String toString() {
        return "PageVisit{" +
                "userName='" + userName + '\'' +
                ", address='" + address + '\'' +
                ", page='" + page + '\'' +
                '}';
    }
}
