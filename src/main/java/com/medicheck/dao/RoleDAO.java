package com.medicheck.dao;

import com.medicheck.model.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO extends BaseDAO {

    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY name";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                roles.add(new Role(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
        } catch (SQLException e) {
            log.error("findAll roles failed", e);
        } finally {
            release(conn);
        }
        return roles;
    }
}
