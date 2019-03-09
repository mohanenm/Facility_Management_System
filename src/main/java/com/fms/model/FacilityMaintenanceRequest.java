package com.fms.model;

import com.google.gson.*;

import java.io.IOException;

public class FacilityMaintenanceRequest {

  public FacilityMaintenanceRequest(int id, MaintenanceRequest maintenanceRequest) {
    this.id = id;
    this.maintenanceRequest = maintenanceRequest;
  }

  public int getId() {
    return id;
  }

  public String toString() {
    GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
    Gson gson = builder.create();
    return gson.toJson(this);
  }

  public MaintenanceRequest getMaintenanceRequest() {
    return maintenanceRequest;
  }

  public static FacilityMaintenanceRequest fromJson(String facilityMaintenanceRequest) throws IOException {
    Gson gson = new GsonBuilder().serializeNulls().create();
    return gson.fromJson(facilityMaintenanceRequest, FacilityMaintenanceRequest.class);
  }

  private int id;
  private MaintenanceRequest maintenanceRequest;

}
