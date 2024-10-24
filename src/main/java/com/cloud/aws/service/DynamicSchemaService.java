package com.cloud.aws.service;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud.aws.model.UserRequest;

@Service
public class DynamicSchemaService {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional
	public void updateSchemaAndStoreData(String tableName, Map<String, String> data) throws Exception {
		createTableIfNotExists(tableName);
		Set<String> existingColumns = getExistingColumns(tableName);

		for (Map.Entry<String, String> entry : data.entrySet()) {
			String columnName = entry.getKey();
			if (!existingColumns.contains(columnName.toLowerCase())) {
				addColumn(tableName, columnName);
			}
		}

		insertData(tableName, data);
	}

	private void createTableIfNotExists(String tableName) {
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (id SERIAL PRIMARY KEY)");
	}

	private Set<String> getExistingColumns(String tableName) throws Exception {
		Set<String> columns = new HashSet<>();
		DatabaseMetaData md = dataSource.getConnection().getMetaData();
		ResultSet rs = md.getColumns(null, null, tableName, null);
		while (rs.next()) {
			columns.add(rs.getString("COLUMN_NAME").toLowerCase());
		}
		return columns;
	}

	private void addColumn(String tableName, String columnName) {
		jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " VARCHAR(255)");
	}

	private void insertData(String tableName, Map<String, String> data) {
		StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
		StringBuilder values = new StringBuilder("VALUES (");

		for (String key : data.keySet()) {
			sql.append(key).append(", ");
			values.append("?, ");
		}

		sql.setLength(sql.length() - 2);
		values.setLength(values.length() - 2);

		sql.append(") ").append(values).append(")");

		jdbcTemplate.update(sql.toString(), data.values().toArray());
	}
	/*
	 * public List<UserRequest> retrieveDataByUserId(long id) { String sql =
	 * "SELECT ID, USERID, FILE_NAME FROM INVOICE WHERE USERID = ?";
	 * 
	 * List<UserRequest> results = jdbcTemplate.query(sql, new
	 * RowMapper<UserRequest>() { public UserRequest mapRow(ResultSet rs, int
	 * rowNum) throws SQLException { UserRequest invoice = new UserRequest();
	 * invoice.setUserid(rs.getLong("USERID"));
	 * invoice.setFileName(rs.getString("FILE_NAME"));
	 * invoice.setId(rs.getLong("ID")); return invoice; } }, id);
	 * 
	 * // Log the results if (results.isEmpty()) {
	 * System.out.println("No records found for USERID: " + id); } else {
	 * System.out.println("Records found: " + results); }
	 * 
	 * return results; }
	 */
	
}