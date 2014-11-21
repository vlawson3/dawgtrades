// Gnu Emacs C++ mode:  -*- Java -*-
//
// Class:	RegisteredUserIteratorImpl
//
// Vic Lawson
//
//
//

package edu.uga.dawgtrades.persist.impl;



import java.sql.ResultSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.uga.dawgtrades.model.DTException;
import edu.uga.dawgtrades.model.ObjectModel;
import edu.uga.dawgtrades.model.RegisteredUser;


public class RegisteredUserIterator 
    implements Iterator<RegisteredUser>
{
    private ResultSet   rs = null;
    private ObjectModel objectModel = null;
    private boolean     more = false;

    // these two will be used to create a new object
    //
    public RegisteredUserIterator( ResultSet rs, ObjectModel objectModel )
            throws DTException
    { 
        this.rs = rs;
        this.objectModel = objectModel;
        try {
            more = rs.next();
        }
        catch( Exception e ) {	// just in case...
            throw new DTException( "RegisteredUserIterator: Cannot create user iterator; root cause: " + e );
        }
    }

    public boolean hasNext() 
    { 
        return more; 
    }

    public RegisteredUser next() 
    {
    	long id;
    	String name;
    	String firstName;
    	String lastName;
    	String password;
    	boolean isAdmin;
    	String email;
    	String phone;
    	boolean canText;

        if( more ) {

            try {
		
                lastName = rs.getString( 1 );
                firstName = rs.getString( 2 );
                phone = rs.getString( 3 );
                email = rs.getString( 4 );
                name = rs.getString( 5 );
                password = rs.getString( 6 );
                isAdmin = rs.getBoolean( 7 );
                canText = rs.getBoolean( 8 );
                id = rs.getLong( 9 );
                
                more = rs.next();
            }
            catch( Exception e ) {	// just in case...
                throw new NoSuchElementException( "RegisteredUserIterator: No next User object; root cause: " + e );
            }
            
            RegisteredUser user = null;
			try {
				user = objectModel.createRegisteredUser(name, firstName, lastName, password, isAdmin, email, phone, canText);
			} catch (DTException e) {
				
				e.printStackTrace();
			}
            user.setId( id );
            
            return user;
        }
        else {
            throw new NoSuchElementException( "UserIterator: No next User object" );
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

};
