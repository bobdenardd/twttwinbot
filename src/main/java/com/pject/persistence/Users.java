package com.pject.persistence;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Properties;

/**
 * Users - Short description of the class
 *
 * @author Camille
 *         Last: 04/09/15 14:30
 * @version $Id$
 */
public class Users {

    private Properties userProperties;

    public Users(Properties properties) {
        this.userProperties = properties;
    }

    public void addFollowed(String follower) {
        if(!isAlreadyFollowed(follower)) {
            this.userProperties.put(StringUtils.EMPTY + System.currentTimeMillis(), follower + "##" + System.currentTimeMillis());
        }
    }

    public boolean isAlreadyFollowed(String user) {
        for(Object object : this.userProperties.values()) {
            try {
                if(user.equalsIgnoreCase(object.toString().split("##")[0])) {
                    return true;
                }
            } catch(Exception e) {
                // crapy
            }
        }
        return false;
    }

    public void removeFollow(String user) {
        String key = null;
        for(Map.Entry entry : this.userProperties.entrySet()) {
            if(entry.toString().startsWith(user)) {
                key = entry.getKey().toString();
            }
        }
        if(key != null) {
            this.userProperties.remove(key);
        }
    }

    public Properties getProperties() {
        return this.userProperties;
    }

}
