package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/5
 * @Desc:
 */

public class OnLineContent {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ContactsBean> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactsBean> contacts) {
        this.contacts = contacts;
    }

    private String id;
    private String type;
    private String status;
    private List<ContactsBean> contacts;
}
