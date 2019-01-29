package urjc.isi.pruebasSparkJava;

import java.util.List;

import spark.Request;

public class Comment {
	//Comentarios
	
	//COMPLETO!!!
	public String newComment(String text, int user, String film, Injector I) {
		if (text==null) {
			return("Comentario invalido");
		}else if (user<0) {
			return("Usuario invalido");
		}else if (film==null) {
			return("Pelicula invalida");
		}else {			
				I.insertUser(user);
				List<String> info_film=I.filterByName(film);
				int id_film=Integer.parseInt(info_film.get(6));
				System.out.println(id_film);
				I.insertComments(id_film, user, text);
				return "Comentario almacenado";
			}
		//Obtengo id de la pelicula
		//Almaceno el nuevo comentario
	}
	
	//COMPLETO!!!
	public String commentsFilm(String film, Injector I){
		if (film == null){
			return ("Pelicula invalida");
		}
		String text = "<u><b>Comentarios:</b></u><br>";
		List<String> info_film=I.filterByName(film);
		int id_film=Integer.parseInt(info_film.get(6));
		List<String> comments=I.getFilmComments(id_film);
		if (comments.size()!=0) {
			for  (String comment :comments) {
				text=text+comment+"<br><br>";
			}
		}
		return text;
		
	}
	
	//COMPLETO!!!
	public String commentToString(String matrix_coment[][])
	{
		if (matrix_coment == null){
			return "Comentario invalido";
		}
		String text = "<u><b>Comentarios:</b></u><br>";
		for (int x = 0; x < matrix_coment.length; x++){
			String coments = " ";
			text += coments + matrix_coment[x][0] + ":" + matrix_coment[x][1]+"<br>";
		}
		text +=" ";
		return text;
	}
	
	//COMPLETO!!!
	public String postComment(Request request, Injector I) {
		String comment=request.queryParams("comment");
		System.out.println(comment);
		
		String user_string=request.queryParams("user");
		int user=Integer.parseInt(user_string);
		System.out.println(user);
		String film=request.queryParams("film");
		System.out.println(film);
		String result=newComment(comment, user, film, I);
		return result;
	}
}

