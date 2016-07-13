package org.aweture.wonk.models;

public class Notification {
    public String filter;
    public String period;
    public String date;

    public Notification(String filter, String className, String period, String date) {
        this.filter = filter;
        this.period = period;
        this.date = date;
    }

    public Notification(String notificationString) {
        String[] values = notificationString.split("#");
        filter = values[0];
        period = values[1];
        date = values[2];
    }

    @Override
    public String toString() {
        return filter + "#" + period + "#" + date;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Notification) {
            Notification n = (Notification) o;
            return n.filter.equals(filter) && n.period.equals(period) && n.date.equals(date);
        }
        return false;
    }
}
