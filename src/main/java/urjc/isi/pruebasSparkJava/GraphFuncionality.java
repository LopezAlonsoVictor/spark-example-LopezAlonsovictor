package urjc.isi.pruebasSparkJava;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.servlet.MultipartConfigElement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class GraphFuncionality {
	
	/**
     * Calcula la distancia entre actores o pelicuas.
     * @param graph sobre el que calcular la distancia
     * @param name1 para buscar y comparar
     * @param name2 para buscar y comparar
     * @return String con la distancia y la ruta, u otro string en caso de error o 'no ruta'.
     */
    public static String doDistance(Graph graph, String name1, String name2) {
    	if (graph.E() == 0) throw new NullPointerException("GraphFuncionality.doDistance");
    	
    	String result = new String("");
    	ArrayList<String> allNames = new ArrayList<String>();
    	String noMatch = "<p>Ninguna coincidencia. Error al introducir nombre," +
    					"o no existe en nuestra BD</p><br>";
    	try {
	    	if (name1.equals("") || name2.equals("")){ //caso de no introducir nada
	    		throw new IllegalArgumentException("GraphFuncionality.doDistance");
	    	}else if (!graph.hasVertex(name1) && !graph.hasVertex(name2)){ //caso 2 erroneos
	    		allNames = nameChecker(Main.getConnection(),name1);
	    		result = "<p>Campo 1: </p>";
	    		if (!allNames.isEmpty()) { 
	    			result += "<p>Múltiples coincidencias. Copia nombre exacto para cálculo de distancia:</p><ul>"; 
	    			for(String aux: allNames) {
	    				result += "<li>" + aux + "</li>"; 
	    			}
	    			result += "</ul>";
	    		}else {
	    			result += noMatch;
	    		}
	    		
	    		allNames.clear(); //limpiamos por si acaso
	    		allNames = nameChecker(Main.getConnection(),name2);
	    		result += "<p>Campo 2: </p>"; 
	    		if (!allNames.isEmpty()) {
	    			result += "<p>Múltiples coincidencias. Copia nombre exacto para cálculo de distancia:</p><ul>"; 
	    			for(String aux: allNames) {
	    				result += "<li>" + aux + "</li>"; 
	    			}
	    			result += "</ul>";
	    		}else {
	    			result += noMatch;
	    		}
	    		
	    	}else if (!graph.hasVertex(name1)){ //no coincidencia nombre1
	    		allNames = nameChecker(Main.getConnection(),name1); //Control de nombres
	    		if (!allNames.isEmpty()) {
	    			result = "<p>Campo 1: </p>" + 
	    					"<p>Múltiples coincidencias. Copia nombre exacto para cálculo de distancia:</p><ul>"; 
	    			for(String aux: allNames) {
	    				result += "<li>" + aux + "</li>"; 
	    			}
	    			result += "</ul>";
	    		}else { //no coincidencia
	    			result += noMatch;
	    		}
	    	}else if (!graph.hasVertex(name2)){ //no coincidencia nombre2
	    		allNames = nameChecker(Main.getConnection(),name2);
	    		if (!allNames.isEmpty()) {
	    			result = "<p>Campo 2: </p>" + 
	    					"<p>Múltiples coincidencias. Copia nombre exacto para cálculo de distancia:</p><ul>"; 
	    			for(String aux: allNames) {
	    				result += "<li>" + aux + "</li>"; 
	    			}
	    			result += "</ul>";
	    		}else {
	    			result += noMatch;
	    		}
			}else{
				PathFinder pf = new PathFinder(graph, name1);
				if (pf.hasPathTo(name2)) { //si tenemos ruta, procedemos	
					String edge = " --> ";
					for (String v : pf.pathTo(name2)) {
						result += v + edge;
					}       
					result = result.substring(0, result.length() - edge.length());
					result += "<br><br>Distancia = " + pf.distanceTo(name2);
				} else {
					result = "<p>Ninguna ruta disponible entre " + name1 + " y " + name2 + ".</p>";
				}
			}
    	}catch(IllegalArgumentException e) {
    		result ="<p>ERROR. Ver 'uso'. Por favor, inténtalo de nuevo.</p>";
    	}
		return result;
	}
    
    /**
     * Comprueba si se han introducido nombres incorrectos/incompletos 
     * (p.e. 'Tom', en vez de 'Tom Cruise', o no coincidente como 'abcd').
     * @param name para buscar y comparar.
     * @return ArrayList con nombres coincidentes con parte de los strings dados 
     * (p.e. devuelve todos los 'Tom' de la tabla, en el caso de haber introducido 'Tom'
     * en vez de 'Tom Cruise'). Devuelve ArrayList vacío en caso de no coincidencia.
     */
    public static ArrayList<String> nameChecker(Connection conn, String name){
    	if (name.equals("")) throw new IllegalArgumentException("GraphFuncionality.nameChecker.names"); //por si acaso (ver Tests)
    	
    	String sql = "SELECT title FROM movies " + //consulta para nombre_pelis
    			"WHERE title LIKE ?";
    	String sql2 = "SELECT primaryName FROM workers " + //consulta para nombre_actores
    			"WHERE primaryName LIKE ?";
    	
    	ArrayList<String> result = new ArrayList<String>();
    	try {	
    		if (conn.isClosed()) throw new IllegalArgumentException("GraphFuncionality.nameChecker.connection"); //comprobar si connection dada sigue abierta
	    	PreparedStatement pstmt = conn.prepareStatement(sql);
	    	pstmt.setString(1, "%" + name + "%");
	    	PreparedStatement pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setString(1, "%" + name + "%");
			
	    	ResultSet rs = pstmt.executeQuery();
	    	if(rs.next()) { //pelis
	    		do{
	    			result.add(rs.getString(1)); //columna 1
	    		}while(rs.next());
	    	}
	    			
	    	ResultSet rs2 = pstmt2.executeQuery(); //ejecutamos aqui la segunda query, para no matar la primera
	    	if (rs2.next()) { //actores
	        	do{
	        		result.add(rs2.getString(1));
	        	}while(rs2.next()); 
	    	}
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}	
    	return result;
    }
    
    
    /**
     * Devuelve los vecinos dado un name (nodo). Uso de IndexGraph.java.
     * @param graph sobre el que hallar vecinos
     * @param name sobre el que hallar vecinos
     * @return String con sus vecinos, u otro string en caso de error o 'no vecinos'.
     */
    public static String doGraphFilter(Graph graph, String name) {
    	if (graph.E() == 0) throw new NullPointerException("GraphFuncionality.doGraphFilter");
    	
    	String result = new String("");
    	ArrayList<String> allNames = new ArrayList<String>();
    	String noMatch = "<p>Ninguna coincidencia. Error al introducir nombre," +
    					"o no existe en nuestra BD</p><br>";
    	try {
	    	if (name.equals("")){ //caso de no introducir nada
	    		throw new IllegalArgumentException("GraphFuncionality.doGraphFilter");
	    	}else if (!graph.hasVertex(name)){ //no coincidencia nombre
	    		allNames = nameChecker(Main.getConnection(),name);
	    		if (!allNames.isEmpty()) {
	    			result =  "<p>Múltiples coincidencias. Copia nombre exacto en el formulario:</p><ul>"; 
	    			for(String aux: allNames) {
	    				result += "<li>" + aux + "</li>"; 
	    			}
	    			result += "</ul>";
	    		}else { //no coincidencia
	    			result += noMatch;
	    		}
			}else{
		    	for (String w : graph.adjacentTo(name)){
		    		result += "<li>" + w + "</li>";
		        }
			}
    	}catch(IllegalArgumentException e) {
    		result ="<p>ERROR. Ver 'uso'. Por favor, inténtalo de nuevo.</p>";
    	}
		return result;
    }
    

    /**
     * Ranking de los actores/actrices con más aparaciones en x o más películas.
     * @param graph sobre el que calcular la distancia
     * @param number actores con x películas o más
     * @return Iterable con nombre de actor y número de películas realizadas.
     */
    public static Iterable<String> doRanking(Graph graph, String number) {
    	ST<String, Integer> result = new ST<String, Integer>();
    	if (number.equals("")) throw new IllegalArgumentException("GraphFuncionality.doRanking.number"); //por si acaso (ver Tests)
    	
    	int numberAux = Integer.parseInt(number);
    	for (String v : graph.vertices()) {
    		if (graph.type(v) == 0) { //actores
    			if(graph.degree(v) >= numberAux) result.put(v, graph.degree(v));
            }
        }
    	return result.keys();
    }
    
    
    /**
     * Devuelve nombres de películas que contengan una cadena de caracteres dada.
     * @param conn es el objeto de tipo Connection.
     * @param movie_name es la cadena de caracteres correspondiente al nombre de la película.
     * @return ArrayList de nombres de películas que contengan la cadena de caracteres.
     */
	public static ArrayList<String> nameCheckerMovie(Connection conn, String movie_name){    	
    	String sql = "SELECT title FROM movies " + //Query para sacar nombres de películas.
    			"WHERE title LIKE ?";
    	
    	ArrayList<String> result = new ArrayList<String>();
    	try {	
	    	PreparedStatement pstmt = conn.prepareStatement(sql);
	    	pstmt.setString(1, "%" + movie_name + "%");
			
	    	ResultSet rs = pstmt.executeQuery();
	    	if(rs.next()) { //La primera llamada a next situa el cursor en la primera fila de resultados.
	    		do{
	    			result.add(rs.getString(1)); //getString(1) para acceder a la columna 1 de la fila por la que se vaya iterando.
	    		}while(rs.next());
	    	}
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}	
    	return result;
    }
    
    //DEPRECATED
    /**
     * Dado el grafo y el nombre de una película, devuelve un ArrayList de películas cercanas a ella en el grafo.
     * @param graph es el grafo.
     * @param movie es el nombre de la película sobre la que se calcularán distancias relativas.
     * @return ArrayList de películas cercanas en el grafo.
     */
    public static ArrayList<String> relatedMovies(Graph graph, String movie) {
    	if (graph.V() == 0) throw new NullPointerException("GraphFuncionality.relatedMovies"); //Compruebo que grafo no vacío.
    	
    	ArrayList<String> rel_movies = new ArrayList<String>(); //Aquí guardaré sólo las películas que estén a distancia 2 de movie.
    	
    	//Compruebo que nombre introducido es de película.
    	if (graph.type(movie) != 1) throw new IllegalArgumentException("GraphFuncionality.relatedMovies"); //Compruebo que se ha introducido nombre de película.
    	
    	PathFinder pf = new PathFinder(graph, movie); //Hago pathFinder con movie como source vertex (Se calculan las ST prev y dist).
    	
    	for (String aux : pf.dameKeys()) { //Hallo todos los vértices de la ST dist de PathFinder.java
    		int distance = pf.distanceTo(aux); //Calculo la distancia desde source hasta vertice aux.
    		if (distance == 2) rel_movies.add(aux); //Solo almaceno si esa distancia es 2.
    	}
    	
    	return rel_movies;
    }
    
    /**
     * Dado el grafo y el nombre de una película, si es vértice, devuelve un String con la lista 
     * de películas cercanas a ella en el grafo. Si no es vértice, devuelve una lista de películas
     * propuestas que contienen dicha cadena de caracteres.
     * @param graph es el grafo.
     * @param movie es el nombre de la película sobre la que se calcularán distancias relativas.
     * @return String con lista de películas.
     */
    public static String relatedMovies2(Graph graph, String movie) {
    	if (graph.V() == 0) throw new NullPointerException("GraphFuncionality.relatedMovies2"); //Compruebo que grafo no vacío.
    	
    	String resultado = new String("");
    	ArrayList<String> rel_movies = new ArrayList<String>(); //Aquí guardaré sólo las películas que estén a distancia 2 de movie.
    	
    	if (graph.hasVertex(movie)) { //Si nombre es un vértice del grafo.
        	if (graph.type(movie) != 1) throw new IllegalArgumentException("GraphFuncionality.relatedMovies2"); //Compruebo que se ha introducido nombre de película.
        	
        	PathFinder pf = new PathFinder(graph, movie); //Hago pathFinder con movie como source vertex (Se calculan las ST prev y dist).
	    	
	    	for (String aux : pf.dameKeys()) { //Hallo todos los vértices de la ST dist de PathFinder.java
	    		int distance = pf.distanceTo(aux); //Calculo la distancia desde source hasta vertice aux.
	    		if (distance == 2) rel_movies.add(aux); //Solo almaceno en rel_movies si esa distancia es 2.
	    	}
	    	
	    	resultado += show_items(rel_movies);
    	
    	
    	}else { //Si nombre NO es un vértice del grafo.
    		ArrayList<String> allNames = nameCheckerMovie(Main.getConnection(), movie);
			if (!allNames.isEmpty()) {
    			resultado += "<p>Múltiples coincidencias. Copia el nombre exacto de una película para que busque relacionados:</p>";
    			resultado += show_items(allNames);
    			
			} else {
				resultado += "<p>No hay coincidencias.</p>";
			}
    	}
    	return resultado;	
    }
    
    
    /**
     * Dado un ArrayList<String> de items, devuelve un String con una lista HTML de dichos items.
     * @param related_items es el ArrayList de items.
     * @return String con la lista HTML de los items.
     */
    public static String show_items(ArrayList<String> items) {
    	String result;
    	if (items.isEmpty()) { //ArrayList vacío.
    		result = "<p>Error al introducir nombre. Dicho nombre, ni nada relacionado a él, se encuentra en nuestra BD.</p><br>";
    	} else {
    		result =  "<ul>"; 
    		for(String aux: items) {
    			result += "<li>" + aux + "</li>"; 
    		}
    		result += "</ul>";
    	}
		return result;
    }
    
    
    /**
     * Devuelve nombres de actrices y actores que contengan una cadena de caracteres dada.
     * @param conn es el objeto de tipo Connection.
     * @param movie_name es la cadena de caracteres correspondiente al nombre de la actriz/actor.
     * @return ArrayList de nombres de actrices/actores que contengan la cadena de caracteres.
     */
	public static ArrayList<String> nameCheckerActor(Connection conn, String actor_name){    	
    	String sql = "SELECT primaryName FROM workers " + //consulta para nombres de actrices/actores.
    			"WHERE primaryName LIKE ?";
    	
    	ArrayList<String> result = new ArrayList<String>();
    	try {	
	    	PreparedStatement pstmt = conn.prepareStatement(sql);
	    	pstmt.setString(1, "%" + actor_name + "%");
			
	    	ResultSet rs = pstmt.executeQuery();
	    	if(rs.next()) { //La primera llamada a next situa el cursor en la primera fila de resultados.
	    		do{
	    			result.add(rs.getString(1)); //getString(1) para acceder a la columna 1 de la fila por la que se vaya iterando.
	    		}while(rs.next());
	    	}
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}	
    	return result;
    }
    
    
    /**
     * Dado el grafo y el nombre de una actriz o actor, si es vértice, devuelve un String con la lista 
     * de actrices y actores cercanos en el grafo. Si no es vértice, devuelve una lista de actrices y actores
     * propuestos que contienen dicha cadena de caracteres.
     * @param graph es el grafo.
     * @param actor es el nombre de la actriz/actor sobre el que se calcularán distancias relativas.
     * @return ArrayList de actrices/actores cercanos en el grafo.
     */
    public static String relatedActors(Graph graph, String actor) {
    	if (graph.V() == 0) throw new NullPointerException("GraphFuncionality.relatedActors"); //Compruebo que grafo no vacío.
    	
    	String resultado = new String("");
    	ArrayList<String> rel_actors = new ArrayList<String>(); //Aquí guardaré sólo las actrices/actores que estén a distancia 2 de actor.
    	
    	if (graph.hasVertex(actor)) { //Si nombre es un vértice del grafo.
        	if (graph.type(actor) != 0) throw new IllegalArgumentException("GraphFuncionality.relatedActors"); //Compruebo que se ha introducido nombre de actriz/actor.
        	
        	PathFinder pf = new PathFinder(graph, actor); //Hago pathFinder con actor como source vertex (Se calculan las ST prev y dist).
	    	
	    	for (String aux : pf.dameKeys()) { //Hallo todos los vértices de la ST dist de PathFinder.java
	    		int distance = pf.distanceTo(aux); //Calculo la distancia desde source hasta vertice aux.
	    		if (distance == 2) rel_actors.add(aux); //Solo almaceno en rel_actors si esa distancia es 2.
	    	}
	    	
	    	resultado += show_items(rel_actors);
    	
    	
    	}else { //Si nombre NO es un vértice del grafo.
    		ArrayList<String> allNames = nameCheckerActor(Main.getConnection(), actor);
			if (!allNames.isEmpty()) {
    			resultado += "<p>Múltiples coincidencias. Copia el nombre exacto de una actriz o actor para que busque relacionados:</p>";
    			resultado += show_items(allNames);
    			
			} else {
				resultado += "<p>No hay coincidencias.</p>";
			}
    	}
    	return resultado;	
    }
    
    
}
