package edu.uga.dawgtrades.boundary;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.uga.dawgtrades.authentication.Session;
import edu.uga.dawgtrades.authentication.SessionManager;
import edu.uga.dawgtrades.logic.Logic;
import edu.uga.dawgtrades.logic.impl.LogicImpl;
import edu.uga.dawgtrades.model.Auction;
import edu.uga.dawgtrades.model.Bid;
import edu.uga.dawgtrades.model.Category;
import edu.uga.dawgtrades.model.DTException;
import edu.uga.dawgtrades.model.ExperienceReport;
import edu.uga.dawgtrades.model.ObjectModel;
import edu.uga.dawgtrades.model.RegisteredUser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * Servlet implementation class AuctionItem_Result
 * @author Justin
 */

public class AuctionItemResult extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    static  String  templateDir = "WEB-INF/templates";
    static  String  resultTemplateName = "AuctionItem-Result.ftl";

    private Configuration  cfg; 


    public void init() {
	
	cfg = new Configuration();
    cfg.setServletContextForTemplateLoading(
            getServletContext(), 
            "WEB-INF/templates"
            );

    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		 /**
	     * @see HttpServlet#HttpServlet()
	     */
	    	Template       resultTemplate = null;
	        HttpSession    httpSession = null;
	        BufferedWriter toClient = null;
	        String categoryName;
	        String identifier;
	        String description;
	        String name;
	        long itemId;
	        long auctionId = 0;
	    	float minPrice;
	        String         ssid = null;
	        Session        session = null;
	        ObjectModel    objectModel = null;
	        Logic          logic = null;
	 
	        // get the ftl template
	        try {
	            resultTemplate = cfg.getTemplate( resultTemplateName );
	        } 
	        catch (IOException e) {
	            throw new ServletException( "Register.doPost: Can't load template in: " + templateDir + ": " + e.toString());
	        }
	        

	        httpSession = req.getSession();
	        if( httpSession == null ) {       // assume not logged in!
	            DTError.error( cfg, toClient, "Session expired or illegal; please log in" );
	            return;
	        }

	        ssid = (String) httpSession.getAttribute( "ssid" );
	        if( ssid == null ) {       // not logged in!
	            DTError.error( cfg, toClient, "Session expired or illegal; please log in" );
	            return;
	        }

	        session = SessionManager.getSessionById( ssid );
	        objectModel = session.getObjectModel();
	        if( objectModel == null ) {
	            DTError.error( cfg, toClient, "Session expired or illegal; please log in" );
	            return;
	        }
	        RegisteredUser user = session.getUser();
	        if( user == null ) {
	            DTError.error( cfg, toClient, "Session expired or illegal; please log in" );
	            return;    
	        } 
	       // Prepare the HTTP response:
	        // - Use the charset of template for the output
	        // - Use text/html MIME-type
	        //
	        toClient = new BufferedWriter(
	                new OutputStreamWriter( res.getOutputStream(), resultTemplate.getEncoding() )
	                );

	        res.setContentType("text/html; charset=" + resultTemplate.getEncoding());
	         
	        logic = new LogicImpl( objectModel );
	       
	        name = req.getParameter("name");
	        categoryName = req.getParameter("category");
	        identifier = req.getParameter("identifier");
	        description = req.getParameter("description");
	    	minPrice = Float.parseFloat(req.getParameter("minprice"));
	    	
	    	//TODO: ERROR CHECKING
	    	
	    	Iterator<Category> catIter = null;
	    	Category modelCategory = objectModel.createCategory();
	    	modelCategory.setName(categoryName);
			try {
				catIter = objectModel.findCategory(modelCategory);
			} catch (DTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	while (catIter.hasNext()) {
	    		modelCategory = catIter.next();
	    	}
	  
	    	try {
	            itemId = logic.createItem(modelCategory.getId(), user.getId(), identifier, name, description);
	            auctionId = logic.createAuction(itemId, minPrice);
	        }
	        catch ( Exception e ) {
	            DTError.error( cfg, toClient, e );
	            return;
	        }
	    	
	    	// Setup the data-model
	        //
	        Map<String,Object> root = new HashMap<String,Object>();

	        // Build the data-model
	        //
	        root.put( "auction_name", name );
	        root.put( "auction_id", auctionId );

	        // Merge the data-model and the template
	        //
	        try {
	            resultTemplate.process( root, toClient );
	            toClient.flush();
	        }
	        catch (TemplateException e) {
	            throw new ServletException( "Error while processing FreeMarker template", e);
	        }

	        toClient.close(); 

	    	
	}

}
