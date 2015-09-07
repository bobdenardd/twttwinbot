package com.pject.helper;

public enum ApiCall {

    FOLLOW("/friendships/outgoing"),
    SEARCH("/search/tweets"),
    RETWEET("/statuses/retweets/:id");

    private String methodName;

    private ApiCall(String methodName) {
        this.methodName = methodName;
    }

    public String call() {
        return this.methodName;
    }

    public static ApiCall forName(String name) {
        for (ApiCall method : values()) {
            if (method.call().equalsIgnoreCase(name)) {
                return method;
            }
        }
        return null;
    }

}
