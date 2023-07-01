package com.acenkzproject.projectcrud.model;

public class ModelMhs {
    private String id, nim, nama, avatar;

    public ModelMhs(String nim, String nama, String avatar) {
        this.nim = nim;
        this.nama = nama;
        this.avatar = avatar;
    }

    private ModelMhs() {

    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
