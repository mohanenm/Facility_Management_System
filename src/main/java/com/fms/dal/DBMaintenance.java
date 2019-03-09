package com.fms.dal;

import com.fms.model.*;
import com.google.common.collect.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DBMaintenance {

  private PreparedStatement insertMaintenanceRequest;

  private PreparedStatement deleteMaintenanceRequest;

  private PreparedStatement insertFacilityMaintenanceRequest;

  private PreparedStatement deleteFacilityMaintenanceRequest;

  private PreparedStatement deleteFromFacilityMaintenanceSchedule;

  private PreparedStatement queryMaintenanceIdFromFacilityId;

  private PreparedStatement queryRoomMaintenanceRequest;

  private PreparedStatement checkRoomAvailability;

  private PreparedStatement queryFacilityMaintenanceRequest;

  public DBMaintenance() throws SQLException {
    insertMaintenanceRequest =
        DBConnection.getConnection()
            .prepareStatement(
                "INSERT into maintenance_request "
                    + "(maintenance_type_id, description, is_vacate_required,"
                    + "is_routine) values (?,?,?,?) "
                    + "RETURNING id");

    deleteMaintenanceRequest =
            DBConnection.getConnection()
                    .prepareStatement(""
                            + "delete from maintenance_request\n"
                            + "where \n"
                            + "    id = ?");



    insertFacilityMaintenanceRequest =
        DBConnection.getConnection()
            .prepareStatement(
                "INSERT into facility_maintenance_request "
                    + "(maintenance_request_id, facility_id) "
                    + " values (?,?) "
                    + "RETURNING id");

    deleteFacilityMaintenanceRequest =
            DBConnection.getConnection().prepareStatement(
                    "delete from facility_maintenance_request\n"
                            + "where \n"
                            + "    id = ?");

    deleteFromFacilityMaintenanceSchedule =
            DBConnection.getConnection().prepareStatement(
                    "delete from facility_maintenance_schedule\n"
                            + "where \n"
                            + "    facility_maintenence_request_id = ?");

    //ToDo: queryRoomMaintenanceRequest = DBConnection.getConnection().prepareStatement("SELECT ");

    queryMaintenanceIdFromFacilityId = DBConnection.getConnection()
            .prepareStatement(
            "select\n" +
                    "    maintenance_request_id\n" +
                    "from\n" +
                    "    facility_maintenance_request\n" +
                    "where\n" +
                    "    id = ?");


    queryFacilityMaintenanceRequest =
        DBConnection.getConnection()
            .prepareStatement(
                ""
                    + "select \n"
                    + "    fmr.id as facility_maintenance_request_id,\n"
                    + "    mr.id as maintenance_request_id,\n"
                    + "    mr.maintenance_type_id,\n"
                    + "    mr.description as description,\n"
                    + "    mr.is_vacate_required as is_vacate_required,\n"
                    + "    mr.is_routine as is_routine \n"
                    + "from \n"
                    + "    facility_maintenance_request fmr\n"
                    + "    join maintenance_request mr on (fmr.maintenance_request_id = mr.id)\n"
                    + "where\n"
                    + "    fmr.id = ?");

    checkRoomAvailability =
        DBConnection.getConnection()
            .prepareStatement(
                ""
                    + "select\n"
                    + "    *\n"
                    + "from \n"
                    + "    facility_maintenance_schedule as fms\n"
                    + "    where \n"
                    + "    (? between fms.start and fms.finish) or\n"
                    + "    (? between fms.start and fms.finish) or\n"
                    + "    (\n"
                    + "        (? < fms.start) and \n"
                    + "        (? > fms.finish)\n"
                    + "    )");
  }

  // Returns the maintenance request with the associated database id for the request
  public FacilityMaintenanceRequest makeFacilityMaintRequest(
      int facilityId, MaintenanceRequest maintenanceRequest) throws SQLException {

    try {

      ///// Insert base maintenance request
      insertMaintenanceRequest.setInt(1, maintenanceRequest.getMaintenanceTypeId());
      insertMaintenanceRequest.setString(2, maintenanceRequest.getDescription());
      insertMaintenanceRequest.setBoolean(3, maintenanceRequest.isVacateRequired());
      insertMaintenanceRequest.setBoolean(4, maintenanceRequest.isRoutine());

      ResultSet resultSet = insertMaintenanceRequest.executeQuery();
      resultSet.next();

      int maintenanceRequestId = resultSet.getInt((1));
      System.out.println("Insert of maint req id -> " + maintenanceRequestId);

      ///// Insert facility maintenance request
      insertFacilityMaintenanceRequest.setInt(1, maintenanceRequestId);
      insertFacilityMaintenanceRequest.setInt(2, facilityId);
      resultSet = insertFacilityMaintenanceRequest.executeQuery();
      resultSet.next();

      int facilityMaintenanceRequestId = resultSet.getInt((1));
      System.out.println("Insert of facility maint req id -> " + facilityMaintenanceRequestId);

      return new FacilityMaintenanceRequest(facilityMaintenanceRequestId, maintenanceRequest);

    } catch (SQLException e) {
      System.out.println("caught exception: " + e.toString());
      throw e;
    }
  }

  public void removeFacilityMaintRequest(
          int facilityMaintenanceRequestId) throws SQLException {
    try {

      deleteFromFacilityMaintenanceSchedule.setInt(1, facilityMaintenanceRequestId);
      deleteFromFacilityMaintenanceSchedule.executeUpdate();


      queryMaintenanceIdFromFacilityId.setInt(1, facilityMaintenanceRequestId);
      ResultSet resultSet = queryMaintenanceIdFromFacilityId.executeQuery();
      resultSet.next();

      int maintenanceId = resultSet.getInt(1);

      deleteFacilityMaintenanceRequest.setInt (1, facilityMaintenanceRequestId);
      deleteMaintenanceRequest.setInt(1, maintenanceId);
    } catch (SQLException e) {
      Logger logger = LogManager.getLogger();
      logger.log(Level.ERROR, "Caught exception: " + e);
    }
  }

  public boolean scheduleRoomMaintenance(
      int roomRequestId, Range<LocalDateTime> maintenancePeriod) {

    /// Look up RoomMaintenanceRequest

    /// See if requested maintenancePeriod available for room

    /// If so attempt insert of maintenancePeriod

    return true;
  }

  public boolean scheduleFacilityMaintenance(
      int facilityRequestId, Range<LocalDateTime> maintenancePeriod) throws SQLException {

    /// Look up RoomMaintenanceRequest

    Timestamp start = Timestamp.valueOf(maintenancePeriod.lowerEndpoint());
    Timestamp finish = Timestamp.valueOf(maintenancePeriod.upperEndpoint());

    checkRoomAvailability.setTimestamp(1, start);
    checkRoomAvailability.setTimestamp(2, finish);
    checkRoomAvailability.setTimestamp(3, start);
    checkRoomAvailability.setTimestamp(4, finish);
    ResultSet resultSet = checkRoomAvailability.executeQuery();

    // If our query returns any records, its a conflict.
    // If next is false, no records, no conflict.
    boolean hasConflict = resultSet.next() != false;

    System.out.println("Has conflict ->" + hasConflict);

    return hasConflict;

    /// See if requested maintenancePeriod available for room

    /// If so attempt insert of maintenancePeriod

  }
}
