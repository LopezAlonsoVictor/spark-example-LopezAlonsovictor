package urjc.isi.pruebasSparkJava;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.HashMap;


public class Injector {

	private static Connection c;

	public Injector(String name) {
		try {		    
		    String dbUrl = System.getenv(name);
		    c = DriverManager.getConnection(dbUrl);

			c.setAutoCommit(false);
		}catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}


	public static Boolean searchTitleId(Integer titleID) {
		String sql = "SELECT titleid FROM movies WHERE titleid = "+ titleID;
		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
			ResultSet rs= pstmt.executeQuery();
			rs.next();
			rs.getInt("titleid");
			return true;
		}catch (SQLException e) {
			return false;
		}
	}
	
	public static Boolean searchNameId(Integer NameID) {
		String sql = "SELECT nameid FROM workers WHERE nameid = "+ NameID;
		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
			ResultSet rs= pstmt.executeQuery();
			rs.next();
			rs.getInt("nameid");
			return true;
		}catch (SQLException e) {
			return false;
		}
	}


	public static void insertFilm(String data1, String data2, String data3){
    	String sql="";
		int random = 0;
		//Comprobar elementos que son distintos que null
    	if(data1 == null || data2 == null){
    		throw new NullPointerException();
    	}
    	random = (int) (Math.random() * 1000)+1; //Ponemos más 1 para que no pueda haber titleid 0
    	while(searchTitleId(random)) {
    		random = (int) (Math.random() * 1000)+1;
    	}
    	sql = "INSERT INTO movies(titleid, title, year, genres) VALUES(?,?,?,?)";
    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {       		
			pstmt.setInt(1, random);		
			pstmt.setString(2, data1);
        	pstmt.setInt(3, Integer.valueOf(data2));
        	pstmt.setString(4, data3);
        	pstmt.executeUpdate();
        	c.commit();
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }

    }

	public static void insertActor(String data1){
    	String sql="";
		Integer random = 0;
		//Comprobar elementos que son distintos que null
    	if(data1 == null){
    		throw new NullPointerException();
    	}
    	random = (int) (Math.random() * 1000)+1; //Ponemos más 1 para que no pueda haber titleid 0
    	while(searchNameId(random)) {
    		random = (int) (Math.random() * 1000)+1;
    	}
    	sql = "INSERT INTO workers(nameid, primary_name) VALUES(?,?)";

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {  
    		pstmt.setInt(1, random);
			pstmt.setString(2, data1);
        	pstmt.executeUpdate();
        	c.commit();
    	} catch (SQLException e) {
    	   	 System.out.println(e.getMessage());
    	}
    }
	

	public List<String> filterByName(String film) {
		String sql = "SELECT * FROM movies WHERE title = "+"'"+film+"'";
		List<String> result = new ArrayList<String>();

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		if (rs.next()) {
        		String id=Integer.toString(rs.getInt("titleid"));
                String title = rs.getString("title");
                String year = Integer.toString(rs.getInt("year"));
                String runtimeMinutes = Integer.toString(rs.getInt("runtime_minutes"));
                String averageRating = (Double.toString(rs.getDouble("average_rating"))).substring(0, 3);
                String numVotes = Integer.toString(rs.getInt("num_votes"));
                String genres = rs.getString("genres");
                result.add(title);
                result.add(year);
                result.add(runtimeMinutes);
                result.add(averageRating);
                result.add(numVotes);
                result.add(genres);
                result.add(id);
    		}
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return result;
	}

	public List<String> filterByYear(String year) {
		String sql = "SELECT * FROM movies WHERE year = "+"'"+year+"'";
		List<String> result = new ArrayList<String>();

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		while(rs.next()) {
                String title = rs.getString("title");
                result.add(title);
            }
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return result;
	}

	public List<String> filterByDuration(String minutes) {
		String sql = "SELECT * FROM movies WHERE runtimeminutes <= "+"'"+minutes+"'";
		List<String> result = new ArrayList<String>();

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		while(rs.next()) {
                String title = rs.getString("title");
                result.add(title);
            }
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return result;
	}

	public List<String> filterByRating(String rating) {
		String sql = "SELECT * FROM movies WHERE averagerating >= "+"'"+rating+"'";
		List<String> result = new ArrayList<String>();

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		while(rs.next()) {
                String title = rs.getString("title");
                result.add(title);
            }
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return result;
	}

	public float meanScores(String film) {
		String sql = "SELECT avg(score) FROM ratings JOIN movies ON movies.titleid = ratings.titleid WHERE movies.title LIKE '"+film +"' GROUP BY ratings.titleid";
    	float result = 0;

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		if (rs.next()) {
    			result = rs.getFloat("avg");
    		}
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return result;
	}
	
		public String[][] userandcomments(String film){
		String sql = "SELECT comment,clientid FROM comments JOIN movies ON movies.titleid = comments.titleid JOIN clients ON clients.clientid=movies.clientid WHERE movies.title LIKE "+"+film+"+" GROUP BY clientid";
		
		String name_col= "clientID";
		String name_col2= "commentID";
		String table = "comments";
		String table2 = "clients";
		Integer total_comment = 0;
		Integer total_clients = 0;
		total_comment = contar(table,name_col);
		total_clients = contar(table2,name_col2);
		String[][] result = new String[total_clients][total_comment];
		Integer aux = 0;
		
		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();

    		while(rs.next()) {
    			aux = rs.getInt("ClientID");
    			for (int i = 0; i< total_comment;i++) {
    				if (result[aux-1][i] == null) {
    					result[aux-1][i] = rs.getString("comment");
    					break;
    				}
    			}
   		}
		} catch (SQLException e) {
			
    		System.out.println(e.getMessage());
    	}
		return result;
	}

	public Integer contar(String name_table,String name_col) {
		String sql = "SELECT COUNT("+name_col+") FROM "+ name_table;
		Integer result = 0;

		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		result = rs.getInt("COUNT("+name_col+")");
    	} catch (SQLException e) {

    		System.out.println(e.getMessage());
    	}
		return result;
	}

	public List<String> getFilmComments(int film){
		String sql = "SELECT clientid, comment FROM comments WHERE titleid="+film;
		
		List<String> result = new ArrayList<String>();
		
		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();

    		while(rs.next()) {
    			String title = Integer.toString(rs.getInt("clientid"))+" : "+rs.getString("comment");
                result.add(title);
   		}
		} catch (SQLException e) {

    		System.out.println(e.getMessage());
    	}
		return result;
	}

	public List<String> filterByGenre(String genre) {
		String sql = "SELECT title FROM movies WHERE genres LIKE "+'"'+"%"+genre+"%"+'"';
		List<String> result = new ArrayList<String>();

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		while(rs.next()) {

                String title = rs.getString("title");
                result.add(title);
            }
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return result;
	}

	public List<String> filterByActorActress(String name) {
		String sql = "SELECT title FROM movies JOIN works_in ON movies.titleid=works_in.titleid ";
		sql+= "JOIN workers ON workers.nameid=works_in.nameid ";
		sql += "WHERE workers.primary_name LIKE "+ "'" + name +"'";
		sql += " and (works_as LIKE 'actor' or works_as LIKE 'actress')";
		sql += " ORDER BY movies.titleid DESC";
		List<String> result = new ArrayList<String>();

    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		c.commit();
    		while(rs.next()) {
                String title = rs.getString("title");
                result.add(title);
            }
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return result;
	}

	public void makeDataHashMap(Map<Integer, Map<Integer, Double>> data) {
		String sql = "SELECT * FROM ratings ORDER BY clientid;";

		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
			Integer titleid = rs.getInt("titleid");
			Integer clientid = rs.getInt("clientid");
			Double score = rs.getDouble("score");

			if (!data.containsKey(clientid)) {
				data.put(clientid, new HashMap<Integer, Double>());
			}

			data.get(clientid).put(titleid, score);
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}


    public Boolean searchRating(Integer titleID, Integer clientID) {
		String sql = "SELECT score FROM ratings WHERE titleid = "+ titleID;
		sql += " and clientid = "+ clientID;
		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
			ResultSet rs= pstmt.executeQuery();
			rs.next();
			rs.getInt("score");
			return true;
		}catch (SQLException e) {
			return false;
		}
	}

    public void insertRating(Integer titleid, Integer clientid, Integer score) {
	String sql= new String();

    	if(searchRating(titleid, clientid)) {
    		sql = "UPDATE ratings SET score=" + score;
    		sql += " WHERE titleid=" + titleid + " and clientid="+ clientid;
    		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
        		pstmt.executeUpdate();
        		c.commit();
        	} catch (SQLException e) {
        		System.out.println(e.getMessage());
        	}
    	}else {
    		sql = "INSERT INTO ratings(titleid, clientid,score) VALUES(?,?,?)";
    		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
        		pstmt.setInt(1, titleid);
        		pstmt.setInt(2, clientid);
        		pstmt.setInt(3, score);
        		pstmt.executeUpdate();
        		c.commit();
        	} catch (SQLException e) {
        		System.out.println(e.getMessage());
        	}
    	}
    }

    public Boolean searchUser(Integer clientID) {
		String sql = "SELECT clientID FROM clients WHERE clientid = "+ clientID;
		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
			ResultSet rs= pstmt.executeQuery();
			rs.next();
			rs.getInt("clientID");
			return true;
		}catch (SQLException e) {
			return false;
		}
	}

	public void insertUser(Integer clientid) {
		String sql= new String();

    	if(!searchUser(clientid) ){
    		sql = "INSERT INTO clients(clientID) VALUES("+clientid+")";
    		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
        		pstmt.executeUpdate();
        		c.commit();
        	} catch (SQLException e) {
        		System.out.println(e.getMessage());
        	}
    	}
    }


//titleid, clientID y comment
//NOMBRE TABLA: comments(Hay que crearla)
    public void insertComments(Integer titleid, Integer clientid, String comment) {
    	String sql = "SELECT MAX(\"commentId\") FROM comments";
    	try (PreparedStatement pstmt = c.prepareStatement(sql)) {   
    		ResultSet rs = pstmt.executeQuery();
    		if(rs.next()){
    			int lastId = rs.getInt("max");
    			System.out.println(lastId);
    	
    			sql= "INSERT INTO comments(\"commentId\",titleid, clientid, comment) VALUES(?,?,?,?)";
    			try (PreparedStatement pstmt2 = c.prepareStatement(sql)) {
		   			pstmt2.setInt(1, lastId+1);			
		   			pstmt2.setInt(2, titleid);
		   	    	pstmt2.setInt(3, clientid);
		   	    	pstmt2.setString(4, comment);
		   	    	pstmt2.executeUpdate();
		   	    	c.commit();
    			} catch (SQLException e) {
            		System.out.println(e.getMessage());
            	}	
		   	    
    		}
    	} catch (SQLException e) {
        		System.out.println(e.getMessage());
        }
   	}

    public void updateAverageRating(Integer titleID, Float averageRating) {
		String sql = "UPDATE movies SET average_rating = " + averageRating; 
		sql += " WHERE titleid = " + titleID;
		try (PreparedStatement pstmt = c.prepareStatement(sql)) {
    		pstmt.executeUpdate();
    		c.commit();
    	} catch (SQLException e) {    		
    		System.out.println(e.getMessage());
    	}
		
	}

	public void close() {
        try {
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
