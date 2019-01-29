package urjc.isi.pruebasSparkJava;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import javax.servlet.MultipartConfigElement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Main {
  
    public static String doWork(Request request, Response response) throws ClassNotFoundException, URISyntaxException {
	String result = new String("Hello World");

	return result;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        port(getHerokuAssignedPort());
		Injector connector = new Injector("JDBC_DATABASE_URL");
		Score score =new Score();
    	Comment comment =new Comment();
		SlopeOneFilter slopeOneFilter = new SlopeOneFilter(connector);

        // spark server
        String home = "<html><body>" +
    		"<h1>Bienvenidos a la web de películas</h1>" +
    			"<form action='/addfilms' method='get'>" +
    				"<div class='button'>" +
    					"Añade película: <br/>" +
    					"<button type='submit'>Add Films</button>" +
    				"</div>" +
    			"</form>" +
			"<form action='/showlastadded' method='get'>" +
				"<div class='button'>" +
					"Últimas películas añadidas: <br/>" +
					"<button type='submit'>Show Last Added</button>" +
				"</div>" +
			"</form>" +
    			"<a href='/filter'>Búsqueda de películas</a>" +
    			"<br><br>" +
    			"<a href='/recommend'>Recomendar peliculas a un usuario</a>" +
    			"<br><br>" +
    			"<p>Grafos:</p>" +
    			"<ul>" + 
					"<li><a href= '/distance'>Distancia entre actores y películas<a/></li>" +
					"<li><a href= '/graph_info'>Información sobre el grafo<a/></li>" +
					"<li><a href= '/graph_filter'>Uso de grafos para filtrado<a/></li>" +
				"</ul>" + 
				
				"<form action='/relatedMovies' method='get'>" +
				"<div class='button'>" +
					"Búsqueda de películas relacionadas: <br/>" +
					"<button type='submit'>Related Movies</button>" +
				"</div>" +
				"</form>" +
				
				"<form action='/relatedActors' method='get'>" +
				"<div class='button'>" +
					"Búsqueda de actores relacionados: <br/>" +
				"<button type='submit'>Related Actors</button>" +
				"</div>" +
				"</form>" +	
    		
    		"</body></html>";

        // spark server
        get("/", (req, res) -> home);
        post("/score",(req, res)-> score.postScore(req, connector,slopeOneFilter));
        post("/comment",(req, res)-> comment.postComment(req, connector));

        get("/addfilms", (req, res) ->
    		"<div style='color:#1A318C'><b>PÁGINA PARA AÑADIR PELÍCULA A LA BASE DE DATOS:</b>"
    		+"<form action='/add_films' method='post'>" +
    		"<label for='film'>Película que desea añadir: </label>" + //required: campo obligatorio
    		"<input type='text' required name='film' id='film'" +
    		"pattern=[A-Za-z0-9 ].{1,}>" + //Excluimos todos lo que no sean letras y numeros, y no se puede dejar el campo vacio.
    		"<p></p>"+
    		"<form action='/add_films' method='post'>" +
    		"<label for='year'>Año: </label>" + 
    		"<input type='text' required name='year' id='year'" + //required: Campo obligatorio
    		"pattern=[0-9]{4}>" + //Solo se pueden introducir 4 cifras
    		"<p></p>"+
    		"<form action='/add_films' method='post'>" +
    		"<label for='genres'>Género: </label>" + 
    		"<input type='text' name='genres' id='genres'" +
    		"pattern=[A-Za-z]{0,}>" +
    		"<p></p>"+
    		"<form action='/add_films' method='post'>" +
    		"<label for='actor'>Actor: </label>" + 
    		"<input type='text' name='actor' id='actor'" +
    		"pattern=[A-Za-z ]{0,}>" +
    		"<p><input type='submit' value='Enviar'></p>" +
    		"</form>"
    		+ "<p>Implementada funcionalidad a espera de solucionar problemas con la base de datos</p>");
        //Incluido formulario para añadir películas
        
        post("/add_films", (req, res) -> {
        	last_added = "</p>pelicula: " + req.queryParams("film")
        		+ "</p>year: " + req.queryParams("year") 
        		+ "</p>Género: " + req.queryParams("genres")
        		+ "</p>Actor: " + req.queryParams("actor");
        	String result = "Has añadido ->" + last_added;
        	Injector.insertFilm(req.queryParams("film")
        			,req.queryParams("year"), req.queryParams("genres"));
        	//String title_ID = selectTitle_ID(connection, "movies", req.queryParams("film"), req.queryParams("year"), req.queryParams("genres"));
        	Injector.insertActor(req.queryParams("actor"));
        	//String name_ID = selectName_ID(connection, "workers", req.queryParams("actor"));
        	//insertWorks_In(connection, title_ID, name_ID);
        	return result;	
        });

        get("/showlastadded", (req, res) -> {
        	if (last_added == null) {
        		return "No se han añadido películas" +
        				"</p>Esta página mostrará más peliculas recientemente añadidas " +
        				"cuando dispongamos de acceso a la base de datos</p>";
        	}else {
        		return "<div style='color:#1A318C'><b>ÚLTIMA PELÍCULA AÑADIDA:</b></p>" +  last_added +
        				"</p>Esta página mostrará más peliculas recientemente añadidas " +
        				"cuando dispongamos de acceso a la base de datos</p>";
		}
        });

        // Recurso /filter encargado de la funcionalidad del filtrado de películas.
        get("/filter", (req, res) -> Filter.showFilterMenu());
        
        // Recurso /filter_name encargado de mostrar la info de una película dado el nombre.
        post("/filter_name", (req, res) -> Filter.showFilmByName(connector, req));
        
        // Recurso /filter_year encargado de mostrar todas las películas dado un año.
        post("/filter_year", (req, res) -> Filter.showFilmByYear(connector, req));
        
        // Recurso /filter_actoractress encargado de mostrar todas las películas
        // en las que participa un actor o una actriz.
        post("/filter_actoractress", (req, res) -> Filter.showFilmByActorActress(connector, req));

        // Recurso /filter_duration encargado de mostrar todas las películas con una 
        // duración menor a la dada
        post("/filter_duration", (req, res) -> Filter.showFilmByDuration(connector, req));

        // Recurso /filter_genre encargado de mostrar todas las películas dado un genero.
        post("/filter_genre", (req, res) -> Filter.showFilmByGenre(connector, req));

        // Recurso /filter_rating encargado de mostrar todas las películas dado una
        // valoración mínima.
        post("/filter_rating", (req, res) -> Filter.showFilmByRating(connector, req));
        
        get("/recommend", (req, res) -> slopeOneFilter.showSOMenu());
        post("/recommend", (req, res) -> slopeOneFilter.recommend(req));


        get("/distance", (req, res) -> {
        	String form = 
        		"<h3>Calculador de distancias mediante grafos</h3> " +
        		"<form action='/distance_show' method='post'>" +
    				"<div>" + 
    					"<label for='name'>Nombre del actor o película (1): </label>" +
    					"<input type='text' name='name1'/><br>" +
    					"<label for='name'>Nombre del actor o película (2): </label>" +
    					"<input type='text' name='name2'/><br>" +
    					"<button type='submit'>Enviar</button>" +
    				"</div>" +
    			"</form>" +
        		"<br><p><u>--Uso--</u></p>" + 
        		"<ul>" + 
    			  "<li>Pelicula --> (1):'NombrePeli1' | (2): 'NombrePeli2'" + 
    			  "<br>Ejemplo: 'The Great Gatsby'</li>" +
    			  "<br>" +
    			  "<li>Actor --> (1):'Nombre1 Apellido1' | (2): 'Nombre2 Apellido2'" +
    			  "<br>Ejemplo: 'Leonardo DiCaprio'</li>" +
    			"</ul>" +
        		"<p>*Nota* Si no se sabe el nombre exacto, poner una palabra (p.e. 'Leonardo' " +
    			"o 'Great' en este caso). Se ofreceran las coincidencias de esa palabra.</p>";
    		return form;
        });
        
        post("/distance_show", (req, res) -> {
        	Graph graph = new Graph("Database/film_actors.txt", "/");
    		//Graph graph = new Graph("data/other-data/moviesG.txt", "/"); para antigüa tabla
    		String name1= req.queryParams("name1");
    		String name2 = req.queryParams("name2");
    		String result = GraphFuncionality.doDistance(graph, name1, name2);    		
        	return "<p>Has buscado la distancia entre: '" + 
        			name1 + "' y '" + name2 + "'.</p>" +
        			"<p>RESULTADO:</p>" + 
        			result + 
        			"<br><a href='/'>Volver</a>";
        	
        	//EJEMPLO (con film_actors.txt):
        	//Leonardo DiCaprio | The Great Gatsby --> NAME 1
        	//Tobey Maguire | The Great Gatsby / Spiderman --> actor que relaciona
        	//Willem Dafoe (Spiderman) --> NAME 2
        	//Distancia 4
        });
        
        get("/graph_info", (req, res) -> {
        	Graph graph = new Graph("Database/film_actors.txt", "/"); //podemos poner como global?
        	String nodos = String.format("%d", graph.V());
        	String edges = String.format("%d", graph.E());
        	String maxDegree = String.format("%d", SmallWorld.maxDegree(graph));
        	String maxDegreeName = SmallWorld.maxDegreeName(graph);
        	String minDegree = String.format("%d", SmallWorld.minDegree(graph));
        	String minDegreeName = SmallWorld.minDegreeName(graph);
        	String averageDegree = String.format("%.3f", SmallWorld.averageDegree(graph));
        	//String length = String.format("%d", SmallWorld.pathLength(graph, "King Kong"));
        	//El método de arriba es muy lento computacionalmente (tarda alrededor de 1 min).
        	
        	String result = "<p>Información sobre nuestro grafo:</p>" + 
        			"<ul>" + 
        			"<li>Número de nodos (vértices) = " + nodos + "</li>" +
        			"<li>Número de enlaces (edges) = " + edges + "</li>" +
        			"<li>Grado máximo (nodo con más vecinos) = " + maxDegree + " --> " + maxDegreeName +  ".</li>" +
        			"<li>Grado mínimo (nodo con menos vecinos) = " + minDegree + " --> " + minDegreeName +  ".</li>" +
        			"<li>Grado medio = " + averageDegree + "</li>" +
        			"</ul>";
        	return result;
        });
        

        get("/graph_filter", (req, res) -> {
        	String form =
        	"<h3>Filtrado mediante grafos</h3> " +
        	"<p>Proporcione nombre de película o actor. Se obtendrán actores que han " + 
        	"trabajado en esa película, o películas en las que ha trabajado ese actor:</p>" +
    		"<form action='/graph_filter_show' method='post'>" +
				"<div>" + 
					"<label for='name'>Nombre del actor o película: </label>" +
					"<input type='text' name='name'/>" +
					"<button type='submit'>Enviar</button>" +
				"</div>" +
			"</form>" +
        	"<p>*Nota* Si no se sabe el nombre exacto, poner una palabra (p.e. 'Mark' " +
			"o 'Batman'). Se ofreceran las coincidencias de esa palabra.</p>" +
        	
			"<hr>" + 
			
			"<p>Ranking de actores. Introduzca un número. Se obtendrán actores que han " + 
			"trabajado en x o más películas, el número y los nombres de estas:</p>" +
			"<form action='/graph_filter_ranking_show' method='post'>" +
			"<div>" + 
				"<label for='name'>Número mínimo de películas: </label>" +
				"<input type='number' name='number'/>" +
				"<button type='submit'>Enviar</button>" +
			"</div>" +
			"</form>"
			;
        	return form;
        });
        
        post("/graph_filter_show", (req, res) -> {
        	Graph graph = new Graph("Database/film_actors.txt", "/");
    		String name= req.queryParams("name");
    		String result = GraphFuncionality.doGraphFilter(graph, name);
    		
        	return "<p>Has buscado los vecinos de: '" + name + ".</p>" +
        			"<p>RESULTADO:</p>" +
        			"<ul>" + 
        			result + 
        			"</ul>" +
        			"<br><a href='/'>Volver</a>";
        });
        
        post("/graph_filter_ranking_show", (req, res) -> {
        	Graph graph = new Graph("Database/film_actors.txt", "/");
    		String number= req.queryParams("number");
    		String result = "";
    		
    		if (number.equals("")) {
    			result = "<p>ERROR. Debe introducir un número en el form. Por favor, inténtalo de nuevo.</p>";
    		}else {
    			result = "<table>" + 
    				"<tr>" +
						"<th>Actor</th>" + 
						"<th>Número de películas</th>" +
					"</tr>";
	
    			for (String v : GraphFuncionality.doRanking(graph, number)) { //Iterador
    				if (graph.getter(v) == 0) {
    					result += "<tr>" + 
    							"<td>" + v + "</td>" + 
    							"<td>" + graph.degree(v) + "</td></tr>";
    				}
    			}
    			result += "</table>";
    		}
    		
        	return "<p>Has buscado actores con " + number + " o más películas.</p>" +
        			"<p>RESULTADO:</p>" +
        			result + 
        			"<br><a href='/'>Volver</a>";
        });
        
        
        get("/relatedMovies", (req, res) -> {
        	String page =
        			"<h3>Funcionalidad 'Relacionados (PELÍCULAS)' </h3> " +
        	        		"<form action='/relatedMovies_show' method='post'>" +
        	    				"<div>" + 
        	    					"<label for='name'>Nombre de la película: </label>" +
        	    					"<input type='text' name='name1'/><br>" +
        	    					"<button type='submit'>Enviar</button>" +
        	    				"</div>" +
        	    			"</form>" +
        	        		"<br><p><u>--Uso--</u></p>" + 
        	        		"<ul>" + 
        	    			  "<li>Nombre de la película: 'The Great Gatsby''" +
        	    			  "<br>" +
        	    			"</ul>" +
        	        		"<p>*Nota* Si no conoces el nombre exacto, escribe al menos una palabra (p.e. 'Spider'). " +
        	    			"Te ofreceremos las coincidencias de esa palabra.</p>" + 
                			"<br><a href='/'>Volver</a>";
        	
        	return page;
        });
        
        
        get("/relatedActors", (req, res) -> {
        	String page =
        			"<h3>Funcionalidad 'Relacionados (ACTORES)' </h3> " +
        	        		"<form action='/relatedActors_show' method='post'>" +
        	    				"<div>" + 
        	    					"<label for='name'>Nombre del actor/actriz: </label>" +
        	    					"<input type='text' name='name1'/><br>" +
        	    					"<button type='submit'>Enviar</button>" +
        	    				"</div>" +
        	    			"</form>" +
        	        		"<br><p><u>--Uso--</u></p>" + 
        	        		"<ul>" + 
        	    			  "<li>Nombre del actor/actriz: 'Angelina Jolie''" +
        	    			  "<br>" +
        	    			"</ul>" +
        	        		"<p>*Nota* Si no conoces el nombre exacto, escribe al menos una palabra (p.e. 'Angelina'). " +
        	    			"Te ofreceremos las coincidencias de esa palabra.</p>" + 
                			"<br><a href='/'>Volver</a>";
        	
        	return page;
        });
        
 
        post("/relatedMovies_show", (req, res) -> {
        	Graph graph = new Graph("Database/film_actors.txt", "/");
    		String name = req.queryParams("name1"); //name es el nombre introducido en el formulario.
    		
    		//Compruebo si se ha enviado formulario vacío.
    		if (name.equals("")) {
				return "<p>Error. No puedo ayudarte si no introduces nada.</p>" + 
    			"<br><a href='/relatedMovies'>Prueba otra vez</a>";
			}
    		
    		if (graph.hasVertex(name)) {	//Si name es vértice del grafo
        		if (graph.type(name) == 1) {	//Si name es película.
        			String page_rel_movies = GraphFuncionality.relatedMovies2(graph, name);
        		
        			return "<p>Películas cercanas en el grafo a '" + name + "' son: " + "</p>" +
        			page_rel_movies + 
            		"<br><a href='/'>Volver</a>";

        		} else {	//Si name no es película.
        			return "<p>Error. Lo que has introducido no es una película.</p>" + 
        			"<br><a href='/relatedMovies'>Prueba otra vez</a>";
        		}
    		} else {	//Si name NO es vértice del grafo.
    			String page_rel_movies = GraphFuncionality.relatedMovies2(graph, name);
    			
    			return "<p>Quizás con '" + name + "' quisiste decir: " + "</p>" +
    			page_rel_movies + 
        		"<br><a href='/relatedMovies'>Prueba otra vez</a>";
    		}
        	
        });        
        
        
        post("/relatedActors_show", (req, res) -> {
        	Graph graph = new Graph("Database/film_actors.txt", "/");
    		String name = req.queryParams("name1"); //name es el nombre introducido en el formulario.
    		
    		//Compruebo si se ha enviado formulario vacío.
    		if (name.equals("")) {
				return "<p>Error. No puedo ayudarte si no introduces nada.</p>" + 
    			"<br><a href='/relatedActors'>Prueba otra vez</a>";
			}
    		
    		if (graph.hasVertex(name)) {	//Si name es vértice del grafo
        		if (graph.type(name) == 0) {	//Si name es actor o actriz.
        			String page_rel_actors = GraphFuncionality.relatedActors(graph, name);
        		
        			return "<p>Actrices y actores cercanos en el grafo a '" + name + "' (con los que ha trabajado) son: " + "</p>" +
        			page_rel_actors + 
            		"<br><a href='/'>Volver</a>";

        		} else {	//Si name no es actor ni actriz.
        			return "<p>Error. Lo que has introducido no es un nombre de actriz ni de actor.</p>" + 
        			"<br><a href='/relatedActors'>Prueba otra vez</a>";
        		}
    		} else {	//Si name NO es vértice del grafo.
    			String page_rel_actors = GraphFuncionality.relatedActors(graph, name);
    			
    			return "<p>Quizás con '" + name + "' quisiste decir: " + "</p>" +
    			page_rel_actors + 
        		"<br><a href='/relatedActors'>Prueba otra vez</a>";
    		}
    
        	
        });
        
    }

    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
