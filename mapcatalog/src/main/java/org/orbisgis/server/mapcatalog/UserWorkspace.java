package org.orbisgis.server.mapcatalog; /**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Java model of the table UserWorkspace
 */
public class UserWorkspace {
    private String id_user;
    private String id_workspace;
    private String read = "0";
    private String write = "0";
    private String manageUser = "0";

    /**
     * Constructor
     * @param id_user
     * @param id_workspace
     * @param read
     * @param write
     * @param manageUser
     */
    public UserWorkspace(String id_user, String id_workspace, String read, String write, String manageUser) {
        this.id_user = id_user;
        this.id_workspace = id_workspace;
        this.read = read;
        this.write = write;
        this.manageUser = manageUser;
    }

    public String getId_user() {
        return id_user;
    }

    public String getId_workspace() {
        return id_workspace;
    }

    public String getRead() {
        return read;
    }

    public String getWrite() {
        return write;
    }

    public String getManageUser() {
        return manageUser;
    }

    /**
     * Method that saves a instantiated User_Workspace relation into database. Handles SQL injections.
     * @param MC the mapcatalog object for the connection
     * @return The ID of the User just created (primary key)
     */
    public Long save(MapCatalog MC) {
        Long last = null;
        try{
            String query = "INSERT INTO user_workspace (id_user,id_workspace,read,write,manage_user) VALUES (? , ? , ? , ? , ?);";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, id_user);
            pstmt.setString(2, id_workspace);
            pstmt.setString(3, read);
            pstmt.setString(4, write);
            pstmt.setString(5, manageUser);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next()){
                last = rs.getLong(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last;
    }

    /**
     * Deletes a user_workspace relation from database
     * @param MC the mapcatalog object for the connection
     * @param id_user The primary key of the user
     * @param id_workspace The primary key of the workspace
     */
    public static void delete(MapCatalog MC, Long id_user, Long id_workspace) {
        String query = "DELETE FROM user_workspace WHERE id_user = ? AND id_workspace = ?;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_user);
            stmt.setLong(2, id_workspace);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that queries the database for UserWorkspace relations, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of UserWorkspace containing the result of the query
     */
    public static List<UserWorkspace> page(MapCatalog MC, String[] attributes, String[] values){
        String query = "SELECT * FROM user_workspace WHERE ";
        List<UserWorkspace> paged = new LinkedList<UserWorkspace>();
        try {
            //case argument invalid
            if(attributes == null || values == null){
                throw new IllegalArgumentException("Arguments cannot be null");
            }
            if(attributes.length != values.length){
                throw new IllegalArgumentException("String arrays have to be of the same length");
            }
            //preparation of the query
            query+=attributes[0]+" = ?";
            for(int i=1; i<attributes.length; i++){
                if(values[i]==null){
                    query += "AND "+attributes[i]+" IS NULL";
                }else{
                    query += " AND "+attributes[i]+" = ?";
                }
            }
            //preparation of the statement
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            int j=1;
            for(int i=0; i<values.length; i++){
                if(values[i]!=null){
                    stmt.setString(j, values[i]);
                    j++;
                }
            }
            //Retrieving values
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String id_user = rs.getString("id_user");
                String id_workspace = rs.getString("id_workspace");
                String read = rs.getString("read");
                String write = rs.getString("write");
                String manageUser = rs.getString("manage_user");
                UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
                paged.add(usewor);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {e.printStackTrace();}
        return paged;
    }

    /**
     * Querys for a join from user_workspace and User, to get the information about each user linked to a workspace
     * @param MC the mapcatalog object for the connection
     * @param id
     * @return
     */
    public static HashMap<UserWorkspace, User> pageWithUser(MapCatalog MC, String id){
        String query = "SELECT * FROM USER_WORKSPACE JOIN USER ON USER.ID_USER=USER_WORKSPACE.ID_USER WHERE USER_WORKSPACE.ID_WORKSPACE = ?";
        HashMap<UserWorkspace, User> paged = new HashMap<UserWorkspace, User>();
        try {
            //preparation of the statement
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setString(1, id);
            //Retrieving values
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String id_user = rs.getString("id_user");
                String id_workspace = rs.getString("id_workspace");
                String read = rs.getString("read");
                String write = rs.getString("write");
                String manageUser = rs.getString("manage_user");
                UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
                String name = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String location = rs.getString("location");
                String profession = rs.getString("profession");
                String additional = rs.getString("additional");
                User use = new User(id_user, name, email, password, location,profession,additional);
                paged.put(usewor,use);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {e.printStackTrace();}
        return paged;
    }

    /**
     * Execute a query "UPDATE" in the database
     * @param MC the mapcatalog used for database connection
     */
    public void update(MapCatalog MC){
        String query = "UPDATE user_workspace SET read = ? , write = ? , manage_user = ? WHERE id_user = ? AND id_workspace = ?;";
        try {
            //preparation of the statement
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setString(1, read);
            stmt.setString(2, write);
            stmt.setString(3, manageUser);
            stmt.setString(4, id_user);
            stmt.setString(5, id_workspace);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {e.printStackTrace();}
    }
}