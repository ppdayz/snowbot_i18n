package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/5
 * @Desc:
 */
public class ContactsBean {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ContactsDataBean> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactsDataBean> contacts) {
        this.contacts = contacts;
    }

    private String id;
    private String type;
    private String role;
    private String status;
    private List<ContactsDataBean> contacts;
}
