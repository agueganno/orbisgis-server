package controllers;

/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 * <p/>
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * <p/>
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 * <p/>
 * This file is part of OrbisGIS.
 * <p/>
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */

import play.data.*;
import play.mvc.*;
import views.html.*;
import org.orbisgis.server.mapcatalog.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import csp.ContentSecurityPolicy;

@ContentSecurityPolicy
public class General extends Controller{
    private static MapCatalog MC = MapCatalogC.getMapCatalog();

    /**
     * Renders the home page
     * @return
     */
    public static Result home() {
        return ok(home.render());
    }

    /**
     * class that represent the login form
     */
    public static class Login {

        public String email;
        public String password;

    }

    /**
     * class that represent the signin form
     */
    public static class Signin {
        public String name;
        public String email;
        public String password;
        public String location;
    }

    /**
     * Renders the login page
     * @return
     */
    public static Result login() {
        return ok(login.render(Form.form(Login.class),""));
    }

    /**
     * Checks if the login form is correct, and logs in the user
     * @return The home page if sucess, the login page with error if error.
     * @throws Exception
     */
    public static Result authenticate() throws Exception{
        Form<Login> form = Form.form(Login.class).bindFromRequest();
        Login log = form.get();
        String email = log.email;
        String password = log.password;
        String error ="";
        if(email != null && password != null){
            String[] attributes = {"email","password"};
            String[] values = {email,MapCatalog.hasher(password)};
            List<User> list = User.page(MC, attributes, values);
            if(!list.isEmpty()){
                session().clear();
                session("email", email);
                session("id_user", list.get(0).getId_user());
                return ok(home.render());
            }else{error="Error: Email or password invalid";}
        }
        return (badRequest(login.render(form,error)));
    }

    /**
     * Clear the cookie session
     * @return The login page
     */
    public static Result logout(){
        session().clear();
        return redirect(routes.General.login());
    }

    /**
     * Renders the sign in page only if no one is logged in
     * @return
     */
    public static Result signin(){
        if(session().get("email")!=null){
            flash("error","You must log out to create another account");
            return forbidden(home.render());
        }
        return ok(signin.render(Form.form(Signin.class),""));
    }

    /**
     * Saves the user that just signed in
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static Result signedin() throws NoSuchAlgorithmException {
        Form<Signin> form = Form.form(Signin.class).bindFromRequest();
        Signin sign = form.get();
        String[] attribute = {"email"};
        String[] values = {sign.email};
        List<User> user = User.page(MC, attribute, values);
        String error="";
        if(sign.email!=null && sign.password.length()>=6){ //check the form
            if(user.isEmpty()){ //check if user mail is used
                User usr = new User(sign.name, sign.email, sign.password, sign.location);
                usr.save(MC);
                return ok(home.render());
            }else{error="Error: Email already used";}
        }else{error="Error: Email or password invalid";}
        return (badRequest(signin.render(form,error)));
    }

    @Security.Authenticated(Secured.class)
    public static Result profilePage() {
        String id_user = session("id_user");
        String[] attributes = {"id_user"};
        String[] values = {id_user};
        User use = User.page(MC, attributes, values).get(0);
        return ok(profile.render(use));
    }

    @Security.Authenticated(Secured.class)
    public static Result changeProfile() {
        String id_user = session("id_user");
        String[] attributes = {"id_user"};
        String[] values = {id_user};
        User temp = User.page(MC, attributes, values).get(0);
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String email = form.get("email");
        String location = form.get("location");
        String profession = form.get("profession");
        String additional = form.get("additional");
        User use = new User(id_user,name,email,temp.getPassword(),location,profession,additional);
        use.update(MC);
        return profilePage();
    }
}