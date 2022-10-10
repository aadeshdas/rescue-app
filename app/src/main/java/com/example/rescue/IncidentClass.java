package com.example.rescue;

public class IncidentClass {
    private double longitude1;
    private double latitude1;
    private double longitude2;
    private double latitude2;
    private double longitude3;
    private double latitude3;
    private double longitude4;
    private double latitude4;
    private double longitude5;
    private double latitude5;
    private String IDval;
    private String url;
    private String date;

    public IncidentClass(){
        IDval = "NILL ID Val";
        url = "NILL URL Val";
        date = "NILL DATE val";
        latitude1 = 0.0;
        longitude1 = 0.0;
        latitude2 = 0.0;
        longitude2 = 0.0;
        latitude3 = 0.0;
        longitude3 = 0.0;
        latitude4 = 0.0;
        longitude4 = 0.0;
        latitude5 = 0.0;
        longitude5 = 0.0;
    }

    public IncidentClass(String IDval, String url , String date,double longitude1, double latitude1,double longitude2, double latitude2,double longitude3, double latitude3,double longitude4, double latitude4,double longitude5, double latitude5) {
        this.IDval = IDval;
        this.url = url;
        this.date = date;
        this.longitude1 = longitude1;
        this.latitude1 = latitude1;
        this.longitude2 = longitude2;
        this.latitude2 = latitude2;
        this.longitude3 = longitude3;
        this.latitude3 = latitude3;
        this.longitude4 = longitude4;
        this.latitude4 = latitude4;
        this.longitude5 = longitude5;
        this.latitude5 = latitude5;
    }

    public String getIDval() {
        return IDval;
    }

    public void setIDval(String IDval) {
        this.IDval = IDval;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLongitude1() {
        return longitude1;
    }

    public void setLongitude1(double longitude1) {
        this.longitude1 = longitude1;
    }

    public double getLatitude1() {
        return latitude1;
    }

    public void setLatitude1(double latitude1) {
        this.latitude1 = latitude1;
    }

    public double getLongitude2() {
        return longitude2;
    }

    public void setLongitude2(double longitude2) {
        this.longitude2 = longitude2;
    }

    public double getLatitude2() {
        return latitude2;
    }

    public void setLatitude2(double latitude2) {
        this.latitude2 = latitude2;
    }

    public double getLongitude3() {
        return longitude3;
    }

    public void setLongitude3(double longitude3) {
        this.longitude3 = longitude3;
    }

    public double getLatitude3() {
        return latitude3;
    }

    public void setLatitude3(double latitude3) {
        this.latitude3 = latitude3;
    }

    public double getLongitude4() {
        return longitude4;
    }

    public void setLongitude4(double longitude4) {
        this.longitude4 = longitude4;
    }

    public double getLatitude4() {
        return latitude4;
    }

    public void setLatitude4(double latitude4) {
        this.latitude4 = latitude4;
    }

    public double getLongitude5() {
        return longitude5;
    }

    public void setLongitude5(double longitude5) {
        this.longitude5 = longitude5;
    }

    public double getLatitude5() {
        return latitude5;
    }

    public void setLatitude5(double latitude5) {
        this.latitude5 = latitude5;
    }
}

