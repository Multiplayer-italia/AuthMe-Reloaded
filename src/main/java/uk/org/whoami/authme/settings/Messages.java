/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.whoami.authme.settings;

import java.io.File;
import java.util.HashMap;

public class Messages extends CustomConfiguration {

    private static Messages singleton = null;
    private HashMap<String, String> map;

    public Messages() {
        super(new File(Settings.MESSAGE_FILE));
        loadDefaults();
        loadFile();
        singleton = this;
        
    }
    
    private void loadDefaults() {
        this.set("logged_in", "&cAlready logged in!");
        this.set("not_logged_in", "&cNot logged in!");
        this.set("reg_disabled", "&cRegistration is disabled");
        this.set("user_regged", "&cUsername already registered");
        this.set("usage_reg", "&cUsage: /register password ConfirmPassword");
        this.set("usage_log", "&cUsage: /login password");
        this.set("user_unknown", "&cUsername not registered");
        this.set("pwd_changed", "&cPassword changed!");
        this.set("reg_only", "Registered players only! Please visit http://example.com to register");
        this.set("valid_session", "&cSession login");
        this.set("login_msg", "&cPlease login with \"/login password\"");
        this.set("reg_msg", "&cPlease register with \"/register password ConfirmPassword\"");
        this.set("timeout", "Login Timeout");
        this.set("wrong_pwd", "&cWrong password");
        this.set("logout", "&cSuccessful logout");
        this.set("usage_unreg", "&cUsage: /unregister password");
        this.set("registered", "&cSuccessfully registered!");
        this.set("unregistered", "&cSuccessfully unregistered!");
        this.set("login", "&cSuccessful login!");
        this.set("no_perm", "&cNo Permission");
        this.set("same_nick", "Same nick is already playing");
        this.set("reg_voluntarily", "You can register your nickname with the server with the command \"/register password ConfirmPassword\"");
        this.set("reload", "Configuration and database has been reloaded");
        this.set("error", "An error ocurred; Please contact the admin");
        this.set("unknown_user", "User is not in database");
        this.set("unsafe_spawn","Your Quit location was unsafe, teleporting you to World Spawn");
        this.set("unvalid_session","Session Dataes doesnt corrispond Plaese wait the end of session");
        this.set("max_reg","You have Exeded the max number of Registration for your Account"); 
        this.set("password_error","Password doesnt match");
        this.set("pass_len","Your password dind't reach the minimum length");
        this.set("vb_nonActiv","Your Account isent Activated yet check your Emails!");
        this.set("usage_changepassword", "Usage: /changepassword oldPassword newPassword");
        
    }

	private void loadFile() {
        this.load();
        this.save();
       
    }

    public String _(String msg) {
        String loc = this.getString(msg);
        if (loc != null) {
            return loc.replace("&", "\u00a7");
        }
        return msg;
    }
    
    
    public static Messages getInstance() {
        if (singleton == null) {
            singleton = new Messages();
        }        
        return singleton;
    }
}
