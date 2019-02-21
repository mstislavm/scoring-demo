package com.reunico.bpm.vtb;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * This is an easy adapter implementation 
 * illustrating how a Java Delegate can be used 
 * from within a BPMN 2.0 Service Task.
 */
public class JdbcDelegate implements JavaDelegate {
 
  private final Logger LOGGER = Logger.getLogger(JdbcDelegate.class.getName());
  private String columns;
  private String table;
  private String where;
  private String url;
  private String user;
  private String password;
  private String driver = "org.postgresql.Driver";
  private boolean hasResult = false;
  public void execute(DelegateExecution execution) throws Exception {
    columns = execution.getVariable("columns").toString();
    table = execution.getVariable("table").toString();
    where = execution.getVariable("where").toString();
    url = execution.getVariable("url").toString();
    user = execution.getVariable("user").toString();
    password = execution.getVariable("password") != null ? execution.getVariable("password").toString() : null;

    try {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url, user, password);
        Statement statement = connection.createStatement();

        String query =
                "SELECT " + columns +
                        " FROM " + table +
                        " WHERE " + where + "";
        LOGGER.info("Run query: " + query);
        ResultSet resultSet = statement.executeQuery(query);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columns = metaData.getColumnCount();
        ArrayList<HashMap> arrayList = new ArrayList(20);

        // check of result set if empty
        hasResult = !resultSet.isBeforeFirst() ? false : true;

        while (resultSet.next()){
            HashMap<String, Object> row = new HashMap<>(columns);
            for (int i=1; i<=columns; i++){
                row.put(metaData.getColumnName(i),resultSet.getObject(i));
                row.get("");
            }
            arrayList.add(row);

        }
        execution.setVariable("hasResult", hasResult);
        execution.setVariable("queryResult", arrayList);
        LOGGER.info("Query has result? " + hasResult);
        connection.close();
    } catch (Exception e){
        e.printStackTrace();
    }
    LOGGER.info("\n\n  ... JdbcDelegate invoked by "
            + "processDefinitionId=" + execution.getProcessDefinitionId()
            + ", activtyId=" + execution.getCurrentActivityId()
            + ", activtyName='" + execution.getCurrentActivityName() + "'"
            + ", processInstanceId=" + execution.getProcessInstanceId()
            + ", businessKey=" + execution.getProcessBusinessKey()
            + ", executionId=" + execution.getId()
            + " \n\n");

  }

}
