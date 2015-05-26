/*
*************************************************************************
Copyright (c) 2011-2014:
Istituto Nazionale di Fisica Nucleare (INFN), Italy
Consorzio COMETA (COMETA), Italy

See http://www.infn.it and and http://www.consorzio-cometa.it for details on
the copyright holders.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author <a href="mailto:giuseppe.larocca@ct.infn.it">Giuseppe La Rocca</a>
***************************************************************************
*/
package it.infn.ct.nuclemd;

// import liferay libraries
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.io.OutputStreamWriter;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;

// import DataEngine libraries
import com.liferay.portal.util.PortalUtil;
import it.infn.ct.GridEngine.InformationSystem.BDII;
import it.infn.ct.GridEngine.Job.*;

// import generic Java libraries
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URI;

// import portlet libraries
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

// Importing Apache libraries
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Nuclemd extends GenericPortlet {

    private static Log log = LogFactory.getLog(Nuclemd.class);   

    @Override
    protected void doEdit(RenderRequest request,
            RenderResponse response)
            throws PortletException, IOException
    {

        PortletPreferences portletPreferences =
                (PortletPreferences) request.getPreferences();

        response.setContentType("text/html");
        
        // Getting the LATO INFRASTRUCTURE from the portlet preferences
        String lato_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("lato_nuclemd_INFRASTRUCTURE", "N/A");
        // Getting the login credential from the portlet preferences for LATO
        String lato_nuclemd_LOGIN = portletPreferences.getValue("lato_nuclemd_LOGIN", "N/A");
        // Getting the password credential from the portlet preferences for LATO
        String lato_nuclemd_PASSWD = portletPreferences.getValue("lato_nuclemd_PASSWD", "N/A");
        // Getting the cluster hostname(s) from the portlet preferences for LATO
        String[] lato_nuclemd_WMS = portletPreferences.getValues("lato_nuclemd_WMS", new String[5]);
        // Getting the ETOKENSERVER from the portlet preferences for LATO
        String lato_nuclemd_ETOKENSERVER = portletPreferences.getValue("lato_nuclemd_ETOKENSERVER", "N/A");
        // Getting the MYPROXYSERVER from the portlet preferences for LATO
        String lato_nuclemd_MYPROXYSERVER = portletPreferences.getValue("lato_nuclemd_MYPROXYSERVER", "N/A");
        // Getting the PORT from the portlet preferences for LATO
        String lato_nuclemd_PORT = portletPreferences.getValue("lato_nuclemd_PORT", "N/A");
        // Getting the ROBOTID from the portlet preferences for LATO
        String lato_nuclemd_ROBOTID = portletPreferences.getValue("lato_nuclemd_ROBOTID", "N/A");
        // Getting the WEBDAV Server for LATO
        String lato_nuclemd_WEBDAV = portletPreferences.getValue("lato_nuclemd_WEBDAV", "N/A");
        // Getting the ROLE from the portlet preferences for LATO
        String lato_nuclemd_ROLE = portletPreferences.getValue("lato_nuclemd_ROLE", "N/A");
        // Getting the RENEWAL from the portlet preferences for LATO
        String lato_nuclemd_RENEWAL = portletPreferences.getValue("lato_nuclemd_RENEWAL", "checked");
        // Getting the DISABLEVOMS from the portlet preferences for LATO
        String lato_nuclemd_DISABLEVOMS = portletPreferences.getValue("lato_nuclemd_DISABLEVOMS", "unchecked");

        // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("garuda_nuclemd_INFRASTRUCTURE", "N/A");
        // Getting the NUCLEMD VONAME from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_VONAME = portletPreferences.getValue("garuda_nuclemd_VONAME", "N/A");
        // Getting the NUCLEMD TOPPBDII from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_TOPBDII = portletPreferences.getValue("garuda_nuclemd_TOPBDII", "N/A");
        // Getting the NUCLEMD WMS from the portlet preferences for the GARUDA VO
        String[] garuda_nuclemd_WMS = portletPreferences.getValues("garuda_nuclemd_WMS", new String[5]);
        // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_ETOKENSERVER = portletPreferences.getValue("garuda_nuclemd_ETOKENSERVER", "N/A");
        // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_MYPROXYSERVER = portletPreferences.getValue("garuda_nuclemd_MYPROXYSERVER", "N/A");
        // Getting the NUCLEMD PORT from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_PORT = portletPreferences.getValue("garuda_nuclemd_PORT", "N/A");
        // Getting the NUCLEMD ROBOTID from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_ROBOTID = portletPreferences.getValue("garuda_nuclemd_ROBOTID", "N/A");
        // Getting the WEBDAV Server for GARUDA
        String garuda_nuclemd_WEBDAV = portletPreferences.getValue("garuda_nuclemd_WEBDAV", "N/A");
        // Getting the NUCLEMD ROLE from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_ROLE = portletPreferences.getValue("garuda_nuclemd_ROLE", "N/A");
        // Getting the NUCLEMD RENEWAL from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_RENEWAL = portletPreferences.getValue("garuda_nuclemd_RENEWAL", "checked");
        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GARUDA VO
        String garuda_nuclemd_DISABLEVOMS = portletPreferences.getValue("garuda_nuclemd_DISABLEVOMS", "unchecked");

        // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the EUMED VO
        String eumed_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("eumed_nuclemd_INFRASTRUCTURE", "N/A");
        // Getting the NUCLEMD VONAME from the portlet preferences for the EUMED VO
        String eumed_nuclemd_VONAME = portletPreferences.getValue("eumed_nuclemd_VONAME", "N/A");
        // Getting the NUCLEMD TOPPBDII from the portlet preferences for the EUMED VO
        String eumed_nuclemd_TOPBDII = portletPreferences.getValue("eumed_nuclemd_TOPBDII", "N/A");
        // Getting the NUCLEMD WMS from the portlet preferences for the EUMED VO
        String[] eumed_nuclemd_WMS = portletPreferences.getValues("eumed_nuclemd_WMS", new String[5]);
        // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the EUMED VO
        String eumed_nuclemd_ETOKENSERVER = portletPreferences.getValue("eumed_nuclemd_ETOKENSERVER", "N/A");
        // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the EUMED VO
        String eumed_nuclemd_MYPROXYSERVER = portletPreferences.getValue("eumed_nuclemd_MYPROXYSERVER", "N/A");
        // Getting the NUCLEMD PORT from the portlet preferences for the EUMED VO
        String eumed_nuclemd_PORT = portletPreferences.getValue("eumed_nuclemd_PORT", "N/A");
        // Getting the NUCLEMD ROBOTID from the portlet preferences for the EUMED VO
        String eumed_nuclemd_ROBOTID = portletPreferences.getValue("eumed_nuclemd_ROBOTID", "N/A");
        // Getting the WEBDAV Server for EUMED
        String eumed_nuclemd_WEBDAV = portletPreferences.getValue("eumed_nuclemd_WEBDAV", "N/A");
        // Getting the NUCLEMD ROLE from the portlet preferences for the EUMED VO
        String eumed_nuclemd_ROLE = portletPreferences.getValue("eumed_nuclemd_ROLE", "N/A");
        // Getting the NUCLEMD RENEWAL from the portlet preferences for the EUMED VO
        String eumed_nuclemd_RENEWAL = portletPreferences.getValue("eumed_nuclemd_RENEWAL", "checked");
        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the EUMED VO
        String eumed_nuclemd_DISABLEVOMS = portletPreferences.getValue("eumed_nuclemd_DISABLEVOMS", "unchecked");
        
        // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("sagrid_nuclemd_INFRASTRUCTURE", "N/A");
        // Getting the NUCLEMD VONAME from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_VONAME = portletPreferences.getValue("sagrid_nuclemd_VONAME", "N/A");
        // Getting the NUCLEMD TOPPBDII from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_TOPBDII = portletPreferences.getValue("sagrid_nuclemd_TOPBDII", "N/A");
        // Getting the NUCLEMD WMS from the portlet preferences for the SAGRID VO
        String[] sagrid_nuclemd_WMS = portletPreferences.getValues("sagrid_nuclemd_WMS", new String[5]);
        // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_ETOKENSERVER = portletPreferences.getValue("sagrid_nuclemd_ETOKENSERVER", "N/A");
        // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_MYPROXYSERVER = portletPreferences.getValue("sagrid_nuclemd_MYPROXYSERVER", "N/A");
        // Getting the NUCLEMD PORT from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_PORT = portletPreferences.getValue("sagrid_nuclemd_PORT", "N/A");
        // Getting the NUCLEMD ROBOTID from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_ROBOTID = portletPreferences.getValue("sagrid_nuclemd_ROBOTID", "N/A");
        // Getting the WEBDAV Server for SAGRID
        String sagrid_nuclemd_WEBDAV = portletPreferences.getValue("sagrid_nuclemd_WEBDAV", "N/A");
        // Getting the NUCLEMD ROLE from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_ROLE = portletPreferences.getValue("sagrid_nuclemd_ROLE", "N/A");
        // Getting the NUCLEMD RENEWAL from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_RENEWAL = portletPreferences.getValue("sagrid_nuclemd_RENEWAL", "checked");
        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the SAGRID VO
        String sagrid_nuclemd_DISABLEVOMS = portletPreferences.getValue("sagrid_nuclemd_DISABLEVOMS", "unchecked");

        // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the SEE VO
        String see_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("see_nuclemd_INFRASTRUCTURE", "N/A");
        // Getting the NUCLEMD VONAME from the portlet preferences for the SEE VO
        String see_nuclemd_VONAME = portletPreferences.getValue("see_nuclemd_VONAME", "N/A");
        // Getting the NUCLEMD TOPPBDII from the portlet preferences for the SEE VO
        String see_nuclemd_TOPBDII = portletPreferences.getValue("see_nuclemd_TOPBDII", "N/A");
        // Getting the NUCLEMD WMS from the portlet preferences for the SEE VO
        String[] see_nuclemd_WMS = portletPreferences.getValues("see_nuclemd_WMS", new String[5]);
        // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the SEE VO
        String see_nuclemd_ETOKENSERVER = portletPreferences.getValue("see_nuclemd_ETOKENSERVER", "N/A");
        // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the SEE VO
        String see_nuclemd_MYPROXYSERVER = portletPreferences.getValue("see_nuclemd_MYPROXYSERVER", "N/A");
        // Getting the NUCLEMD PORT from the portlet preferences for the SEE VO
        String see_nuclemd_PORT = portletPreferences.getValue("see_nuclemd_PORT", "N/A");
        // Getting the NUCLEMD ROBOTID from the portlet preferences for the SEE VO
        String see_nuclemd_ROBOTID = portletPreferences.getValue("see_nuclemd_ROBOTID", "N/A");
        // Getting the WEBDAV Server for SEE
        String see_nuclemd_WEBDAV = portletPreferences.getValue("see_nuclemd_WEBDAV", "N/A");
        // Getting the NUCLEMD ROLE from the portlet preferences for the SEE VO
        String see_nuclemd_ROLE = portletPreferences.getValue("see_nuclemd_ROLE", "N/A");
        // Getting the NUCLEMD RENEWAL from the portlet preferences for the SEE VO
        String see_nuclemd_RENEWAL = portletPreferences.getValue("see_nuclemd_RENEWAL", "checked");
        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the SEE VO
        String see_nuclemd_DISABLEVOMS = portletPreferences.getValue("see_nuclemd_DISABLEVOMS", "unchecked");
        
        // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("gridit_nuclemd_INFRASTRUCTURE", "N/A");
        // Getting the NUCLEMD VONAME from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_VONAME = portletPreferences.getValue("gridit_nuclemd_VONAME", "N/A");
        // Getting the NUCLEMD TOPPBDII from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_TOPBDII = portletPreferences.getValue("gridit_nuclemd_TOPBDII", "N/A");
        // Getting the NUCLEMD WMS from the portlet preferences for the GRIDIT VO
        String[] gridit_nuclemd_WMS = portletPreferences.getValues("gridit_nuclemd_WMS", new String[5]);
        // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_ETOKENSERVER = portletPreferences.getValue("gridit_nuclemd_ETOKENSERVER", "N/A");
        // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_MYPROXYSERVER = portletPreferences.getValue("gridit_nuclemd_MYPROXYSERVER", "N/A");
        // Getting the NUCLEMD PORT from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_PORT = portletPreferences.getValue("gridit_nuclemd_PORT", "N/A");
        // Getting the NUCLEMD ROBOTID from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_ROBOTID = portletPreferences.getValue("gridit_nuclemd_ROBOTID", "N/A");
        // Getting the WEBDAV Server for GRIDIT
        String gridit_nuclemd_WEBDAV = portletPreferences.getValue("gridit_nuclemd_WEBDAV", "N/A");
        // Getting the NUCLEMD ROLE from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_ROLE = portletPreferences.getValue("gridit_nuclemd_ROLE", "N/A");
        // Getting the NUCLEMD RENEWAL from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_RENEWAL = portletPreferences.getValue("gridit_nuclemd_RENEWAL", "checked");
        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GRIDIT VO
        String gridit_nuclemd_DISABLEVOMS = portletPreferences.getValue("gridit_nuclemd_DISABLEVOMS", "unchecked");

        // Getting the NUCLEMD APPID from the portlet preferences
        String nuclemd_APPID = portletPreferences.getValue("nuclemd_APPID", "N/A");
        // Getting the LOG LEVEL from the portlet preferences
        String nuclemd_LOGLEVEL = portletPreferences.getValue("nuclemd_LOGLEVEL", "INFO");
        // Getting the METADATA METADATA_HOST from the portlet preferences
        String nuclemd_METADATA_HOST = portletPreferences.getValue("nuclemd_METADATA_HOST", "N/A");
        // Getting the NUCLEMD OUTPUT_PATH from the portlet preferences
        String nuclemd_OUTPUT_PATH = portletPreferences.getValue("nuclemd_OUTPUT_PATH", "/tmp");
        // Getting the NUCLEMD SFOTWARE from the portlet preferences
        String nuclemd_SOFTWARE = portletPreferences.getValue("nuclemd_SOFTWARE", "N/A");
        // Getting the TRACKING_DB_HOSTNAME from the portlet preferences
        String TRACKING_DB_HOSTNAME = portletPreferences.getValue("TRACKING_DB_HOSTNAME", "N/A");
        // Getting the TRACKING_DB_USERNAME from the portlet preferences
        String TRACKING_DB_USERNAME = portletPreferences.getValue("TRACKING_DB_USERNAME", "N/A");
        // Getting the TRACKING_DB_PASSWORD from the portlet preferences
        String TRACKING_DB_PASSWORD = portletPreferences.getValue("TRACKING_DB_PASSWORD", "N/A");
        // Getting the SMTP_HOST from the portlet preferences
        String SMTP_HOST = portletPreferences.getValue("SMTP_HOST", "N/A");
        // Getting the SENDER MAIL from the portlet preferences
        String SENDER_MAIL = portletPreferences.getValue("SENDER_MAIL", "N/A");
        // Get the list of enabled Infrastructures
        String[] infras = portletPreferences.getValues("nuclemd_ENABLEINFRASTRUCTURE", new String[3]);

        // Set the default portlet preferences
        request.setAttribute("garuda_nuclemd_INFRASTRUCTURE", garuda_nuclemd_INFRASTRUCTURE.trim());
        request.setAttribute("garuda_nuclemd_VONAME", garuda_nuclemd_VONAME.trim());
        request.setAttribute("garuda_nuclemd_TOPBDII", garuda_nuclemd_TOPBDII.trim());
        request.setAttribute("garuda_nuclemd_WMS", garuda_nuclemd_WMS);
        request.setAttribute("garuda_nuclemd_ETOKENSERVER", garuda_nuclemd_ETOKENSERVER.trim());
        request.setAttribute("garuda_nuclemd_MYPROXYSERVER", garuda_nuclemd_MYPROXYSERVER.trim());
        request.setAttribute("garuda_nuclemd_PORT", garuda_nuclemd_PORT.trim());
        request.setAttribute("garuda_nuclemd_ROBOTID", garuda_nuclemd_ROBOTID.trim());
        request.setAttribute("garuda_nuclemd_WEBDAV", garuda_nuclemd_WEBDAV.trim());
        request.setAttribute("garuda_nuclemd_ROLE", garuda_nuclemd_ROLE.trim());
        request.setAttribute("garuda_nuclemd_RENEWAL", garuda_nuclemd_RENEWAL);
        request.setAttribute("garuda_nuclemd_DISABLEVOMS", garuda_nuclemd_DISABLEVOMS);
        
        request.setAttribute("lato_nuclemd_INFRASTRUCTURE", lato_nuclemd_INFRASTRUCTURE.trim());
        request.setAttribute("lato_nuclemd_LOGIN", lato_nuclemd_LOGIN.trim());
        request.setAttribute("lato_nuclemd_PASSWD", lato_nuclemd_PASSWD.trim());
        request.setAttribute("lato_nuclemd_WMS", lato_nuclemd_WMS);
        request.setAttribute("lato_nuclemd_ETOKENSERVER", lato_nuclemd_ETOKENSERVER.trim());
        request.setAttribute("lato_nuclemd_MYPROXYSERVER", lato_nuclemd_MYPROXYSERVER.trim());
        request.setAttribute("lato_nuclemd_PORT", lato_nuclemd_PORT.trim());
        request.setAttribute("lato_nuclemd_ROBOTID", lato_nuclemd_ROBOTID.trim());
        request.setAttribute("lato_nuclemd_WEBDAV", lato_nuclemd_WEBDAV.trim());
        request.setAttribute("lato_nuclemd_ROLE", lato_nuclemd_ROLE.trim());
        request.setAttribute("lato_nuclemd_RENEWAL", lato_nuclemd_RENEWAL);
        request.setAttribute("lato_nuclemd_DISABLEVOMS", lato_nuclemd_DISABLEVOMS);

        request.setAttribute("eumed_nuclemd_INFRASTRUCTURE", eumed_nuclemd_INFRASTRUCTURE.trim());
        request.setAttribute("eumed_nuclemd_VONAME", eumed_nuclemd_VONAME.trim());
        request.setAttribute("eumed_nuclemd_TOPBDII", eumed_nuclemd_TOPBDII.trim());
        request.setAttribute("eumed_nuclemd_WMS", eumed_nuclemd_WMS);
        request.setAttribute("eumed_nuclemd_ETOKENSERVER", eumed_nuclemd_ETOKENSERVER.trim());
        request.setAttribute("eumed_nuclemd_MYPROXYSERVER", eumed_nuclemd_MYPROXYSERVER.trim());
        request.setAttribute("eumed_nuclemd_PORT", eumed_nuclemd_PORT.trim());
        request.setAttribute("eumed_nuclemd_ROBOTID", eumed_nuclemd_ROBOTID.trim());
        request.setAttribute("eumed_nuclemd_WEBDAV", eumed_nuclemd_WEBDAV.trim());
        request.setAttribute("eumed_nuclemd_ROLE", eumed_nuclemd_ROLE.trim());
        request.setAttribute("eumed_nuclemd_RENEWAL", eumed_nuclemd_RENEWAL);
        request.setAttribute("eumed_nuclemd_DISABLEVOMS", eumed_nuclemd_DISABLEVOMS);
        
        request.setAttribute("sagrid_nuclemd_INFRASTRUCTURE", sagrid_nuclemd_INFRASTRUCTURE.trim());
        request.setAttribute("sagrid_nuclemd_VONAME", sagrid_nuclemd_VONAME.trim());
        request.setAttribute("sagrid_nuclemd_TOPBDII", sagrid_nuclemd_TOPBDII.trim());
        request.setAttribute("sagrid_nuclemd_WMS", sagrid_nuclemd_WMS);
        request.setAttribute("sagrid_nuclemd_ETOKENSERVER", sagrid_nuclemd_ETOKENSERVER.trim());
        request.setAttribute("sagrid_nuclemd_MYPROXYSERVER", sagrid_nuclemd_MYPROXYSERVER.trim());
        request.setAttribute("sagrid_nuclemd_PORT", sagrid_nuclemd_PORT.trim());
        request.setAttribute("sagrid_nuclemd_ROBOTID", sagrid_nuclemd_ROBOTID.trim());
        request.setAttribute("sagrid_nuclemd_WEBDAV", sagrid_nuclemd_WEBDAV.trim());
        request.setAttribute("sagrid_nuclemd_ROLE", sagrid_nuclemd_ROLE.trim());
        request.setAttribute("sagrid_nuclemd_RENEWAL", sagrid_nuclemd_RENEWAL);
        request.setAttribute("sagrid_nuclemd_DISABLEVOMS", sagrid_nuclemd_DISABLEVOMS);

        request.setAttribute("see_nuclemd_INFRASTRUCTURE", see_nuclemd_INFRASTRUCTURE.trim());
        request.setAttribute("see_nuclemd_VONAME", see_nuclemd_VONAME.trim());
        request.setAttribute("see_nuclemd_TOPBDII", see_nuclemd_TOPBDII.trim());
        request.setAttribute("see_nuclemd_WMS", see_nuclemd_WMS);
        request.setAttribute("see_nuclemd_ETOKENSERVER", see_nuclemd_ETOKENSERVER.trim());
        request.setAttribute("see_nuclemd_MYPROXYSERVER", see_nuclemd_MYPROXYSERVER.trim());
        request.setAttribute("see_nuclemd_PORT", see_nuclemd_PORT.trim());
        request.setAttribute("see_nuclemd_ROBOTID", see_nuclemd_ROBOTID.trim());
        request.setAttribute("see_nuclemd_WEBDAV", see_nuclemd_WEBDAV.trim());
        request.setAttribute("see_nuclemd_ROLE", see_nuclemd_ROLE.trim());
        request.setAttribute("see_nuclemd_RENEWAL", see_nuclemd_RENEWAL);
        request.setAttribute("see_nuclemd_DISABLEVOMS", see_nuclemd_DISABLEVOMS);
        
        request.setAttribute("gridit_nuclemd_INFRASTRUCTURE", gridit_nuclemd_INFRASTRUCTURE.trim());
        request.setAttribute("gridit_nuclemd_VONAME", gridit_nuclemd_VONAME.trim());
        request.setAttribute("gridit_nuclemd_TOPBDII", gridit_nuclemd_TOPBDII.trim());
        request.setAttribute("gridit_nuclemd_WMS", gridit_nuclemd_WMS);
        request.setAttribute("gridit_nuclemd_ETOKENSERVER", gridit_nuclemd_ETOKENSERVER.trim());
        request.setAttribute("gridit_nuclemd_MYPROXYSERVER", gridit_nuclemd_MYPROXYSERVER.trim());
        request.setAttribute("gridit_nuclemd_PORT", gridit_nuclemd_PORT.trim());
        request.setAttribute("gridit_nuclemd_ROBOTID", gridit_nuclemd_ROBOTID.trim());
        request.setAttribute("gridit_nuclemd_WEBDAV", gridit_nuclemd_WEBDAV.trim());
        request.setAttribute("gridit_nuclemd_ROLE", gridit_nuclemd_ROLE.trim());
        request.setAttribute("gridit_nuclemd_RENEWAL", gridit_nuclemd_RENEWAL);
        request.setAttribute("gridit_nuclemd_DISABLEVOMS", gridit_nuclemd_DISABLEVOMS);

        request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", infras);
        request.setAttribute("nuclemd_APPID", nuclemd_APPID.trim());
        request.setAttribute("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
        request.setAttribute("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
        request.setAttribute("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
        request.setAttribute("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
        request.setAttribute("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
        request.setAttribute("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
        request.setAttribute("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
        request.setAttribute("SMTP_HOST", SMTP_HOST.trim());
        request.setAttribute("SENDER_MAIL", SENDER_MAIL.trim());
        
        if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
        log.info("\nStarting the EDIT mode...with this settings"
        + "\nlato_nuclemd_INFRASTRUCTURE: " + lato_nuclemd_INFRASTRUCTURE
        + "\nlato_nuclemd_LOGIN: " + lato_nuclemd_LOGIN
        + "\nlato_nuclemd_PASSWD: " + lato_nuclemd_PASSWD                    
        + "\nlato_nuclemd_ETOKENSERVER: " + lato_nuclemd_ETOKENSERVER
        + "\nlato_nuclemd_MYPROXYSERVER: " + lato_nuclemd_MYPROXYSERVER
        + "\nlato_nuclemd_PORT: " + lato_nuclemd_PORT
        + "\nlato_nuclemd_ROBOTID: " + lato_nuclemd_ROBOTID
        + "\nlato_nuclemd_WEBDAV: " + lato_nuclemd_WEBDAV
        + "\nlato_nuclemd_ROLE: " + lato_nuclemd_ROLE
        + "\nlato_nuclemd_RENEWAL: " + lato_nuclemd_RENEWAL
        + "\nlato_nuclemd_DISABLEVOMS: " + lato_nuclemd_DISABLEVOMS
                
        + "\ngaruda_nuclemd_INFRASTRUCTURE: " + garuda_nuclemd_INFRASTRUCTURE
        + "\ngaruda_nuclemd_VONAME: " + garuda_nuclemd_VONAME
        + "\ngaruda_nuclemd_TOPBDII: " + garuda_nuclemd_TOPBDII                    
        + "\ngaruda_nuclemd_ETOKENSERVER: " + garuda_nuclemd_ETOKENSERVER
        + "\ngaruda_nuclemd_MYPROXYSERVER: " + garuda_nuclemd_MYPROXYSERVER
        + "\ngaruda_nuclemd_PORT: " + garuda_nuclemd_PORT
        + "\ngaruda_nuclemd_ROBOTID: " + garuda_nuclemd_ROBOTID
        + "\ngaruda_nuclemd_WEBDAV: " + garuda_nuclemd_WEBDAV 
        + "\ngaruda_nuclemd_ROLE: " + garuda_nuclemd_ROLE
        + "\ngaruda_nuclemd_RENEWAL: " + garuda_nuclemd_RENEWAL
        + "\ngaruda_nuclemd_DISABLEVOMS: " + garuda_nuclemd_DISABLEVOMS

        + "\n\neumed_nuclemd_INFRASTRUCTURE: " + eumed_nuclemd_INFRASTRUCTURE
        + "\neumed_nuclemd_VONAME: " + eumed_nuclemd_VONAME
        + "\neumed_nuclemd_TOPBDII: " + eumed_nuclemd_TOPBDII                    
        + "\neumed_nuclemd_ETOKENSERVER: " + eumed_nuclemd_ETOKENSERVER
        + "\neumed_nuclemd_MYPROXYSERVER: " + eumed_nuclemd_MYPROXYSERVER
        + "\neumed_nuclemd_PORT: " + eumed_nuclemd_PORT
        + "\neumed_nuclemd_ROBOTID: " + eumed_nuclemd_ROBOTID
        + "\neumed_nuclemd_WEBDAV: " + eumed_nuclemd_WEBDAV
        + "\neumed_nuclemd_ROLE: " + eumed_nuclemd_ROLE
        + "\neumed_nuclemd_RENEWAL: " + eumed_nuclemd_RENEWAL
        + "\neumed_nuclemd_DISABLEVOMS: " + eumed_nuclemd_DISABLEVOMS
                
        + "\n\nsagrid_nuclemd_INFRASTRUCTURE: " + sagrid_nuclemd_INFRASTRUCTURE
        + "\nsagrid_nuclemd_VONAME: " + sagrid_nuclemd_VONAME
        + "\nsagrid_nuclemd_TOPBDII: " + sagrid_nuclemd_TOPBDII                    
        + "\nsagrid_nuclemd_ETOKENSERVER: " + sagrid_nuclemd_ETOKENSERVER
        + "\nsagrid_nuclemd_MYPROXYSERVER: " + sagrid_nuclemd_MYPROXYSERVER
        + "\nsagrid_nuclemd_PORT: " + sagrid_nuclemd_PORT
        + "\nsagrid_nuclemd_ROBOTID: " + sagrid_nuclemd_ROBOTID
        + "\nsagrid_nuclemd_WEBDAV: " + sagrid_nuclemd_WEBDAV
        + "\nsagrid_nuclemd_ROLE: " + sagrid_nuclemd_ROLE
        + "\nsagrid_nuclemd_RENEWAL: " + sagrid_nuclemd_RENEWAL
        + "\nsagrid_nuclemd_DISABLEVOMS: " + sagrid_nuclemd_DISABLEVOMS

        + "\n\nsee_nuclemd_INFRASTRUCTURE: " + see_nuclemd_INFRASTRUCTURE
        + "\nsee_nuclemd_VONAME: " + see_nuclemd_VONAME
        + "\nsee_nuclemd_TOPBDII: " + see_nuclemd_TOPBDII                   
        + "\nsee_nuclemd_ETOKENSERVER: " + see_nuclemd_ETOKENSERVER
        + "\nsee_nuclemd_MYPROXYSERVER: " + see_nuclemd_MYPROXYSERVER
        + "\nsee_nuclemd_PORT: " + see_nuclemd_PORT
        + "\nsee_nuclemd_ROBOTID: " + see_nuclemd_ROBOTID
        + "\nsee_nuclemd_WEBDAV: " + see_nuclemd_WEBDAV 
        + "\nsee_nuclemd_ROLE: " + see_nuclemd_ROLE
        + "\nsee_nuclemd_RENEWAL: " + see_nuclemd_RENEWAL
        + "\nsee_nuclemd_DISABLEVOMS: " + see_nuclemd_DISABLEVOMS
                
        + "\n\ngridit_nuclemd_INFRASTRUCTURE: " + gridit_nuclemd_INFRASTRUCTURE
        + "\ngridit_nuclemd_VONAME: " + gridit_nuclemd_VONAME
        + "\ngridit_nuclemd_TOPBDII: " + gridit_nuclemd_TOPBDII                   
        + "\ngridit_nuclemd_ETOKENSERVER: " + gridit_nuclemd_ETOKENSERVER
        + "\ngridit_nuclemd_MYPROXYSERVER: " + gridit_nuclemd_MYPROXYSERVER
        + "\ngridit_nuclemd_PORT: " + gridit_nuclemd_PORT
        + "\ngridit_nuclemd_ROBOTID: " + gridit_nuclemd_ROBOTID
        + "\ngridit_nuclemd_WEBDAV: " + gridit_nuclemd_WEBDAV 
        + "\ngridit_nuclemd_ROLE: " + gridit_nuclemd_ROLE
        + "\ngridit_nuclemd_RENEWAL: " + gridit_nuclemd_RENEWAL
        + "\ngridit_nuclemd_DISABLEVOMS: " + gridit_nuclemd_DISABLEVOMS                
        
        + "\nnuclemd_APPID: " + nuclemd_APPID
        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
        + "\nnuclemd_SOFTWARE: " +nuclemd_SOFTWARE
        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
        + "\nSMTP Server: " + SMTP_HOST
        + "\nSender: " + SENDER_MAIL);
        }

        PortletRequestDispatcher dispatcher =
                getPortletContext().getRequestDispatcher("/edit.jsp");

        dispatcher.include(request, response);
    }

    @Override
    protected void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        //super.doHelp(request, response);

        response.setContentType("text/html");

        log.info("\nStarting the HELP mode...");
        PortletRequestDispatcher dispatcher =
                getPortletContext().getRequestDispatcher("/help.jsp");

        dispatcher.include(request, response);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        PortletPreferences portletPreferences =
                (PortletPreferences) request.getPreferences();

        response.setContentType("text/html");

        //java.util.Enumeration listPreferences = portletPreferences.getNames();
        PortletRequestDispatcher dispatcher = null;
        
        String lato_nuclemd_PASSWD = "";
        String lato_nuclemd_LOGIN = "";
        String garuda_nuclemd_TOPBDII = "";
        String garuda_nuclemd_VONAME = "";
        String eumed_nuclemd_TOPBDII = "";
        String eumed_nuclemd_VONAME = "";
        String sagrid_nuclemd_TOPBDII = "";
        String sagrid_nuclemd_VONAME = "";
        String see_nuclemd_TOPBDII = "";
        String see_nuclemd_VONAME = "";
        String gridit_nuclemd_TOPBDII = "";
        String gridit_nuclemd_VONAME = "";
        
        String lato_nuclemd_ENABLEINFRASTRUCTURE = "";
        String garuda_nuclemd_ENABLEINFRASTRUCTURE = "";
        String eumed_nuclemd_ENABLEINFRASTRUCTURE = "";
        String sagrid_nuclemd_ENABLEINFRASTRUCTURE = "";
        String see_nuclemd_ENABLEINFRASTRUCTURE = "";
        String gridit_nuclemd_ENABLEINFRASTRUCTURE = "";
        String[] infras = new String[6];
        
        String[] lato_nuclemd_WMS = new String [5];
        
        String[] nuclemd_INFRASTRUCTURES = 
                portletPreferences.getValues("nuclemd_ENABLEINFRASTRUCTURE", new String[5]);
        
        for (int i=0; i<nuclemd_INFRASTRUCTURES.length; i++) {            
            if (nuclemd_INFRASTRUCTURES[i]!=null && nuclemd_INFRASTRUCTURES[i].equals("lato")) 
                { lato_nuclemd_ENABLEINFRASTRUCTURE = "checked"; log.info ("\n LATO!"); }
            if (nuclemd_INFRASTRUCTURES[i]!=null && nuclemd_INFRASTRUCTURES[i].equals("garuda")) 
                { garuda_nuclemd_ENABLEINFRASTRUCTURE = "checked"; log.info ("\n GARUDA!"); }
            if (nuclemd_INFRASTRUCTURES[i]!=null && nuclemd_INFRASTRUCTURES[i].equals("eumed")) 
                { eumed_nuclemd_ENABLEINFRASTRUCTURE = "checked"; log.info ("\n EUMED!"); }
            if (nuclemd_INFRASTRUCTURES[i]!=null && nuclemd_INFRASTRUCTURES[i].equals("sagrid")) 
                { sagrid_nuclemd_ENABLEINFRASTRUCTURE = "checked"; log.info ("\n SAGRID!"); }
            if (nuclemd_INFRASTRUCTURES[i]!=null && nuclemd_INFRASTRUCTURES[i].equals("see")) 
                { see_nuclemd_ENABLEINFRASTRUCTURE = "checked"; log.info ("\n SEE!"); }
            if (nuclemd_INFRASTRUCTURES[i]!=null && nuclemd_INFRASTRUCTURES[i].equals("gridit")) 
                { gridit_nuclemd_ENABLEINFRASTRUCTURE = "checked"; log.info ("\n GRIDIT!"); }
        }
        
        // Getting the NUCLEMD ENABLEINFRASTRUCTURE from the portlet preferences
        //nuclemd_ENABLEINFRASTRUCTURE = portletPreferences.getValue("nuclemd_ENABLEINFRASTRUCTURE", "NULL");
        // Getting the NUCLEMD APPID from the portlet preferences
        String nuclemd_APPID = portletPreferences.getValue("nuclemd_APPID", "N/A");
        // Getting the LOGLEVEL from the portlet preferences
        String nuclemd_LOGLEVEL = portletPreferences.getValue("nuclemd_LOGLEVEL", "INFO");
        // Getting the METADATA METADATA_HOST from the portlet preferences
        String nuclemd_METADATA_HOST = portletPreferences.getValue("nuclemd_METADATA_HOST", "N/A");
        // Getting the NUCLEMD OUTPUT_PATH from the portlet preferences
        String nuclemd_OUTPUT_PATH = portletPreferences.getValue("nuclemd_OUTPUT_PATH", "/tmp");
        // Getting the NUCLEMD SOFTWARE from the portlet preferences
        String nuclemd_SOFTWARE = portletPreferences.getValue("nuclemd_SOFTWARE", "N/A");
        // Getting the TRACKING_DB_HOSTNAME from the portlet preferences
        String TRACKING_DB_HOSTNAME = portletPreferences.getValue("TRACKING_DB_HOSTNAME", "N/A");
        // Getting the TRACKING_DB_USERNAME from the portlet preferences
        String TRACKING_DB_USERNAME = portletPreferences.getValue("TRACKING_DB_USERNAME", "N/A");
        // Getting the TRACKING_DB_PASSWORD from the portlet preferences
        String TRACKING_DB_PASSWORD = portletPreferences.getValue("TRACKING_DB_PASSWORD", "N/A");
        // Getting the SMTP_HOST from the portlet preferences
        String SMTP_HOST = portletPreferences.getValue("SMTP_HOST", "N/A");
        // Getting the SENDER_MAIL from the portlet preferences
        String SENDER_MAIL = portletPreferences.getValue("SENDER_MAIL", "N/A");
        
        if (lato_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
        {
            infras[0]="lato";
            // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for LATO
            String lato_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("lato_nuclemd_INFRASTRUCTURE", "N/A");
            // Getting the NUCLEMD VONAME from the portlet preferences for LATO
            lato_nuclemd_LOGIN = portletPreferences.getValue("lato_nuclemd_LOGIN", "N/A");
            // Getting the NUCLEMD TOPPBDII from the portlet preferences for LATO
            lato_nuclemd_PASSWD = portletPreferences.getValue("lato_nuclemd_PASSWD", "N/A");
            // Getting the NUCLEMD WMS from the portlet preferences for LATO
            lato_nuclemd_WMS = portletPreferences.getValues("lato_nuclemd_WMS", new String[5]);
            // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for LATO
            String lato_nuclemd_ETOKENSERVER = portletPreferences.getValue("lato_nuclemd_ETOKENSERVER", "N/A");
            // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for LATO
            String lato_nuclemd_MYPROXYSERVER = portletPreferences.getValue("lato_nuclemd_MYPROXYSERVER", "N/A");
            // Getting the NUCLEMD PORT from the portlet preferences for LATO
            String lato_nuclemd_PORT = portletPreferences.getValue("lato_nuclemd_PORT", "N/A");
            // Getting the NUCLEMD ROBOTID from the portlet preferences for LATO
            String lato_nuclemd_ROBOTID = portletPreferences.getValue("lato_nuclemd_ROBOTID", "N/A");
            // Getting the NUCLEMD WEBDAV from the portlet preferences for LATO
            String lato_nuclemd_WEBDAV = portletPreferences.getValue("lato_nuclemd_WEBDAV", "N/A");
            // Getting the NUCLEMD ROLE from the portlet preferences for LATO
            String lato_nuclemd_ROLE = portletPreferences.getValue("lato_nuclemd_ROLE", "N/A");
            // Getting the NUCLEMD RENEWAL from the portlet preferences for LATO
            String lato_nuclemd_RENEWAL = portletPreferences.getValue("lato_nuclemd_RENEWAL", "checked");
            // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for LATO
            String lato_nuclemd_DISABLEVOMS = portletPreferences.getValue("lato_nuclemd_DISABLEVOMS", "unchecked");
            
            // Fetching all the WMS Endpoints for LATO
            String lato_WMS = "";
            if (lato_nuclemd_ENABLEINFRASTRUCTURE.equals("checked")) {
                if (lato_nuclemd_WMS!=null) {
                    //log.info("length="+lato_nuclemd_WMS.length);
                    for (int i = 0; i < lato_nuclemd_WMS.length; i++)
                        if (!(lato_nuclemd_WMS[i].trim().equals("N/A")) ) 
                            lato_WMS += lato_nuclemd_WMS[i] + " ";                        
                } else { log.info("WMS not set for LATO!"); lato_nuclemd_ENABLEINFRASTRUCTURE="unchecked"; }
            }
            
            // Save the portlet preferences
            request.setAttribute("lato_nuclemd_INFRASTRUCTURE", lato_nuclemd_INFRASTRUCTURE.trim());
            request.setAttribute("lato_nuclemd_LOGIN", lato_nuclemd_LOGIN.trim());
            request.setAttribute("lato_nuclemd_PASSWD", lato_nuclemd_PASSWD.trim());
            request.setAttribute("lato_nuclemd_WMS", lato_WMS);
            request.setAttribute("lato_nuclemd_ETOKENSERVER", lato_nuclemd_ETOKENSERVER.trim());
            request.setAttribute("lato_nuclemd_MYPROXYSERVER", lato_nuclemd_MYPROXYSERVER.trim());
            request.setAttribute("lato_nuclemd_PORT", lato_nuclemd_PORT.trim());
            request.setAttribute("lato_nuclemd_ROBOTID", lato_nuclemd_ROBOTID.trim());
            request.setAttribute("lato_nuclemd_WEBDAV", lato_nuclemd_WEBDAV.trim());
            request.setAttribute("lato_nuclemd_ROLE", lato_nuclemd_ROLE.trim());
            request.setAttribute("lato_nuclemd_RENEWAL", lato_nuclemd_RENEWAL);
            request.setAttribute("lato_nuclemd_DISABLEVOMS", lato_nuclemd_DISABLEVOMS);
            
            //request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", nuclemd_ENABLEINFRASTRUCTURE);
            request.setAttribute("nuclemd_APPID", nuclemd_APPID.trim());
            request.setAttribute("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
            request.setAttribute("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
            request.setAttribute("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
            request.setAttribute("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
            request.setAttribute("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
            request.setAttribute("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
            request.setAttribute("SMTP_HOST", SMTP_HOST.trim());
            request.setAttribute("SENDER_MAIL", SENDER_MAIL.trim());
        }
        
        if (garuda_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
        {
            infras[1]="garuda";
            // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("garuda_nuclemd_INFRASTRUCTURE", "N/A");
            // Getting the NUCLEMD VONAME from the portlet preferences for the GARUDA VO
            garuda_nuclemd_VONAME = portletPreferences.getValue("garuda_nuclemd_VONAME", "N/A");
            // Getting the NUCLEMD TOPPBDII from the portlet preferences for the GARUDA VO
            garuda_nuclemd_TOPBDII = portletPreferences.getValue("garuda_nuclemd_TOPBDII", "N/A");
            // Getting the NUCLEMD WMS from the portlet preferences for the GARUDA VO
            String[] garuda_nuclemd_WMS = portletPreferences.getValues("garuda_nuclemd_WMS", new String[5]);
            // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_ETOKENSERVER = portletPreferences.getValue("garuda_nuclemd_ETOKENSERVER", "N/A");
            // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_MYPROXYSERVER = portletPreferences.getValue("garuda_nuclemd_MYPROXYSERVER", "N/A");
            // Getting the NUCLEMD PORT from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_PORT = portletPreferences.getValue("garuda_nuclemd_PORT", "N/A");
            // Getting the NUCLEMD ROBOTID from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_ROBOTID = portletPreferences.getValue("garuda_nuclemd_ROBOTID", "N/A");
            // Getting the NUCLEMD WEBDAV from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_WEBDAV = portletPreferences.getValue("garuda_nuclemd_WEBDAV", "N/A");
            // Getting the NUCLEMD ROLE from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_ROLE = portletPreferences.getValue("garuda_nuclemd_ROLE", "N/A");
            // Getting the NUCLEMD RENEWAL from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_RENEWAL = portletPreferences.getValue("garuda_nuclemd_RENEWAL", "checked");
            // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GARUDA VO
            String garuda_nuclemd_DISABLEVOMS = portletPreferences.getValue("garuda_nuclemd_DISABLEVOMS", "unchecked");
            
            // Fetching all the WMS Endpoints for the GARUDA VO
            String garuda_WMS = "";
            if (garuda_nuclemd_ENABLEINFRASTRUCTURE.equals("checked")) {            
                if (garuda_nuclemd_WMS!=null) {
                    //log.info("length="+garuda_nuclemd_WMS.length);
                    for (int i = 0; i < garuda_nuclemd_WMS.length; i++)
                        if (!(garuda_nuclemd_WMS[i].trim().equals("N/A")) ) 
                            garuda_WMS += garuda_nuclemd_WMS[i] + " ";                        
                } else { log.info("WMS not set for GARUDA!"); garuda_nuclemd_ENABLEINFRASTRUCTURE="unchecked"; }
            }
            
            // Save the portlet preferences
            request.setAttribute("garuda_nuclemd_INFRASTRUCTURE", garuda_nuclemd_INFRASTRUCTURE.trim());
            request.setAttribute("garuda_nuclemd_VONAME", garuda_nuclemd_VONAME.trim());
            request.setAttribute("garuda_nuclemd_TOPBDII", garuda_nuclemd_TOPBDII.trim());
            request.setAttribute("garuda_nuclemd_WMS", garuda_WMS);
            request.setAttribute("garuda_nuclemd_ETOKENSERVER", garuda_nuclemd_ETOKENSERVER.trim());
            request.setAttribute("garuda_nuclemd_MYPROXYSERVER", garuda_nuclemd_MYPROXYSERVER.trim());
            request.setAttribute("garuda_nuclemd_PORT", garuda_nuclemd_PORT.trim());
            request.setAttribute("garuda_nuclemd_ROBOTID", garuda_nuclemd_ROBOTID.trim());
            request.setAttribute("garuda_nuclemd_WEBDAV", garuda_nuclemd_WEBDAV.trim());
            request.setAttribute("garuda_nuclemd_ROLE", garuda_nuclemd_ROLE.trim());
            request.setAttribute("garuda_nuclemd_RENEWAL", garuda_nuclemd_RENEWAL);
            request.setAttribute("garuda_nuclemd_DISABLEVOMS", garuda_nuclemd_DISABLEVOMS);
            
            //request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", nuclemd_ENABLEINFRASTRUCTURE);
            request.setAttribute("nuclemd_APPID", nuclemd_APPID.trim());
            request.setAttribute("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
            request.setAttribute("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
            request.setAttribute("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
            request.setAttribute("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
            request.setAttribute("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
            request.setAttribute("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
            request.setAttribute("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
            request.setAttribute("SMTP_HOST", SMTP_HOST.trim());
            request.setAttribute("SENDER_MAIL", SENDER_MAIL.trim());
        }
        
        if (eumed_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
        {
            infras[2]="eumed";
            // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the EUMED VO
            String eumed_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("eumed_nuclemd_INFRASTRUCTURE", "N/A");
            // Getting the NUCLEMD VONAME from the portlet preferences for the EUMED VO
            eumed_nuclemd_VONAME = portletPreferences.getValue("eumed_nuclemd_VONAME", "N/A");
            // Getting the NUCLEMD TOPPBDII from the portlet preferences for the EUMED VO
            eumed_nuclemd_TOPBDII = portletPreferences.getValue("eumed_nuclemd_TOPBDII", "N/A");
            // Getting the NUCLEMD WMS from the portlet preferences for the EUMED VO
            String[] eumed_nuclemd_WMS = portletPreferences.getValues("eumed_nuclemd_WMS", new String[5]);
            // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the EUMED VO
            String eumed_nuclemd_ETOKENSERVER = portletPreferences.getValue("eumed_nuclemd_ETOKENSERVER", "N/A");
            // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the EUMED VO
            String eumed_nuclemd_MYPROXYSERVER = portletPreferences.getValue("eumed_nuclemd_MYPROXYSERVER", "N/A");
            // Getting the NUCLEMD PORT from the portlet preferences for the EUMED VO
            String eumed_nuclemd_PORT = portletPreferences.getValue("eumed_nuclemd_PORT", "N/A");
            // Getting the NUCLEMD ROBOTID from the portlet preferences for the EUMED VO
            String eumed_nuclemd_ROBOTID = portletPreferences.getValue("eumed_nuclemd_ROBOTID", "N/A");
            // Getting the NUCLEMD WEBDAV from the portlet preferences for the EUMED VO
            String eumed_nuclemd_WEBDAV = portletPreferences.getValue("eumed_nuclemd_WEBDAV", "N/A");
            // Getting the NUCLEMD ROLE from the portlet preferences for the EUMED VO
            String eumed_nuclemd_ROLE = portletPreferences.getValue("eumed_nuclemd_ROLE", "N/A");
            // Getting the NUCLEMD RENEWAL from the portlet preferences for the EUMED VO
            String eumed_nuclemd_RENEWAL = portletPreferences.getValue("eumed_nuclemd_RENEWAL", "checked");
            // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the EUMED VO
            String eumed_nuclemd_DISABLEVOMS = portletPreferences.getValue("eumed_nuclemd_DISABLEVOMS", "unchecked");
                                    
            // Fetching all the WMS Endpoints for the EUMED VO
            String eumed_WMS = "";
            if (eumed_nuclemd_ENABLEINFRASTRUCTURE.equals("checked")) {            
                if (eumed_nuclemd_WMS!=null) {
                    //log.info("length="+eumed_nuclemd_WMS.length);
                    for (int i = 0; i < eumed_nuclemd_WMS.length; i++)
                        if (!(eumed_nuclemd_WMS[i].trim().equals("N/A")) ) 
                            eumed_WMS += eumed_nuclemd_WMS[i] + " ";                        
                } else { log.info("WMS not set for EUMED!"); eumed_nuclemd_ENABLEINFRASTRUCTURE="unchecked"; }
            }
            
            // Save the portlet preferences
            request.setAttribute("eumed_nuclemd_INFRASTRUCTURE", eumed_nuclemd_INFRASTRUCTURE.trim());
            request.setAttribute("eumed_nuclemd_VONAME", eumed_nuclemd_VONAME.trim());
            request.setAttribute("eumed_nuclemd_TOPBDII", eumed_nuclemd_TOPBDII.trim());
            request.setAttribute("eumed_nuclemd_WMS", eumed_WMS);
            request.setAttribute("eumed_nuclemd_ETOKENSERVER", eumed_nuclemd_ETOKENSERVER.trim());
            request.setAttribute("eumed_nuclemd_MYPROXYSERVER", eumed_nuclemd_MYPROXYSERVER.trim());
            request.setAttribute("eumed_nuclemd_PORT", eumed_nuclemd_PORT.trim());
            request.setAttribute("eumed_nuclemd_ROBOTID", eumed_nuclemd_ROBOTID.trim());
            request.setAttribute("eumed_nuclemd_WEBDAV", eumed_nuclemd_WEBDAV.trim());
            request.setAttribute("eumed_nuclemd_ROLE", eumed_nuclemd_ROLE.trim());
            request.setAttribute("eumed_nuclemd_RENEWAL", eumed_nuclemd_RENEWAL);
            request.setAttribute("eumed_nuclemd_DISABLEVOMS", eumed_nuclemd_DISABLEVOMS);

            //request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", nuclemd_ENABLEINFRASTRUCTURE);
            request.setAttribute("nuclemd_APPID", nuclemd_APPID.trim());
            request.setAttribute("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
            request.setAttribute("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
            request.setAttribute("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
            request.setAttribute("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
            request.setAttribute("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
            request.setAttribute("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
            request.setAttribute("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
            request.setAttribute("SMTP_HOST", SMTP_HOST.trim());
            request.setAttribute("SENDER_MAIL", SENDER_MAIL.trim());
        }
        
        if (sagrid_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
        {
            infras[3]="sagrid";
            // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("sagrid_nuclemd_INFRASTRUCTURE", "N/A");
            // Getting the NUCLEMD VONAME from the portlet preferences for the SAGRID VO
            sagrid_nuclemd_VONAME = portletPreferences.getValue("sagrid_nuclemd_VONAME", "N/A");
            // Getting the NUCLEMD TOPPBDII from the portlet preferences for the SAGRID VO
            sagrid_nuclemd_TOPBDII = portletPreferences.getValue("sagrid_nuclemd_TOPBDII", "N/A");
            // Getting the NUCLEMD WMS from the portlet preferences for the SAGRID VO
            String[] sagrid_nuclemd_WMS = portletPreferences.getValues("sagrid_nuclemd_WMS", new String[5]);
            // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_ETOKENSERVER = portletPreferences.getValue("sagrid_nuclemd_ETOKENSERVER", "N/A");
            // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_MYPROXYSERVER = portletPreferences.getValue("sagrid_nuclemd_MYPROXYSERVER", "N/A");
            // Getting the NUCLEMD PORT from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_PORT = portletPreferences.getValue("sagrid_nuclemd_PORT", "N/A");
            // Getting the NUCLEMD ROBOTID from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_ROBOTID = portletPreferences.getValue("sagrid_nuclemd_ROBOTID", "N/A");
            // Getting the NUCLEMD WEBDAV from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_WEBDAV = portletPreferences.getValue("sagrid_nuclemd_WEBDAV", "N/A");
            // Getting the NUCLEMD ROLE from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_ROLE = portletPreferences.getValue("sagrid_nuclemd_ROLE", "N/A");
            // Getting the NUCLEMD RENEWAL from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_RENEWAL = portletPreferences.getValue("sagrid_nuclemd_RENEWAL", "checked");
            // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the SAGRID VO
            String sagrid_nuclemd_DISABLEVOMS = portletPreferences.getValue("sagrid_nuclemd_DISABLEVOMS", "unchecked");
                                    
            // Fetching all the WMS Endpoints for the EUMED VO
            String sagrid_WMS = "";
            if (sagrid_nuclemd_ENABLEINFRASTRUCTURE.equals("checked")) {            
                if (sagrid_nuclemd_WMS!=null) {
                    //log.info("length="+sagrid_nuclemd_WMS.length);
                    for (int i = 0; i < sagrid_nuclemd_WMS.length; i++)
                        if (!(sagrid_nuclemd_WMS[i].trim().equals("N/A")) ) 
                            sagrid_WMS += sagrid_nuclemd_WMS[i] + " ";                        
                } else { log.info("WMS not set for SAGRID!"); sagrid_nuclemd_ENABLEINFRASTRUCTURE="unchecked"; }
            }
            
            // Save the portlet preferences
            request.setAttribute("sagrid_nuclemd_INFRASTRUCTURE", sagrid_nuclemd_INFRASTRUCTURE.trim());
            request.setAttribute("sagrid_nuclemd_VONAME", sagrid_nuclemd_VONAME.trim());
            request.setAttribute("sagrid_nuclemd_TOPBDII", sagrid_nuclemd_TOPBDII.trim());
            request.setAttribute("sagrid_nuclemd_WMS", sagrid_WMS);
            request.setAttribute("sagrid_nuclemd_ETOKENSERVER", sagrid_nuclemd_ETOKENSERVER.trim());
            request.setAttribute("sagrid_nuclemd_MYPROXYSERVER", sagrid_nuclemd_MYPROXYSERVER.trim());
            request.setAttribute("sagrid_nuclemd_PORT", sagrid_nuclemd_PORT.trim());
            request.setAttribute("sagrid_nuclemd_ROBOTID", sagrid_nuclemd_ROBOTID.trim());
            request.setAttribute("sagrid_nuclemd_WEBDAV", sagrid_nuclemd_WEBDAV.trim());
            request.setAttribute("sagrid_nuclemd_ROLE", sagrid_nuclemd_ROLE.trim());
            request.setAttribute("sagrid_nuclemd_RENEWAL", sagrid_nuclemd_RENEWAL);
            request.setAttribute("sagrid_nuclemd_DISABLEVOMS", sagrid_nuclemd_DISABLEVOMS);

            //request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", nuclemd_ENABLEINFRASTRUCTURE);
            request.setAttribute("nuclemd_APPID", nuclemd_APPID.trim());
            request.setAttribute("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
            request.setAttribute("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
            request.setAttribute("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
            request.setAttribute("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
            request.setAttribute("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
            request.setAttribute("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
            request.setAttribute("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
            request.setAttribute("SMTP_HOST", SMTP_HOST.trim());
            request.setAttribute("SENDER_MAIL", SENDER_MAIL.trim());
        }        

        if (see_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
        {
            infras[4]="see";
            // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the SEE VO
            String see_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("see_nuclemd_INFRASTRUCTURE", "N/A");
            // Getting the NUCLEMD VONAME from the portlet preferences for the see VO
            see_nuclemd_VONAME = portletPreferences.getValue("see_nuclemd_VONAME", "N/A");
            // Getting the NUCLEMD TOPPBDII from the portlet preferences for the SEE VO
            see_nuclemd_TOPBDII = portletPreferences.getValue("see_nuclemd_TOPBDII", "N/A");
            // Getting the NUCLEMD WMS from the portlet preferences for the SEE VO
            String[] see_nuclemd_WMS = portletPreferences.getValues("see_nuclemd_WMS", new String[5]);
            // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the SEE VO
            String see_nuclemd_ETOKENSERVER = portletPreferences.getValue("see_nuclemd_ETOKENSERVER", "N/A");
            // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the SEE VO
            String see_nuclemd_MYPROXYSERVER = portletPreferences.getValue("see_nuclemd_MYPROXYSERVER", "N/A");
            // Getting the NUCLEMD PORT from the portlet preferences for the SEE VO
            String see_nuclemd_PORT = portletPreferences.getValue("see_nuclemd_PORT", "N/A");
            // Getting the NUCLEMD ROBOTID from the portlet preferences for the SEE VO
            String see_nuclemd_ROBOTID = portletPreferences.getValue("see_nuclemd_ROBOTID", "N/A");
            // Getting the NUCLEMD WEBDAV from the portlet preferences for the SEE VO
            String see_nuclemd_WEBDAV = portletPreferences.getValue("see_nuclemd_WEBDAV", "N/A");
            // Getting the NUCLEMD ROLE from the portlet preferences for the SEE VO
            String see_nuclemd_ROLE = portletPreferences.getValue("see_nuclemd_ROLE", "N/A");
            // Getting the NUCLEMD RENEWAL from the portlet preferences for the SEE VO
            String see_nuclemd_RENEWAL = portletPreferences.getValue("see_nuclemd_RENEWAL", "checked");
            // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the SEE VO
            String see_nuclemd_DISABLEVOMS = portletPreferences.getValue("see_nuclemd_DISABLEVOMS", "unchecked");              
            
            // Fetching all the WMS Endpoints for the SEE VO
            String see_WMS = "";
            if (see_nuclemd_ENABLEINFRASTRUCTURE.equals("checked")) {            
                if (see_nuclemd_WMS!=null) {
                    //log.info("length="+see_nuclemd_WMS.length);
                    for (int i = 0; i < see_nuclemd_WMS.length; i++)
                        if (!(see_nuclemd_WMS[i].trim().equals("N/A")) ) 
                            see_WMS += see_nuclemd_WMS[i] + " ";                        
                } else { log.info("WMS not set for SEE!"); see_nuclemd_ENABLEINFRASTRUCTURE="unchecked"; }
            }
            
            // Save the portlet preferences
            request.setAttribute("see_nuclemd_INFRASTRUCTURE", see_nuclemd_INFRASTRUCTURE.trim());
            request.setAttribute("see_nuclemd_VONAME", see_nuclemd_VONAME.trim());
            request.setAttribute("see_nuclemd_TOPBDII", see_nuclemd_TOPBDII.trim());
            request.setAttribute("see_nuclemd_WMS", see_WMS);
            request.setAttribute("see_nuclemd_ETOKENSERVER", see_nuclemd_ETOKENSERVER.trim());
            request.setAttribute("see_nuclemd_MYPROXYSERVER", see_nuclemd_MYPROXYSERVER.trim());
            request.setAttribute("see_nuclemd_PORT", see_nuclemd_PORT.trim());
            request.setAttribute("see_nuclemd_ROBOTID", see_nuclemd_ROBOTID.trim());
            request.setAttribute("see_nuclemd_WEBDAV", see_nuclemd_WEBDAV.trim());
            request.setAttribute("see_nuclemd_ROLE", see_nuclemd_ROLE.trim());
            request.setAttribute("see_nuclemd_RENEWAL", see_nuclemd_RENEWAL);
            request.setAttribute("see_nuclemd_DISABLEVOMS", see_nuclemd_DISABLEVOMS);

            //request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", nuclemd_ENABLEINFRASTRUCTURE);
            request.setAttribute("nuclemd_APPID", nuclemd_APPID.trim());
            request.setAttribute("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
            request.setAttribute("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
            request.setAttribute("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
            request.setAttribute("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
            request.setAttribute("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
            request.setAttribute("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
            request.setAttribute("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
            request.setAttribute("SMTP_HOST", SMTP_HOST.trim());
            request.setAttribute("SENDER_MAIL", SENDER_MAIL.trim());
        }
        
        if (gridit_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
        {
            infras[5]="gridit";
            // Getting the NUCLEMD INFRASTRUCTURE from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("gridit_nuclemd_INFRASTRUCTURE", "N/A");
            // Getting the NUCLEMD VONAME from the portlet preferences for the GRIDIT VO
            gridit_nuclemd_VONAME = portletPreferences.getValue("gridit_nuclemd_VONAME", "N/A");
            // Getting the NUCLEMD TOPPBDII from the portlet preferences for the GRIDIT VO
            gridit_nuclemd_TOPBDII = portletPreferences.getValue("gridit_nuclemd_TOPBDII", "N/A");
            // Getting the NUCLEMD WMS from the portlet preferences for the GRIDIT VO
            String[] gridit_nuclemd_WMS = portletPreferences.getValues("gridit_nuclemd_WMS", new String[5]);
            // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_ETOKENSERVER = portletPreferences.getValue("gridit_nuclemd_ETOKENSERVER", "N/A");
            // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_MYPROXYSERVER = portletPreferences.getValue("gridit_nuclemd_MYPROXYSERVER", "N/A");
            // Getting the NUCLEMD PORT from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_PORT = portletPreferences.getValue("gridit_nuclemd_PORT", "N/A");
            // Getting the NUCLEMD ROBOTID from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_ROBOTID = portletPreferences.getValue("gridit_nuclemd_ROBOTID", "N/A");
            // Getting the NUCLEMD WEBDAV from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_WEBDAV = portletPreferences.getValue("gridit_nuclemd_WEBDAV", "N/A");
            // Getting the NUCLEMD ROLE from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_ROLE = portletPreferences.getValue("gridit_nuclemd_ROLE", "N/A");
            // Getting the NUCLEMD RENEWAL from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_RENEWAL = portletPreferences.getValue("gridit_nuclemd_RENEWAL", "checked");
            // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GRIDIT VO
            String gridit_nuclemd_DISABLEVOMS = portletPreferences.getValue("gridit_nuclemd_DISABLEVOMS", "unchecked");
            
            // Fetching all the WMS Endpoints for the GRIDIT VO
            String gridit_WMS = "";
            if (gridit_nuclemd_ENABLEINFRASTRUCTURE.equals("checked")) {            
                if (gridit_nuclemd_WMS!=null) {
                    //log.info("length="+gridit_nuclemd_WMS.length);
                    for (int i = 0; i < gridit_nuclemd_WMS.length; i++)
                        if (!(gridit_nuclemd_WMS[i].trim().equals("N/A")) ) 
                            gridit_WMS += gridit_nuclemd_WMS[i] + " ";                        
                } else { log.info("WMS not set for GRIDIT!"); gridit_nuclemd_ENABLEINFRASTRUCTURE="unchecked"; }
            }
            
            // Save the portlet preferences
            request.setAttribute("gridit_nuclemd_INFRASTRUCTURE", gridit_nuclemd_INFRASTRUCTURE.trim());
            request.setAttribute("gridit_nuclemd_VONAME", gridit_nuclemd_VONAME.trim());
            request.setAttribute("gridit_nuclemd_TOPBDII", gridit_nuclemd_TOPBDII.trim());
            request.setAttribute("gridit_nuclemd_WMS", gridit_WMS);
            request.setAttribute("gridit_nuclemd_ETOKENSERVER", gridit_nuclemd_ETOKENSERVER.trim());
            request.setAttribute("gridit_nuclemd_MYPROXYSERVER", gridit_nuclemd_MYPROXYSERVER.trim());
            request.setAttribute("gridit_nuclemd_PORT", gridit_nuclemd_PORT.trim());
            request.setAttribute("gridit_nuclemd_ROBOTID", gridit_nuclemd_ROBOTID.trim());
            request.setAttribute("gridit_nuclemd_WEBDAV", gridit_nuclemd_WEBDAV.trim());
            request.setAttribute("gridit_nuclemd_ROLE", gridit_nuclemd_ROLE.trim());
            request.setAttribute("gridit_nuclemd_RENEWAL", gridit_nuclemd_RENEWAL);
            request.setAttribute("gridit_nuclemd_DISABLEVOMS", gridit_nuclemd_DISABLEVOMS);

            //request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", nuclemd_ENABLEINFRASTRUCTURE);
            request.setAttribute("nuclemd_APPID", nuclemd_APPID.trim());
            request.setAttribute("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
            request.setAttribute("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
            request.setAttribute("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
            request.setAttribute("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
            request.setAttribute("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
            request.setAttribute("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
            request.setAttribute("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
            request.setAttribute("SMTP_HOST", SMTP_HOST.trim());
            request.setAttribute("SENDER_MAIL", SENDER_MAIL.trim());
        }                
        
        // Save in the preferences the list of supported infrastructures 
        request.setAttribute("nuclemd_ENABLEINFRASTRUCTURE", infras);

        HashMap<String,Properties> GPS_table = new HashMap<String, Properties>();
        HashMap<String,Properties> GPS_queue = new HashMap<String, Properties>();

        // ********************************************************
        List<String> CEqueues_lato = null;        
        List<String> CEqueues_garuda = null;
        List<String> CEqueues_eumed = null;
        List<String> CEqueues_sagrid = null;
        List<String> CEqueues_see = null;
        List<String> CEqueues_gridit = null;
        
        List<String> CEs_list_lato = null;        
        List<String> CEs_list_garuda = null;        
        List<String> CEs_list_eumed = null;
        List<String> CEs_list_sagrid = null;
        List<String> CEs_list_see = null;
        List<String> CEs_list_gridit = null;
        
        List<String> CEs_list_TOT = new ArrayList<String>();
        List<String> CEs_queue_TOT = new ArrayList<String>();
        
        BDII bdii = null;
        
        String[] SOFTWARE_LIST = nuclemd_SOFTWARE.split(",");
        
        // Scanning the list of resources publishing the SW TAG(s)
        for(String SOFTWARE: SOFTWARE_LIST)
        {

            try {
                if (lato_nuclemd_ENABLEINFRASTRUCTURE.equals("checked") && 
                   (!lato_nuclemd_PASSWD.equals("N/A")) ) 
                {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                            log.info("-----*FETCHING*THE*<LATO>*RESOURCES*-----");
                    
                    CEs_list_lato = new ArrayList();                    
                    CEqueues_lato = new ArrayList();
                    
                    // Fetching all the WMS Endpoints for LATO                    
                    if (lato_nuclemd_WMS!=null) {
                        for (int i = 0; i < lato_nuclemd_WMS.length; i++)
                            if (!(lato_nuclemd_WMS[i].trim().equals("N/A")) ) {                                    
                                CEqueues_lato.add(lato_nuclemd_WMS[i].trim());
                                CEs_list_lato.add(lato_nuclemd_WMS[i].trim().replace("ssh://", ""));                                    
                            }
                    } 
                }
             
                //=========================================================
                // IMPORTANT: THIS FIX IS ONLY TO SHOW GARUDA SITES 
                //            IN THE GOOGLE MAP                
                //=========================================================
                if (garuda_nuclemd_ENABLEINFRASTRUCTURE.equals("checked") && 
                   (!garuda_nuclemd_TOPBDII.equals("N/A")) ) 
                {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                            log.info("-----*FETCHING*THE*<GARUDA>*RESOURCES*-----");
                    
                    CEs_list_garuda = new ArrayList();
                    CEs_list_garuda.add("xn03.ctsf.cdacb.in");
                    
                    CEqueues_garuda = new ArrayList();
                    CEqueues_garuda.add("gatekeeper://xn03.ctsf.cdacb.in:8443/GW");
                }
                
                if (eumed_nuclemd_ENABLEINFRASTRUCTURE.equals("checked") && 
                   (!eumed_nuclemd_TOPBDII.equals("N/A")) ) 
                {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("-----*FETCHING*THE*<EUMED>*RESOURCES*-----");
                    
                    bdii = new BDII(new URI(eumed_nuclemd_TOPBDII));
                    CEs_list_eumed = 
                            getListofCEForSoftwareTag(eumed_nuclemd_VONAME,
                                                      eumed_nuclemd_TOPBDII,
                                                      SOFTWARE);
                    
                    CEqueues_eumed = 
                            bdii.queryCEQueues(eumed_nuclemd_VONAME);
                }
                
                if (sagrid_nuclemd_ENABLEINFRASTRUCTURE.equals("checked") && 
                   (!sagrid_nuclemd_TOPBDII.equals("N/A")) ) 
                {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("-----*FETCHING*THE*<SAGRID>*RESOURCES*-----");
                    
                    bdii = new BDII(new URI(sagrid_nuclemd_TOPBDII));
                    CEs_list_sagrid = 
                            getListofCEForSoftwareTag(sagrid_nuclemd_VONAME,
                                                      sagrid_nuclemd_TOPBDII,
                                                      SOFTWARE);
                    
                    CEqueues_sagrid = 
                            bdii.queryCEQueues(sagrid_nuclemd_VONAME);
                }
                
                if (see_nuclemd_ENABLEINFRASTRUCTURE.equals("checked") &&
                   (!see_nuclemd_TOPBDII.equals("N/A")) ) 
                {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("-----*FETCHING*THE*<SEE>*RESOURCES*-----");
                    
                    bdii = new BDII(new URI(see_nuclemd_TOPBDII));
                    CEs_list_see = 
                            getListofCEForSoftwareTag(see_nuclemd_VONAME,
                                                      see_nuclemd_TOPBDII,
                                                      SOFTWARE);
                    
                    CEqueues_see = 
                            bdii.queryCEQueues(see_nuclemd_VONAME);
                }
                
                if (gridit_nuclemd_ENABLEINFRASTRUCTURE.equals("checked") &&
                   (!gridit_nuclemd_TOPBDII.equals("N/A")) ) 
                {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("-----*FETCHING*THE*<GRIDIT>*RESOURCES*-----");
                    
                    bdii = new BDII(new URI(gridit_nuclemd_TOPBDII));
                    CEs_list_gridit = 
                            getListofCEForSoftwareTag(gridit_nuclemd_VONAME,
                                                      gridit_nuclemd_TOPBDII,
                                                      SOFTWARE);
                    
                    CEqueues_gridit = 
                            bdii.queryCEQueues(gridit_nuclemd_VONAME);
                }
                
                // Merging the list of CEs and queues                
                if (lato_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                        CEs_list_TOT.addAll(CEs_list_lato);
                if (garuda_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))                        
                        CEs_list_TOT.addAll(CEs_list_garuda);
                if (eumed_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                        CEs_list_TOT.addAll(CEs_list_eumed);
                if (sagrid_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                        CEs_list_TOT.addAll(CEs_list_sagrid);
                if (see_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                        CEs_list_TOT.addAll(CEs_list_see);
                if (gridit_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                        CEs_list_TOT.addAll(CEs_list_gridit);
                                
                if (lato_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                    CEs_queue_TOT.addAll(CEqueues_lato);
                if (garuda_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                    CEs_queue_TOT.addAll(CEqueues_garuda);
                if (eumed_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                    CEs_queue_TOT.addAll(CEqueues_eumed);
                if (sagrid_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                    CEs_queue_TOT.addAll(CEqueues_sagrid);
                if (see_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                    CEs_queue_TOT.addAll(CEqueues_see);
                if (gridit_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                    CEs_queue_TOT.addAll(CEqueues_gridit);
                
            } catch (URISyntaxException ex) {
               Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NamingException e) {}
        } // fine list SW tag
                                
        //=========================================================
        // IMPORTANT: INSTANCIATE THE UsersTrackingDBInterface
        //            CLASS USING THE EMPTY CONSTRUCTOR WHEN
        //            WHEN THE PORTLET IS DEPLOYED IN PRODUCTION!!!
        //=========================================================
        /*UsersTrackingDBInterface DBInterface =
            new UsersTrackingDBInterface(
                TRACKING_DB_HOSTNAME.trim(),
                TRACKING_DB_USERNAME.trim(),
                TRACKING_DB_PASSWORD.trim());*/
                
         UsersTrackingDBInterface DBInterface =
            new UsersTrackingDBInterface();
                    
         if ( (CEs_list_TOT != null) && (!CEs_list_TOT.isEmpty()) )
         {
            log.info("NOT EMPTY LIST!");
            // Fetching the list of CEs publushing the SW
            for (String CE:CEs_list_TOT) 
            {
                log.info("Fetching the CE="+CE);
                Properties coordinates = new Properties();
                Properties queue = new Properties();

                float coords[] = DBInterface.getCECoordinate(CE);                        

                String GPS_LAT = Float.toString(coords[0]);
                String GPS_LNG = Float.toString(coords[1]);

                coordinates.setProperty("LAT", GPS_LAT);
                coordinates.setProperty("LNG", GPS_LNG);

                // Fetching the Queues
                for (String CEqueue:CEs_queue_TOT)
                    if (CEqueue.contains(CE))
                        queue.setProperty("QUEUE", CEqueue);
                        
                // Saving the GPS location in a Java HashMap
                GPS_table.put(CE, coordinates);

                // Saving the queue in a Java HashMap
                GPS_queue.put(CE, queue);
            }
         } else log.info ("EMPTY LIST!");

         // Checking the HashMap
         Set set = GPS_table.entrySet();
         Iterator iter = set.iterator();
         
         while ( iter.hasNext() )
         {
            Map.Entry entry = (Map.Entry)iter.next();
            log.info(" - GPS location of the CE " 
                     + entry.getKey() 
                     + " => " 
                     + entry.getValue());
         }

         // Checking the HashMap
         set = GPS_queue.entrySet();
         iter = set.iterator();
         while ( iter.hasNext() )
         {
            Map.Entry entry = (Map.Entry)iter.next();
            log.info(" - Queue " 
                     + entry.getKey() 
                     + " => " 
                     + entry.getValue());
         }

         Gson gson = new GsonBuilder().create();
         request.setAttribute ("GPS_table", gson.toJson(GPS_table));
         request.setAttribute ("GPS_queue", gson.toJson(GPS_queue));

         // ********************************************************
         dispatcher = getPortletContext().getRequestDispatcher("/view.jsp");       
         dispatcher.include(request, response);
    }

    // The init method will be called when installing for the first time the portlet
    // This is the right time to setup the default values into the preferences
    @Override
    public void init() throws PortletException {
        super.init();
    }

    @Override
    public void processAction(ActionRequest request,
                              ActionResponse response)
                throws PortletException, IOException 
    {
        try {
            String action = "";

            // Getting the action to be processed from the request
            action = request.getParameter("ActionEvent");

            // Determine the username and the email address
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);        
            User user = themeDisplay.getUser();
            
            String username = user.getScreenName();
            String emailAddress = user.getDisplayEmailAddress();
            
            Company company = PortalUtil.getCompany(request);
                String portal = company.getName();

            PortletPreferences portletPreferences =
                    (PortletPreferences) request.getPreferences();

            if (action.equals("CONFIG_NUCLEMD_PORTLET")) {
                log.info("\nPROCESS ACTION => " + action);
                
                // Getting the NUCLEMD APPID from the portlet request
                String nuclemd_APPID = request.getParameter("nuclemd_APPID");
                // Getting the LOGLEVEL from the portlet request
                String nuclemd_LOGLEVEL = request.getParameter("nuclemd_LOGLEVEL");
                // Getting the NUCLEMD_METADATA_HOST from the portlet request
                String nuclemd_METADATA_HOST = request.getParameter("nuclemd_METADATA_HOST");
                // Getting the NUCLEMD OUTPUT_PATH from the portlet request
                String nuclemd_OUTPUT_PATH = request.getParameter("nuclemd_OUTPUT_PATH");
                // Getting the NUCLEMD SOFTWARE from the portlet request
                String nuclemd_SOFTWARE = request.getParameter("nuclemd_SOFTWARE");
                // Getting the TRACKING_DB_HOSTNAME from the portlet request
                String TRACKING_DB_HOSTNAME = request.getParameter("TRACKING_DB_HOSTNAME");
                // Getting the TRACKING_DB_USERNAME from the portlet request
                String TRACKING_DB_USERNAME = request.getParameter("TRACKING_DB_USERNAME");
                // Getting the TRACKING_DB_PASSWORD from the portlet request
                String TRACKING_DB_PASSWORD = request.getParameter("TRACKING_DB_PASSWORD");
                // Getting the SMTP_HOST from the portlet request
                String SMTP_HOST = request.getParameter("SMTP_HOST");
                // Getting the SENDER_MAIL from the portlet request
                String SENDER_MAIL = request.getParameter("SENDER_MAIL");
                String[] infras = new String[6];
                
                String lato_nuclemd_ENABLEINFRASTRUCTURE = "unchecked";
                String garuda_nuclemd_ENABLEINFRASTRUCTURE = "unchecked";
                String eumed_nuclemd_ENABLEINFRASTRUCTURE = "unchecked";
                String sagrid_nuclemd_ENABLEINFRASTRUCTURE = "unchecked";
                String see_nuclemd_ENABLEINFRASTRUCTURE = "unchecked";
                String gridit_nuclemd_ENABLEINFRASTRUCTURE = "unchecked";
                
                String[] nuclemd_INFRASTRUCTURES = request.getParameterValues("nuclemd_ENABLEINFRASTRUCTURES");         

                if (nuclemd_INFRASTRUCTURES != null) {
                    Arrays.sort(nuclemd_INFRASTRUCTURES);
                    lato_nuclemd_ENABLEINFRASTRUCTURE =
                        Arrays.binarySearch(nuclemd_INFRASTRUCTURES, "lato") >= 0 ? "checked" : "unchecked";
                    garuda_nuclemd_ENABLEINFRASTRUCTURE =
                        Arrays.binarySearch(nuclemd_INFRASTRUCTURES, "garuda") >= 0 ? "checked" : "unchecked";
                    eumed_nuclemd_ENABLEINFRASTRUCTURE =
                        Arrays.binarySearch(nuclemd_INFRASTRUCTURES, "eumed") >= 0 ? "checked" : "unchecked";
                    sagrid_nuclemd_ENABLEINFRASTRUCTURE =
                        Arrays.binarySearch(nuclemd_INFRASTRUCTURES, "sagrid") >= 0 ? "checked" : "unchecked";
                    see_nuclemd_ENABLEINFRASTRUCTURE =
                        Arrays.binarySearch(nuclemd_INFRASTRUCTURES, "see") >= 0 ? "checked" : "unchecked";
                    gridit_nuclemd_ENABLEINFRASTRUCTURE =
                        Arrays.binarySearch(nuclemd_INFRASTRUCTURES, "gridit") >= 0 ? "checked" : "unchecked";
                }           
                
                if (lato_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                {
                    infras[0]="lato";
                     // Getting the NUCLEMD INFRASTRUCTURE from the portlet request for LATO
                    String lato_nuclemd_INFRASTRUCTURE = request.getParameter("lato_nuclemd_INFRASTRUCTURE");
                    // Getting the NUCLEMD VONAME from the portlet request for LATO
                    String lato_nuclemd_LOGIN = request.getParameter("lato_nuclemd_LOGIN");
                    // Getting the NUCLEMD TOPBDII from the portlet request for LATO
                    String lato_nuclemd_PASSWD = request.getParameter("lato_nuclemd_PASSWD");
                    // Getting the NUCLEMD WMS from the portlet request for LATO
                    String[] lato_nuclemd_WMS = request.getParameterValues("lato_nuclemd_WMS");
                    // Getting the NUCLEMD ETOKENSERVER from the portlet request for LATO
                    String lato_nuclemd_ETOKENSERVER = request.getParameter("lato_nuclemd_ETOKENSERVER");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet request for LATO
                    String lato_nuclemd_MYPROXYSERVER = request.getParameter("lato_nuclemd_MYPROXYSERVER");
                    // Getting the NUCLEMD PORT from the portlet request for LATO
                    String lato_nuclemd_PORT = request.getParameter("lato_nuclemd_PORT");
                    // Getting the NUCLEMD ROBOTID from the portlet request for LATO
                    String lato_nuclemd_ROBOTID = request.getParameter("lato_nuclemd_ROBOTID");
                    // Getting the NUCLEMD WEBDAV from the portlet request for LATO
                    String lato_nuclemd_WEBDAV = request.getParameter("lato_nuclemd_WEBDAV");
                    // Getting the NUCLEMD ROLE from the portlet request for LATO
                    String lato_nuclemd_ROLE = request.getParameter("lato_nuclemd_ROLE");
                    // Getting the NUCLEMD OPTIONS from the portlet request for LATO
                    String[] lato_nuclemd_OPTIONS = request.getParameterValues("lato_nuclemd_OPTIONS");

                    String lato_nuclemd_RENEWAL = "";
                    String lato_nuclemd_DISABLEVOMS = "";

                    if (lato_nuclemd_OPTIONS == null){
                        lato_nuclemd_RENEWAL = "checked";
                        lato_nuclemd_DISABLEVOMS = "unchecked";
                    } else {
                        Arrays.sort(lato_nuclemd_OPTIONS);
                        // Getting the NUCLEMD RENEWAL from the portlet preferences for LATO
                        lato_nuclemd_RENEWAL = Arrays.binarySearch(lato_nuclemd_OPTIONS, "enableRENEWAL") >= 0 ? "checked" : "unchecked";
                        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for LATO
                        lato_nuclemd_DISABLEVOMS = Arrays.binarySearch(lato_nuclemd_OPTIONS, "disableVOMS") >= 0 ? "checked" : "unchecked";
                    }
                    
                    int nmax=0;                
                    for (int i = 0; i < lato_nuclemd_WMS.length; i++)
                        if ( lato_nuclemd_WMS[i]!=null && (!lato_nuclemd_WMS[i].trim().equals("N/A")) )                        
                            nmax++;
                    
                    log.info("\n\nLength="+nmax);
                    String[] lato_nuclemd_WMS_trimmed = new String[nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        lato_nuclemd_WMS_trimmed[i]=lato_nuclemd_WMS[i].trim();
                        log.info ("\n\nLATO [" + i + "] WMS=[" + lato_nuclemd_WMS_trimmed[i] + "]");
                    }
                    
                    // Set the portlet preferences
                    portletPreferences.setValue("lato_nuclemd_INFRASTRUCTURE", lato_nuclemd_INFRASTRUCTURE.trim());
                    portletPreferences.setValue("lato_nuclemd_LOGIN", lato_nuclemd_LOGIN.trim());
                    portletPreferences.setValue("lato_nuclemd_PASSWD", lato_nuclemd_PASSWD.trim());
                    portletPreferences.setValues("lato_nuclemd_WMS", lato_nuclemd_WMS_trimmed);
                    portletPreferences.setValue("lato_nuclemd_ETOKENSERVER", lato_nuclemd_ETOKENSERVER.trim());
                    portletPreferences.setValue("lato_nuclemd_MYPROXYSERVER", lato_nuclemd_MYPROXYSERVER.trim());
                    portletPreferences.setValue("lato_nuclemd_PORT", lato_nuclemd_PORT.trim());
                    portletPreferences.setValue("lato_nuclemd_ROBOTID", lato_nuclemd_ROBOTID.trim());
                    portletPreferences.setValue("lato_nuclemd_WEBDAV", lato_nuclemd_WEBDAV.trim());
                    portletPreferences.setValue("lato_nuclemd_ROLE", lato_nuclemd_ROLE.trim());
                    portletPreferences.setValue("lato_nuclemd_RENEWAL", lato_nuclemd_RENEWAL);
                    portletPreferences.setValue("lato_nuclemd_DISABLEVOMS", lato_nuclemd_DISABLEVOMS);                
                    
                    portletPreferences.setValue("nuclemd_APPID", nuclemd_APPID.trim());
                    portletPreferences.setValue("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
                    portletPreferences.setValue("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
                    portletPreferences.setValue("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
                    portletPreferences.setValue("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
                    portletPreferences.setValue("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
                    portletPreferences.setValue("SMTP_HOST", SMTP_HOST.trim());
                    portletPreferences.setValue("SENDER_MAIL", SENDER_MAIL.trim());
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n\nPROCESS ACTION => " + action
                        + "\n- Storing the NUCLEMD portlet preferences ..."
                        + "\nlato_nuclemd_INFRASTRUCTURE: " + lato_nuclemd_INFRASTRUCTURE
                        + "\nlato_nuclemd_LOGIN: " + lato_nuclemd_LOGIN
                        + "\nlato_nuclemd_PASSWD: " + lato_nuclemd_PASSWD                    
                        + "\nlato_nuclemd_ETOKENSERVER: " + lato_nuclemd_ETOKENSERVER
                        + "\nlato_nuclemd_MYPROXYSERVER: " + lato_nuclemd_MYPROXYSERVER
                        + "\nlato_nuclemd_PORT: " + lato_nuclemd_PORT
                        + "\nlato_nuclemd_ROBOTID: " + lato_nuclemd_ROBOTID
                        + "\nlato_nuclemd_WEBDAV: " + lato_nuclemd_WEBDAV
                        + "\nlato_nuclemd_ROLE: " + lato_nuclemd_ROLE
                        + "\nlato_nuclemd_RENEWAL: " + lato_nuclemd_RENEWAL
                        + "\nlato_nuclemd_DISABLEVOMS: " + lato_nuclemd_DISABLEVOMS
                            
                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + "lato"
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOSTL: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);
                    }
                }

                if (garuda_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                {
                    infras[1]="garuda";
                     // Getting the NUCLEMD INFRASTRUCTURE from the portlet request for the GARUDA VO
                    String garuda_nuclemd_INFRASTRUCTURE = request.getParameter("garuda_nuclemd_INFRASTRUCTURE");
                    // Getting the NUCLEMD VONAME from the portlet request for the GARUDA VO
                    String garuda_nuclemd_VONAME = request.getParameter("garuda_nuclemd_VONAME");
                    // Getting the NUCLEMD TOPBDII from the portlet request for the GARUDA VO
                    String garuda_nuclemd_TOPBDII = request.getParameter("garuda_nuclemd_TOPBDII");
                    // Getting the NUCLEMD WMS from the portlet request for the GARUDA VO
                    String[] garuda_nuclemd_WMS = request.getParameterValues("garuda_nuclemd_WMS");
                    // Getting the NUCLEMD ETOKENSERVER from the portlet request for the GARUDA VO
                    String garuda_nuclemd_ETOKENSERVER = request.getParameter("garuda_nuclemd_ETOKENSERVER");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet request for the GARUDA VO
                    String garuda_nuclemd_MYPROXYSERVER = request.getParameter("garuda_nuclemd_MYPROXYSERVER");
                    // Getting the NUCLEMD PORT from the portlet request for the GARUDA VO
                    String garuda_nuclemd_PORT = request.getParameter("garuda_nuclemd_PORT");
                    // Getting the NUCLEMD ROBOTID from the portlet request for the GARUDA VO
                    String garuda_nuclemd_ROBOTID = request.getParameter("garuda_nuclemd_ROBOTID");
                    // Getting the NUCLEMD WEBDAV from the portlet request for the GARUDA VO
                    String garuda_nuclemd_WEBDAV = request.getParameter("garuda_nuclemd_WEBDAV");
                    // Getting the NUCLEMD ROLE from the portlet request for the GARUDA VO
                    String garuda_nuclemd_ROLE = request.getParameter("garuda_nuclemd_ROLE");
                    // Getting the NUCLEMD OPTIONS from the portlet request for the GARUDA VO
                    String[] garuda_nuclemd_OPTIONS = request.getParameterValues("garuda_nuclemd_OPTIONS");

                    String garuda_nuclemd_RENEWAL = "";
                    String garuda_nuclemd_DISABLEVOMS = "";

                    if (garuda_nuclemd_OPTIONS == null){
                        garuda_nuclemd_RENEWAL = "checked";
                        garuda_nuclemd_DISABLEVOMS = "unchecked";
                    } else {
                        Arrays.sort(garuda_nuclemd_OPTIONS);
                        // Getting the NUCLEMD RENEWAL from the portlet preferences for the GARUDA VO
                        garuda_nuclemd_RENEWAL = Arrays.binarySearch(garuda_nuclemd_OPTIONS, "enableRENEWAL") >= 0 ? "checked" : "unchecked";
                        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GARUDA VO
                        garuda_nuclemd_DISABLEVOMS = Arrays.binarySearch(garuda_nuclemd_OPTIONS, "disableVOMS") >= 0 ? "checked" : "unchecked";
                    }
                    
                    int nmax=0;                
                    for (int i = 0; i < garuda_nuclemd_WMS.length; i++)
                        if ( garuda_nuclemd_WMS[i]!=null && (!garuda_nuclemd_WMS[i].trim().equals("N/A")) )                        
                            nmax++;
                    
                    log.info("\n\nLength="+nmax);
                    String[] garuda_nuclemd_WMS_trimmed = new String[nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        garuda_nuclemd_WMS_trimmed[i]=garuda_nuclemd_WMS[i].trim();
                        log.info ("\n\nLATO [" + i + "] WMS=[" + garuda_nuclemd_WMS_trimmed[i] + "]");
                    }
                    
                    // Set the portlet preferences
                    portletPreferences.setValue("garuda_nuclemd_INFRASTRUCTURE", garuda_nuclemd_INFRASTRUCTURE.trim());
                    portletPreferences.setValue("garuda_nuclemd_VONAME", garuda_nuclemd_VONAME.trim());
                    portletPreferences.setValue("garuda_nuclemd_TOPBDII", garuda_nuclemd_TOPBDII.trim());
                    portletPreferences.setValues("garuda_nuclemd_WMS", garuda_nuclemd_WMS_trimmed);
                    portletPreferences.setValue("garuda_nuclemd_ETOKENSERVER", garuda_nuclemd_ETOKENSERVER.trim());
                    portletPreferences.setValue("garuda_nuclemd_MYPROXYSERVER", garuda_nuclemd_MYPROXYSERVER.trim());
                    portletPreferences.setValue("garuda_nuclemd_PORT", garuda_nuclemd_PORT.trim());
                    portletPreferences.setValue("garuda_nuclemd_ROBOTID", garuda_nuclemd_ROBOTID.trim());
                    portletPreferences.setValue("garuda_nuclemd_WEBDAV", garuda_nuclemd_WEBDAV.trim());
                    portletPreferences.setValue("garuda_nuclemd_ROLE", garuda_nuclemd_ROLE.trim());
                    portletPreferences.setValue("garuda_nuclemd_RENEWAL", garuda_nuclemd_RENEWAL);
                    portletPreferences.setValue("garuda_nuclemd_DISABLEVOMS", garuda_nuclemd_DISABLEVOMS);                
                    
                    portletPreferences.setValue("nuclemd_APPID", nuclemd_APPID.trim());
                    portletPreferences.setValue("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
                    portletPreferences.setValue("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
                    portletPreferences.setValue("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
                    portletPreferences.setValue("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
                    portletPreferences.setValue("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
                    portletPreferences.setValue("SMTP_HOST", SMTP_HOST.trim());
                    portletPreferences.setValue("SENDER_MAIL", SENDER_MAIL.trim());
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n\nPROCESS ACTION => " + action
                        + "\n- Storing the NUCLEMD portlet preferences ..."
                        + "\ngaruda_nuclemd_INFRASTRUCTURE: " + garuda_nuclemd_INFRASTRUCTURE
                        + "\ngaruda_nuclemd_VONAME: " + garuda_nuclemd_VONAME
                        + "\ngaruda_nuclemd_TOPBDII: " + garuda_nuclemd_TOPBDII                    
                        + "\ngaruda_nuclemd_ETOKENSERVER: " + garuda_nuclemd_ETOKENSERVER
                        + "\ngaruda_nuclemd_MYPROXYSERVER: " + garuda_nuclemd_MYPROXYSERVER
                        + "\ngaruda_nuclemd_PORT: " + garuda_nuclemd_PORT
                        + "\ngaruda_nuclemd_ROBOTID: " + garuda_nuclemd_ROBOTID
                        + "\ngaruda_nuclemd_WEBDAV: " + garuda_nuclemd_WEBDAV
                        + "\ngaruda_nuclemd_ROLE: " + garuda_nuclemd_ROLE
                        + "\ngaruda_nuclemd_RENEWAL: " + garuda_nuclemd_RENEWAL
                        + "\ngaruda_nuclemd_DISABLEVOMS: " + garuda_nuclemd_DISABLEVOMS
                            
                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + "garuda"
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);
                    }
                }

                if (eumed_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                {
                    infras[2]="eumed";
                    // Getting the NUCLEMD INFRASTRUCTURE from the portlet request for the EUMED VO
                    String eumed_nuclemd_INFRASTRUCTURE = request.getParameter("eumed_nuclemd_INFRASTRUCTURE");
                    // Getting the NUCLEMD VONAME from the portlet request for the EUMED VO
                    String eumed_nuclemd_VONAME = request.getParameter("eumed_nuclemd_VONAME");
                    // Getting the NUCLEMD TOPBDII from the portlet request for the EUMED VO
                    String eumed_nuclemd_TOPBDII = request.getParameter("eumed_nuclemd_TOPBDII");
                    // Getting the NUCLEMD WMS from the portlet request for the EUMED VO
                    String[] eumed_nuclemd_WMS = request.getParameterValues("eumed_nuclemd_WMS");
                    // Getting the NUCLEMD ETOKENSERVER from the portlet request for the EUMED VO
                    String eumed_nuclemd_ETOKENSERVER = request.getParameter("eumed_nuclemd_ETOKENSERVER");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet request for the EUMED VO
                    String eumed_nuclemd_MYPROXYSERVER = request.getParameter("eumed_nuclemd_MYPROXYSERVER");
                    // Getting the NUCLEMD PORT from the portlet request for the EUMED VO
                    String eumed_nuclemd_PORT = request.getParameter("eumed_nuclemd_PORT");
                    // Getting the NUCLEMD ROBOTID from the portlet request for the EUMED VO
                    String eumed_nuclemd_ROBOTID = request.getParameter("eumed_nuclemd_ROBOTID");
                    // Getting the NUCLEMD WEBDAV from the portlet request for the EUMED VO
                    String eumed_nuclemd_WEBDAV = request.getParameter("eumed_nuclemd_WEBDAV");
                    // Getting the NUCLEMD ROLE from the portlet request for the EUMED VO
                    String eumed_nuclemd_ROLE = request.getParameter("eumed_nuclemd_ROLE");
                    // Getting the NUCLEMD OPTIONS from the portlet request for the EUMED VO
                    String[] eumed_nuclemd_OPTIONS = request.getParameterValues("eumed_nuclemd_OPTIONS");

                    String eumed_nuclemd_RENEWAL = "";
                    String eumed_nuclemd_DISABLEVOMS = "";

                    if (eumed_nuclemd_OPTIONS == null){
                        eumed_nuclemd_RENEWAL = "checked";
                        eumed_nuclemd_DISABLEVOMS = "unchecked";
                    } else {
                        Arrays.sort(eumed_nuclemd_OPTIONS);
                        // Getting the NUCLEMD RENEWAL from the portlet preferences for the EUMED VO
                        eumed_nuclemd_RENEWAL = Arrays.binarySearch(eumed_nuclemd_OPTIONS, "enableRENEWAL") >= 0 ? "checked" : "unchecked";
                        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GARUDA VO
                        eumed_nuclemd_DISABLEVOMS = Arrays.binarySearch(eumed_nuclemd_OPTIONS, "disableVOMS") >= 0 ? "checked" : "unchecked";
                    }
                    
                    int nmax=0;                
                    for (int i = 0; i < eumed_nuclemd_WMS.length; i++)
                        if ( eumed_nuclemd_WMS[i]!=null && (!eumed_nuclemd_WMS[i].trim().equals("N/A")) )                        
                            nmax++;
                    
                    log.info("\n\nLength="+nmax);
                    String[] eumed_nuclemd_WMS_trimmed = new String[nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        eumed_nuclemd_WMS_trimmed[i]=eumed_nuclemd_WMS[i].trim();
                        log.info ("\n\nEUMED [" + i + "] WMS=[" + eumed_nuclemd_WMS_trimmed[i] + "]");
                    }
                    
                    // Set the portlet preferences
                    portletPreferences.setValue("eumed_nuclemd_INFRASTRUCTURE", eumed_nuclemd_INFRASTRUCTURE.trim());
                    portletPreferences.setValue("eumed_nuclemd_VONAME", eumed_nuclemd_VONAME.trim());
                    portletPreferences.setValue("eumed_nuclemd_TOPBDII", eumed_nuclemd_TOPBDII.trim());
                    portletPreferences.setValues("eumed_nuclemd_WMS", eumed_nuclemd_WMS_trimmed);
                    portletPreferences.setValue("eumed_nuclemd_ETOKENSERVER", eumed_nuclemd_ETOKENSERVER.trim());
                    portletPreferences.setValue("eumed_nuclemd_MYPROXYSERVER", eumed_nuclemd_MYPROXYSERVER.trim());
                    portletPreferences.setValue("eumed_nuclemd_PORT", eumed_nuclemd_PORT.trim());
                    portletPreferences.setValue("eumed_nuclemd_ROBOTID", eumed_nuclemd_ROBOTID.trim());
                    portletPreferences.setValue("eumed_nuclemd_WEBDAV", eumed_nuclemd_WEBDAV.trim());
                    portletPreferences.setValue("eumed_nuclemd_ROLE", eumed_nuclemd_ROLE.trim());
                    portletPreferences.setValue("eumed_nuclemd_RENEWAL", eumed_nuclemd_RENEWAL);
                    portletPreferences.setValue("eumed_nuclemd_DISABLEVOMS", eumed_nuclemd_DISABLEVOMS); 
                    
                    portletPreferences.setValue("nuclemd_APPID", nuclemd_APPID.trim());
                    portletPreferences.setValue("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
                    portletPreferences.setValue("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
                    portletPreferences.setValue("nuclemd_OUTPATH_PATH", nuclemd_OUTPUT_PATH.trim());
                    portletPreferences.setValue("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
                    portletPreferences.setValue("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
                    portletPreferences.setValue("SMTP_HOST", SMTP_HOST.trim());
                    portletPreferences.setValue("SENDER_MAIL", SENDER_MAIL.trim());
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n\nPROCESS ACTION => " + action
                        + "\n- Storing the NUCLEMD portlet preferences ..."                    
                        + "\n\neumed_nuclemd_INFRASTRUCTURE: " + eumed_nuclemd_INFRASTRUCTURE
                        + "\neumed_nuclemd_VONAME: " + eumed_nuclemd_VONAME
                        + "\neumed_nuclemd_TOPBDII: " + eumed_nuclemd_TOPBDII                    
                        + "\neumed_nuclemd_ETOKENSERVER: " + eumed_nuclemd_ETOKENSERVER
                        + "\neumed_nuclemd_MYPROXYSERVER: " + eumed_nuclemd_MYPROXYSERVER
                        + "\neumed_nuclemd_PORT: " + eumed_nuclemd_PORT
                        + "\neumed_nuclemd_ROBOTID: " + eumed_nuclemd_ROBOTID
                        + "\neumed_nuclemd_WEBDAV: " + eumed_nuclemd_WEBDAV
                        + "\neumed_nuclemd_ROLE: " + eumed_nuclemd_ROLE
                        + "\neumed_nuclemd_RENEWAL: " + eumed_nuclemd_RENEWAL
                        + "\neumed_nuclemd_DISABLEVOMS: " + eumed_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + "eumed"
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);
                    }
                }                                
                
                if (sagrid_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                {
                    infras[3]="sagrid";
                    // Getting the NUCLEMD INFRASTRUCTURE from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_INFRASTRUCTURE = request.getParameter("sagrid_nuclemd_INFRASTRUCTURE");
                    // Getting the NUCLEMD VONAME from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_VONAME = request.getParameter("sagrid_nuclemd_VONAME");
                    // Getting the NUCLEMD TOPBDII from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_TOPBDII = request.getParameter("sagrid_nuclemd_TOPBDII");
                    // Getting the NUCLEMD WMS from the portlet request for the SAGRID VO
                    String[] sagrid_nuclemd_WMS = request.getParameterValues("sagrid_nuclemd_WMS");
                    // Getting the NUCLEMD ETOKENSERVER from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_ETOKENSERVER = request.getParameter("sagrid_nuclemd_ETOKENSERVER");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_MYPROXYSERVER = request.getParameter("sagrid_nuclemd_MYPROXYSERVER");
                    // Getting the NUCLEMD PORT from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_PORT = request.getParameter("sagrid_nuclemd_PORT");
                    // Getting the NUCLEMD ROBOTID from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_ROBOTID = request.getParameter("sagrid_nuclemd_ROBOTID");
                    // Getting the NUCLEMD WEBDAV from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_WEBDAV = request.getParameter("sagrid_nuclemd_WEBDAV");
                    // Getting the NUCLEMD ROLE from the portlet request for the SAGRID VO
                    String sagrid_nuclemd_ROLE = request.getParameter("sagrid_nuclemd_ROLE");
                    // Getting the NUCLEMD OPTIONS from the portlet request for the SAGRID VO
                    String[] sagrid_nuclemd_OPTIONS = request.getParameterValues("sagrid_nuclemd_OPTIONS");

                    String sagrid_nuclemd_RENEWAL = "";
                    String sagrid_nuclemd_DISABLEVOMS = "";

                    if (sagrid_nuclemd_OPTIONS == null){
                        sagrid_nuclemd_RENEWAL = "checked";
                        sagrid_nuclemd_DISABLEVOMS = "unchecked";
                    } else {
                        Arrays.sort(sagrid_nuclemd_OPTIONS);
                        // Getting the NUCLEMD RENEWAL from the portlet preferences for the SAGRID VO
                        sagrid_nuclemd_RENEWAL = Arrays.binarySearch(sagrid_nuclemd_OPTIONS, "enableRENEWAL") >= 0 ? "checked" : "unchecked";
                        // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the SAGRID VO
                        sagrid_nuclemd_DISABLEVOMS = Arrays.binarySearch(sagrid_nuclemd_OPTIONS, "disableVOMS") >= 0 ? "checked" : "unchecked";
                    }
                    
                    int nmax=0;                
                    for (int i = 0; i < sagrid_nuclemd_WMS.length; i++)
                        if ( sagrid_nuclemd_WMS[i]!=null && (!sagrid_nuclemd_WMS[i].trim().equals("N/A")) )                        
                            nmax++;
                    
                    log.info("\n\nLength="+nmax);
                    String[] sagrid_nuclemd_WMS_trimmed = new String[nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        sagrid_nuclemd_WMS_trimmed[i]=sagrid_nuclemd_WMS[i].trim();
                        log.info ("\n\nSAGRID [" + i + "] WMS=[" + sagrid_nuclemd_WMS_trimmed[i] + "]");
                    }
                    
                    // Set the portlet preferences
                    portletPreferences.setValue("sagrid_nuclemd_INFRASTRUCTURE", sagrid_nuclemd_INFRASTRUCTURE.trim());
                    portletPreferences.setValue("sagrid_nuclemd_VONAME", sagrid_nuclemd_VONAME.trim());
                    portletPreferences.setValue("sagrid_nuclemd_TOPBDII", sagrid_nuclemd_TOPBDII.trim());
                    portletPreferences.setValues("sagrid_nuclemd_WMS", sagrid_nuclemd_WMS_trimmed);
                    portletPreferences.setValue("sagrid_nuclemd_ETOKENSERVER", sagrid_nuclemd_ETOKENSERVER.trim());
                    portletPreferences.setValue("sagrid_nuclemd_MYPROXYSERVER", sagrid_nuclemd_MYPROXYSERVER.trim());
                    portletPreferences.setValue("sagrid_nuclemd_PORT", sagrid_nuclemd_PORT.trim());
                    portletPreferences.setValue("sagrid_nuclemd_ROBOTID", sagrid_nuclemd_ROBOTID.trim());
                    portletPreferences.setValue("sagrid_nuclemd_WEBDAV", sagrid_nuclemd_WEBDAV.trim());
                    portletPreferences.setValue("sagrid_nuclemd_ROLE", sagrid_nuclemd_ROLE.trim());
                    portletPreferences.setValue("sagrid_nuclemd_RENEWAL", sagrid_nuclemd_RENEWAL);
                    portletPreferences.setValue("sagrid_nuclemd_DISABLEVOMS", sagrid_nuclemd_DISABLEVOMS); 
                    
                    portletPreferences.setValue("nuclemd_APPID", nuclemd_APPID.trim());
                    portletPreferences.setValue("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
                    portletPreferences.setValue("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
                    portletPreferences.setValue("nuclemd_OUTPATH_PATH", nuclemd_OUTPUT_PATH.trim());
                    portletPreferences.setValue("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
                    portletPreferences.setValue("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
                    portletPreferences.setValue("SMTP_HOST", SMTP_HOST.trim());
                    portletPreferences.setValue("SENDER_MAIL", SENDER_MAIL.trim());
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n\nPROCESS ACTION => " + action
                        + "\n- Storing the NUCLEMD portlet preferences ..."                    
                        + "\n\nsagrid_nuclemd_INFRASTRUCTURE: " + sagrid_nuclemd_INFRASTRUCTURE
                        + "\nsagrid_nuclemd_VONAME: " + sagrid_nuclemd_VONAME
                        + "\nsagrid_nuclemd_TOPBDII: " + sagrid_nuclemd_TOPBDII                    
                        + "\nsagrid_nuclemd_ETOKENSERVER: " + sagrid_nuclemd_ETOKENSERVER
                        + "\nsagrid_nuclemd_MYPROXYSERVER: " + sagrid_nuclemd_MYPROXYSERVER
                        + "\nsagrid_nuclemd_PORT: " + sagrid_nuclemd_PORT
                        + "\nsagrid_nuclemd_ROBOTID: " + sagrid_nuclemd_ROBOTID                            
                        + "\nsagrid_nuclemd_WEBDAV: " + sagrid_nuclemd_WEBDAV
                        + "\neumed_nuclemd_ROLE: " + sagrid_nuclemd_ROLE
                        + "\nsagrid_nuclemd_RENEWAL: " + sagrid_nuclemd_RENEWAL
                        + "\nsagrid_nuclemd_DISABLEVOMS: " + sagrid_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + "sagrid"
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);
                    }
                }
                
                if (see_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                {
                    infras[4]="see";
                    // Getting the NUCLEMD INFRASTRUCTURE from the portlet request for the SEE VO
                    String see_nuclemd_INFRASTRUCTURE = request.getParameter("see_nuclemd_INFRASTRUCTURE");
                    // Getting the NUCLEMD VONAME from the portlet request for the SEE VO
                    String see_nuclemd_VONAME = request.getParameter("see_nuclemd_VONAME");
                    // Getting the NUCLEMD TOPBDII from the portlet request for the SEE VO
                    String see_nuclemd_TOPBDII = request.getParameter("see_nuclemd_TOPBDII");
                    // Getting the NUCLEMD WMS from the portlet request for the SEE VO
                    String[] see_nuclemd_WMS = request.getParameterValues("see_nuclemd_WMS");
                    // Getting the NUCLEMD ETOKENSERVER from the portlet request for the SEE VO
                    String see_nuclemd_ETOKENSERVER = request.getParameter("see_nuclemd_ETOKENSERVER");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet request for the SEE VO
                    String see_nuclemd_MYPROXYSERVER = request.getParameter("see_nuclemd_MYPROXYSERVER");
                    // Getting the NUCLEMD PORT from the portlet request for the SEE VO
                    String see_nuclemd_PORT = request.getParameter("see_nuclemd_PORT");
                    // Getting the NUCLEMD ROBOTID from the portlet request for the SEE VO
                    String see_nuclemd_ROBOTID = request.getParameter("see_nuclemd_ROBOTID");
                    // Getting the NUCLEMD WEBDAV from the portlet request for the SEE VO
                    String see_nuclemd_WEBDAV = request.getParameter("see_nuclemd_WEBDAV");
                    // Getting the NUCLEMD ROLE from the portlet request for the SEE VO
                    String see_nuclemd_ROLE = request.getParameter("see_nuclemd_ROLE");
                    // Getting the NUCLEMD OPTIONS from the portlet request for the SEE VO
                    String[] see_nuclemd_OPTIONS = request.getParameterValues("see_nuclemd_OPTIONS");

                    String see_nuclemd_RENEWAL = "";
                    String see_nuclemd_DISABLEVOMS = "";

                    if (see_nuclemd_OPTIONS == null){
                        see_nuclemd_RENEWAL = "checked";
                        see_nuclemd_DISABLEVOMS = "unchecked";
                    } else {
                        Arrays.sort(see_nuclemd_OPTIONS);
                        // Get the NUCLEMD RENEWAL from the portlet preferences for the SEE VO
                        see_nuclemd_RENEWAL = Arrays.binarySearch(see_nuclemd_OPTIONS, "enableRENEWAL") >= 0 ? "checked" : "unchecked";
                        // Get the NUCLEMD DISABLEVOMS from the portlet preferences for the SEE VO
                        see_nuclemd_DISABLEVOMS = Arrays.binarySearch(see_nuclemd_OPTIONS, "disableVOMS") >= 0 ? "checked" : "unchecked";
                    }
                    
                    int nmax=0;                
                    for (int i = 0; i < see_nuclemd_WMS.length; i++)
                        if ( see_nuclemd_WMS[i]!=null && (!see_nuclemd_WMS[i].trim().equals("N/A")) )                        
                            nmax++;
                    
                    log.info("\n\nLength="+nmax);
                    String[] see_nuclemd_WMS_trimmed = new String[nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        see_nuclemd_WMS_trimmed[i]=see_nuclemd_WMS[i].trim();
                        log.info ("\n\nSEE [" + i + "] WMS=[" + see_nuclemd_WMS_trimmed[i] + "]");
                    }

                    // Set the portlet preferences
                    portletPreferences.setValue("see_nuclemd_INFRASTRUCTURE", see_nuclemd_INFRASTRUCTURE.trim());
                    portletPreferences.setValue("see_nuclemd_VONAME", see_nuclemd_VONAME.trim());
                    portletPreferences.setValue("see_nuclemd_TOPBDII", see_nuclemd_TOPBDII.trim());
                    portletPreferences.setValues("see_nuclemd_WMS", see_nuclemd_WMS_trimmed);
                    portletPreferences.setValue("see_nuclemd_ETOKENSERVER", see_nuclemd_ETOKENSERVER.trim());
                    portletPreferences.setValue("see_nuclemd_MYPROXYSERVER", see_nuclemd_MYPROXYSERVER.trim());
                    portletPreferences.setValue("see_nuclemd_PORT", see_nuclemd_PORT.trim());
                    portletPreferences.setValue("see_nuclemd_ROBOTID", see_nuclemd_ROBOTID.trim());
                    portletPreferences.setValue("see_nuclemd_WEBDAV", see_nuclemd_WEBDAV.trim());
                    portletPreferences.setValue("see_nuclemd_ROLE", see_nuclemd_ROLE.trim());
                    portletPreferences.setValue("see_nuclemd_RENEWAL", see_nuclemd_RENEWAL);
                    portletPreferences.setValue("see_nuclemd_DISABLEVOMS", see_nuclemd_DISABLEVOMS);
                    
                    portletPreferences.setValue("nuclemd_APPID", nuclemd_APPID.trim());
                    portletPreferences.setValue("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
                    portletPreferences.setValue("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
                    portletPreferences.setValue("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
                    portletPreferences.setValue("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
                    portletPreferences.setValue("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
                    portletPreferences.setValue("SMTP_HOST", SMTP_HOST.trim());
                    portletPreferences.setValue("SENDER_MAIL", SENDER_MAIL.trim());
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n\nPROCESS ACTION => " + action
                        + "\n- Storing the NUCLEMD portlet preferences ..."                    
                        + "\n\nsee_nuclemd_INFRASTRUCTURE: " + see_nuclemd_INFRASTRUCTURE
                        + "\nsee_nuclemd_VONAME: " + see_nuclemd_VONAME
                        + "\nsee_nuclemd_TOPBDII: " + see_nuclemd_TOPBDII                    
                        + "\nsee_nuclemd_ETOKENSERVER: " + see_nuclemd_ETOKENSERVER
                        + "\nsee_nuclemd_MYPROXYSERVER: " + see_nuclemd_MYPROXYSERVER
                        + "\nsee_nuclemd_PORT: " + see_nuclemd_PORT
                        + "\nsee_nuclemd_ROBOTID: " + see_nuclemd_ROBOTID
                        + "\nsee_nuclemd_WEBDAV: " + see_nuclemd_WEBDAV
                        + "\nsee_nuclemd_ROLE: " + see_nuclemd_ROLE
                        + "\nsee_nuclemd_RENEWAL: " + see_nuclemd_RENEWAL
                        + "\nsee_nuclemd_DISABLEVOMS: " + see_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + "see"
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);
                    }
                }
                
                if (gridit_nuclemd_ENABLEINFRASTRUCTURE.equals("checked"))
                {
                    infras[5]="gridit";
                    // Getting the NUCLEMD INFRASTRUCTURE from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_INFRASTRUCTURE = request.getParameter("gridit_nuclemd_INFRASTRUCTURE");
                    // Getting the NUCLEMD VONAME from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_VONAME = request.getParameter("gridit_nuclemd_VONAME");
                    // Getting the NUCLEMD TOPBDII from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_TOPBDII = request.getParameter("gridit_nuclemd_TOPBDII");
                    // Getting the NUCLEMD WMS from the portlet request for the GRIDIT VO
                    String[] gridit_nuclemd_WMS = request.getParameterValues("gridit_nuclemd_WMS");
                    // Getting the NUCLEMD ETOKENSERVER from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_ETOKENSERVER = request.getParameter("gridit_nuclemd_ETOKENSERVER");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_MYPROXYSERVER = request.getParameter("gridit_nuclemd_MYPROXYSERVER");
                    // Getting the NUCLEMD PORT from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_PORT = request.getParameter("gridit_nuclemd_PORT");
                    // Getting the NUCLEMD ROBOTID from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_ROBOTID = request.getParameter("gridit_nuclemd_ROBOTID");
                    // Getting the NUCLEMD WEBDAV from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_WEBDAV = request.getParameter("gridit_nuclemd_WEBDAV");
                    // Getting the NUCLEMD ROLE from the portlet request for the GRIDIT VO
                    String gridit_nuclemd_ROLE = request.getParameter("gridit_nuclemd_ROLE");
                    // Getting the NUCLEMD OPTIONS from the portlet request for the GRIDIT VO
                    String[] gridit_nuclemd_OPTIONS = request.getParameterValues("gridit_nuclemd_OPTIONS");

                    String gridit_nuclemd_RENEWAL = "";
                    String gridit_nuclemd_DISABLEVOMS = "";

                    if (gridit_nuclemd_OPTIONS == null){
                        gridit_nuclemd_RENEWAL = "checked";
                        gridit_nuclemd_DISABLEVOMS = "unchecked";
                    } else {
                        Arrays.sort(gridit_nuclemd_OPTIONS);
                        // Get the NUCLEMD RENEWAL from the portlet preferences for the GRIDIT VO
                        gridit_nuclemd_RENEWAL = Arrays.binarySearch(gridit_nuclemd_OPTIONS, "enableRENEWAL") >= 0 ? "checked" : "unchecked";
                        // Get the NUCLEMD DISABLEVOMS from the portlet preferences for the GRIDIT VO
                        gridit_nuclemd_DISABLEVOMS = Arrays.binarySearch(gridit_nuclemd_OPTIONS, "disableVOMS") >= 0 ? "checked" : "unchecked";
                    }
                    
                    int nmax=0;                
                    for (int i = 0; i < gridit_nuclemd_WMS.length; i++)
                        if ( gridit_nuclemd_WMS[i]!=null && (!gridit_nuclemd_WMS[i].trim().equals("N/A")) )                        
                            nmax++;
                    
                    log.info("\n\nLength="+nmax);
                    String[] gridit_nuclemd_WMS_trimmed = new String[nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        gridit_nuclemd_WMS_trimmed[i]=gridit_nuclemd_WMS[i].trim();
                        log.info ("\n\nGRIDIT [" + i + "] WMS=[" + gridit_nuclemd_WMS_trimmed[i] + "]");
                    }

                    // Set the portlet preferences
                    portletPreferences.setValue("gridit_nuclemd_INFRASTRUCTURE", gridit_nuclemd_INFRASTRUCTURE.trim());
                    portletPreferences.setValue("gridit_nuclemd_VONAME", gridit_nuclemd_VONAME.trim());
                    portletPreferences.setValue("gridit_nuclemd_TOPBDII", gridit_nuclemd_TOPBDII.trim());
                    portletPreferences.setValues("gridit_nuclemd_WMS", gridit_nuclemd_WMS_trimmed);
                    portletPreferences.setValue("gridit_nuclemd_ETOKENSERVER", gridit_nuclemd_ETOKENSERVER.trim());
                    portletPreferences.setValue("gridit_nuclemd_MYPROXYSERVER", gridit_nuclemd_MYPROXYSERVER.trim());
                    portletPreferences.setValue("gridit_nuclemd_PORT", gridit_nuclemd_PORT.trim());
                    portletPreferences.setValue("gridit_nuclemd_ROBOTID", gridit_nuclemd_ROBOTID.trim());
                    portletPreferences.setValue("gridit_nuclemd_WEBDAV", gridit_nuclemd_WEBDAV.trim());
                    portletPreferences.setValue("gridit_nuclemd_ROLE", gridit_nuclemd_ROLE.trim());
                    portletPreferences.setValue("gridit_nuclemd_RENEWAL", gridit_nuclemd_RENEWAL);
                    portletPreferences.setValue("gridit_nuclemd_DISABLEVOMS", gridit_nuclemd_DISABLEVOMS);
                    
                    portletPreferences.setValue("nuclemd_APPID", nuclemd_APPID.trim());
                    portletPreferences.setValue("nuclemd_LOGLEVEL", nuclemd_LOGLEVEL.trim());
                    portletPreferences.setValue("nuclemd_METADATA_HOST", nuclemd_METADATA_HOST.trim());
                    portletPreferences.setValue("nuclemd_OUTPUT_PATH", nuclemd_OUTPUT_PATH.trim());
                    portletPreferences.setValue("nuclemd_SOFTWARE", nuclemd_SOFTWARE.trim());
                    portletPreferences.setValue("TRACKING_DB_HOSTNAME", TRACKING_DB_HOSTNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_USERNAME", TRACKING_DB_USERNAME.trim());
                    portletPreferences.setValue("TRACKING_DB_PASSWORD", TRACKING_DB_PASSWORD.trim());
                    portletPreferences.setValue("SMTP_HOST", SMTP_HOST.trim());
                    portletPreferences.setValue("SENDER_MAIL", SENDER_MAIL.trim());
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n\nPROCESS ACTION => " + action
                        + "\n- Storing the NUCLEMD portlet preferences ..."                    
                        + "\n\ngridit_nuclemd_INFRASTRUCTURE: " + gridit_nuclemd_INFRASTRUCTURE
                        + "\ngridit_nuclemd_VONAME: " + gridit_nuclemd_VONAME
                        + "\ngridit_nuclemd_TOPBDII: " + gridit_nuclemd_TOPBDII                    
                        + "\ngridit_nuclemd_ETOKENSERVER: " + gridit_nuclemd_ETOKENSERVER
                        + "\ngridit_nuclemd_MYPROXYSERVER: " + gridit_nuclemd_MYPROXYSERVER
                        + "\ngridit_nuclemd_PORT: " + gridit_nuclemd_PORT
                        + "\ngridit_nuclemd_ROBOTID: " + gridit_nuclemd_ROBOTID
                        + "\ngridit_nuclemd_WEBDAV: " + gridit_nuclemd_WEBDAV
                        + "\ngridit_nuclemd_ROLE: " + gridit_nuclemd_ROLE
                        + "\ngridit_nuclemd_RENEWAL: " + gridit_nuclemd_RENEWAL
                        + "\ngridit_nuclemd_DISABLEVOMS: " + gridit_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + "gridit"
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);
                    }
                }
                
                for (int i=0; i<infras.length; i++)
                log.info("\n - Infrastructure Enabled = " + infras[i]);
                portletPreferences.setValues("nuclemd_ENABLEINFRASTRUCTURE", infras);
                portletPreferences.setValue("lato_nuclemd_ENABLEINFRASTRUCTURE", infras[0]);            
                portletPreferences.setValue("garuda_nuclemd_ENABLEINFRASTRUCTURE", infras[1]);            
                portletPreferences.setValue("eumed_nuclemd_ENABLEINFRASTRUCTURE", infras[2]);
                portletPreferences.setValue("sagrid_nuclemd_ENABLEINFRASTRUCTURE", infras[3]);
                portletPreferences.setValue("see_nuclemd_ENABLEINFRASTRUCTURE", infras[4]);
                portletPreferences.setValue("gridit_nuclemd_ENABLEINFRASTRUCTURE", infras[5]);

                portletPreferences.store();
                response.setPortletMode(PortletMode.VIEW);
            } // end PROCESS ACTION [ CONFIG_NUCLEMD_PORTLET ]
            

            if (action.equals("SUBMIT_NUCLEMD_PORTLET")) {
                log.info("\nPROCESS ACTION => " + action);            
                InfrastructureInfo infrastructures[] = new InfrastructureInfo[6];
                String jdlRequirements[] = new String[2];
                String nuclemd_DEFAULT_STORAGE = "";
                int MAX=0;                
                
                // Getting the NUCLEMD APPID from the portlet preferences
                String nuclemd_APPID = portletPreferences.getValue("nuclemd_APPID", "N/A");
                // Getting the LOGLEVEL from the portlet preferences
                String nuclemd_LOGLEVEL = portletPreferences.getValue("nuclemd_LOGLEVEL", "INFO");
                // Getting the NUCLEMD_METADATA_HOST from the portlet preferences
                String nuclemd_METADATA_HOST = portletPreferences.getValue("nuclemd_METADATA_HOST", "INFO");
                // Getting the NUCLEMD OUTPUT_PATH from the portlet preferences
                String nuclemd_OUTPUT_PATH = portletPreferences.getValue("nuclemd_OUTPUT_PATH", "/tmp");
                // Getting the NUCLEMD SOFTWARE from the portlet preferences
                String nuclemd_SOFTWARE = portletPreferences.getValue("nuclemd_SOFTWARE", "N/A");
                // Getting the TRACKING_DB_HOSTNAME from the portlet request
                String TRACKING_DB_HOSTNAME = portletPreferences.getValue("TRACKING_DB_HOSTNAME", "N/A");
                // Getting the TRACKING_DB_USERNAME from the portlet request
                String TRACKING_DB_USERNAME = portletPreferences.getValue("TRACKING_DB_USERNAME", "N/A");
                // Getting the TRACKING_DB_PASSWORD from the portlet request
                String TRACKING_DB_PASSWORD = portletPreferences.getValue("TRACKING_DB_PASSWORD","N/A");
                // Getting the SMTP_HOST from the portlet request
                String SMTP_HOST = portletPreferences.getValue("SMTP_HOST","N/A");
                // Getting the SENDER_MAIL from the portlet request
                String SENDER_MAIL = portletPreferences.getValue("SENDER_MAIL","N/A");
                
                String lato_nuclemd_ENABLEINFRASTRUCTURE =
                        portletPreferences.getValue("lato_nuclemd_ENABLEINFRASTRUCTURE","null");
                String garuda_nuclemd_ENABLEINFRASTRUCTURE =
                        portletPreferences.getValue("garuda_nuclemd_ENABLEINFRASTRUCTURE","null");
                String eumed_nuclemd_ENABLEINFRASTRUCTURE =
                        portletPreferences.getValue("eumed_nuclemd_ENABLEINFRASTRUCTURE","null");
                String sagrid_nuclemd_ENABLEINFRASTRUCTURE =
                        portletPreferences.getValue("sagrid_nuclemd_ENABLEINFRASTRUCTURE","null");
                String see_nuclemd_ENABLEINFRASTRUCTURE =
                        portletPreferences.getValue("see_nuclemd_ENABLEINFRASTRUCTURE","null");
                String gridit_nuclemd_ENABLEINFRASTRUCTURE =
                        portletPreferences.getValue("gridit_nuclemd_ENABLEINFRASTRUCTURE","null");
                
                if (lato_nuclemd_ENABLEINFRASTRUCTURE != null &&
                    lato_nuclemd_ENABLEINFRASTRUCTURE.equals("lato"))
                {
                    MAX++;
                    // Getting the NUCLEMD VONAME from the portlet preferences for LATO
                    String lato_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("lato_nuclemd_INFRASTRUCTURE", "N/A");
                    // Getting the NUCLEMD VONAME from the portlet preferences for LATO
                    String lato_nuclemd_LOGIN = portletPreferences.getValue("lato_nuclemd_LOGIN", "N/A");
                    // Getting the NUCLEMD TOPPBDII from the portlet preferences for LATO
                    String lato_nuclemd_PASSWD = portletPreferences.getValue("lato_nuclemd_PASSWD", "N/A");
                    // Getting the NUCLEMD WMS from the portlet preferences for LATO                
                    String[] lato_nuclemd_WMS = portletPreferences.getValues("lato_nuclemd_WMS", new String[5]);
                    // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for LATO
                    String lato_nuclemd_ETOKENSERVER = portletPreferences.getValue("lato_nuclemd_ETOKENSERVER", "N/A");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for LATO
                    String lato_nuclemd_MYPROXYSERVER = portletPreferences.getValue("lato_nuclemd_MYPROXYSERVER", "N/A");
                    // Getting the NUCLEMD PORT from the portlet preferences for LATO
                    String lato_nuclemd_PORT = portletPreferences.getValue("lato_nuclemd_PORT", "N/A");
                    // Getting the NUCLEMD ROBOTID from the portlet preferences for LATO
                    String lato_nuclemd_ROBOTID = portletPreferences.getValue("lato_nuclemd_ROBOTID", "N/A");
                    // Getting the NUCLEMD WEBDAV from the portlet preferences for LATO
                    String lato_nuclemd_WEBDAV = portletPreferences.getValue("lato_nuclemd_WEBDAV", "N/A");
                    // Getting the NUCLEMD ROLE from the portlet preferences for LATO
                    String lato_nuclemd_ROLE = portletPreferences.getValue("lato_nuclemd_ROLE", "N/A");
                    // Getting the NUCLEMD RENEWAL from the portlet preferences for LATO
                    String lato_nuclemd_RENEWAL = portletPreferences.getValue("lato_nuclemd_RENEWAL", "checked");
                    // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for LATO
                    String lato_nuclemd_DISABLEVOMS = portletPreferences.getValue("lato_nuclemd_DISABLEVOMS", "unchecked");
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n- Getting the NUCLEMD portlet preferences ..."
                        + "\nlato_nuclemd_INFRASTRUCTURE: " + lato_nuclemd_INFRASTRUCTURE
                        + "\nlato_nuclemd_LOGIN: " + lato_nuclemd_LOGIN
                        + "\nlato_nuclemd_PASSWD: " + lato_nuclemd_PASSWD                    
                        + "\nlato_nuclemd_ETOKENSERVER: " + lato_nuclemd_ETOKENSERVER
                        + "\nlato_nuclemd_MYPROXYSERVER: " + lato_nuclemd_MYPROXYSERVER
                        + "\nlato_nuclemd_PORT: " + lato_nuclemd_PORT
                        + "\nlato_nuclemd_ROBOTID: " + lato_nuclemd_ROBOTID
                        + "\nlato_nuclemd_WEBDAV: " + lato_nuclemd_WEBDAV
                        + "\nlato_nuclemd_ROLE: " + lato_nuclemd_ROLE
                        + "\nlato_nuclemd_RENEWAL: " + lato_nuclemd_RENEWAL
                        + "\nlato_nuclemd_DISABLEVOMS: " + lato_nuclemd_DISABLEVOMS
                       
                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + lato_nuclemd_ENABLEINFRASTRUCTURE
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);
                    }
                    
                    // Defining the WMS list for the "LATO" Infrastructure
                    int nmax=0;
                    for (int i = 0; i < lato_nuclemd_WMS.length; i++)
                        if ((lato_nuclemd_WMS[i]!=null) && (!lato_nuclemd_WMS[i].equals("N/A"))) nmax++;

                    String lato_wmsList[] = new String [nmax];                
                        for (int i = 0; i < nmax; i++)
                        {
                            if (lato_nuclemd_WMS[i]!=null) {
                            lato_wmsList[i]=lato_nuclemd_WMS[i].trim();
                            log.info ("\n\n[" + nmax
                                              + "] Submit to LATO ["
                                              + i
                                              + "] using WMS=["
                                              + lato_wmsList[i]
                                              + "]");
                            }
                        }
                    
                    infrastructures[0] = new InfrastructureInfo(
                            "SSH",
                            "ssh",
                            lato_nuclemd_LOGIN,
                            lato_nuclemd_PASSWD,
                            lato_wmsList);               
                }
                            
                if (garuda_nuclemd_ENABLEINFRASTRUCTURE != null &&
                    garuda_nuclemd_ENABLEINFRASTRUCTURE.equals("garuda"))
                {
                    MAX++;
                    // Getting the NUCLEMDVONAME from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("garuda_nuclemd_INFRASTRUCTURE", "N/A");
                    // Getting the NUCLEMD VONAME from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_VONAME = portletPreferences.getValue("garuda_nuclemd_VONAME", "N/A");
                    // Getting the NUCLEMD TOPPBDII from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_TOPBDII = portletPreferences.getValue("garuda_nuclemd_TOPBDII", "N/A");
                    // Getting the NUCLEMD WMS from the portlet preferences for the GARUDA VO                
                    String[] garuda_nuclemd_WMS = portletPreferences.getValues("garuda_nuclemd_WMS", new String[5]);
                    // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_ETOKENSERVER = portletPreferences.getValue("garuda_nuclemd_ETOKENSERVER", "N/A");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_MYPROXYSERVER = portletPreferences.getValue("garuda_nuclemd_MYPROXYSERVER", "N/A");
                    // Getting the NUCLEMD PORT from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_PORT = portletPreferences.getValue("garuda_nuclemd_PORT", "N/A");
                    // Getting the NUCLEMD ROBOTID from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_ROBOTID = portletPreferences.getValue("garuda_nuclemd_ROBOTID", "N/A");
                    // Getting the NUCLEMD WEBDAV from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_WEBDAV = portletPreferences.getValue("garuda_nuclemd_WEBDAV", "N/A");
                    // Getting the NUCLEMD ROLE from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_ROLE = portletPreferences.getValue("garuda_nuclemd_ROLE", "N/A");
                    // Getting the NUCLEMD RENEWAL from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_RENEWAL = portletPreferences.getValue("garuda_nuclemd_RENEWAL", "checked");
                    // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GARUDA VO
                    String garuda_nuclemd_DISABLEVOMS = portletPreferences.getValue("garuda_nuclemd_DISABLEVOMS", "unchecked");                    
                            
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n- Getting the NUCLEMD portlet preferences ..."
                        + "\ngaruda_nuclemd_INFRASTRUCTURE: " + garuda_nuclemd_INFRASTRUCTURE
                        + "\ngaruda_nuclemd_VONAME: " + garuda_nuclemd_VONAME
                        + "\ngaruda_nuclemd_TOPBDII: " + garuda_nuclemd_TOPBDII                    
                        + "\ngaruda_nuclemd_ETOKENSERVER: " + garuda_nuclemd_ETOKENSERVER
                        + "\ngaruda_nuclemd_MYPROXYSERVER: " + garuda_nuclemd_MYPROXYSERVER
                        + "\ngaruda_nuclemd_PORT: " + garuda_nuclemd_PORT
                        + "\ngaruda_nuclemd_ROBOTID: " + garuda_nuclemd_ROBOTID
                        + "\ngaruda_nuclemd_WEBDAV: " + garuda_nuclemd_WEBDAV
                        + "\ngaruda_nuclemd_ROLE: " + garuda_nuclemd_ROLE
                        + "\ngaruda_nuclemd_RENEWAL: " + garuda_nuclemd_RENEWAL
                        + "\ngaruda_nuclemd_DISABLEVOMS: " + garuda_nuclemd_DISABLEVOMS
                       
                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + garuda_nuclemd_ENABLEINFRASTRUCTURE
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);                                            
                    }
                    
                    // Defining the WMS list for the "GARUDA" Infrastructure
                    int nmax=0;
                    for (int i = 0; i < garuda_nuclemd_WMS.length; i++)
                        if ((garuda_nuclemd_WMS[i]!=null) && (!garuda_nuclemd_WMS[i].equals("N/A"))) nmax++;

                    String wmsList[] = new String [nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        if (garuda_nuclemd_WMS[i]!=null) {
                        wmsList[i]=garuda_nuclemd_WMS[i].trim();
                        log.info ("\n\n[" + nmax
                                          + "] Submit to GARUDA ["
                                          + i
                                          + "] using WMSGRAM=["
                                          + wmsList[i]
                                          + "]");
                        }
                    }

                    infrastructures[1] = new InfrastructureInfo(
                       "GARUDA",
                        "wsgram",
                        "",
                        wmsList,
                        garuda_nuclemd_ETOKENSERVER,
                        garuda_nuclemd_PORT,
                        garuda_nuclemd_ROBOTID,
                        garuda_nuclemd_VONAME,
                        garuda_nuclemd_ROLE);
                }
                
                if (eumed_nuclemd_ENABLEINFRASTRUCTURE != null &&
                    eumed_nuclemd_ENABLEINFRASTRUCTURE.equals("eumed"))
                {
                    MAX++;
                    // Getting the NUCLEMD VONAME from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("eumed_nuclemd_INFRASTRUCTURE", "N/A");
                    // Getting the NUCLEMD VONAME from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_VONAME = portletPreferences.getValue("eumed_nuclemd_VONAME", "N/A");
                    // Getting the NUCLEMD TOPPBDII from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_TOPBDII = portletPreferences.getValue("eumed_nuclemd_TOPBDII", "N/A");
                    // Getting the NUCLEMD WMS from the portlet preferences for the EUMED VO
                    String[] eumed_nuclemd_WMS = portletPreferences.getValues("eumed_nuclemd_WMS", new String[5]);
                    // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_ETOKENSERVER = portletPreferences.getValue("eumed_nuclemd_ETOKENSERVER", "N/A");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_MYPROXYSERVER = portletPreferences.getValue("eumed_nuclemd_MYPROXYSERVER", "N/A");
                    // Getting the NUCLEMD PORT from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_PORT = portletPreferences.getValue("eumed_nuclemd_PORT", "N/A");
                    // Getting the NUCLEMD ROBOTID from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_ROBOTID = portletPreferences.getValue("eumed_nuclemd_ROBOTID", "N/A");
                    // Getting the NUCLEMD WEBDAV from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_WEBDAV = portletPreferences.getValue("eumed_nuclemd_WEBDAV", "N/A");
                    // Getting the NUCLEMD ROLE from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_ROLE = portletPreferences.getValue("eumed_nuclemd_ROLE", "N/A");
                    // Getting the NUCLEMD RENEWAL from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_RENEWAL = portletPreferences.getValue("eumed_nuclemd_RENEWAL", "checked");
                    // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the EUMED VO
                    String eumed_nuclemd_DISABLEVOMS = portletPreferences.getValue("eumed_nuclemd_DISABLEVOMS", "unchecked");                    
                    nuclemd_DEFAULT_STORAGE = eumed_nuclemd_WEBDAV;
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n- Getting the NUCLEMD portlet preferences ..."
                        + "\n\neumed_nuclemd_INFRASTRUCTURE: " + eumed_nuclemd_INFRASTRUCTURE
                        + "\neumed_nuclemd_VONAME: " + eumed_nuclemd_VONAME
                        + "\neumed_nuclemd_TOPBDII: " + eumed_nuclemd_TOPBDII                    
                        + "\neumed_nuclemd_ETOKENSERVER: " + eumed_nuclemd_ETOKENSERVER
                        + "\neumed_nuclemd_MYPROXYSERVER: " + eumed_nuclemd_MYPROXYSERVER
                        + "\neumed_nuclemd_PORT: " + eumed_nuclemd_PORT
                        + "\neumed_nuclemd_ROBOTID: " + eumed_nuclemd_ROBOTID
                        + "\neumed_nuclemd_WEBDAV: " + eumed_nuclemd_WEBDAV
                        + "\neumed_nuclemd_ROLE: " + eumed_nuclemd_ROLE
                        + "\neumed_nuclemd_RENEWAL: " + eumed_nuclemd_RENEWAL
                        + "\neumed_nuclemd_DISABLEVOMS: " + eumed_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + eumed_nuclemd_ENABLEINFRASTRUCTURE
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_DEFAULT_STORAGE: " + nuclemd_DEFAULT_STORAGE
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);                                           
                    }
                    
                    // Defining the WMS list for the "EUMED" Infrastructure
                    int nmax=0;
                    for (int i = 0; i < eumed_nuclemd_WMS.length; i++)
                        if ((eumed_nuclemd_WMS[i]!=null) && (!eumed_nuclemd_WMS[i].equals("N/A"))) nmax++;
                    
                    String wmsList[] = new String [nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        if (eumed_nuclemd_WMS[i]!=null) {
                        wmsList[i]=eumed_nuclemd_WMS[i].trim();
                        log.info ("\n\n[" + nmax
                                          + "] Submit to EUMED ["
                                          + i
                                          + "] using WMS=["
                                          + wmsList[i]
                                          + "]");
                        }
                    }
                    
                    // Setting the JDL Requirements
                    String[] SOFTWARE_LIST = nuclemd_SOFTWARE.split(",");
                    int index=0;
                    
                    for(String SOFTWARE: SOFTWARE_LIST)
                    {
                        jdlRequirements[index++] = 
                            "VO-"
                            + eumed_nuclemd_VONAME
                            + "-"
                            + SOFTWARE;                            
                    }
                    
                    infrastructures[2] = new InfrastructureInfo(
                        eumed_nuclemd_VONAME,
                        eumed_nuclemd_TOPBDII,
                        wmsList,
                        eumed_nuclemd_ETOKENSERVER,
                        eumed_nuclemd_PORT,
                        eumed_nuclemd_ROBOTID,
                        eumed_nuclemd_VONAME,
                        eumed_nuclemd_ROLE,
                        true, // set the RFC proxy for the infrastructure                            
                        jdlRequirements[0] + "," + jdlRequirements[1]);
                        //"VO-" + eumed_nuclemd_VONAME + "-" + nuclemd_SOFTWARE);
                }
                
                
                
                if (sagrid_nuclemd_ENABLEINFRASTRUCTURE != null &&
                    sagrid_nuclemd_ENABLEINFRASTRUCTURE.equals("sagrid"))
                {
                    MAX++;
                    // Getting the NUCLEMD VONAME from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("sagrid_nuclemd_INFRASTRUCTURE", "N/A");
                    // Getting the NUCLEMD VONAME from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_VONAME = portletPreferences.getValue("sagrid_nuclemd_VONAME", "N/A");
                    // Getting the NUCLEMD TOPPBDII from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_TOPBDII = portletPreferences.getValue("sagrid_nuclemd_TOPBDII", "N/A");
                    // Getting the NUCLEMD WMS from the portlet preferences for the SAGRID VO
                    String[] sagrid_nuclemd_WMS = portletPreferences.getValues("sagrid_nuclemd_WMS", new String[5]);
                    // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_ETOKENSERVER = portletPreferences.getValue("sagrid_nuclemd_ETOKENSERVER", "N/A");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_MYPROXYSERVER = portletPreferences.getValue("sagrid_nuclemd_MYPROXYSERVER", "N/A");
                    // Getting the NUCLEMD PORT from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_PORT = portletPreferences.getValue("sagrid_nuclemd_PORT", "N/A");
                    // Getting the NUCLEMD ROBOTID from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_ROBOTID = portletPreferences.getValue("sagrid_nuclemd_ROBOTID", "N/A");
                    // Getting the NUCLEMD WEBDAV from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_WEBDAV = portletPreferences.getValue("sagrid_nuclemd_WEBDAV", "N/A");
                    // Getting the NUCLEMD ROLE from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_ROLE = portletPreferences.getValue("sagrid_nuclemd_ROLE", "N/A");
                    // Getting the NUCLEMD RENEWAL from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_RENEWAL = portletPreferences.getValue("sagrid_nuclemd_RENEWAL", "checked");
                    // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the SAGRID VO
                    String sagrid_nuclemd_DISABLEVOMS = portletPreferences.getValue("sagrid_nuclemd_DISABLEVOMS", "unchecked");
                    nuclemd_DEFAULT_STORAGE = sagrid_nuclemd_WEBDAV;
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n- Getting the NUCLEMD portlet preferences ..."
                        + "\n\nsagrid_nuclemd_INFRASTRUCTURE: " + sagrid_nuclemd_INFRASTRUCTURE
                        + "\nsagrid_nuclemd_VONAME: " + sagrid_nuclemd_VONAME
                        + "\nsagrid_nuclemd_TOPBDII: " + sagrid_nuclemd_TOPBDII                    
                        + "\nsagrid_nuclemd_ETOKENSERVER: " + sagrid_nuclemd_ETOKENSERVER
                        + "\nsagrid_nuclemd_MYPROXYSERVER: " + sagrid_nuclemd_MYPROXYSERVER
                        + "\nsagrid_nuclemd_PORT: " + sagrid_nuclemd_PORT
                        + "\nsagrid_nuclemd_ROBOTID: " + sagrid_nuclemd_ROBOTID
                        + "\nsagrid_nuclemd_WEBDAV: " + sagrid_nuclemd_WEBDAV
                        + "\nsagrid_nuclemd_ROLE: " + sagrid_nuclemd_ROLE
                        + "\nsagrid_nuclemd_RENEWAL: " + sagrid_nuclemd_RENEWAL
                        + "\nsagrid_nuclemd_DISABLEVOMS: " + sagrid_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + eumed_nuclemd_ENABLEINFRASTRUCTURE
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_DEFAULT_STORAGE: " + nuclemd_DEFAULT_STORAGE
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);                                            
                    }
                    
                    // Defining the WMS list for the "SAGRID" Infrastructure
                    int nmax=0;
                    for (int i = 0; i < sagrid_nuclemd_WMS.length; i++)
                        if ((sagrid_nuclemd_WMS[i]!=null) && (!sagrid_nuclemd_WMS[i].equals("N/A"))) nmax++;
                    
                    String wmsList[] = new String [nmax];
                    for (int i = 0; i < nmax; i++)
                    {
                        if (sagrid_nuclemd_WMS[i]!=null) {
                        wmsList[i]=sagrid_nuclemd_WMS[i].trim();
                        log.info ("\n\n[" + nmax
                                          + "] Submit to SAGRID ["
                                          + i
                                          + "] using WMS=["
                                          + wmsList[i]
                                          + "]");
                        }
                    }
                    
                    // Setting the JDL Requirements
                    String[] SOFTWARE_LIST = nuclemd_SOFTWARE.split(",");
                    int index=0;
                    
                    for(String SOFTWARE: SOFTWARE_LIST)
                    {
                        jdlRequirements[index++] = 
                            "VO-"
                            + sagrid_nuclemd_VONAME
                            + "-"
                            + SOFTWARE;                            
                    }
                    
                    infrastructures[3] = new InfrastructureInfo(
                        sagrid_nuclemd_VONAME,
                        sagrid_nuclemd_TOPBDII,
                        wmsList,
                        sagrid_nuclemd_ETOKENSERVER,
                        sagrid_nuclemd_PORT,
                        sagrid_nuclemd_ROBOTID,
                        sagrid_nuclemd_VONAME,
                        sagrid_nuclemd_ROLE,
                        true, // set the RFC proxy for the infrastructure                            
                        jdlRequirements[0] + "," + jdlRequirements[1]);
                        //"VO-" + sagrid_nuclemd_VONAME + "-" + nuclemd_SOFTWARE);
                }

                if (see_nuclemd_ENABLEINFRASTRUCTURE != null &&
                    see_nuclemd_ENABLEINFRASTRUCTURE.equals("see")) 
                {
                    MAX++;
                    // Getting the NUCLEMD VONAME from the portlet preferences for the SEE VO
                    String see_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("see_nuclemd_INFRASTRUCTURE", "N/A");
                    // Getting the NUCLEMD VONAME from the portlet preferences for the SEE VO
                    String see_nuclemd_VONAME = portletPreferences.getValue("see_nuclemd_VONAME", "N/A");
                    // Getting the NUCLEMD TOPPBDII from the portlet preferences for the SEE VO
                    String see_nuclemd_TOPBDII = portletPreferences.getValue("see_nuclemd_TOPBDII", "N/A");
                    // Getting the NUCLEMD WMS from the portlet preferences for the SEE VO
                    String[] see_nuclemd_WMS = portletPreferences.getValues("see_nuclemd_WMS", new String[5]);
                    // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the SEE VO
                    String see_nuclemd_ETOKENSERVER = portletPreferences.getValue("see_nuclemd_ETOKENSERVER", "N/A");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the SEE VO
                    String see_nuclemd_MYPROXYSERVER = portletPreferences.getValue("see_nuclemd_MYPROXYSERVER", "N/A");
                    // Getting the NUCLEMD PORT from the portlet preferences for the SEE VO
                    String see_nuclemd_PORT = portletPreferences.getValue("see_nuclemd_PORT", "N/A");
                    // Getting the NUCLEMD ROBOTID from the portlet preferences for the SEE VO
                    String see_nuclemd_ROBOTID = portletPreferences.getValue("see_nuclemd_ROBOTID", "N/A");
                    // Getting the NUCLEMD WEBDAV from the portlet preferences for the SEE VO
                    String see_nuclemd_WEBDAV = portletPreferences.getValue("see_nuclemd_WEBDAV", "N/A");
                    // Getting the NUCLEMD ROLE from the portlet preferences for the SEE VO
                    String see_nuclemd_ROLE = portletPreferences.getValue("see_nuclemd_ROLE", "N/A");
                    // Getting the NUCLEMD RENEWAL from the portlet preferences for the SEE VO
                    String see_nuclemd_RENEWAL = portletPreferences.getValue("see_nuclemd_RENEWAL", "checked");
                    // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the SEE VO
                    String see_nuclemd_DISABLEVOMS = portletPreferences.getValue("see_nuclemd_DISABLEVOMS", "unchecked");
                    nuclemd_DEFAULT_STORAGE = see_nuclemd_WEBDAV;
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n- Getting the NUCLEMD portlet preferences ..."
                        + "\n\nsee_nuclemd_INFRASTRUCTURE: " + see_nuclemd_INFRASTRUCTURE
                        + "\nsee_nuclemd_VONAME: " + see_nuclemd_VONAME
                        + "\nsee_nuclemd_TOPBDII: " + see_nuclemd_TOPBDII                        
                        + "\nsee_nuclemd_ETOKENSERVER: " + see_nuclemd_ETOKENSERVER
                        + "\nsee_nuclemd_MYPROXYSERVER: " + see_nuclemd_MYPROXYSERVER
                        + "\nsee_nuclemd_PORT: " + see_nuclemd_PORT
                        + "\nsee_nuclemd_ROBOTID: " + see_nuclemd_ROBOTID
                        + "\nsee_nuclemd_WEBDAV: " + see_nuclemd_WEBDAV
                        + "\nsee_nuclemd_ROLE: " + see_nuclemd_ROLE
                        + "\nsee_nuclemd_RENEWAL: " + see_nuclemd_RENEWAL
                        + "\nsee_nuclemd_DISABLEVOMS: " + see_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + see_nuclemd_ENABLEINFRASTRUCTURE
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_DEFAULT_STORAGE: " + nuclemd_DEFAULT_STORAGE
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH                        
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);                                            
                    }
                    
                    // Defining the WMS list for the "SEE" Infrastructure
                    int nmax=0;
                    for (int i = 0; i < see_nuclemd_WMS.length; i++)
                        if ((see_nuclemd_WMS[i]!=null) && (!see_nuclemd_WMS[i].equals("N/A"))) nmax++;
                    
                    String wmsList[] = new String [see_nuclemd_WMS.length];
                    for (int i = 0; i < see_nuclemd_WMS.length; i++)
                    {
                        if (see_nuclemd_WMS[i]!=null) {
                        wmsList[i]=see_nuclemd_WMS[i].trim();
                        log.info ("\n\nSubmit for SEE [" + i + "] using WMS=[" + wmsList[i] + "]");
                        }
                    }
                    
                    // Setting the JDL Requirements
                    String[] SOFTWARE_LIST = nuclemd_SOFTWARE.split(",");
                    int index=0;
                    
                    for(String SOFTWARE: SOFTWARE_LIST)
                    {
                        jdlRequirements[index++] = 
                        "VO-"
                        + see_nuclemd_VONAME
                        + "-"
                        + SOFTWARE;                            
                    }
                    
                    infrastructures[4] = new InfrastructureInfo(
                        see_nuclemd_VONAME,
                        see_nuclemd_TOPBDII,
                        wmsList,
                        see_nuclemd_ETOKENSERVER,
                        see_nuclemd_PORT,
                        see_nuclemd_ROBOTID,
                        see_nuclemd_VONAME,
                        see_nuclemd_ROLE,
                        true, // set the RFC proxy for the infrastructure
                        jdlRequirements[0] + "," + jdlRequirements[1]);
                        //"VO-" + see_nuclemd_VONAME + "-" + nuclemd_SOFTWARE);                                                            
                }
                
                if (gridit_nuclemd_ENABLEINFRASTRUCTURE != null &&
                    gridit_nuclemd_ENABLEINFRASTRUCTURE.equals("gridit")) 
                {
                    MAX++;
                    // Getting the NUCLEMD VONAME from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_INFRASTRUCTURE = portletPreferences.getValue("gridit_nuclemd_INFRASTRUCTURE", "N/A");
                    // Getting the NUCLEMD VONAME from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_VONAME = portletPreferences.getValue("gridit_nuclemd_VONAME", "N/A");
                    // Getting the NUCLEMD TOPPBDII from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_TOPBDII = portletPreferences.getValue("gridit_nuclemd_TOPBDII", "N/A");
                    // Getting the NUCLEMD WMS from the portlet preferences for the GRIDIT VO
                    String[] gridit_nuclemd_WMS = portletPreferences.getValues("gridit_nuclemd_WMS", new String[5]);
                    // Getting the NUCLEMD ETOKENSERVER from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_ETOKENSERVER = portletPreferences.getValue("gridit_nuclemd_ETOKENSERVER", "N/A");
                    // Getting the NUCLEMD MYPROXYSERVER from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_MYPROXYSERVER = portletPreferences.getValue("gridit_nuclemd_MYPROXYSERVER", "N/A");
                    // Getting the NUCLEMD PORT from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_PORT = portletPreferences.getValue("gridit_nuclemd_PORT", "N/A");
                    // Getting the NUCLEMD ROBOTID from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_ROBOTID = portletPreferences.getValue("gridit_nuclemd_ROBOTID", "N/A");
                    // Getting the NUCLEMD WEBDAV from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_WEBDAV = portletPreferences.getValue("gridit_nuclemd_WEBDAV", "N/A");
                    // Getting the NUCLEMD ROLE from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_ROLE = portletPreferences.getValue("gridit_nuclemd_ROLE", "N/A");
                    // Getting the NUCLEMD RENEWAL from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_RENEWAL = portletPreferences.getValue("gridit_nuclemd_RENEWAL", "checked");
                    // Getting the NUCLEMD DISABLEVOMS from the portlet preferences for the GRIDIT VO
                    String gridit_nuclemd_DISABLEVOMS = portletPreferences.getValue("gridit_nuclemd_DISABLEVOMS", "unchecked");                    
                    nuclemd_DEFAULT_STORAGE = gridit_nuclemd_WEBDAV;
                    
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE")) {
                    log.info("\n- Getting the NUCLEMD portlet preferences ..."
                        + "\n\ngridit_nuclemd_INFRASTRUCTURE: " + gridit_nuclemd_INFRASTRUCTURE
                        + "\ngridit_nuclemd_VONAME: " + gridit_nuclemd_VONAME
                        + "\ngridit_nuclemd_TOPBDII: " + gridit_nuclemd_TOPBDII                        
                        + "\ngridit_nuclemd_ETOKENSERVER: " + gridit_nuclemd_ETOKENSERVER
                        + "\ngridit_nuclemd_MYPROXYSERVER: " + gridit_nuclemd_MYPROXYSERVER
                        + "\ngridit_nuclemd_PORT: " + gridit_nuclemd_PORT
                        + "\ngridit_nuclemd_ROBOTID: " + gridit_nuclemd_ROBOTID
                        + "\ngridit_nuclemd_WEBDAV: " + gridit_nuclemd_WEBDAV
                        + "\ngridit_nuclemd_ROLE: " + gridit_nuclemd_ROLE
                        + "\ngridit_nuclemd_RENEWAL: " + gridit_nuclemd_RENEWAL
                        + "\ngridit_nuclemd_DISABLEVOMS: " + gridit_nuclemd_DISABLEVOMS

                        + "\n\nnuclemd_ENABLEINFRASTRUCTURE: " + see_nuclemd_ENABLEINFRASTRUCTURE
                        + "\nnuclemd_APPID: " + nuclemd_APPID
                        + "\nnuclemd_LOGLEVEL: " + nuclemd_LOGLEVEL
                        + "\nnuclemd_METADATA_HOST: " + nuclemd_METADATA_HOST
                        + "\nnuclemd_DEFAULT_STORAGE: " + nuclemd_DEFAULT_STORAGE
                        + "\nnuclemd_OUTPUT_PATH: " + nuclemd_OUTPUT_PATH                        
                        + "\nnuclemd_SOFTWARE: " + nuclemd_SOFTWARE
                        + "\nTracking_DB_Hostname: " + TRACKING_DB_HOSTNAME
                        + "\nTracking_DB_Username: " + TRACKING_DB_USERNAME
                        + "\nTracking_DB_Password: " + TRACKING_DB_PASSWORD
                        + "\nSMTP_HOST: " + SMTP_HOST
                        + "\nSENDER_MAIL: " + SENDER_MAIL);                                            
                    }
                    
                    // Defining the WMS list for the "GRIDIT" Infrastructure
                    int nmax=0;
                    for (int i = 0; i < gridit_nuclemd_WMS.length; i++)
                        if ((gridit_nuclemd_WMS[i]!=null) && (!gridit_nuclemd_WMS[i].equals("N/A"))) nmax++;
                    
                    String wmsList[] = new String [gridit_nuclemd_WMS.length];
                    for (int i = 0; i < gridit_nuclemd_WMS.length; i++)
                    {
                        if (gridit_nuclemd_WMS[i]!=null) {
                        wmsList[i]=gridit_nuclemd_WMS[i].trim();
                        log.info ("\n\nSubmit to GRIDIT [" + i + "] using WMS=[" + wmsList[i] + "]");
                        }
                    }
                    
                    // Setting the JDL Requirements
                    String[] SOFTWARE_LIST = nuclemd_SOFTWARE.split(",");
                    int index=0;
                    
                    for(String SOFTWARE: SOFTWARE_LIST)
                    {
                        jdlRequirements[index++] = 
                        "VO-"
                        + gridit_nuclemd_VONAME
                        + "-"
                        + SOFTWARE;                            
                    }
                    
                    infrastructures[5] = new InfrastructureInfo(
                        gridit_nuclemd_VONAME,
                        gridit_nuclemd_TOPBDII,
                        wmsList,
                        gridit_nuclemd_ETOKENSERVER,
                        gridit_nuclemd_PORT,
                        gridit_nuclemd_ROBOTID,
                        gridit_nuclemd_VONAME,
                        gridit_nuclemd_ROLE,
                        true); // set the RFC proxy for the infrastructure
                        //jdlRequirements[0] + "," + jdlRequirements[1]);                       
                }
                
                String[] NUCLEMD_Parameters = new String [9];                

                // Upload the input settings for the application
                NUCLEMD_Parameters = uploadNuclemdSettings( request, response, username );
                //String trimmed = NUCLEMD_Parameters[1].replaceAll("\\s", "_");

                log.info ("\n\n [ NUCLEMD simulation settings ]");
                log.info("\n- Input Parameters: ");
                log.info("\n- Release = " + NUCLEMD_Parameters[8]);
                log.info("\n- Description = " + NUCLEMD_Parameters[0]);
                log.info("\n- Computing Resource = " + NUCLEMD_Parameters[1]);
                log.info("\n- Enable Notification = " + NUCLEMD_Parameters[5]);
                log.info("\n- WallClock Time = " + NUCLEMD_Parameters[6]);;
                log.info("\n- Enable Demo = " + NUCLEMD_Parameters[7]);
                
                log.info ("\n\n [ Additional settings (for the eTokenSever) ]");
                log.info("\n- Username = " + username);
                log.info("\n- Portal = " + portal);
                                
                if (NUCLEMD_Parameters[7] == null) 
                {
                log.info("\n- INP File = " + NUCLEMD_Parameters[2]);                
                log.info("\n- CONF(1) File = " + NUCLEMD_Parameters[3]);
                log.info("\n- CONF(2) File = " + NUCLEMD_Parameters[4]);
                } else log.info("\n- Demo input files selected ");
                
                // Preparing to submit jobs in different grid infrastructure..
                //=============================================================
                // IMPORTANT: INSTANCIATE THE MultiInfrastructureJobSubmission
                //            CLASS USING THE EMPTY CONSTRUCTOR WHEN
                //            WHEN THE PORTLET IS DEPLOYED IN PRODUCTION!!!
                //=============================================================
                /*MultiInfrastructureJobSubmission NuclemdMultiJobSubmission =
                new MultiInfrastructureJobSubmission(TRACKING_DB_HOSTNAME,
                                                     TRACKING_DB_USERNAME,
                                                     TRACKING_DB_PASSWORD);*/
                
                MultiInfrastructureJobSubmission NuclemdMultiJobSubmission =
                    new MultiInfrastructureJobSubmission();

                // Set the list of infrastructure(s) activated for the portlet           
                if (infrastructures[0]!=null) {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("\n- Adding the LATO Infrastructure.");
                     NuclemdMultiJobSubmission.addInfrastructure(infrastructures[0]); 
                }            
                if (infrastructures[1]!=null) {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("\n- Adding the GARUDA Infrastructure.");
                     NuclemdMultiJobSubmission.addInfrastructure(infrastructures[1]); 
                }
                if (infrastructures[2]!=null) {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("\n- Adding the EUMED Infrastructure.");
                     NuclemdMultiJobSubmission.addInfrastructure(infrastructures[2]);
                }
                if (infrastructures[3]!=null) {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("\n- Adding the SAGRID Infrastructure.");
                     NuclemdMultiJobSubmission.addInfrastructure(infrastructures[3]);
                }
                if (infrastructures[4]!=null) {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("\n- Adding the SEE Infrastructure.");
                     NuclemdMultiJobSubmission.addInfrastructure(infrastructures[4]);
                }
                if (infrastructures[5]!=null) {
                    if (nuclemd_LOGLEVEL.trim().equals("VERBOSE"))
                        log.info("\n- Adding the GRIDIT Infrastructure.");
                     NuclemdMultiJobSubmission.addInfrastructure(infrastructures[5]);
                }
                
                String NuclemdFilesPath = getPortletContext().getRealPath("/") +
                                        "WEB-INF/config";                        
                
                // Set the Output path forresults
                //NuclemdMultiJobSubmission.setOutputPath("/tmp");
                NuclemdMultiJobSubmission.setOutputPath(nuclemd_OUTPUT_PATH);
                            
                // Set the StandardOutput for NUCLEMD
                NuclemdMultiJobSubmission.setJobOutput(".std.txt");

                // Set the StandardError for NUCLEMD
                NuclemdMultiJobSubmission.setJobError(".std.err");
                
                // Set the Executable for NUCLEMD
                NuclemdMultiJobSubmission.setExecutable("start_nuclemd.sh");                        
                
                String InputSandbox = "";
                String Arguments = "";
                
                InfrastructureInfo infrastructure = 
                        NuclemdMultiJobSubmission.getInfrastructure();
                    
                String Middleware = null;
                if (infrastructure.getMiddleware().equals("glite"))
                    Middleware = "gLite";
                    
                if (infrastructure.getMiddleware().equals("wsgram"))
                    Middleware = "wsgram";
                    
                if (infrastructure.getMiddleware().equals("ssh"))
                    Middleware = "ssh";
                
                log.info("\n- Selected Infrastructure = " + infrastructure.getName());
                log.info("\n- Enabled Middleware = " + Middleware);
                                          
                if (NUCLEMD_Parameters[7] != null) 
                {
                    // Demo ?                    
                    if (NUCLEMD_Parameters[8].equals("nuclemd_ver1")) 
                    {
                        // Release v0.1
                        InputSandbox = 
                            NuclemdFilesPath + "/start_nuclemd.sh" + "," 
                          + NuclemdFilesPath + "/nuclemd_v1" + "," 
                          + NuclemdFilesPath + "/v0.1/comd_coll1.inp" + ","
                          + NuclemdFilesPath + "/v0.1/conf_18O_new_1.dat" + ","
                          + NuclemdFilesPath + "/v0.1/conf_ca40_tr0165.dat" ;
                    
                        Arguments = 
                            username + ","
                          + NuclemdFilesPath + "/comd_coll1.inp" + "," 
                          + NuclemdFilesPath + "/conf_18O_new_1.dat" + ","
                          + NuclemdFilesPath + "/conf_ca40_tr0165.dat" + ","
                          + NUCLEMD_Parameters[7] + ","
                          + NUCLEMD_Parameters[8];
                    } else {
                        // Release v0.2
                        InputSandbox = 
                            NuclemdFilesPath + "/start_nuclemd.sh" + "," 
                          + NuclemdFilesPath + "/nuclemd_v2" + "," 
                          + NuclemdFilesPath + "/v0.2/comd_c1c2.inp" + ","
                          + NuclemdFilesPath + "/v0.2/conf_124Sn_c1c2_L72K250_1.dat" + ","
                          + NuclemdFilesPath + "/v0.2/fake.dat";
                    
                        Arguments = 
                            username + ","
                          + NuclemdFilesPath + "/comd_c1c2.inp" + "," 
                          + NuclemdFilesPath + "/conf_124Sn_c1c2_L72K250_1.dat" + ","
                          + NuclemdFilesPath + "/fake.dat" + ","
                          + NUCLEMD_Parameters[7] + ","
                          + NUCLEMD_Parameters[8];
                    }
                } else {
                    if (NUCLEMD_Parameters[8].equals("nuclemd_ver1")) 
                    {
                        // Release v0.1
                        InputSandbox = 
                            NuclemdFilesPath + "/start_nuclemd.sh" + "," 
                          + NuclemdFilesPath + "/nuclemd_v1" + ","
                          + NUCLEMD_Parameters[2] + ","
                          + NUCLEMD_Parameters[3] + ","
                          + NUCLEMD_Parameters[4];
                    
                        Arguments = 
                            username + ","
                          + NUCLEMD_Parameters[2] + "," 
                          + NUCLEMD_Parameters[3] + "," 
                          + NUCLEMD_Parameters[4] + ","
                          + NUCLEMD_Parameters[7] + ","
                          + NUCLEMD_Parameters[8]; 
                    } else {
                        // Release v0.2
                        InputSandbox = 
                            NuclemdFilesPath + "/start_nuclemd.sh" + "," 
                          + NuclemdFilesPath + "/nuclemd_v2" + "," 
                          + NUCLEMD_Parameters[2] + ","
                          + NUCLEMD_Parameters[3] + ","
                          + NuclemdFilesPath + "/v0.2/fake.dat";
                    
                        Arguments = 
                            username + ","
                          + NUCLEMD_Parameters[2] + "," 
                          + NUCLEMD_Parameters[3] + "," 
                          + NuclemdFilesPath + "/v0.2/fake.dat" + ","
                          + NUCLEMD_Parameters[7] + ","
                          + NUCLEMD_Parameters[8];
                    }
                }                                 
                
                // Set the list of Arguments for NUCLEMD
                NuclemdMultiJobSubmission.setArguments(Arguments);
                
                // Set InputSandbox files (string with comma separated list of file names)
                NuclemdMultiJobSubmission.setInputFiles(InputSandbox);                                

                // OutputSandbox (string with comma separated list of file names)
                String README = "output.README";
                String NUCLEMD_LOG = "nuclemd.log";
                String NuclemdFiles="results.tar.gz";

                // Set the OutputSandbox files (string with comma separated list of file names)
                NuclemdMultiJobSubmission
                        .setOutputFiles(NuclemdFiles + "," 
                                        + NUCLEMD_LOG + ","
                                        + README);
                
                // Set the MaxWallClockTime Requirements
                String MaxWallClockTimeRequirements[] = new String[1];
                MaxWallClockTimeRequirements[0] = 
                        "JDLRequirements=(other.GlueCEPolicyMaxWallClockTime>"
                        + NUCLEMD_Parameters[6]
                        + ")";
                
                NuclemdMultiJobSubmission
                        .setJDLRequirements(MaxWallClockTimeRequirements);
                            
                /*String jdlRequirements[] = new String[1];
                jdlRequirements[0] = "JDLRequirements=(other.GlueCEPolicyMaxCPUTime>1440)";
                NuclemdMultiJobSubmission.setJDLRequirements(jdlRequirements);*/
                                                
                // Check if more than one infrastructure have been enabled                
                if (MAX==1) 
                    if (!NUCLEMD_Parameters[1].isEmpty())
                        log.info ("\nSetting the CE = " + NUCLEMD_Parameters[1]);
                        NuclemdMultiJobSubmission.setJobQueue(NUCLEMD_Parameters[1]);

                InetAddress addr = InetAddress.getLocalHost();
                //Company company;
                
                try {
                    company = PortalUtil.getCompany(request);
                    String gateway = company.getName();
                    
                    // Send a notification email to the user if enabled.
                    if (NUCLEMD_Parameters[5]!=null)
                        if ( (SMTP_HOST==null) || 
                             (SMTP_HOST.trim().equals("")) ||
                             (SMTP_HOST.trim().equals("N/A")) ||
                             (SENDER_MAIL==null) || 
                             (SENDER_MAIL.trim().equals("")) ||
                             (SENDER_MAIL.trim().equals("N/A"))
                           )
                        log.info ("\nThe Notification Service is not properly configured!");
                    else {
                                // Setting the user's email for notifications
                                NuclemdMultiJobSubmission.setUserEmail(emailAddress);
                                
                                // Setting the Sender
                                if (!SENDER_MAIL.isEmpty())
                                    NuclemdMultiJobSubmission.setSenderEmail(SENDER_MAIL);
                            
                                sendHTMLEmail(username, 
                                           emailAddress, 
                                           SENDER_MAIL, 
                                           SMTP_HOST, 
                                           "NUCLEMD", 
                                           gateway);
                    }                                
                                    
                    log.info("\n- Submission in progress ...");
                    NuclemdMultiJobSubmission.submitJobAsync(
                            infrastructure,
                            username,
                            addr.getHostAddress()+":8162",
                            Integer.valueOf(nuclemd_APPID),
                            NUCLEMD_Parameters[0]);
                    
                } catch (PortalException ex) {
                    Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SystemException ex) {
                    Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
                }                                    
            } // end PROCESS ACTION [ SUBMIT_NUCLEMD_PORTLET ]
        } catch (PortalException ex) {
            Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SystemException ex) {
            Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response)
                throws PortletException, IOException
    {
        //super.serveResource(request, response);

        PortletPreferences portletPreferences = (PortletPreferences) request.getPreferences();

        final String action = (String) request.getParameter("action");

        if (action.equals("get-ratings")) {
            //Get CE Ratings from the portlet preferences
            String nuclemd_CE = (String) request.getParameter("nuclemd_CE");

            String json = "{ \"avg\":\"" + 
                    portletPreferences.getValue(nuclemd_CE+"_avg", "0.0") +
                    "\", \"cnt\":\"" + portletPreferences.getValue(nuclemd_CE+"_cnt", "0") + "\"}";

            response.setContentType("application/json");
            response.getPortletOutputStream().write( json.getBytes() );

        } else if (action.equals("set-ratings")) {

            String nuclemd_CE = (String) request.getParameter("nuclemd_CE");
            int vote = Integer.parseInt(request.getParameter("vote"));

             double avg = Double.parseDouble(portletPreferences.getValue(nuclemd_CE+"_avg", "0.0"));
             long cnt = Long.parseLong(portletPreferences.getValue(nuclemd_CE+"_cnt", "0"));

             portletPreferences.setValue(nuclemd_CE+"_avg", Double.toString(((avg*cnt)+vote) / (cnt +1)));
             portletPreferences.setValue(nuclemd_CE+"_cnt", Long.toString(cnt+1));

             portletPreferences.store();
        }
    }

    // Upload NUCLEMD input files
    public String[] uploadNuclemdSettings(ActionRequest actionRequest,
                                          ActionResponse actionResponse, String username)
    {
        String[] NUCLEMD_Parameters = new String [9];
        boolean status;

        // Check that we have a file upload request
        boolean isMultipart = PortletFileUpload.isMultipartContent(actionRequest);

        if (isMultipart) {
            // Create a factory for disk-based file items.
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Set factory constrains
            File NUCLEMD_Repository = new File ("/tmp");
            if (!NUCLEMD_Repository.exists()) status = NUCLEMD_Repository.mkdirs();
            factory.setRepository(NUCLEMD_Repository);

            // Create a new file upload handler.
            PortletFileUpload upload = new PortletFileUpload(factory);

            try {
                    // Parse the request
                    List items = upload.parseRequest(actionRequest);
                    // Processing items
                    Iterator iter = items.iterator();

                    while (iter.hasNext())
                    {
                        FileItem item = (FileItem) iter.next();
                        String fieldName = item.getFieldName();
                        
                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                        String timeStamp = dateFormat.format(Calendar.getInstance().getTime());

                        // Processing a regular form field
                        if ( item.isFormField() )
                        {                     
                            if (fieldName.equals("nuclemd_release"))
                                NUCLEMD_Parameters[8]=item.getString();
                            
                            if (fieldName.equals("nuclemd_desc"))
                                if (item.getString().equals("Please, insert here a description"))
                                    NUCLEMD_Parameters[0]="NUCLEMD Simulation Started";
                                else
                                    NUCLEMD_Parameters[0]=item.getString();                            
                                                        
                            if (fieldName.equals("nuclemd_CE"))
                                NUCLEMD_Parameters[1]=item.getString();                                                       
                            
                        } else {
                            // Processing a file upload
                            if (fieldName.equals("nuclemd_fileinp"))
                            {                                                               
                                log.info("\n- Uploading the input file: "
                                       + "\n[ " + item.getName() + " ]"
                                       + "\n[ " + item.getContentType() + " ]"
                                       + "\n[ " + item.getSize() + "KBytes ]"
                                       );                                                                

                                // Writing the file to disk
                                String uploadNuclemdFile = 
                                        NUCLEMD_Repository + "/" 
                                        + timeStamp + "_" 
                                        + username + "_" 
                                        + item.getName();

                                log.info("\n- Writing the file: [ "
                                        + uploadNuclemdFile.toString()
                                        + " ] to disk");

                                item.write(new File(uploadNuclemdFile));
                                
                                // Writing the file to disk
                                String uploadNuclemdFile_stripped = 
                                        NUCLEMD_Repository + "/" 
                                        + timeStamp + "_" 
                                        + username + "_" 
                                        + item.getName() + "_stripped";                                                                
                                
                                NUCLEMD_Parameters[2]=
                                        RemoveCarriageReturn(
                                        uploadNuclemdFile, 
                                        uploadNuclemdFile_stripped);          
                           }
                            
                            if (fieldName.equals("nuclemd_fileconf_1"))
                            {                                                               
                                log.info("\n- Uploading the input file: "
                                       + "\n[ " + item.getName() + " ]"
                                       + "\n[ " + item.getContentType() + " ]"
                                       + "\n[ " + item.getSize() + "KBytes ]"
                                       );                                                                

                                // Writing the file to disk
                                String uploadNuclemdFile = 
                                        NUCLEMD_Repository + "/" 
                                        + timeStamp + "_" 
                                        + username + "_" 
                                        + item.getName();

                                log.info("\n- Writing the file: [ "
                                        + uploadNuclemdFile.toString()
                                        + " ] to disk");

                                item.write(new File(uploadNuclemdFile));
                                
                                // Writing the file to disk
                                String uploadNuclemdFile_stripped = 
                                        NUCLEMD_Repository + "/" 
                                        + timeStamp + "_" 
                                        + username + "_" 
                                        + item.getName() + "_stripped";                                                                
                                
                                NUCLEMD_Parameters[3]=
                                        RemoveCarriageReturn(
                                        uploadNuclemdFile, 
                                        uploadNuclemdFile_stripped);          
                            }
                            
                            if (fieldName.equals("nuclemd_fileconf_2"))
                            {                                                               
                                log.info("\n- Uploading the input file: "
                                       + "\n[ " + item.getName() + " ]"
                                       + "\n[ " + item.getContentType() + " ]"
                                       + "\n[ " + item.getSize() + "KBytes ]"
                                       );                                                                

                                // Writing the file to disk
                                String uploadNuclemdFile = 
                                        NUCLEMD_Repository + "/" 
                                        + timeStamp + "_" 
                                        + username + "_" 
                                        + item.getName();

                                log.info("\n- Writing the file: [ "
                                        + uploadNuclemdFile.toString()
                                        + " ] to disk");

                                item.write(new File(uploadNuclemdFile));
                                
                                // Writing the file to disk
                                String uploadNuclemdFile_stripped = 
                                        NUCLEMD_Repository + "/" 
                                        + timeStamp + "_" 
                                        + username + "_" 
                                        + item.getName() + "_stripped";                                                                
                                
                                NUCLEMD_Parameters[4]=
                                        RemoveCarriageReturn(
                                        uploadNuclemdFile, 
                                        uploadNuclemdFile_stripped);                              
                            }
                        }
                        
                        if (fieldName.equals("EnableNotification"))
                                NUCLEMD_Parameters[5]=item.getString();
                        
                        if (fieldName.equals("nuclemd_maxwallclocktime"))
                                NUCLEMD_Parameters[6]=item.getString();
                        
                        if (fieldName.equals("EnableDemo"))
                                NUCLEMD_Parameters[7]=item.getString();
                                                
                    } // end while
            } catch (FileUploadException ex) {
              Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
              Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return NUCLEMD_Parameters;
    }
    
    // Retrieve a random Computing Element
    // matching the Software Tag for the application    
    public String getRandomCE(String nuclemd_VONAME,
                              String nuclemd_TOPBDII,
                              String nuclemd_SOFTWARE,
                              Integer nuclemd_MaxCPUTime,
                              String selected)
                              throws PortletException, IOException
    {
        String randomCE = null;
        BDII bdii = null;    
        List<String> CEqueues = null;
                        
        String[] SOFTWARE_LIST = nuclemd_SOFTWARE.split(",");
        
        for(String SOFTWARE: SOFTWARE_LIST)
        {
            log.info("\n- Querying the Information System [ "
                      + nuclemd_TOPBDII
                      + " ] and fetching a random CE matching the SW tag [ VO-"
                      + nuclemd_VONAME
                      + "-"
                      + SOFTWARE + " ]");  

            try {               

                bdii = new BDII( new URI(nuclemd_TOPBDII) );               
                
                // Get the list of the available queues
                CEqueues = bdii.queryCEQueues(nuclemd_VONAME);
                                
                // Get the list of the Computing Elements for the given SW tag
                randomCE = bdii.getRandomCEForSWTag("VO-" 
                           + nuclemd_VONAME
                           + "-"
                           + SOFTWARE);
                
                /*randomCE = bdii.getRandomCEFromSWTag_MaxCPUTime(
                        "VO-" + nuclemd_VONAME + "-" + nuclemd_SOFTWARE, 
                        nuclemd_VONAME, 
                        nuclemd_MaxCPUTime);*/
                                    
                /*if (!selected.isEmpty())
                while (flag) {
                    // Fetching the Queues
                    for (String CEqueue:CEqueues) {
                        // Selecting only the "infinite" queue
                            if (CEqueue.contains(selected)) {
                                randomCE=CEqueue;                    
                                flag=false;
                            }                        
                    }
                }*/
                
                // Fetching the Queues
                    for (String CEqueue:CEqueues) {
                        if (CEqueue.contains(randomCE))
                            randomCE=CEqueue;
                    }            

            } catch (URISyntaxException ex) {
                    Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                    Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (!selected.isEmpty()) log.info("\n- Selected the following computing farm =  " + randomCE);
        else log.info("\n- Selected *randomly* the following computing farm =  " + randomCE);
        return randomCE;
    }        

    // Retrieve the list of Computing Elements
    // matching the Software Tag for the NUCLEMD application    
    public List<String> getListofCEForSoftwareTag(String nuclemd_VONAME,
                                                  String nuclemd_TOPBDII,
                                                  String nuclemd_SOFTWARE)
                                throws PortletException, IOException
    {
        List<String> CEs_list = null;
        BDII bdii = null;
        
        log.info("\n- Querying the Information System [ "
                     + nuclemd_TOPBDII
                     + " ] and looking for CEs matching SW tag [ VO-"
                     + nuclemd_VONAME
                     + "-"
                     + nuclemd_SOFTWARE + " ]");  

            try {

                    bdii = new BDII( new URI(nuclemd_TOPBDII) );           
                    // Retrieve a full list of GlueCEInfoHostName for a given VO
                    CEs_list = bdii.getGlueCEInfoHostNames(nuclemd_VONAME);
                    
                    // Retrieve a full list of GlueCEInfoHostName 
                    // publishing a SW tag for a given VO
                    /*CEs_list = bdii.queryCEForSWTag(
                               "VO-"
                               + nuclemd_VONAME
                               + "-"
                               + nuclemd_SOFTWARE);*/

            } catch (URISyntaxException ex) {
                    Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                    Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            }        

        return CEs_list;
    }

    // Get the GPS location of the given grid resource
    public String[] getCECoordinate(RenderRequest request,
                                    String CE)
                                    throws PortletException, IOException
    {
        String[] GPS_locations = null;
        BDII bdii = null;

        PortletPreferences portletPreferences =
                (PortletPreferences) request.getPreferences();

        // Getting the NUCLEMD TOPPBDII from the portlet preferences
        String garuda_nuclemd_TOPBDII = 
                portletPreferences.getValue("garuda_nuclemd_TOPBDII", "N/A");
        String eumed_nuclemd_TOPBDII = 
                portletPreferences.getValue("eumed_nuclemd_TOPBDII", "N/A");
        String sagrid_nuclemd_TOPBDII = 
                portletPreferences.getValue("sagrid_nuclemd_TOPBDII", "N/A");
        String see_nuclemd_TOPBDII = 
                portletPreferences.getValue("see_nuclemd_TOPBDII", "N/A");
        String gridit_nuclemd_TOPBDII = 
                portletPreferences.getValue("gridit_nuclemd_TOPBDII", "N/A");
        
        // Getting the NUCLEMD ENABLEINFRASTRUCTURE from the portlet preferences
        String nuclemd_ENABLEINFRASTRUCTURE = 
                portletPreferences.getValue("nuclemd_ENABLEINFRASTRUCTURE", "N/A");

            try {
                if ( nuclemd_ENABLEINFRASTRUCTURE.equals("garuda") )
                     bdii = new BDII( new URI(garuda_nuclemd_TOPBDII) );

                if ( nuclemd_ENABLEINFRASTRUCTURE.equals("eumed") )
                     bdii = new BDII( new URI(eumed_nuclemd_TOPBDII) );
                
                if ( nuclemd_ENABLEINFRASTRUCTURE.equals("sagridd") )
                     bdii = new BDII( new URI(sagrid_nuclemd_TOPBDII) );

                if ( nuclemd_ENABLEINFRASTRUCTURE.equals("see") )
                    bdii = new BDII( new URI(see_nuclemd_TOPBDII) );
                
                if ( nuclemd_ENABLEINFRASTRUCTURE.equals("gridit") )
                    bdii = new BDII( new URI(gridit_nuclemd_TOPBDII) );

                GPS_locations = bdii.queryCECoordinate("ldap://" + CE + ":2170");

            } catch (URISyntaxException ex) {
                Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            }

            return GPS_locations;
    }
    
    private void sendHTMLEmail (String USERNAME,
                                String TO, 
                                String FROM, 
                                String SMTP_HOST, 
                                String ApplicationAcronym,
                                String GATEWAY)
    {
                
        log.info("\n- Sending email notification to the user " + USERNAME + " [ " + TO + " ]");
        
        log.info("\n- SMTP Server = " + SMTP_HOST);
        log.info("\n- Sender = " + FROM);
        log.info("\n- Receiver = " + TO);
        log.info("\n- Application = " + ApplicationAcronym);
        log.info("\n- Gateway = " + GATEWAY);        
        
        // Assuming you are sending email from localhost
        String HOST = "localhost";
        
        // Get system properties
        Properties properties = System.getProperties();
        properties.setProperty(SMTP_HOST, HOST);
        
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);
        
        try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(FROM));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
         //message.addRecipient(Message.RecipientType.CC, new InternetAddress(FROM));

         // Set Subject: header field
         message.setSubject(" [liferay-sg-gateway] - [ " + GATEWAY + " ] ");

	 Date currentDate = new Date();
	 currentDate.setTime (currentDate.getTime());

         // Send the actual HTML message, as big as you like
         message.setContent(
	 "<br/><H4>" +         
         "<img src=\"http://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/195775_220075701389624_155250493_n.jpg\" width=\"100\">Science Gateway Notification" +
	 "</H4><hr><br/>" +
         "<b>Description:</b> Notification for the application <b>[ " + ApplicationAcronym + " ]</b><br/><br/>" +         
         "<i>The application has been successfully submitted from the [ " + GATEWAY + " ]</i><br/><br/>" +
         "<b>TimeStamp:</b> " + currentDate + "<br/><br/>" +
	 "<b>Disclaimer:</b><br/>" +
	 "<i>This is an automatic message sent by the Science Gateway based on Liferay technology.<br/>" + 
	 "If you did not submit any jobs through the Science Gateway, please " +
         "<a href=\"mailto:" + FROM + "\">contact us</a></i>",
	 "text/html");

         // Send message
         Transport.send(message);         
      } catch (MessagingException ex) { 
          Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);          
      }
    }
    
    public String RemoveCarriageReturn (String InputFileName, String OutputFileName)             
    {
        // Remove the carriage return char from a named file.                                
        FileInputStream fis;
        try {
            
            fis = new FileInputStream(InputFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            
            File fout = new File(OutputFileName);
            FileOutputStream fos = new FileOutputStream(fout);                                
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));
            
            // The pattern matches control characters
            Pattern p = Pattern.compile("\r");
            Matcher m = p.matcher("");
            String aLine = null;

            try {
                while((aLine = in.readLine()) != null) {
                    m.reset(aLine);
                    //Replaces control characters with an empty string.
                    String result = m.replaceAll("");                    
                    out.write(result);
                    out.newLine();
                }
                out.close();                
            } catch (IOException ex) {
                Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Nuclemd.class.getName()).log(Level.SEVERE, null, ex);
        }                                                                                
        
        log.info("\n- Writing the user's stripped file: [ " + 
                  OutputFileName.toString() + " ] to disk");
        
        return OutputFileName;
    }        
}
