package urjc.isi.pruebasSparkJava;

import java.net.URISyntaxException;
import java.util.List;

import spark.Request;

public class Score {
	
	
		//Guardo la nueva puntuacion    COMPLETO!!!
		public String newScore(int score, int user, String film, Injector I) {
			if (score<0 ||score>10) {
				return("Puntuación invalido");
			}else if (user<0) {
				return("Usuario invalido");
			}else if (film.equals(null)) {
				return("Pelicula invalida");
			}else {			
				I.insertUser(user);
				List<String> info_film=I.filterByName(film);
				int id_film=Integer.parseInt(info_film.get(6));
        		System.out.println(user);
        		System.out.println(score);

				I.insertRating(id_film, user, score);
			}			
			return ("Puntuacion añadida");
		}
		
		//Obtengo la nueva media    COMPLETO!!!
		public float getScore(String film, Injector I) {
			float media =I.meanScores(film);
			return media;
		}
		
		//Actualizo la media   INCOMPLETO, FALTA LA FUNCION DE BBDD
		public void changeScore(float score, String film, Injector I) {
			List<String> info_film=I.filterByName(film);
			int id_film=Integer.parseInt(info_film.get(6));
			I.updateAverageRating(id_film, score);
    		System.out.println(score);
    		System.out.println(id_film);
		}
		
		
		
		public String postScore(Request request, Injector I, SlopeOneFilter sof) {
			
			//Saco la puntuacion a int
			String score_string=request.queryParams("score");
			int score=Integer.parseInt(score_string);
			//Saco el usuario a int
			String user_string=request.queryParams("user");
			int user=Integer.parseInt(user_string);
			//Saco la pelicula.
			String film=request.queryParams("film");
			//Linea  de código para actualizar el map data de SlopeOneFilter
			sof.updateData(request, I);
			
			try {
				String result=newScore(score, user, film, I);
				float mean=getScore(film, I);
        		System.out.println(mean);
				changeScore(mean, film, I);
				return result;
			}catch(IllegalArgumentException e) {
				return e.getMessage();
			}
		}
}
