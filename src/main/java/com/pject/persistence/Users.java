package com.pject.persistence;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
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

    private static final long TWO_WEEKS = 1000 * 60 *60 * 24 * 14;
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

    public List<Long> getOldFollowed() {
        List<Long> result = Lists.newArrayList();
        long currentMillis = System.currentTimeMillis();
        for(Map.Entry<Object, Object> entry : this.userProperties.entrySet()) {
            try {
                long millis = Long.parseLong(entry.getValue().toString().split("##")[1]);
                if(currentMillis - millis >= TWO_WEEKS) {
                    result.add((Long) entry.getKey());
                }
            } catch(Exception e) {
                this.userProperties.remove(entry.getKey());
            }
        }
        return result;
    }

    public Properties getProperties() {
        return this.userProperties;
    }

}
