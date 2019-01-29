package urjc.isi.pruebasSparkJava;

//Clase encargada de todo lo relacionado con el filtrado.

import spark.Request;
import java.util.List;

public class Filter {

	// Método encargado de mostrar al usuario el menú con las distintas
	// opciones para filtrar (HTML devuelto al hacer GET sobre /filter).
	public static String showFilterMenu() {
		String menu = 	"<h1>Búsqueda personalizada de películas</h1>" + 
						"<p>Elija el criterio por el que desea filtrar en nuestra base de datos.</p>" +
						"<hr>" +
						"<h4>1. Filtrar por nombre de la película.</h4>" +
						"<form action='/filter_name' method='post'>" +
							"<label for='film'>Nombre de la película: </label>" + 
							"<input type='text' name='film' id='film'> " +
							"<input type='submit' value='Buscar'>" +
						"</form>" +
						"<hr>" +
						"<h4>2. Filtrar por año de estreno de la película.</h4>" +
						"<form action='/filter_year' method='post'>" +
							"<label for='year'>Año de la película: </label>" + 
							"<input type='text' name='year' id='year'> " +
							"<input type='submit' value='Buscar'>" +
						"</form>" +
						"<hr>" +
						"<h4>3. Filtrar por nombre de un actor/actriz que participa en la película.</h4>" +
						"<form action='/filter_actoractress' method='post'>" +
							"<label for='actoractress'>Actor/actriz que participa en la película: </label>" + 
							"<input type='text' name='actoractress' id='actoractress'> " +
							"<input type='submit' value='Buscar'>" +
						"</form>" +
						"<hr>" +
						"<h4>4. Filtrar por duración máxima de las películas (minutos).</h4>" +
						"<form action='/filter_duration' method='post'>" +
							"<label for='duration'>Duración máxima en minutos : </label>" + 
							"<input type='text' name='duration' id='duration'> " +
							"<input type='submit' value='Buscar'>" +
						"</form>" +
						"<hr>" +
						"<h4>5. Filtrar por género de la película.</h4>" +
						"<form action='/filter_genre' method='post'>" +
							"<label for='genre'>Género de la película: </label>" + 
							"<input type='text' name='genre' id='genre'> " +
							"<input type='submit' value='Buscar'>" +
						"</form>" +
						"<hr>" +
						"<h4>6. Filtrar por valoración mínima la película.</h4>" +
						"<form action='/filter_rating' method='post'>" +
							"<label for='rating'>Valoración mínima de la película: </label>" + 
							"<input type='text' name='rating' id='rating'> " +
							"<input type='submit' value='Buscar'>" +
						"</form>" +
						"<hr>";
		return menu;
	}

	// Método encargado de mostrar al usuario toda la información sobre
	// la película cuyo nombre ha introducido (HTML devuelto al hacer
	// POST sobre /filter_name).
	public static String showFilmByName(Injector conn, Request req) {
		List<String> movieFields;
		String response;
		
		Formulario f=new Formulario();
		Comment c=new Comment();
		
    	movieFields = conn.filterByName(req.queryParams("film"));
    	
    	if (movieFields.isEmpty()) {
    		response = "<p>Desafortunadamente, no se ha encontrado ninguna película "
    				+ "con nombre " + "'" + req.queryParams("film") + "'" + " en la "
    				+ "base de datos.</p>";
    	} else {
        	response = "<table border=2" +
						"<tr>" +
							"<th>Título</th>" +
							"<th>Año</th>" +
							"<th>Duración</th>" +
							"<th>Puntuación media</th>" +
							"<th>Número de votos</th>" +
							"<th>Géneros</th>" +
						"</tr>" +
						"<tr align='center'>" +
							"<td>" + movieFields.get(0) + "</td>" +
							"<td>" + movieFields.get(1) + "</td>" +
							"<td>" + movieFields.get(2) + " min" + "</td>" +
							"<td>" + movieFields.get(3) + "</td>" +
							"<td>" + movieFields.get(4) + "</td>" +
							"<td>" + movieFields.get(5) + "</td>" +
						"</tr>" +
					"</table>";
        	response = response+ f.formulary(movieFields.get(0));
        	response=response+c.commentsFilm(movieFields.get(0), conn);
    	}
    	return response;
	}
	
	// Método encargado de mostrar al usuario todas las películas estrenadas
	// en el año que ha introducido (HTML devuelto al hacer POST sobre /filter_year).
	public static String showFilmByYear(Injector conn, Request req) {
		List<String> movies;
		String response;
    	
    	movies = conn.filterByYear(req.queryParams("year"));

    	if (movies.isEmpty()) {
    		response = "<p>Desafortunadamente, no se ha encontrado ninguna película "
    				+ "con año de estreno " + "'" + req.queryParams("year") + "'" +
    				" en la base de datos.</p>";
    	} else {
    		response = "<table border=2" +
    						"<tr>" +
    							"<th>Año: " + req.queryParams("year") + "</th>";
    		for (int i = 0; i < movies.size(); i++) {
    			response += "<tr align='center'>" +
    							"<td>" + movies.get(i) + "</td>" +
    						"</tr>";
    		}
    	}	
    	return response;
	}
	
	// Método encargado de mostrar al usuario todas las películas en las que
	// participa el actor/actriz que ha introducido (HTML devuelto al hacer POST
	// sobre /filter_actoractress).
	public static String showFilmByActorActress(Injector conn, Request req) {
		List<String> movies;
		String response;
    	
    	movies = conn.filterByActorActress(req.queryParams("actoractress"));

    	if (movies.isEmpty()) {
    		response = "<p>Desafortunadamente, no se ha encontrado ninguna película "
    				+ "con el actor/actriz " + "'" + req.queryParams("actoractress")
    				+ "'" + " en la base de datos.</p>";
    	} else {
    		response = "<table border=2" +
    						"<tr>" +
    							"<th>Actor/Actriz: " + req.queryParams("actoractress") + "</th>";
    		for (int i = 0; i < movies.size(); i++) {
    			response += "<tr align='center'>" +
    							"<td>" + movies.get(i) + "</td>" +
    						"</tr>";
    		}
    	}	
    	return response;
	}
	
	// Método encargado de mostrar al usuario todas las películas con una duración
	// menor a la que se ha introducido (HTML devuelto al hacer POST sobre /filter_duration)
	public static String showFilmByDuration(Injector conn,Request req) {
		List<String> movies;
		String response;
    	
    	movies = conn.filterByDuration(req.queryParams("duration"));
		
    	if (movies.isEmpty()) {
    		response = "<p>Desafortunadamente, no se ha encontrado ninguna película "
    				+ "con una duración menor a " + "'" + req.queryParams("duration") + "'" +
    				"minutos en la base de datos.</p>";
    	} else {
    		response = "<table border=2" +
    						"<tr>" +
    							"<th>Duration: " + req.queryParams("duration") + "</th>";
    		for (int i = 0; i < movies.size(); i++) {
    			response += "<tr align='center'>" +
    							"<td>" + movies.get(i) + "</td>" +
    						"</tr>";
    		}
    	}	
    	return response;
	}

	// Método encargado de mostrar al usuario todas las películas con el genero
	// que se ha introducido (HTML devuelto al hacer POST sobre /filter_genre)
	public static String showFilmByGenre(Injector conn,Request req) {
		List<String> movies;
		String response;
    	
    	movies = conn.filterByGenre(req.queryParams("genre"));
		
    	if (movies.isEmpty()) {
    		response = "<p>Desafortunadamente, no se ha encontrado ninguna película "
    				+ "con un género  " + "'" + req.queryParams("genre") + "'" +
    				" en la base de datos.</p>";
    	} else {
    		response = "<table border=2" +
    						"<tr>" +
    							"<th>Género: " + req.queryParams("genre") + "</th>";
    		for (int i = 0; i < movies.size(); i++) {
    			response += "<tr align='center'>" +
    							"<td>" + movies.get(i) + "</td>" +
    						"</tr>";
    		}
    	}	
    	return response;
	}

	// Método encargado de mostrar al usuario todas las películas con una valoración
	// mayor a la que se ha introducido (HTML devuelto al hacer POST sobre /filter_rating)
	public static String showFilmByRating(Injector conn,Request req) {
		List<String> movies;
		String response;
    	
    	movies = conn.filterByRating(req.queryParams("rating"));
		
    	if (movies.isEmpty()) {
    		response = "<p>Desafortunadamente, no se ha encontrado ninguna película "
    				+ "con una valoración mayor a " + "'" + req.queryParams("rating") + "'" +
    				"en la base de datos.</p>";
    	} else {
    		response = "<table border=2" +
    						"<tr>" +
    							"<th>Valoración: " + req.queryParams("rating") + "</th>";
    		for (int i = 0; i < movies.size(); i++) {
    			response += "<tr align='center'>" +
    							"<td>" + movies.get(i) + "</td>" +
    						"</tr>";
    		}
    	}	
    	return response;
	}
}
