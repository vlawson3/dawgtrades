package edu.uga.dawgtrades.persistence.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.mysql.jdbc.PreparedStatement;

import edu.uga.dawgtrades.model.DTException;
import edu.uga.dawgtrades.model.MembershipImpl;


class membershipManager
{
    private ObjectModel objectModel = null;
    private Connection  conn = null;
    
    public MembershipManager( Connection conn, ObjectModel objectModel )
    {
        this.conn = conn;
        this.objectModel = objectModel;
    }
    
    public void save( Membership membership ) 
            throws DTException
    {
        String               insertMembershipSql = "insert into membership (fee, fee_date) values ( ?, ? )";              
        String               updateMembershipSql = "update membership set fee = ?, fee_date = ? ";              
        PreparedStatement    stmt;
        int                  inscnt;
        long                 membershipId;
        
        try {
            
            if( !membership.isPersistent() )
                stmt = (PreparedStatement) conn.prepareStatement( insertMembershipSql );
            else
                stmt = (PreparedStatement) conn.prepareStatement( updateMembershipSql );

            if( membership.getPrice() != null )
                stmt.setString( 1, membership.getPrice() );
            else
                throw new DTException( "MembershipManager.save: can't save a membership: price undefined" );
				
            if( membership.getDate() != null )
                stmt.setString( 2, membership.getDate() );
            else
                throw new DTException( "MembershipManager.save: can't save a membership: date undefined");	
										
            inscnt = stmt.executeUpdate();

            if( !membership.isPersistent() ) {
                // in case this this object is stored for the first time,
                // we need to establish its persistent identifier (primary key)
                if( inscnt == 1 ) {
                    String sql = "select last_insert_id()";
                    if( stmt.execute( sql ) ) { // statement returned a result
                        // retrieve the result
                        ResultSet r = stmt.getResultSet();
                        // we will use only the first row!
                        while( r.next() ) {
                            // retrieve the last insert auto_increment value
                            membershipId = r.getLong( 1 );
                            if( membershipId > 0 )
                                membership.setId( membershipId ); // set this membership's db id (proxy object)
                        }
                    }
                }
            }
            else {
                if( inscnt < 1 )
                    throw new DTException( "MembershipManager.save: failed to save a membership" ); 
            }
        }
        catch( SQLException e ) {
            e.printStackTrace();
            throw new DTException( "MembershipManager.save: failed to save a membership: " + e );
        }
    }

    public Iterator<membership> restore( membership modelMembership ) 
            throws DTException
    {
        String       selectMembershipSql = "select fee, fee_date 	from membership"; 
        Statement    stmt = null;
        StringBuffer query = new StringBuffer( 100 );
        StringBuffer condition = new StringBuffer( 100 );

        condition.setLength( 0 );
        
        // form the query based on the given membership object instance
        query.append( selectMembershipSql );
        
        if( modelMembership != null ) {
            if( modelMembership.getId() >= 0 ) // id is unique, so it is sufficient to get a membership
                query.append( " where membership_fee_id = " + modelMembership.getId() );
            else {
                if( modelMembership.getPrice() != null ) {
                    if( condition.length() > 0 )
                        condition.append( " and" );
                    condition.append( " fee = '" + modelMembership.getPrice() + "'" );
                }
				
                if( modelMembership.getDate() != null ) {
                    if( condition.length() > 0 )
                        condition.append( " and" );
                    condition.append( " fee_date = '" + modelMembership.getDate() + "'" );
                }				               				
					
                if( condition.length() > 0 ) {
                    query.append(  " where " );
                    query.append( condition );
                }
            }
        }
        
        try {

            stmt = conn.createStatement();

            // retrieve the persistent membership object
            //
            if( stmt.execute( query.toString() ) ) { // statement returned a result
                ResultSet r = stmt.getResultSet();
                return new MembershipIterator( r, objectModel );
            }
        }
        catch( Exception e ) {      // just in case...
            throw new DTException( "membershipManager.restore: Could not restore persistent membership object; Root cause: " + e );
        }
        
        // if we get to this point, it's an error
        throw new DTException( "membershipManager.restore: Could not restore persistent membership object" );
    }
    
    public void delete( Membership membership ) 
            throws DTException
    {
        String               deleteMembershipSql = "delete from membership where membership_fee_id = ?";              
        PreparedStatement    stmt = null;
        int                  inscnt;
        
        // form the query based on the given membership object instance
        if( !membership.isPersistent() ) // is the membership object persistent?  If not, nothing to actually delete
            return;
        
        try {
            
            //DELETE t1, t2 FROM t1, t2 WHERE t1.id = t2.id;
            //DELETE FROM t1, t2 USING t1, t2 WHERE t1.id = t2.id;
            stmt = (PreparedStatement) conn.prepareStatement( deleteMembershipSql );
            
            stmt.setLong( 1, membership.getId() );
            
            inscnt = stmt.executeUpdate();
            
            if( inscnt == 0 ) {
                throw new DTException( "MembershipManager.delete: failed to delete this membership" );
            }
        }
        catch( SQLException e ) {
            throw new DTException( "MembershipManager.delete: failed to delete this Membership: " + e.getMessage() );
        }
    }
}